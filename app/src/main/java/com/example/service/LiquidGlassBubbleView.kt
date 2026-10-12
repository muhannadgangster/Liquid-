package com.example.service

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import com.example.data.GlassMaterialPreset
import com.example.data.GlassPreferences
import kotlin.math.hypot

@SuppressLint("ViewConstructor")
class LiquidGlassBubbleView @JvmOverloads constructor(
    context: Context,
    private val windowManager: WindowManager,
    private val layoutParams: WindowManager.LayoutParams,
    private val onBubbleClicked: () -> Unit,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var preferences: GlassPreferences = GlassPreferences()
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false

    // Shimmer animation
    private var shimmerPhase = 0f
    private var shimmerAnimator: ValueAnimator? = null

    // Paints
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val glassPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val boundsRect = RectF()
    private val innerRect = RectF()

    init {
        startShimmer()
    }

    fun updatePreferences(newPrefs: GlassPreferences) {
        this.preferences = newPrefs
        if (newPrefs.batterySaverMode) {
            shimmerAnimator?.cancel()
        } else if (shimmerAnimator?.isRunning != true) {
            startShimmer()
        }
        invalidate()
    }

    private fun startShimmer() {
        shimmerAnimator?.cancel()
        shimmerAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = (3000L / preferences.animationSpeed.coerceAtLeast(0.5f)).toLong()
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                shimmerPhase = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        shimmerAnimator?.cancel()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        val radius = w / 2f
        val cx = radius
        val cy = radius

        val preset = preferences.preset
        val transparency = preferences.transparency
        val glow = preferences.glowIntensity
        val thickness = preferences.glassThickness

        // 1. Soft Outer Neon / Refraction Glow
        if (!preferences.batterySaverMode && glow > 0.05f) {
            glowPaint.strokeWidth = 6f * glow
            val glowColor = (preset.accentColorHex and 0x00FFFFFF) or ((glow * 0.45f * 255).toInt().toLong() shl 24)
            glowPaint.color = glowColor.toInt()
            canvas.drawCircle(cx, cy, radius - 4f, glowPaint)
        }

        // 2. Translucent Frosted Glass Base
        val baseAlpha = ((1f - transparency) * 255).toInt().coerceIn(40, 240)
        val pColor = preset.primaryColorHex.toInt()
        val sColor = preset.secondaryColorHex.toInt()

        val glassShader = RadialGradient(
            cx - radius * 0.3f,
            cy - radius * 0.3f,
            radius * 1.2f,
            intArrayOf(
                adjustAlpha(pColor, baseAlpha),
                adjustAlpha(sColor, (baseAlpha * 1.1f).toInt().coerceAtMost(255)),
                adjustAlpha(Color.BLACK, (baseAlpha * 0.7f).toInt())
            ),
            floatArrayOf(0f, 0.65f, 1f),
            Shader.TileMode.CLAMP
        )
        glassPaint.shader = glassShader
        canvas.drawCircle(cx, cy, radius - 5f, glassPaint)

        // 3. Specular Glare / Liquid Shimmer Reflection (Glossy Glass Highlight)
        val glareWidth = radius * 1.4f
        val glareX = (cx - radius * 0.5f) + (shimmerPhase * radius * 0.6f)
        val highlightShader = LinearGradient(
            glareX - glareWidth * 0.5f,
            cy - radius,
            glareX + glareWidth * 0.5f,
            cy + radius,
            intArrayOf(
                adjustAlpha(Color.WHITE, 0),
                adjustAlpha(Color.WHITE, (preferences.reflectionIntensity * 160).toInt()),
                adjustAlpha(preset.accentColorHex.toInt(), (preferences.reflectionIntensity * 90).toInt()),
                adjustAlpha(Color.WHITE, 0)
            ),
            floatArrayOf(0f, 0.4f, 0.6f, 1f),
            Shader.TileMode.CLAMP
        )
        highlightPaint.shader = highlightShader
        canvas.drawCircle(cx, cy, radius - 6f, highlightPaint)

        // 4. Glass Edge Refraction / Beveled Border
        borderPaint.strokeWidth = thickness
        val borderShader = LinearGradient(
            0f, 0f, w, h,
            intArrayOf(
                adjustAlpha(Color.WHITE, 220),
                adjustAlpha(preset.refractionColorHex.toInt(), 180),
                adjustAlpha(preset.accentColorHex.toInt(), 140),
                adjustAlpha(Color.WHITE, 80)
            ),
            floatArrayOf(0f, 0.35f, 0.75f, 1f),
            Shader.TileMode.CLAMP
        )
        borderPaint.shader = borderShader
        canvas.drawCircle(cx, cy, radius - 5f, borderPaint)

        // 5. Stylized Liquid Glass Center Symbol (Glowing droplets / aperture)
        val iconSize = radius * 0.38f
        iconPaint.strokeWidth = 3.5f
        iconPaint.color = adjustAlpha(Color.WHITE, 230)
        canvas.drawCircle(cx, cy, iconSize * 0.6f, iconPaint)

        // Mini accent droplet
        iconPaint.strokeWidth = 2.5f
        iconPaint.color = preset.accentColorHex.toInt()
        canvas.drawCircle(cx + iconSize * 0.7f, cy - iconSize * 0.5f, iconSize * 0.25f, iconPaint)
    }

    private fun adjustAlpha(color: Int, alpha: Int): Int {
        val safeAlpha = alpha.coerceIn(0, 255)
        return (color and 0x00FFFFFF) or (safeAlpha shl 24)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = layoutParams.x
                initialY = layoutParams.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                isDragging = false
                triggerHaptic()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - initialTouchX
                val dy = event.rawY - initialTouchY
                if (!isDragging && hypot(dx, dy) > touchSlop) {
                    isDragging = true
                }
                if (isDragging) {
                    layoutParams.x = (initialX + dx).toInt()
                    layoutParams.y = (initialY + dy).toInt()
                    try {
                        windowManager.updateViewLayout(this, layoutParams)
                    } catch (e: Exception) {
                        // View may be detached
                    }
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (!isDragging) {
                    triggerHaptic()
                    onBubbleClicked()
                } else if (preferences.edgeSnapping) {
                    snapToEdge()
                }
                isDragging = false
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun snapToEdge() {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val currentX = layoutParams.x
        val targetX = if (currentX + width / 2 < screenWidth / 2) {
            16
        } else {
            screenWidth - width - 16
        }

        ValueAnimator.ofInt(currentX, targetX).apply {
            duration = 200L
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                layoutParams.x = it.animatedValue as Int
                try {
                    windowManager.updateViewLayout(this@LiquidGlassBubbleView, layoutParams)
                } catch (e: Exception) {
                    // Detached
                }
            }
            start()
        }
    }

    private fun triggerHaptic() {
        if (!preferences.hapticsEnabled) return
        try {
            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v?.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v?.vibrate(18)
            }
        } catch (e: Exception) {
            // Ignore
        }
    }
}

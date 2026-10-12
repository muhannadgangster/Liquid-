package com.example.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.example.MainActivity
import com.example.data.GlassMaterialPreset
import com.example.data.GlassPreferences

@SuppressLint("ViewConstructor")
class LiquidGlassExpandedPanelView @JvmOverloads constructor(
    context: Context,
    private val onClose: () -> Unit,
    private val onEmergencyStop: () -> Unit,
    private val onPresetSelected: (GlassMaterialPreset) -> Unit,
    private val onTransparencyChanged: (Float) -> Unit,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var preferences = GlassPreferences()
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private var isTorchOn = false
    private var cameraId: String? = null

    // UI elements
    private val panelCard: LinearLayout
    private val titleText: TextView
    private val presetBadgeText: TextView
    private val volumeSlider: SeekBar
    private val volumeText: TextView
    private val torchButton: Button
    private val scratchpadInput: EditText
    private val presetsContainer: LinearLayout

    init {
        // Scrim background to detect touches outside panel
        setBackgroundColor(Color.argb(80, 0, 0, 0))
        setOnClickListener {
            onClose()
        }

        // Camera init for torch
        try {
            cameraId = cameraManager?.cameraIdList?.firstOrNull()
        } catch (e: Exception) {
            // Ignore
        }

        // Panel Container
        panelCard = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val pad = dp(18)
            setPadding(pad, pad, pad, pad)
            // Prevent clicks on the card from propagating to the scrim
            setOnClickListener { /* Consume */ }
        }

        val cardParams = LayoutParams(
            dp(330),
            LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }
        addView(panelCard, cardParams)

        // 1. Header Row (Title, Preset, Emergency Stop, Close)
        val headerRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val titleContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        titleText = TextView(context).apply {
            text = "Liquid Glass"
            textSize = 17f
            setTextColor(Color.WHITE)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        presetBadgeText = TextView(context).apply {
            text = "Active Preset"
            textSize = 11f
            setTextColor(Color.argb(200, 147, 197, 253))
        }
        titleContainer.addView(titleText)
        titleContainer.addView(presetBadgeText)
        headerRow.addView(titleContainer)

        // Emergency Stop Button
        val stopButton = Button(context).apply {
            text = "Stop"
            textSize = 10f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.argb(160, 220, 38, 38))
            setPadding(dp(8), dp(4), dp(8), dp(4))
            val btnParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(34))
            btnParams.setMargins(0, 0, dp(6), 0)
            layoutParams = btnParams
            setOnClickListener {
                triggerHaptic()
                onEmergencyStop()
            }
        }
        headerRow.addView(stopButton)

        // Close Button
        val closeButton = Button(context).apply {
            text = "✕"
            textSize = 13f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.argb(80, 255, 255, 255))
            val btnParams = LinearLayout.LayoutParams(dp(34), dp(34))
            layoutParams = btnParams
            setOnClickListener {
                triggerHaptic()
                onClose()
            }
        }
        headerRow.addView(closeButton)
        panelCard.addView(headerRow)

        // Divider
        panelCard.addView(createDivider())

        // 2. Quick Media Volume Control
        val volHeaderRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val volLabel = TextView(context).apply {
            text = "Quick Volume"
            textSize = 12f
            setTextColor(Color.argb(220, 255, 255, 255))
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        volumeText = TextView(context).apply {
            textSize = 12f
            setTextColor(Color.WHITE)
        }
        volHeaderRow.addView(volLabel)
        volHeaderRow.addView(volumeText)
        panelCard.addView(volHeaderRow)

        val maxVol = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
        val curVol = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 8
        volumeText.text = "$curVol / $maxVol"

        volumeSlider = SeekBar(context).apply {
            max = maxVol
            progress = curVol
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        try {
                            audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
                            volumeText.text = "$progress / $maxVol"
                        } catch (e: Exception) {
                            // Ignore
                        }
                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        panelCard.addView(volumeSlider)

        // 3. System Tools Row (Torch Toggle & Open Dashboard)
        val toolsRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            val p = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            p.setMargins(0, dp(6), 0, dp(6))
            layoutParams = p
        }

        torchButton = Button(context).apply {
            text = "Torch: OFF"
            textSize = 11f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.argb(100, 75, 85, 99))
            layoutParams = LinearLayout.LayoutParams(0, dp(38), 1f).apply {
                setMargins(0, 0, dp(4), 0)
            }
            setOnClickListener {
                toggleTorch()
            }
        }
        toolsRow.addView(torchButton)

        val openAppBtn = Button(context).apply {
            text = "Open App"
            textSize = 11f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.argb(120, 37, 99, 235))
            layoutParams = LinearLayout.LayoutParams(0, dp(38), 1f).apply {
                setMargins(dp(4), 0, 0, 0)
            }
            setOnClickListener {
                triggerHaptic()
                val intent = Intent(context, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                context.startActivity(intent)
                onClose()
            }
        }
        toolsRow.addView(openAppBtn)
        panelCard.addView(toolsRow)

        // 4. Glass Presets Picker (Horizontal Scroll)
        val presetSectionLabel = TextView(context).apply {
            text = "Refraction Preset"
            textSize = 11f
            setTextColor(Color.argb(200, 209, 213, 219))
            val p = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            p.setMargins(0, dp(4), 0, dp(4))
            layoutParams = p
        }
        panelCard.addView(presetSectionLabel)

        val presetScrollView = HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
        }
        presetsContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        populatePresetButtons()
        presetScrollView.addView(presetsContainer)
        panelCard.addView(presetScrollView)

        // 5. Mini Liquid Note Scratchpad
        val noteSectionLabel = TextView(context).apply {
            text = "Quick Glass Note (On-Device)"
            textSize = 11f
            setTextColor(Color.argb(200, 209, 213, 219))
            val p = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            p.setMargins(0, dp(8), 0, dp(4))
            layoutParams = p
        }
        panelCard.addView(noteSectionLabel)

        scratchpadInput = EditText(context).apply {
            hint = "Type temporary notes floating above apps..."
            setHintTextColor(Color.argb(120, 255, 255, 255))
            setTextColor(Color.WHITE)
            textSize = 12f
            setBackgroundColor(Color.argb(60, 0, 0, 0))
            setPadding(dp(10), dp(8), dp(10), dp(8))
            maxLines = 3
        }
        panelCard.addView(scratchpadInput)

        applyGlassStyling()
    }

    private fun populatePresetButtons() {
        presetsContainer.removeAllViews()
        GlassMaterialPreset.entries.forEach { p ->
            val btn = Button(context).apply {
                text = p.displayName.substringBefore(" ")
                textSize = 10f
                setTextColor(Color.WHITE)
                val isSelected = p == preferences.preset
                val bgAlpha = if (isSelected) 180 else 70
                setBackgroundColor(Color.argb(bgAlpha, (p.primaryColorHex ushr 16 and 0xFF).toInt(), (p.primaryColorHex ushr 8 and 0xFF).toInt(), (p.primaryColorHex and 0xFF).toInt()))
                val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(32))
                lp.setMargins(0, 0, dp(4), 0)
                layoutParams = lp
                setOnClickListener {
                    triggerHaptic()
                    onPresetSelected(p)
                }
            }
            presetsContainer.addView(btn)
        }
    }

    private fun toggleTorch() {
        triggerHaptic()
        val cId = cameraId ?: return
        try {
            isTorchOn = !isTorchOn
            cameraManager?.setTorchMode(cId, isTorchOn)
            torchButton.text = if (isTorchOn) "Torch: ON" else "Torch: OFF"
            torchButton.setBackgroundColor(if (isTorchOn) Color.argb(180, 234, 179, 8) else Color.argb(100, 75, 85, 99))
        } catch (e: Exception) {
            // Some devices don't allow torch when camera is opened
        }
    }

    fun updatePreferences(newPrefs: GlassPreferences) {
        this.preferences = newPrefs
        presetBadgeText.text = newPrefs.preset.displayName
        populatePresetButtons()
        applyGlassStyling()
    }

    private fun applyGlassStyling() {
        val preset = preferences.preset
        val alpha = ((1f - preferences.transparency) * 255).toInt().coerceIn(120, 245)
        val corner = dp(preferences.cornerRadius.toInt()).toFloat()

        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = corner
            val col1 = Color.argb(alpha, 15, 23, 42) // deep navy
            val col2 = Color.argb(alpha, (preset.primaryColorHex ushr 16 and 0xFF).toInt(), (preset.primaryColorHex ushr 8 and 0xFF).toInt(), (preset.primaryColorHex and 0xFF).toInt())
            colors = intArrayOf(col1, col2)
            orientation = GradientDrawable.Orientation.TOP_BOTTOM
            setStroke(dp(preferences.glassThickness.toInt().coerceIn(1, 4)), Color.argb(180, 255, 255, 255))
        }
        panelCard.background = drawable
    }

    private fun createDivider(): View {
        return View(context).apply {
            val p = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1))
            p.setMargins(0, dp(10), 0, dp(10))
            layoutParams = p
            setBackgroundColor(Color.argb(40, 255, 255, 255))
        }
    }

    private fun dp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            resources.displayMetrics
        ).toInt()
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

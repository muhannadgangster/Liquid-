package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GlassMaterialPreset
import com.example.data.GlassPreferences
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassTextPrimary
import com.example.ui.theme.SurfaceDarkGlass

@Composable
fun Modifier.liquidGlass(
    preferences: GlassPreferences,
    cornerRadius: Dp = preferences.cornerRadius.dp,
    animateGlare: Boolean = !preferences.batterySaverMode
): Modifier {
    val preset = preferences.preset
    val trans = preferences.transparency
    val reflection = preferences.reflectionIntensity
    val glow = preferences.glowIntensity
    val thickness = preferences.glassThickness

    val infiniteTransition = rememberInfiniteTransition(label = "LiquidGlassGlare")
    val glareOffset by if (animateGlare) {
        val durationMs = (3200f / preferences.animationSpeed.coerceAtLeast(0.5f)).toInt()
        infiniteTransition.animateFloat(
            initialValue = -0.4f,
            targetValue = 1.4f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = durationMs, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "GlareOffset"
        )
    } else {
        remember { androidx.compose.runtime.mutableFloatStateOf(0.4f) }
    }

    return this.drawBehind {
        val w = size.width
        val h = size.height
        val radiusPx = cornerRadius.toPx()

        // 1. Soft Outer Refraction Glow
        if (glow > 0.05f) {
            val glowColor = Color(preset.accentColorHex).copy(alpha = glow * 0.22f)
            drawRoundRect(
                color = glowColor,
                topLeft = Offset(-4f, -4f),
                size = Size(w + 8f, h + 8f),
                cornerRadius = CornerRadius(radiusPx + 4f, radiusPx + 4f),
                style = Stroke(width = 8f * glow)
            )
        }

        // 2. Translucent Frosted Glass Base
        val baseAlpha = ((1f - trans) * 0.85f).coerceIn(0.15f, 0.95f)
        val pColor = Color(preset.primaryColorHex).copy(alpha = baseAlpha)
        val sColor = Color(preset.secondaryColorHex).copy(alpha = (baseAlpha * 0.9f).coerceAtMost(0.95f))
        val darkBack = SurfaceDarkGlass.copy(alpha = (baseAlpha * 0.8f))

        val bgBrush = Brush.linearGradient(
            colors = listOf(pColor, sColor, darkBack),
            start = Offset(0f, 0f),
            end = Offset(w, h)
        )
        drawRoundRect(
            brush = bgBrush,
            size = size,
            cornerRadius = CornerRadius(radiusPx, radiusPx)
        )

        // 3. Specular Glare / Liquid Shimmer Reflection
        if (reflection > 0.05f) {
            val glareCenter = w * glareOffset
            val glareBrush = Brush.linearGradient(
                0.0f to Color.Transparent,
                0.35f to Color.White.copy(alpha = 0.28f * reflection),
                0.55f to Color(preset.accentColorHex).copy(alpha = 0.20f * reflection),
                1.0f to Color.Transparent,
                start = Offset(glareCenter - w * 0.4f, 0f),
                end = Offset(glareCenter + w * 0.4f, h)
            )
            drawRoundRect(
                brush = glareBrush,
                size = size,
                cornerRadius = CornerRadius(radiusPx, radiusPx)
            )
        }

        // 4. Refraction Bevel Border
        val borderBrush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.75f),
                Color(preset.refractionColorHex).copy(alpha = 0.60f),
                Color(preset.accentColorHex).copy(alpha = 0.45f),
                Color.White.copy(alpha = 0.20f)
            ),
            start = Offset(0f, 0f),
            end = Offset(w, h)
        )
        drawRoundRect(
            brush = borderBrush,
            size = size,
            cornerRadius = CornerRadius(radiusPx, radiusPx),
            style = Stroke(width = thickness.coerceIn(1f, 6f))
        )
    }
}

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    preferences: GlassPreferences,
    cornerRadius: Dp = preferences.cornerRadius.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .liquidGlass(preferences, cornerRadius)
            .padding(16.dp),
        content = content
    )
}

@Composable
fun LiquidGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    preferences: GlassPreferences,
    icon: (@Composable () -> Unit)? = null,
    testTag: String = "liquid_glass_button"
) {
    val context = LocalContext.current
    val preset = preferences.preset

    Box(
        modifier = modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(preferences.cornerRadius.dp / 1.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color(preset.accentColorHex))
            ) {
                triggerHaptic(context, preferences)
                onClick()
            }
            .liquidGlass(
                preferences = preferences,
                cornerRadius = preferences.cornerRadius.dp / 1.5f
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = GlassTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun LiquidGlassBadge(
    text: String,
    preset: GlassMaterialPreset,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(Color(preset.primaryColorHex).copy(alpha = 0.35f))
            .border(1.dp, Color(preset.refractionColorHex).copy(alpha = 0.6f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(Color(preset.accentColorHex))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = GlassTextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun triggerHaptic(context: Context, preferences: GlassPreferences) {
    if (!preferences.hapticsEnabled) return
    try {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(16, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(16)
        }
    } catch (e: Exception) {
        // Ignore
    }
}

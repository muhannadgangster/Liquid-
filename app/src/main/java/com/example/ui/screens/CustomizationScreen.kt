package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GlassMaterialPreset
import com.example.data.GlassPreferences
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.GlassTextMuted
import com.example.ui.theme.GlassTextPrimary
import com.example.ui.theme.GlassTextSecondary

@Composable
fun CustomizationScreen(
    preferences: GlassPreferences,
    onUpdateTransparency: (Float) -> Unit,
    onUpdateBlur: (Float) -> Unit,
    onUpdateReflection: (Float) -> Unit,
    onUpdateGlow: (Float) -> Unit,
    onUpdateSpeed: (Float) -> Unit,
    onUpdateThickness: (Float) -> Unit,
    onUpdateCornerRadius: (Float) -> Unit,
    onUpdatePreset: (GlassMaterialPreset) -> Unit,
    onUpdateThemeMode: (String) -> Unit,
    onUpdateBatterySaver: (Boolean) -> Unit,
    onUpdateHaptics: (Boolean) -> Unit,
    onResetDefaults: () -> Unit
) {
    val scrollState = rememberScrollState()
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Presets Gallery
        LiquidGlassCard(
            preferences = preferences,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = null,
                            tint = Color(preferences.preset.accentColorHex),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Material Refraction Presets",
                            color = GlassTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Select high-fidelity liquid shaders and color refractions:",
                    color = GlassTextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                GlassMaterialPreset.entries.forEach { preset ->
                    val isSelected = preset == preferences.preset
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.03f))
                            .border(
                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                color = if (isSelected) Color(preset.accentColorHex) else Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onUpdatePreset(preset) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Preset color swatch
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(preset.primaryColorHex).copy(alpha = 0.9f))
                                .border(2.dp, Color(preset.refractionColorHex), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = preset.displayName,
                                color = GlassTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = preset.description,
                                color = GlassTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // 2. Optical Glass Physics Sliders
        LiquidGlassCard(
            preferences = preferences,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Optical Physics & Refraction",
                    color = GlassTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                // Transparency Slider
                SliderSettingItem(
                    label = "Glass Transparency",
                    valueText = "${((preferences.transparency) * 100).toInt()}%",
                    value = preferences.transparency,
                    valueRange = 0.10f..0.85f,
                    activeColor = Color(preferences.preset.accentColorHex),
                    testTag = "transparency_slider",
                    onValueChange = onUpdateTransparency
                )

                // Blur Intensity
                SliderSettingItem(
                    label = "Frosted Blur Simulation",
                    valueText = "${(preferences.blurIntensity * 100).toInt()}%",
                    value = preferences.blurIntensity,
                    valueRange = 0f..1f,
                    activeColor = Color(preferences.preset.accentColorHex),
                    testTag = "blur_slider",
                    onValueChange = onUpdateBlur
                )

                // Specular Glare / Reflection
                SliderSettingItem(
                    label = "Glossy Reflection & Glare",
                    valueText = "${(preferences.reflectionIntensity * 100).toInt()}%",
                    value = preferences.reflectionIntensity,
                    valueRange = 0f..1f,
                    activeColor = Color(preferences.preset.accentColorHex),
                    testTag = "reflection_slider",
                    onValueChange = onUpdateReflection
                )

                // Outer Neon / Refractive Glow
                SliderSettingItem(
                    label = "Chromatic Glow Intensity",
                    valueText = "${(preferences.glowIntensity * 100).toInt()}%",
                    value = preferences.glowIntensity,
                    valueRange = 0f..1f,
                    activeColor = Color(preferences.preset.accentColorHex),
                    testTag = "glow_slider",
                    onValueChange = onUpdateGlow
                )

                // Fluid Animation Speed
                SliderSettingItem(
                    label = "Fluid Animation Speed",
                    valueText = "${String.format("%.1f", preferences.animationSpeed)}x",
                    value = preferences.animationSpeed,
                    valueRange = 0.5f..2.0f,
                    activeColor = Color(preferences.preset.accentColorHex),
                    testTag = "speed_slider",
                    onValueChange = onUpdateSpeed
                )

                // Glass Thickness (Bevel outline)
                SliderSettingItem(
                    label = "Glass Bevel Thickness",
                    valueText = "${preferences.glassThickness.toInt()} px",
                    value = preferences.glassThickness,
                    valueRange = 1f..6f,
                    activeColor = Color(preferences.preset.accentColorHex),
                    testTag = "thickness_slider",
                    onValueChange = onUpdateThickness
                )

                // Corner Radius
                SliderSettingItem(
                    label = "Corner Curvature Radius",
                    valueText = "${preferences.cornerRadius.toInt()} dp",
                    value = preferences.cornerRadius,
                    valueRange = 8f..36f,
                    activeColor = Color(preferences.preset.accentColorHex),
                    testTag = "corner_radius_slider",
                    onValueChange = onUpdateCornerRadius
                )
            }
        }

        // 3. Theme & Performance Settings
        LiquidGlassCard(
            preferences = preferences,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "System Adaptation & Performance",
                    color = GlassTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                // Theme Mode Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("DARK" to "Dark Glass", "LIGHT" to "Crystal Light", "SYSTEM" to "Auto Sync").forEach { (mode, title) ->
                        val isCurrent = preferences.themeMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isCurrent) Color(preferences.preset.accentColorHex).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f))
                                .border(1.dp, if (isCurrent) Color(preferences.preset.accentColorHex) else Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                                .clickable { onUpdateThemeMode(mode) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                color = if (isCurrent) Color.White else GlassTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Battery Saver Mode Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BatteryChargingFull,
                            contentDescription = null,
                            tint = Color(preferences.preset.accentColorHex),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Battery-Saving Mode",
                                color = GlassTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Disables active shimmer loop for ultra-low battery",
                                color = GlassTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Switch(
                        checked = preferences.batterySaverMode,
                        onCheckedChange = onUpdateBatterySaver,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(preferences.preset.accentColorHex)
                        )
                    )
                }

                // Haptic Feedback Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = Color(preferences.preset.accentColorHex),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Tactile Glass Haptics",
                                color = GlassTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Gentle micro-vibration on touching glass panels",
                                color = GlassTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Switch(
                        checked = preferences.hapticsEnabled,
                        onCheckedChange = onUpdateHaptics,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(preferences.preset.accentColorHex)
                        )
                    )
                }
            }
        }

        // 4. Reset to Default Button
        LiquidGlassButton(
            text = "Restore Factory Glass Presets",
            preferences = preferences.copy(glassThickness = 1.5f),
            modifier = Modifier.fillMaxWidth(),
            testTag = "reset_defaults_button",
            icon = {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            },
            onClick = { showResetDialog = true }
        )

        Spacer(modifier = Modifier.height(20.dp))
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset All Settings?") },
            text = { Text("This will restore default transparency, blur, refraction shaders, and animation speeds.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetDefaults()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset", color = EmergencyRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SliderSettingItem(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    activeColor: Color,
    testTag: String,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = GlassTextPrimary,
                fontSize = 13.sp
            )
            Text(
                text = valueText,
                color = activeColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.testTag(testTag),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = activeColor,
                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
            )
        )
    }
}

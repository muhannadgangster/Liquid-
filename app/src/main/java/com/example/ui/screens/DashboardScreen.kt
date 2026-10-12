package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GlassPreferences
import com.example.service.LiquidGlassOverlayService
import com.example.ui.components.LiquidGlassBadge
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DeepSpaceNavy
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassTextMuted
import com.example.ui.theme.GlassTextPrimary
import com.example.ui.theme.GlassTextSecondary
import com.example.ui.theme.SuccessEmerald
import com.example.ui.theme.WarningAmber

@Composable
fun DashboardScreen(
    preferences: GlassPreferences,
    onToggleMaster: (Boolean) -> Unit,
    onToggleOverlay: (Boolean) -> Unit,
    onEmergencyStop: () -> Unit,
    onNavigateToCustomization: () -> Unit,
    onNavigateToOverlaySettings: () -> Unit
) {
    val context = LocalContext.current
    val hasOverlayPermission = Settings.canDrawOverlays(context)
    val scrollState = rememberScrollState()

    // Sandbox interactive state
    var sandboxSwitch by remember { mutableStateOf(true) }
    var sandboxSlider by remember { mutableFloatStateOf(0.65f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Liquid Glass Header Card
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
                    Column {
                        Text(
                            text = "Liquid Glass Everywhere",
                            color = GlassTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Translucent System-Wide Companion",
                            color = GlassTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    LiquidGlassBadge(
                        text = preferences.preset.displayName,
                        preset = preferences.preset
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Master Toggle Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (preferences.masterEnabled) CyberCyan.copy(alpha = 0.25f) else Color.Gray.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Master Switch",
                                tint = if (preferences.masterEnabled) CyberCyan else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Master Engine",
                                color = GlassTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (preferences.masterEnabled) "Liquid visual engine active" else "Engine disabled",
                                color = GlassTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = preferences.masterEnabled,
                        onCheckedChange = { onToggleMaster(it) },
                        modifier = Modifier.testTag("master_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CyberCyan,
                            uncheckedTrackColor = Color.DarkGray
                        )
                    )
                }
            }
        }

        // 2. Permission Banner if missing
        if (!hasOverlayPermission) {
            LiquidGlassCard(
                preferences = preferences.copy(glassThickness = 2.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Permission Warning",
                        tint = WarningAmber,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Overlay Permission Required",
                            color = GlassTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Grant 'Display over other apps' to allow floating liquid glass controls over other applications.",
                            color = GlassTextSecondary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LiquidGlassButton(
                            text = "Grant Overlay Permission",
                            preferences = preferences,
                            testTag = "grant_overlay_permission_button",
                            onClick = {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }

        // 3. Floating Overlay Service Controller Card
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
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Overlay Status",
                            tint = if (preferences.overlayEnabled && hasOverlayPermission) SuccessEmerald else WarningAmber,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "System Overlay Engine",
                            color = GlassTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = if (preferences.overlayEnabled && hasOverlayPermission) "ACTIVE OVER APPS" else "STANDBY",
                        color = if (preferences.overlayEnabled && hasOverlayPermission) SuccessEmerald else GlassTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "A native Android WindowManager overlay floats over supported apps, offering draggable glass bubbles, media sliders, and quick tools without intercepting normal taps.",
                    color = GlassTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LiquidGlassButton(
                        text = if (preferences.overlayEnabled) "Stop Overlay" else "Start Overlay",
                        preferences = preferences,
                        modifier = Modifier.weight(1f),
                        testTag = "toggle_overlay_button",
                        icon = {
                            Icon(
                                imageVector = if (preferences.overlayEnabled) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        onClick = {
                            if (!hasOverlayPermission) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            } else {
                                val newState = !preferences.overlayEnabled
                                onToggleOverlay(newState)
                                if (newState) {
                                    LiquidGlassOverlayService.start(context)
                                } else {
                                    LiquidGlassOverlayService.stop(context)
                                }
                            }
                        }
                    )

                    // Emergency Stop Button
                    LiquidGlassButton(
                        text = "Emergency Stop",
                        preferences = preferences.copy(glassThickness = 2f),
                        modifier = Modifier.weight(1f),
                        testTag = "emergency_stop_button",
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = EmergencyRed,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        onClick = {
                            onEmergencyStop()
                            LiquidGlassOverlayService.stop(context)
                        }
                    )
                }
            }
        }

        // 4. Live Liquid Glass Sandbox (Interactive Glass Playground)
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
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = "Sandbox",
                            tint = CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Interactive Glass Sandbox",
                            color = GlassTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "LIVE PREVIEW",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Test refractive glass controls, specular glare, and tactile haptic response in real-time below:",
                    color = GlassTextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive Switch inside sandbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Fluid Specular Highlight",
                        color = GlassTextPrimary,
                        fontSize = 13.sp
                    )
                    Switch(
                        checked = sandboxSwitch,
                        onCheckedChange = { sandboxSwitch = it },
                        modifier = Modifier.testTag("sandbox_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(preferences.preset.accentColorHex)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Interactive Slider inside sandbox
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Refraction Depth Level",
                            color = GlassTextPrimary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${(sandboxSlider * 100).toInt()}%",
                            color = Color(preferences.preset.accentColorHex),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = sandboxSlider,
                        onValueChange = { sandboxSlider = it },
                        modifier = Modifier.testTag("sandbox_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color(preferences.preset.accentColorHex),
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }

        // 5. Quick Shortcuts to Customize & Overlay Settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LiquidGlassButton(
                text = "Visual Engine Settings",
                preferences = preferences,
                modifier = Modifier.weight(1f),
                testTag = "nav_customization_button",
                icon = {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                },
                onClick = onNavigateToCustomization
            )

            LiquidGlassButton(
                text = "Overlay Behavior",
                preferences = preferences,
                modifier = Modifier.weight(1f),
                testTag = "nav_overlay_settings_button",
                icon = {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                },
                onClick = onNavigateToOverlaySettings
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

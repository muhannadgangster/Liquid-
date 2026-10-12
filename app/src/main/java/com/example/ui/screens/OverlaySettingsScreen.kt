package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.GlassTextMuted
import com.example.ui.theme.GlassTextPrimary
import com.example.ui.theme.GlassTextSecondary
import com.example.ui.theme.WarningAmber

@Composable
fun OverlaySettingsScreen(
    preferences: GlassPreferences,
    onToggleOverlay: (Boolean) -> Unit,
    onUpdateBubbleSize: (Int) -> Unit,
    onToggleEdgeSnapping: (Boolean) -> Unit,
    onToggleShowQuickTools: (Boolean) -> Unit,
    onToggleShowQuickApps: (Boolean) -> Unit,
    onEmergencyStop: () -> Unit
) {
    val context = LocalContext.current
    val hasOverlayPermission = Settings.canDrawOverlays(context)
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Overlay Engine Master Switch
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Floating Overlay Engine",
                            color = GlassTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Renders floating liquid glass controls and panels above other apps",
                            color = GlassTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = preferences.overlayEnabled && hasOverlayPermission,
                        onCheckedChange = { enable ->
                            if (!hasOverlayPermission) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            } else {
                                onToggleOverlay(enable)
                                if (enable) {
                                    LiquidGlassOverlayService.start(context)
                                } else {
                                    LiquidGlassOverlayService.stop(context)
                                }
                            }
                        },
                        modifier = Modifier.testTag("overlay_master_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(preferences.preset.accentColorHex)
                        )
                    )
                }
            }
        }

        // 2. Movable Bubble Behavior
        LiquidGlassCard(
            preferences = preferences,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Floating Bubble Mechanics",
                    color = GlassTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                // Bubble Size Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Bubble Dimension",
                            color = GlassTextPrimary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${preferences.bubbleSizeDp} dp",
                            color = Color(preferences.preset.accentColorHex),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = preferences.bubbleSizeDp.toFloat(),
                        onValueChange = { onUpdateBubbleSize(it.toInt()) },
                        valueRange = 44f..72f,
                        steps = 6,
                        modifier = Modifier.testTag("bubble_size_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color(preferences.preset.accentColorHex)
                        )
                    )
                }

                // Edge Snapping Switch
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
                            imageVector = Icons.Default.NearMe,
                            contentDescription = null,
                            tint = Color(preferences.preset.accentColorHex),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Magnetic Edge Snapping",
                                color = GlassTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Gently snaps floating bubble to left or right screen border on release",
                                color = GlassTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Switch(
                        checked = preferences.edgeSnapping,
                        onCheckedChange = onToggleEdgeSnapping,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(preferences.preset.accentColorHex)
                        )
                    )
                }
            }
        }

        // 3. Floating Quick Tools Configuration
        LiquidGlassCard(
            preferences = preferences,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Companion Shade Quick Tools",
                    color = GlassTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Configure utilities available inside the expanded liquid glass shade over apps:",
                    color = GlassTextSecondary,
                    fontSize = 12.sp
                )

                // Tool 1: Media Volume Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = Color(preferences.preset.accentColorHex),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Interactive Audio Volume Bar",
                            color = GlassTextPrimary,
                            fontSize = 13.sp
                        )
                    }
                    Switch(
                        checked = preferences.showQuickToolsInOverlay,
                        onCheckedChange = onToggleShowQuickTools,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(preferences.preset.accentColorHex)
                        )
                    )
                }

                // Tool 2: Torch & Notes
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Note,
                            contentDescription = null,
                            tint = Color(preferences.preset.accentColorHex),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Torch Toggle & Glass Scratchpad",
                            color = GlassTextPrimary,
                            fontSize = 13.sp
                        )
                    }
                    Switch(
                        checked = preferences.showQuickAppsInOverlay,
                        onCheckedChange = onToggleShowQuickApps,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(preferences.preset.accentColorHex)
                        )
                    )
                }
            }
        }

        // 4. Touch Passthrough & Non-Blocking Design Explanation
        LiquidGlassCard(
            preferences = preferences,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PanTool,
                        contentDescription = null,
                        tint = Color(preferences.preset.accentColorHex),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Touch Passthrough Guarantee",
                        color = GlassTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "When collapsed into the floating bubble (56 dp), 99% of the screen remains completely untouched. Touches, typing on keyboard, gestures, and scrolling pass directly to underlying apps (FLAG_NOT_FOCUSABLE).",
                    color = GlassTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        // 5. System UI Limits Disclosure
        LiquidGlassCard(
            preferences = preferences,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = WarningAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Android System UI Boundaries",
                        color = GlassTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Under standard Android security policy, ordinary applications without root or ROM firmware modifications cannot directly modify third-party app views or replace Android's system status bar, lock screen, and system settings. Liquid Glass Everywhere adheres strictly to Google Play policy by delivering its liquid glass experience via compliant WindowManager overlays.",
                    color = GlassTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        // 6. Emergency Stop Button
        LiquidGlassButton(
            text = "Emergency Stop (Hide All Overlays)",
            preferences = preferences.copy(glassThickness = 2f),
            modifier = Modifier.fillMaxWidth(),
            testTag = "overlay_emergency_stop_button",
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

        Spacer(modifier = Modifier.height(16.dp))
    }
}

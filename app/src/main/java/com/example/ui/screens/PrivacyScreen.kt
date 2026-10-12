package com.example.ui.screens

import android.content.Context
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NoPhotography
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.ui.theme.SuccessEmerald

@Composable
fun PrivacyScreen(
    preferences: GlassPreferences,
    onEmergencyStop: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Zero-Cloud / 100% Offline Certificate
        LiquidGlassCard(
            preferences = preferences,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = SuccessEmerald,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "100% Offline & Private",
                            color = GlassTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Zero Network Permissions Declared",
                            color = SuccessEmerald,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Liquid Glass Everywhere contains NO INTERNET permission in its AndroidManifest.xml. It is physically impossible for the app to transmit data, logs, keystrokes, or screen contents to any external server or third-party cloud. All visual configurations, scratchpad notes, and app profiles remain strictly quarantined on your local device.",
                    color = GlassTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }

        // 2. Strict Privacy Commitments
        LiquidGlassCard(
            preferences = preferences,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Zero-Surveillance Architecture",
                    color = GlassTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                PrivacyGuaranteeItem(
                    icon = Icons.Default.NoPhotography,
                    title = "No Screen Capturing or OCR",
                    description = "We never take screenshots, record your display, or analyze pixels of other applications."
                )

                PrivacyGuaranteeItem(
                    icon = Icons.Default.VisibilityOff,
                    title = "No Keystroke Logging",
                    description = "Overlay controls are non-focusable when collapsed (FLAG_NOT_FOCUSABLE), ensuring your keyboard input is never intercepted."
                )

                PrivacyGuaranteeItem(
                    icon = Icons.Default.Lock,
                    title = "No Ads, Analytics, or Tracking SDKs",
                    description = "Clean codebase free from telemetry, ad networks, user profiling, or remote tracking libraries."
                )
            }
        }

        // 3. Permission Transparency Audit
        LiquidGlassCard(
            preferences = preferences,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "System Permissions Audit",
                    color = GlassTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                PermissionAuditRow(
                    name = "Display Over Other Apps",
                    technical = "android.permission.SYSTEM_ALERT_WINDOW",
                    reason = "Required to render the floating liquid glass bubble and expanded companion shade over your active apps."
                )

                PermissionAuditRow(
                    name = "Foreground Service",
                    technical = "android.permission.FOREGROUND_SERVICE",
                    reason = "Keeps the overlay active with an ongoing notification and emergency stop button."
                )

                PermissionAuditRow(
                    name = "Usage Access (Optional)",
                    technical = "android.permission.PACKAGE_USAGE_STATS",
                    reason = "Optional: Permits automatic profile switching when launching specific compatible apps. Never leaves device."
                )

                PermissionAuditRow(
                    name = "Tactile Vibration",
                    technical = "android.permission.VIBRATE",
                    reason = "Provides subtle haptic feedback when touching glass controls."
                )
            }
        }

        // 4. Emergency Kill-Switch
        LiquidGlassCard(
            preferences = preferences.copy(glassThickness = 2.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = EmergencyRed,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Instant Master Kill-Switch",
                        color = GlassTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Immediately terminates all background services, floating overlays, and timers. Cleans up all WindowManager view bindings instantly.",
                    color = GlassTextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(4.dp))
                LiquidGlassButton(
                    text = "Kill All Overlays & Terminate Service",
                    preferences = preferences.copy(glassThickness = 2f),
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "kill_all_services_button",
                    onClick = {
                        onEmergencyStop()
                        LiquidGlassOverlayService.stop(context)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PrivacyGuaranteeItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SuccessEmerald,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                color = GlassTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                color = GlassTextMuted,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun PermissionAuditRow(
    name: String,
    technical: String,
    reason: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .padding(10.dp)
    ) {
        Text(
            text = name,
            color = GlassTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = technical,
            color = Color(0xFF60A5FA),
            fontSize = 10.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = reason,
            color = GlassTextSecondary,
            fontSize = 11.sp,
            lineHeight = 15.sp
        )
    }
}

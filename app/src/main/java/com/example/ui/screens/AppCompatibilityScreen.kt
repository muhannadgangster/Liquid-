package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.data.AppItem
import com.example.data.CompatibilityLevel
import com.example.data.GlassMaterialPreset
import com.example.data.GlassPreferences
import com.example.data.InstalledAppsRepository
import com.example.service.ForegroundAppDetector
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassTextMuted
import com.example.ui.theme.GlassTextPrimary
import com.example.ui.theme.GlassTextSecondary
import com.example.ui.theme.SuccessEmerald
import com.example.ui.theme.WarningAmber

@Composable
fun AppCompatibilityScreen(
    preferences: GlassPreferences,
    appsRepository: InstalledAppsRepository,
    onTogglePerAppProfiles: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val installedApps by appsRepository.installedApps.collectAsState()
    val isLoading by appsRepository.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    val hasUsageAccess = ForegroundAppDetector.hasUsageStatsPermission(context)

    LaunchedEffect(Unit) {
        if (installedApps.isEmpty()) {
            appsRepository.loadInstalledApps()
        }
    }

    val filteredApps = remember(installedApps, searchQuery) {
        if (searchQuery.isBlank()) installedApps
        else installedApps.filter {
            it.appName.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // 1. Overview & Usage Stats Status
            LiquidGlassCard(
                preferences = preferences,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Apps,
                                contentDescription = null,
                                tint = Color(preferences.preset.accentColorHex),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "App Compatibility & Profiles",
                                color = GlassTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Switch(
                            checked = preferences.perAppProfilesEnabled,
                            onCheckedChange = onTogglePerAppProfiles,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(preferences.preset.accentColorHex)
                            )
                        )
                    }

                    Text(
                        text = "Customize individual liquid glass refraction profiles for each application. When you open a configured app, the overlay adapts dynamically.",
                        color = GlassTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    // Usage Access Permission Card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (hasUsageAccess) Icons.Default.Check else Icons.Default.Security,
                            contentDescription = null,
                            tint = if (hasUsageAccess) SuccessEmerald else WarningAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (hasUsageAccess) "Foreground Detection: Active" else "Usage Access: Optional",
                                color = GlassTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (hasUsageAccess)
                                    "Permits automatic profile switching on app launch"
                                else
                                    "Grant Usage Access in Settings to auto-detect active foreground apps",
                                color = GlassTextMuted,
                                fontSize = 11.sp
                            )
                        }
                        if (!hasUsageAccess) {
                            LiquidGlassButton(
                                text = "Settings",
                                preferences = preferences.copy(glassThickness = 1.5f),
                                testTag = "open_usage_settings_button",
                                onClick = {
                                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }
        }

        // 2. Compatibility Level Guide
        item {
            LiquidGlassCard(
                preferences = preferences,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Android Platform Compatibility Guide",
                        color = GlassTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(SuccessEmerald)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Supported: Floating companion bubble & controls overlay seamlessly",
                            color = GlassTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(WarningAmber)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Partially Supported: Financial / FLAG_SECURE screens restrict overlay drawing",
                            color = GlassTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(EmergencyRed)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Not Supported: Direct code injection into third-party APKs blocked by Android security",
                            color = GlassTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // 3. Search Field
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_apps_input"),
                placeholder = { Text("Search installed applications...", color = GlassTextMuted) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = GlassTextSecondary)
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = GlassTextPrimary,
                    unfocusedTextColor = GlassTextPrimary,
                    focusedBorderColor = Color(preferences.preset.accentColorHex),
                    unfocusedBorderColor = GlassBorderSubtle
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(preferences.preset.accentColorHex))
                }
            }
        } else if (filteredApps.isEmpty()) {
            item {
                LiquidGlassCard(
                    preferences = preferences,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No matching apps found",
                            color = GlassTextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(filteredApps, key = { it.packageName }) { app ->
                AppProfileCard(
                    app = app,
                    preferences = preferences,
                    onToggleEnabled = { enabled ->
                        appsRepository.setAppOverlayEnabled(app.packageName, enabled)
                    },
                    onSelectPreset = { preset ->
                        appsRepository.setAppPreset(app.packageName, preset)
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AppProfileCard(
    app: AppItem,
    preferences: GlassPreferences,
    onToggleEnabled: (Boolean) -> Unit,
    onSelectPreset: (GlassMaterialPreset) -> Unit
) {
    var showDropdown by remember { mutableStateOf(false) }

    LiquidGlassCard(
        preferences = preferences,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App Icon
                if (app.icon != null) {
                    val bitmap = remember(app.packageName) {
                        try {
                            app.icon.toBitmap(64, 64).asImageBitmap()
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = app.appName,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Apps, contentDescription = null, tint = Color.White)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Apps, contentDescription = null, tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        color = GlassTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = app.packageName,
                        color = GlassTextMuted,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }

                // Enable/Disable Switch for this app
                Switch(
                    checked = app.isOverlayEnabled,
                    onCheckedChange = onToggleEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(preferences.preset.accentColorHex)
                    )
                )
            }

            // Compatibility status indicator & preset selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (color, label) = when (app.compatibilityLevel) {
                        CompatibilityLevel.SUPPORTED -> SuccessEmerald to "Supported"
                        CompatibilityLevel.PARTIALLY_SUPPORTED -> WarningAmber to "Partially Supported"
                        CompatibilityLevel.SANDBOX_RESTRICTED -> EmergencyRed to "Restricted"
                    }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = label,
                        color = color,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Preset selector button
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(app.assignedPreset.primaryColorHex).copy(alpha = 0.4f))
                            .border(1.dp, Color(app.assignedPreset.accentColorHex), RoundedCornerShape(6.dp))
                            .clickable { showDropdown = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = Color(app.assignedPreset.accentColorHex),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = app.assignedPreset.displayName.substringBefore(" "),
                            color = GlassTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false }
                    ) {
                        GlassMaterialPreset.entries.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p.displayName, fontSize = 12.sp) },
                                onClick = {
                                    onSelectPreset(p)
                                    showDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            Text(
                text = app.compatibilityReason,
                color = GlassTextMuted,
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
        }
    }
}

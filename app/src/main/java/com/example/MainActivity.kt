package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.GlassPreferences
import com.example.ui.MainViewModel
import com.example.ui.components.LiquidGlassBadge
import com.example.ui.screens.AppCompatibilityScreen
import com.example.ui.screens.CustomizationScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.OverlaySettingsScreen
import com.example.ui.screens.PrivacyScreen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DeepSpaceNavy
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassTextPrimary
import com.example.ui.theme.GlassTextSecondary
import com.example.ui.theme.MyApplicationTheme

enum class AppDestination(val label: String, val icon: ImageVector, val testTag: String) {
    DASHBOARD("Overview", Icons.Default.Dashboard, "tab_dashboard"),
    CUSTOMIZE("Customize", Icons.Default.Tune, "tab_customize"),
    OVERLAY("Overlay", Icons.Default.Layers, "tab_overlay"),
    COMPATIBILITY("Apps", Icons.Default.Apps, "tab_compatibility"),
    PRIVACY("Privacy", Icons.Default.Security, "tab_privacy")
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val preferences by viewModel.preferences.collectAsStateWithLifecycle()

            MyApplicationTheme(themeMode = preferences.themeMode) {
                MainContent(
                    preferences = preferences,
                    viewModel = viewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    preferences: GlassPreferences,
    viewModel: MainViewModel
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.DASHBOARD) }

    // Custom back handler to return to Dashboard if on a sub-tab
    BackHandler(enabled = currentDestination != AppDestination.DASHBOARD) {
        currentDestination = AppDestination.DASHBOARD
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(DeepSpaceNavy)) {
        val isExpanded = maxWidth >= 600.dp

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Glowing liquid glass orb icon
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(preferences.preset.primaryColorHex).copy(alpha = 0.8f))
                                    .border(1.5.dp, Color(preferences.preset.accentColorHex), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.app_full_name),
                                color = GlassTextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            LiquidGlassBadge(
                                text = preferences.preset.displayName.substringBefore(" "),
                                preset = preferences.preset,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DeepSpaceNavy.copy(alpha = 0.95f),
                        titleContentColor = GlassTextPrimary
                    )
                )
            },
            bottomBar = {
                if (!isExpanded) {
                    NavigationBar(
                        containerColor = DeepSpaceNavy,
                        tonalElevation = 8.dp
                    ) {
                        AppDestination.entries.forEach { dest ->
                            val selected = currentDestination == dest
                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentDestination = dest },
                                icon = {
                                    Icon(
                                        imageVector = dest.icon,
                                        contentDescription = dest.label,
                                        tint = if (selected) Color(preferences.preset.accentColorHex) else GlassTextSecondary
                                    )
                                },
                                label = {
                                    Text(
                                        text = dest.label,
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selected) Color.White else GlassTextSecondary
                                    )
                                },
                                modifier = Modifier.testTag(dest.testTag),
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = Color(preferences.preset.accentColorHex).copy(alpha = 0.2f)
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // NavigationRail for tablets / expanded screens
                if (isExpanded) {
                    NavigationRail(
                        containerColor = DeepSpaceNavy,
                        modifier = Modifier.border(1.dp, GlassBorderSubtle)
                    ) {
                        AppDestination.entries.forEach { dest ->
                            val selected = currentDestination == dest
                            NavigationRailItem(
                                selected = selected,
                                onClick = { currentDestination = dest },
                                icon = {
                                    Icon(
                                        imageVector = dest.icon,
                                        contentDescription = dest.label,
                                        tint = if (selected) Color(preferences.preset.accentColorHex) else GlassTextSecondary
                                    )
                                },
                                label = {
                                    Text(
                                        text = dest.label,
                                        fontSize = 11.sp,
                                        color = if (selected) Color.White else GlassTextSecondary
                                    )
                                },
                                modifier = Modifier.testTag(dest.testTag),
                                colors = NavigationRailItemDefaults.colors(
                                    indicatorColor = Color(preferences.preset.accentColorHex).copy(alpha = 0.2f)
                                )
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    when (currentDestination) {
                        AppDestination.DASHBOARD -> DashboardScreen(
                            preferences = preferences,
                            onToggleMaster = { viewModel.setMasterEnabled(it) },
                            onToggleOverlay = { viewModel.setOverlayEnabled(it) },
                            onEmergencyStop = { viewModel.emergencyStop() },
                            onNavigateToCustomization = { currentDestination = AppDestination.CUSTOMIZE },
                            onNavigateToOverlaySettings = { currentDestination = AppDestination.OVERLAY }
                        )

                        AppDestination.CUSTOMIZE -> CustomizationScreen(
                            preferences = preferences,
                            onUpdateTransparency = { viewModel.setTransparency(it) },
                            onUpdateBlur = { viewModel.setBlur(it) },
                            onUpdateReflection = { viewModel.setReflection(it) },
                            onUpdateGlow = { viewModel.setGlow(it) },
                            onUpdateSpeed = { viewModel.setAnimationSpeed(it) },
                            onUpdateThickness = { viewModel.setGlassThickness(it) },
                            onUpdateCornerRadius = { viewModel.setCornerRadius(it) },
                            onUpdatePreset = { viewModel.setPreset(it) },
                            onUpdateThemeMode = { viewModel.setThemeMode(it) },
                            onUpdateBatterySaver = { viewModel.setBatterySaver(it) },
                            onUpdateHaptics = { viewModel.setHaptics(it) },
                            onResetDefaults = { viewModel.resetToDefaults() }
                        )

                        AppDestination.OVERLAY -> OverlaySettingsScreen(
                            preferences = preferences,
                            onToggleOverlay = { viewModel.setOverlayEnabled(it) },
                            onUpdateBubbleSize = { viewModel.setBubbleSize(it) },
                            onToggleEdgeSnapping = { viewModel.setEdgeSnapping(it) },
                            onToggleShowQuickTools = { viewModel.setShowQuickTools(it) },
                            onToggleShowQuickApps = { viewModel.setShowQuickApps(it) },
                            onEmergencyStop = { viewModel.emergencyStop() }
                        )

                        AppDestination.COMPATIBILITY -> AppCompatibilityScreen(
                            preferences = preferences,
                            appsRepository = viewModel.appsRepository,
                            onTogglePerAppProfiles = { viewModel.setPerAppProfilesEnabled(it) }
                        )

                        AppDestination.PRIVACY -> PrivacyScreen(
                            preferences = preferences,
                            onEmergencyStop = { viewModel.emergencyStop() }
                        )
                    }
                }
            }
        }
    }
}

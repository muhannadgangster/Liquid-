package com.example.data

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

enum class CompatibilityLevel {
    SUPPORTED,
    PARTIALLY_SUPPORTED,
    SANDBOX_RESTRICTED
}

data class AppItem(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val compatibilityLevel: CompatibilityLevel,
    val compatibilityReason: String,
    val isOverlayEnabled: Boolean = true,
    val assignedPreset: GlassMaterialPreset = GlassMaterialPreset.CYBER_IRIDESCENT
)

class InstalledAppsRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("liquid_glass_app_profiles", Context.MODE_PRIVATE)

    private val _installedApps = MutableStateFlow<List<AppItem>>(emptyList())
    val installedApps: StateFlow<List<AppItem>> = _installedApps.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    suspend fun loadInstalledApps() = withContext(Dispatchers.IO) {
        _isLoading.value = true
        val pm = context.packageManager

        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = pm.queryIntentActivities(intent, 0)
        val seenPackages = mutableSetOf<String>()
        val appList = mutableListOf<AppItem>()

        for (info in resolveInfos) {
            val pkg = info.activityInfo.packageName
            if (pkg == context.packageName || seenPackages.contains(pkg)) {
                continue
            }
            seenPackages.add(pkg)

            val label = info.loadLabel(pm).toString()
            val icon = try {
                info.loadIcon(pm)
            } catch (e: Exception) {
                null
            }

            val (level, reason) = determineCompatibility(pkg, info.activityInfo.applicationInfo)
            val isEnabled = prefs.getBoolean("app_enabled_$pkg", true)
            val presetId = prefs.getString("app_preset_$pkg", GlassMaterialPreset.CYBER_IRIDESCENT.id)
                ?: GlassMaterialPreset.CYBER_IRIDESCENT.id
            val preset = GlassMaterialPreset.fromId(presetId)

            appList.add(
                AppItem(
                    packageName = pkg,
                    appName = label,
                    icon = icon,
                    compatibilityLevel = level,
                    compatibilityReason = reason,
                    isOverlayEnabled = isEnabled,
                    assignedPreset = preset
                )
            )
        }

        appList.sortBy { it.appName.lowercase() }
        _installedApps.value = appList
        _isLoading.value = false
    }

    private fun determineCompatibility(packageName: String, appInfo: ApplicationInfo): Pair<CompatibilityLevel, String> {
        val lowerPkg = packageName.lowercase()
        return when {
            lowerPkg.contains("bank") || lowerPkg.contains("auth") || lowerPkg.contains("wallet") || lowerPkg.contains("pay") -> {
                CompatibilityLevel.PARTIALLY_SUPPORTED to
                        "Banking and security applications frequently enforce FLAG_SECURE or overlay tap-jacking protection. Overlay will politely dim or minimize."
            }
            (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 && (lowerPkg.contains("settings") || lowerPkg.contains("systemui")) -> {
                CompatibilityLevel.PARTIALLY_SUPPORTED to
                        "System UI and Settings windows enforce elevated Android permission layers. Floating companion bubble remains interactive."
            }
            else -> {
                CompatibilityLevel.SUPPORTED to
                        "Fully supported. The floating liquid glass companion shade, bubble, and quick controls display seamlessly above this application."
            }
        }
    }

    fun setAppOverlayEnabled(packageName: String, enabled: Boolean) {
        prefs.edit().putBoolean("app_enabled_$packageName", enabled).apply()
        _installedApps.value = _installedApps.value.map {
            if (it.packageName == packageName) it.copy(isOverlayEnabled = enabled) else it
        }
    }

    fun setAppPreset(packageName: String, preset: GlassMaterialPreset) {
        prefs.edit().putString("app_preset_$packageName", preset.id).apply()
        _installedApps.value = _installedApps.value.map {
            if (it.packageName == packageName) it.copy(assignedPreset = preset) else it
        }
    }

    fun getAppProfile(packageName: String): Pair<Boolean, GlassMaterialPreset> {
        val isEnabled = prefs.getBoolean("app_enabled_$packageName", true)
        val presetId = prefs.getString("app_preset_$packageName", GlassMaterialPreset.CYBER_IRIDESCENT.id)
            ?: GlassMaterialPreset.CYBER_IRIDESCENT.id
        return Pair(isEnabled, GlassMaterialPreset.fromId(presetId))
    }
}

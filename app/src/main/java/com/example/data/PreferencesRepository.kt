package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "liquid_glass_prefs")

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val MASTER_ENABLED = booleanPreferencesKey("master_enabled")
        val OVERLAY_ENABLED = booleanPreferencesKey("overlay_enabled")
        val TRANSPARENCY = floatPreferencesKey("transparency")
        val BLUR_INTENSITY = floatPreferencesKey("blur_intensity")
        val REFLECTION_INTENSITY = floatPreferencesKey("reflection_intensity")
        val GLOW_INTENSITY = floatPreferencesKey("glow_intensity")
        val ANIMATION_SPEED = floatPreferencesKey("animation_speed")
        val GLASS_THICKNESS = floatPreferencesKey("glass_thickness")
        val CORNER_RADIUS = floatPreferencesKey("corner_radius")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val PRESET_ID = stringPreferencesKey("preset_id")
        val BATTERY_SAVER = booleanPreferencesKey("battery_saver")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val BUBBLE_SIZE_DP = intPreferencesKey("bubble_size_dp")
        val EDGE_SNAPPING = booleanPreferencesKey("edge_snapping")
        val PER_APP_PROFILES_ENABLED = booleanPreferencesKey("per_app_profiles_enabled")
        val SHOW_QUICK_TOOLS = booleanPreferencesKey("show_quick_tools")
        val SHOW_QUICK_APPS = booleanPreferencesKey("show_quick_apps")
    }

    val preferencesFlow: Flow<GlassPreferences> = context.dataStore.data.map { preferences ->
        GlassPreferences(
            masterEnabled = preferences[Keys.MASTER_ENABLED] ?: true,
            overlayEnabled = preferences[Keys.OVERLAY_ENABLED] ?: false,
            transparency = preferences[Keys.TRANSPARENCY] ?: 0.35f,
            blurIntensity = preferences[Keys.BLUR_INTENSITY] ?: 0.70f,
            reflectionIntensity = preferences[Keys.REFLECTION_INTENSITY] ?: 0.80f,
            glowIntensity = preferences[Keys.GLOW_INTENSITY] ?: 0.65f,
            animationSpeed = preferences[Keys.ANIMATION_SPEED] ?: 1.0f,
            glassThickness = preferences[Keys.GLASS_THICKNESS] ?: 3.0f,
            cornerRadius = preferences[Keys.CORNER_RADIUS] ?: 24.0f,
            themeMode = preferences[Keys.THEME_MODE] ?: "SYSTEM",
            preset = GlassMaterialPreset.fromId(preferences[Keys.PRESET_ID] ?: "cyber_iridescent"),
            batterySaverMode = preferences[Keys.BATTERY_SAVER] ?: false,
            hapticsEnabled = preferences[Keys.HAPTICS_ENABLED] ?: true,
            bubbleSizeDp = preferences[Keys.BUBBLE_SIZE_DP] ?: 56,
            edgeSnapping = preferences[Keys.EDGE_SNAPPING] ?: true,
            perAppProfilesEnabled = preferences[Keys.PER_APP_PROFILES_ENABLED] ?: true,
            showQuickToolsInOverlay = preferences[Keys.SHOW_QUICK_TOOLS] ?: true,
            showQuickAppsInOverlay = preferences[Keys.SHOW_QUICK_APPS] ?: true
        )
    }

    suspend fun updateMasterEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.MASTER_ENABLED] = enabled }
    }

    suspend fun updateOverlayEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.OVERLAY_ENABLED] = enabled }
    }

    suspend fun updateTransparency(value: Float) {
        context.dataStore.edit { it[Keys.TRANSPARENCY] = value.coerceIn(0.1f, 0.9f) }
    }

    suspend fun updateBlurIntensity(value: Float) {
        context.dataStore.edit { it[Keys.BLUR_INTENSITY] = value.coerceIn(0.0f, 1.0f) }
    }

    suspend fun updateReflectionIntensity(value: Float) {
        context.dataStore.edit { it[Keys.REFLECTION_INTENSITY] = value.coerceIn(0.0f, 1.0f) }
    }

    suspend fun updateGlowIntensity(value: Float) {
        context.dataStore.edit { it[Keys.GLOW_INTENSITY] = value.coerceIn(0.0f, 1.0f) }
    }

    suspend fun updateAnimationSpeed(value: Float) {
        context.dataStore.edit { it[Keys.ANIMATION_SPEED] = value.coerceIn(0.5f, 2.0f) }
    }

    suspend fun updateGlassThickness(value: Float) {
        context.dataStore.edit { it[Keys.GLASS_THICKNESS] = value.coerceIn(1.0f, 6.0f) }
    }

    suspend fun updateCornerRadius(value: Float) {
        context.dataStore.edit { it[Keys.CORNER_RADIUS] = value.coerceIn(8.0f, 36.0f) }
    }

    suspend fun updateThemeMode(mode: String) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    suspend fun updatePreset(preset: GlassMaterialPreset) {
        context.dataStore.edit { it[Keys.PRESET_ID] = preset.id }
    }

    suspend fun updateBatterySaver(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BATTERY_SAVER] = enabled }
    }

    suspend fun updateHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HAPTICS_ENABLED] = enabled }
    }

    suspend fun updateBubbleSizeDp(size: Int) {
        context.dataStore.edit { it[Keys.BUBBLE_SIZE_DP] = size.coerceIn(44, 76) }
    }

    suspend fun updateEdgeSnapping(enabled: Boolean) {
        context.dataStore.edit { it[Keys.EDGE_SNAPPING] = enabled }
    }

    suspend fun updatePerAppProfilesEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.PER_APP_PROFILES_ENABLED] = enabled }
    }

    suspend fun updateShowQuickTools(show: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_QUICK_TOOLS] = show }
    }

    suspend fun updateShowQuickApps(show: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_QUICK_APPS] = show }
    }

    suspend fun resetToDefaults() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}

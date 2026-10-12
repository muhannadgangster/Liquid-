package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GlassMaterialPreset
import com.example.data.GlassPreferences
import com.example.data.InstalledAppsRepository
import com.example.data.PreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val preferencesRepository = PreferencesRepository(application)
    val appsRepository = InstalledAppsRepository(application)

    val preferences: StateFlow<GlassPreferences> = preferencesRepository.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GlassPreferences()
        )

    fun setMasterEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateMasterEnabled(enabled)
        }
    }

    fun setOverlayEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateOverlayEnabled(enabled)
        }
    }

    fun setTransparency(value: Float) {
        viewModelScope.launch {
            preferencesRepository.updateTransparency(value)
        }
    }

    fun setBlur(value: Float) {
        viewModelScope.launch {
            preferencesRepository.updateBlurIntensity(value)
        }
    }

    fun setReflection(value: Float) {
        viewModelScope.launch {
            preferencesRepository.updateReflectionIntensity(value)
        }
    }

    fun setGlow(value: Float) {
        viewModelScope.launch {
            preferencesRepository.updateGlowIntensity(value)
        }
    }

    fun setAnimationSpeed(value: Float) {
        viewModelScope.launch {
            preferencesRepository.updateAnimationSpeed(value)
        }
    }

    fun setGlassThickness(value: Float) {
        viewModelScope.launch {
            preferencesRepository.updateGlassThickness(value)
        }
    }

    fun setCornerRadius(value: Float) {
        viewModelScope.launch {
            preferencesRepository.updateCornerRadius(value)
        }
    }

    fun setPreset(preset: GlassMaterialPreset) {
        viewModelScope.launch {
            preferencesRepository.updatePreset(preset)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesRepository.updateThemeMode(mode)
        }
    }

    fun setBatterySaver(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateBatterySaver(enabled)
        }
    }

    fun setHaptics(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateHapticsEnabled(enabled)
        }
    }

    fun setBubbleSize(size: Int) {
        viewModelScope.launch {
            preferencesRepository.updateBubbleSizeDp(size)
        }
    }

    fun setEdgeSnapping(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateEdgeSnapping(enabled)
        }
    }

    fun setPerAppProfilesEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updatePerAppProfilesEnabled(enabled)
        }
    }

    fun setShowQuickTools(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateShowQuickTools(enabled)
        }
    }

    fun setShowQuickApps(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateShowQuickApps(enabled)
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            preferencesRepository.resetToDefaults()
        }
    }

    fun emergencyStop() {
        viewModelScope.launch {
            preferencesRepository.updateOverlayEnabled(false)
        }
    }
}

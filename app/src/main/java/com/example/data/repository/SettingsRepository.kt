package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("qobley_settings", Context.MODE_PRIVATE)

    private val _vibrationEnabled = MutableStateFlow(prefs.getBoolean("vibration_enabled", true))
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

    private val _soundEnabled = MutableStateFlow(prefs.getBoolean("sound_enabled", false))
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _duplicatePrevention = MutableStateFlow(prefs.getBoolean("duplicate_prevention", true))
    val duplicatePrevention: StateFlow<Boolean> = _duplicatePrevention.asStateFlow()

    private val _launchScreen = MutableStateFlow(prefs.getString("launch_screen", "home") ?: "home")
    val launchScreen: StateFlow<String> = _launchScreen.asStateFlow()

    private val _themeMode = MutableStateFlow(prefs.getString("theme_mode", "system") ?: "system")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("vibration_enabled", enabled).apply()
        _vibrationEnabled.value = enabled
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
        _soundEnabled.value = enabled
    }

    fun setDuplicatePrevention(enabled: Boolean) {
        prefs.edit().putBoolean("duplicate_prevention", enabled).apply()
        _duplicatePrevention.value = enabled
    }

    fun setLaunchScreen(screen: String) {
        prefs.edit().putString("launch_screen", screen).apply()
        _launchScreen.value = screen
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString("theme_mode", mode).apply()
        _themeMode.value = mode
    }
}

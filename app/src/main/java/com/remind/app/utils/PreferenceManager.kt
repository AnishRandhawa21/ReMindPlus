package com.remind.app.utils

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreferenceManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("remind_plus_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_THEME = "key_theme"
        private const val KEY_ACCENT_COLOR = "key_accent_color"
        private const val KEY_AUTO_SYNC = "key_auto_sync"
        private const val KEY_NOTIFICATIONS = "key_notifications"
        private const val KEY_VIBRATION = "key_vibration"
        private const val KEY_SOUND = "key_sound"
    }

    private val _themeFlow = MutableStateFlow(sharedPreferences.getString(KEY_THEME, "System") ?: "System")
    val themeFlow: StateFlow<String> = _themeFlow

    private val _accentColorFlow = MutableStateFlow(sharedPreferences.getInt(KEY_ACCENT_COLOR, 0))
    val accentColorFlow: StateFlow<Int> = _accentColorFlow

    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
        when (key) {
            KEY_THEME -> _themeFlow.value = prefs.getString(KEY_THEME, "System") ?: "System"
            KEY_ACCENT_COLOR -> _accentColorFlow.value = prefs.getInt(KEY_ACCENT_COLOR, 0)
        }
    }

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    var theme: String
        get() = sharedPreferences.getString(KEY_THEME, "System") ?: "System"
        set(value) {
            sharedPreferences.edit().putString(KEY_THEME, value).apply()
        }

    var accentColor: Int
        get() = sharedPreferences.getInt(KEY_ACCENT_COLOR, 0)
        set(value) = sharedPreferences.edit().putInt(KEY_ACCENT_COLOR, value).apply()

    var autoSync: Boolean
        get() = sharedPreferences.getBoolean(KEY_AUTO_SYNC, true)
        set(value) = sharedPreferences.edit().putBoolean(KEY_AUTO_SYNC, value).apply()

    var notificationsEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_NOTIFICATIONS, true)
        set(value) = sharedPreferences.edit().putBoolean(KEY_NOTIFICATIONS, value).apply()

    var vibrationEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_VIBRATION, true)
        set(value) = sharedPreferences.edit().putBoolean(KEY_VIBRATION, value).apply()

    var soundEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_SOUND, false)
        set(value) = sharedPreferences.edit().putBoolean(KEY_SOUND, value).apply()
}

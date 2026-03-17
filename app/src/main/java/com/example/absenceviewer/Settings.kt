package com.example.absenceviewer

import android.content.Context
import android.content.SharedPreferences


// use by creating instance of this class when needed with given app context
// example: val settings = Settings(context)
class Settings(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    // Theme Einstellung
    var themeMode: Int
    get() = prefs.getInt("theme_mode", 0) // Standard: 0
    set(value) = prefs.edit().putInt("theme_mode", value).apply()

    // Klassen Einstellung
    var selectedClass: ClassName
    get() {
        val name = prefs.getString("selected_class", ClassName.All.name)
        return ClassName.entries.find { it.name == name } ?: ClassName.All
    }
    set(value) = prefs.edit().putString("selected_class", value.name).apply()

    var notificationsEnabled: Boolean
    get() = prefs.getBoolean("notifications_enabled", true)
    set(value) = prefs.edit().putBoolean("notifications_enabled", value).apply()
}
package com.example.absenceviewer

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.input.key.type
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken

class MessageFilter(context: Context) {
    private val sharedPreferences : SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    var filteredClass : List<String>
        get() = sharedPreferences.getString("filteredClass", null)?.split(",")?.map { it.toString() } ?: emptyList()
        set(filteredClass : List<String>) = sharedPreferences.edit().putString("filteredClass", filteredClass.joinToString(",")).apply()

}
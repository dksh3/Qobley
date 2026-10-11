package com.example.widget

import android.content.Context
import android.content.SharedPreferences

object QobleyWidgetPrefs {
    private const val PREFS_NAME = "qobley_widget_prefs"
    private const val KEY_PREFIX_CODE_ID = "widget_code_id_"
    private const val KEY_PREFIX_PRIMARY_ACTION = "widget_primary_action_"
    private const val KEY_PREFIX_THEME = "widget_theme_"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveWidgetCodeId(context: Context, appWidgetId: Int, codeId: Long) {
        getPrefs(context).edit().putLong(KEY_PREFIX_CODE_ID + appWidgetId, codeId).apply()
    }

    fun getWidgetCodeId(context: Context, appWidgetId: Int): Long? {
        val prefs = getPrefs(context)
        val key = KEY_PREFIX_CODE_ID + appWidgetId
        return if (prefs.contains(key)) prefs.getLong(key, -1L) else null
    }

    fun deleteWidgetCodeId(context: Context, appWidgetId: Int) {
        getPrefs(context).edit().remove(KEY_PREFIX_CODE_ID + appWidgetId).apply()
    }

    fun saveWidgetPrimaryAction(context: Context, appWidgetId: Int, action: String) {
        getPrefs(context).edit().putString(KEY_PREFIX_PRIMARY_ACTION + appWidgetId, action).apply()
    }

    fun getWidgetPrimaryAction(context: Context, appWidgetId: Int, defaultAction: String = "scan"): String {
        return getPrefs(context).getString(KEY_PREFIX_PRIMARY_ACTION + appWidgetId, defaultAction) ?: defaultAction
    }
}

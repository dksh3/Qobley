package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.MainActivity
import com.example.R
import com.example.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object QobleyShortcutManager {

    /**
     * Publishes dynamic launcher shortcuts for top favorites or recently scanned codes.
     * Maximum 2-3 dynamic shortcuts are pushed to avoid cluttering launcher long-press menu.
     */
    fun updateDynamicShortcuts(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val favorites = db.codeDao().getFavoritesSync(2)
                val recents = if (favorites.size < 2) {
                    db.codeDao().getRecentSync(2 - favorites.size)
                } else emptyList()

                val itemsToPromote = (favorites + recents).distinctBy { it.id }.take(3)
                if (itemsToPromote.isEmpty()) {
                    ShortcutManagerCompat.removeAllDynamicShortcuts(context)
                    return@launch
                }

                val shortcuts = itemsToPromote.mapIndexed { index, code ->
                    val shortcutIntent = Intent(context, MainActivity::class.java).apply {
                        action = Intent.ACTION_VIEW
                        data = Uri.parse("qobley://item/${code.id}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }

                    ShortcutInfoCompat.Builder(context, "dynamic_code_${code.id}")
                        .setShortLabel(code.title.take(12))
                        .setLongLabel("${if (code.isFavorite) "★ " else ""}${code.title}")
                        .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
                        .setIntent(shortcutIntent)
                        .setRank(index)
                        .build()
                }

                ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts)
            } catch (e: Exception) {
                // Silently handle devices without dynamic shortcut launcher support
                e.printStackTrace()
            }
        }
    }
}

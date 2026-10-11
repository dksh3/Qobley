package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.db.AppDatabase
import com.example.generator.BarcodeGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object QobleyWidgetUpdater {
    /**
     * Broadcasts updates to all active Qobley widgets across the Android home screen.
     * Invoked when codes are scanned, created, deleted, or favorited.
     */
    fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)

        // 1. Small 1x1 widgets
        val smallComponent = ComponentName(context, QobleySmallWidget::class.java)
        val smallIds = appWidgetManager.getAppWidgetIds(smallComponent)
        if (smallIds.isNotEmpty()) {
            val intent = Intent(context, QobleySmallWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, smallIds)
            }
            context.sendBroadcast(intent)
        }

        // 2. Medium widgets
        val mediumComponent = ComponentName(context, QobleyQuickScanWidget::class.java)
        val mediumIds = appWidgetManager.getAppWidgetIds(mediumComponent)
        if (mediumIds.isNotEmpty()) {
            val intent = Intent(context, QobleyQuickScanWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, mediumIds)
            }
            context.sendBroadcast(intent)
        }

        val mediumAltComponent = ComponentName(context, QobleyMediumWidget::class.java)
        val mediumAltIds = appWidgetManager.getAppWidgetIds(mediumAltComponent)
        if (mediumAltIds.isNotEmpty()) {
            val intent = Intent(context, QobleyMediumWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, mediumAltIds)
            }
            context.sendBroadcast(intent)
        }

        // 3. Large Command Center widgets
        val largeComponent = ComponentName(context, QobleyLargeWidget::class.java)
        val largeIds = appWidgetManager.getAppWidgetIds(largeComponent)
        if (largeIds.isNotEmpty()) {
            val intent = Intent(context, QobleyLargeWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, largeIds)
            }
            context.sendBroadcast(intent)
        }

        // 4. Favorites widgets
        val favComponent = ComponentName(context, QobleyFavoritesWidget::class.java)
        val favIds = appWidgetManager.getAppWidgetIds(favComponent)
        if (favIds.isNotEmpty()) {
            val intent = Intent(context, QobleyFavoritesWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, favIds)
            }
            context.sendBroadcast(intent)
        }

        // 5. Single Code Pinned widgets
        val singleComponent = ComponentName(context, QobleySingleCodeWidget::class.java)
        val singleIds = appWidgetManager.getAppWidgetIds(singleComponent)
        if (singleIds.isNotEmpty()) {
            val intent = Intent(context, QobleySingleCodeWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, singleIds)
            }
            context.sendBroadcast(intent)
        }
    }
}

/**
 * Small 1x1 Quick Scan Widget:
 * Tapping it immediately launches qobley://scan and opens the camera scanner.
 */
class QobleySmallWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_quick_scan_small)

            val scanIntent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse("qobley://scan")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                201,
                scanIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widget_small_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

/**
 * Medium Quick Access Widget:
 * [ Scan ]
 * [ QR ] [ Barcode ] [ Image ]
 */
class QobleyMediumWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_medium_access)

            // Scan -> qobley://scan
            val scanIntent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse("qobley://scan")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            views.setOnClickPendingIntent(
                R.id.widget_btn_scan,
                PendingIntent.getActivity(context, 301, scanIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            )

            // QR -> qobley://create/qr
            val qrIntent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse("qobley://create/qr")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            views.setOnClickPendingIntent(
                R.id.widget_btn_create_qr,
                PendingIntent.getActivity(context, 302, qrIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            )

            // Barcode -> qobley://create/barcode
            val barcodeIntent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse("qobley://create/barcode")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            views.setOnClickPendingIntent(
                R.id.widget_btn_create_barcode,
                PendingIntent.getActivity(context, 303, barcodeIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            )

            // Image -> qobley://scan/image
            val imageIntent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse("qobley://scan/image")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            views.setOnClickPendingIntent(
                R.id.widget_btn_scan_image,
                PendingIntent.getActivity(context, 304, imageIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            )

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

/**
 * Large Command Center Widget:
 * Primary Scan, Creators (QR, Barcode, Wi-Fi, Payment), and Recent Items from Room Database.
 */
class QobleyLargeWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val db = AppDatabase.getInstance(context)
            val recentList = try {
                db.codeDao().getRecentSync(2)
            } catch (_: Exception) {
                emptyList()
            }

            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_large_command_center)

                // Scan -> qobley://scan
                val scanIntent = Intent(context, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = Uri.parse("qobley://scan")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                views.setOnClickPendingIntent(
                    R.id.widget_btn_scan,
                    PendingIntent.getActivity(context, 401, scanIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                )

                // History link -> qobley://history
                val historyIntent = Intent(context, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = Uri.parse("qobley://history")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                views.setOnClickPendingIntent(
                    R.id.widget_large_history_link,
                    PendingIntent.getActivity(context, 402, historyIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                )

                // QR creator -> qobley://create/qr
                val qrIntent = Intent(context, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = Uri.parse("qobley://create/qr")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                views.setOnClickPendingIntent(
                    R.id.widget_btn_create_qr,
                    PendingIntent.getActivity(context, 403, qrIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                )

                // Barcode creator -> qobley://create/barcode
                val barcodeIntent = Intent(context, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = Uri.parse("qobley://create/barcode")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                views.setOnClickPendingIntent(
                    R.id.widget_btn_create_barcode,
                    PendingIntent.getActivity(context, 404, barcodeIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                )

                // Wi-Fi creator -> qobley://create/qr?type=wifi
                val wifiIntent = Intent(context, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = Uri.parse("qobley://create/qr?type=wifi")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                views.setOnClickPendingIntent(
                    R.id.widget_btn_create_wifi,
                    PendingIntent.getActivity(context, 405, wifiIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                )

                // Payment creator -> qobley://create/qr?type=payment
                val payIntent = Intent(context, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = Uri.parse("qobley://create/qr?type=payment")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                views.setOnClickPendingIntent(
                    R.id.widget_btn_create_payment,
                    PendingIntent.getActivity(context, 406, payIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                )

                // Populate recent item 1
                if (recentList.isNotEmpty()) {
                    val item1 = recentList[0]
                    views.setTextViewText(R.id.widget_recent_title_1, item1.title)
                    views.setTextViewText(R.id.widget_recent_preview_1, item1.preview)
                    views.setTextViewText(R.id.widget_recent_type_1, item1.codeType.name.take(4))

                    val item1Intent = Intent(context, MainActivity::class.java).apply {
                        action = Intent.ACTION_VIEW
                        data = Uri.parse("qobley://item/${item1.id}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    views.setOnClickPendingIntent(
                        R.id.widget_recent_item_1,
                        PendingIntent.getActivity(context, 411, item1Intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                    )
                }

                // Populate recent item 2
                if (recentList.size > 1) {
                    val item2 = recentList[1]
                    views.setTextViewText(R.id.widget_recent_title_2, item2.title)
                    views.setTextViewText(R.id.widget_recent_preview_2, item2.preview)
                    views.setTextViewText(R.id.widget_recent_type_2, item2.codeType.name.take(4))

                    val item2Intent = Intent(context, MainActivity::class.java).apply {
                        action = Intent.ACTION_VIEW
                        data = Uri.parse("qobley://item/${item2.id}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    views.setOnClickPendingIntent(
                        R.id.widget_recent_item_2,
                        PendingIntent.getActivity(context, 412, item2Intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                    )
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}

/**
 * Favorites Widget:
 * Displays user's starred codes for one-tap access.
 */
class QobleyFavoritesWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val db = AppDatabase.getInstance(context)
            val favorites = try {
                db.codeDao().getFavoritesSync(3)
            } catch (_: Exception) {
                emptyList()
            }

            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_favorites)

                // Header link -> qobley://saved
                val savedIntent = Intent(context, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = Uri.parse("qobley://saved")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                views.setOnClickPendingIntent(
                    R.id.widget_fav_saved_link,
                    PendingIntent.getActivity(context, 501, savedIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                )

                if (favorites.isEmpty()) {
                    views.setViewVisibility(R.id.widget_fav_empty, View.VISIBLE)
                    views.setViewVisibility(R.id.widget_fav_item_1, View.GONE)
                    views.setViewVisibility(R.id.widget_fav_item_2, View.GONE)
                    views.setViewVisibility(R.id.widget_fav_item_3, View.GONE)

                    views.setOnClickPendingIntent(
                        R.id.widget_fav_empty_btn,
                        PendingIntent.getActivity(context, 502, savedIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                    )
                } else {
                    views.setViewVisibility(R.id.widget_fav_empty, View.GONE)

                    // Item 1
                    val item1 = favorites[0]
                    views.setViewVisibility(R.id.widget_fav_item_1, View.VISIBLE)
                    views.setTextViewText(R.id.widget_fav_title_1, item1.title)
                    views.setTextViewText(R.id.widget_fav_preview_1, item1.preview)
                    views.setTextViewText(R.id.widget_fav_type_1, item1.category.take(4).uppercase())
                    val item1Intent = Intent(context, MainActivity::class.java).apply {
                        action = Intent.ACTION_VIEW
                        data = Uri.parse("qobley://item/${item1.id}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    views.setOnClickPendingIntent(
                        R.id.widget_fav_item_1,
                        PendingIntent.getActivity(context, 511, item1Intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                    )

                    // Item 2
                    if (favorites.size > 1) {
                        val item2 = favorites[1]
                        views.setViewVisibility(R.id.widget_fav_item_2, View.VISIBLE)
                        views.setTextViewText(R.id.widget_fav_title_2, item2.title)
                        views.setTextViewText(R.id.widget_fav_preview_2, item2.preview)
                        views.setTextViewText(R.id.widget_fav_type_2, item2.category.take(4).uppercase())
                        val item2Intent = Intent(context, MainActivity::class.java).apply {
                            action = Intent.ACTION_VIEW
                            data = Uri.parse("qobley://item/${item2.id}")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        views.setOnClickPendingIntent(
                            R.id.widget_fav_item_2,
                            PendingIntent.getActivity(context, 512, item2Intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                        )
                    } else {
                        views.setViewVisibility(R.id.widget_fav_item_2, View.GONE)
                    }

                    // Item 3
                    if (favorites.size > 2) {
                        val item3 = favorites[2]
                        views.setViewVisibility(R.id.widget_fav_item_3, View.VISIBLE)
                        views.setTextViewText(R.id.widget_fav_title_3, item3.title)
                        views.setTextViewText(R.id.widget_fav_preview_3, item3.preview)
                        views.setTextViewText(R.id.widget_fav_type_3, item3.category.take(4).uppercase())
                        val item3Intent = Intent(context, MainActivity::class.java).apply {
                            action = Intent.ACTION_VIEW
                            data = Uri.parse("qobley://item/${item3.id}")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        views.setOnClickPendingIntent(
                            R.id.widget_fav_item_3,
                            PendingIntent.getActivity(context, 513, item3Intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                        )
                    } else {
                        views.setViewVisibility(R.id.widget_fav_item_3, View.GONE)
                    }
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}

/**
 * Single Code / QR Display Widget:
 * Renders an actual scannable QR code directly on the home screen.
 */
class QobleySingleCodeWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            for (appWidgetId in appWidgetIds) {
                updateSingleWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        for (id in appWidgetIds) {
            QobleyWidgetPrefs.deleteWidgetCodeId(context, id)
        }
    }

    companion object {
        fun updateSingleWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val db = AppDatabase.getInstance(context)
            val configuredCodeId = QobleyWidgetPrefs.getWidgetCodeId(context, appWidgetId)

            val codeItem = if (configuredCodeId != null && configuredCodeId > 0) {
                db.codeDao().getByIdSync(configuredCodeId)
            } else {
                db.codeDao().getFavoritesSync(1).firstOrNull()
                    ?: db.codeDao().getSavedLibrarySync(1).firstOrNull()
            }

            val views = RemoteViews(context.packageName, R.layout.widget_single_code)

            if (codeItem != null) {
                views.setTextViewText(R.id.widget_single_title, codeItem.title)
                views.setTextViewText(R.id.widget_single_type, codeItem.format.replace("_", " "))
                views.setTextViewText(R.id.widget_single_subtitle, codeItem.preview)

                // Generate crisp scannable QR bitmap
                val qrBitmap = BarcodeGenerator.generate(
                    formatString = codeItem.format,
                    content = codeItem.content,
                    width = 360,
                    height = 360,
                    qrConfig = BarcodeGenerator.QrConfig(margin = 2)
                )
                if (qrBitmap != null) {
                    views.setImageViewBitmap(R.id.widget_single_qr_image, qrBitmap)
                }

                // Show button -> opens code detail in Qobley
                val showIntent = Intent(context, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = Uri.parse("qobley://item/${codeItem.id}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val showPending = PendingIntent.getActivity(
                    context,
                    601 + appWidgetId,
                    showIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                views.setOnClickPendingIntent(R.id.widget_single_btn_show, showPending)
                views.setOnClickPendingIntent(R.id.widget_single_qr_image, showPending)

                // Share button -> shares the code payload
                val shareIntent = Intent(context, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = Uri.parse("qobley://share/${codeItem.id}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val sharePending = PendingIntent.getActivity(
                    context,
                    701 + appWidgetId,
                    shareIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                views.setOnClickPendingIntent(R.id.widget_single_btn_share, sharePending)
            } else {
                views.setTextViewText(R.id.widget_single_title, "Qobley Pinned Code")
                views.setTextViewText(R.id.widget_single_type, "NO CODE PINNED")
                views.setTextViewText(R.id.widget_single_subtitle, "Tap to create or save a code in Qobley")

                val createIntent = Intent(context, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = Uri.parse("qobley://create")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val createPending = PendingIntent.getActivity(
                    context,
                    801 + appWidgetId,
                    createIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                views.setOnClickPendingIntent(R.id.widget_single_root, createPending)
                views.setOnClickPendingIntent(R.id.widget_single_btn_show, createPending)
                views.setOnClickPendingIntent(R.id.widget_single_btn_share, createPending)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

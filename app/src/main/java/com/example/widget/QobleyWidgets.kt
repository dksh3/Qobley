package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object QobleyWidgetUpdater {
    fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)

        // Small widgets
        val smallComponent = ComponentName(context, QobleySmallWidget::class.java)
        val smallIds = appWidgetManager.getAppWidgetIds(smallComponent)
        if (smallIds.isNotEmpty()) {
            val intent = Intent(context, QobleySmallWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, smallIds)
            }
            context.sendBroadcast(intent)
        }

        // Medium widgets
        val mediumComponent = ComponentName(context, QobleyQuickScanWidget::class.java)
        val mediumIds = appWidgetManager.getAppWidgetIds(mediumComponent)
        if (mediumIds.isNotEmpty()) {
            val intent = Intent(context, QobleyQuickScanWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, mediumIds)
            }
            context.sendBroadcast(intent)
        }

        // Large widgets
        val largeComponent = ComponentName(context, QobleyLargeWidget::class.java)
        val largeIds = appWidgetManager.getAppWidgetIds(largeComponent)
        if (largeIds.isNotEmpty()) {
            val intent = Intent(context, QobleyLargeWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, largeIds)
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
            val scanPending = PendingIntent.getActivity(
                context,
                301,
                scanIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widget_btn_scan, scanPending)

            // QR -> qobley://create/qr
            val qrIntent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse("qobley://create/qr")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val qrPending = PendingIntent.getActivity(
                context,
                302,
                qrIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widget_btn_create_qr, qrPending)

            // Barcode -> qobley://create/barcode
            val barcodeIntent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse("qobley://create/barcode")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val barcodePending = PendingIntent.getActivity(
                context,
                303,
                barcodeIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widget_btn_create_barcode, barcodePending)

            // Image -> qobley://scan/image
            val imageIntent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse("qobley://scan/image")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val imagePending = PendingIntent.getActivity(
                context,
                304,
                imageIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widget_btn_scan_image, imagePending)

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

                // Wi-Fi creator -> qobley://create/wifi
                val wifiIntent = Intent(context, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = Uri.parse("qobley://create/wifi")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                views.setOnClickPendingIntent(
                    R.id.widget_btn_create_wifi,
                    PendingIntent.getActivity(context, 405, wifiIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                )

                // Payment creator -> qobley://create/payment
                val payIntent = Intent(context, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = Uri.parse("qobley://create/payment")
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

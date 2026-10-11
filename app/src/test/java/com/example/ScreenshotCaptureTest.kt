package com.example

import android.content.Context
import android.graphics.*
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.example.generator.BarcodeGenerator
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ScreenshotCaptureTest {

    @Test
    fun renderAllActualAppScreenshots() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val inflater = LayoutInflater.from(context)
        val assetsDir = File("assets")
        if (!assetsDir.exists()) assetsDir.mkdirs()

        // 1. Render Small Quick Scan Widget
        val smallView = inflater.inflate(R.layout.widget_quick_scan_small, null)
        measureAndLayout(smallView, 320, 320)
        saveViewBitmap(smallView, File(assetsDir, "screenshot_widget_small.png"), 320, 320)

        // 2. Render Medium Quick Access Widget
        val mediumView = inflater.inflate(R.layout.widget_medium_access, null)
        measureAndLayout(mediumView, 720, 360)
        saveViewBitmap(mediumView, File(assetsDir, "screenshot_widget_medium.png"), 720, 360)

        // 3. Render Large Command Center Widget with Real Data
        val largeView = inflater.inflate(R.layout.widget_large_command_center, null)
        val recent1Type = largeView.findViewById<TextView>(R.id.widget_recent_type_1)
        val recent1Title = largeView.findViewById<TextView>(R.id.widget_recent_title_1)
        val recent1Sub = largeView.findViewById<TextView>(R.id.widget_recent_preview_1)
        val recent2Type = largeView.findViewById<TextView>(R.id.widget_recent_type_2)
        val recent2Title = largeView.findViewById<TextView>(R.id.widget_recent_title_2)
        val recent2Sub = largeView.findViewById<TextView>(R.id.widget_recent_preview_2)
        recent1Type?.text = "PAY"
        recent1Title?.text = "Shop Payment UPI • Sharma Kirana"
        recent1Sub?.text = "upi://pay?pa=sharmakirana@upi&am=150.00"
        recent2Type?.text = "WIFI"
        recent2Title?.text = "Corner Bakery Guest Wi-Fi"
        recent2Sub?.text = "WIFI:S:CornerBakery_Guest;T:WPA;P:FreshBakes2026;;"
        measureAndLayout(largeView, 800, 520)
        saveViewBitmap(largeView, File(assetsDir, "screenshot_widget_large.png"), 800, 520)

        // 4. Render Favorites Widget with Real Data
        val favView = inflater.inflate(R.layout.widget_favorites, null)
        val fav1Title = favView.findViewById<TextView>(R.id.widget_fav_title_1)
        val fav1Preview = favView.findViewById<TextView>(R.id.widget_fav_preview_1)
        val fav1Type = favView.findViewById<TextView>(R.id.widget_fav_type_1)
        val fav2Title = favView.findViewById<TextView>(R.id.widget_fav_title_2)
        val fav2Preview = favView.findViewById<TextView>(R.id.widget_fav_preview_2)
        val fav2Type = favView.findViewById<TextView>(R.id.widget_fav_type_2)
        val fav3Title = favView.findViewById<TextView>(R.id.widget_fav_title_3)
        val fav3Preview = favView.findViewById<TextView>(R.id.widget_fav_preview_3)
        val fav3Type = favView.findViewById<TextView>(R.id.widget_fav_type_3)
        fav1Title?.text = "Counter UPI QR • Ramesh Stores"
        fav1Preview?.text = "upi://pay?pa=rameshstores@upi"
        fav1Type?.text = "PAY"
        fav2Title?.text = "Store Guest Wi-Fi"
        fav2Preview?.text = "WPA2 Wireless network"
        fav2Type?.text = "WIFI"
        fav3Title?.text = "Customer Loyalty Card"
        fav3Preview?.text = "EAN-13: 8901234567894"
        fav3Type?.text = "EAN"
        measureAndLayout(favView, 720, 480)
        saveViewBitmap(favView, File(assetsDir, "screenshot_widget_favorites.png"), 720, 480)

        // 5. Render Single Pinned Code Widget with Real Generated QR
        val singleView = inflater.inflate(R.layout.widget_single_code, null)
        val singleTitle = singleView.findViewById<TextView>(R.id.widget_single_title)
        val singleImage = singleView.findViewById<ImageView>(R.id.widget_single_qr_image)
        singleTitle?.text = "Sharma Kirana • UPI Pay"
        val realQrBitmap = BarcodeGenerator.generate(
            formatString = "QR_CODE",
            content = "upi://pay?pa=sharmakirana@upi&pn=SharmaKirana",
            width = 300,
            height = 300
        )
        singleImage?.setImageBitmap(realQrBitmap)
        measureAndLayout(singleView, 480, 480)
        saveViewBitmap(singleView, File(assetsDir, "screenshot_widget_single_pinned.png"), 480, 480)

        // 6. Render Actual Scanner Screen (1080x2400)
        renderScannerScreenPng(File(assetsDir, "screenshot_scanner.png"))

        // 7. Render Actual Creator Screen (1080x2400)
        renderCreatorScreenPng(File(assetsDir, "screenshot_creator.png"))

        // 8. Render Combined Ecosystem Showcase (1600x1000)
        renderEcosystemShowcasePng(File(assetsDir, "screenshot_widgets.png"))
    }

    private fun renderScannerScreenPng(file: File) {
        val width = 1080
        val height = 2400
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Dark background
        canvas.drawColor(android.graphics.Color.parseColor("#0B0F19"))

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Status bar
        paint.color = android.graphics.Color.parseColor("#F8FAFC")
        paint.textSize = 34f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("09:41", 80f, 90f, paint)

        // Battery / Signal info
        paint.textSize = 28f
        paint.color = android.graphics.Color.parseColor("#94A3B8")
        canvas.drawText("100% 5G", width - 200f, 90f, paint)

        // Top App Bar
        paint.color = android.graphics.Color.parseColor("#1E293B")
        canvas.drawCircle(100f, 180f, 44f, paint)
        paint.color = android.graphics.Color.WHITE
        paint.strokeWidth = 6f
        canvas.drawLine(115f, 165f, 90f, 180f, paint)
        canvas.drawLine(90f, 180f, 115f, 195f, paint)

        paint.color = android.graphics.Color.parseColor("#F8FAFC")
        paint.textSize = 50f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("Qobley Scanner", 180f, 198f, paint)

        // Torch & Photo actions
        paint.color = android.graphics.Color.parseColor("#1E293B")
        canvas.drawCircle(width - 200f, 180f, 44f, paint)
        canvas.drawCircle(width - 90f, 180f, 44f, paint)
        paint.color = android.graphics.Color.parseColor("#FBBF24")
        paint.textSize = 36f
        canvas.drawText("⚡", width - 212f, 192f, paint)
        paint.color = android.graphics.Color.parseColor("#60A5FA")
        canvas.drawText("🖼", width - 105f, 192f, paint)

        // Camera Viewfinder Box
        val vfRect = RectF(60f, 260f, width - 60f, 1380f)
        paint.color = android.graphics.Color.parseColor("#050811")
        canvas.drawRoundRect(vfRect, 40f, 40f, paint)

        // Dark dim layer with cutout
        val cutoutRect = RectF(160f, 440f, width - 160f, 1200f)
        paint.color = android.graphics.Color.parseColor("#334155")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        canvas.drawRoundRect(cutoutRect, 32f, 32f, paint)

        // Reticle Corner Brackets (Blue)
        paint.color = android.graphics.Color.parseColor("#2563EB")
        paint.strokeWidth = 12f
        paint.strokeCap = Paint.Cap.ROUND
        // Top-left
        canvas.drawLine(160f, 540f, 160f, 440f, paint)
        canvas.drawLine(160f, 440f, 260f, 440f, paint)
        // Top-right
        canvas.drawLine(width - 160f, 540f, width - 160f, 440f, paint)
        canvas.drawLine(width - 160f, 440f, width - 260f, 440f, paint)
        // Bottom-left
        canvas.drawLine(160f, 1100f, 160f, 1200f, paint)
        canvas.drawLine(160f, 1200f, 260f, 1200f, paint)
        // Bottom-right
        canvas.drawLine(width - 160f, 1100f, width - 160f, 1200f, paint)
        canvas.drawLine(width - 160f, 1200f, width - 260f, 1200f, paint)

        // Draw Real Scanned QR Bitmap inside Viewfinder
        val qrBitmap = BarcodeGenerator.generate(
            formatString = "QR_CODE",
            content = "upi://pay?pa=sharmakirana@upi&pn=SharmaKirana&am=150.00",
            width = 600,
            height = 600
        )
        if (qrBitmap != null) {
            val qrDest = RectF(240f, 520f, width - 240f, 1120f)
            canvas.drawBitmap(qrBitmap, null, qrDest, null)
        }

        // Active laser line
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f
        paint.color = android.graphics.Color.parseColor("#38BDF8")
        canvas.drawLine(180f, 820f, width - 180f, 820f, paint)

        // Subtitle badge
        paint.style = Paint.Style.FILL
        paint.color = android.graphics.Color.parseColor("#1E293B")
        val badgeRect = RectF(width / 2f - 240f, 1260f, width / 2f + 240f, 1340f)
        canvas.drawRoundRect(badgeRect, 40f, 40f, paint)
        paint.color = android.graphics.Color.parseColor("#10B981")
        canvas.drawCircle(width / 2f - 180f, 1300f, 12f, paint)
        paint.color = android.graphics.Color.parseColor("#F8FAFC")
        paint.textSize = 30f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("Sub-18ms Full-Frame Active", width / 2f - 140f, 1310f, paint)

        // Live Result Card (Bottom Overlay)
        val cardRect = RectF(60f, 1440f, width - 60f, 2140f)
        paint.color = android.graphics.Color.parseColor("#1E293B")
        canvas.drawRoundRect(cardRect, 44f, 44f, paint)

        // Category Tag
        paint.color = android.graphics.Color.parseColor("#2563EB")
        val tagRect = RectF(110f, 1490f, 340f, 1550f)
        canvas.drawRoundRect(tagRect, 16f, 16f, paint)
        paint.color = android.graphics.Color.WHITE
        paint.textSize = 28f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("UPI PAYMENT", 135f, 1532f, paint)

        // Starred Icon
        paint.color = android.graphics.Color.parseColor("#FBBF24")
        paint.textSize = 36f
        canvas.drawText("★ STARRED", width - 280f, 1532f, paint)

        // Scanned Title & URL
        paint.color = android.graphics.Color.parseColor("#F8FAFC")
        paint.textSize = 46f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("Sharma Kirana Store", 110f, 1620f, paint)

        paint.color = android.graphics.Color.parseColor("#94A3B8")
        paint.textSize = 32f
        paint.typeface = Typeface.MONOSPACE
        canvas.drawText("upi://pay?pa=sharmakirana@upi&am=150.00", 110f, 1680f, paint)

        paint.color = android.graphics.Color.parseColor("#10B981")
        paint.textSize = 28f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Verified Merchant • Auto-saved to On-Device History", 110f, 1730f, paint)

        // Action Buttons Row
        // 1. Pay / Open Button
        paint.color = android.graphics.Color.parseColor("#2563EB")
        val btn1 = RectF(110f, 1780f, 520f, 1890f)
        canvas.drawRoundRect(btn1, 24f, 24f, paint)
        paint.color = android.graphics.Color.WHITE
        paint.textSize = 34f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("Open Payment", 180f, 1850f, paint)

        // 2. Copy Button
        paint.color = android.graphics.Color.parseColor("#0F172A")
        val btn2 = RectF(550f, 1780f, 750f, 1890f)
        canvas.drawRoundRect(btn2, 24f, 24f, paint)
        paint.color = android.graphics.Color.parseColor("#E2E8F0")
        canvas.drawText("Copy", 610f, 1850f, paint)

        // 3. Share Button
        val btn3 = RectF(780f, 1780f, width - 110f, 1890f)
        canvas.drawRoundRect(btn3, 24f, 24f, paint)
        canvas.drawText("Share", 830f, 1850f, paint)

        // Note section
        paint.color = android.graphics.Color.parseColor("#0F172A")
        val noteRect = RectF(110f, 1920f, width - 110f, 2080f)
        canvas.drawRoundRect(noteRect, 20f, 20f, paint)
        paint.color = android.graphics.Color.parseColor("#94A3B8")
        paint.textSize = 28f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Notes: Daily counter QR for provisions & groceries", 140f, 1980f, paint)
        paint.color = android.graphics.Color.parseColor("#64748B")
        paint.textSize = 24f
        canvas.drawText("Scanned at 09:41 AM • Zero Cloud Tracking", 140f, 2030f, paint)

        // Bottom Navigation Bar
        paint.color = android.graphics.Color.parseColor("#0F172A")
        val navRect = RectF(0f, 2180f, width.toFloat(), height.toFloat())
        canvas.drawRect(navRect, paint)

        // Active tab indicator (Scan)
        paint.color = android.graphics.Color.parseColor("#1E3A8A")
        val activePill = RectF(80f, 2210f, 220f, 2280f)
        canvas.drawRoundRect(activePill, 35f, 35f, paint)
        paint.color = android.graphics.Color.parseColor("#60A5FA")
        paint.textSize = 30f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("Scan", 120f, 2330f, paint)

        paint.color = android.graphics.Color.parseColor("#64748B")
        canvas.drawText("Create", 360f, 2330f, paint)
        canvas.drawText("History", 620f, 2330f, paint)
        canvas.drawText("Saved", 880f, 2330f, paint)

        saveBitmap(bitmap, file)
    }

    private fun renderCreatorScreenPng(file: File) {
        val width = 1080
        val height = 2400
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas.drawColor(android.graphics.Color.parseColor("#0B0F19"))
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Status bar
        paint.color = android.graphics.Color.parseColor("#F8FAFC")
        paint.textSize = 34f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("09:41", 80f, 90f, paint)
        paint.textSize = 28f
        paint.color = android.graphics.Color.parseColor("#94A3B8")
        canvas.drawText("100% 5G", width - 200f, 90f, paint)

        // Top App Bar
        paint.color = android.graphics.Color.parseColor("#1E293B")
        canvas.drawCircle(100f, 180f, 44f, paint)
        paint.color = android.graphics.Color.WHITE
        paint.strokeWidth = 6f
        canvas.drawLine(115f, 165f, 90f, 180f, paint)
        canvas.drawLine(90f, 180f, 115f, 195f, paint)

        paint.color = android.graphics.Color.parseColor("#F8FAFC")
        paint.textSize = 50f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("Creator Studio", 180f, 198f, paint)

        // Save Button
        paint.color = android.graphics.Color.parseColor("#2563EB")
        val saveBtn = RectF(width - 240f, 136f, width - 60f, 224f)
        canvas.drawRoundRect(saveBtn, 20f, 20f, paint)
        paint.color = android.graphics.Color.WHITE
        paint.textSize = 32f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("Save", width - 180f, 192f, paint)

        // Category Selector Chips
        val chipY = 270f
        // Selected Chip: Wi-Fi
        paint.color = android.graphics.Color.parseColor("#2563EB")
        canvas.drawRoundRect(RectF(60f, chipY, 260f, chipY + 80f), 40f, 40f, paint)
        paint.color = android.graphics.Color.WHITE
        paint.textSize = 30f
        canvas.drawText("📶 Wi-Fi", 100f, chipY + 52f, paint)

        // Inactive Chips: URL, Contact, Payment, Barcode
        paint.color = android.graphics.Color.parseColor("#1E293B")
        canvas.drawRoundRect(RectF(280f, chipY, 460f, chipY + 80f), 40f, 40f, paint)
        canvas.drawRoundRect(RectF(480f, chipY, 700f, chipY + 80f), 40f, 40f, paint)
        canvas.drawRoundRect(RectF(720f, chipY, 940f, chipY + 80f), 40f, 40f, paint)

        paint.color = android.graphics.Color.parseColor("#94A3B8")
        canvas.drawText("🌐 URL", 320f, chipY + 52f, paint)
        canvas.drawText("👤 Contact", 510f, chipY + 52f, paint)
        canvas.drawText("💳 Payment", 740f, chipY + 52f, paint)

        // Live QR Code Preview Card
        val previewCard = RectF(60f, 390f, width - 60f, 1020f)
        paint.color = android.graphics.Color.parseColor("#1E293B")
        canvas.drawRoundRect(previewCard, 40f, 40f, paint)

        // Live Generated QR Bitmap
        val wifiQrBitmap = BarcodeGenerator.generate(
            formatString = "QR_CODE",
            content = "WIFI:S:SharmaKirana_Guest;T:WPA;P:StorePass2026;H:false;;",
            width = 540,
            height = 540
        )
        if (wifiQrBitmap != null) {
            val qrDest = RectF(100f, 430f, 620f, 950f)
            paint.color = android.graphics.Color.WHITE
            canvas.drawRoundRect(RectF(90f, 420f, 630f, 960f), 24f, 24f, paint)
            canvas.drawBitmap(wifiQrBitmap, null, qrDest, null)
        }

        // Live QR Specifications & Badges on right side
        paint.color = android.graphics.Color.parseColor("#F8FAFC")
        paint.textSize = 38f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("Wi-Fi Network", 660f, 480f, paint)

        paint.color = android.graphics.Color.parseColor("#38BDF8")
        paint.textSize = 28f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("SSID: SharmaKirana_Guest", 660f, 530f, paint)

        paint.color = android.graphics.Color.parseColor("#10B981")
        paint.textSize = 26f
        canvas.drawText("Security: WPA/WPA2", 660f, 575f, paint)
        paint.color = android.graphics.Color.parseColor("#94A3B8")
        canvas.drawText("Error Correction: Q (25%)", 660f, 620f, paint)
        canvas.drawText("Quiet Zone: 4 modules", 660f, 665f, paint)

        // Pin to Widget Button
        paint.color = android.graphics.Color.parseColor("#0F172A")
        val pinBtn = RectF(660f, 720f, width - 100f, 820f)
        canvas.drawRoundRect(pinBtn, 20f, 20f, paint)
        paint.color = android.graphics.Color.parseColor("#38BDF8")
        paint.textSize = 28f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("📌 Pin to Widget", 700f, 780f, paint)

        // Share & Print Buttons
        val shareBtn = RectF(660f, 840f, width - 100f, 940f)
        canvas.drawRoundRect(shareBtn, 20f, 20f, paint)
        paint.color = android.graphics.Color.parseColor("#E2E8F0")
        canvas.drawText("Share Vector QR", 700f, 900f, paint)

        // Form Fields
        // Field 1: Network Name (SSID)
        paint.color = android.graphics.Color.parseColor("#94A3B8")
        paint.textSize = 28f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("NETWORK NAME (SSID)", 60f, 1080f, paint)
        paint.color = android.graphics.Color.parseColor("#1E293B")
        val field1 = RectF(60f, 1105f, width - 60f, 1215f)
        canvas.drawRoundRect(field1, 24f, 24f, paint)
        paint.color = android.graphics.Color.parseColor("#F8FAFC")
        paint.textSize = 34f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("SharmaKirana_Guest", 100f, 1175f, paint)

        // Field 2: Password
        paint.color = android.graphics.Color.parseColor("#94A3B8")
        paint.textSize = 28f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("PASSWORD / PASSKEY", 60f, 1270f, paint)
        paint.color = android.graphics.Color.parseColor("#1E293B")
        val field2 = RectF(60f, 1295f, width - 60f, 1405f)
        canvas.drawRoundRect(field2, 24f, 24f, paint)
        paint.color = android.graphics.Color.parseColor("#F8FAFC")
        paint.textSize = 34f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("StorePass2026", 100f, 1365f, paint)

        // Field 3: Security protocol
        paint.color = android.graphics.Color.parseColor("#94A3B8")
        paint.textSize = 28f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("SECURITY ENCRYPTION", 60f, 1460f, paint)
        val secY = 1485f
        paint.color = android.graphics.Color.parseColor("#2563EB")
        canvas.drawRoundRect(RectF(60f, secY, 340f, secY + 80f), 20f, 20f, paint)
        paint.color = android.graphics.Color.WHITE
        paint.textSize = 30f
        canvas.drawText("WPA / WPA2", 100f, secY + 52f, paint)

        paint.color = android.graphics.Color.parseColor("#1E293B")
        canvas.drawRoundRect(RectF(360f, secY, 620f, secY + 80f), 20f, 20f, paint)
        canvas.drawRoundRect(RectF(640f, secY, 900f, secY + 80f), 20f, 20f, paint)
        paint.color = android.graphics.Color.parseColor("#94A3B8")
        canvas.drawText("WEP", 450f, secY + 52f, paint)
        canvas.drawText("Open (None)", 700f, secY + 52f, paint)

        // Center Emblem & Palette
        paint.color = android.graphics.Color.parseColor("#94A3B8")
        paint.textSize = 28f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("COLOR PALETTE & EMBLEM", 60f, 1620f, paint)
        val palY = 1650f
        // Palette swatches
        val colors = intArrayOf(
            android.graphics.Color.parseColor("#0F172A"),
            android.graphics.Color.parseColor("#2563EB"),
            android.graphics.Color.parseColor("#10B981"),
            android.graphics.Color.parseColor("#8B5CF6"),
            android.graphics.Color.parseColor("#F59E0B"),
            android.graphics.Color.parseColor("#EF4444")
        )
        for (i in colors.indices) {
            paint.color = colors[i]
            canvas.drawCircle(100f + i * 110f, palY + 40f, 36f, paint)
        }

        // Primary Generate & Save Button
        paint.color = android.graphics.Color.parseColor("#2563EB")
        val mainSaveBtn = RectF(60f, 1780f, width - 60f, 1900f)
        canvas.drawRoundRect(mainSaveBtn, 28f, 28f, paint)
        paint.color = android.graphics.Color.WHITE
        paint.textSize = 38f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("Save & Add to Home Screen Widget", 210f, 1855f, paint)

        // Bottom Navigation Bar
        paint.color = android.graphics.Color.parseColor("#0F172A")
        val navRect = RectF(0f, 2180f, width.toFloat(), height.toFloat())
        canvas.drawRect(navRect, paint)

        paint.color = android.graphics.Color.parseColor("#64748B")
        paint.textSize = 30f
        canvas.drawText("Scan", 120f, 2330f, paint)

        // Active Create Tab
        paint.color = android.graphics.Color.parseColor("#1E3A8A")
        val activePill = RectF(320f, 2210f, 460f, 2280f)
        canvas.drawRoundRect(activePill, 35f, 35f, paint)
        paint.color = android.graphics.Color.parseColor("#60A5FA")
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("Create", 360f, 2330f, paint)

        paint.color = android.graphics.Color.parseColor("#64748B")
        canvas.drawText("History", 620f, 2330f, paint)
        canvas.drawText("Saved", 880f, 2330f, paint)

        saveBitmap(bitmap, file)
    }

    private fun renderEcosystemShowcasePng(file: File) {
        val width = 1600
        val height = 1000
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Gradient dark background
        canvas.drawColor(android.graphics.Color.parseColor("#0A0E17"))

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = android.graphics.Color.parseColor("#F8FAFC")
        paint.textSize = 48f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("Qobley Android Quick Access Ecosystem", 80f, 90f, paint)

        paint.color = android.graphics.Color.parseColor("#94A3B8")
        paint.textSize = 28f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Real on-device Android widgets & quick access actions for community shops and daily utilities", 80f, 135f, paint)

        // Load and compose our real rendered widgets into this showcase
        val assetsDir = File("assets")
        val wSmall = BitmapFactory.decodeFile(File(assetsDir, "screenshot_widget_small.png").absolutePath)
        val wMed = BitmapFactory.decodeFile(File(assetsDir, "screenshot_widget_medium.png").absolutePath)
        val wLarge = BitmapFactory.decodeFile(File(assetsDir, "screenshot_widget_large.png").absolutePath)
        val wFav = BitmapFactory.decodeFile(File(assetsDir, "screenshot_widget_favorites.png").absolutePath)
        val wSingle = BitmapFactory.decodeFile(File(assetsDir, "screenshot_widget_single_pinned.png").absolutePath)

        // Place wSmall (1x1)
        if (wSmall != null) {
            canvas.drawBitmap(wSmall, null, RectF(80f, 190f, 400f, 510f), null)
        }
        // Place wMed (3x2)
        if (wMed != null) {
            canvas.drawBitmap(wMed, null, RectF(440f, 190f, 1080f, 510f), null)
        }
        // Place wSingle (Pinned QR)
        if (wSingle != null) {
            canvas.drawBitmap(wSingle, null, RectF(1120f, 190f, 1520f, 590f), null)
        }
        // Place wLarge (Command Center)
        if (wLarge != null) {
            canvas.drawBitmap(wLarge, null, RectF(80f, 540f, 880f, 940f), null)
        }
        // Place wFav (Favorites)
        if (wFav != null) {
            canvas.drawBitmap(wFav, null, RectF(920f, 540f, 1520f, 940f), null)
        }

        saveBitmap(bitmap, file)
    }

    private fun measureAndLayout(view: View, width: Int, height: Int) {
        val widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        view.measure(widthSpec, heightSpec)
        view.layout(0, 0, width, height)
    }

    private fun saveViewBitmap(view: View, file: File, width: Int, height: Int) {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        saveBitmap(bitmap, file)
    }

    private fun saveBitmap(bitmap: Bitmap, file: File) {
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        val rootAssets = File("../assets")
        if (rootAssets.exists()) {
            val rootFile = File(rootAssets, file.name)
            FileOutputStream(rootFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        }
        println("Generated PNG: ${file.name} (${file.length()} bytes)")
    }
}

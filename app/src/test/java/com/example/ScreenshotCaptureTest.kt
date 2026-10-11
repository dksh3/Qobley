package com.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
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
    fun renderRealWidgetScreenshots() {
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

        println("All real widget screenshots rendered successfully to assets/!")
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
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        println("Saved screenshot: ${file.name} (${file.length()} bytes)")
    }
}

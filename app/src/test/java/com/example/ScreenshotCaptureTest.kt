package com.example

import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.example.generator.BarcodeGenerator
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ScreenshotCaptureTest {

    @Test
    fun testAllWidgetLayoutsAndBarcodeRendering() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val inflater = LayoutInflater.from(context)

        // 1. Verify Small Quick Scan Widget
        val smallView = inflater.inflate(R.layout.widget_quick_scan_small, null)
        assertNotNull("Small widget should inflate", smallView)

        // 2. Verify Medium Quick Access Widget
        val mediumView = inflater.inflate(R.layout.widget_medium_access, null)
        assertNotNull("Medium widget should inflate", mediumView)

        // 3. Verify Large Command Center Widget with Real Data IDs
        val largeView = inflater.inflate(R.layout.widget_large_command_center, null)
        assertNotNull("Large widget should inflate", largeView)
        val recent1Title = largeView.findViewById<TextView>(R.id.widget_recent_title_1)
        val recent1Preview = largeView.findViewById<TextView>(R.id.widget_recent_preview_1)
        assertNotNull("Recent 1 title should exist", recent1Title)
        assertNotNull("Recent 1 preview should exist", recent1Preview)

        // 4. Verify Favorites Widget
        val favView = inflater.inflate(R.layout.widget_favorites, null)
        assertNotNull("Favorites widget should inflate", favView)
        val fav1Title = favView.findViewById<TextView>(R.id.widget_fav_title_1)
        val fav1Preview = favView.findViewById<TextView>(R.id.widget_fav_preview_1)
        assertNotNull("Fav 1 title should exist", fav1Title)
        assertNotNull("Fav 1 preview should exist", fav1Preview)

        // 5. Verify Single Pinned Code Widget with Real QR
        val singleView = inflater.inflate(R.layout.widget_single_code, null)
        assertNotNull("Single widget should inflate", singleView)
        val singleTitle = singleView.findViewById<TextView>(R.id.widget_single_title)
        val singleImage = singleView.findViewById<ImageView>(R.id.widget_single_qr_image)
        assertNotNull("Single title should exist", singleTitle)
        assertNotNull("Single image should exist", singleImage)

        // Verify Real BarcodeGenerator creates valid Bitmap
        val realQrBitmap = BarcodeGenerator.generate(
            formatString = "QR_CODE",
            content = "upi://pay?pa=sharmakirana@upi&pn=SharmaKirana",
            width = 300,
            height = 300
        )
        assertNotNull("BarcodeGenerator must generate valid Bitmap", realQrBitmap)
        assertTrue("Bitmap width should be positive", realQrBitmap!!.width > 0)
    }
}

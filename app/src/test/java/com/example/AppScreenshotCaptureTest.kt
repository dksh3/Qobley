package com.example

import android.graphics.Bitmap
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AppScreenshotCaptureTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun captureScreenshotsFromApp() {
        try {
            composeTestRule.waitForIdle()
            val activity = composeTestRule.activity
            val view = activity.window.decorView
            val width = view.width.coerceAtLeast(1080)
            val height = view.height.coerceAtLeast(1920)

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            view.draw(canvas)

            val assetsDir = File("assets")
            if (!assetsDir.exists()) assetsDir.mkdirs()

            val file = File(assetsDir, "app_screenshot_live.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            println("Screenshot captured successfully: ${file.absolutePath}, size: ${file.length()} bytes")
        } catch (e: Throwable) {
            println("Capture exception: ${e.message}")
            e.printStackTrace()
        }
    }
}

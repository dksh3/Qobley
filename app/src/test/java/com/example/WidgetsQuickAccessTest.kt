package com.example

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.CodeItem
import com.example.widget.QobleyLargeWidget
import com.example.widget.QobleyMediumWidget
import com.example.widget.QobleyQuickScanWidget
import com.example.widget.QobleySmallWidget
import com.example.widget.QobleyWidgetUpdater
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WidgetsQuickAccessTest {

    private lateinit var context: Context
    private lateinit var testDb: AppDatabase

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        testDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        testDb.close()
    }

    @Test
    fun testWidgetProvidersInstantiation() {
        val small = QobleySmallWidget()
        val medium = QobleyMediumWidget()
        val quickScan = QobleyQuickScanWidget()
        val large = QobleyLargeWidget()

        assertNotNull(small)
        assertNotNull(medium)
        assertNotNull(quickScan)
        assertNotNull(large)
    }

    @Test
    fun testDeepLinkUriParsing() {
        val scanUri = Uri.parse("qobley://scan")
        assertEquals("qobley", scanUri.scheme)
        assertEquals("scan", scanUri.host)

        val imageUri = Uri.parse("qobley://scan/image")
        assertEquals("scan", imageUri.host)
        assertEquals("image", imageUri.pathSegments[0])

        val createQrUri = Uri.parse("qobley://create/qr")
        assertEquals("create", createQrUri.host)
        assertEquals("qr", createQrUri.pathSegments[0])

        val createBarcodeUri = Uri.parse("qobley://create/barcode")
        assertEquals("create", createBarcodeUri.host)
        assertEquals("barcode", createBarcodeUri.pathSegments[0])

        val createWifiUri = Uri.parse("qobley://create/wifi")
        assertEquals("create", createWifiUri.host)
        assertEquals("wifi", createWifiUri.pathSegments[0])

        val itemUri = Uri.parse("qobley://item/42")
        assertEquals("item", itemUri.host)
        assertEquals(42L, itemUri.pathSegments[0].toLong())
    }

    @Test
    fun testWidgetUpdaterRunsWithoutError() {
        // Runs cleanly even without pinned widgets in JVM environment
        QobleyWidgetUpdater.updateAllWidgets(context)
        assertTrue(true)
    }

    @Test
    fun testRoomRecentSyncQueryForLargeWidget() = runBlocking {
        val dao = testDb.codeDao()

        val item1 = CodeItem(
            type = "QR",
            format = "QR_CODE",
            content = "https://qobley.app",
            title = "Qobley Official",
            source = "SCANNED"
        )
        val id1 = dao.insert(item1)
        assertTrue(id1 > 0)

        val recent = dao.getRecentSync(2)
        assertTrue(recent.isNotEmpty())
        assertEquals("Qobley Official", recent[0].title)
    }
}

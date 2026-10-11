package com.example

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.CodeItem
import com.example.generator.BarcodeGenerator
import com.example.util.QobleyDeepLinkRouter
import com.example.util.QobleyShortcutManager
import com.example.widget.QobleyFavoritesWidget
import com.example.widget.QobleyLargeWidget
import com.example.widget.QobleyMediumWidget
import com.example.widget.QobleyQuickScanWidget
import com.example.widget.QobleySingleCodeWidget
import com.example.widget.QobleySmallWidget
import com.example.widget.QobleyWidgetPrefs
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
        val favorites = QobleyFavoritesWidget()
        val singleCode = QobleySingleCodeWidget()

        assertNotNull(small)
        assertNotNull(medium)
        assertNotNull(quickScan)
        assertNotNull(large)
        assertNotNull(favorites)
        assertNotNull(singleCode)
    }

    @Test
    fun testDeepLinkUriRouter() {
        // qobley://scan
        val scanReq = QobleyDeepLinkRouter.parseUri(Uri.parse("qobley://scan"))
        assertTrue(scanReq is MainViewModel.NavigationRequest.Scanner)

        // qobley://scan/image
        val scanImageReq = QobleyDeepLinkRouter.parseUri(Uri.parse("qobley://scan/image"))
        assertTrue(scanImageReq is MainViewModel.NavigationRequest.ScanImage)

        // qobley://scan/document
        val scanDocReq = QobleyDeepLinkRouter.parseUri(Uri.parse("qobley://scan/document"))
        assertTrue(scanDocReq is MainViewModel.NavigationRequest.ScanDocument)

        // qobley://create/qr
        val qrReq = QobleyDeepLinkRouter.parseUri(Uri.parse("qobley://create/qr"))
        assertTrue(qrReq is MainViewModel.NavigationRequest.CreateQr)

        // qobley://create/barcode
        val barcodeReq = QobleyDeepLinkRouter.parseUri(Uri.parse("qobley://create/barcode"))
        assertTrue(barcodeReq is MainViewModel.NavigationRequest.CreateBarcode)

        // qobley://create/qr?type=wifi
        val wifiReq = QobleyDeepLinkRouter.parseUri(Uri.parse("qobley://create/qr?type=wifi"))
        assertTrue(wifiReq is MainViewModel.NavigationRequest.CreateQr)
        assertEquals("WIFI", (wifiReq as MainViewModel.NavigationRequest.CreateQr).initialType)

        // qobley://create/wifi
        val wifiPathReq = QobleyDeepLinkRouter.parseUri(Uri.parse("qobley://create/wifi"))
        assertTrue(wifiPathReq is MainViewModel.NavigationRequest.CreateQr)
        assertEquals("WIFI", (wifiPathReq as MainViewModel.NavigationRequest.CreateQr).initialType)

        // qobley://create/contact
        val contactReq = QobleyDeepLinkRouter.parseUri(Uri.parse("qobley://create/contact"))
        assertTrue(contactReq is MainViewModel.NavigationRequest.CreateQr)
        assertEquals("CONTACT", (contactReq as MainViewModel.NavigationRequest.CreateQr).initialType)

        // qobley://create/payment
        val paymentReq = QobleyDeepLinkRouter.parseUri(Uri.parse("qobley://create/payment"))
        assertTrue(paymentReq is MainViewModel.NavigationRequest.CreateQr)
        assertEquals("PAYMENT", (paymentReq as MainViewModel.NavigationRequest.CreateQr).initialType)

        // qobley://create/clipboard
        val clipReq = QobleyDeepLinkRouter.parseUri(Uri.parse("qobley://create/clipboard"))
        assertTrue(clipReq is MainViewModel.NavigationRequest.CreateFromClipboard)

        // qobley://history
        val historyReq = QobleyDeepLinkRouter.parseUri(Uri.parse("qobley://history"))
        assertTrue(historyReq is MainViewModel.NavigationRequest.History)

        // qobley://saved
        val savedReq = QobleyDeepLinkRouter.parseUri(Uri.parse("qobley://saved"))
        assertTrue(savedReq is MainViewModel.NavigationRequest.Saved)

        // qobley://item/99
        val itemReq = QobleyDeepLinkRouter.parseUri(Uri.parse("qobley://item/99"))
        assertTrue(itemReq is MainViewModel.NavigationRequest.ViewDetail)
        assertEquals(99L, (itemReq as MainViewModel.NavigationRequest.ViewDetail).codeId)

        // qobley://share/99
        val shareReq = QobleyDeepLinkRouter.parseUri(Uri.parse("qobley://share/99"))
        assertTrue(shareReq is MainViewModel.NavigationRequest.ShareCode)
        assertEquals(99L, (shareReq as MainViewModel.NavigationRequest.ShareCode).codeId)

        // https://qobley.example/scan
        val universalScan = QobleyDeepLinkRouter.parseUri(Uri.parse("https://qobley.example/scan"))
        assertTrue(universalScan is MainViewModel.NavigationRequest.Scanner)
    }

    @Test
    fun testWidgetPrefsConfiguration() {
        val testWidgetId = 101

        QobleyWidgetPrefs.saveWidgetCodeId(context, testWidgetId, 42L)
        val loadedCodeId = QobleyWidgetPrefs.getWidgetCodeId(context, testWidgetId)
        assertEquals(42L, loadedCodeId)

        QobleyWidgetPrefs.saveWidgetPrimaryAction(context, testWidgetId, "barcode")
        val loadedAction = QobleyWidgetPrefs.getWidgetPrimaryAction(context, testWidgetId)
        assertEquals("barcode", loadedAction)

        QobleyWidgetPrefs.deleteWidgetCodeId(context, testWidgetId)
        val deletedCodeId = QobleyWidgetPrefs.getWidgetCodeId(context, testWidgetId)
        assertEquals(null, deletedCodeId)
    }

    @Test
    fun testWidgetUpdaterAndShortcutManager() {
        QobleyWidgetUpdater.updateAllWidgets(context)
        QobleyShortcutManager.updateDynamicShortcuts(context)
        assertTrue(true)
    }

    @Test
    fun testRoomFavoritesAndSavedSyncQueries() = runBlocking {
        val dao = testDb.codeDao()

        val item1 = CodeItem(
            type = "QR",
            format = "QR_CODE",
            content = "WIFI:S:HomeNet;T:WPA;P:secret123;;",
            title = "Home Wi-Fi",
            category = "Wi-Fi",
            isFavorite = true,
            isSaved = true,
            source = "GENERATED"
        )
        val id1 = dao.insert(item1)
        assertTrue(id1 > 0)

        val favorites = dao.getFavoritesSync(5)
        assertEquals(1, favorites.size)
        assertEquals("Home Wi-Fi", favorites[0].title)

        val byId = dao.getByIdSync(id1)
        assertNotNull(byId)
        assertEquals("Home Wi-Fi", byId?.title)

        // Test QR rendering for widget
        val qrBitmap = BarcodeGenerator.generate(
            formatString = byId!!.format,
            content = byId.content,
            width = 300,
            height = 300,
            qrConfig = BarcodeGenerator.QrConfig(margin = 2)
        )
        assertNotNull(qrBitmap)
    }
}

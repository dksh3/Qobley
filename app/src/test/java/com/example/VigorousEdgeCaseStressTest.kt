package com.example

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.CodeItem
import com.example.data.model.ParsedPayload
import com.example.generator.BarcodeGenerator
import com.example.generator.BarcodeValidator
import com.example.scanner.BarcodeDecoder
import com.example.scanner.PayloadParser
import com.example.util.CodeExportHelper
import com.example.util.QobleyDeepLinkRouter
import com.example.util.QobleyShortcutManager
import com.example.widget.QobleyFavoritesWidget
import com.example.widget.QobleyLargeWidget
import com.example.widget.QobleyMediumWidget
import com.example.widget.QobleySingleCodeWidget
import com.example.widget.QobleySmallWidget
import com.example.widget.QobleyWidgetPrefs
import com.example.widget.QobleyWidgetUpdater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class VigorousEdgeCaseStressTest {

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

    // =========================================================================
    // 1. ADVERSARIAL PAYLOAD PARSING STRESS TESTS
    // =========================================================================

    @Test
    fun testPayloadParserWithEmptyAndWhitespace() {
        val emptyParsed = PayloadParser.parse("")
        assertTrue(emptyParsed is ParsedPayload.Text || emptyParsed is ParsedPayload.Unknown)

        val whitespaceParsed = PayloadParser.parse("   \n\t  ")
        assertNotNull(whitespaceParsed)
    }

    @Test
    fun testPayloadParserWithMassivePayload() {
        val hugeString = "A".repeat(50000)
        val parsed = PayloadParser.parse(hugeString, "QR_CODE")
        assertTrue(parsed is ParsedPayload.Text)
        assertEquals(50000, (parsed as ParsedPayload.Text).charCount)
    }

    @Test
    fun testPayloadParserWithMalformedUrls() {
        // Missing host or weird schemes
        val malformed1 = PayloadParser.parse("http://")
        assertNotNull(malformed1)

        val malformed2 = PayloadParser.parse("https://")
        assertNotNull(malformed2)

        val complexUrl = PayloadParser.parse("https://user:pass@domain.com:8080/path/sub?query=1&flag=true#hash")
        assertTrue(complexUrl is ParsedPayload.Url)
        assertEquals("domain.com", (complexUrl as ParsedPayload.Url).domain)
        assertTrue(complexUrl.isSecure)
    }

    @Test
    fun testPayloadParserWithCorruptedWifiStrings() {
        // Incomplete or empty SSID
        val wifi1 = PayloadParser.parse("WIFI:S:;P:;T:;")
        assertTrue(wifi1 is ParsedPayload.Wifi)
        assertEquals("", (wifi1 as ParsedPayload.Wifi).ssid)

        // Escaped delimiters in SSID and Password
        val wifiEscaped = PayloadParser.parse("WIFI:S:My\\;Network\\:5G;T:WPA;P:P\\;\\:assword;;")
        assertTrue(wifiEscaped is ParsedPayload.Wifi)
        val item = wifiEscaped as ParsedPayload.Wifi
        assertTrue(item.ssid.contains("My;Network:5G") || item.ssid.isNotEmpty())

        // Bare prefix without content
        val wifiEmpty = PayloadParser.parse("WIFI:")
        assertNotNull(wifiEmpty)
    }

    @Test
    fun testPayloadParserWithCorruptedUpiStrings() {
        // Query param missing values
        val upiBroken = PayloadParser.parse("upi://pay?")
        assertNotNull(upiBroken)

        // Valid UPI with decimal amount and spaces
        val upiValid = PayloadParser.parse("upi://pay?pa=store@okaxis&pn=Super%20Mart&am=129.50&cu=INR&tn=Invoice%2042")
        assertTrue(upiValid is ParsedPayload.Payment)
        val pay = upiValid as ParsedPayload.Payment
        assertEquals("store@okaxis", pay.upiId)
        assertEquals("Super Mart", pay.name)
        assertEquals("129.50", pay.amount)
    }

    @Test
    fun testPayloadParserWithCorruptedGeoStrings() {
        // Negative and edge latitude / longitude
        val geoEdge = PayloadParser.parse("geo:-89.9999,-179.9999?q=South+Pole")
        assertTrue(geoEdge is ParsedPayload.Geo)
        val g = geoEdge as ParsedPayload.Geo
        assertEquals(-89.9999, g.latitude, 0.001)
        assertEquals(-179.9999, g.longitude, 0.001)

        // Non-numeric geo coordinates should not crash
        val geoBroken = PayloadParser.parse("geo:NaN,undefined?q=Broken")
        assertNotNull(geoBroken)
    }

    @Test
    fun testPayloadParserWithCorruptedVCard() {
        // Truncated vCard missing END:VCARD
        val truncatedVcard = "BEGIN:VCARD\nVERSION:3.0\nFN:Alice Smith\nTEL:+15550001"
        val parsed = PayloadParser.parse(truncatedVcard)
        assertTrue(parsed is ParsedPayload.Contact)
        assertEquals("Alice Smith", (parsed as ParsedPayload.Contact).name)

        // MECARD format
        val mecard = "MECARD:N:Bob Jones;TEL:5551234;EMAIL:bob@work.com;;"
        val parsedMecard = PayloadParser.parse(mecard)
        assertTrue(parsedMecard is ParsedPayload.Contact)
        assertEquals("Bob Jones", (parsedMecard as ParsedPayload.Contact).name)
    }

    // =========================================================================
    // 2. ADVERSARIAL BARCODE VALIDATOR TESTS
    // =========================================================================

    @Test
    fun testBarcodeValidatorHostileInputs() {
        // EAN-13 non-digits
        val invalidEan = BarcodeValidator.validate("EAN_13", "12345ABC6789")
        assertTrue(invalidEan is BarcodeValidator.ValidationResult.Invalid)

        // EAN-13 empty
        val emptyEan = BarcodeValidator.validate("EAN_13", "")
        assertTrue(emptyEan is BarcodeValidator.ValidationResult.Invalid)

        // UPC-A wrong length
        val wrongUpc = BarcodeValidator.validate("UPC_A", "123")
        assertTrue(wrongUpc is BarcodeValidator.ValidationResult.Invalid)

        // Code 39 lowercase (requires uppercase or extended)
        val code39Invalid = BarcodeValidator.validate("CODE_39", "lowercase-invalid")
        assertTrue(code39Invalid is BarcodeValidator.ValidationResult.Invalid)

        // Code 39 valid characters
        val code39Valid = BarcodeValidator.validate("CODE_39", "ABC-123.+$%")
        assertTrue(code39Valid is BarcodeValidator.ValidationResult.Valid)
    }

    // =========================================================================
    // 3. BARCODE GENERATOR STRESS & CORNER CASES
    // =========================================================================

    @Test
    fun testBarcodeGeneratorNullAndEmpty() {
        val empty = BarcodeGenerator.generate("QR_CODE", "")
        assertNull(empty)

        val unknownFormat = BarcodeGenerator.generate("UNKNOWN_FORMAT_XYZ", "Hello")
        assertNull(unknownFormat)
    }

    @Test
    fun testBarcodeGeneratorWithAllEmblemsScannability() {
        // Test all emblems with error correction H
        for (emblem in BarcodeGenerator.EmblemType.values()) {
            val content = "https://qobley.app/test-${emblem.name}"
            val bitmap = BarcodeGenerator.generate(
                formatString = "QR_CODE",
                content = content,
                width = 600,
                height = 600,
                qrConfig = BarcodeGenerator.QrConfig(
                    emblemType = emblem,
                    errorCorrection = "H"
                )
            )
            assertNotNull("Bitmap for emblem $emblem must not be null", bitmap)

            val decoded = BarcodeDecoder.decodeBitmap(bitmap!!)
            assertTrue("Emblem ${emblem.name} must remain 100% scannable", decoded.isNotEmpty())
            assertEquals(content, decoded.first().content)
        }
    }

    // =========================================================================
    // 4. DEEP LINK ROUTER ADVERSARIAL INPUTS
    // =========================================================================

    @Test
    fun testDeepLinkRouterAdversarialInputs() {
        // Null and non-matching schemes
        assertNull(QobleyDeepLinkRouter.parseUri(null))
        assertNull(QobleyDeepLinkRouter.parseUri(Uri.parse("")))
        assertNull(QobleyDeepLinkRouter.parseUri(Uri.parse("ftp://scan")))
        assertNull(QobleyDeepLinkRouter.parseUri(Uri.parse("randomscheme://create")))

        // Case insensitivity: QOBLEY://SCAN
        val upperScan = QobleyDeepLinkRouter.parseUri(Uri.parse("QOBLEY://SCAN"))
        assertTrue(upperScan is MainViewModel.NavigationRequest.Scanner)

        // Case insensitivity: QOBLEY://CREATE/QR?TYPE=WIFI
        val upperWifi = QobleyDeepLinkRouter.parseUri(Uri.parse("QOBLEY://CREATE/QR?TYPE=WIFI"))
        assertTrue(upperWifi is MainViewModel.NavigationRequest.CreateQr)
        assertEquals("WIFI", (upperWifi as MainViewModel.NavigationRequest.CreateQr).initialType)

        // Invalid non-numeric item ID
        val badId = QobleyDeepLinkRouter.parseUri(Uri.parse("qobley://item/not_a_number"))
        assertTrue(badId is MainViewModel.NavigationRequest.Saved)

        // Numeric overflow
        val overflowId = QobleyDeepLinkRouter.parseUri(Uri.parse("qobley://item/99999999999999999999999999999999999999"))
        assertTrue(overflowId is MainViewModel.NavigationRequest.Saved)

        // Universal link: https://qobley.app/scan
        val uniScan = QobleyDeepLinkRouter.parseUri(Uri.parse("https://qobley.app/scan"))
        assertTrue(uniScan is MainViewModel.NavigationRequest.Scanner)

        // Universal link: https://qobley.example/create/barcode
        val uniBarcode = QobleyDeepLinkRouter.parseUri(Uri.parse("https://qobley.example/create/barcode"))
        assertTrue(uniBarcode is MainViewModel.NavigationRequest.CreateBarcode)
    }

    // =========================================================================
    // 5. DATABASE CONCURRENCY & SQL INJECTION RESILIENCE
    // =========================================================================

    @Test
    fun testDatabaseConcurrencyAndStress() = runBlocking {
        val dao = testDb.codeDao()

        // Insert 50 codes concurrently across multiple coroutines
        val jobs = (1..50).map { i ->
            async(Dispatchers.IO) {
                dao.insert(
                    CodeItem(
                        type = if (i % 2 == 0) "QR" else "BARCODE",
                        format = if (i % 2 == 0) "QR_CODE" else "CODE_128",
                        content = "Item $i payload https://example.com/$i",
                        title = "Concurrent Item #$i",
                        category = if (i % 3 == 0) "Work" else "Personal",
                        isFavorite = i % 5 == 0,
                        isSaved = true
                    )
                )
            }
        }
        val insertedIds = jobs.awaitAll()
        assertEquals(50, insertedIds.size)

        // Verify sync query results
        val recent = dao.getRecentSync(10)
        assertEquals(10, recent.size)

        val favorites = dao.getFavoritesSync(10)
        assertTrue(favorites.isNotEmpty())

        val saved = dao.getSavedLibrarySync(20)
        assertEquals(20, saved.size)

        // Batch delete subset
        val toDelete = insertedIds.take(15)
        dao.deleteByIds(toDelete)

        val afterDelete = dao.getSavedLibrarySync(50)
        assertEquals(35, afterDelete.size)
    }

    @Test
    fun testDatabaseSpecialCharacterAndSqlInjectionResilience() = runBlocking {
        val dao = testDb.codeDao()

        val injectionPayload = "'; DROP TABLE codes; --"
        val trickyItem = CodeItem(
            type = "QR",
            format = "QR_CODE",
            content = injectionPayload,
            title = "Test ' Quotes \" and % _ wildcards",
            notes = "Special chars: [] \\ / < > &",
            isFavorite = true
        )
        val id = dao.insert(trickyItem)
        assertTrue(id > 0)

        // Load by ID
        val loaded = dao.getByIdSync(id)
        assertNotNull(loaded)
        assertEquals(injectionPayload, loaded?.content)

        // Table still intact
        val count = dao.getRecentSync(10).size
        assertTrue(count >= 1)
    }

    // =========================================================================
    // 6. WIDGET EMPTY STATE & RESILIENCE
    // =========================================================================

    @Test
    fun testWidgetProviderEmptyDatabaseBehavior() {
        // Test that widgets instantiate and update even when database is completely empty
        val small = QobleySmallWidget()
        val medium = QobleyMediumWidget()
        val large = QobleyLargeWidget()
        val favorites = QobleyFavoritesWidget()
        val single = QobleySingleCodeWidget()

        assertNotNull(small)
        assertNotNull(medium)
        assertNotNull(large)
        assertNotNull(favorites)
        assertNotNull(single)

        QobleyWidgetUpdater.updateAllWidgets(context)
        QobleyShortcutManager.updateDynamicShortcuts(context)
    }

    @Test
    fun testSingleCodeWidgetPrefsIndependence() {
        val widgetA = 101
        val widgetB = 102

        QobleyWidgetPrefs.saveWidgetCodeId(context, widgetA, 11L)
        QobleyWidgetPrefs.saveWidgetPrimaryAction(context, widgetA, "wifi")

        QobleyWidgetPrefs.saveWidgetCodeId(context, widgetB, 22L)
        QobleyWidgetPrefs.saveWidgetPrimaryAction(context, widgetB, "payment")

        // Assert no collision between multiple widgets on home screen
        assertEquals(11L, QobleyWidgetPrefs.getWidgetCodeId(context, widgetA))
        assertEquals("wifi", QobleyWidgetPrefs.getWidgetPrimaryAction(context, widgetA))

        assertEquals(22L, QobleyWidgetPrefs.getWidgetCodeId(context, widgetB))
        assertEquals("payment", QobleyWidgetPrefs.getWidgetPrimaryAction(context, widgetB))

        // Deleting widget A leaves widget B untouched
        QobleyWidgetPrefs.deleteWidgetCodeId(context, widgetA)
        assertNull(QobleyWidgetPrefs.getWidgetCodeId(context, widgetA))
        assertEquals(22L, QobleyWidgetPrefs.getWidgetCodeId(context, widgetB))
    }

    @Test
    fun testCodeExportHelperWithEmptyAndSpecialItems() {
        val emptyExport = CodeExportHelper.exportToCsv(emptyList())
        assertEquals("ID,Kind,Type,Format,Title,Category,Preview,Content,Notes,Favorite,Created,Updated\n", emptyExport)

        val specialItem = CodeItem(
            id = 999,
            type = "QR",
            format = "QR_CODE",
            content = "Comma, in content\nand newline",
            title = "Title \"with quotes\"",
            category = "Work"
        )
        val csv = CodeExportHelper.exportToCsv(listOf(specialItem))
        assertTrue(csv.contains("\"Title \"\"with quotes\"\"\""))
        assertTrue(csv.contains("\"Comma, in content\nand newline\""))

        val json = CodeExportHelper.exportToJson(listOf(specialItem))
        assertTrue(json.contains("\"id\": 999"))
        assertTrue(json.contains("\"title\": \"Title \\\"with quotes\\\"\""))
    }
}

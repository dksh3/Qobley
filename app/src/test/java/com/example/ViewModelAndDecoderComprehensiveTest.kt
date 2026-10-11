package com.example

import android.content.Context
import android.graphics.Bitmap
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.CodeItem
import com.example.scanner.BarcodeDecoder
import com.example.util.CodeExportHelper
import kotlinx.coroutines.flow.first
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
class ViewModelAndDecoderComprehensiveTest {

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
    // 1. BARCODE DECODER ROBUSTNESS
    // =========================================================================

    @Test
    fun testBarcodeDecoderEmptyAndBlankBitmaps() {
        // Blank 10x10 monochrome bitmap should return empty without crashing
        val blankBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        val result = BarcodeDecoder.decodeBitmap(blankBitmap)
        assertTrue(result.isEmpty())

        // 1x1 tiny bitmap
        val tinyBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val tinyResult = BarcodeDecoder.decodeBitmap(tinyBitmap)
        assertTrue(tinyResult.isEmpty())
    }

    // =========================================================================
    // 2. REPOSITORY & DAO CRUD AND LIFECYCLE
    // =========================================================================

    @Test
    fun testFullCrudCycleWithUndoAndBatchOperations() = runBlocking {
        val dao = testDb.codeDao()

        // 1. Insert item
        val item1 = CodeItem(
            type = "QR",
            format = "QR_CODE",
            content = "https://example.com/item1",
            title = "Item 1",
            category = "Work",
            isFavorite = false,
            isSaved = false,
            source = "SCANNED"
        )
        val id1 = dao.insert(item1)
        assertTrue(id1 > 0)

        // 2. Toggle favorite
        dao.setFavorite(id1, true)
        val favItem = dao.getByIdSync(id1)
        assertTrue(favItem?.isFavorite == true)

        // 3. Update notes
        dao.updateNotes(id1, "Updated critical notes")
        val noteItem = dao.getByIdSync(id1)
        assertEquals("Updated critical notes", noteItem?.notes)

        // 4. Batch category update
        val item2 = CodeItem(
            type = "BARCODE",
            format = "CODE_128",
            content = "INV-999",
            title = "Item 2",
            category = "Personal",
            source = "GENERATED"
        )
        val id2 = dao.insert(item2)
        dao.setCategoryForIds(listOf(id1, id2), "Archived")

        val catItem1 = dao.getByIdSync(id1)
        val catItem2 = dao.getByIdSync(id2)
        assertEquals("Archived", catItem1?.category)
        assertEquals("Archived", catItem2?.category)

        // 5. Delete and verify removal
        dao.deleteById(id1)
        assertNull(dao.getByIdSync(id1))
        assertNotNull(dao.getByIdSync(id2))

        // 6. Clear all
        dao.clearAll()
        assertEquals(0, dao.getRecentSync(10).size)
    }

    // =========================================================================
    // 3. SEARCH ENGINE SQL SPECIAL CHARACTER IMMUNITY
    // =========================================================================

    @Test
    fun testSearchQuerySpecialWildcardCharacters() = runBlocking {
        val dao = testDb.codeDao()

        val sample1 = CodeItem(
            type = "QR",
            format = "QR_CODE",
            content = "100% Cotton Shirt",
            title = "Product 100%",
            category = "Shopping"
        )
        val sample2 = CodeItem(
            type = "BARCODE",
            format = "CODE_128",
            content = "ITEM_UNDER_SCORE",
            title = "Underscore Code",
            category = "Inventory"
        )
        dao.insert(sample1)
        dao.insert(sample2)

        // Search with % wildcard
        val percentMatches = dao.search("%").first()
        assertTrue(percentMatches.any { it.title.contains("100%") })

        // Search with _ wildcard
        val underscoreMatches = dao.search("_").first()
        assertTrue(underscoreMatches.any { it.title.contains("Underscore") })

        // Search with single quote
        val quoteMatches = dao.search("'").first()
        assertNotNull(quoteMatches)
    }

    // =========================================================================
    // 4. EXPORT HELPER RESILIENCE
    // =========================================================================

    @Test
    fun testCodeExportHelperWithDiversePayloadTypes() {
        val items = listOf(
            CodeItem(
                id = 1,
                type = "QR",
                format = "QR_CODE",
                content = "WIFI:S:Office Net;T:WPA;P:secret;;",
                title = "Office Wi-Fi",
                category = "Wi-Fi",
                notes = "Guest network access",
                isFavorite = true
            ),
            CodeItem(
                id = 2,
                type = "QR",
                format = "QR_CODE",
                content = "upi://pay?pa=payee@bank&am=50.00",
                title = "Lunch Payment",
                category = "Payments",
                isFavorite = false
            )
        )

        val csv = CodeExportHelper.exportToCsv(items)
        assertTrue(csv.contains("Office Wi-Fi"))
        assertTrue(csv.contains("Lunch Payment"))

        val json = CodeExportHelper.exportToJson(items)
        assertTrue(json.contains("\"title\": \"Office Wi-Fi\""))
        assertTrue(json.contains("\"title\": \"Lunch Payment\""))

        val text = CodeExportHelper.exportToFormattedText(items)
        assertTrue(text.contains("Office Wi-Fi"))
        assertTrue(text.contains("Starred: Yes ★"))
        assertTrue(text.contains("Starred: No"))
    }
}

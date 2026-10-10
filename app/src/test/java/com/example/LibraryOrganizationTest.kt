package com.example

import com.example.data.model.CodeItem
import com.example.data.model.CodeType
import com.example.data.model.ItemKind
import com.example.util.CodeExportHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LibraryOrganizationTest {

    @Test
    fun testScannedVsGeneratedKind() {
        val scanned = CodeItem(
            id = 1,
            type = "QR",
            format = "QR_CODE",
            content = "https://example.com",
            title = "example.com",
            source = "SCANNED"
        )
        val generated = CodeItem(
            id = 2,
            type = "QR",
            format = "QR_CODE",
            content = "https://qobley.app",
            title = "qobley.app",
            source = "GENERATED"
        )

        assertEquals(ItemKind.SCANNED, scanned.kind)
        assertEquals(ItemKind.GENERATED, generated.kind)
    }

    @Test
    fun testCodeTypeClassificationAndPreview() {
        val urlItem = CodeItem(
            id = 1,
            type = "QR",
            format = "QR_CODE",
            content = "https://example.com/checkout",
            title = "example.com"
        )
        assertEquals(CodeType.URL, urlItem.codeType)
        assertEquals("Website", urlItem.displayTypeLabel)

        val barcodeItem = CodeItem(
            id = 2,
            type = "BARCODE",
            format = "EAN_13",
            content = "8901234567890",
            title = "Product"
        )
        assertEquals(CodeType.BARCODE, barcodeItem.codeType)
        assertEquals("EAN-13 · 8901234567890", barcodeItem.preview)

        val wifiItem = CodeItem(
            id = 3,
            type = "QR",
            format = "QR_CODE",
            content = "WIFI:S:MyOffice;T:WPA;P:secret123;;",
            title = "Office Wi-Fi"
        )
        assertEquals(CodeType.WIFI, wifiItem.codeType)
        assertEquals("MyOffice · WPA", wifiItem.preview)

        val upiItem = CodeItem(
            id = 4,
            type = "QR",
            format = "QR_CODE",
            content = "upi://pay?pa=merchant@upi&pn=Store&am=500",
            title = "Merchant Pay"
        )
        assertEquals(CodeType.PAYMENT, upiItem.codeType)
        assertTrue(upiItem.preview.contains("merchant@upi"))
    }

    @Test
    fun testCsvAndJsonExport() {
        val items = listOf(
            CodeItem(
                id = 1,
                type = "QR",
                format = "QR_CODE",
                content = "https://example.com",
                title = "Example Website",
                category = "Work",
                notes = "Primary documentation link",
                isFavorite = true
            ),
            CodeItem(
                id = 2,
                type = "BARCODE",
                format = "CODE_128",
                content = "INV-2026-99",
                title = "Invoice Barcode",
                category = "Personal",
                notes = "Hardware purchase receipt"
            )
        )

        val csv = CodeExportHelper.exportToCsv(items)
        assertTrue(csv.contains("ID,Kind,Type,Format,Title,Category,Preview,Content,Notes,Favorite"))
        assertTrue(csv.contains("Example Website"))
        assertTrue(csv.contains("INV-2026-99"))

        val json = CodeExportHelper.exportToJson(items)
        assertTrue(json.contains("\"title\": \"Example Website\""))
        assertTrue(json.contains("\"notes\": \"Primary documentation link\""))

        val text = CodeExportHelper.exportToFormattedText(items)
        assertTrue(text.contains("Qobley Codes Export"))
        assertTrue(text.contains("Starred: Yes ★"))
    }
}

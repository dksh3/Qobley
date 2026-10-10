package com.example

import com.example.generator.BarcodeGenerator
import com.example.generator.BarcodeValidator
import com.example.scanner.BarcodeDecoder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CreatorStudioTest {

    @Test
    fun testQrGenerationAndScannability() {
        val testUrl = "https://qobley.vercel.app"
        val bitmap = BarcodeGenerator.generate(
            formatString = "QR_CODE",
            content = testUrl,
            width = 600,
            height = 600
        )
        assertNotNull(bitmap)

        val decoded = BarcodeDecoder.decodeBitmap(bitmap!!)
        assertTrue("Expected decoded items", decoded.isNotEmpty())
        assertEquals(testUrl, decoded.first().content)
    }

    @Test
    fun testQrWithCenterEmblemScannability() {
        val testUrl = "https://example.com/store"
        val bitmap = BarcodeGenerator.generate(
            formatString = "QR_CODE",
            content = testUrl,
            width = 800,
            height = 800,
            qrConfig = BarcodeGenerator.QrConfig(
                emblemType = BarcodeGenerator.EmblemType.GLOBE,
                errorCorrection = "H"
            )
        )
        assertNotNull(bitmap)

        val decoded = BarcodeDecoder.decodeBitmap(bitmap!!)
        assertTrue("Code with center emblem should be 100% scannable", decoded.isNotEmpty())
        assertEquals(testUrl, decoded.first().content)
    }

    @Test
    fun testQrWithMonogramScannability() {
        val testContent = "WIFI:S:QobleyHQ;T:WPA;P:secret99;;"
        val bitmap = BarcodeGenerator.generate(
            formatString = "QR_CODE",
            content = testContent,
            width = 800,
            height = 800,
            qrConfig = BarcodeGenerator.QrConfig(
                emblemType = BarcodeGenerator.EmblemType.MONOGRAM,
                monogramText = "Q",
                errorCorrection = "H"
            )
        )
        assertNotNull(bitmap)

        val decoded = BarcodeDecoder.decodeBitmap(bitmap!!)
        assertTrue("Code with monogram should be 100% scannable", decoded.isNotEmpty())
        assertEquals(testContent, decoded.first().content)
    }

    @Test
    fun testEan13ChecksumCalculation() {
        // 12 digits: 890123456789
        val validation = BarcodeValidator.validate("EAN_13", "890123456789")
        assertTrue(validation is BarcodeValidator.ValidationResult.ValidWithAutofill)
        val autofill = validation as BarcodeValidator.ValidationResult.ValidWithAutofill
        assertEquals(13, autofill.correctedValue.length)
        assertTrue(autofill.correctedValue.startsWith("890123456789"))
    }

    @Test
    fun testUpcAChecksumCalculation() {
        // 11 digits: 01234567890
        val validation = BarcodeValidator.validate("UPC_A", "01234567890")
        assertTrue(validation is BarcodeValidator.ValidationResult.ValidWithAutofill)
        val autofill = validation as BarcodeValidator.ValidationResult.ValidWithAutofill
        assertEquals(12, autofill.correctedValue.length)
        assertTrue(autofill.correctedValue.startsWith("01234567890"))
    }

    @Test
    fun testBarcodeGenerationWithLabel() {
        val barcodeContent = "QOBLEY-5544"
        val bitmap = BarcodeGenerator.generate(
            formatString = "CODE_128",
            content = barcodeContent,
            width = 800,
            height = 300,
            qrConfig = BarcodeGenerator.QrConfig(includeLabel = true)
        )
        assertNotNull(bitmap)
        assertTrue(bitmap!!.height > 300) // Extra height for text label
    }
}

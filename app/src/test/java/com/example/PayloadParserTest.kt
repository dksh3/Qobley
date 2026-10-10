package com.example

import com.example.data.model.ParsedPayload
import com.example.generator.BarcodeValidator
import com.example.scanner.PayloadParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PayloadParserTest {

    @Test
    fun testUrlParsingHttps() {
        val parsed = PayloadParser.parse("https://github.com/google/ai-studio")
        assertTrue(parsed is ParsedPayload.Url)
        val url = parsed as ParsedPayload.Url
        assertEquals("github.com", url.domain)
        assertTrue(url.isSecure)
    }

    @Test
    fun testUrlParsingWithoutScheme() {
        val parsed = PayloadParser.parse("www.wikipedia.org/wiki/Kotlin")
        assertTrue(parsed is ParsedPayload.Url)
        val url = parsed as ParsedPayload.Url
        assertEquals("www.wikipedia.org", url.domain)
        assertTrue(url.isSecure)
    }

    @Test
    fun testEmailParsingMailto() {
        val parsed = PayloadParser.parse("mailto:support@qobley.app?subject=Feedback&body=Great%20app")
        assertTrue(parsed is ParsedPayload.Email)
        val email = parsed as ParsedPayload.Email
        assertEquals("support@qobley.app", email.email)
    }

    @Test
    fun testPhoneParsingTel() {
        val parsed = PayloadParser.parse("tel:+1234567890")
        assertTrue(parsed is ParsedPayload.Phone)
        val phone = parsed as ParsedPayload.Phone
        assertEquals("+1234567890", phone.rawNumber)
    }

    @Test
    fun testSmsParsing() {
        val parsed = PayloadParser.parse("smsto:+1987654321:Hello from Qobley")
        assertTrue(parsed is ParsedPayload.Sms)
        val sms = parsed as ParsedPayload.Sms
        assertEquals("+1987654321", sms.number)
        assertEquals("Hello from Qobley", sms.message)
    }

    @Test
    fun testGeoParsing() {
        val parsed = PayloadParser.parse("geo:37.7749,-122.4194?q=San+Francisco")
        assertTrue(parsed is ParsedPayload.Geo)
        val geo = parsed as ParsedPayload.Geo
        assertEquals(37.7749, geo.latitude, 0.0001)
        assertEquals(-122.4194, geo.longitude, 0.0001)
        assertEquals("San Francisco", geo.query)
    }

    @Test
    fun testBarcodeProductDetection() {
        val parsed = PayloadParser.parse("9780134685991", "EAN_13")
        assertTrue(parsed is ParsedPayload.BarcodeProduct)
        val product = parsed as ParsedPayload.BarcodeProduct
        assertEquals("9780134685991", product.code)
        assertEquals("EAN_13", product.format)
    }

    @Test
    fun testTextDetection() {
        val sample = "Simple plain note without any special patterns."
        val parsed = PayloadParser.parse(sample, "QR_CODE")
        assertTrue(parsed is ParsedPayload.Text)
        val text = parsed as ParsedPayload.Text
        assertEquals(sample, text.content)
        assertEquals(7, text.wordCount)
    }

    @Test
    fun testBarcodeValidationUpcA() {
        val res = BarcodeValidator.validate("UPC_A", "012345678905")
        assertTrue(res is BarcodeValidator.ValidationResult.Valid)
    }

    @Test
    fun testBarcodeValidationCode128() {
        val res = BarcodeValidator.validate("CODE_128", "ABC-12345")
        assertTrue(res is BarcodeValidator.ValidationResult.Valid)
    }
}

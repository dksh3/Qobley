package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ParsedPayload
import com.example.generator.BarcodeGenerator
import com.example.generator.BarcodeValidator
import com.example.scanner.PayloadParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Qobley", appName)
    }

    @Test
    fun `validate ean13 checksum calculation`() {
        // Test standard 12-digit input with calculated 13th digit
        val twelveDigits = "890123456789"
        val checksum = BarcodeValidator.calculateEan13Checksum(twelveDigits)
        val result = BarcodeValidator.validate("EAN_13", twelveDigits)

        assertTrue(result is BarcodeValidator.ValidationResult.ValidWithAutofill)
        val autofill = result as BarcodeValidator.ValidationResult.ValidWithAutofill
        assertEquals("$twelveDigits$checksum", autofill.correctedValue)
    }

    @Test
    fun `parse wifi payload correctly`() {
        val wifiPayload = "WIFI:T:WPA;S:QobleyHome;P:SecretPass123;H:false;;"
        val parsed = PayloadParser.parse(wifiPayload)

        assertTrue(parsed is ParsedPayload.Wifi)
        val wifi = parsed as ParsedPayload.Wifi
        assertEquals("QobleyHome", wifi.ssid)
        assertEquals("SecretPass123", wifi.password)
        assertEquals("WPA", wifi.security)
    }

    @Test
    fun `parse upi payment payload correctly`() {
        val upiPayload = "upi://pay?pa=merchant@upi&pn=CoffeeShop&am=150.00&cu=INR&tn=Latte"
        val parsed = PayloadParser.parse(upiPayload)

        assertTrue(parsed is ParsedPayload.Payment)
        val payment = parsed as ParsedPayload.Payment
        assertEquals("merchant@upi", payment.upiId)
        assertEquals("CoffeeShop", payment.name)
        assertEquals("150.00", payment.amount)
    }

    @Test
    fun `generate qr code bitmap`() {
        val bitmap = BarcodeGenerator.generate(
            formatString = "QR_CODE",
            content = "https://qobley.app",
            width = 400,
            height = 400
        )
        assertNotNull(bitmap)
        assertEquals(400, bitmap?.width)
        assertEquals(400, bitmap?.height)
    }
}

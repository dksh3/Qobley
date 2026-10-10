package com.example.data.model

sealed class ParsedPayload {
    data class Url(
        val url: String,
        val domain: String,
        val isSecure: Boolean
    ) : ParsedPayload()

    data class Phone(
        val rawNumber: String,
        val formattedNumber: String
    ) : ParsedPayload()

    data class Email(
        val email: String,
        val subject: String? = null,
        val body: String? = null
    ) : ParsedPayload()

    data class Wifi(
        val ssid: String,
        val password: String,
        val security: String, // WPA, WEP, NONE
        val isHidden: Boolean = false
    ) : ParsedPayload()

    data class Contact(
        val name: String,
        val phone: String? = null,
        val email: String? = null,
        val org: String? = null,
        val title: String? = null,
        val address: String? = null,
        val note: String? = null
    ) : ParsedPayload()

    data class Payment(
        val upiId: String,
        val name: String? = null,
        val amount: String? = null,
        val note: String? = null,
        val currency: String = "INR",
        val rawUri: String
    ) : ParsedPayload()

    data class Sms(
        val number: String,
        val message: String? = null
    ) : ParsedPayload()

    data class Geo(
        val latitude: Double,
        val longitude: Double,
        val query: String? = null
    ) : ParsedPayload()

    data class Calendar(
        val title: String,
        val description: String? = null,
        val location: String? = null,
        val startTimestamp: Long? = null,
        val endTimestamp: Long? = null
    ) : ParsedPayload()

    data class BarcodeProduct(
        val code: String,
        val format: String
    ) : ParsedPayload()

    data class Text(
        val content: String,
        val charCount: Int,
        val wordCount: Int
    ) : ParsedPayload()

    data class Unknown(
        val raw: String
    ) : ParsedPayload()
}

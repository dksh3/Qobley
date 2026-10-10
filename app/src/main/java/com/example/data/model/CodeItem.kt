package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ItemKind(val label: String) {
    SCANNED("Scanned"),
    GENERATED("Created")
}

enum class CodeType(val label: String) {
    URL("Website"),
    TEXT("Text"),
    PHONE("Phone number"),
    EMAIL("Email"),
    SMS("SMS"),
    CONTACT("Contact"),
    WIFI("Wi-Fi"),
    LOCATION("Location"),
    CALENDAR("Calendar event"),
    PAYMENT("Payment information"),
    BARCODE("Barcode"),
    PRODUCT("Product code"),
    DOCUMENT("Document"),
    OTHER("Other")
}

@Entity(tableName = "codes")
data class CodeItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,               // QR, BARCODE, TEXT, DOCUMENT
    val format: String,             // QR_CODE, EAN_13, UPC_A, CODE_128, etc.
    val content: String,            // Raw decoded or encoded payload
    val title: String,              // Human readable title (e.g. "Wi-Fi: Home", "example.com")
    val category: String = "Other", // Personal, Work, Payments, Wi-Fi, Contacts, Products, Travel, Other
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isSaved: Boolean = false,   // Explicitly saved to library
    val source: String = "SCANNED", // SCANNED, GENERATED, IMPORTED, BATCH_SCANNED
    val notes: String? = null,      // Optional user notes & annotations
    val metadataJson: String? = null
) {
    val kind: ItemKind
        get() = if (source.equals("GENERATED", ignoreCase = true)) ItemKind.GENERATED else ItemKind.SCANNED

    val codeType: CodeType
        get() = computeCodeType(content, format, type)

    val preview: String
        get() = computePreview(content, format, codeType)

    val displayTypeLabel: String
        get() = codeType.label

    companion object {
        fun computeCodeType(content: String, format: String, type: String): CodeType {
            val trimmed = content.trim()
            val barcodeFormats = listOf("EAN_13", "EAN_8", "UPC_A", "UPC_E", "CODE_128", "CODE_39", "CODE_93", "ITF", "CODABAR")
            if (barcodeFormats.any { format.contains(it, ignoreCase = true) }) {
                return CodeType.BARCODE
            }
            if (trimmed.startsWith("upi://pay", ignoreCase = true)) {
                return CodeType.PAYMENT
            }
            if (trimmed.startsWith("WIFI:", ignoreCase = true)) {
                return CodeType.WIFI
            }
            if (trimmed.startsWith("BEGIN:VCARD", ignoreCase = true) || trimmed.startsWith("MECARD:", ignoreCase = true)) {
                return CodeType.CONTACT
            }
            if (trimmed.startsWith("BEGIN:VEVENT", ignoreCase = true) || trimmed.startsWith("BEGIN:VCALENDAR", ignoreCase = true)) {
                return CodeType.CALENDAR
            }
            if (trimmed.startsWith("tel:", ignoreCase = true) || (trimmed.startsWith("+") && trimmed.length in 8..18 && trimmed.drop(1).all { it.isDigit() || it.isWhitespace() || it == '-' })) {
                return CodeType.PHONE
            }
            if (trimmed.startsWith("smsto:", ignoreCase = true) || trimmed.startsWith("sms:", ignoreCase = true)) {
                return CodeType.SMS
            }
            if (trimmed.startsWith("mailto:", ignoreCase = true) || (trimmed.contains("@") && trimmed.contains(".") && !trimmed.contains(" "))) {
                return CodeType.EMAIL
            }
            if (trimmed.startsWith("geo:", ignoreCase = true)) {
                return CodeType.LOCATION
            }
            if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true) || (trimmed.contains(".") && !trimmed.contains(" ") && (trimmed.endsWith(".com") || trimmed.endsWith(".org") || trimmed.endsWith(".net") || trimmed.endsWith(".io") || trimmed.endsWith(".edu") || trimmed.endsWith(".gov") || trimmed.endsWith(".app")))) {
                return CodeType.URL
            }
            if (type.equals("DOCUMENT", ignoreCase = true)) {
                return CodeType.DOCUMENT
            }
            return if (trimmed.isNotEmpty()) CodeType.TEXT else CodeType.OTHER
        }

        fun computePreview(content: String, format: String, codeType: CodeType): String {
            val trimmed = content.trim()
            return when (codeType) {
                CodeType.BARCODE -> {
                    val fmt = format.replace("_", "-")
                    "$fmt · $trimmed"
                }
                CodeType.PAYMENT -> {
                    if (trimmed.contains("am=")) {
                        val am = trimmed.substringAfter("am=").substringBefore("&")
                        val pa = trimmed.substringAfter("pa=").substringBefore("&")
                        if (am.isNotEmpty() && pa.isNotEmpty()) "$pa · ₹$am" else pa
                    } else if (trimmed.contains("pa=")) {
                        trimmed.substringAfter("pa=").substringBefore("&")
                    } else {
                        trimmed.take(40)
                    }
                }
                CodeType.WIFI -> {
                    val ssid = trimmed.substringAfter("S:", "").substringBefore(";")
                    val sec = trimmed.substringAfter("T:", "").substringBefore(";")
                    if (ssid.isNotEmpty()) {
                        if (sec.isNotEmpty()) "$ssid · $sec" else ssid
                    } else "Wi-Fi Network"
                }
                CodeType.PHONE -> {
                    trimmed.removePrefix("tel:").trim()
                }
                CodeType.EMAIL -> {
                    trimmed.removePrefix("mailto:").substringBefore("?").trim()
                }
                CodeType.CONTACT -> {
                    val fn = trimmed.lines().firstOrNull { it.startsWith("FN:", ignoreCase = true) }?.substring(3)?.trim()
                    if (!fn.isNullOrEmpty()) fn else "Contact Card"
                }
                CodeType.SMS -> {
                    val num = trimmed.removePrefix("smsto:").removePrefix("sms:").substringBefore("?").substringBefore(":")
                    val body = if (trimmed.contains("body=")) trimmed.substringAfter("body=").substringBefore("&") else ""
                    if (body.isNotEmpty()) "To: $num · $body" else "To: $num"
                }
                CodeType.URL -> {
                    val clean = trimmed.removePrefix("https://").removePrefix("http://").removePrefix("www.")
                    clean.take(45)
                }
                CodeType.TEXT -> {
                    "\"${trimmed.replace('\n', ' ').take(45)}\""
                }
                else -> trimmed.replace('\n', ' ').take(40)
            }
        }
    }
}

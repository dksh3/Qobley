package com.example.scanner

import android.net.Uri
import com.example.data.model.ParsedPayload
import java.util.Locale

object PayloadParser {

    fun parse(raw: String, format: String = "QR_CODE"): ParsedPayload {
        val trimmed = raw.trim()

        // 1. Standard 1D/2D Barcode Product / Identifier formats
        val barcodeFormats = listOf("EAN_13", "EAN_8", "UPC_A", "UPC_E", "CODE_128", "CODE_39", "CODE_93", "ITF", "CODABAR")
        if (barcodeFormats.any { format.contains(it, ignoreCase = true) }) {
            return ParsedPayload.BarcodeProduct(
                code = trimmed,
                format = format
            )
        }

        // 2. UPI / Payment payloads
        if (trimmed.startsWith("upi://pay", ignoreCase = true)) {
            return parseUpi(trimmed)
        }

        // 3. Wi-Fi configurations
        if (trimmed.startsWith("WIFI:", ignoreCase = true)) {
            return parseWifi(trimmed)
        }

        // 4. Calendar Events (vEvent)
        if (trimmed.startsWith("BEGIN:VEVENT", ignoreCase = true) || trimmed.startsWith("BEGIN:VCALENDAR", ignoreCase = true)) {
            return parseCalendar(trimmed)
        }

        // 5. Contact cards (vCard or MeCard)
        if (trimmed.startsWith("BEGIN:VCARD", ignoreCase = true) || trimmed.startsWith("MECARD:", ignoreCase = true)) {
            return parseContact(trimmed)
        }

        // 6. SMS messages
        if (trimmed.startsWith("smsto:", ignoreCase = true) || trimmed.startsWith("sms:", ignoreCase = true)) {
            return parseSms(trimmed)
        }

        // 7. Phone numbers
        if (trimmed.startsWith("tel:", ignoreCase = true) || isLikelyPhoneNumber(trimmed)) {
            val num = if (trimmed.startsWith("tel:", ignoreCase = true)) trimmed.substring(4) else trimmed
            return ParsedPayload.Phone(
                rawNumber = num,
                formattedNumber = formatPhoneNumber(num)
            )
        }

        // 8. Emails
        if (trimmed.startsWith("mailto:", ignoreCase = true) || isLikelyEmail(trimmed)) {
            return parseEmail(trimmed)
        }

        // 9. Geo coordinates
        if (trimmed.startsWith("geo:", ignoreCase = true)) {
            return parseGeo(trimmed)
        }

        // 10. URLs / Web links
        if (isLikelyUrl(trimmed)) {
            val normalizedUrl = if (!trimmed.startsWith("http://", ignoreCase = true) && !trimmed.startsWith("https://", ignoreCase = true)) {
                "https://$trimmed"
            } else {
                trimmed
            }
            val domain = extractDomain(normalizedUrl)
            return ParsedPayload.Url(
                url = normalizedUrl,
                domain = domain,
                isSecure = normalizedUrl.startsWith("https://", ignoreCase = true)
            )
        }

        // 11. Plain text vs Unknown
        if (trimmed.isNotEmpty()) {
            val words = trimmed.split("\\s+".toRegex()).filter { it.isNotEmpty() }
            return ParsedPayload.Text(
                content = trimmed,
                charCount = trimmed.length,
                wordCount = words.size
            )
        }

        return ParsedPayload.Unknown(raw = raw)
    }

    private fun parseUpi(uriString: String): ParsedPayload {
        return try {
            val uri = Uri.parse(uriString)
            val pa = uri.getQueryParameter("pa") ?: ""
            val pn = uri.getQueryParameter("pn")
            val am = uri.getQueryParameter("am")
            val tn = uri.getQueryParameter("tn")
            val cu = uri.getQueryParameter("cu") ?: "INR"

            ParsedPayload.Payment(
                upiId = pa,
                name = pn,
                amount = am,
                note = tn,
                currency = cu,
                rawUri = uriString
            )
        } catch (_: Exception) {
            ParsedPayload.Unknown(raw = uriString)
        }
    }

    private fun parseWifi(raw: String): ParsedPayload {
        var ssid = ""
        var password = ""
        var security = "WPA"
        var hidden = false

        val tokens = raw.removePrefix("WIFI:").removeSuffix(";;").split(";")
        for (token in tokens) {
            when {
                token.startsWith("S:") -> ssid = unescapeWifi(token.substring(2))
                token.startsWith("P:") -> password = unescapeWifi(token.substring(2))
                token.startsWith("T:") -> security = token.substring(2).uppercase(Locale.US)
                token.startsWith("H:") -> hidden = token.substring(2).equals("true", ignoreCase = true)
            }
        }

        return ParsedPayload.Wifi(
            ssid = ssid.ifEmpty { "Hidden Network" },
            password = password,
            security = if (security.isEmpty()) "WPA" else security,
            isHidden = hidden
        )
    }

    private fun unescapeWifi(value: String): String {
        return value.replace("\\;", ";").replace("\\:", ":").replace("\\\\", "\\")
    }

    private fun parseContact(raw: String): ParsedPayload {
        var name = ""
        var phone: String? = null
        var email: String? = null
        var org: String? = null
        var title: String? = null
        var address: String? = null
        var note: String? = null

        if (raw.startsWith("BEGIN:VCARD", ignoreCase = true)) {
            val lines = raw.lines()
            for (line in lines) {
                val upper = line.uppercase(Locale.US)
                when {
                    upper.startsWith("FN:") -> name = line.substring(3).trim()
                    upper.startsWith("N:") && name.isEmpty() -> {
                        val parts = line.substring(2).split(";").filter { it.isNotBlank() }
                        name = parts.reversed().joinToString(" ").trim()
                    }
                    upper.startsWith("TEL") -> {
                        val idx = line.indexOf(':')
                        if (idx != -1 && phone == null) phone = line.substring(idx + 1).trim()
                    }
                    upper.startsWith("EMAIL") -> {
                        val idx = line.indexOf(':')
                        if (idx != -1 && email == null) email = line.substring(idx + 1).trim()
                    }
                    upper.startsWith("ORG:") -> org = line.substring(4).trim()
                    upper.startsWith("TITLE:") -> title = line.substring(6).trim()
                    upper.startsWith("ADR") -> {
                        val idx = line.indexOf(':')
                        if (idx != -1) address = line.substring(idx + 1).replace(";", " ").trim()
                    }
                    upper.startsWith("NOTE:") -> note = line.substring(5).trim()
                }
            }
        } else if (raw.startsWith("MECARD:", ignoreCase = true)) {
            val fields = raw.removePrefix("MECARD:").removeSuffix(";;").split(";")
            for (field in fields) {
                when {
                    field.startsWith("N:") -> name = field.substring(2).replace(",", " ").trim()
                    field.startsWith("TEL:") -> if (phone == null) phone = field.substring(4).trim()
                    field.startsWith("EMAIL:") -> if (email == null) email = field.substring(6).trim()
                    field.startsWith("ORG:") -> org = field.substring(4).trim()
                    field.startsWith("ADR:") -> address = field.substring(4).trim()
                    field.startsWith("NOTE:") -> note = field.substring(5).trim()
                }
            }
        }

        return ParsedPayload.Contact(
            name = name.ifEmpty { "Unknown Contact" },
            phone = phone,
            email = email,
            org = org,
            title = title,
            address = address,
            note = note
        )
    }

    private fun parseSms(raw: String): ParsedPayload {
        var number = ""
        var message: String? = null

        if (raw.startsWith("smsto:", ignoreCase = true)) {
            val content = raw.substring(6)
            val parts = content.split(":", limit = 2)
            number = parts[0]
            if (parts.size > 1) message = parts[1]
        } else if (raw.startsWith("sms:", ignoreCase = true)) {
            val content = raw.substring(4)
            val parts = content.split("?", limit = 2)
            number = parts[0]
            if (parts.size > 1) {
                val queryParams = parts[1].split("&")
                for (param in queryParams) {
                    val kv = param.split("=", limit = 2)
                    if (kv.getOrNull(0)?.equals("body", ignoreCase = true) == true) {
                        message = kv.getOrNull(1)?.let { Uri.decode(it) }
                    }
                }
            }
        }

        return ParsedPayload.Sms(
            number = number,
            message = message
        )
    }

    private fun parseEmail(raw: String): ParsedPayload {
        var email = ""
        var subject: String? = null
        var body: String? = null

        if (raw.startsWith("mailto:", ignoreCase = true)) {
            val content = raw.substring(7)
            val parts = content.split("?", limit = 2)
            email = parts[0]
            if (parts.size > 1) {
                val queryParams = parts[1].split("&")
                for (param in queryParams) {
                    val kv = param.split("=", limit = 2)
                    val key = kv.getOrNull(0)?.lowercase(Locale.US)
                    val value = kv.getOrNull(1)?.let { Uri.decode(it) }
                    when (key) {
                        "subject" -> subject = value
                        "body" -> body = value
                    }
                }
            }
        } else {
            email = raw
        }

        return ParsedPayload.Email(
            email = email,
            subject = subject,
            body = body
        )
    }

    private fun parseGeo(raw: String): ParsedPayload {
        val geoPart = raw.removePrefix("geo:")
        val coords = geoPart.substringBefore("?").split(",")
        val lat = coords.getOrNull(0)?.toDoubleOrNull() ?: 0.0
        val lng = coords.getOrNull(1)?.toDoubleOrNull() ?: 0.0
        val query = if (geoPart.contains("?q=")) {
            Uri.decode(geoPart.substringAfter("?q=").replace("+", " "))
        } else {
            null
        }

        return ParsedPayload.Geo(
            latitude = lat,
            longitude = lng,
            query = query
        )
    }

    private fun isLikelyPhoneNumber(str: String): Boolean {
        val cleaned = str.replace("[\\s\\-\\(\\)\\.]".toRegex(), "")
        return cleaned.matches("^(\\+)?[0-9]{5,15}$".toRegex())
    }

    private fun formatPhoneNumber(num: String): String {
        return num.trim()
    }

    private fun isLikelyEmail(str: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(str).matches()
    }

    private fun isLikelyUrl(str: String): Boolean {
        if (str.startsWith("http://", ignoreCase = true) || str.startsWith("https://", ignoreCase = true)) {
            return true
        }
        val lower = str.lowercase(Locale.US)
        if (lower.startsWith("www.") || (lower.contains(".") && !lower.contains(" ") && (
            lower.endsWith(".com") || lower.endsWith(".org") || lower.endsWith(".net") ||
            lower.endsWith(".io") || lower.endsWith(".app") || lower.endsWith(".edu") ||
            lower.endsWith(".gov") || lower.endsWith(".co") || lower.endsWith(".in") ||
            lower.endsWith(".ai") || lower.endsWith(".me") || lower.endsWith(".dev")
        ))) {
            return true
        }
        return false
    }

    private fun parseCalendar(raw: String): ParsedPayload {
        var title = "Calendar Event"
        var description: String? = null
        var location: String? = null

        val lines = raw.lines()
        for (line in lines) {
            val upper = line.uppercase(Locale.US)
            when {
                upper.startsWith("SUMMARY:") -> title = line.substring(8).trim()
                upper.startsWith("DESCRIPTION:") -> description = line.substring(12).trim()
                upper.startsWith("LOCATION:") -> location = line.substring(9).trim()
            }
        }

        return ParsedPayload.Calendar(
            title = title,
            description = description,
            location = location
        )
    }

    private fun extractDomain(url: String): String {
        return try {
            val uri = Uri.parse(url)
            uri.host ?: url
        } catch (_: Exception) {
            url
        }
    }
}

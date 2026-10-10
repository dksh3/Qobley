package com.example.generator

object BarcodeValidator {

    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class ValidWithAutofill(val correctedValue: String, val note: String) : ValidationResult()
        data class Invalid(val reason: String) : ValidationResult()
    }

    fun validate(format: String, input: String): ValidationResult {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult.Invalid("Please enter content to generate")
        }

        return when (format.uppercase()) {
            "QR_CODE" -> ValidationResult.Valid
            "DATA_MATRIX" -> ValidationResult.Valid
            "AZTEC" -> ValidationResult.Valid
            "PDF_417" -> ValidationResult.Valid

            "EAN_13" -> validateEan13(trimmed)
            "EAN_8" -> validateEan8(trimmed)
            "UPC_A" -> validateUpcA(trimmed)
            "UPC_E" -> validateUpcE(trimmed)
            "CODE_128" -> validateCode128(trimmed)
            "CODE_39" -> validateCode39(trimmed)
            "CODE_93" -> validateCode93(trimmed)
            "ITF" -> validateItf(trimmed)
            "CODABAR" -> validateCodabar(trimmed)

            else -> ValidationResult.Valid
        }
    }

    private fun validateEan13(value: String): ValidationResult {
        if (!value.all { it.isDigit() }) {
            return ValidationResult.Invalid("EAN-13 allows only digits (0-9)")
        }
        if (value.length == 12) {
            val checksum = calculateEan13Checksum(value)
            return ValidationResult.ValidWithAutofill(
                correctedValue = "$value$checksum",
                note = "Calculated checksum digit: $checksum"
            )
        }
        if (value.length == 13) {
            val expectedChecksum = calculateEan13Checksum(value.substring(0, 12))
            val actualChecksum = value[12].digitToInt()
            return if (expectedChecksum == actualChecksum) {
                ValidationResult.Valid
            } else {
                ValidationResult.Invalid("Invalid checksum digit: expected $expectedChecksum, found $actualChecksum")
            }
        }
        return ValidationResult.Invalid("EAN-13 requires exactly 12 or 13 digits (currently ${value.length})")
    }

    fun calculateEan13Checksum(twelveDigits: String): Int {
        var sum = 0
        for (i in 0 until 12) {
            val digit = twelveDigits[i].digitToInt()
            sum += if (i % 2 == 0) digit else digit * 3
        }
        val remainder = sum % 10
        return if (remainder == 0) 0 else 10 - remainder
    }

    private fun validateEan8(value: String): ValidationResult {
        if (!value.all { it.isDigit() }) {
            return ValidationResult.Invalid("EAN-8 allows only digits (0-9)")
        }
        if (value.length == 7) {
            val checksum = calculateEan8Checksum(value)
            return ValidationResult.ValidWithAutofill(
                correctedValue = "$value$checksum",
                note = "Calculated checksum digit: $checksum"
            )
        }
        if (value.length == 8) {
            val expected = calculateEan8Checksum(value.substring(0, 7))
            val actual = value[7].digitToInt()
            return if (expected == actual) ValidationResult.Valid
            else ValidationResult.Invalid("Invalid checksum: expected $expected, found $actual")
        }
        return ValidationResult.Invalid("EAN-8 requires 7 or 8 digits (currently ${value.length})")
    }

    fun calculateEan8Checksum(sevenDigits: String): Int {
        var sum = 0
        for (i in 0 until 7) {
            val digit = sevenDigits[i].digitToInt()
            sum += if (i % 2 == 0) digit * 3 else digit
        }
        val remainder = sum % 10
        return if (remainder == 0) 0 else 10 - remainder
    }

    private fun validateUpcA(value: String): ValidationResult {
        if (!value.all { it.isDigit() }) {
            return ValidationResult.Invalid("UPC-A allows only digits (0-9)")
        }
        if (value.length == 11) {
            val checksum = calculateUpcAChecksum(value)
            return ValidationResult.ValidWithAutofill(
                correctedValue = "$value$checksum",
                note = "Calculated checksum digit: $checksum"
            )
        }
        if (value.length == 12) {
            val expected = calculateUpcAChecksum(value.substring(0, 11))
            val actual = value[11].digitToInt()
            return if (expected == actual) ValidationResult.Valid
            else ValidationResult.Invalid("Invalid checksum: expected $expected, found $actual")
        }
        return ValidationResult.Invalid("UPC-A requires 11 or 12 digits (currently ${value.length})")
    }

    fun calculateUpcAChecksum(elevenDigits: String): Int {
        var sum = 0
        for (i in 0 until 11) {
            val digit = elevenDigits[i].digitToInt()
            sum += if (i % 2 == 0) digit * 3 else digit
        }
        val remainder = sum % 10
        return if (remainder == 0) 0 else 10 - remainder
    }

    private fun validateUpcE(value: String): ValidationResult {
        if (!value.all { it.isDigit() }) {
            return ValidationResult.Invalid("UPC-E allows only digits (0-9)")
        }
        if (value.length in 6..8) {
            return ValidationResult.Valid
        }
        return ValidationResult.Invalid("UPC-E requires 6, 7 or 8 digits (currently ${value.length})")
    }

    private fun validateCode128(value: String): ValidationResult {
        if (value.any { it.code > 127 }) {
            return ValidationResult.Invalid("Code 128 supports standard ASCII characters (0-127)")
        }
        return ValidationResult.Valid
    }

    private fun validateCode39(value: String): ValidationResult {
        val validChars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. $/+%"
        val upper = value.uppercase()
        if (upper.any { it !in validChars }) {
            return ValidationResult.Invalid("Code 39 only allows uppercase letters, digits, and - . $ / + % space")
        }
        return ValidationResult.Valid
    }

    private fun validateCode93(value: String): ValidationResult {
        val validChars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. $/+%"
        val upper = value.uppercase()
        if (upper.any { it !in validChars }) {
            return ValidationResult.Invalid("Code 93 allows uppercase letters, digits, and - . $ / + % space")
        }
        return ValidationResult.Valid
    }

    private fun validateItf(value: String): ValidationResult {
        if (!value.all { it.isDigit() }) {
            return ValidationResult.Invalid("ITF allows only digits (0-9)")
        }
        if (value.length % 2 != 0) {
            return ValidationResult.Invalid("ITF requires an even number of digits (currently ${value.length})")
        }
        return ValidationResult.Valid
    }

    private fun validateCodabar(value: String): ValidationResult {
        val upper = value.uppercase()
        val validBody = "0123456789-$:/.+"
        val startStop = "ABCD"

        if (upper.length < 2) {
            return ValidationResult.Invalid("Codabar requires at least 2 characters including start/stop delimiters")
        }
        if (upper.first() !in startStop || upper.last() !in startStop) {
            return ValidationResult.Invalid("Codabar must start and end with A, B, C, or D (e.g. A1234B)")
        }
        val body = upper.substring(1, upper.length - 1)
        if (body.any { it !in validBody }) {
            return ValidationResult.Invalid("Codabar body allows digits and - $ : / . +")
        }
        return ValidationResult.Valid
    }
}

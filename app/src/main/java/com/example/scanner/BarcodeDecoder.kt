package com.example.scanner

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.util.EnumMap

data class DetectedItem(
    val content: String,
    val format: String,
    val type: String = "CODE" // CODE or EXTRACTED_TEXT
)

object BarcodeDecoder {

    private val allFormats = listOf(
        BarcodeFormat.QR_CODE,
        BarcodeFormat.DATA_MATRIX,
        BarcodeFormat.AZTEC,
        BarcodeFormat.PDF_417,
        BarcodeFormat.CODE_128,
        BarcodeFormat.CODE_39,
        BarcodeFormat.CODE_93,
        BarcodeFormat.EAN_13,
        BarcodeFormat.EAN_8,
        BarcodeFormat.UPC_A,
        BarcodeFormat.UPC_E,
        BarcodeFormat.CODABAR,
        BarcodeFormat.ITF
    )

    fun decodeBitmap(bitmap: Bitmap): List<DetectedItem> {
        val detected = mutableListOf<DetectedItem>()
        val seenContents = mutableSetOf<String>()

        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val hints: MutableMap<DecodeHintType, Any> = EnumMap(DecodeHintType::class.java)
        hints[DecodeHintType.POSSIBLE_FORMATS] = allFormats
        hints[DecodeHintType.TRY_HARDER] = true
        hints[DecodeHintType.CHARACTER_SET] = "UTF-8"

        val reader = MultiFormatReader()
        reader.setHints(hints)

        // 1. Try decoding full bitmap
        try {
            val source = RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val result = reader.decodeWithState(binaryBitmap)
            if (result != null && seenContents.add(result.text)) {
                detected.add(DetectedItem(result.text, result.barcodeFormat.name))
            }
        } catch (_: Exception) {}

        // 2. If image is large, also try quadrant crops (top-half, bottom-half, left, right)
        // to detect multiple barcodes/QRs on receipts, invoices or packaging
        if (width > 300 && height > 300) {
            val crops = listOf(
                // Top half
                Pair(0, 0) to Pair(width, height / 2),
                // Bottom half
                Pair(0, height / 2) to Pair(width, height / 2),
                // Left half
                Pair(0, 0) to Pair(width / 2, height),
                // Right half
                Pair(width / 2, 0) to Pair(width / 2, height)
            )

            for ((origin, size) in crops) {
                try {
                    val cropSource = RGBLuminanceSource(width, height, pixels)
                        .crop(origin.first, origin.second, size.first, size.second)
                    val cropBinary = BinaryBitmap(HybridBinarizer(cropSource))
                    reader.reset()
                    val cropResult = reader.decodeWithState(cropBinary)
                    if (cropResult != null && seenContents.add(cropResult.text)) {
                        detected.add(DetectedItem(cropResult.text, cropResult.barcodeFormat.name))
                    }
                } catch (_: Exception) {}
            }
        }

        return detected
    }
}

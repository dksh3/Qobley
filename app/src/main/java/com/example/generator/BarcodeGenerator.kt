package com.example.generator

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.EnumMap

object BarcodeGenerator {

    enum class EmblemType(val label: String) {
        NONE("None"),
        GLOBE("Globe (Web)"),
        WIFI("Wi-Fi"),
        PHONE("Phone"),
        EMAIL("Email"),
        LOCATION("Location"),
        USER("Contact"),
        PAYMENT("Payment ($)"),
        SHIELD("Shield / 2FA"),
        STAR("Star"),
        MONOGRAM("Monogram")
    }

    data class QrConfig(
        val foregroundColor: Int = Color.BLACK,
        val backgroundColor: Int = Color.WHITE,
        val errorCorrection: String = "M", // L, M, Q, H
        val margin: Int = 1,
        val emblemType: EmblemType = EmblemType.NONE,
        val monogramText: String = "Q",
        val includeLabel: Boolean = false // for 1D barcodes
    )

    fun generate(
        formatString: String,
        content: String,
        width: Int = 800,
        height: Int = 800,
        qrConfig: QrConfig = QrConfig()
    ): Bitmap? {
        if (content.isEmpty()) return null

        val format = mapToBarcodeFormat(formatString) ?: return null
        val isQr = format == BarcodeFormat.QR_CODE

        val hints: MutableMap<EncodeHintType, Any> = EnumMap(EncodeHintType::class.java)
        hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
        hints[EncodeHintType.MARGIN] = if (isQr) qrConfig.margin else 2

        if (isQr) {
            // If emblem is active, automatically enforce at least Q or H to ensure 100% scannability
            val ecLevel = if (qrConfig.emblemType != EmblemType.NONE) {
                if (qrConfig.errorCorrection.uppercase() == "H") ErrorCorrectionLevel.H else ErrorCorrectionLevel.Q
            } else {
                when (qrConfig.errorCorrection.uppercase()) {
                    "L" -> ErrorCorrectionLevel.L
                    "Q" -> ErrorCorrectionLevel.Q
                    "H" -> ErrorCorrectionLevel.H
                    else -> ErrorCorrectionLevel.M
                }
            }
            hints[EncodeHintType.ERROR_CORRECTION] = ecLevel
        }

        return try {
            val is1D = is1DBarcode(format)
            val baseHeight = if (is1D) {
                if (height == width) (width * 0.4).toInt().coerceAtLeast(200) else height
            } else {
                height
            }

            val writer = MultiFormatWriter()
            val bitMatrix = writer.encode(content, format, width, baseHeight, hints)
            val matrixWidth = bitMatrix.width
            val matrixHeight = bitMatrix.height
            val pixels = IntArray(matrixWidth * matrixHeight)

            val fg = if (isQr) qrConfig.foregroundColor else Color.BLACK
            val bg = if (isQr) qrConfig.backgroundColor else Color.WHITE

            for (y in 0 until matrixHeight) {
                val offset = y * matrixWidth
                for (x in 0 until matrixWidth) {
                    pixels[offset + x] = if (bitMatrix[x, y]) fg else bg
                }
            }

            val baseBitmap = Bitmap.createBitmap(matrixWidth, matrixHeight, Bitmap.Config.ARGB_8888)
            baseBitmap.setPixels(pixels, 0, matrixWidth, 0, 0, matrixWidth, matrixHeight)

            // 1. If QR with center emblem, draw protective badge in center
            if (isQr && qrConfig.emblemType != EmblemType.NONE) {
                val canvas = Canvas(baseBitmap)
                val emblemSize = (matrixWidth * 0.20f).toInt()
                val cx = matrixWidth / 2f
                val cy = matrixHeight / 2f
                val half = emblemSize / 2f

                // Protective quiet boundary in background color
                val bgPaint = Paint().apply {
                    color = qrConfig.backgroundColor
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                val borderPaint = Paint().apply {
                    color = qrConfig.foregroundColor
                    style = Paint.Style.STROKE
                    strokeWidth = (matrixWidth * 0.006f).coerceAtLeast(2f)
                    isAntiAlias = true
                }
                val cornerRadius = emblemSize * 0.26f
                val rect = RectF(cx - half, cy - half, cx + half, cy + half)
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint)
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)

                val fgPaint = Paint().apply {
                    color = qrConfig.foregroundColor
                    isAntiAlias = true
                    textAlign = Paint.Align.CENTER
                }

                if (qrConfig.emblemType == EmblemType.MONOGRAM) {
                    fgPaint.textSize = emblemSize * 0.52f
                    fgPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    val text = qrConfig.monogramText.trim().take(3).uppercase().ifEmpty { "Q" }
                    val textBounds = Rect()
                    fgPaint.getTextBounds(text, 0, text.length, textBounds)
                    val textY = cy - textBounds.exactCenterY()
                    canvas.drawText(text, cx, textY, fgPaint)
                } else {
                    val glyph = when (qrConfig.emblemType) {
                        EmblemType.GLOBE -> "🌐"
                        EmblemType.WIFI -> "📶"
                        EmblemType.PHONE -> "📞"
                        EmblemType.EMAIL -> "✉"
                        EmblemType.LOCATION -> "📍"
                        EmblemType.USER -> "👤"
                        EmblemType.PAYMENT -> "$"
                        EmblemType.SHIELD -> "🛡"
                        EmblemType.STAR -> "★"
                        else -> "●"
                    }
                    fgPaint.textSize = emblemSize * 0.50f
                    fgPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    val glyphBounds = Rect()
                    fgPaint.getTextBounds(glyph, 0, glyph.length, glyphBounds)
                    val glyphY = cy - glyphBounds.exactCenterY()
                    canvas.drawText(glyph, cx, glyphY, fgPaint)
                }
                return baseBitmap
            }

            // 2. If 1D barcode and includeLabel requested, append human-readable label at bottom
            if (is1D && qrConfig.includeLabel) {
                val labelHeight = (matrixHeight * 0.20f).toInt().coerceIn(36, 64)
                val totalHeight = matrixHeight + labelHeight
                val labeledBitmap = Bitmap.createBitmap(matrixWidth, totalHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(labeledBitmap)
                canvas.drawColor(bg)
                canvas.drawBitmap(baseBitmap, 0f, 0f, null)

                val textPaint = Paint().apply {
                    color = fg
                    textSize = labelHeight * 0.55f
                    typeface = Typeface.MONOSPACE
                    isAntiAlias = true
                    textAlign = Paint.Align.CENTER
                }
                val textBounds = Rect()
                textPaint.getTextBounds(content, 0, content.length, textBounds)
                val textY = matrixHeight + (labelHeight / 2f) - textBounds.exactCenterY()
                canvas.drawText(content, matrixWidth / 2f, textY, textPaint)
                return labeledBitmap
            }

            baseBitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun is1DBarcode(format: BarcodeFormat): Boolean {
        return when (format) {
            BarcodeFormat.CODE_128,
            BarcodeFormat.CODE_39,
            BarcodeFormat.CODE_93,
            BarcodeFormat.EAN_13,
            BarcodeFormat.EAN_8,
            BarcodeFormat.UPC_A,
            BarcodeFormat.UPC_E,
            BarcodeFormat.CODABAR,
            BarcodeFormat.ITF -> true
            else -> false
        }
    }

    fun mapToBarcodeFormat(formatString: String): BarcodeFormat? {
        return when (formatString.uppercase()) {
            "QR", "QR_CODE" -> BarcodeFormat.QR_CODE
            "CODE_128" -> BarcodeFormat.CODE_128
            "CODE_39" -> BarcodeFormat.CODE_39
            "CODE_93" -> BarcodeFormat.CODE_93
            "EAN_13" -> BarcodeFormat.EAN_13
            "EAN_8" -> BarcodeFormat.EAN_8
            "UPC_A" -> BarcodeFormat.UPC_A
            "UPC_E" -> BarcodeFormat.UPC_E
            "CODABAR" -> BarcodeFormat.CODABAR
            "ITF" -> BarcodeFormat.ITF
            "DATA_MATRIX" -> BarcodeFormat.DATA_MATRIX
            "AZTEC" -> BarcodeFormat.AZTEC
            "PDF_417", "PDF417" -> BarcodeFormat.PDF_417
            else -> null
        }
    }
}

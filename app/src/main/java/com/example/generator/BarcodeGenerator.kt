package com.example.generator

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.EnumMap

object BarcodeGenerator {

    enum class DotStyle(val label: String) {
        SQUARE("Classic Square"),
        ROUNDED("Rounded Squircle"),
        DOTS("Circular Dots")
    }

    enum class EyeFrameStyle(val label: String) {
        SQUARE("Square"),
        ROUNDED("Rounded"),
        CIRCLE("Circular Ring")
    }

    enum class EyePupilStyle(val label: String) {
        SQUARE("Square"),
        CIRCLE("Circle"),
        DIAMOND("Diamond")
    }

    enum class EmblemType(val label: String, val glyph: String) {
        NONE("None", ""),
        GLOBE("Web Link", "🌐"),
        WIFI("Wi-Fi", "📶"),
        PHONE("Phone Call", "📞"),
        EMAIL("Email", "✉"),
        LOCATION("Location", "📍"),
        USER("Contact Card", "👤"),
        PAYMENT("Payment", "$"),
        SHIELD("Security / 2FA", "🛡"),
        STAR("Favorite Star", "★"),
        HEART("Heart Love", "♥"),
        CODE("Developer Code", "</>"),
        CART("Retail Store", "🛒"),
        COFFEE("Coffee Cafe", "☕"),
        MONOGRAM("Custom Text / Monogram", "")
    }

    enum class MonogramShape(val label: String) {
        CIRCLE("Circle"),
        ROUNDED_SQUARE("Rounded Square"),
        SHIELD("Shield Badge")
    }

    enum class BannerPosition(val label: String) {
        BOTTOM("Bottom Banner"),
        TOP("Top Banner")
    }

    enum class BarcodeLabelPosition(val label: String) {
        BOTTOM("Below Barcode"),
        TOP("Above Barcode"),
        NONE("Hidden / No Text")
    }

    data class QrConfig(
        val foregroundColor: Int = Color.BLACK,
        val backgroundColor: Int = Color.WHITE,
        val errorCorrection: String = "M", // L, M, Q, H
        val margin: Int = 1,
        val emblemType: EmblemType = EmblemType.NONE,
        val monogramText: String = "Q",
        val monogramShape: MonogramShape = MonogramShape.CIRCLE,
        val includeLabel: Boolean = false, // for 1D barcodes backward compatibility
        val dotStyle: DotStyle = DotStyle.SQUARE,
        val eyeFrameStyle: EyeFrameStyle = EyeFrameStyle.SQUARE,
        val eyePupilStyle: EyePupilStyle = EyePupilStyle.SQUARE,
        val eyeColor: Int? = null,
        val gradientSecondaryColor: Int? = null,
        val isTransparentBackground: Boolean = false,
        val bannerText: String? = null,
        val bannerPosition: BannerPosition = BannerPosition.BOTTOM,
        val barcodeHeightMultiplier: Float = 1.0f,
        val customBarcodeLabel: String? = null,
        val barcodeLabelPosition: BarcodeLabelPosition = BarcodeLabelPosition.BOTTOM
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
            // If custom style, emblem, or rounded dots are active, automatically enforce high error correction
            val needsHighEc = qrConfig.emblemType != EmblemType.NONE ||
                    qrConfig.dotStyle != DotStyle.SQUARE ||
                    qrConfig.eyeFrameStyle != EyeFrameStyle.SQUARE ||
                    qrConfig.eyePupilStyle != EyePupilStyle.SQUARE

            val ecLevel = if (needsHighEc) {
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
                val scaled = (width * 0.38f * qrConfig.barcodeHeightMultiplier).toInt().coerceIn(140, 600)
                if (height == width) scaled else (height * qrConfig.barcodeHeightMultiplier).toInt().coerceIn(140, 600)
            } else {
                height
            }

            val writer = MultiFormatWriter()
            val bitMatrix = writer.encode(content, format, width, baseHeight, hints)
            val matrixWidth = bitMatrix.width
            val matrixHeight = bitMatrix.height

            val fg = qrConfig.foregroundColor
            val bg = if (qrConfig.isTransparentBackground) Color.TRANSPARENT else qrConfig.backgroundColor

            val baseBitmap = Bitmap.createBitmap(matrixWidth, matrixHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(baseBitmap)

            if (!qrConfig.isTransparentBackground) {
                canvas.drawColor(bg)
            }

            if (isQr) {
                renderCustomQr(canvas, bitMatrix, matrixWidth, matrixHeight, qrConfig)
            } else {
                renderBarcode(canvas, bitMatrix, matrixWidth, matrixHeight, fg)
            }

            // Apply 1D barcode label if applicable
            var currentBitmap = baseBitmap
            if (is1D && (qrConfig.includeLabel || qrConfig.barcodeLabelPosition != BarcodeLabelPosition.NONE)) {
                val labelPos = if (qrConfig.includeLabel && qrConfig.barcodeLabelPosition == BarcodeLabelPosition.NONE) {
                    BarcodeLabelPosition.BOTTOM
                } else {
                    qrConfig.barcodeLabelPosition
                }
                if (labelPos != BarcodeLabelPosition.NONE) {
                    currentBitmap = attachBarcodeLabel(currentBitmap, content, qrConfig, labelPos)
                }
            }

            // Apply Designer Frame / "Scan Me" Banner if specified
            if (isQr && !qrConfig.bannerText.isNullOrBlank()) {
                currentBitmap = attachQrBanner(currentBitmap, qrConfig.bannerText.trim(), qrConfig)
            }

            currentBitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun renderBarcode(canvas: Canvas, bitMatrix: com.google.zxing.common.BitMatrix, width: Int, height: Int, fg: Int) {
        val paint = Paint().apply {
            color = fg
            style = Paint.Style.FILL
            isAntiAlias = false
        }
        for (x in 0 until width) {
            if (bitMatrix[x, 0]) {
                canvas.drawRect(x.toFloat(), 0f, (x + 1).toFloat(), height.toFloat(), paint)
            }
        }
    }

    private fun renderCustomQr(
        canvas: Canvas,
        bitMatrix: com.google.zxing.common.BitMatrix,
        width: Int,
        height: Int,
        qrConfig: QrConfig
    ) {
        val cellW = width.toFloat() / bitMatrix.width
        val cellH = height.toFloat() / bitMatrix.height

        val modulePaint = Paint().apply {
            color = qrConfig.foregroundColor
            style = Paint.Style.FILL
            isAntiAlias = qrConfig.dotStyle != DotStyle.SQUARE
        }

        if (qrConfig.gradientSecondaryColor != null && qrConfig.gradientSecondaryColor != qrConfig.foregroundColor) {
            modulePaint.shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                qrConfig.foregroundColor, qrConfig.gradientSecondaryColor,
                Shader.TileMode.CLAMP
            )
        }

        val matrixDim = bitMatrix.width

        // Check if a coordinate falls within one of the 3 finder patterns (7x7 modules)
        fun isEyeRegion(x: Int, y: Int): Boolean {
            val inTopLeft = x < 7 && y < 7
            val inTopRight = x >= matrixDim - 7 && y < 7
            val inBottomLeft = x < 7 && y >= matrixDim - 7
            return inTopLeft || inTopRight || inBottomLeft
        }

        // 1. Draw Data Modules (skipping the 7x7 corner eye zones if custom eyes are enabled)
        val customEyes = qrConfig.eyeFrameStyle != EyeFrameStyle.SQUARE ||
                qrConfig.eyePupilStyle != EyePupilStyle.SQUARE ||
                qrConfig.eyeColor != null

        for (y in 0 until matrixDim) {
            for (x in 0 until matrixDim) {
                if (customEyes && isEyeRegion(x, y)) {
                    continue
                }
                if (bitMatrix[x, y]) {
                    val l = x * cellW
                    val t = y * cellH
                    val r = l + cellW
                    val b = t + cellH
                    when (qrConfig.dotStyle) {
                        DotStyle.SQUARE -> {
                            canvas.drawRect(l, t, r + 0.15f, b + 0.15f, modulePaint)
                        }
                        DotStyle.ROUNDED -> {
                            val rect = RectF(l + cellW * 0.05f, t + cellH * 0.05f, r - cellW * 0.05f, b - cellH * 0.05f)
                            canvas.drawRoundRect(rect, cellW * 0.35f, cellH * 0.35f, modulePaint)
                        }
                        DotStyle.DOTS -> {
                            val cx = l + cellW / 2f
                            val cy = t + cellH / 2f
                            canvas.drawCircle(cx, cy, cellW * 0.44f, modulePaint)
                        }
                    }
                }
            }
        }

        // 2. Draw Eye Patterns
        val eyeColor = qrConfig.eyeColor ?: qrConfig.foregroundColor
        val eyePaint = Paint().apply {
            color = eyeColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val eyeBgPaint = Paint().apply {
            color = if (qrConfig.isTransparentBackground) Color.TRANSPARENT else qrConfig.backgroundColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        if (customEyes) {
            drawEye(canvas, 0f, 0f, cellW, cellH, eyePaint, eyeBgPaint, qrConfig)
            drawEye(canvas, (matrixDim - 7) * cellW, 0f, cellW, cellH, eyePaint, eyeBgPaint, qrConfig)
            drawEye(canvas, 0f, (matrixDim - 7) * cellH, cellW, cellH, eyePaint, eyeBgPaint, qrConfig)
        } else {
            // Classic eyes rendered along with bitmatrix if custom eyes wasn't flagged
            for (y in 0 until matrixDim) {
                for (x in 0 until matrixDim) {
                    if (isEyeRegion(x, y) && bitMatrix[x, y]) {
                        val l = x * cellW
                        val t = y * cellH
                        canvas.drawRect(l, t, l + cellW + 0.15f, t + cellH + 0.15f, modulePaint)
                    }
                }
            }
        }

        // 3. Center Emblem / Badge
        if (qrConfig.emblemType != EmblemType.NONE) {
            drawCenterEmblem(canvas, width, height, qrConfig)
        }
    }

    private fun drawEye(
        canvas: Canvas,
        originX: Float,
        originY: Float,
        cellW: Float,
        cellH: Float,
        eyePaint: Paint,
        eyeBgPaint: Paint,
        config: QrConfig
    ) {
        val totalSize = 7 * cellW
        val outerRect = RectF(originX, originY, originX + totalSize, originY + totalSize)
        val middleRect = RectF(originX + cellW, originY + cellH, originX + totalSize - cellW, originY + totalSize - cellH)
        val pupilRect = RectF(originX + cellW * 2f, originY + cellH * 2f, originX + totalSize - cellW * 2f, originY + totalSize - cellH * 2f)

        // Outer 7x7
        when (config.eyeFrameStyle) {
            EyeFrameStyle.SQUARE -> canvas.drawRect(outerRect, eyePaint)
            EyeFrameStyle.ROUNDED -> canvas.drawRoundRect(outerRect, cellW * 2f, cellH * 2f, eyePaint)
            EyeFrameStyle.CIRCLE -> canvas.drawCircle(outerRect.centerX(), outerRect.centerY(), totalSize / 2f, eyePaint)
        }

        // Middle 5x5 cutout
        when (config.eyeFrameStyle) {
            EyeFrameStyle.SQUARE -> canvas.drawRect(middleRect, eyeBgPaint)
            EyeFrameStyle.ROUNDED -> canvas.drawRoundRect(middleRect, cellW * 1.5f, cellH * 1.5f, eyeBgPaint)
            EyeFrameStyle.CIRCLE -> canvas.drawCircle(middleRect.centerX(), middleRect.centerY(), (5 * cellW) / 2f, eyeBgPaint)
        }

        // Inner 3x3 Pupil
        when (config.eyePupilStyle) {
            EyePupilStyle.SQUARE -> canvas.drawRect(pupilRect, eyePaint)
            EyePupilStyle.CIRCLE -> canvas.drawCircle(pupilRect.centerX(), pupilRect.centerY(), (3 * cellW) / 2f, eyePaint)
            EyePupilStyle.DIAMOND -> {
                val path = Path().apply {
                    moveTo(pupilRect.centerX(), pupilRect.top)
                    lineTo(pupilRect.right, pupilRect.centerY())
                    lineTo(pupilRect.centerX(), pupilRect.bottom)
                    lineTo(pupilRect.left, pupilRect.centerY())
                    close()
                }
                canvas.drawPath(path, eyePaint)
            }
        }
    }

    private fun drawCenterEmblem(canvas: Canvas, width: Int, height: Int, qrConfig: QrConfig) {
        val emblemSize = (width * 0.20f).toInt()
        val cx = width / 2f
        val cy = height / 2f
        val half = emblemSize / 2f

        val bgPaint = Paint().apply {
            color = if (qrConfig.isTransparentBackground) Color.WHITE else qrConfig.backgroundColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val borderPaint = Paint().apply {
            color = qrConfig.foregroundColor
            style = Paint.Style.STROKE
            strokeWidth = (width * 0.006f).coerceAtLeast(2f)
            isAntiAlias = true
        }

        val rect = RectF(cx - half, cy - half, cx + half, cy + half)

        when (qrConfig.monogramShape) {
            MonogramShape.CIRCLE -> {
                canvas.drawCircle(cx, cy, half, bgPaint)
                canvas.drawCircle(cx, cy, half, borderPaint)
            }
            MonogramShape.ROUNDED_SQUARE -> {
                val cr = emblemSize * 0.28f
                canvas.drawRoundRect(rect, cr, cr, bgPaint)
                canvas.drawRoundRect(rect, cr, cr, borderPaint)
            }
            MonogramShape.SHIELD -> {
                val path = Path().apply {
                    moveTo(cx, cy - half)
                    lineTo(cx + half, cy - half * 0.7f)
                    lineTo(cx + half * 0.8f, cy + half * 0.5f)
                    lineTo(cx, cy + half)
                    lineTo(cx - half * 0.8f, cy + half * 0.5f)
                    lineTo(cx - half, cy - half * 0.7f)
                    close()
                }
                canvas.drawPath(path, bgPaint)
                canvas.drawPath(path, borderPaint)
            }
        }

        val fgPaint = Paint().apply {
            color = qrConfig.foregroundColor
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        if (qrConfig.emblemType == EmblemType.MONOGRAM) {
            fgPaint.textSize = emblemSize * 0.52f
            fgPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val text = qrConfig.monogramText.trim().take(3).uppercase().ifEmpty { "Q" }
            val bounds = Rect()
            fgPaint.getTextBounds(text, 0, text.length, bounds)
            val textY = cy - bounds.exactCenterY()
            canvas.drawText(text, cx, textY, fgPaint)
        } else {
            val glyph = qrConfig.emblemType.glyph.ifEmpty { "●" }
            fgPaint.textSize = emblemSize * 0.48f
            fgPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val bounds = Rect()
            fgPaint.getTextBounds(glyph, 0, glyph.length, bounds)
            val textY = cy - bounds.exactCenterY()
            canvas.drawText(glyph, cx, textY, fgPaint)
        }
    }

    private fun attachBarcodeLabel(
        barcodeBitmap: Bitmap,
        content: String,
        config: QrConfig,
        position: BarcodeLabelPosition
    ): Bitmap {
        val labelText = config.customBarcodeLabel?.ifBlank { null } ?: content
        val labelHeight = (barcodeBitmap.height * 0.24f).toInt().coerceIn(40, 72)
        val totalHeight = barcodeBitmap.height + labelHeight

        val outBitmap = Bitmap.createBitmap(barcodeBitmap.width, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outBitmap)

        val bg = if (config.isTransparentBackground) Color.TRANSPARENT else config.backgroundColor
        if (!config.isTransparentBackground) {
            canvas.drawColor(bg)
        }

        val barcodeY = if (position == BarcodeLabelPosition.TOP) labelHeight.toFloat() else 0f
        val labelCenterY = if (position == BarcodeLabelPosition.TOP) labelHeight / 2f else barcodeBitmap.height + (labelHeight / 2f)

        canvas.drawBitmap(barcodeBitmap, 0f, barcodeY, null)

        val textPaint = Paint().apply {
            color = config.foregroundColor
            textSize = labelHeight * 0.52f
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val bounds = Rect()
        textPaint.getTextBounds(labelText, 0, labelText.length, bounds)
        val textY = labelCenterY - bounds.exactCenterY()
        canvas.drawText(labelText, barcodeBitmap.width / 2f, textY, textPaint)

        return outBitmap
    }

    private fun attachQrBanner(qrBitmap: Bitmap, bannerText: String, config: QrConfig): Bitmap {
        val bannerHeight = (qrBitmap.height * 0.18f).toInt().coerceIn(52, 120)
        val totalHeight = qrBitmap.height + bannerHeight

        val outBitmap = Bitmap.createBitmap(qrBitmap.width, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outBitmap)

        val bg = if (config.isTransparentBackground) Color.TRANSPARENT else config.backgroundColor
        if (!config.isTransparentBackground) {
            canvas.drawColor(bg)
        }

        val isBottom = config.bannerPosition == BannerPosition.BOTTOM
        val qrY = if (isBottom) 0f else bannerHeight.toFloat()
        val bannerCenterY = if (isBottom) qrBitmap.height + bannerHeight / 2f else bannerHeight / 2f

        canvas.drawBitmap(qrBitmap, 0f, qrY, null)

        // Draw stylish banner pill
        val pillPaint = Paint().apply {
            color = config.foregroundColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val pillH = bannerHeight * 0.65f
        val pillW = qrBitmap.width * 0.76f
        val cx = qrBitmap.width / 2f
        val pillRect = RectF(cx - pillW / 2f, bannerCenterY - pillH / 2f, cx + pillW / 2f, bannerCenterY + pillH / 2f)
        canvas.drawRoundRect(pillRect, pillH / 2f, pillH / 2f, pillPaint)

        val textPaint = Paint().apply {
            color = if (config.isTransparentBackground) Color.WHITE else config.backgroundColor
            textSize = pillH * 0.50f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val bounds = Rect()
        textPaint.getTextBounds(bannerText, 0, bannerText.length, bounds)
        val textY = bannerCenterY - bounds.exactCenterY()
        canvas.drawText(bannerText, cx, textY, textPaint)

        return outBitmap
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

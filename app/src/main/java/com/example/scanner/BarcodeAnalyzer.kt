package com.example.scanner

import android.graphics.RectF
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.Result
import com.google.zxing.ResultPoint
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.multi.GenericMultipleBarcodeReader
import java.util.EnumMap

data class ScannedItem(
    val content: String,
    val format: String,
    val normalizedX: Float = 0.5f,
    val normalizedY: Float = 0.5f,
    val normalizedBounds: RectF? = null
)

class BarcodeAnalyzer(
    private val onMultipleDetections: ((List<ScannedItem>) -> Unit)? = null,
    private val onBarcodeDetected: (content: String, format: String, item: ScannedItem) -> Unit
) : ImageAnalysis.Analyzer {

    // Secondary constructor for single callback backward compatibility
    constructor(onBarcodeDetected: (content: String, format: String) -> Unit) : this(
        onMultipleDetections = null,
        onBarcodeDetected = { content, format, _ -> onBarcodeDetected(content, format) }
    )

    private val reader = MultiFormatReader().apply {
        val hints: MutableMap<DecodeHintType, Any> = EnumMap(DecodeHintType::class.java)
        hints[DecodeHintType.POSSIBLE_FORMATS] = listOf(
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
        hints[DecodeHintType.TRY_HARDER] = java.lang.Boolean.TRUE
        hints[DecodeHintType.CHARACTER_SET] = "UTF-8"
        setHints(hints)
    }

    private val multiReader = GenericMultipleBarcodeReader(reader)

    private var lastScannedContent: String? = null
    private var lastScannedTimestamp: Long = 0
    var duplicateCooldownMs: Long = 1500L
    var isPaused: Boolean = false

    override fun analyze(imageProxy: ImageProxy) {
        if (isPaused) {
            imageProxy.close()
            return
        }

        val plane = imageProxy.planes.getOrNull(0)
        if (plane == null) {
            imageProxy.close()
            return
        }

        val buffer = plane.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        val width = imageProxy.width
        val height = imageProxy.height
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees

        try {
            // In portrait, camera sensor is oriented 90 or 270 degrees.
            // Transposing YUV gives upright horizontal scanlines for 1D barcodes and 1:1 screen coordinates.
            val shouldRotate = rotationDegrees == 90 || rotationDegrees == 270
            val (analyzedBytes, effWidth, effHeight) = if (shouldRotate) {
                Triple(rotateYuv90(bytes, width, height), height, width)
            } else {
                Triple(bytes, width, height)
            }

            // Full-frame analysis: 0, 0, effWidth, effHeight without artificial square clipping
            val source = PlanarYUVLuminanceSource(
                analyzedBytes,
                effWidth,
                effHeight,
                0,
                0,
                effWidth,
                effHeight,
                false
            )
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

            // Attempt multi-code detection across the entire frame
            val results: Array<Result> = try {
                multiReader.decodeMultiple(binaryBitmap)
            } catch (_: Exception) {
                try {
                    val single = reader.decodeWithState(binaryBitmap)
                    if (single != null) arrayOf(single) else emptyArray()
                } catch (_: Exception) {
                    emptyArray()
                }
            }

            if (results.isNotEmpty()) {
                val now = System.currentTimeMillis()
                val detectedItems = results.map { res ->
                    val pts = res.resultPoints
                    val bounds = calculateNormalizedBounds(pts, effWidth, effHeight)
                    val centerX = bounds?.centerX() ?: 0.5f
                    val centerY = bounds?.centerY() ?: 0.5f

                    ScannedItem(
                        content = res.text,
                        format = res.barcodeFormat.name,
                        normalizedX = centerX,
                        normalizedY = centerY,
                        normalizedBounds = bounds
                    )
                }

                if (detectedItems.size > 1 && onMultipleDetections != null) {
                    onMultipleDetections.invoke(detectedItems)
                } else {
                    val first = detectedItems[0]
                    if (first.content != lastScannedContent || now - lastScannedTimestamp > duplicateCooldownMs) {
                        lastScannedContent = first.content
                        lastScannedTimestamp = now
                        onBarcodeDetected(first.content, first.format, first)
                    }
                }
            }
        } catch (_: Exception) {
            // No barcode found in current frame; continue smoothly
        } finally {
            reader.reset()
            imageProxy.close()
        }
    }

    private fun rotateYuv90(data: ByteArray, width: Int, height: Int): ByteArray {
        val rotated = ByteArray(data.size)
        var i = 0
        for (x in 0 until width) {
            for (y in height - 1 downTo 0) {
                rotated[i++] = data[y * width + x]
            }
        }
        return rotated
    }

    private fun calculateNormalizedBounds(pts: Array<ResultPoint>?, width: Int, height: Int): RectF? {
        if (pts.isNullOrEmpty()) return null
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var maxY = Float.MIN_VALUE

        for (pt in pts) {
            if (pt.x < minX) minX = pt.x
            if (pt.y < minY) minY = pt.y
            if (pt.x > maxX) maxX = pt.x
            if (pt.y > maxY) maxY = pt.y
        }

        // Add 16px buffer around detected points
        val margin = 16f
        val clampedMinX = ((minX - margin) / width).coerceIn(0f, 1f)
        val clampedMinY = ((minY - margin) / height).coerceIn(0f, 1f)
        val clampedMaxX = ((maxX + margin) / width).coerceIn(0f, 1f)
        val clampedMaxY = ((maxY + margin) / height).coerceIn(0f, 1f)

        return RectF(clampedMinX, clampedMinY, clampedMaxX, clampedMaxY)
    }
}

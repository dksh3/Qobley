package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.print.pdf.PrintedPdfDocument
import java.io.FileOutputStream

object QobleyPrintHelper {

    fun printCode(
        context: Context,
        title: String,
        content: String,
        bitmap: Bitmap,
        onFinished: (() -> Unit)? = null
    ) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val jobName = "Qobley_${title.replace(" ", "_")}"

        printManager.print(jobName, object : PrintDocumentAdapter() {
            private var pdfDocument: PrintedPdfDocument? = null

            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback,
                extras: Bundle?
            ) {
                pdfDocument = PrintedPdfDocument(context, newAttributes)
                if (cancellationSignal?.isCanceled == true) {
                    callback.onLayoutCancelled()
                    return
                }
                val info = PrintDocumentInfo.Builder("qobley_code.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()
                callback.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback
            ) {
                val doc = pdfDocument ?: return
                val page = doc.startPage(0)
                if (cancellationSignal?.isCanceled == true) {
                    callback.onWriteCancelled()
                    doc.close()
                    return
                }

                val canvas = page.canvas
                val pageWidth = canvas.width
                val pageHeight = canvas.height

                // Draw Title
                val titlePaint = Paint().apply {
                    color = Color.BLACK
                    textSize = 26f
                    isFakeBoldText = true
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }
                canvas.drawText(title, pageWidth / 2f, 90f, titlePaint)

                // Draw Subtitle / Preview text
                val subPaint = Paint().apply {
                    color = Color.DKGRAY
                    textSize = 13f
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }
                val subtitle = if (content.length > 60) content.take(57) + "..." else content
                canvas.drawText(subtitle, pageWidth / 2f, 118f, subPaint)

                // Scale and center the bitmap
                val is1D = bitmap.width > bitmap.height * 1.4f
                val maxDim = (pageWidth * 0.70f).toInt().coerceAtMost((pageHeight * 0.50f).toInt())
                val destWidth = if (is1D) (pageWidth * 0.82f).toInt() else maxDim
                val destHeight = if (is1D) (destWidth * (bitmap.height.toFloat() / bitmap.width)).toInt() else maxDim
                val left = (pageWidth - destWidth) / 2
                val top = (pageHeight - destHeight) / 2 - 20
                val destRect = Rect(left, top, left + destWidth, top + destHeight)
                val srcRect = Rect(0, 0, bitmap.width, bitmap.height)
                canvas.drawBitmap(bitmap, srcRect, destRect, null)

                // Footer branding
                val footerPaint = Paint().apply {
                    color = Color.GRAY
                    textSize = 10f
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }
                canvas.drawText("Printed with Qobley · Universal Code Studio", pageWidth / 2f, pageHeight - 50f, footerPaint)

                doc.finishPage(page)

                try {
                    val out = FileOutputStream(destination.fileDescriptor)
                    doc.writeTo(out)
                    callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    onFinished?.invoke()
                } catch (e: Exception) {
                    callback.onWriteFailed(e.message)
                } finally {
                    doc.close()
                }
            }
        }, PrintAttributes.Builder().build())
    }
}

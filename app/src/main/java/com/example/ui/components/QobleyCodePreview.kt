package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.generator.BarcodeGenerator
import com.example.scanner.BarcodeDecoder
import com.example.util.QobleyPrintHelper
import java.io.File
import java.io.FileOutputStream

@Composable
fun QobleyCodePreview(
    content: String,
    format: String,
    modifier: Modifier = Modifier,
    displayLabel: String? = null,
    qrConfig: BarcodeGenerator.QrConfig = BarcodeGenerator.QrConfig(),
    showActionRow: Boolean = true,
    onToast: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val bitmap = remember(content, format, qrConfig) {
        BarcodeGenerator.generate(
            formatString = format,
            content = content,
            width = 800,
            height = 800,
            qrConfig = qrConfig
        )
    }

    val isScannable = remember(bitmap, content) {
        if (bitmap != null && content.isNotEmpty()) {
            val decoded = BarcodeDecoder.decodeBitmap(bitmap)
            // Either exact match or prefix/contains match
            decoded.any { it.content == content || it.content.contains(content) || content.contains(it.content) }
        } else {
            false
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // High contrast preview card - Solid object feel with quiet border
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (qrConfig.isTransparentBackground) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f) else Color(qrConfig.backgroundColor),
            modifier = Modifier
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f), RoundedCornerShape(14.dp))
                .clickable {
                    clipboardManager.setText(AnnotatedString(content))
                    onToast("Content copied to clipboard")
                }
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (bitmap != null) {
                    val is1D = format != "QR_CODE" && format != "DATA_MATRIX" && format != "AZTEC" && format != "PDF_417"
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Code Preview",
                        modifier = Modifier
                            .sizeIn(
                                minWidth = if (is1D) 200.dp else 180.dp,
                                maxWidth = if (is1D) 320.dp else 240.dp,
                                minHeight = if (is1D) 90.dp else 180.dp,
                                maxHeight = if (is1D) 160.dp else 260.dp
                            )
                            .testTag("code_preview_image")
                    )

                    if (!displayLabel.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = displayLabel,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(qrConfig.foregroundColor)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = format.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = Color(qrConfig.foregroundColor).copy(alpha = 0.7f),
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Scannability Verification Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .background(
                                color = if (isScannable) Color(0xFF059669).copy(alpha = 0.12f) else Color(0xFFD97706).copy(alpha = 0.12f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (isScannable) Icons.Outlined.CheckCircle else Icons.Outlined.WarningAmber,
                            contentDescription = null,
                            tint = if (isScannable) Color(0xFF059669) else Color(0xFFD97706),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isScannable) "Scannable · Verified" else "Test decode check",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isScannable) Color(0xFF059669) else Color(0xFFD97706),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Invalid Code Input",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }

        if (showActionRow) {
            Spacer(modifier = Modifier.height(12.dp))

            // Action row: Copy, Share, Download, Print
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(content))
                        onToast("Payload copied to clipboard")
                    },
                    modifier = Modifier.testTag("copy_preview_content")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "Copy Content",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = {
                        shareContent(context, content)
                    },
                    modifier = Modifier.testTag("share_preview_content")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share Content",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (bitmap != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            shareOrSaveBitmap(context, bitmap, format, onToast)
                        },
                        modifier = Modifier.testTag("export_preview_image")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Download,
                            contentDescription = "Export Code Image",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            val title = displayLabel ?: format.replace("_", " ")
                            QobleyPrintHelper.printCode(
                                context = context,
                                title = title,
                                content = content,
                                bitmap = bitmap,
                                onFinished = { onToast("Printing job sent") }
                            )
                        },
                        modifier = Modifier.testTag("print_preview_code")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Print,
                            contentDescription = "Print Code",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun shareContent(context: Context, text: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share with"))
}

private fun shareOrSaveBitmap(
    context: Context,
    bitmap: Bitmap,
    format: String,
    onToast: (String) -> Unit
) {
    try {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "qobley_${format.lowercase()}_${System.currentTimeMillis()}.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, contentUri)
            type = "image/png"
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(Intent.createChooser(intent, "Export Code Image"))
        onToast("Image ready to save or share")
    } catch (e: Exception) {
        onToast("Export failed: ${e.message}")
    }
}

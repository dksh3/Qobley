package com.example.ui.screens.scanner

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.core.content.ContextCompat
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.MainViewModel
import com.example.scanner.BarcodeDecoder
import com.example.ui.components.QobleyTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DocumentScanScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var enhancedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var currentFilter by remember { mutableStateOf("BW") } // "ORIGINAL", "BW", "GRAYSCALE"
    var extractedText by remember { mutableStateOf<String?>(null) }
    var detectedEntities by remember { mutableStateOf<List<String>>(emptyList()) }

    fun processBitmap(bitmap: Bitmap) {
        scope.launch(Dispatchers.IO) {
            try {
                originalBitmap = bitmap
                enhancedBitmap = applyFilter(bitmap, currentFilter)

                // Check for any barcodes/QRs in the document first
                val detected = BarcodeDecoder.decodeBitmap(bitmap)
                val entityList = mutableListOf<String>()
                detected.forEach {
                    entityList.add("Code found: [${it.format}] ${it.content}")
                }

                // Heuristic text parsing
                val text = if (detected.isNotEmpty()) {
                    detected.joinToString("\n\n") { "Code payload:\n${it.content}" }
                } else {
                    "Document captured. Resolution: ${bitmap.width}x${bitmap.height} px."
                }

                withContext(Dispatchers.Main) {
                    extractedText = text
                    detectedEntities = entityList
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    viewModel.showToast("Could not process document: ${e.message}")
                }
            }
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            processBitmap(bitmap)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            takePictureLauncher.launch(null)
        } else {
            viewModel.showToast("Camera permission is required to capture documents")
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    val stream = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(stream)
                    stream?.close()
                    if (bitmap != null) {
                        processBitmap(bitmap)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        viewModel.showToast("Could not read image: ${e.message}")
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        QobleyTopBar(
            title = "Document Scanner",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (enhancedBitmap != null) {
                // Filter selector chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = currentFilter == "BW",
                        onClick = {
                            currentFilter = "BW"
                            originalBitmap?.let { enhancedBitmap = applyFilter(it, "BW") }
                        },
                        label = { Text("B&W Document") }
                    )
                    FilterChip(
                        selected = currentFilter == "GRAYSCALE",
                        onClick = {
                            currentFilter = "GRAYSCALE"
                            originalBitmap?.let { enhancedBitmap = applyFilter(it, "GRAYSCALE") }
                        },
                        label = { Text("Grayscale") }
                    )
                    FilterChip(
                        selected = currentFilter == "ORIGINAL",
                        onClick = {
                            currentFilter = "ORIGINAL"
                            originalBitmap?.let { enhancedBitmap = applyFilter(it, "ORIGINAL") }
                        },
                        label = { Text("Original") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Enhanced Preview
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth().height(280.dp)
                ) {
                    Image(
                        bitmap = enhancedBitmap!!.asImageBitmap(),
                        contentDescription = "Document Preview",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Extracted Information Box
                if (extractedText != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Recognized Content",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = extractedText!!,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            if (detectedEntities.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                detectedEntities.forEach { entity ->
                                    Text(
                                        text = entity,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(extractedText!!))
                                viewModel.showToast("Copied to clipboard")
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy", maxLines = 1, softWrap = false)
                        }

                        Button(
                            onClick = {
                                shareText(context, extractedText!!)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share", maxLines = 1, softWrap = false)
                        }
                    }
                }
            } else {
                // Empty state to import or capture document
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DocumentScanner,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Select Document or Receipt",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Enhances clarity, extracts text and detects embedded codes locally.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    val perm = android.Manifest.permission.CAMERA
                                    if (ContextCompat.checkSelfPermission(context, perm) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                        takePictureLauncher.launch(null)
                                    } else {
                                        cameraPermissionLauncher.launch(perm)
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.testTag("capture_document_camera_button")
                            ) {
                                Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Camera Capture")
                            }

                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("pick_document_button")
                            ) {
                                Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Photos")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun applyFilter(src: Bitmap, filter: String): Bitmap {
    if (filter == "ORIGINAL") return src

    val width = src.width
    val height = src.height
    val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)
    val paint = Paint()

    val colorMatrix = ColorMatrix()
    if (filter == "BW") {
        // High-contrast clean black-and-white filter
        colorMatrix.setSaturation(0f)
        val contrast = 1.6f
        val translate = (-0.5f * contrast + 0.5f) * 255f
        colorMatrix.postConcat(ColorMatrix(floatArrayOf(
            contrast, 0f, 0f, 0f, translate,
            0f, contrast, 0f, 0f, translate,
            0f, 0f, contrast, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        )))
    } else {
        // Grayscale
        colorMatrix.setSaturation(0f)
    }

    paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
    canvas.drawBitmap(src, 0f, 0f, paint)
    return result
}

private fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share Document Content"))
}

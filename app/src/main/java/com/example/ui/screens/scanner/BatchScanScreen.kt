package com.example.ui.screens.scanner

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.ViewWeek
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.MainViewModel
import com.example.data.model.CodeItem
import com.example.scanner.BarcodeAnalyzer
import java.util.concurrent.Executors

@Composable
fun BatchScanScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val batchItems by viewModel.batchItems.collectAsState()
    val duplicatePrevention by viewModel.settingsRepo.duplicatePrevention.collectAsState()

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val analyzer = remember {
        BarcodeAnalyzer { content, format ->
            viewModel.addBatchItem(content, format)
        }
    }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("batch_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "Scanned: ${batchItems.size}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            if (batchItems.isNotEmpty()) {
                IconButton(
                    onClick = { viewModel.clearBatch() },
                    modifier = Modifier.testTag("batch_clear_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Clear All",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }
        }

        // Viewfinder pane (compact top half)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(horizontal = 16.dp)
                .background(Color.Black, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(cameraExecutor, analyzer)
                            }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Scanning crosshair
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .border(2.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Duplicate prevention toggle row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Prevent Duplicate Scans",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Switch(
                checked = duplicatePrevention,
                onCheckedChange = { viewModel.settingsRepo.setDuplicatePrevention(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Batch Items List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            if (batchItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Point camera at codes sequentially to add them to this batch.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(batchItems) { item ->
                    BatchItemRow(item = item)
                }
            }
        }

        // Bottom Actions Bar
        var showExportDialog by remember { mutableStateOf(false) }

        if (showExportDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showExportDialog = false },
                title = { Text("Export Batch Data") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        androidx.compose.material3.TextButton(
                            onClick = {
                                showExportDialog = false
                                exportBatch(context, batchItems, "csv")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("CSV (.csv)", maxLines = 1, softWrap = false)
                        }
                        androidx.compose.material3.TextButton(
                            onClick = {
                                showExportDialog = false
                                exportBatch(context, batchItems, "txt")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Plain Text (.txt)", maxLines = 1, softWrap = false)
                        }
                        androidx.compose.material3.TextButton(
                            onClick = {
                                showExportDialog = false
                                exportBatch(context, batchItems, "json")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("JSON (.json)", maxLines = 1, softWrap = false)
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = { showExportDialog = false }) {
                        Text("Cancel", maxLines = 1, softWrap = false)
                    }
                }
            )
        }

        if (batchItems.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { showExportDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("export_batch_button")
                ) {
                    Icon(imageVector = Icons.Outlined.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export", maxLines = 1, softWrap = false)
                }

                Button(
                    onClick = {
                        viewModel.saveBatchToHistory()
                        onBack()
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.weight(1f).testTag("save_batch_button")
                ) {
                    Icon(imageVector = Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save", maxLines = 1, softWrap = false)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}

@Composable
private fun BatchItemRow(item: CodeItem) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (item.type == "QR") Icons.Outlined.QrCode else Icons.Outlined.ViewWeek,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.content,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
            }
            Text(
                text = item.format.replace("_", " "),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun exportBatch(context: Context, items: List<CodeItem>, format: String) {
    val (content, mimeType, filename) = when (format) {
        "json" -> {
            val json = buildString {
                appendLine("[")
                items.forEachIndexed { index, item ->
                    val escaped = item.content.replace("\\", "\\\\").replace("\"", "\\\"")
                    append("  {\"index\": ${index + 1}, \"format\": \"${item.format}\", \"content\": \"$escaped\", \"timestamp\": ${item.createdAt}}")
                    if (index < items.size - 1) appendLine(",") else appendLine()
                }
                append("]")
            }
            Triple(json, "application/json", "qobley_batch_${System.currentTimeMillis()}.json")
        }
        "txt" -> {
            val txt = buildString {
                appendLine("Qobley Batch Scans (${items.size} items)")
                appendLine("---------------------------------------")
                items.forEachIndexed { index, item ->
                    appendLine("${index + 1}. [${item.format}] ${item.content}")
                }
            }
            Triple(txt, "text/plain", "qobley_batch_${System.currentTimeMillis()}.txt")
        }
        else -> { // csv
            val csv = buildString {
                appendLine("Index,Format,Content,Timestamp")
                items.forEachIndexed { index, item ->
                    val escaped = item.content.replace("\"", "\"\"")
                    appendLine("${index + 1},\"${item.format}\",\"$escaped\",${item.createdAt}")
                }
            }
            Triple(csv, "text/csv", "qobley_batch_${System.currentTimeMillis()}.csv")
        }
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Qobley Batch Scan ($filename)")
        putExtra(Intent.EXTRA_TEXT, content)
    }
    context.startActivity(Intent.createChooser(intent, "Export Batch Scan"))
}

package com.example.ui.screens.scanner

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.ViewWeek
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.MainViewModel
import com.example.scanner.BarcodeAnalyzer
import com.example.scanner.ScannedItem
import com.example.ui.components.QobleyScannerOverlay
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Composable
fun ScannerScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onNavigateBatch: () -> Unit,
    onNavigateDocument: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptic = LocalHapticFeedback.current

    var hasCameraPermission by remember { mutableStateOf(false) }
    var isTorchOn by remember { mutableStateOf(false) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var cameraControl by remember { mutableStateOf<androidx.camera.core.CameraControl?>(null) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    var currentZoom by remember { mutableFloatStateOf(1f) }

    // Instant detection highlight (result indicator) & multi-code markers
    var detectedHighlight by remember { mutableStateOf<ScannedItem?>(null) }
    var liveMultipleDetections by remember { mutableStateOf<List<ScannedItem>?>(null) }

    val multipleDetectionsDialog by viewModel.multipleDetections.collectAsState()
    val activeScanResult by viewModel.activeScanResult.collectAsState()

    // Camera permission request launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            viewModel.showToast("Camera permission is required to scan codes")
        }
    }

    LaunchedEffect(Unit) {
        val permission = android.Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(context, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            hasCameraPermission = true
        } else {
            permissionLauncher.launch(permission)
        }
    }

    // Photo picker launcher (Gallery)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.scanImageUri(context, uri)
        }
    }

    val scope = rememberCoroutineScope()
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    val analyzer = remember {
        BarcodeAnalyzer(
            onMultipleDetections = { detections ->
                if (activeScanResult == null) {
                    liveMultipleDetections = detections
                    try {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    } catch (_: Exception) {}
                }
            },
            onBarcodeDetected = { content, format, item ->
                if (activeScanResult == null && !analyzer.isPaused) {
                    detectedHighlight = item
                    liveMultipleDetections = null
                    analyzer.isPaused = true
                    try {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    } catch (_: Exception) {}
                    viewModel.handleScannedCode(content, format)

                    // 2-second cooldown so detection HUD displays while instant action runs
                    scope.launch {
                        kotlinx.coroutines.delay(2000)
                        if (activeScanResult == null) {
                            detectedHighlight = null
                            analyzer.isPaused = false
                        }
                    }
                }
            }
        )
    }

    // Handle resume scanning when active result sheet is dismissed
    LaunchedEffect(activeScanResult) {
        if (activeScanResult == null) {
            analyzer.isPaused = false
            detectedHighlight = null
            liveMultipleDetections = null
        } else {
            analyzer.isPaused = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasCameraPermission) {
            // Full-frame camera preview with pinch-to-zoom & tap-to-focus
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                    previewViewRef = previewView
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

                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(lensFacing)
                            .build()

                        try {
                            cameraProvider.unbindAll()
                            val camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                            cameraControl = camera.cameraControl
                            cameraControl?.enableTorch(isTorchOn)
                            cameraControl?.setZoomRatio(currentZoom)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                update = {
                    cameraControl?.enableTorch(isTorchOn)
                    cameraControl?.setZoomRatio(currentZoom)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        // Pinch to zoom gesture support
                        detectTransformGestures { _, _, zoom, _ ->
                            val next = (currentZoom * zoom).coerceIn(1f, 5f)
                            if (next != currentZoom) {
                                currentZoom = next
                                cameraControl?.setZoomRatio(next)
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        // Tap to focus
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull()
                                if (change != null && change.pressed) {
                                    val pv = previewViewRef
                                    if (pv != null) {
                                        val point = pv.meteringPointFactory.createPoint(
                                            change.position.x,
                                            change.position.y
                                        )
                                        val action = FocusMeteringAction.Builder(
                                            point,
                                            FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE
                                        ).setAutoCancelDuration(3, TimeUnit.SECONDS).build()
                                        cameraControl?.startFocusAndMetering(action)
                                    }
                                }
                            }
                        }
                    }
            )
        } else {
            // Permission request screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Camera Permission Required",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Qobley needs camera access to point and scan codes. Scans stay strictly on this device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFA1A1AA)
                )
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(
                    onClick = { permissionLauncher.launch(android.Manifest.permission.CAMERA) },
                    modifier = Modifier.testTag("request_camera_permission_button")
                ) {
                    Text("Grant Permission", color = Color.White)
                }
            }
        }

        // Modern instant full-frame overlay without artificial scan boxes
        QobleyScannerOverlay(
            isTorchOn = isTorchOn,
            onToggleTorch = {
                isTorchOn = !isTorchOn
                cameraControl?.enableTorch(isTorchOn)
            },
            onSwitchCamera = {
                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                    CameraSelector.LENS_FACING_FRONT
                } else {
                    CameraSelector.LENS_FACING_BACK
                }
                isTorchOn = false
            },
            onOpenGallery = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onBack = onBack,
            currentZoom = currentZoom,
            onToggleZoom = {
                val nextZoom = if (currentZoom < 1.4f) 2.0f else 1.0f
                currentZoom = nextZoom
                cameraControl?.setZoomRatio(nextZoom)
            },
            detectedHighlight = detectedHighlight,
            multipleDetections = liveMultipleDetections,
            onSelectDetection = { selected ->
                detectedHighlight = selected
                liveMultipleDetections = null
                analyzer.isPaused = true
                viewModel.handleScannedCode(selected.content, selected.format)
            }
        )
    }

    // Imported image multiple detections dialog
    if (multipleDetectionsDialog != null) {
        val items = multipleDetectionsDialog!!
        AlertDialog(
            onDismissRequest = { viewModel.clearMultipleDetections() },
            title = {
                Text("${items.size} codes found", style = MaterialTheme.typography.titleLarge)
            },
            text = {
                LazyColumn {
                    items(items) { detected ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    viewModel.clearMultipleDetections()
                                    viewModel.handleScannedCode(detected.content, detected.format, "IMPORTED")
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (detected.format == "QR_CODE") Icons.Outlined.QrCode else Icons.Outlined.ViewWeek,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = detected.format.replace("_", " "),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = detected.content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.clearMultipleDetections() }) {
                    Text("Cancel")
                }
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}

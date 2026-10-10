package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.navigation.Screen
import com.example.ui.screens.create.BarcodeCreatorScreen
import com.example.ui.screens.create.CreateScreen
import com.example.ui.screens.create.QrCreatorScreen
import com.example.ui.screens.history.HistoryScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.result.CodeDetailScreen
import com.example.ui.screens.result.ScanResultSheet
import com.example.ui.screens.saved.SavedScreen
import com.example.ui.screens.scanner.BatchScanScreen
import com.example.ui.screens.scanner.DocumentScanScreen
import com.example.ui.screens.scanner.ScannerScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle initial intent
        handleIntent(intent)

        setContent {
            val themeMode by viewModel.settingsRepo.themeMode.collectAsState()
            val isDark = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDark) {
                QobleyApp(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return

        when (intent.action) {
            Intent.ACTION_VIEW -> {
                val data: Uri? = intent.data
                if (data != null && data.scheme == "qobley") {
                    val host = data.host ?: ""
                    val pathSegments = data.pathSegments
                    when (host) {
                        "scan" -> {
                            if (pathSegments.isNotEmpty() && pathSegments[0] == "image") {
                                viewModel.postNavigationRequest(MainViewModel.NavigationRequest.ScanImage)
                            } else {
                                viewModel.setActiveScanResult(null)
                                viewModel.postNavigationRequest(MainViewModel.NavigationRequest.Scanner)
                            }
                        }
                        "create" -> {
                            val subType = if (pathSegments.isNotEmpty()) pathSegments[0] else "qr"
                            when (subType.lowercase()) {
                                "barcode" -> viewModel.postNavigationRequest(MainViewModel.NavigationRequest.CreateBarcode)
                                "wifi" -> viewModel.postNavigationRequest(MainViewModel.NavigationRequest.CreateQr(initialType = "WIFI"))
                                "payment" -> viewModel.postNavigationRequest(MainViewModel.NavigationRequest.CreateQr(initialType = "PAYMENT"))
                                "contact" -> viewModel.postNavigationRequest(MainViewModel.NavigationRequest.CreateQr(initialType = "CONTACT"))
                                "url" -> viewModel.postNavigationRequest(MainViewModel.NavigationRequest.CreateQr(initialType = "URL"))
                                "text" -> viewModel.postNavigationRequest(MainViewModel.NavigationRequest.CreateQr(initialType = "TEXT"))
                                else -> viewModel.postNavigationRequest(MainViewModel.NavigationRequest.CreateQr())
                            }
                        }
                        "history" -> viewModel.postNavigationRequest(MainViewModel.NavigationRequest.History)
                        "saved" -> viewModel.postNavigationRequest(MainViewModel.NavigationRequest.Saved)
                        "item" -> {
                            val id = if (pathSegments.isNotEmpty()) pathSegments[0].toLongOrNull() else null
                            if (id != null) {
                                viewModel.postNavigationRequest(MainViewModel.NavigationRequest.ViewDetail(id))
                            }
                        }
                    }
                }
            }
            Intent.ACTION_SEND -> {
                if (intent.type == "text/plain") {
                    val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                    if (!sharedText.isNullOrBlank()) {
                        viewModel.handleScannedCode(sharedText, "QR_CODE", "IMPORTED")
                    }
                } else if (intent.type?.startsWith("image/") == true) {
                    val imageUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                    if (imageUri != null) {
                        viewModel.scanImageUri(this, imageUri)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QobleyApp(viewModel: MainViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var currentTab by remember { mutableStateOf("home") } // home, create, history, saved, settings
    var currentSubScreen by remember { mutableStateOf<String?>(null) } // scanner, batch, document, qr_creator, barcode_creator, detail
    var detailCodeId by remember { mutableLongStateOf(0L) }
    var qrInitialType by remember { mutableStateOf("URL") }
    var barcodeInitialFormat by remember { mutableStateOf("CODE_128") }

    val activeScanResult by viewModel.activeScanResult.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val navRequest by viewModel.navigationRequest.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Android zero-permission Photo Picker launcher for navigation requests
    val photoPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.scanImageUri(context, uri)
        }
    }

    LaunchedEffect(navRequest) {
        val req = navRequest ?: return@LaunchedEffect
        when (req) {
            is MainViewModel.NavigationRequest.Scanner -> {
                currentSubScreen = "scanner"
            }
            is MainViewModel.NavigationRequest.ScanImage -> {
                photoPickerLauncher.launch(
                    androidx.activity.result.PickVisualMediaRequest(
                        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }
            is MainViewModel.NavigationRequest.CreateQr -> {
                qrInitialType = req.initialType
                currentSubScreen = "qr_creator"
            }
            is MainViewModel.NavigationRequest.CreateBarcode -> {
                barcodeInitialFormat = "CODE_128"
                currentSubScreen = "barcode_creator"
            }
            is MainViewModel.NavigationRequest.History -> {
                currentSubScreen = null
                currentTab = "history"
            }
            is MainViewModel.NavigationRequest.Saved -> {
                currentSubScreen = null
                currentTab = "saved"
            }
            is MainViewModel.NavigationRequest.ViewDetail -> {
                scope.launch {
                    val code = viewModel.repository.getById(req.codeId)
                    if (code != null) {
                        viewModel.setActiveScanResult(code)
                    }
                }
            }
        }
        viewModel.clearNavigationRequest()
    }

    BackHandler(enabled = currentSubScreen != null) {
        currentSubScreen = null
    }

    LaunchedEffect(toastMessage) {
        val msg = toastMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    val isFullScreenOverlay = currentSubScreen in listOf("scanner", "batch", "document")

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!isFullScreenOverlay) {
                NavigationBar(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("qobley_bottom_navigation")
                ) {
                    NavigationBarItem(
                        selected = currentTab == "home" && currentSubScreen == null,
                        onClick = {
                            currentTab = "home"
                            currentSubScreen = null
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == "home" && currentSubScreen == null) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("Home") },
                        modifier = Modifier.testTag("nav_item_home")
                    )

                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            currentSubScreen = "scanner"
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.QrCodeScanner,
                                contentDescription = "Scan"
                            )
                        },
                        label = { Text("Scan") },
                        modifier = Modifier.testTag("nav_item_scan")
                    )

                    NavigationBarItem(
                        selected = currentTab == "create" && currentSubScreen == null,
                        onClick = {
                            currentTab = "create"
                            currentSubScreen = null
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.AddCircleOutline,
                                contentDescription = "Create"
                            )
                        },
                        label = { Text("Create") },
                        modifier = Modifier.testTag("nav_item_create")
                    )

                    NavigationBarItem(
                        selected = currentTab == "history" && currentSubScreen == null,
                        onClick = {
                            currentTab = "history"
                            currentSubScreen = null
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == "history" && currentSubScreen == null) Icons.Filled.History else Icons.Outlined.History,
                                contentDescription = "History"
                            )
                        },
                        label = { Text("History") },
                        modifier = Modifier.testTag("nav_item_history")
                    )

                    NavigationBarItem(
                        selected = currentTab == "saved" && currentSubScreen == null,
                        onClick = {
                            currentTab = "saved"
                            currentSubScreen = null
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == "saved" && currentSubScreen == null) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Saved"
                            )
                        },
                        label = { Text("Saved") },
                        modifier = Modifier.testTag("nav_item_saved")
                    )

                    NavigationBarItem(
                        selected = currentTab == "settings" && currentSubScreen == null,
                        onClick = {
                            currentTab = "settings"
                            currentSubScreen = null
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == "settings" && currentSubScreen == null) Icons.Filled.Settings else Icons.Outlined.Settings,
                                contentDescription = "Settings"
                            )
                        },
                        label = { Text("Settings") },
                        modifier = Modifier.testTag("nav_item_settings")
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        val context = androidx.compose.ui.platform.LocalContext.current
        val reduceMotion = remember(context) {
            try {
                val scale = android.provider.Settings.Global.getFloat(
                    context.contentResolver,
                    android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
                    1.0f
                )
                scale == 0f
            } catch (_: Exception) {
                false
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isFullScreenOverlay) androidx.compose.foundation.layout.PaddingValues() else innerPadding)
        ) {
            AnimatedContent(
                targetState = currentSubScreen to currentTab,
                transitionSpec = {
                    if (reduceMotion) {
                        fadeIn(animationSpec = tween(40)) togetherWith
                        fadeOut(animationSpec = tween(40))
                    } else {
                        val targetSub = targetState.first
                        val initialSub = initialState.first
                        when {
                            // Scanner / Camera screen: fade + slide vertically
                            targetSub in listOf("scanner", "batch", "document") -> {
                                (slideInVertically(animationSpec = tween(180)) { it / 6 } + fadeIn(animationSpec = tween(180))) togetherWith
                                (slideOutVertically(animationSpec = tween(150)) { -it / 8 } + fadeOut(animationSpec = tween(150)))
                            }
                            initialSub in listOf("scanner", "batch", "document") -> {
                                fadeIn(animationSpec = tween(150)) togetherWith
                                (slideOutVertically(animationSpec = tween(160)) { it / 6 } + fadeOut(animationSpec = tween(160)))
                            }
                            // Creator & Detail screens: horizontal slide + fade
                            targetSub in listOf("qr_creator", "barcode_creator", "detail") -> {
                                (slideInHorizontally(animationSpec = tween(180)) { it / 6 } + fadeIn(animationSpec = tween(180))) togetherWith
                                (slideOutHorizontally(animationSpec = tween(150)) { -it / 8 } + fadeOut(animationSpec = tween(150)))
                            }
                            initialSub in listOf("qr_creator", "barcode_creator", "detail") -> {
                                fadeIn(animationSpec = tween(150)) togetherWith
                                (slideOutHorizontally(animationSpec = tween(160)) { it / 6 } + fadeOut(animationSpec = tween(160)))
                            }
                            // Default tab switch: clean subtle crossfade
                            else -> {
                                fadeIn(animationSpec = tween(150)) togetherWith
                                fadeOut(animationSpec = tween(130))
                            }
                        }
                    }
                },
                label = "subscreen_transition"
            ) { (subScreen, tab) ->
                when {
                    subScreen == "scanner" -> {
                        ScannerScreen(
                            viewModel = viewModel,
                            onBack = { currentSubScreen = null },
                            onNavigateBatch = { currentSubScreen = "batch" },
                            onNavigateDocument = { currentSubScreen = "document" }
                        )
                    }

                    subScreen == "batch" -> {
                        BatchScanScreen(
                            viewModel = viewModel,
                            onBack = { currentSubScreen = null }
                        )
                    }

                    subScreen == "document" -> {
                        DocumentScanScreen(
                            viewModel = viewModel,
                            onBack = { currentSubScreen = null }
                        )
                    }

                    subScreen == "qr_creator" -> {
                        QrCreatorScreen(
                            initialType = qrInitialType,
                            viewModel = viewModel,
                            onBack = { currentSubScreen = null }
                        )
                    }

                    subScreen == "barcode_creator" -> {
                        BarcodeCreatorScreen(
                            initialFormat = barcodeInitialFormat,
                            viewModel = viewModel,
                            onBack = { currentSubScreen = null }
                        )
                    }

                    subScreen == "detail" -> {
                        CodeDetailScreen(
                            codeId = detailCodeId,
                            viewModel = viewModel,
                            onBack = { currentSubScreen = null }
                        )
                    }

                    else -> {
                        when (tab) {
                            "home" -> {
                                HomeScreen(
                                    viewModel = viewModel,
                                    onNavigateScan = { currentSubScreen = "scanner" },
                                    onNavigateBatch = { currentSubScreen = "batch" },
                                    onNavigateDocument = { currentSubScreen = "document" },
                                    onNavigateCreateQr = { type ->
                                        qrInitialType = type ?: "URL"
                                        currentSubScreen = "qr_creator"
                                    },
                                    onNavigateCreateBarcode = {
                                        barcodeInitialFormat = "CODE_128"
                                        currentSubScreen = "barcode_creator"
                                    },
                                    onNavigateSettings = { currentTab = "settings" },
                                    onViewDetail = { item ->
                                        viewModel.setActiveScanResult(item)
                                    }
                                )
                            }

                            "create" -> {
                                CreateScreen(
                                    onSelectQrType = { type ->
                                        qrInitialType = type
                                        currentSubScreen = "qr_creator"
                                    },
                                    onSelectBarcodeFormat = { fmt ->
                                        barcodeInitialFormat = fmt
                                        currentSubScreen = "barcode_creator"
                                    }
                                )
                            }

                            "history" -> {
                                HistoryScreen(
                                    viewModel = viewModel,
                                    onViewDetail = { item ->
                                        viewModel.setActiveScanResult(item)
                                    },
                                    onNavigateScan = { currentSubScreen = "scanner" },
                                    onShowSnackbarWithUndo = { msg, onUndo ->
                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = msg,
                                                actionLabel = "Undo",
                                                duration = SnackbarDuration.Short
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                onUndo()
                                            }
                                        }
                                    }
                                )
                            }

                            "saved" -> {
                                SavedScreen(
                                    viewModel = viewModel,
                                    onViewDetail = { item ->
                                        viewModel.setActiveScanResult(item)
                                    },
                                    onNavigateCreate = {
                                        currentTab = "create"
                                    },
                                    onNavigateScan = { currentSubScreen = "scanner" },
                                    onShowSnackbarWithUndo = { msg, onUndo ->
                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = msg,
                                                actionLabel = "Undo",
                                                duration = SnackbarDuration.Short
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                onUndo()
                                            }
                                        }
                                    }
                                )
                            }

                            "settings" -> {
                                SettingsScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }

            // Contextual Scan Result Sheet
            if (activeScanResult != null) {
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ScanResultSheet(
                    item = activeScanResult!!,
                    sheetState = sheetState,
                    viewModel = viewModel,
                    onDismiss = { viewModel.setActiveScanResult(null) }
                )
            }
        }
    }
}

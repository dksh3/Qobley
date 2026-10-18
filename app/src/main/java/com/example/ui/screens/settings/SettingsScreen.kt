package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Widgets
import android.content.Intent
import android.net.Uri
import android.provider.Settings as AndroidSettings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.ViewWeek
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.core.content.ContextCompat
import com.example.ui.components.SupportedFormatsDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.MainViewModel
import com.example.ui.components.QobleyTopBar

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val vibrationEnabled by viewModel.settingsRepo.vibrationEnabled.collectAsState()
    val soundEnabled by viewModel.settingsRepo.soundEnabled.collectAsState()
    val duplicatePrevention by viewModel.settingsRepo.duplicatePrevention.collectAsState()
    val launchScreen by viewModel.settingsRepo.launchScreen.collectAsState()
    val themeMode by viewModel.settingsRepo.themeMode.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showLaunchDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showFormatsDialog by remember { mutableStateOf(false) }

    var cameraGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        cameraGranted = granted
    }

    var notificationGranted by remember {
        mutableStateOf(
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationGranted = granted
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        QobleyTopBar(title = "Settings")

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                // Scanning Section
                SettingsCategoryHeader("Scanning")

                SettingsSwitchItem(
                    icon = Icons.Outlined.Sensors,
                    title = "Vibrate on scan",
                    subtitle = "Tactile pulse when a code is detected",
                    checked = vibrationEnabled,
                    testTag = "setting_vibrate_switch",
                    onCheckedChange = { viewModel.settingsRepo.setVibrationEnabled(it) }
                )

                SettingsSwitchItem(
                    icon = Icons.Outlined.VolumeUp,
                    title = "Sound feedback",
                    subtitle = "Gentle tone on detection",
                    checked = soundEnabled,
                    testTag = "setting_sound_switch",
                    onCheckedChange = { viewModel.settingsRepo.setSoundEnabled(it) }
                )

                SettingsSwitchItem(
                    icon = Icons.Outlined.QrCodeScanner,
                    title = "Prevent duplicate scans",
                    subtitle = "Ignore repeated scans within 2 seconds",
                    checked = duplicatePrevention,
                    testTag = "setting_duplicate_switch",
                    onCheckedChange = { viewModel.settingsRepo.setDuplicatePrevention(it) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Engine Capabilities & Formats Section
                SettingsCategoryHeader("Engine Capabilities & Formats")

                SettingsClickableItem(
                    icon = Icons.Outlined.CheckCircle,
                    title = "13 Supported Formats (Active)",
                    subtitle = "QR Code, Aztec, Data Matrix, PDF-417, EAN-13, UPC-A, Code 128, Code 39, ITF, Codabar, UPC-E, EAN-8, Code 93",
                    testTag = "setting_formats_spec_item",
                    onClick = { showFormatsDialog = true }
                )

                SettingsInfoItem(
                    icon = Icons.Outlined.QrCodeScanner,
                    title = "Continuous 60 FPS Multi-Format Engine",
                    subtitle = "Feeds full-frame camera preview directly into 13 hardware-accelerated decoders with zero manual format switching."
                )

                SettingsInfoItem(
                    icon = Icons.Outlined.ViewWeek,
                    title = "Multi-Code & Batch Inventory",
                    subtitle = "Real-time detection of multiple barcodes in a single frame with interactive tap-to-select and bulk CSV/JSON export."
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Appearance Section
                SettingsCategoryHeader("Appearance")

                SettingsClickableItem(
                    icon = Icons.Outlined.Palette,
                    title = "Theme",
                    subtitle = when (themeMode) {
                        "light" -> "Light"
                        "dark" -> "Dark"
                        else -> "System default"
                    },
                    testTag = "setting_theme_item",
                    onClick = { showThemeDialog = true }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Quick Access Section
                SettingsCategoryHeader("Quick Access & Widgets")

                SettingsClickableItem(
                    icon = Icons.Outlined.Speed,
                    title = "Open on launch",
                    subtitle = when (launchScreen) {
                        "scanner" -> "Instant Scanner (Direct camera)"
                        "create" -> "Creator Studio"
                        "history" -> "Scan History"
                        "saved" -> "Saved Library"
                        else -> "Home screen (Default)"
                    },
                    testTag = "setting_launch_item",
                    onClick = { showLaunchDialog = true }
                )

                SettingsClickableItem(
                    icon = Icons.Outlined.Widgets,
                    title = "Home Screen Widgets (5 Native Widgets)",
                    subtitle = "1×1 Quick Scan, Quick Access, Command Center, Favorites, and Pinned QR Code",
                    testTag = "setting_widgets_info_item",
                    onClick = {
                        val ctx = context
                        com.example.widget.QobleyWidgetUpdater.updateAllWidgets(ctx)
                        viewModel.showToast("All home screen widgets refreshed")
                    }
                )

                SettingsClickableItem(
                    icon = Icons.Outlined.Tune,
                    title = "Configure Pinned Code Widget",
                    subtitle = "Pick which saved code or Wi-Fi QR appears directly on your home screen",
                    testTag = "setting_configure_widget_item",
                    onClick = {
                        val intent = android.content.Intent(context, com.example.widget.QobleyWidgetConfigActivity::class.java)
                        context.startActivity(intent)
                    }
                )

                SettingsClickableItem(
                    icon = Icons.Outlined.Star,
                    title = "Launcher Shortcuts (Dynamic)",
                    subtitle = "Sync your top starred codes into the app icon long-press menu",
                    testTag = "setting_sync_shortcuts_item",
                    onClick = {
                        com.example.util.QobleyShortcutManager.updateDynamicShortcuts(context)
                        viewModel.showToast("Launcher shortcuts updated with your favorites")
                    }
                )

                SettingsClickableItem(
                    icon = Icons.Outlined.ContentPaste,
                    title = "Create from Clipboard",
                    subtitle = "Classify clipboard content (URL, Wi-Fi, Contact, Text) and open Creator",
                    testTag = "setting_clipboard_action_item",
                    onClick = {
                        viewModel.postNavigationRequest(MainViewModel.NavigationRequest.CreateFromClipboard)
                    }
                )

                SettingsInfoItem(
                    icon = Icons.Outlined.Sensors,
                    title = "Android Quick Settings Tiles",
                    subtitle = "Swipe down twice from top of screen to add 'Qobley Scan' (instant camera) or 'Qobley Create' directly into Quick Settings."
                )

                SettingsInfoItem(
                    icon = Icons.Outlined.Share,
                    title = "Safe Android Share Target",
                    subtitle = "Share text, links, or contacts from any browser or app into Qobley. The creator opens prefilled for review without auto-saving."
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Privacy Section
                SettingsCategoryHeader("Privacy & Data")

                SettingsClickableItem(
                    icon = Icons.Outlined.DeleteSweep,
                    title = "Clear scan history",
                    subtitle = "Remove past scans from device history",
                    testTag = "setting_clear_history_item",
                    onClick = { showClearHistoryDialog = true }
                )

                SettingsInfoItem(
                    icon = Icons.Outlined.Lock,
                    title = "100% On-Device",
                    subtitle = "All camera processing, decoding, and saved codes remain entirely local to your device with zero cloud tracking or telemetry."
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Permissions & System Access Section
                SettingsCategoryHeader("Android System Permissions & Access")

                SettingsClickableItem(
                    icon = Icons.Default.CameraAlt,
                    title = "Camera Access",
                    subtitle = if (cameraGranted) "Granted · Instant 60 FPS real-time scanner active" else "Not granted · Tap to grant permission",
                    testTag = "setting_camera_permission_item",
                    onClick = {
                        if (!cameraGranted) {
                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        } else {
                            val intent = Intent(
                                AndroidSettings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null)
                            )
                            context.startActivity(intent)
                        }
                    }
                )

                SettingsInfoItem(
                    icon = Icons.Outlined.PhotoLibrary,
                    title = "Photo & Gallery Access",
                    subtitle = "Zero-permission Android Photo Picker active. Fully complies with Google Play private storage policies."
                )

                SettingsClickableItem(
                    icon = Icons.Outlined.Notifications,
                    title = "System Notifications",
                    subtitle = if (notificationGranted) "Granted · Batch inventory & export alerts active" else "Tap to grant notification permission",
                    testTag = "setting_notifications_permission_item",
                    onClick = {
                        if (!notificationGranted && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            val intent = Intent(
                                AndroidSettings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null)
                            )
                            context.startActivity(intent)
                        }
                    }
                )

                SettingsClickableItem(
                    icon = Icons.Outlined.Security,
                    title = "Manage in Android Settings",
                    subtitle = "Open system App Info to review all Android OS permissions, battery optimization, and app defaults",
                    testTag = "setting_open_system_settings_item",
                    onClick = {
                        val intent = Intent(
                            AndroidSettings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        )
                        context.startActivity(intent)
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // About Section
                SettingsCategoryHeader("About")

                SettingsInfoItem(
                    icon = Icons.Outlined.Info,
                    title = "Qobley 1.0",
                    subtitle = "Every code. One place. Universal QR, barcode, and document utility."
                )

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }

    // Theme selector dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Choose Theme") },
            text = {
                Column {
                    listOf("system" to "System default", "light" to "Light", "dark" to "Dark").forEach { (key, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.settingsRepo.setThemeMode(key)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (themeMode == key) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            if (themeMode == key) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Launch behavior dialog
    if (showLaunchDialog) {
        AlertDialog(
            onDismissRequest = { showLaunchDialog = false },
            title = { Text("Open on Launch") },
            text = {
                Column {
                    listOf(
                        "home" to "Home screen",
                        "scanner" to "Scanner",
                        "create" to "Creator Studio",
                        "history" to "Scan History",
                        "saved" to "Saved Library"
                    ).forEach { (key, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.settingsRepo.setLaunchScreen(key)
                                    showLaunchDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (launchScreen == key) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            if (launchScreen == key) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLaunchDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear history confirmation dialog
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear History") },
            text = { Text("Are you sure you want to delete all scan history? Your saved library items will not be affected.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearHistory()
                        showClearHistoryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showFormatsDialog) {
        SupportedFormatsDialog(onDismiss = { showFormatsDialog = false })
    }
}

@Composable
private fun SettingsCategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
}

@Composable
private fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
}

@Composable
private fun SettingsInfoItem(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp).padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
}

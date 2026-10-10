package com.example.ui.screens.history

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.DriveFileMove
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.ViewWeek
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.MainViewModel
import com.example.data.model.CodeItem
import com.example.data.model.CodeType
import com.example.ui.components.QobleyEmptyState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    onViewDetail: (CodeItem) -> Unit,
    onNavigateScan: () -> Unit = {},
    onShowSnackbarWithUndo: (String, () -> Unit) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val allCodes by viewModel.allCodes.collectAsState()
    val selectedIds by viewModel.selectedItemIds.collectAsState()

    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedSourceFilter by remember { mutableStateOf("ALL") } // ALL, SCANNED, CREATED
    var selectedTypeFilter by remember { mutableStateOf("ALL") } // ALL, URL, BARCODE, QR, PAYMENT, WIFI, CONTACT, TEXT, OTHER
    var sortOrder by remember { mutableStateOf(MainViewModel.SortOrder.NEWEST) }

    var showClearConfirm by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showCategoryMoveDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    val searchFocusRequester = remember { FocusRequester() }
    val isSelectionMode = selectedIds.isNotEmpty()

    BackHandler(enabled = isSearchActive || isSelectionMode) {
        if (isSelectionMode) {
            viewModel.clearSelection()
        } else if (isSearchActive) {
            isSearchActive = false
            searchQuery = ""
        }
    }

    // Filter items
    val filteredCodes by remember {
        derivedStateOf {
            allCodes.filter { item ->
                val matchesSource = when (selectedSourceFilter) {
                    "SCANNED" -> item.source.contains("SCAN") || item.source == "IMPORTED"
                    "CREATED" -> item.source == "GENERATED"
                    else -> true
                }

                val matchesType = when (selectedTypeFilter) {
                    "ALL" -> true
                    "URL" -> item.codeType == CodeType.URL
                    "BARCODE" -> item.codeType == CodeType.BARCODE
                    "QR" -> item.type == "QR"
                    "PAYMENT" -> item.codeType == CodeType.PAYMENT
                    "WIFI" -> item.codeType == CodeType.WIFI
                    "CONTACT" -> item.codeType == CodeType.CONTACT || item.codeType == CodeType.PHONE
                    "TEXT" -> item.codeType == CodeType.TEXT
                    else -> true
                }

                val matchesQuery = if (searchQuery.isBlank()) true else {
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.content.contains(searchQuery, ignoreCase = true) ||
                    item.format.contains(searchQuery, ignoreCase = true) ||
                    item.category.contains(searchQuery, ignoreCase = true) ||
                    item.codeType.label.contains(searchQuery, ignoreCase = true) ||
                    (!item.notes.isNullOrBlank() && item.notes.contains(searchQuery, ignoreCase = true))
                }

                matchesSource && matchesType && matchesQuery
            }
        }
    }

    // Sort items
    val sortedCodes by remember {
        derivedStateOf {
            when (sortOrder) {
                MainViewModel.SortOrder.NEWEST -> filteredCodes.sortedByDescending { it.createdAt }
                MainViewModel.SortOrder.OLDEST -> filteredCodes.sortedBy { it.createdAt }
                MainViewModel.SortOrder.RECENTLY_USED -> filteredCodes.sortedByDescending { it.updatedAt }
                MainViewModel.SortOrder.ALPHABETICAL -> filteredCodes.sortedBy { it.title.lowercase(Locale.getDefault()) }
                MainViewModel.SortOrder.TYPE -> filteredCodes.sortedWith(compareBy({ it.codeType.name }, { -it.createdAt }))
            }
        }
    }

    // Group items by natural date buckets
    val groupedCodes by remember {
        derivedStateOf {
            if (sortOrder != MainViewModel.SortOrder.NEWEST && sortOrder != MainViewModel.SortOrder.RECENTLY_USED) {
                // If sorted alphabetically or by type, display without date section headers
                mapOf("" to sortedCodes)
            } else {
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val todayStart = cal.timeInMillis
                val yesterdayStart = todayStart - (24 * 60 * 60 * 1000)
                val thisWeekStart = todayStart - (6 * 24 * 60 * 1000 * 60)

                sortedCodes.groupBy { item ->
                    when {
                        item.createdAt >= todayStart -> "Today"
                        item.createdAt >= yesterdayStart -> "Yesterday"
                        item.createdAt >= thisWeekStart -> "This week"
                        else -> "Earlier"
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
        // App Bar (Normal, Search, or Selection Mode)
        if (isSelectionMode) {
            TopAppBar(
                title = {
                    Text(
                        text = "${selectedIds.size} selected",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.clearSelection() }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close selection")
                    }
                },
                actions = {
                    // Select All / Deselect All
                    val isAllSelected = selectedIds.size == sortedCodes.size && sortedCodes.isNotEmpty()
                    IconButton(
                        onClick = {
                            if (isAllSelected) viewModel.clearSelection() else viewModel.selectAll(sortedCodes.map { it.id })
                        }
                    ) {
                        Icon(
                            imageVector = if (isAllSelected) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                            contentDescription = if (isAllSelected) "Deselect All" else "Select All"
                        )
                    }

                    // Batch Favorite
                    IconButton(onClick = { viewModel.favoriteSelected(true) }) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Star selected",
                            tint = Color(0xFFEF4444)
                        )
                    }

                    // Batch Move Category
                    IconButton(onClick = { showCategoryMoveDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.DriveFileMove,
                            contentDescription = "Move category"
                        )
                    }

                    // Batch Share / Export
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Export selected"
                        )
                    }

                    // Batch Delete
                    IconButton(
                        onClick = {
                            viewModel.deleteSelected { count ->
                                onShowSnackbarWithUndo("Deleted $count items") {
                                    viewModel.undoDelete()
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete selected",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            )
        } else if (isSearchActive) {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search history...") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(searchFocusRequester)
                            .testTag("history_search_input")
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            isSearchActive = false
                            searchQuery = ""
                        }
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close Search")
                    }
                },
                actions = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
            LaunchedEffect(Unit) {
                searchFocusRequester.requestFocus()
            }
        } else {
            TopAppBar(
                title = {
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    // Search toggle
                    IconButton(
                        onClick = { isSearchActive = true },
                        modifier = Modifier.testTag("history_search_toggle")
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                    }

                    // Sort button
                    IconButton(
                        onClick = { showSortDialog = true },
                        modifier = Modifier.testTag("history_sort_button")
                    ) {
                        Icon(imageVector = Icons.Outlined.Sort, contentDescription = "Sort")
                    }

                    // More Menu
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Select items") },
                                onClick = {
                                    showMoreMenu = false
                                    if (sortedCodes.isNotEmpty()) {
                                        viewModel.toggleSelectItem(sortedCodes.first().id)
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export all history") },
                                onClick = {
                                    showMoreMenu = false
                                    showExportDialog = true
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Clear scanned history", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMoreMenu = false
                                    showClearConfirm = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }

        // Horizontal Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Source Filters
            FilterChip(
                selected = selectedSourceFilter == "ALL",
                onClick = { selectedSourceFilter = "ALL" },
                label = { Text("All (${allCodes.size})") }
            )
            FilterChip(
                selected = selectedSourceFilter == "SCANNED",
                onClick = { selectedSourceFilter = "SCANNED" },
                label = { Text("Scanned") }
            )
            FilterChip(
                selected = selectedSourceFilter == "CREATED",
                onClick = { selectedSourceFilter = "CREATED" },
                label = { Text("Created") }
            )

            // Divider chip spacer
            Text(
                text = "•",
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.CenterVertically).padding(horizontal = 2.dp)
            )

            // Type Filters
            listOf(
                "ALL" to "All Types",
                "URL" to "Websites",
                "BARCODE" to "Barcodes",
                "PAYMENT" to "Payments",
                "WIFI" to "Wi-Fi",
                "CONTACT" to "Contacts",
                "TEXT" to "Text"
            ).forEach { (key, label) ->
                if (key != "ALL") {
                    FilterChip(
                        selected = selectedTypeFilter == key,
                        onClick = {
                            selectedTypeFilter = if (selectedTypeFilter == key) "ALL" else key
                        },
                        label = { Text(label) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Content List or Empty State
        if (sortedCodes.isEmpty()) {
            QobleyEmptyState(
                icon = Icons.Outlined.History,
                title = if (searchQuery.isNotEmpty()) "No matching codes" else "No scan history",
                description = if (searchQuery.isNotEmpty())
                    "No items found matching \"$searchQuery\". Try a different keyword."
                else
                    "Point the camera at any QR or barcode to get started.",
                actionButton = {
                    Button(
                        onClick = onNavigateScan,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scan a code")
                    }
                }
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 640.dp)
                        .padding(horizontal = 20.dp)
                ) {
                    groupedCodes.forEach { (header, items) ->
                        if (header.isNotEmpty()) {
                            item {
                                Text(
                                    text = header,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                )
                            }
                        }

                        items(items, key = { it.id }) { item ->
                            val isSelected = selectedIds.contains(item.id)

                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissVal ->
                                    when (dismissVal) {
                                        SwipeToDismissBoxValue.EndToStart -> {
                                            viewModel.deleteWithUndo(item) { deleted ->
                                                onShowSnackbarWithUndo("Deleted \"${deleted.title}\"") {
                                                    viewModel.undoDelete()
                                                }
                                            }
                                            true
                                        }
                                        SwipeToDismissBoxValue.StartToEnd -> {
                                            viewModel.toggleFavorite(item)
                                            false
                                        }
                                        else -> false
                                    }
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    val isSwipingDelete = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart
                                    val isSwipingFav = dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd
                                    val bgColor = when {
                                        isSwipingDelete -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                        isSwipingFav -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                        else -> Color.Transparent
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(bgColor, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 16.dp),
                                        contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
                                    ) {
                                        if (isSwipingFav) {
                                            Icon(
                                                imageVector = Icons.Default.Favorite,
                                                contentDescription = "Favorite",
                                                tint = Color(0xFFEF4444)
                                            )
                                        } else if (isSwipingDelete) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            ) {
                                ModernHistoryItemRow(
                                    item = item,
                                    isSelected = isSelected,
                                    isSelectionMode = isSelectionMode,
                                    onClick = {
                                        if (isSelectionMode) {
                                            viewModel.toggleSelectItem(item.id)
                                        } else {
                                            // Tapping opens the existing result bottom sheet
                                            viewModel.setActiveScanResult(item)
                                        }
                                    },
                                    onLongClick = {
                                        viewModel.toggleSelectItem(item.id)
                                    },
                                    onToggleFavorite = { viewModel.toggleFavorite(item) },
                                    onCopy = {
                                        clipboardManager.setText(AnnotatedString(item.content))
                                        viewModel.showToast("Copied to clipboard")
                                    }
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(48.dp))
                    }
                }
            }
        }
    }

    // Sort Selection Dialog
    if (showSortDialog) {
        AlertDialog(
            onDismissRequest = { showSortDialog = false },
            title = { Text("Sort History") },
            text = {
                Column {
                    MainViewModel.SortOrder.values().forEach { order ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    sortOrder = order
                                    showSortDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = sortOrder == order,
                                onClick = {
                                    sortOrder = order
                                    showSortDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = order.label,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSortDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Category Move Dialog for Batch Selection
    if (showCategoryMoveDialog) {
        val categories = listOf("Personal", "Work", "Payments", "Wi-Fi", "Contacts", "Products", "Travel", "Other")
        AlertDialog(
            onDismissRequest = { showCategoryMoveDialog = false },
            title = { Text("Move to Category") },
            text = {
                Column {
                    categories.forEach { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.moveSelectedToCategory(cat)
                                    showCategoryMoveDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Outlined.DriveFileMove, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(cat, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryMoveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Export Dialog
    if (showExportDialog) {
        val itemsToExport = if (selectedIds.isNotEmpty()) {
            allCodes.filter { selectedIds.contains(it.id) }
        } else {
            allCodes
        }
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Codes (${itemsToExport.size})") },
            text = {
                Column {
                    Text("Choose export format:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = {
                            showExportDialog = false
                            viewModel.exportItems(context, itemsToExport, "CSV")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Export as CSV (.csv)")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            showExportDialog = false
                            viewModel.exportItems(context, itemsToExport, "JSON")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Export as JSON (.json)")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            showExportDialog = false
                            viewModel.exportItems(context, itemsToExport, "TEXT")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Share as Formatted Text")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear History Confirmation
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear Scanned History") },
            text = { Text("Remove all scanned history? Created and starred items in your saved library will remain intact.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearHistory()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ModernHistoryItemRow(
    item: CodeItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else Color.Transparent
            )
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onClick() },
                modifier = Modifier.padding(end = 8.dp)
            )
        } else {
            // Type Icon with subtle circular background
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getIconForType(item.codeType, item.type),
                    contentDescription = item.displayTypeLabel,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        // Title, Preview, and Timestamp
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.displayTypeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = " • ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
                Text(
                    text = formatNaturalTime(item.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = item.preview,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }

        if (!isSelectionMode) {
            Spacer(modifier = Modifier.width(6.dp))

            IconButton(onClick = onToggleFavorite, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (item.isFavorite) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(onClick = onCopy, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = "Copy",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun getIconForType(codeType: CodeType, rawType: String): ImageVector {
    return when (codeType) {
        CodeType.URL -> Icons.Outlined.Language
        CodeType.PHONE -> Icons.Outlined.Phone
        CodeType.WIFI -> Icons.Outlined.Wifi
        CodeType.CONTACT -> Icons.Outlined.Person
        CodeType.BARCODE -> Icons.Outlined.ViewWeek
        CodeType.TEXT -> Icons.Outlined.TextFields
        else -> if (rawType == "QR") Icons.Outlined.QrCode else Icons.Outlined.ViewWeek
    }
}

private fun formatNaturalTime(timestamp: Long): String {
    val sdfTime = SimpleDateFormat("h:mm a", Locale.getDefault())
    val sdfDate = SimpleDateFormat("MMM d • h:mm a", Locale.getDefault())
    val now = Calendar.getInstance()
    val then = Calendar.getInstance().apply { timeInMillis = timestamp }

    return if (now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR) &&
        now.get(Calendar.YEAR) == then.get(Calendar.YEAR)
    ) {
        "Today • ${sdfTime.format(Date(timestamp))}"
    } else {
        sdfDate.format(Date(timestamp))
    }
}

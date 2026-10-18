package com.example.ui.screens.saved

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DriveFileMove
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.ViewWeek
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    viewModel: MainViewModel,
    onViewDetail: (CodeItem) -> Unit,
    onNavigateCreate: () -> Unit,
    onNavigateScan: () -> Unit = {},
    onShowSnackbarWithUndo: (String, () -> Unit) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val allCodes by viewModel.allCodes.collectAsState()
    val savedLibrary by viewModel.savedLibrary.collectAsState()
    val selectedIds by viewModel.selectedItemIds.collectAsState()

    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var favoritesOnly by remember { mutableStateOf(false) }

    var showCategoryMoveDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }

    val categories = listOf("All", "Favorites", "Personal", "Work", "Payments", "Wi-Fi", "Contacts", "Products", "Travel", "Other")
    val isSelectionMode = selectedIds.isNotEmpty()

    BackHandler(enabled = isSearchActive || isSelectionMode) {
        if (isSelectionMode) {
            viewModel.clearSelection()
        } else if (isSearchActive) {
            isSearchActive = false
            searchQuery = ""
        }
    }

    // Filter library items
    val libraryItems by remember {
        derivedStateOf {
            // Source items: savedLibrary items (plus explicitly favorite or user created items)
            val sourceItems = if (savedLibrary.isNotEmpty()) savedLibrary else allCodes.filter { it.isFavorite || it.isSaved || it.source == "GENERATED" }

            sourceItems.filter { item ->
                val matchesFav = if (favoritesOnly || selectedCategory == "Favorites") item.isFavorite else true
                val matchesCat = when (selectedCategory) {
                    "All", "Favorites" -> true
                    else -> item.category.equals(selectedCategory, ignoreCase = true)
                }
                val matchesQuery = if (searchQuery.isBlank()) true else {
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.content.contains(searchQuery, ignoreCase = true) ||
                    item.category.contains(searchQuery, ignoreCase = true) ||
                    item.format.contains(searchQuery, ignoreCase = true) ||
                    (!item.notes.isNullOrBlank() && item.notes.contains(searchQuery, ignoreCase = true))
                }
                matchesFav && matchesCat && matchesQuery
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar
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
                    val isAllSelected = selectedIds.size == libraryItems.size && libraryItems.isNotEmpty()
                    IconButton(
                        onClick = {
                            if (isAllSelected) viewModel.clearSelection() else viewModel.selectAll(libraryItems.map { it.id })
                        }
                    ) {
                        Icon(
                            imageVector = if (isAllSelected) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                            contentDescription = if (isAllSelected) "Deselect All" else "Select All"
                        )
                    }

                    // Batch Star
                    IconButton(onClick = { viewModel.favoriteSelected(true) }) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Star selected",
                            tint = Color(0xFFEF4444)
                        )
                    }

                    // Batch Category Move
                    IconButton(onClick = { showCategoryMoveDialog = true }) {
                        Icon(imageVector = Icons.Outlined.DriveFileMove, contentDescription = "Move category")
                    }

                    // Batch Share / Export
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(imageVector = Icons.Outlined.Share, contentDescription = "Export selected")
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
                        placeholder = { Text("Search saved library...") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(searchFocusRequester)
                            .testTag("saved_search_input")
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
                        text = "Library",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    // Search toggle
                    IconButton(
                        onClick = { isSearchActive = true },
                        modifier = Modifier.testTag("saved_search_toggle")
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                    }

                    // Favorites quick toggle
                    IconButton(
                        onClick = {
                            favoritesOnly = !favoritesOnly
                            if (favoritesOnly) selectedCategory = "Favorites" else if (selectedCategory == "Favorites") selectedCategory = "All"
                        },
                        modifier = Modifier.testTag("filter_favorites_toggle")
                    ) {
                        Icon(
                            imageVector = if (favoritesOnly || selectedCategory == "Favorites") Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorites Only",
                            tint = if (favoritesOnly || selectedCategory == "Favorites") Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                                    if (libraryItems.isNotEmpty()) {
                                        viewModel.toggleSelectItem(libraryItems.first().id)
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export library") },
                                onClick = {
                                    showMoreMenu = false
                                    showExportDialog = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }

        // Horizontal Category Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = selectedCategory == cat
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedCategory = cat
                        favoritesOnly = (cat == "Favorites")
                    },
                    label = { Text(cat) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Items list or Empty State
        if (libraryItems.isEmpty()) {
            QobleyEmptyState(
                icon = Icons.Outlined.BookmarkBorder,
                title = if (favoritesOnly || selectedCategory == "Favorites") "No favorites yet" else if (searchQuery.isNotEmpty()) "No matching codes" else "No saved codes in library",
                description = if (favoritesOnly || selectedCategory == "Favorites")
                    "Tap the star or bookmark icon on any code to save it here for fast access."
                else if (searchQuery.isNotEmpty())
                    "No items match \"$searchQuery\"."
                else
                    "Save frequently used codes, Wi-Fi credentials, or payment links for easy reuse anytime.",
                actionButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = onNavigateScan,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Scan", maxLines = 1, softWrap = false)
                        }
                        Button(
                            onClick = onNavigateCreate,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Create", maxLines = 1, softWrap = false)
                        }
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
                    items(libraryItems, key = { it.id }) { item ->
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
                                val isDeleting = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart
                                val isFav = dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd
                                val bgColor = when {
                                    isDeleting -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                    isFav -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                    else -> Color.Transparent
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(bgColor, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
                                ) {
                                    if (isFav) {
                                        Icon(imageVector = Icons.Default.Favorite, contentDescription = "Favorite", tint = Color(0xFFEF4444))
                                    } else if (isDeleting) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        ) {
                            SavedCardItem(
                                item = item,
                                isSelected = isSelected,
                                isSelectionMode = isSelectionMode,
                                onClick = {
                                    if (isSelectionMode) {
                                        viewModel.toggleSelectItem(item.id)
                                    } else {
                                        // Open smart result sheet
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
                                },
                                onShare = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, item.content)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share code"))
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    item {
                        Spacer(modifier = Modifier.height(48.dp))
                    }
                }
            }
        }
    }

    // Category Move Dialog for Batch Selection
    if (showCategoryMoveDialog) {
        val catList = listOf("Personal", "Work", "Payments", "Wi-Fi", "Contacts", "Products", "Travel", "Other")
        AlertDialog(
            onDismissRequest = { showCategoryMoveDialog = false },
            title = { Text("Move to Category") },
            text = {
                Column {
                    catList.forEach { cat ->
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
            libraryItems.filter { selectedIds.contains(it.id) }
        } else {
            libraryItems
        }
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Library (${itemsToExport.size})") },
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
                        Text("Export CSV", maxLines = 1, softWrap = false)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            showExportDialog = false
                            viewModel.exportItems(context, itemsToExport, "JSON")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Export JSON", maxLines = 1, softWrap = false)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            showExportDialog = false
                            viewModel.exportItems(context, itemsToExport, "TEXT")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Share as Text", maxLines = 1, softWrap = false)
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
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SavedCardItem(
    item: CodeItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Type & Category + Favorite button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSelectionMode) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onClick() },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }

                    // Category Pill
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = item.category,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = item.displayTypeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (item.isFavorite) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Preview
            Text(
                text = item.preview,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            // Notes preview badge if attached
            if (!item.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notes,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.notes,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Row: Format / Date and Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.format.replace("_", " ")} • ${formatCardDate(item.createdAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontFamily = FontFamily.Monospace
                )

                Row {
                    IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatCardDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

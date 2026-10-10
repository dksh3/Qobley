package com.example.ui.screens.result

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.MainViewModel
import com.example.data.model.CodeItem
import com.example.ui.components.ContextActionButtons
import com.example.ui.components.QobleyCodePreview
import com.example.ui.components.QobleyTopBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CodeDetailScreen(
    codeId: Long,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var codeItem by remember { mutableStateOf<CodeItem?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(codeId) {
        codeItem = viewModel.repository.getById(codeId)
    }

    val item = codeItem

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        QobleyTopBar(
            title = item?.title ?: "Code Details",
            onBack = onBack,
            actions = {
                if (item != null) {
                    IconButton(
                        onClick = {
                            viewModel.toggleFavorite(item)
                            codeItem = item.copy(isFavorite = !item.isFavorite)
                        },
                        modifier = Modifier.testTag("detail_toggle_favorite")
                    ) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (item.isFavorite) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.testTag("detail_delete_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        )

        if (item != null) {
            var editableTitle by remember(item.id) { mutableStateOf(item.title) }
            var selectedCategory by remember(item.id) { mutableStateOf(item.category) }
            var editableNotes by remember(item.id) { mutableStateOf(item.notes ?: "") }
            val categories = listOf("Personal", "Work", "Payments", "Wi-Fi", "Contacts", "Products", "Travel", "Other")

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 640.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    // Code Preview
                    QobleyCodePreview(
                        content = item.content,
                        format = item.format,
                        showActionRow = false,
                        onToast = { viewModel.showToast(it) }
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Contextual Actions
                    ContextActionButtons(
                        rawContent = item.content,
                        format = item.format,
                        onToast = { viewModel.showToast(it) }
                    )

                Spacer(modifier = Modifier.height(24.dp))

                // Editable Title
                OutlinedTextField(
                    value = editableTitle,
                    onValueChange = {
                        editableTitle = it
                        viewModel.updateCode(item, it, selectedCategory, editableNotes.ifBlank { null })
                    },
                    label = { Text("Title / Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("detail_title_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Notes
                OutlinedTextField(
                    value = editableNotes,
                    onValueChange = {
                        editableNotes = it
                        viewModel.updateCode(item, editableTitle, selectedCategory, it.ifBlank { null })
                    },
                    label = { Text("Custom Notes / Description") },
                    placeholder = { Text("Add personal notes, passwords, context...") },
                    maxLines = 4,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("detail_notes_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Categories
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = {
                                selectedCategory = cat
                                viewModel.updateCode(item, editableTitle, cat, editableNotes.ifBlank { null })
                            },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Technical Metadata Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Information",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        MetadataRow("Format", item.format.replace("_", " "))
                        MetadataRow("Source", item.source.lowercase().replaceFirstChar { it.uppercase() })
                        MetadataRow("Created", formatDateTime(item.createdAt))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Code not found", style = MaterialTheme.typography.titleMedium)
            }
        }
    }

    if (showDeleteConfirm && item != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Code") },
            text = { Text("Are you sure you want to delete \"${item.title}\"? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCode(item)
                        showDeleteConfirm = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, fontFamily = FontFamily.Monospace)
    }
}

private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

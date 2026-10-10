package com.example.ui.screens.create

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.MainViewModel
import com.example.generator.BarcodeGenerator
import com.example.generator.BarcodeValidator
import com.example.ui.components.QobleyCodePreview
import com.example.ui.components.QobleyTopBar
import com.example.util.QobleyPrintHelper

@Composable
fun BarcodeCreatorScreen(
    initialFormat: String = "CODE_128",
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedFormat by remember { mutableStateOf(initialFormat) }
    var includeHumanReadableLabel by remember { mutableStateOf(true) }
    var customLabel by remember { mutableStateOf("") }

    var rawInput by remember {
        mutableStateOf(
            when (initialFormat) {
                "EAN_13" -> "890123456789" // 12 digits, 13th calculated
                "EAN_8" -> "8901234"      // 7 digits, 8th calculated
                "UPC_A" -> "01234567890"  // 11 digits, 12th calculated
                else -> "QOBLEY-102938"
            }
        )
    }

    val formats = listOf(
        "CODE_128", "EAN_13", "UPC_A", "EAN_8", "CODE_39", "CODE_93", "ITF", "CODABAR", "DATA_MATRIX", "PDF_417", "AZTEC"
    )

    val validationResult by remember(selectedFormat, rawInput) {
        derivedStateOf {
            BarcodeValidator.validate(selectedFormat, rawInput)
        }
    }

    val finalContent by remember(validationResult, rawInput) {
        derivedStateOf {
            when (val res = validationResult) {
                is BarcodeValidator.ValidationResult.ValidWithAutofill -> res.correctedValue
                is BarcodeValidator.ValidationResult.Valid -> rawInput.trim()
                else -> ""
            }
        }
    }

    val is1D = selectedFormat != "DATA_MATRIX" && selectedFormat != "PDF_417" && selectedFormat != "AZTEC"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        QobleyTopBar(
            title = "Barcode Studio",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Format Selector Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                formats.forEach { fmt ->
                    FilterChip(
                        selected = selectedFormat == fmt,
                        onClick = {
                            selectedFormat = fmt
                            if (fmt == "EAN_13" && !rawInput.all { it.isDigit() }) rawInput = "890123456789"
                            if (fmt == "EAN_8" && !rawInput.all { it.isDigit() }) rawInput = "8901234"
                            if (fmt == "UPC_A" && !rawInput.all { it.isDigit() }) rawInput = "01234567890"
                        },
                        label = { Text(fmt.replace("_", " ")) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Live Barcode Preview Card
            if (finalContent.isNotEmpty()) {
                QobleyCodePreview(
                    content = finalContent,
                    format = selectedFormat,
                    displayLabel = customLabel.ifEmpty { null },
                    qrConfig = BarcodeGenerator.QrConfig(
                        includeLabel = is1D && includeHumanReadableLabel
                    ),
                    showActionRow = true,
                    onToast = { viewModel.showToast(it) }
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Input TextField
            OutlinedTextField(
                value = rawInput,
                onValueChange = { rawInput = it },
                label = { Text("${selectedFormat.replace("_", " ")} Data / Payload") },
                trailingIcon = {
                    IconButton(onClick = {
                        val clip = clipboardManager.getText()?.text
                        if (!clip.isNullOrBlank()) {
                            rawInput = clip
                            viewModel.showToast("Pasted from clipboard")
                        }
                    }) {
                        Icon(Icons.Outlined.ContentPaste, contentDescription = "Paste", modifier = Modifier.size(20.dp))
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().testTag("barcode_input_text")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Validation & Checksum status banner
            when (val res = validationResult) {
                is BarcodeValidator.ValidationResult.ValidWithAutofill -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Outlined.Check, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${res.note} → Final encoded: ${res.correctedValue}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                is BarcodeValidator.ValidationResult.Invalid -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Outlined.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = res.reason,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                else -> {}
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Label text field for product description or SKU
            OutlinedTextField(
                value = customLabel,
                onValueChange = { customLabel = it },
                label = { Text("Product / SKU Name (optional)") },
                placeholder = { Text("e.g. Organic Coffee Blend 250g") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (is1D) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Human-Readable Numbers", style = MaterialTheme.typography.bodyMedium)
                        Text("Show numbers/text below barcode bars", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = includeHumanReadableLabel,
                        onCheckedChange = { includeHumanReadableLabel = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save to Library Button
            Button(
                onClick = {
                    if (finalContent.isNotEmpty()) {
                        val title = if (customLabel.isNotBlank()) customLabel else "${selectedFormat.replace("_", " ")}: $finalContent"
                        viewModel.saveGeneratedCode(
                            type = "BARCODE",
                            format = selectedFormat,
                            content = finalContent,
                            title = title,
                            category = "Products"
                        )
                        viewModel.showToast("Saved to Library")
                        onBack()
                    }
                },
                enabled = finalContent.isNotEmpty(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.fillMaxWidth().testTag("save_generated_barcode_button")
            ) {
                Icon(imageVector = Icons.Outlined.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save to Library", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary Print & Share Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = {
                        val bitmap = BarcodeGenerator.generate(
                            formatString = selectedFormat,
                            content = finalContent,
                            width = 1200,
                            height = 500,
                            qrConfig = BarcodeGenerator.QrConfig(includeLabel = is1D && includeHumanReadableLabel)
                        )
                        if (bitmap != null) {
                            val title = if (customLabel.isNotBlank()) customLabel else selectedFormat.replace("_", " ")
                            QobleyPrintHelper.printCode(
                                context = context,
                                title = title,
                                content = finalContent,
                                bitmap = bitmap,
                                onFinished = { viewModel.showToast("Printing job sent") }
                            )
                        } else {
                            viewModel.showToast("Cannot print invalid barcode")
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Outlined.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Print Barcode")
                }

                OutlinedButton(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, finalContent)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share barcode"))
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share")
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

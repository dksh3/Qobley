package com.example.ui.screens.create

import android.content.Intent
import android.graphics.Color as AndroidColor
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.ViewWeek
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Data & Format, 1 = Design Studio

    var customDisplayTitle by remember { mutableStateOf("") }
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

    // Customization states
    var fgColorInt by remember { mutableIntStateOf(AndroidColor.BLACK) }
    var bgColorInt by remember { mutableIntStateOf(AndroidColor.WHITE) }
    var isTransparentBg by remember { mutableStateOf(false) }
    var heightMultiplier by remember { mutableStateOf(1.0f) }
    var labelPosition by remember { mutableStateOf(BarcodeGenerator.BarcodeLabelPosition.BOTTOM) }
    var customLabelText by remember { mutableStateOf("") }

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

    val currentQrConfig = remember(fgColorInt, bgColorInt, isTransparentBg, heightMultiplier, labelPosition, customLabelText) {
        BarcodeGenerator.QrConfig(
            foregroundColor = fgColorInt,
            backgroundColor = bgColorInt,
            isTransparentBackground = isTransparentBg,
            barcodeHeightMultiplier = heightMultiplier,
            barcodeLabelPosition = labelPosition,
            customBarcodeLabel = customLabelText.ifBlank { null }
        )
    }

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
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Barcode Preview
            if (finalContent.isNotEmpty()) {
                QobleyCodePreview(
                    content = finalContent,
                    format = selectedFormat,
                    displayLabel = customDisplayTitle.ifBlank { null },
                    qrConfig = currentQrConfig,
                    onToast = { viewModel.showToast(it) }
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Enter valid data to generate live barcode preview",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Studio Mode Tabs: Data & Format vs Design Studio
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.ViewWeek, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Data & Format", maxLines = 1, softWrap = false)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Palette, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Design & Studio", maxLines = 1, softWrap = false)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                // TAB 0: DATA & FORMAT
                Text(
                    text = "Select Barcode Standard",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

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

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = rawInput,
                    onValueChange = { rawInput = it },
                    label = { Text("Barcode Data / Content") },
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
                    modifier = Modifier.fillMaxWidth().testTag("barcode_input_content")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Validation Feedback Alert
                when (val res = validationResult) {
                    is BarcodeValidator.ValidationResult.ValidWithAutofill -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF059669).copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.Check, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = res.note,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF059669),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    is BarcodeValidator.ValidationResult.Invalid -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = res.reason,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    is BarcodeValidator.ValidationResult.Valid -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF059669).copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.Check, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Valid format for $selectedFormat",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF059669)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = customDisplayTitle,
                    onValueChange = { customDisplayTitle = it },
                    label = { Text("Display Title / Item Name (optional)") },
                    placeholder = { Text("e.g. Organic Almond Milk - $4.99") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quick test generator presets
                Text(
                    text = "Quick Data Presets",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Retail SKU" to { selectedFormat = "EAN_13"; rawInput = "890123456789"; customDisplayTitle = "Retail SKU Item" },
                        "Logistics Box" to { selectedFormat = "CODE_128"; rawInput = "PKG-8920-US"; customDisplayTitle = "Express Freight" },
                        "Carton ITF" to { selectedFormat = "ITF"; rawInput = "10012345678902"; customDisplayTitle = "Bulk Carton" },
                        "Medical Device" to { selectedFormat = "DATA_MATRIX"; rawInput = "01089012345678901726051510LOT42"; customDisplayTitle = "Sterile Implant" },
                        "US Product" to { selectedFormat = "UPC_A"; rawInput = "01234567890"; customDisplayTitle = "Beverage Can" }
                    ).forEach { (label, action) ->
                        OutlinedButton(
                            onClick = { action() },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(label, maxLines = 1, softWrap = false, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

            } else {
                // TAB 1: DESIGN & STUDIO
                Text(
                    text = "One-Tap Style Presets",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Classic Retail" to {
                            fgColorInt = AndroidColor.BLACK
                            bgColorInt = AndroidColor.WHITE
                            isTransparentBg = false
                            heightMultiplier = 1.0f
                            labelPosition = BarcodeGenerator.BarcodeLabelPosition.BOTTOM
                        },
                        "Warehouse Shelf" to {
                            fgColorInt = AndroidColor.rgb(15, 23, 42)
                            bgColorInt = AndroidColor.WHITE
                            isTransparentBg = false
                            heightMultiplier = 1.4f
                            labelPosition = BarcodeGenerator.BarcodeLabelPosition.TOP
                        },
                        "Package Logistics" to {
                            fgColorInt = AndroidColor.rgb(19, 78, 74)
                            bgColorInt = AndroidColor.rgb(240, 253, 250)
                            isTransparentBg = false
                            heightMultiplier = 1.0f
                            labelPosition = BarcodeGenerator.BarcodeLabelPosition.BOTTOM
                        },
                        "Sticker Transparent" to {
                            fgColorInt = AndroidColor.BLACK
                            bgColorInt = AndroidColor.TRANSPARENT
                            isTransparentBg = true
                            heightMultiplier = 1.0f
                            labelPosition = BarcodeGenerator.BarcodeLabelPosition.BOTTOM
                        },
                        "Crimson Tag" to {
                            fgColorInt = AndroidColor.rgb(136, 19, 55)
                            bgColorInt = AndroidColor.rgb(255, 241, 242)
                            isTransparentBg = false
                            heightMultiplier = 1.2f
                            labelPosition = BarcodeGenerator.BarcodeLabelPosition.BOTTOM
                        }
                    ).forEach { (name, applyPreset) ->
                        OutlinedButton(
                            onClick = { applyPreset() },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(name, maxLines = 1, softWrap = false, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Barcode Line Color
                Text(
                    text = "Barcode Line Color",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val colors = listOf(
                        "Black" to AndroidColor.BLACK,
                        "Navy" to AndroidColor.rgb(30, 41, 59),
                        "Indigo" to AndroidColor.rgb(30, 27, 75),
                        "Forest" to AndroidColor.rgb(6, 78, 59),
                        "Burgundy" to AndroidColor.rgb(74, 4, 78),
                        "Espresso" to AndroidColor.rgb(39, 28, 25),
                        "Dark Teal" to AndroidColor.rgb(19, 78, 74),
                        "Rust" to AndroidColor.rgb(124, 45, 18)
                    )
                    colors.forEach { (name, c) ->
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(Color(c), CircleShape)
                                .border(
                                    width = if (fgColorInt == c) 3.dp else 1.dp,
                                    color = if (fgColorInt == c) MaterialTheme.colorScheme.primary else Color.LightGray,
                                    shape = CircleShape
                                )
                                .clickable { fgColorInt = c }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Canvas Background
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Background Tone", style = MaterialTheme.typography.labelMedium)
                    FilterChip(
                        selected = isTransparentBg,
                        onClick = { isTransparentBg = !isTransparentBg },
                        label = { Text("Transparent", maxLines = 1, softWrap = false) }
                    )
                }

                if (!isTransparentBg) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "Pure White" to AndroidColor.WHITE,
                            "Warm Cream" to AndroidColor.rgb(254, 251, 246),
                            "Light Slate" to AndroidColor.rgb(248, 250, 252),
                            "Soft Mint" to AndroidColor.rgb(240, 253, 244)
                        ).forEach { (name, c) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(c),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (bgColorInt == c) 2.dp else 1.dp,
                                    color = if (bgColorInt == c) MaterialTheme.colorScheme.primary else Color.LightGray
                                ),
                                modifier = Modifier.clickable { bgColorInt = c }
                            ) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Barcode Height / Density
                if (is1D) {
                    Text(
                        text = "Barcode Height",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "Compact" to 0.65f,
                            "Standard" to 1.0f,
                            "Tall" to 1.35f,
                            "Poster" to 1.75f
                        ).forEach { (label, mult) ->
                            FilterChip(
                                selected = heightMultiplier == mult,
                                onClick = { heightMultiplier = mult },
                                label = { Text(label, maxLines = 1, softWrap = false) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Human-Readable Label Position
                    Text(
                        text = "Human-Readable Code Number",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "Below Barcode" to BarcodeGenerator.BarcodeLabelPosition.BOTTOM,
                            "Above Barcode" to BarcodeGenerator.BarcodeLabelPosition.TOP,
                            "Hidden" to BarcodeGenerator.BarcodeLabelPosition.NONE
                        ).forEach { (label, pos) ->
                            FilterChip(
                                selected = labelPosition == pos,
                                onClick = { labelPosition = pos },
                                label = { Text(label, maxLines = 1, softWrap = false) }
                            )
                        }
                    }

                    if (labelPosition != BarcodeGenerator.BarcodeLabelPosition.NONE) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = customLabelText,
                            onValueChange = { customLabelText = it },
                            label = { Text("Custom Number Label Override (optional)") },
                            placeholder = { Text("Leave blank to show actual barcode data") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Save Action
            Button(
                onClick = {
                    if (finalContent.isNotEmpty()) {
                        viewModel.saveGeneratedCode(
                            type = "BARCODE",
                            format = selectedFormat,
                            content = finalContent,
                            title = customDisplayTitle.ifBlank { "Barcode: $finalContent" },
                            category = "RETAIL"
                        )
                        viewModel.showToast("Saved to Library")
                        onBack()
                    } else {
                        viewModel.showToast("Cannot save empty or invalid code")
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
                Text("Save to Library", style = MaterialTheme.typography.titleMedium, maxLines = 1, softWrap = false)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary Actions (Print & Share)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = {
                        val bitmap = BarcodeGenerator.generate(
                            formatString = selectedFormat,
                            content = finalContent,
                            width = 1200,
                            height = 600,
                            qrConfig = currentQrConfig
                        )
                        if (bitmap != null) {
                            QobleyPrintHelper.printCode(
                                context = context,
                                title = customDisplayTitle.ifBlank { "Barcode $finalContent" },
                                content = finalContent,
                                bitmap = bitmap,
                                onFinished = { viewModel.showToast("Printing job sent") }
                            )
                        } else {
                            viewModel.showToast("Cannot print empty code")
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Outlined.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Print", maxLines = 1, softWrap = false)
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
                    Text("Share", maxLines = 1, softWrap = false)
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

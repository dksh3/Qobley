package com.example.ui.screens.create

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.ViewWeek
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.QobleySearchBar
import com.example.ui.components.QobleyTopBar

@Composable
fun CreateScreen(
    onSelectQrType: (String) -> Unit,
    onSelectBarcodeFormat: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    val tabs = listOf("Universal QR Studio", "Barcodes & 2D Formats")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        QobleyTopBar(
            title = "Universal Studio"
        )

        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (selectedTabIndex == index) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("create_tab_$index")
                )
            }
        }

        // Search Bar for quick filter
        QobleySearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = if (selectedTabIndex == 0) "Search QR creation types..." else "Search barcode formats...",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        if (selectedTabIndex == 0) {
            // Category filter chips
            val categories = listOf("All", "Web & Links", "Communication", "Payments", "Events & Data")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 2.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategoryFilter == cat,
                        onClick = { selectedCategoryFilter = cat },
                        label = { Text(cat) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            if (selectedTabIndex == 0) {
                // QR Creation Types
                val qrItems = listOf(
                    QrTypeItem("URL", "Website & Links", "Universal web link with HTTPS normalization & branded labels", Icons.Outlined.Language, "Web & Links", "create_type_url"),
                    QrTypeItem("SOCIAL", "Social Profile", "Instagram, X / Twitter, YouTube, LinkedIn, GitHub, WhatsApp", Icons.Outlined.Share, "Web & Links", "create_type_social"),
                    QrTypeItem("WIFI", "Wi-Fi Network", "Quick connect without typing passwords (WPA/WPA2/WPA3)", Icons.Outlined.Wifi, "Communication", "create_type_wifi"),
                    QrTypeItem("CONTACT", "Contact Card (vCard)", "Full contact card with name, phone, email, organization", Icons.Outlined.Person, "Communication", "create_type_contact"),
                    QrTypeItem("PAYMENT", "UPI Payment", "Unified Payments Interface with payee ID, name & amount", Icons.Outlined.Payment, "Payments", "create_type_upi"),
                    QrTypeItem("PHONE", "Phone Call", "Instant dialer shortcut with country code support", Icons.Outlined.Call, "Communication", "create_type_phone"),
                    QrTypeItem("SMS", "SMS Message", "Prefilled recipient phone number and SMS body", Icons.Outlined.Message, "Communication", "create_type_sms"),
                    QrTypeItem("EMAIL", "Email Address", "Mailto with recipient address, subject line and body", Icons.Outlined.Email, "Communication", "create_type_email"),
                    QrTypeItem("LOCATION", "Location Coordinates", "GPS latitude/longitude coordinates with map label", Icons.Outlined.LocationOn, "Events & Data", "create_type_location"),
                    QrTypeItem("CALENDAR", "Calendar Event", "Add event to calendar with title, timing and venue", Icons.Outlined.CalendarToday, "Events & Data", "create_type_calendar"),
                    QrTypeItem("TEXT", "Plain Text", "Notes, serial numbers, credentials, instructions", Icons.Outlined.TextFields, "Events & Data", "create_type_text"),
                    QrTypeItem("TOTP", "2FA Authenticator (TOTP)", "Two-factor auth setup payload (otpauth://totp/...)", Icons.Outlined.Security, "Payments", "create_type_totp"),
                    QrTypeItem("EMERGENCY", "Emergency Contact (ICE)", "In Case of Emergency medical contact & blood group info", Icons.Outlined.MedicalServices, "Communication", "create_type_emergency"),
                    QrTypeItem("JSON", "JSON / Structured Data", "Raw JSON payload with schema validation", Icons.Outlined.Code, "Events & Data", "create_type_json"),
                    QrTypeItem("CUSTOM", "Custom URI / Deep Link", "Custom app deep links (e.g. app://, spotify:, custom)", Icons.Outlined.QrCode, "Web & Links", "create_type_custom")
                )

                val filtered = qrItems.filter { item ->
                    val matchesSearch = searchQuery.isBlank() || item.title.contains(searchQuery, ignoreCase = true) || item.subtitle.contains(searchQuery, ignoreCase = true)
                    val matchesCategory = selectedCategoryFilter == "All" || item.category == selectedCategoryFilter
                    matchesSearch && matchesCategory
                }

                item {
                    Text(
                        text = "Choose Payload (${filtered.size} formats available)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }

                items(filtered.size) { i ->
                    val item = filtered[i]
                    CreateTypeRow(
                        icon = item.icon,
                        title = item.title,
                        subtitle = item.subtitle,
                        testTag = item.testTag,
                        onClick = { onSelectQrType(item.typeKey) }
                    )
                }
            } else {
                // Barcode Types
                val barcodeItems = listOf(
                    BarcodeTypeItem("CODE_128", "Code 128", "High-density alphanumeric barcode for logistics, packages & serials", Icons.Outlined.ViewWeek, "create_format_code_128"),
                    BarcodeTypeItem("EAN_13", "EAN-13", "International standard retail product code (12 or 13 digits with auto-checksum)", Icons.Outlined.ViewWeek, "create_format_ean_13"),
                    BarcodeTypeItem("UPC_A", "UPC-A", "Standard US/Canada retail product barcode (11 or 12 digits with auto-checksum)", Icons.Outlined.ViewWeek, "create_format_upc_a"),
                    BarcodeTypeItem("EAN_8", "EAN-8", "Compact retail barcode for small packaging (7 or 8 digits with auto-checksum)", Icons.Outlined.ViewWeek, "create_format_ean_8"),
                    BarcodeTypeItem("CODE_39", "Code 39", "Uppercase alphanumeric barcode for inventory and military badges", Icons.Outlined.ViewWeek, "create_format_code_39"),
                    BarcodeTypeItem("CODE_93", "Code 93", "Higher density alternative to Code 39 with standard ASCII support", Icons.Outlined.ViewWeek, "create_format_code_93"),
                    BarcodeTypeItem("ITF", "ITF (Interleaved 2 of 5)", "High-density numeric barcode for cartons and warehousing", Icons.Outlined.ViewWeek, "create_format_itf"),
                    BarcodeTypeItem("CODABAR", "Codabar", "Self-checking numeric barcode for libraries, blood banks & airbills", Icons.Outlined.ViewWeek, "create_format_codabar"),
                    BarcodeTypeItem("DATA_MATRIX", "Data Matrix", "2D compact matrix code for electronics, components & medical items", Icons.Outlined.QrCode, "create_format_data_matrix"),
                    BarcodeTypeItem("PDF_417", "PDF417", "Stacked 2D barcode for boarding passes, transport & identification", Icons.Outlined.QrCode, "create_format_pdf417"),
                    BarcodeTypeItem("AZTEC", "Aztec Code", "2D matrix barcode with center finder for rail, flight & transport tickets", Icons.Outlined.QrCode, "create_format_aztec")
                )

                val filtered = barcodeItems.filter { item ->
                    searchQuery.isBlank() || item.title.contains(searchQuery, ignoreCase = true) || item.subtitle.contains(searchQuery, ignoreCase = true)
                }

                item {
                    Text(
                        text = "1D & 2D Barcode Standards (${filtered.size} available)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }

                items(filtered.size) { i ->
                    val item = filtered[i]
                    CreateTypeRow(
                        icon = item.icon,
                        title = item.title,
                        subtitle = item.subtitle,
                        testTag = item.testTag,
                        onClick = { onSelectBarcodeFormat(item.formatKey) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }
}

private data class QrTypeItem(
    val typeKey: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val category: String,
    val testTag: String
)

private data class BarcodeTypeItem(
    val formatKey: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
private fun CreateTypeRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

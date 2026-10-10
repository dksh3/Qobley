package com.example.ui.screens.create

import android.content.Intent
import android.graphics.Color as AndroidColor
import android.net.Uri
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.MainViewModel
import com.example.generator.BarcodeGenerator
import com.example.ui.components.QobleyCodePreview
import com.example.ui.components.QobleyTopBar
import com.example.util.QobleyPrintHelper
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrCreatorScreen(
    initialType: String = "URL",
    prefilledText: String? = null,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var qrType by remember { mutableStateOf(initialType) }

    // URL Form States
    var urlText by remember(prefilledText) {
        mutableStateOf(
            if (!prefilledText.isNullOrBlank() && (prefilledText.startsWith("http://") || prefilledText.startsWith("https://") || prefilledText.contains("."))) {
                prefilledText
            } else {
                "https://qobley.vercel.app"
            }
        )
    }
    var displayLabel by remember { mutableStateOf("") }

    // Social Form States
    var selectedSocialPlatform by remember { mutableStateOf("Instagram") }
    var socialHandle by remember { mutableStateOf("") }

    // Wi-Fi Form States
    var wifiSsid by remember { mutableStateOf("") }
    var wifiPassword by remember { mutableStateOf("") }
    var wifiSecurity by remember { mutableStateOf("WPA") }
    var wifiHidden by remember { mutableStateOf(false) }
    var showWifiPassword by remember { mutableStateOf(false) }

    // Contact Form States
    var contactName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var contactOrg by remember { mutableStateOf("") }
    var contactTitle by remember { mutableStateOf("") }
    var contactWebsite by remember { mutableStateOf("") }

    // Payment / UPI Form States
    var upiId by remember { mutableStateOf("") }
    var upiName by remember { mutableStateOf("") }
    var upiAmount by remember { mutableStateOf("") }
    var upiNote by remember { mutableStateOf("") }

    // Communication States
    var phoneNumber by remember { mutableStateOf("") }
    var smsNumber by remember { mutableStateOf("") }
    var smsBody by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }
    var emailSubject by remember { mutableStateOf("") }
    var emailBody by remember { mutableStateOf("") }

    // Location States
    var geoLat by remember { mutableStateOf("") }
    var geoLng by remember { mutableStateOf("") }
    var geoLabel by remember { mutableStateOf("") }

    // Calendar States
    var eventTitle by remember { mutableStateOf("") }
    var eventLocation by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }

    // TOTP States
    var totpAccount by remember { mutableStateOf("") }
    var totpIssuer by remember { mutableStateOf("") }
    var totpSecret by remember { mutableStateOf("") }

    // Emergency Contact States
    var iceName by remember { mutableStateOf("") }
    var icePhone by remember { mutableStateOf("") }
    var iceRelation by remember { mutableStateOf("") }
    var iceMedicalNotes by remember { mutableStateOf("") }

    // Text & JSON & Custom States
    var plainText by remember(prefilledText) {
        mutableStateOf(if (!prefilledText.isNullOrBlank() && prefilledText != urlText) prefilledText else "")
    }
    var jsonContent by remember { mutableStateOf("{\n  \"item\": \"Qobley\",\n  \"status\": \"active\"\n}") }
    var customPayload by remember { mutableStateOf("") }

    // Designer & Appearance State
    var showCustomizationSheet by remember { mutableStateOf(false) }
    var fgColorInt by remember { mutableIntStateOf(AndroidColor.BLACK) }
    var bgColorInt by remember { mutableIntStateOf(AndroidColor.WHITE) }
    var ecLevel by remember { mutableStateOf("M") }
    var marginVal by remember { mutableIntStateOf(1) }
    var selectedEmblem by remember { mutableStateOf(BarcodeGenerator.EmblemType.NONE) }
    var monogramInput by remember { mutableStateOf("Q") }

    // Computed Normalized URL
    val normalizedUrl by remember {
        derivedStateOf {
            val trimmed = urlText.trim()
            when {
                trimmed.startsWith("http://", ignoreCase = true) -> trimmed
                trimmed.startsWith("https://", ignoreCase = true) -> trimmed
                trimmed.isNotEmpty() -> "https://$trimmed"
                else -> ""
            }
        }
    }

    // Build Payload Reactively
    val payload by remember {
        derivedStateOf {
            when (qrType) {
                "URL" -> normalizedUrl

                "SOCIAL" -> {
                    val handle = socialHandle.trim().removePrefix("@")
                    when (selectedSocialPlatform) {
                        "Instagram" -> "https://instagram.com/$handle"
                        "X / Twitter" -> "https://x.com/$handle"
                        "YouTube" -> "https://youtube.com/@$handle"
                        "LinkedIn" -> "https://linkedin.com/in/$handle"
                        "GitHub" -> "https://github.com/$handle"
                        "WhatsApp" -> "https://wa.me/${handle.filter { it.isDigit() }}"
                        "TikTok" -> "https://tiktok.com/@$handle"
                        else -> "https://example.com/$handle"
                    }
                }

                "WIFI" -> {
                    val hiddenFlag = if (wifiHidden) "H:true;" else ""
                    "WIFI:S:$wifiSsid;T:$wifiSecurity;P:$wifiPassword;$hiddenFlag;"
                }

                "CONTACT" -> buildString {
                    append("BEGIN:VCARD\nVERSION:3.0\n")
                    if (contactName.isNotEmpty()) append("FN:$contactName\n")
                    if (contactPhone.isNotEmpty()) append("TEL:$contactPhone\n")
                    if (contactEmail.isNotEmpty()) append("EMAIL:$contactEmail\n")
                    if (contactOrg.isNotEmpty()) append("ORG:$contactOrg\n")
                    if (contactTitle.isNotEmpty()) append("TITLE:$contactTitle\n")
                    if (contactWebsite.isNotEmpty()) append("URL:$contactWebsite\n")
                    append("END:VCARD")
                }

                "PAYMENT" -> buildString {
                    append("upi://pay?pa=$upiId")
                    if (upiName.isNotEmpty()) append("&pn=${Uri.encode(upiName)}")
                    if (upiAmount.isNotEmpty()) append("&am=$upiAmount")
                    append("&cu=INR")
                    if (upiNote.isNotEmpty()) append("&tn=${Uri.encode(upiNote)}")
                }

                "PHONE" -> "tel:${phoneNumber.trim()}"

                "SMS" -> "smsto:${smsNumber.trim()}:${smsBody.trim()}"

                "EMAIL" -> buildString {
                    append("mailto:${emailAddress.trim()}")
                    val params = mutableListOf<String>()
                    if (emailSubject.isNotEmpty()) params.add("subject=${Uri.encode(emailSubject)}")
                    if (emailBody.isNotEmpty()) params.add("body=${Uri.encode(emailBody)}")
                    if (params.isNotEmpty()) append("?${params.joinToString("&")}")
                }

                "LOCATION" -> {
                    val lat = geoLat.trim()
                    val lng = geoLng.trim()
                    if (geoLabel.isNotEmpty()) "geo:$lat,$lng?q=$lat,$lng(${Uri.encode(geoLabel)})"
                    else "geo:$lat,$lng"
                }

                "CALENDAR" -> buildString {
                    append("BEGIN:VEVENT\n")
                    append("SUMMARY:$eventTitle\n")
                    if (eventLocation.isNotEmpty()) append("LOCATION:$eventLocation\n")
                    if (eventDescription.isNotEmpty()) append("DESCRIPTION:$eventDescription\n")
                    append("END:VEVENT")
                }

                "TOTP" -> {
                    val issuerPart = if (totpIssuer.isNotEmpty()) "${Uri.encode(totpIssuer)}:" else ""
                    val accountPart = Uri.encode(totpAccount.ifEmpty { "Account" })
                    val secretPart = totpSecret.trim().replace(" ", "").uppercase()
                    buildString {
                        append("otpauth://totp/$issuerPart$accountPart?secret=$secretPart")
                        if (totpIssuer.isNotEmpty()) append("&issuer=${Uri.encode(totpIssuer)}")
                    }
                }

                "EMERGENCY" -> buildString {
                    append("BEGIN:VCARD\nVERSION:3.0\n")
                    append("FN:ICE: $iceName\n")
                    append("TEL:$icePhone\n")
                    val notes = listOfNotNull(
                        if (iceRelation.isNotEmpty()) "Relation: $iceRelation" else null,
                        if (iceMedicalNotes.isNotEmpty()) "Medical: $iceMedicalNotes" else null
                    ).joinToString(" | ")
                    if (notes.isNotEmpty()) append("NOTE:$notes\n")
                    append("END:VCARD")
                }

                "TEXT" -> plainText

                "JSON" -> jsonContent

                "CUSTOM" -> customPayload

                else -> plainText
            }
        }
    }

    val defaultTitle by remember {
        derivedStateOf {
            if (displayLabel.isNotBlank()) displayLabel
            else when (qrType) {
                "URL" -> urlText.removePrefix("https://").removePrefix("http://")
                "SOCIAL" -> "$selectedSocialPlatform: @${socialHandle.ifEmpty { "Profile" }}"
                "WIFI" -> "Wi-Fi: ${wifiSsid.ifEmpty { "Network" }}"
                "CONTACT" -> "Contact: ${contactName.ifEmpty { "Card" }}"
                "PAYMENT" -> "UPI: ${upiName.ifEmpty { upiId }}"
                "PHONE" -> "Call: $phoneNumber"
                "SMS" -> "SMS: $smsNumber"
                "EMAIL" -> "Email: $emailAddress"
                "LOCATION" -> "Location: ${geoLabel.ifEmpty { "$geoLat, $geoLng" }}"
                "CALENDAR" -> "Event: ${eventTitle.ifEmpty { "Calendar" }}"
                "TOTP" -> "2FA: ${totpIssuer.ifEmpty { "Authenticator" }}"
                "EMERGENCY" -> "ICE: $iceName"
                "JSON" -> "JSON Payload"
                "CUSTOM" -> "Custom URI"
                else -> payload.take(30)
            }
        }
    }

    val defaultCategory by remember {
        derivedStateOf {
            when (qrType) {
                "WIFI" -> "Wi-Fi"
                "CONTACT", "PHONE", "SMS", "EMERGENCY" -> "Contacts"
                "PAYMENT" -> "Payments"
                "URL", "SOCIAL" -> "Work"
                else -> "Personal"
            }
        }
    }

    val typeOptions = listOf(
        "URL" to "Link / URL",
        "SOCIAL" to "Social Profile",
        "WIFI" to "Wi-Fi",
        "CONTACT" to "Contact Card",
        "PAYMENT" to "UPI Payment",
        "PHONE" to "Phone Call",
        "SMS" to "SMS",
        "EMAIL" to "Email",
        "LOCATION" to "Location",
        "CALENDAR" to "Calendar Event",
        "TEXT" to "Plain Text",
        "TOTP" to "2FA Authenticator",
        "EMERGENCY" to "Emergency ICE",
        "JSON" to "JSON",
        "CUSTOM" to "Custom URI"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        QobleyTopBar(
            title = "Universal Creator Studio",
            onBack = onBack,
            actions = {
                IconButton(
                    onClick = { showCustomizationSheet = true },
                    modifier = Modifier.testTag("qr_customize_button")
                ) {
                    Icon(imageVector = Icons.Default.Tune, contentDescription = "Customize Code Appearance")
                }
            }
        )

        // Type Selector Chip Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            typeOptions.forEach { (key, label) ->
                FilterChip(
                    selected = qrType == key,
                    onClick = { qrType = key },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("type_chip_$key")
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Preview Card
            if (payload.isNotEmpty()) {
                QobleyCodePreview(
                    content = payload,
                    format = "QR_CODE",
                    displayLabel = displayLabel.ifEmpty { null },
                    qrConfig = BarcodeGenerator.QrConfig(
                        foregroundColor = fgColorInt,
                        backgroundColor = bgColorInt,
                        errorCorrection = ecLevel,
                        margin = marginVal,
                        emblemType = selectedEmblem,
                        monogramText = monogramInput
                    ),
                    showActionRow = true,
                    onToast = { viewModel.showToast(it) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Input Form Sections Based on Type
            when (qrType) {
                "URL" -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = urlText,
                            onValueChange = { urlText = it },
                            label = { Text("Destination URL or Domain") },
                            placeholder = { Text("e.g. qobley.vercel.app or https://...") },
                            trailingIcon = {
                                IconButton(onClick = {
                                    val clip = clipboardManager.getText()?.text
                                    if (!clip.isNullOrBlank()) {
                                        urlText = clip
                                        viewModel.showToast("Pasted from clipboard")
                                    }
                                }) {
                                    Icon(Icons.Outlined.ContentPaste, contentDescription = "Paste", modifier = Modifier.size(20.dp))
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("qr_input_url")
                        )

                        // URL Normalization & Destination Helper
                        if (!urlText.startsWith("http://") && !urlText.startsWith("https://") && urlText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                    modifier = Modifier.clickable {
                                        urlText = "https://${urlText.trim()}"
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Outlined.Check, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Add HTTPS (https://${urlText.trim()})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }

                                TextButton(
                                    onClick = {
                                        try {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(normalizedUrl)))
                                        } catch (_: Exception) {
                                            viewModel.showToast("Unable to open link")
                                        }
                                    }
                                ) {
                                    Icon(Icons.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Test Link", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Custom Display Label for Link
                        OutlinedTextField(
                            value = displayLabel,
                            onValueChange = { displayLabel = it },
                            label = { Text("Display Label / Brand Title (optional)") },
                            placeholder = { Text("e.g. Official Portfolio, Restaurant Menu") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("qr_input_display_label")
                        )
                    }
                }

                "SOCIAL" -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Platform", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Instagram", "X / Twitter", "YouTube", "LinkedIn", "GitHub", "WhatsApp", "TikTok").forEach { platform ->
                                FilterChip(
                                    selected = selectedSocialPlatform == platform,
                                    onClick = { selectedSocialPlatform = platform },
                                    label = { Text(platform) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = socialHandle,
                            onValueChange = { socialHandle = it },
                            label = { Text(if (selectedSocialPlatform == "WhatsApp") "Phone Number with country code" else "Username / Handle") },
                            placeholder = { Text(if (selectedSocialPlatform == "WhatsApp") "+1234567890" else "username") },
                            leadingIcon = {
                                if (selectedSocialPlatform != "WhatsApp") {
                                    Text("@", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 12.dp))
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("qr_input_social_handle")
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Destination: $payload",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                "WIFI" -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = wifiSsid,
                            onValueChange = { wifiSsid = it },
                            label = { Text("Network Name (SSID)") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("qr_input_wifi_ssid")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = wifiPassword,
                            onValueChange = { wifiPassword = it },
                            label = { Text("Wi-Fi Password") },
                            visualTransformation = if (showWifiPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showWifiPassword = !showWifiPassword }) {
                                    Icon(
                                        imageVector = if (showWifiPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                        contentDescription = "Toggle password visibility"
                                    )
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("qr_input_wifi_password")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("WPA", "WEP", "nopass").forEach { sec ->
                                FilterChip(
                                    selected = wifiSecurity == sec,
                                    onClick = { wifiSecurity = sec },
                                    label = { Text(if (sec == "nopass") "Open (No password)" else sec) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Hidden Network", style = MaterialTheme.typography.bodyMedium)
                            Switch(checked = wifiHidden, onCheckedChange = { wifiHidden = it })
                        }
                    }
                }

                "CONTACT" -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = contactName,
                            onValueChange = { contactName = it },
                            label = { Text("Full Name *") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("qr_input_contact_name")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = contactPhone,
                            onValueChange = { contactPhone = it },
                            label = { Text("Phone Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("qr_input_contact_phone")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = contactEmail,
                            onValueChange = { contactEmail = it },
                            label = { Text("Email Address") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("qr_input_contact_email")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = contactOrg,
                            onValueChange = { contactOrg = it },
                            label = { Text("Organization / Company") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("qr_input_contact_org")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = contactTitle,
                            onValueChange = { contactTitle = it },
                            label = { Text("Job Title") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = contactWebsite,
                            onValueChange = { contactWebsite = it },
                            label = { Text("Website") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                "PAYMENT" -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = upiId,
                            onValueChange = { upiId = it },
                            label = { Text("UPI ID (VPA) *") },
                            placeholder = { Text("e.g. merchant@okhdfcbank") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("qr_input_upi_id")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = upiName,
                            onValueChange = { upiName = it },
                            label = { Text("Payee Name") },
                            placeholder = { Text("e.g. Coffee Shop") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("qr_input_upi_name")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = upiAmount,
                            onValueChange = { upiAmount = it },
                            label = { Text("Amount (INR ₹, optional)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("qr_input_upi_amount")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = upiNote,
                            onValueChange = { upiNote = it },
                            label = { Text("Payment Note (optional)") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("qr_input_upi_note")
                        )
                    }
                }

                "PHONE" -> {
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number to Dial") },
                        placeholder = { Text("+1 555 123 4567") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("qr_input_phone")
                    )
                }

                "SMS" -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = smsNumber,
                            onValueChange = { smsNumber = it },
                            label = { Text("Recipient Phone Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("qr_input_sms_number")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = smsBody,
                            onValueChange = { smsBody = it },
                            label = { Text("Message Body") },
                            minLines = 3,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("qr_input_sms_body")
                        )
                    }
                }

                "EMAIL" -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = emailAddress,
                            onValueChange = { emailAddress = it },
                            label = { Text("Recipient Email Address") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("qr_input_email_address")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = emailSubject,
                            onValueChange = { emailSubject = it },
                            label = { Text("Subject (optional)") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("qr_input_email_subject")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = emailBody,
                            onValueChange = { emailBody = it },
                            label = { Text("Email Body (optional)") },
                            minLines = 3,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("qr_input_email_body")
                        )
                    }
                }

                "LOCATION" -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = geoLat,
                                onValueChange = { geoLat = it },
                                label = { Text("Latitude") },
                                placeholder = { Text("37.7749") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("qr_input_geo_lat")
                            )
                            OutlinedTextField(
                                value = geoLng,
                                onValueChange = { geoLng = it },
                                label = { Text("Longitude") },
                                placeholder = { Text("-122.4194") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("qr_input_geo_lng")
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = geoLabel,
                            onValueChange = { geoLabel = it },
                            label = { Text("Place / Venue Name (optional)") },
                            placeholder = { Text("e.g. San Francisco Ferry Building") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                "CALENDAR" -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = eventTitle,
                            onValueChange = { eventTitle = it },
                            label = { Text("Event Title *") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("qr_input_event_title")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = eventLocation,
                            onValueChange = { eventLocation = it },
                            label = { Text("Event Venue / Location") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("qr_input_event_location")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = eventDescription,
                            onValueChange = { eventDescription = it },
                            label = { Text("Description") },
                            minLines = 2,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                "TOTP" -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = totpAccount,
                            onValueChange = { totpAccount = it },
                            label = { Text("Account Identifier") },
                            placeholder = { Text("user@company.com") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = totpIssuer,
                            onValueChange = { totpIssuer = it },
                            label = { Text("Issuer / Service") },
                            placeholder = { Text("e.g. GitHub, Google, Slack") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = totpSecret,
                            onValueChange = { totpSecret = it },
                            label = { Text("Base32 Secret Key") },
                            placeholder = { Text("JBSWY3DPEHPK3PXP") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                "EMERGENCY" -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = iceName,
                            onValueChange = { iceName = it },
                            label = { Text("Emergency Contact Name *") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = icePhone,
                            onValueChange = { icePhone = it },
                            label = { Text("Emergency Phone Number *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = iceRelation,
                            onValueChange = { iceRelation = it },
                            label = { Text("Relationship (e.g. Spouse, Parent, Doctor)") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = iceMedicalNotes,
                            onValueChange = { iceMedicalNotes = it },
                            label = { Text("Blood Group & Critical Medical Notes") },
                            placeholder = { Text("O+ Positive, Penicillin allergy") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                "TEXT" -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = plainText,
                            onValueChange = { plainText = it },
                            label = { Text("Plain Text Content") },
                            trailingIcon = {
                                IconButton(onClick = {
                                    val clip = clipboardManager.getText()?.text
                                    if (!clip.isNullOrBlank()) {
                                        plainText = clip
                                        viewModel.showToast("Pasted from clipboard")
                                    }
                                }) {
                                    Icon(Icons.Outlined.ContentPaste, contentDescription = "Paste", modifier = Modifier.size(20.dp))
                                }
                            },
                            minLines = 4,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("qr_input_text")
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${plainText.length} characters",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                "JSON" -> {
                    val isJsonValid = remember(jsonContent) {
                        try {
                            JSONObject(jsonContent)
                            true
                        } catch (_: Exception) {
                            false
                        }
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = jsonContent,
                            onValueChange = { jsonContent = it },
                            label = { Text("JSON Content") },
                            minLines = 5,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isJsonValid) "✓ Valid JSON format" else "Notice: JSON syntax is not yet closed",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isJsonValid) Color(0xFF059669) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                "CUSTOM" -> {
                    OutlinedTextField(
                        value = customPayload,
                        onValueChange = { customPayload = it },
                        label = { Text("Raw URI Scheme or Custom Payload") },
                        placeholder = { Text("e.g. spotify:track:..., myapp://action?id=42") },
                        minLines = 3,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Save Action
            Button(
                onClick = {
                    if (payload.isNotBlank()) {
                        viewModel.saveGeneratedCode(
                            type = "QR",
                            format = "QR_CODE",
                            content = payload,
                            title = defaultTitle,
                            category = defaultCategory
                        )
                        viewModel.showToast("Saved to Library")
                        onBack()
                    } else {
                        viewModel.showToast("Please enter content to generate")
                    }
                },
                enabled = payload.isNotBlank(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.fillMaxWidth().testTag("save_generated_qr_button")
            ) {
                Icon(imageVector = Icons.Outlined.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save to Library", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary Quick Actions (Print & Share)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = {
                        val bitmap = BarcodeGenerator.generate(
                            formatString = "QR_CODE",
                            content = payload,
                            width = 1000,
                            height = 1000,
                            qrConfig = BarcodeGenerator.QrConfig(
                                foregroundColor = fgColorInt,
                                backgroundColor = bgColorInt,
                                errorCorrection = ecLevel,
                                margin = marginVal,
                                emblemType = selectedEmblem,
                                monogramText = monogramInput
                            )
                        )
                        if (bitmap != null) {
                            QobleyPrintHelper.printCode(
                                context = context,
                                title = defaultTitle,
                                content = payload,
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
                    Text("Print Code")
                }

                OutlinedButton(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, payload)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share code"))
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

    // Custom Code Designer & Appearance Bottom Sheet
    if (showCustomizationSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCustomizationSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 36.dp)
            ) {
                Text(
                    text = "Custom Code Designer",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Personalize colors, center badges and error resilience",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(18.dp))

                // High Contrast Foreground Color Palette
                Text(text = "Code Color (High Contrast)", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val colors = listOf(
                        "Classic Black" to AndroidColor.BLACK,
                        "Slate Blue" to AndroidColor.rgb(30, 41, 59),
                        "Royal Indigo" to AndroidColor.rgb(30, 27, 75),
                        "Forest Green" to AndroidColor.rgb(6, 78, 59),
                        "Crimson Burgundy" to AndroidColor.rgb(74, 4, 78),
                        "Rich Espresso" to AndroidColor.rgb(39, 28, 25),
                        "Charcoal Teal" to AndroidColor.rgb(19, 78, 74),
                        "Sunset Rust" to AndroidColor.rgb(124, 45, 18)
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

                // Background Tone
                Text(text = "Background Tone", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    val bgColors = listOf(
                        "Pure White" to AndroidColor.WHITE,
                        "Warm Cream" to AndroidColor.rgb(254, 251, 246),
                        "Light Slate" to AndroidColor.rgb(248, 250, 252),
                        "Soft Mint" to AndroidColor.rgb(240, 253, 244)
                    )
                    bgColors.forEach { (name, c) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(c),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (bgColorInt == c) 2.dp else 1.dp,
                                color = if (bgColorInt == c) MaterialTheme.colorScheme.primary else Color.LightGray
                            ),
                            modifier = Modifier
                                .clickable { bgColorInt = c }
                                .padding(vertical = 2.dp)
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

                Spacer(modifier = Modifier.height(20.dp))

                // Center Emblem / Logo Badge
                Text(text = "Center Emblem / Monogram", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = "Embedded with a protective quiet boundary so the code remains 100% scannable",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BarcodeGenerator.EmblemType.values().forEach { emb ->
                        FilterChip(
                            selected = selectedEmblem == emb,
                            onClick = {
                                selectedEmblem = emb
                                if (emb != BarcodeGenerator.EmblemType.NONE && (ecLevel == "L" || ecLevel == "M")) {
                                    ecLevel = "H" // Automatic upgrade for scannability
                                }
                            },
                            label = { Text(emb.label) }
                        )
                    }
                }

                if (selectedEmblem == BarcodeGenerator.EmblemType.MONOGRAM) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = monogramInput,
                        onValueChange = { if (it.length <= 3) monogramInput = it.uppercase() },
                        label = { Text("Monogram Text (1-3 Letters)") },
                        placeholder = { Text("Q") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Error Correction Level
                Text(text = "Error Correction Resilience", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "L" to "7% (Compact)",
                        "M" to "15% (Standard)",
                        "Q" to "25% (High)",
                        "H" to "30% (Emblem/Max)"
                    ).forEach { (level, desc) ->
                        FilterChip(
                            selected = ecLevel == level,
                            onClick = { ecLevel = level },
                            label = { Text("$level: $desc") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Quiet Zone Margin
                Text(text = "Quiet Zone Margin", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1 to "Compact (1)", 2 to "Standard (2)", 4 to "Wide (4)").forEach { (m, name) ->
                        FilterChip(
                            selected = marginVal == m,
                            onClick = { marginVal = m },
                            label = { Text(name) }
                        )
                    }
                }
            }
        }
    }
}

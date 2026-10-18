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
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
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
    var selectedStudioTab by remember { mutableIntStateOf(0) } // 0 = Content & Payload, 1 = Design & Customization

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

    // ULTRA CUSTOMIZATION STATES
    var fgColorInt by remember { mutableIntStateOf(AndroidColor.BLACK) }
    var bgColorInt by remember { mutableIntStateOf(AndroidColor.WHITE) }
    var isTransparentBg by remember { mutableStateOf(false) }
    var isGradientEnabled by remember { mutableStateOf(false) }
    var gradientSecondaryColorInt by remember { mutableIntStateOf(AndroidColor.rgb(37, 99, 235)) }

    var dotStyle by remember { mutableStateOf(BarcodeGenerator.DotStyle.SQUARE) }
    var eyeFrameStyle by remember { mutableStateOf(BarcodeGenerator.EyeFrameStyle.SQUARE) }
    var eyePupilStyle by remember { mutableStateOf(BarcodeGenerator.EyePupilStyle.SQUARE) }
    var customEyeColorInt by remember { mutableStateOf<Int?>(null) }

    var selectedEmblem by remember { mutableStateOf(BarcodeGenerator.EmblemType.NONE) }
    var monogramInput by remember { mutableStateOf("Q") }
    var monogramShape by remember { mutableStateOf(BarcodeGenerator.MonogramShape.CIRCLE) }

    var bannerText by remember { mutableStateOf("") }
    var bannerPosition by remember { mutableStateOf(BarcodeGenerator.BannerPosition.BOTTOM) }

    var ecLevel by remember { mutableStateOf("M") }
    var marginVal by remember { mutableIntStateOf(1) }

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
                "CONTACT" -> "Contact: ${contactName.ifEmpty { "Person" }}"
                "PAYMENT" -> "Pay: ${upiName.ifEmpty { upiId.ifEmpty { "Merchant" } }}"
                "PHONE" -> "Phone: $phoneNumber"
                "SMS" -> "SMS: $smsNumber"
                "EMAIL" -> "Email: $emailAddress"
                "LOCATION" -> if (geoLabel.isNotEmpty()) geoLabel else "Location: $geoLat, $geoLng"
                "CALENDAR" -> "Event: ${eventTitle.ifEmpty { "Meeting" }}"
                "TOTP" -> "2FA: ${totpIssuer.ifEmpty { totpAccount.ifEmpty { "Authenticator" } }}"
                "EMERGENCY" -> "ICE: ${iceName.ifEmpty { "Emergency Contact" }}"
                "TEXT" -> plainText.take(24)
                "JSON" -> "JSON Schema"
                "CUSTOM" -> "Custom URI"
                else -> "Custom QR"
            }
        }
    }

    val defaultCategory by remember {
        derivedStateOf {
            when (qrType) {
                "URL", "SOCIAL", "CUSTOM" -> "URL"
                "WIFI" -> "WIFI"
                "CONTACT", "EMERGENCY" -> "CONTACT"
                "PAYMENT" -> "PAYMENT"
                "PHONE" -> "PHONE"
                "SMS" -> "SMS"
                "EMAIL" -> "EMAIL"
                "LOCATION" -> "LOCATION"
                "CALENDAR" -> "CALENDAR"
                "TOTP" -> "PAYMENT"
                else -> "TEXT"
            }
        }
    }

    // Active QrConfig state
    val currentQrConfig = remember(
        fgColorInt, bgColorInt, isTransparentBg, isGradientEnabled, gradientSecondaryColorInt,
        dotStyle, eyeFrameStyle, eyePupilStyle, customEyeColorInt, selectedEmblem,
        monogramInput, monogramShape, bannerText, bannerPosition, ecLevel, marginVal
    ) {
        BarcodeGenerator.QrConfig(
            foregroundColor = fgColorInt,
            backgroundColor = bgColorInt,
            errorCorrection = ecLevel,
            margin = marginVal,
            emblemType = selectedEmblem,
            monogramText = monogramInput,
            monogramShape = monogramShape,
            dotStyle = dotStyle,
            eyeFrameStyle = eyeFrameStyle,
            eyePupilStyle = eyePupilStyle,
            eyeColor = customEyeColorInt,
            gradientSecondaryColor = if (isGradientEnabled) gradientSecondaryColorInt else null,
            isTransparentBackground = isTransparentBg,
            bannerText = bannerText.ifBlank { null },
            bannerPosition = bannerPosition
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        QobleyTopBar(
            title = "QR Studio",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Interactive Preview
            if (payload.isNotBlank()) {
                QobleyCodePreview(
                    content = payload,
                    format = "QR_CODE",
                    displayLabel = defaultTitle,
                    qrConfig = currentQrConfig,
                    onToast = { viewModel.showToast(it) }
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Fill in payload data to generate live QR code",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Studio Mode Tabs: Content vs Design & Customization
            TabRow(
                selectedTabIndex = selectedStudioTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedStudioTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedStudioTab == 0,
                    onClick = { selectedStudioTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Content & Data", maxLines = 1, softWrap = false)
                        }
                    }
                )
                Tab(
                    selected = selectedStudioTab == 1,
                    onClick = { selectedStudioTab = 1 },
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

            if (selectedStudioTab == 0) {
                // TAB 0: CONTENT & PAYLOAD
                val types = listOf(
                    "URL" to "Web Link",
                    "SOCIAL" to "Social",
                    "WIFI" to "Wi-Fi",
                    "CONTACT" to "vCard",
                    "PAYMENT" to "UPI Pay",
                    "PHONE" to "Phone",
                    "SMS" to "SMS",
                    "EMAIL" to "Email",
                    "LOCATION" to "GPS Map",
                    "CALENDAR" to "Event",
                    "TEXT" to "Plain Text",
                    "TOTP" to "2FA Auth",
                    "EMERGENCY" to "ICE Contact",
                    "JSON" to "JSON",
                    "CUSTOM" to "Custom URI"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    types.forEach { (typeKey, label) ->
                        FilterChip(
                            selected = qrType == typeKey,
                            onClick = { qrType = typeKey },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Render Active Payload Form
                when (qrType) {
                    "URL" -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = urlText,
                                onValueChange = { urlText = it },
                                label = { Text("Website Link / Address *") },
                                placeholder = { Text("example.com") },
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
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = displayLabel,
                                onValueChange = { displayLabel = it },
                                label = { Text("Display Name / Label (optional)") },
                                placeholder = { Text("e.g. My Portfolio") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("qr_input_label")
                            )
                        }
                    }

                    "SOCIAL" -> {
                        val platforms = listOf("Instagram", "X / Twitter", "YouTube", "LinkedIn", "GitHub", "WhatsApp", "TikTok")
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                platforms.forEach { plat ->
                                    FilterChip(
                                        selected = selectedSocialPlatform == plat,
                                        onClick = { selectedSocialPlatform = plat },
                                        label = { Text(plat) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = socialHandle,
                                onValueChange = { socialHandle = it },
                                label = { Text(if (selectedSocialPlatform == "WhatsApp") "Phone Number with Country Code" else "Profile Username / Handle") },
                                placeholder = { Text(if (selectedSocialPlatform == "WhatsApp") "15551234567" else "@username") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("qr_input_social_handle")
                            )
                        }
                    }

                    "WIFI" -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = wifiSsid,
                                onValueChange = { wifiSsid = it },
                                label = { Text("Network Name (SSID) *") },
                                placeholder = { Text("e.g. Shop_Guest_WiFi") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("qr_input_wifi_ssid")
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = wifiPassword,
                                onValueChange = { wifiPassword = it },
                                label = { Text("Wi-Fi Password") },
                                trailingIcon = {
                                    IconButton(onClick = { showWifiPassword = !showWifiPassword }) {
                                        Icon(
                                            imageVector = if (showWifiPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                },
                                visualTransformation = if (showWifiPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("qr_input_wifi_password")
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("WPA", "WEP", "nopass").forEach { sec ->
                                    FilterChip(
                                        selected = wifiSecurity == sec,
                                        onClick = { wifiSecurity = sec },
                                        label = { Text(if (sec == "nopass") "Open" else sec) }
                                    )
                                }
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
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = contactOrg,
                                onValueChange = { contactOrg = it },
                                label = { Text("Organization / Company") },
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
                                label = { Text("UPI Virtual Payment Address (VPA) *") },
                                placeholder = { Text("e.g. merchant@okhdfcbank") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("qr_input_upi_id")
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = upiName,
                                onValueChange = { upiName = it },
                                label = { Text("Payee Merchant Name") },
                                placeholder = { Text("e.g. Ramesh Grocery") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = upiAmount,
                                onValueChange = { upiAmount = it },
                                label = { Text("Fixed Amount (optional)") },
                                placeholder = { Text("e.g. 150.00") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
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
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = smsBody,
                                onValueChange = { smsBody = it },
                                label = { Text("Message Body") },
                                minLines = 3,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
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
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = emailSubject,
                                onValueChange = { emailSubject = it },
                                label = { Text("Subject Line") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = emailBody,
                                onValueChange = { emailBody = it },
                                label = { Text("Email Message Body") },
                                minLines = 3,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
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
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = geoLng,
                                    onValueChange = { geoLng = it },
                                    label = { Text("Longitude") },
                                    placeholder = { Text("-122.4194") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = geoLabel,
                                onValueChange = { geoLabel = it },
                                label = { Text("Place / Landmark Name (optional)") },
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
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = eventLocation,
                                onValueChange = { eventLocation = it },
                                label = { Text("Event Location") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
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
                                value = totpIssuer,
                                onValueChange = { totpIssuer = it },
                                label = { Text("Issuer / Service Name") },
                                placeholder = { Text("e.g. GitHub, Google, AWS") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))
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
                                value = totpSecret,
                                onValueChange = { totpSecret = it },
                                label = { Text("Base32 Secret Key *") },
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
                                label = { Text("Critical Medical Notes & Blood Group") },
                                placeholder = { Text("e.g. O+ Positive, Penicillin allergy") },
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

            } else {
                // TAB 1: DESIGN & STUDIO (ULTRA CUSTOMIZABLE)
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
                        "Classic Mono" to {
                            fgColorInt = AndroidColor.BLACK
                            bgColorInt = AndroidColor.WHITE
                            isTransparentBg = false
                            isGradientEnabled = false
                            dotStyle = BarcodeGenerator.DotStyle.SQUARE
                            eyeFrameStyle = BarcodeGenerator.EyeFrameStyle.SQUARE
                            eyePupilStyle = BarcodeGenerator.EyePupilStyle.SQUARE
                            customEyeColorInt = null
                        },
                        "Cyber Neon" to {
                            fgColorInt = AndroidColor.rgb(6, 182, 212)
                            bgColorInt = AndroidColor.rgb(15, 23, 42)
                            isTransparentBg = false
                            isGradientEnabled = true
                            gradientSecondaryColorInt = AndroidColor.rgb(236, 72, 153)
                            dotStyle = BarcodeGenerator.DotStyle.DOTS
                            eyeFrameStyle = BarcodeGenerator.EyeFrameStyle.ROUNDED
                            eyePupilStyle = BarcodeGenerator.EyePupilStyle.CIRCLE
                            customEyeColorInt = AndroidColor.rgb(236, 72, 153)
                            ecLevel = "H"
                        },
                        "Executive Navy & Gold" to {
                            fgColorInt = AndroidColor.rgb(15, 23, 42)
                            bgColorInt = AndroidColor.WHITE
                            isTransparentBg = false
                            isGradientEnabled = false
                            dotStyle = BarcodeGenerator.DotStyle.ROUNDED
                            eyeFrameStyle = BarcodeGenerator.EyeFrameStyle.ROUNDED
                            eyePupilStyle = BarcodeGenerator.EyePupilStyle.DIAMOND
                            customEyeColorInt = AndroidColor.rgb(217, 119, 6)
                            ecLevel = "H"
                        },
                        "Sunset Flame" to {
                            fgColorInt = AndroidColor.rgb(234, 88, 12)
                            bgColorInt = AndroidColor.rgb(254, 251, 246)
                            isTransparentBg = false
                            isGradientEnabled = true
                            gradientSecondaryColorInt = AndroidColor.rgb(219, 39, 119)
                            dotStyle = BarcodeGenerator.DotStyle.ROUNDED
                            eyeFrameStyle = BarcodeGenerator.EyeFrameStyle.CIRCLE
                            eyePupilStyle = BarcodeGenerator.EyePupilStyle.CIRCLE
                            customEyeColorInt = AndroidColor.rgb(190, 24, 93)
                            ecLevel = "H"
                        },
                        "Emerald Minimal" to {
                            fgColorInt = AndroidColor.rgb(6, 78, 59)
                            bgColorInt = AndroidColor.rgb(240, 253, 244)
                            isTransparentBg = false
                            isGradientEnabled = false
                            dotStyle = BarcodeGenerator.DotStyle.ROUNDED
                            eyeFrameStyle = BarcodeGenerator.EyeFrameStyle.ROUNDED
                            eyePupilStyle = BarcodeGenerator.EyePupilStyle.CIRCLE
                            customEyeColorInt = AndroidColor.rgb(16, 185, 129)
                            ecLevel = "H"
                        },
                        "Sticker Transparent" to {
                            fgColorInt = AndroidColor.BLACK
                            bgColorInt = AndroidColor.TRANSPARENT
                            isTransparentBg = true
                            isGradientEnabled = false
                            dotStyle = BarcodeGenerator.DotStyle.ROUNDED
                            eyeFrameStyle = BarcodeGenerator.EyeFrameStyle.ROUNDED
                            eyePupilStyle = BarcodeGenerator.EyePupilStyle.SQUARE
                            customEyeColorInt = null
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

                // Module & Dot Style
                Text(text = "Module & Dot Shape", style = MaterialTheme.typography.labelMedium, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BarcodeGenerator.DotStyle.values().forEach { ds ->
                        FilterChip(
                            selected = dotStyle == ds,
                            onClick = {
                                dotStyle = ds
                                if (ds != BarcodeGenerator.DotStyle.SQUARE && (ecLevel == "L" || ecLevel == "M")) {
                                    ecLevel = "H"
                                }
                            },
                            label = { Text(ds.label, maxLines = 1, softWrap = false) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Corner Eye Frames
                Text(text = "Corner Eye Frame Style", style = MaterialTheme.typography.labelMedium, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BarcodeGenerator.EyeFrameStyle.values().forEach { fs ->
                        FilterChip(
                            selected = eyeFrameStyle == fs,
                            onClick = {
                                eyeFrameStyle = fs
                                if (fs != BarcodeGenerator.EyeFrameStyle.SQUARE && (ecLevel == "L" || ecLevel == "M")) {
                                    ecLevel = "H"
                                }
                            },
                            label = { Text(fs.label, maxLines = 1, softWrap = false) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Inner Eye Pupil
                Text(text = "Inner Eye Pupil Shape", style = MaterialTheme.typography.labelMedium, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BarcodeGenerator.EyePupilStyle.values().forEach { ps ->
                        FilterChip(
                            selected = eyePupilStyle == ps,
                            onClick = {
                                eyePupilStyle = ps
                                if (ps != BarcodeGenerator.EyePupilStyle.SQUARE && (ecLevel == "L" || ecLevel == "M")) {
                                    ecLevel = "H"
                                }
                            },
                            label = { Text(ps.label, maxLines = 1, softWrap = false) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Custom Eye Color
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Corner Eye Color Accent", style = MaterialTheme.typography.labelMedium)
                    FilterChip(
                        selected = customEyeColorInt == null,
                        onClick = { customEyeColorInt = null },
                        label = { Text("Match Code", maxLines = 1, softWrap = false) }
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val eyeColors = listOf(
                        "Gold" to AndroidColor.rgb(217, 119, 6),
                        "Cyan" to AndroidColor.rgb(6, 182, 212),
                        "Magenta" to AndroidColor.rgb(219, 39, 119),
                        "Emerald" to AndroidColor.rgb(16, 185, 129),
                        "Indigo" to AndroidColor.rgb(79, 70, 229),
                        "Crimson" to AndroidColor.rgb(225, 29, 72),
                        "Amber" to AndroidColor.rgb(245, 158, 11)
                    )
                    eyeColors.forEach { (name, c) ->
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color(c), CircleShape)
                                .border(
                                    width = if (customEyeColorInt == c) 3.dp else 1.dp,
                                    color = if (customEyeColorInt == c) MaterialTheme.colorScheme.primary else Color.LightGray,
                                    shape = CircleShape
                                )
                                .clickable { customEyeColorInt = c }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Color Studio: Primary Code Color & Gradients
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Module Color Palette", style = MaterialTheme.typography.labelMedium)
                    FilterChip(
                        selected = isGradientEnabled,
                        onClick = { isGradientEnabled = !isGradientEnabled },
                        label = { Text("Gradient Mode", maxLines = 1, softWrap = false) }
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val primaryColors = listOf(
                        "Classic Black" to AndroidColor.BLACK,
                        "Slate Blue" to AndroidColor.rgb(30, 41, 59),
                        "Royal Indigo" to AndroidColor.rgb(30, 27, 75),
                        "Forest Green" to AndroidColor.rgb(6, 78, 59),
                        "Crimson Burgundy" to AndroidColor.rgb(74, 4, 78),
                        "Rich Espresso" to AndroidColor.rgb(39, 28, 25),
                        "Electric Blue" to AndroidColor.rgb(37, 99, 235),
                        "Sunset Orange" to AndroidColor.rgb(234, 88, 12),
                        "Dark Cyan" to AndroidColor.rgb(14, 116, 144),
                        "Deep Violet" to AndroidColor.rgb(109, 40, 217)
                    )
                    primaryColors.forEach { (name, c) ->
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

                if (isGradientEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Secondary Gradient Color", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val secondaryColors = listOf(
                            "Sky Blue" to AndroidColor.rgb(56, 189, 248),
                            "Hot Pink" to AndroidColor.rgb(244, 63, 94),
                            "Amber Gold" to AndroidColor.rgb(251, 191, 36),
                            "Mint Green" to AndroidColor.rgb(52, 211, 153),
                            "Purple" to AndroidColor.rgb(168, 85, 247),
                            "Cyan" to AndroidColor.rgb(34, 211, 238)
                        )
                        secondaryColors.forEach { (name, c) ->
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(Color(c), CircleShape)
                                    .border(
                                        width = if (gradientSecondaryColorInt == c) 3.dp else 1.dp,
                                        color = if (gradientSecondaryColorInt == c) MaterialTheme.colorScheme.primary else Color.LightGray,
                                        shape = CircleShape
                                    )
                                    .clickable { gradientSecondaryColorInt = c }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Canvas Background
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Canvas Background", style = MaterialTheme.typography.labelMedium)
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
                            "Soft Mint" to AndroidColor.rgb(240, 253, 244),
                            "Midnight Dark" to AndroidColor.rgb(15, 23, 42)
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
                                    color = if (c == AndroidColor.rgb(15, 23, 42)) Color.White else Color.Black,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Designer Frame & "Scan Me" Banner
                Text(text = "Designer Frame & Call-to-Action Banner", style = MaterialTheme.typography.labelMedium, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "None" to "",
                        "SCAN ME" to "SCAN ME",
                        "PAY HERE" to "PAY HERE",
                        "JOIN WI-FI" to "JOIN WI-FI",
                        "CONNECT" to "CONNECT",
                        "VISIT" to "VISIT SITE"
                    ).forEach { (label, txt) ->
                        FilterChip(
                            selected = bannerText == txt,
                            onClick = { bannerText = txt },
                            label = { Text(label, maxLines = 1, softWrap = false) }
                        )
                    }
                }

                if (bannerText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = bannerText,
                        onValueChange = { bannerText = it.take(24) },
                        label = { Text("Custom Banner Text") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = bannerPosition == BarcodeGenerator.BannerPosition.BOTTOM,
                            onClick = { bannerPosition = BarcodeGenerator.BannerPosition.BOTTOM },
                            label = { Text("Bottom Pill", maxLines = 1, softWrap = false) }
                        )
                        FilterChip(
                            selected = bannerPosition == BarcodeGenerator.BannerPosition.TOP,
                            onClick = { bannerPosition = BarcodeGenerator.BannerPosition.TOP },
                            label = { Text("Top Header", maxLines = 1, softWrap = false) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Center Emblem / Logo Badge
                Text(text = "Center Emblem Badge", style = MaterialTheme.typography.labelMedium, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BarcodeGenerator.EmblemType.values().forEach { emb ->
                        FilterChip(
                            selected = selectedEmblem == emb,
                            onClick = {
                                selectedEmblem = emb
                                if (emb != BarcodeGenerator.EmblemType.NONE && (ecLevel == "L" || ecLevel == "M")) {
                                    ecLevel = "H"
                                }
                            },
                            label = { Text(emb.label, maxLines = 1, softWrap = false) }
                        )
                    }
                }

                if (selectedEmblem == BarcodeGenerator.EmblemType.MONOGRAM) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = monogramInput,
                            onValueChange = { if (it.length <= 3) monogramInput = it.uppercase() },
                            label = { Text("Monogram (1-3 Letters)") },
                            placeholder = { Text("Q") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BarcodeGenerator.MonogramShape.values().forEach { shape ->
                            FilterChip(
                                selected = monogramShape == shape,
                                onClick = { monogramShape = shape },
                                label = { Text(shape.label, maxLines = 1, softWrap = false) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Error Correction & Quiet Zone
                Text(text = "Error Correction Level", style = MaterialTheme.typography.labelMedium, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "L" to "7% (Compact)",
                        "M" to "15% (Standard)",
                        "Q" to "25% (High)",
                        "H" to "30% (Max Scannability)"
                    ).forEach { (level, desc) ->
                        FilterChip(
                            selected = ecLevel == level,
                            onClick = { ecLevel = level },
                            label = { Text("$level: $desc", maxLines = 1, softWrap = false) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(text = "Quiet Zone Border Margin", style = MaterialTheme.typography.labelMedium, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        0 to "Flush (0)",
                        1 to "Compact (1)",
                        2 to "Standard (2)",
                        4 to "Wide (4)"
                    ).forEach { (m, name) ->
                        FilterChip(
                            selected = marginVal == m,
                            onClick = { marginVal = m },
                            label = { Text(name, maxLines = 1, softWrap = false) }
                        )
                    }
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
                Text("Save to Library", style = MaterialTheme.typography.titleMedium, maxLines = 1, softWrap = false)
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
                            qrConfig = currentQrConfig
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
                    Text("Print", maxLines = 1, softWrap = false)
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
                    Text("Share", maxLines = 1, softWrap = false)
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

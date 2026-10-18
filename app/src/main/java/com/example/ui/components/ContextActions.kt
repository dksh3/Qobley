package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.provider.ContactsContract
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ParsedPayload
import com.example.scanner.PayloadParser
import com.example.util.WifiConnectHelper

@Composable
fun ContextActionButtons(
    rawContent: String,
    format: String,
    modifier: Modifier = Modifier,
    onToast: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val parsed = PayloadParser.parse(rawContent, format)

    Column(modifier = modifier.fillMaxWidth()) {
        // Content Summary Box - Clean, quiet, without heavy card borders
        PayloadSummaryBox(parsed = parsed)

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Dominant Primary Action (Filled)
        when (parsed) {
            is ParsedPayload.Url -> {
                PrimaryActionButton(
                    icon = Icons.Outlined.OpenInNew,
                    label = "Open Website",
                    testTag = "action_open_url",
                    onClick = { openUrl(context, parsed.url, onToast) }
                )
            }

            is ParsedPayload.Phone -> {
                PrimaryActionButton(
                    icon = Icons.Outlined.Call,
                    label = "Call",
                    testTag = "action_call_phone",
                    onClick = { dialPhone(context, parsed.rawNumber, onToast) }
                )
            }

            is ParsedPayload.Email -> {
                PrimaryActionButton(
                    icon = Icons.Outlined.Email,
                    label = "Send Email",
                    testTag = "action_compose_email",
                    onClick = { sendEmail(context, parsed.email, parsed.subject, parsed.body, onToast) }
                )
            }

            is ParsedPayload.Wifi -> {
                PrimaryActionButton(
                    icon = Icons.Outlined.Wifi,
                    label = "Connect Wi-Fi",
                    testTag = "action_connect_wifi",
                    onClick = {
                        WifiConnectHelper.connectToWifi(
                            context = context,
                            ssid = parsed.ssid,
                            password = parsed.password,
                            security = parsed.security,
                            hidden = parsed.isHidden,
                            onFeedback = onToast
                        )
                    }
                )
            }

            is ParsedPayload.Contact -> {
                PrimaryActionButton(
                    icon = Icons.Outlined.PersonAdd,
                    label = "Add Contact",
                    testTag = "action_save_contact",
                    onClick = {
                        addContact(
                            context = context,
                            name = parsed.name,
                            phone = parsed.phone,
                            email = parsed.email,
                            onToast = onToast
                        )
                    }
                )
            }

            is ParsedPayload.Payment -> {
                PrimaryActionButton(
                    icon = Icons.Outlined.Payment,
                    label = if (!parsed.amount.isNullOrBlank()) "Pay ₹${parsed.amount}" else "Pay",
                    testTag = "action_open_upi",
                    onClick = { openUri(context, parsed.rawUri, onToast) }
                )
            }

            is ParsedPayload.Calendar -> {
                PrimaryActionButton(
                    icon = Icons.Outlined.CalendarToday,
                    label = "Add Event",
                    testTag = "action_add_calendar",
                    onClick = { addCalendarEvent(context, parsed.title, parsed.description, parsed.location, onToast) }
                )
            }

            is ParsedPayload.Geo -> {
                PrimaryActionButton(
                    icon = Icons.Outlined.LocationOn,
                    label = "Open Map",
                    testTag = "action_open_map",
                    onClick = { openGeo(context, parsed.latitude, parsed.longitude, parsed.query, onToast) }
                )
            }

            is ParsedPayload.Sms -> {
                PrimaryActionButton(
                    icon = Icons.Outlined.Message,
                    label = "Send SMS",
                    testTag = "action_send_sms",
                    onClick = { sendSms(context, parsed.number, parsed.message, onToast) }
                )
            }

            is ParsedPayload.BarcodeProduct -> {
                PrimaryActionButton(
                    icon = Icons.Outlined.Search,
                    label = "Search Product",
                    testTag = "action_search_product",
                    onClick = { searchProduct(context, parsed.code, onToast) }
                )
            }

            else -> {
                PrimaryActionButton(
                    icon = Icons.Outlined.ContentCopy,
                    label = "Copy",
                    testTag = "action_copy_primary",
                    onClick = {
                        clipboardManager.setText(AnnotatedString(rawContent))
                        onToast("Copied to clipboard")
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Secondary & Tertiary Actions Row (Understated Text/Icon buttons, 48dp touch targets)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TertiaryActionButton(
                icon = Icons.Outlined.ContentCopy,
                label = "Copy",
                testTag = "action_universal_copy",
                onClick = {
                    clipboardManager.setText(AnnotatedString(rawContent))
                    onToast("Copied to clipboard")
                }
            )

            Spacer(modifier = Modifier.width(16.dp))

            TertiaryActionButton(
                icon = Icons.Outlined.Share,
                label = "Share",
                testTag = "action_universal_share",
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, rawContent)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share"))
                }
            )

            if (parsed is ParsedPayload.Wifi && parsed.password.isNotEmpty()) {
                Spacer(modifier = Modifier.width(16.dp))
                TertiaryActionButton(
                    icon = Icons.Outlined.ContentCopy,
                    label = "Copy Pass",
                    testTag = "action_copy_wifi_pass",
                    onClick = {
                        clipboardManager.setText(AnnotatedString(parsed.password))
                        onToast("Wi-Fi password copied")
                    }
                )
            } else if (parsed is ParsedPayload.Phone) {
                Spacer(modifier = Modifier.width(16.dp))
                TertiaryActionButton(
                    icon = Icons.Outlined.PersonAdd,
                    label = "Add Contact",
                    testTag = "action_add_contact",
                    onClick = { addContact(context, name = "", phone = parsed.rawNumber, onToast = onToast) }
                )
            } else if (parsed is ParsedPayload.Url) {
                Spacer(modifier = Modifier.width(16.dp))
                TertiaryActionButton(
                    icon = Icons.Outlined.Search,
                    label = "Web Search",
                    testTag = "action_web_search",
                    onClick = { searchProduct(context, parsed.url, onToast) }
                )
            } else if (parsed is ParsedPayload.BarcodeProduct) {
                Spacer(modifier = Modifier.width(16.dp))
                TertiaryActionButton(
                    icon = Icons.Outlined.Search,
                    label = "DuckDuckGo",
                    testTag = "action_search_ddg",
                    onClick = { searchDuckDuckGo(context, parsed.code, onToast) }
                )
            }
        }
    }
}

@Composable
private fun PayloadSummaryBox(
    parsed: ParsedPayload
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val (badgeIcon, badgeLabel) = when (parsed) {
                is ParsedPayload.Url -> Icons.Outlined.Language to "Website Link"
                is ParsedPayload.Phone -> Icons.Outlined.Call to "Phone Number"
                is ParsedPayload.Email -> Icons.Outlined.Email to "Email Address"
                is ParsedPayload.Wifi -> Icons.Outlined.Wifi to "Wi-Fi Network"
                is ParsedPayload.Contact -> Icons.Outlined.PersonAdd to "Contact Card"
                is ParsedPayload.Payment -> Icons.Outlined.Payment to "Payment Request"
                is ParsedPayload.BarcodeProduct -> Icons.Outlined.Search to "Product Barcode"
                is ParsedPayload.Text -> Icons.Outlined.ContentCopy to "Plain Text"
                else -> Icons.Outlined.ContentCopy to "Code Content"
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = badgeIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = badgeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (parsed) {
                is ParsedPayload.Url -> {
                    Text(
                        text = parsed.domain,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = parsed.url,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3
                    )
                }

                is ParsedPayload.Phone -> {
                    Text(
                        text = parsed.formattedNumber,
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                is ParsedPayload.Email -> {
                    Text(
                        text = parsed.email,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (!parsed.subject.isNullOrBlank()) {
                        Text(
                            text = "Subject: ${parsed.subject}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                is ParsedPayload.Wifi -> {
                    Text(
                        text = parsed.ssid,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Security: ${parsed.security}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (parsed.password.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Password: ${parsed.password}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                is ParsedPayload.Contact -> {
                    Text(
                        text = parsed.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (!parsed.phone.isNullOrBlank()) {
                        Text(
                            text = "Phone: ${parsed.phone}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (!parsed.email.isNullOrBlank()) {
                        Text(
                            text = "Email: ${parsed.email}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                is ParsedPayload.Payment -> {
                    if (!parsed.amount.isNullOrBlank()) {
                        Text(
                            text = "₹${parsed.amount}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = "UPI ID: ${parsed.upiId}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (!parsed.name.isNullOrBlank()) {
                        Text(
                            text = "Payee: ${parsed.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                is ParsedPayload.BarcodeProduct -> {
                    Text(
                        text = parsed.code,
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Format: ${parsed.format.replace("_", " ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                is ParsedPayload.Text -> {
                    Text(
                        text = parsed.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${parsed.charCount} characters",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                is ParsedPayload.Unknown -> {
                    Text(
                        text = parsed.raw,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                else -> {
                    Text(
                        text = "Information detected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

@Composable
private fun PrimaryActionButton(
    icon: ImageVector,
    label: String,
    testTag: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag(testTag)
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TertiaryActionButton(
    icon: ImageVector,
    label: String,
    testTag: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .height(44.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// Intent dispatchers
private fun openUrl(context: Context, url: String, onToast: (String) -> Unit) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        onToast("Could not open browser")
    }
}

private fun dialPhone(context: Context, phone: String, onToast: (String) -> Unit) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
        context.startActivity(intent)
    } catch (e: Exception) {
        onToast("Could not open dialer")
    }
}

private fun sendEmail(context: Context, email: String, subject: String?, body: String?, onToast: (String) -> Unit) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            if (subject != null) putExtra(Intent.EXTRA_SUBJECT, subject)
            if (body != null) putExtra(Intent.EXTRA_TEXT, body)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        onToast("Could not open email app")
    }
}

private fun addContact(
    context: Context,
    name: String,
    phone: String? = null,
    email: String? = null,
    onToast: (String) -> Unit
) {
    try {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            type = ContactsContract.RawContacts.CONTENT_TYPE
            if (name.isNotEmpty()) putExtra(ContactsContract.Intents.Insert.NAME, name)
            if (!phone.isNullOrEmpty()) putExtra(ContactsContract.Intents.Insert.PHONE, phone)
            if (!email.isNullOrEmpty()) putExtra(ContactsContract.Intents.Insert.EMAIL, email)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        onToast("Could not open contacts app")
    }
}

private fun openUri(context: Context, uriString: String, onToast: (String) -> Unit) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString))
        context.startActivity(intent)
    } catch (e: Exception) {
        onToast("No app available to handle this request")
    }
}

private fun searchProduct(context: Context, query: String, onToast: (String) -> Unit) {
    try {
        val searchUrl = "https://www.google.com/search?q=" + Uri.encode(query)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl))
        context.startActivity(intent)
    } catch (e: Exception) {
        onToast("Could not open browser")
    }
}

private fun searchDuckDuckGo(context: Context, query: String, onToast: (String) -> Unit) {
    try {
        val searchUrl = "https://duckduckgo.com/?q=" + Uri.encode(query)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl))
        context.startActivity(intent)
    } catch (e: Exception) {
        onToast("Could not open browser")
    }
}

private fun addCalendarEvent(
    context: Context,
    title: String,
    description: String?,
    location: String?,
    onToast: (String) -> Unit
) {
    try {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, title)
            if (!description.isNullOrEmpty()) putExtra(CalendarContract.Events.DESCRIPTION, description)
            if (!location.isNullOrEmpty()) putExtra(CalendarContract.Events.EVENT_LOCATION, location)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        onToast("Could not open calendar app")
    }
}

private fun openGeo(
    context: Context,
    latitude: Double,
    longitude: Double,
    query: String?,
    onToast: (String) -> Unit
) {
    try {
        val uriStr = if (query != null) "geo:$latitude,$longitude?q=${Uri.encode(query)}" else "geo:$latitude,$longitude"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriStr))
        context.startActivity(intent)
    } catch (e: Exception) {
        onToast("Could not open map app")
    }
}

private fun sendSms(
    context: Context,
    number: String,
    message: String?,
    onToast: (String) -> Unit
) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$number")
            if (!message.isNullOrEmpty()) putExtra("sms_body", message)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        onToast("Could not open messaging app")
    }
}

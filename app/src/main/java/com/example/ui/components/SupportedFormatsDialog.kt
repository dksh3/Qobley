package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.ViewWeek
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class FormatSpec(
    val format: String,
    val category: String,
    val displayName: String,
    val description: String,
    val commonUsage: String,
    val is2D: Boolean = true
)

object SupportedFormatsCatalog {
    val allFormats = listOf(
        FormatSpec(
            format = "QR_CODE",
            category = "2D Matrix",
            displayName = "QR Code (Quick Response)",
            description = "High-speed 2D matrix supporting up to 7,089 numeric or 4,296 alphanumeric characters.",
            commonUsage = "Websites, Wi-Fi credentials, vCard contacts, UPI / crypto payments, event tickets.",
            is2D = true
        ),
        FormatSpec(
            format = "AZTEC",
            category = "2D Matrix",
            displayName = "Aztec Code",
            description = "High-density 2D matrix with a central square bullseye finder pattern, requiring zero quiet zone.",
            commonUsage = "Airline mobile boarding passes, European railway e-tickets, healthcare registration.",
            is2D = true
        ),
        FormatSpec(
            format = "DATA_MATRIX",
            category = "2D Matrix",
            displayName = "Data Matrix",
            description = "Compact 2D code encoding immense data in micro millimeter footprints with ECC 200 error correction.",
            commonUsage = "Micro-electronics, surgical instruments, aerospace defense parts, pharmaceutical packaging.",
            is2D = true
        ),
        FormatSpec(
            format = "PDF_417",
            category = "2D Matrix",
            displayName = "PDF-417 (Portable Data File)",
            description = "Multi-row stacked 2D barcode capable of holding over a kilobyte of raw machine-readable data.",
            commonUsage = "Government driver licenses, state ID cards, FedEx/USPS postal shipping labels, airline passes.",
            is2D = true
        ),
        FormatSpec(
            format = "CODE_128",
            category = "1D Linear",
            displayName = "Code 128",
            description = "High-density universal linear barcode encoding all 128 ASCII characters (uppercase, lowercase, symbols).",
            commonUsage = "Global e-commerce fulfillment, warehouse logistics, container tracking, inventory serials.",
            is2D = false
        ),
        FormatSpec(
            format = "EAN_13",
            category = "1D Linear",
            displayName = "EAN-13 (International Article Number)",
            description = "13-digit standard retail barcode used across Europe, Asia, Latin America, and globally.",
            commonUsage = "Supermarket point-of-sale scanning, consumer packaged goods, ISBN books.",
            is2D = false
        ),
        FormatSpec(
            format = "UPC_A",
            category = "1D Linear",
            displayName = "UPC-A (Universal Product Code)",
            description = "Standard 12-digit linear barcode governing retail and consumer merchandise across North America.",
            commonUsage = "Retail checkout, grocery stores, department stores throughout the United States and Canada.",
            is2D = false
        ),
        FormatSpec(
            format = "CODE_39",
            category = "1D Linear",
            displayName = "Code 39 (3 of 9)",
            description = "Self-checking alphanumeric barcode standard widely mandated across industrial manufacturing.",
            commonUsage = "Automotive parts assembly, US Department of Defense inventory (LOGMARS), badge access.",
            is2D = false
        ),
        FormatSpec(
            format = "CODE_93",
            category = "1D Linear",
            displayName = "Code 93",
            description = "Higher density companion to Code 39 offering dual check digits for increased read reliability.",
            commonUsage = "Postal delivery tracking, electronic hardware components, laboratory specimens.",
            is2D = false
        ),
        FormatSpec(
            format = "EAN_8",
            category = "1D Linear",
            displayName = "EAN-8",
            description = "Compressed 8-digit retail barcode designed specifically for miniature products where EAN-13 is too wide.",
            commonUsage = "Chewing gum, cosmetics, small confectionery, compact retail accessories.",
            is2D = false
        ),
        FormatSpec(
            format = "UPC_E",
            category = "1D Linear",
            displayName = "UPC-E (Zero-Suppressed)",
            description = "6-digit condensed variation of UPC-A eliminating redundant zeros for compact packaging.",
            commonUsage = "Candy bars, travel toiletries, soda cans, North American pocket packaging.",
            is2D = false
        ),
        FormatSpec(
            format = "CODABAR",
            category = "1D Linear",
            displayName = "Codabar (NW-7)",
            description = "Discrete self-checking linear barcode with distinct start/stop characters (A, B, C, D).",
            commonUsage = "Blood banks, medical diagnostic laboratories, library book systems, air express waybills.",
            is2D = false
        ),
        FormatSpec(
            format = "ITF",
            category = "1D Linear",
            displayName = "ITF (Interleaved 2 of 5)",
            description = "Continuous 2-width numeric barcode encoding digits in pairs across alternating bars and spaces.",
            commonUsage = "Corrugated cardboard shipping cartons, wholesale warehouse master cases, distribution pallets.",
            is2D = false
        )
    )
}

@Composable
fun SupportedFormatsDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "13 Formats Supported",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Instant 60 FPS hardware-accelerated decoding",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Continuous Multi-Format Engine",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Qobley feeds every camera frame directly into 13 concurrent decoders. No format switching required—point at any barcode, QR, ticket, or micro-matrix and it decodes instantly.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                items(SupportedFormatsCatalog.allFormats) { spec ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (spec.is2D) Icons.Outlined.QrCode else Icons.Outlined.ViewWeek,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = spec.displayName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Text(
                                        text = spec.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = spec.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.Top) {
                                Text(
                                    text = "Common in: ",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = spec.commonUsage,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("dismiss_formats_dialog")
            ) {
                Text("Got It")
            }
        }
    )
}

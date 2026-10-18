package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.scanner.ScannedItem
import kotlin.math.roundToInt

@Composable
fun QobleyScannerOverlay(
    isTorchOn: Boolean,
    onToggleTorch: () -> Unit,
    onSwitchCamera: () -> Unit,
    onOpenGallery: () -> Unit,
    onBack: () -> Unit,
    currentZoom: Float = 1f,
    onToggleZoom: () -> Unit = {},
    detectedHighlight: ScannedItem? = null,
    multipleDetections: List<ScannedItem>? = null,
    onSelectDetection: (ScannedItem) -> Unit = {},
    onShowFormats: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenWidthPx = constraints.maxWidth.toFloat()
        val screenHeightPx = constraints.maxHeight.toFloat()

        // 1. Detected Code Highlight (Result Indicator - Appears ONLY after detection)
        if (detectedHighlight != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val bounds = detectedHighlight.normalizedBounds
                if (bounds != null) {
                    val left = bounds.left * screenWidthPx
                    val top = bounds.top * screenHeightPx
                    val width = ((bounds.right - bounds.left) * screenWidthPx).coerceAtLeast(80.dp.toPx())
                    val height = ((bounds.bottom - bounds.top) * screenHeightPx).coerceAtLeast(80.dp.toPx())

                    // Subtle, crisp outline around the detected code
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.9f),
                        topLeft = Offset(left, top),
                        size = Size(width, height),
                        cornerRadius = CornerRadius(14.dp.toPx()),
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                } else {
                    // Fallback center indicator if precise bounds aren't available
                    val cx = detectedHighlight.normalizedX * screenWidthPx
                    val cy = detectedHighlight.normalizedY * screenHeightPx
                    val boxSize = 120.dp.toPx()

                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.85f),
                        topLeft = Offset(cx - boxSize / 2f, cy - boxSize / 2f),
                        size = Size(boxSize, boxSize),
                        cornerRadius = CornerRadius(14.dp.toPx()),
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                }
            }
        }

        // 2. Multiple Code Interactive Markers (When 2+ codes appear in frame)
        if (multipleDetections != null && multipleDetections.size > 1) {
            multipleDetections.forEachIndexed { index, item ->
                val xPx = (item.normalizedX * screenWidthPx).roundToInt()
                val yPx = (item.normalizedY * screenHeightPx).roundToInt()

                Box(
                    modifier = Modifier
                        .offset { IntOffset(xPx - 24.dp.roundToPx(), yPx - 24.dp.roundToPx()) }
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, Color(0xFF18181B), CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onSelectDetection(item)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            // Floating badge indicating multiple codes
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 96.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xCC000000),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "${multipleDetections.size} codes found · Tap to select",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // 3. Minimal Top Bar: Back, 13 Formats Active badge & Gallery
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            Surface(
                shape = CircleShape,
                color = Color(0x66000000),
                modifier = Modifier.size(44.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("scanner_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // 13 Formats Active Advertisement & Specifications Pill
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0x77000000),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onShowFormats() }
                    .testTag("scanner_formats_badge")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(Color(0xFF10B981), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "13 Formats Active",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            // Gallery / Photo Library button
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0x66000000),
                modifier = Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .clickable { onOpenGallery() }
                    .testTag("scanner_gallery_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Gallery",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Gallery",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }

        // 4. Minimal Bottom Controls (Flashlight, Zoom, Switch Camera)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color(0x66000000),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Flashlight Toggle
                    IconButton(
                        onClick = onToggleTorch,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("scanner_torch_button")
                    ) {
                        Icon(
                            imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = if (isTorchOn) "Turn off flashlight" else "Turn on flashlight",
                            tint = if (isTorchOn) Color(0xFFFBBF24) else Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Zoom Toggle (1x / 2x)
                    Surface(
                        shape = CircleShape,
                        color = if (currentZoom > 1.4f) Color(0xCCFFFFFF) else Color(0x33FFFFFF),
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable { onToggleZoom() }
                            .testTag("scanner_zoom_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (currentZoom > 1.4f) "2×" else "1×",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (currentZoom > 1.4f) Color.Black else Color.White
                            )
                        }
                    }

                    // Camera Switch (Front/Back)
                    IconButton(
                        onClick = onSwitchCamera,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("scanner_switch_camera_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Switch camera",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

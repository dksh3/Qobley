package com.example

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.CodeItem
import com.example.data.model.ParsedPayload
import com.example.data.repository.CodeRepository
import com.example.data.repository.SettingsRepository
import com.example.scanner.BarcodeDecoder
import com.example.scanner.DetectedItem
import com.example.scanner.PayloadParser
import com.example.util.CodeExportHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val repository = CodeRepository(db.codeDao())
    val settingsRepo = SettingsRepository(application)

    // Code flows
    val allCodes = repository.getAllCodes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scannedHistory = repository.getScannedHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedLibrary = repository.getSavedLibrary()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites = repository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentCodes = repository.getRecent(10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Currently active scan result (for bottom sheet or detail view)
    private val _activeScanResult = MutableStateFlow<CodeItem?>(null)
    val activeScanResult: StateFlow<CodeItem?> = _activeScanResult.asStateFlow()

    // Navigation request from intent / deep links / shortcuts
    sealed class NavigationRequest {
        object Scanner : NavigationRequest()
        object ScanImage : NavigationRequest()
        object ScanDocument : NavigationRequest()
        object CreateTab : NavigationRequest()
        data class CreateQr(val prefilledText: String? = null, val initialType: String = "URL") : NavigationRequest()
        object CreateBarcode : NavigationRequest()
        object CreateFromClipboard : NavigationRequest()
        object History : NavigationRequest()
        object Saved : NavigationRequest()
        data class ViewDetail(val codeId: Long) : NavigationRequest()
        data class ShareCode(val codeId: Long) : NavigationRequest()
    }

    private val _navigationRequest = MutableStateFlow<NavigationRequest?>(null)
    val navigationRequest: StateFlow<NavigationRequest?> = _navigationRequest.asStateFlow()

    fun postNavigationRequest(req: NavigationRequest) {
        _navigationRequest.value = req
    }

    fun clearNavigationRequest() {
        _navigationRequest.value = null
    }

    // Multiple detections sheet
    private val _multipleDetections = MutableStateFlow<List<DetectedItem>?>(null)
    val multipleDetections: StateFlow<List<DetectedItem>?> = _multipleDetections.asStateFlow()

    // Batch scan session
    private val _batchItems = MutableStateFlow<List<CodeItem>>(emptyList())
    val batchItems: StateFlow<List<CodeItem>> = _batchItems.asStateFlow()

    // Ephemeral Toast message
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Multi-select batch actions state
    private val _selectedItemIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedItemIds: StateFlow<Set<Long>> = _selectedItemIds.asStateFlow()

    // Sort order enum
    enum class SortOrder(val label: String) {
        NEWEST("Newest first"),
        OLDEST("Oldest first"),
        RECENTLY_USED("Recently updated"),
        ALPHABETICAL("Alphabetical (A–Z)"),
        TYPE("By Code Type")
    }

    fun toggleSelectItem(id: Long) {
        val current = _selectedItemIds.value
        _selectedItemIds.value = if (current.contains(id)) current - id else current + id
    }

    fun selectAll(ids: List<Long>) {
        _selectedItemIds.value = ids.toSet()
    }

    fun clearSelection() {
        _selectedItemIds.value = emptySet()
    }

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun setActiveScanResult(item: CodeItem?) {
        _activeScanResult.value = item
    }

    fun clearMultipleDetections() {
        _multipleDetections.value = null
    }

    // Handles live or picked scan result
    fun handleScannedCode(content: String, format: String, source: String = "SCANNED") {
        triggerHapticFeedback()

        val parsed = PayloadParser.parse(content, format)
        val title = when (parsed) {
            is ParsedPayload.Url -> parsed.domain
            is ParsedPayload.Phone -> "Phone: ${parsed.formattedNumber}"
            is ParsedPayload.Email -> "Email: ${parsed.email}"
            is ParsedPayload.Wifi -> "Wi-Fi: ${parsed.ssid}"
            is ParsedPayload.Contact -> parsed.name
            is ParsedPayload.Payment -> "Pay: ${parsed.name ?: parsed.upiId}"
            is ParsedPayload.BarcodeProduct -> "${format.replace("_", " ")}: ${parsed.code}"
            is ParsedPayload.Text -> if (parsed.content.length > 25) parsed.content.take(25) + "..." else parsed.content
            else -> content.take(30)
        }

        val category = when (parsed) {
            is ParsedPayload.Wifi -> "Wi-Fi"
            is ParsedPayload.Contact, is ParsedPayload.Phone -> "Contacts"
            is ParsedPayload.Payment -> "Payments"
            is ParsedPayload.BarcodeProduct -> "Products"
            is ParsedPayload.Url -> "Work"
            else -> "Other"
        }

        val type = if (format == "QR_CODE") "QR" else "BARCODE"

        val codeItem = CodeItem(
            type = type,
            format = format,
            content = content,
            title = title,
            category = category,
            source = source,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            repository.saveCode(codeItem)
            com.example.widget.QobleyWidgetUpdater.updateAllWidgets(getApplication())

            // Instantly execute primary action (Wi-Fi connect, URL open, UPI pay, Dial, etc.)
            executeInstantScanAction(parsed, content)
        }
    }

    private fun executeInstantScanAction(parsed: ParsedPayload, rawContent: String) {
        val app = getApplication<Application>()
        when (parsed) {
            is ParsedPayload.Wifi -> {
                com.example.util.WifiConnectHelper.connectToWifi(
                    context = app,
                    ssid = parsed.ssid,
                    password = parsed.password,
                    security = parsed.security,
                    hidden = parsed.isHidden,
                    onFeedback = { showToast(it) }
                )
            }
            is ParsedPayload.Url -> {
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(parsed.url)).apply {
                        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    app.startActivity(intent)
                    showToast("Opening ${parsed.domain}...")
                } catch (_: Exception) {
                    showToast("Could not open ${parsed.url}")
                }
            }
            is ParsedPayload.Payment -> {
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(parsed.rawUri)).apply {
                        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    val chooser = android.content.Intent.createChooser(intent, "Pay with...").apply {
                        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    app.startActivity(chooser)
                    val amountStr = if (!parsed.amount.isNullOrBlank()) " ₹${parsed.amount}" else ""
                    showToast("Opening payment app$amountStr...")
                } catch (_: Exception) {
                    showToast("No payment app found")
                }
            }
            is ParsedPayload.Phone -> {
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL, Uri.parse("tel:${parsed.rawNumber}")).apply {
                        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    app.startActivity(intent)
                    showToast("Dialing ${parsed.formattedNumber}...")
                } catch (_: Exception) {
                    showToast("Could not open dialer")
                }
            }
            is ParsedPayload.Email -> {
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:${parsed.email}")
                        if (parsed.subject != null) putExtra(android.content.Intent.EXTRA_SUBJECT, parsed.subject)
                        if (parsed.body != null) putExtra(android.content.Intent.EXTRA_TEXT, parsed.body)
                        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    app.startActivity(intent)
                    showToast("Opening email to ${parsed.email}...")
                } catch (_: Exception) {
                    showToast("Could not open email app")
                }
            }
            is ParsedPayload.Sms -> {
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("smsto:${parsed.number}")
                        if (parsed.message != null) putExtra("sms_body", parsed.message)
                        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    app.startActivity(intent)
                    showToast("Opening SMS to ${parsed.number}...")
                } catch (_: Exception) {
                    showToast("Could not open SMS app")
                }
            }
            is ParsedPayload.Geo -> {
                try {
                    val geoUri = if (parsed.query != null) {
                        "geo:0,0?q=" + Uri.encode(parsed.query)
                    } else {
                        "geo:${parsed.latitude},${parsed.longitude}"
                    }
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(geoUri)).apply {
                        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    app.startActivity(intent)
                    showToast("Opening map...")
                } catch (_: Exception) {
                    showToast("Could not open map")
                }
            }
            is ParsedPayload.Contact -> {
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_INSERT).apply {
                        type = android.provider.ContactsContract.RawContacts.CONTENT_TYPE
                        if (parsed.name.isNotEmpty()) putExtra(android.provider.ContactsContract.Intents.Insert.NAME, parsed.name)
                        if (!parsed.phone.isNullOrEmpty()) putExtra(android.provider.ContactsContract.Intents.Insert.PHONE, parsed.phone)
                        if (!parsed.email.isNullOrEmpty()) putExtra(android.provider.ContactsContract.Intents.Insert.EMAIL, parsed.email)
                        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    app.startActivity(intent)
                    showToast("Adding contact: ${parsed.name}")
                } catch (_: Exception) {
                    showToast("Could not open contacts app")
                }
            }
            is ParsedPayload.Calendar -> {
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_INSERT).apply {
                        data = android.provider.CalendarContract.Events.CONTENT_URI
                        putExtra(android.provider.CalendarContract.Events.TITLE, parsed.title)
                        if (parsed.description != null) putExtra(android.provider.CalendarContract.Events.DESCRIPTION, parsed.description)
                        if (parsed.location != null) putExtra(android.provider.CalendarContract.Events.EVENT_LOCATION, parsed.location)
                        if (parsed.startTime != null) putExtra(android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME, parsed.startTime)
                        if (parsed.endTime != null) putExtra(android.provider.CalendarContract.EXTRA_EVENT_END_TIME, parsed.endTime)
                        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    app.startActivity(intent)
                    showToast("Adding event: ${parsed.title}")
                } catch (_: Exception) {
                    showToast("Could not open calendar")
                }
            }
            is ParsedPayload.BarcodeProduct -> {
                val clipManager = app.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                clipManager?.setPrimaryClip(android.content.ClipData.newPlainText("Barcode", parsed.code))
                try {
                    val searchUrl = "https://www.google.com/search?q=" + Uri.encode(parsed.code)
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(searchUrl)).apply {
                        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    app.startActivity(intent)
                    showToast("Product code copied: ${parsed.code}")
                } catch (_: Exception) {
                    showToast("Barcode copied: ${parsed.code}")
                }
            }
            else -> {
                val clipManager = app.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                clipManager?.setPrimaryClip(android.content.ClipData.newPlainText("Scanned Text", rawContent))
                val preview = if (rawContent.length > 25) rawContent.take(25) + "..." else rawContent
                showToast("Copied to clipboard: $preview")
            }
        }
    }

    // Handles batch scan item
    fun addBatchItem(content: String, format: String) {
        val current = _batchItems.value
        val preventDuplicates = settingsRepo.duplicatePrevention.value
        if (preventDuplicates && current.any { it.content == content }) {
            return
        }

        triggerHapticFeedback()
        val type = if (format == "QR_CODE") "QR" else "BARCODE"
        val item = CodeItem(
            type = type,
            format = format,
            content = content,
            title = content.take(30),
            source = "BATCH_SCANNED",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        _batchItems.value = listOf(item) + current
    }

    fun clearBatch() {
        _batchItems.value = emptyList()
    }

    fun saveBatchToHistory() {
        val items = _batchItems.value
        if (items.isEmpty()) return
        viewModelScope.launch {
            repository.saveBatch(items)
            showToast("Saved ${items.size} batch scans to history")
            _batchItems.value = emptyList()
        }
    }

    // Scan image from URI (Gallery or Shared Intent)
    fun scanImageUri(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap == null) {
                    showToast("Could not read image")
                    return@launch
                }

                val detected = BarcodeDecoder.decodeBitmap(bitmap)
                withContext(Dispatchers.Main) {
                    when {
                        detected.isEmpty() -> {
                            showToast("No QR code or barcode found in this image")
                        }
                        detected.size == 1 -> {
                            val item = detected.first()
                            handleScannedCode(item.content, item.format, source = "IMPORTED")
                        }
                        else -> {
                            // Multiple items found! Show selection list
                            _multipleDetections.value = detected
                        }
                    }
                }
            } catch (e: Exception) {
                showToast("Error scanning image: ${e.message}")
            }
        }
    }

    // Toggle favorite
    fun toggleFavorite(item: CodeItem) {
        triggerHapticFeedback()
        viewModelScope.launch {
            repository.toggleFavorite(item)
            val isNowFav = !item.isFavorite
            com.example.widget.QobleyWidgetUpdater.updateAllWidgets(getApplication())
            showToast(if (isNowFav) "Added to favorites" else "Removed from favorites")
            if (_activeScanResult.value?.id == item.id) {
                _activeScanResult.value = item.copy(isFavorite = isNowFav)
            }
        }
    }

    // Toggle saved to library
    fun toggleSaved(item: CodeItem) {
        triggerHapticFeedback()
        viewModelScope.launch {
            repository.toggleSaved(item)
            val isNowSaved = !item.isSaved
            com.example.widget.QobleyWidgetUpdater.updateAllWidgets(getApplication())
            showToast(if (isNowSaved) "Saved to Library" else "Removed from Library")
            if (_activeScanResult.value?.id == item.id) {
                _activeScanResult.value = item.copy(isSaved = isNowSaved)
            }
        }
    }

    // Update code title, category, and notes
    fun updateCode(item: CodeItem, title: String, category: String, notes: String? = item.notes) {
        viewModelScope.launch {
            val updated = item.copy(
                title = title,
                category = category,
                notes = notes,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateCode(updated)
            com.example.widget.QobleyWidgetUpdater.updateAllWidgets(getApplication())
            if (_activeScanResult.value?.id == item.id) {
                _activeScanResult.value = updated
            }
            showToast("Saved")
        }
    }

    // Update only notes
    fun updateNotes(item: CodeItem, notes: String?) {
        viewModelScope.launch {
            repository.updateNotes(item.id, notes)
            val updated = item.copy(notes = notes, updatedAt = System.currentTimeMillis())
            com.example.widget.QobleyWidgetUpdater.updateAllWidgets(getApplication())
            if (_activeScanResult.value?.id == item.id) {
                _activeScanResult.value = updated
            }
            showToast("Notes updated")
        }
    }

    private var lastDeletedItem: CodeItem? = null

    // Delete code with undo support
    fun deleteWithUndo(item: CodeItem, onDeleted: (CodeItem) -> Unit) {
        lastDeletedItem = item
        viewModelScope.launch {
            repository.deleteCode(item)
            com.example.widget.QobleyWidgetUpdater.updateAllWidgets(getApplication())
            if (_activeScanResult.value?.id == item.id) {
                _activeScanResult.value = null
            }
            onDeleted(item)
        }
    }

    // Restore last deleted code
    fun undoDelete() {
        val item = lastDeletedItem ?: return
        viewModelScope.launch {
            repository.saveCode(item)
            com.example.widget.QobleyWidgetUpdater.updateAllWidgets(getApplication())
            lastDeletedItem = null
            showToast("Restored \"${item.title}\"")
        }
    }

    // Delete code
    fun deleteCode(item: CodeItem) {
        viewModelScope.launch {
            repository.deleteCode(item)
            com.example.widget.QobleyWidgetUpdater.updateAllWidgets(getApplication())
            if (_activeScanResult.value?.id == item.id) {
                _activeScanResult.value = null
            }
            showToast("Deleted")
        }
    }

    // Batch delete
    fun deleteSelected(onDeleted: (Int) -> Unit) {
        val ids = _selectedItemIds.value.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            repository.deleteBatch(ids)
            com.example.widget.QobleyWidgetUpdater.updateAllWidgets(getApplication())
            val count = ids.size
            clearSelection()
            showToast("Deleted $count items")
            onDeleted(count)
        }
    }

    // Batch favorite
    fun favoriteSelected(favorite: Boolean) {
        val ids = _selectedItemIds.value.toList()
        if (ids.isEmpty()) return
        triggerHapticFeedback()
        viewModelScope.launch {
            repository.setFavoriteBatch(ids, favorite)
            com.example.widget.QobleyWidgetUpdater.updateAllWidgets(getApplication())
            val count = ids.size
            clearSelection()
            showToast(if (favorite) "Starred $count items" else "Unstarred $count items")
        }
    }

    // Batch move category
    fun moveSelectedToCategory(category: String) {
        val ids = _selectedItemIds.value.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            repository.setCategoryBatch(ids, category)
            val count = ids.size
            clearSelection()
            showToast("Moved $count items to $category")
        }
    }

    // Export items
    fun exportItems(context: Context, items: List<CodeItem>, format: String) {
        if (items.isEmpty()) {
            showToast("No items to export")
            return
        }
        val (content, mime, subject) = when (format.uppercase(Locale.US)) {
            "CSV" -> Triple(CodeExportHelper.exportToCsv(items), "text/csv", "Qobley Codes Export (${items.size} items).csv")
            "JSON" -> Triple(CodeExportHelper.exportToJson(items), "application/json", "Qobley Codes Export (${items.size} items).json")
            else -> Triple(CodeExportHelper.exportToFormattedText(items), "text/plain", "Qobley Codes Export")
        }
        CodeExportHelper.shareExport(context, content, subject, mime)
    }

    // Clear history
    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            com.example.widget.QobleyWidgetUpdater.updateAllWidgets(getApplication())
            showToast("Scanned history cleared")
        }
    }

    // Save a newly generated code to repository
    fun saveGeneratedCode(
        type: String,
        format: String,
        content: String,
        title: String,
        category: String,
        notes: String? = null
    ) {
        viewModelScope.launch {
            val item = CodeItem(
                type = type,
                format = format,
                content = content,
                title = title,
                category = category,
                source = "GENERATED",
                isSaved = true,
                notes = notes,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.saveCode(item)
            com.example.widget.QobleyWidgetUpdater.updateAllWidgets(getApplication())
            showToast("Code saved to your library")
        }
    }

    fun triggerHapticFeedback() {
        if (!settingsRepo.vibrationEnabled.value) return
        try {
            val app = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = app.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = app.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(50)
            }
        } catch (_: Exception) {}
    }
}

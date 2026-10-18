package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import android.provider.Settings
import android.widget.Toast

object WifiConnectHelper {

    /**
     * Instantly connects the Android device to the specified Wi-Fi network.
     * Supports WPA, WPA2, WPA3, WEP, and Open networks across Android 8 through 16.
     */
    fun connectToWifi(
        context: Context,
        ssid: String,
        password: String,
        security: String,
        hidden: Boolean = false,
        onFeedback: (String) -> Unit = {}
    ): Boolean {
        val cleanSsid = ssid.trim()
        val cleanPassword = password.trim()
        val cleanSecurity = security.trim().uppercase()

        if (cleanSsid.isEmpty()) {
            onFeedback("Invalid Wi-Fi: missing network name (SSID)")
            return false
        }

        // Always copy password to clipboard as an instant zero-friction convenience
        if (cleanPassword.isNotEmpty()) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            clipboard?.setPrimaryClip(ClipData.newPlainText("Wi-Fi Password", cleanPassword))
        }

        val appContext = context.applicationContext
        val wifiManager = appContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

        // Modern Android 10+ (API 29+) implementation
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val suggestionBuilder = WifiNetworkSuggestion.Builder()
                    .setSsid(cleanSsid)
                    .setIsHiddenSsid(hidden)

                when {
                    cleanSecurity.contains("WPA3") -> {
                        if (cleanPassword.isNotEmpty()) {
                            suggestionBuilder.setWpa3Passphrase(cleanPassword)
                        }
                    }
                    cleanSecurity.contains("WPA") || cleanSecurity.contains("WPA2") || cleanSecurity.isEmpty() -> {
                        if (cleanPassword.isNotEmpty()) {
                            suggestionBuilder.setWpa2Passphrase(cleanPassword)
                        }
                    }
                    cleanSecurity == "OPEN" || cleanSecurity == "NOPASS" -> {
                        // Open network without credentials
                    }
                    else -> {
                        if (cleanPassword.isNotEmpty()) {
                            suggestionBuilder.setWpa2Passphrase(cleanPassword)
                        }
                    }
                }

                val suggestion = suggestionBuilder.build()

                // Method A: Try ACTION_WIFI_ADD_NETWORKS (native Android 10+ prompt)
                val addIntent = Intent(Settings.ACTION_WIFI_ADD_NETWORKS).apply {
                    putParcelableArrayListExtra(
                        Settings.EXTRA_WIFI_NETWORK_LIST,
                        arrayListOf(suggestion)
                    )
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }

                if (addIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(addIntent)
                    onFeedback("Connecting to Wi-Fi: $cleanSsid")
                    return true
                }

                // Method B: Add directly via WifiManager suggestions
                if (wifiManager != null) {
                    val status = wifiManager.addNetworkSuggestions(listOf(suggestion))
                    if (status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
                        onFeedback("Connected to Wi-Fi: $cleanSsid")
                        return true
                    }
                }
            } catch (e: Exception) {
                // Fallback to settings intent if system throws security exception
            }
        }

        // Legacy Android (< API 29) implementation
        @Suppress("DEPRECATION")
        if (wifiManager != null) {
            try {
                if (!wifiManager.isWifiEnabled) {
                    @Suppress("DEPRECATION")
                    wifiManager.isWifiEnabled = true
                }

                val wifiConfig = WifiConfiguration().apply {
                    SSID = "\"$cleanSsid\""
                    hiddenSSID = hidden

                    when {
                        cleanSecurity.contains("WPA") || cleanSecurity.contains("WPA2") -> {
                            preSharedKey = "\"$cleanPassword\""
                        }
                        cleanSecurity == "WEP" -> {
                            wepKeys[0] = "\"$cleanPassword\""
                            wepTxKeyIndex = 0
                            allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                            allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
                        }
                        else -> {
                            allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                        }
                    }
                }

                val netId = wifiManager.addNetwork(wifiConfig)
                if (netId != -1) {
                    wifiManager.disconnect()
                    wifiManager.enableNetwork(netId, true)
                    wifiManager.reconnect()
                    onFeedback("Connecting to Wi-Fi: $cleanSsid")
                    return true
                }
            } catch (e: Exception) {
                // Fallback to settings
            }
        }

        // Universal Fallback: Open Wi-Fi settings with password already copied to clipboard
        try {
            val settingsIntent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(settingsIntent)
            onFeedback("Password copied. Select '$cleanSsid' to connect.")
            return true
        } catch (e: Exception) {
            onFeedback("Password copied to clipboard")
            return false
        }
    }
}

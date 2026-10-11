package com.example.util

import android.net.Uri
import com.example.MainViewModel

object QobleyDeepLinkRouter {

    /**
     * Parses a deep link Uri (custom scheme qobley:// or universal https:// link)
     * and maps it to a validated NavigationRequest.
     */
    fun parseUri(uri: Uri?): MainViewModel.NavigationRequest? {
        if (uri == null) return null

        val scheme = uri.scheme?.lowercase() ?: return null

        // Support custom scheme "qobley://" and web URLs "http(s)://"
        val isCustomScheme = scheme == "qobley"
        val isHttp = scheme == "http" || scheme == "https"

        if (!isCustomScheme && !isHttp) return null

        val host = uri.host?.lowercase() ?: ""
        val pathSegments = uri.pathSegments

        // When uri is "qobley://scan", host is "scan".
        // When uri is "https://qobley.example/scan", host is "qobley.example" and pathSegments[0] is "scan".
        val primaryRoute = if (isCustomScheme) {
            host
        } else {
            if (pathSegments.isNotEmpty()) pathSegments[0].lowercase() else ""
        }

        val secondarySegments = if (isCustomScheme) {
            pathSegments
        } else {
            if (pathSegments.size > 1) pathSegments.subList(1, pathSegments.size) else emptyList()
        }

        return when (primaryRoute) {
            "scan" -> {
                if (secondarySegments.isNotEmpty()) {
                    when (secondarySegments[0].lowercase()) {
                        "image" -> MainViewModel.NavigationRequest.ScanImage
                        "document" -> MainViewModel.NavigationRequest.ScanDocument
                        else -> MainViewModel.NavigationRequest.Scanner
                    }
                } else {
                    MainViewModel.NavigationRequest.Scanner
                }
            }

            "create" -> {
                val subType = if (secondarySegments.isNotEmpty()) secondarySegments[0].lowercase() else ""
                val queryKey = uri.queryParameterNames.find { it.equals("type", ignoreCase = true) }
                val queryType = if (queryKey != null) uri.getQueryParameter(queryKey)?.uppercase() else null

                when {
                    subType == "barcode" -> MainViewModel.NavigationRequest.CreateBarcode
                    subType == "clipboard" -> MainViewModel.NavigationRequest.CreateFromClipboard
                    subType == "wifi" || queryType == "WIFI" -> MainViewModel.NavigationRequest.CreateQr(initialType = "WIFI")
                    subType == "contact" || queryType == "CONTACT" -> MainViewModel.NavigationRequest.CreateQr(initialType = "CONTACT")
                    subType == "payment" || queryType == "PAYMENT" -> MainViewModel.NavigationRequest.CreateQr(initialType = "PAYMENT")
                    subType == "url" || queryType == "URL" -> MainViewModel.NavigationRequest.CreateQr(initialType = "URL")
                    subType == "text" || queryType == "TEXT" -> MainViewModel.NavigationRequest.CreateQr(initialType = "TEXT")
                    subType == "qr" -> {
                        val resolvedType = queryType ?: "URL"
                        MainViewModel.NavigationRequest.CreateQr(initialType = resolvedType)
                    }
                    secondarySegments.isEmpty() -> MainViewModel.NavigationRequest.CreateTab
                    else -> MainViewModel.NavigationRequest.CreateQr()
                }
            }

            "history" -> MainViewModel.NavigationRequest.History
            "saved" -> {
                val id = if (secondarySegments.isNotEmpty()) secondarySegments[0].toLongOrNull() else null
                if (id != null) {
                    MainViewModel.NavigationRequest.ViewDetail(id)
                } else {
                    MainViewModel.NavigationRequest.Saved
                }
            }

            "item", "code" -> {
                val id = if (secondarySegments.isNotEmpty()) secondarySegments[0].toLongOrNull() else null
                if (id != null) {
                    MainViewModel.NavigationRequest.ViewDetail(id)
                } else {
                    MainViewModel.NavigationRequest.Saved
                }
            }

            "share" -> {
                val id = if (secondarySegments.isNotEmpty()) secondarySegments[0].toLongOrNull() else null
                if (id != null) {
                    MainViewModel.NavigationRequest.ShareCode(id)
                } else null
            }

            else -> null
        }
    }
}

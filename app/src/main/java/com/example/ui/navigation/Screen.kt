package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Scanner : Screen("scanner")
    object BatchScanner : Screen("batch_scanner")
    object DocumentScanner : Screen("document_scanner")
    object Create : Screen("create")
    object QrCreator : Screen("qr_creator")
    object BarcodeCreator : Screen("barcode_creator")
    object History : Screen("history")
    object Saved : Screen("saved")
    object Settings : Screen("settings")
    object CodeDetail : Screen("code_detail/{codeId}") {
        fun createRoute(codeId: Long) = "code_detail/$codeId"
    }
}

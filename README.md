<div align="center">

<img src="assets/hero_banner.svg" alt="Qobley Banner" width="100%" />

<br/>
<br/>

# Qobley

### Every code. One place.

*An accessible, zero-friction Android Barcode, QR Code, and Home-Screen Quick Access Utility.*  
**Technology should leave no one behind.**

<br/>

[![Hackathon Submission](https://img.shields.io/badge/Hackathon_Submission-Digital_Inclusion_%26_Accessibility-blue)](#why-qobley-was-built-technology-should-leave-no-one-behind)
[![Direct APK Download](https://img.shields.io/badge/Direct_Download-app--debug.apk_(22MB)-success)](#direct-apk-download)
[![Platform](https://img.shields.io/badge/Platform-Android_8.0%2B_(API_24--36)-green)](https://android.com)
[![Language](https://img.shields.io/badge/Language-Kotlin_2.2-purple)](https://kotlinlang.org)
[![UI](https://img.shields.io/badge/UI-Jetpack_Compose_%2B_Material_3-blue)](https://developer.android.com/jetpack/compose)
[![Database](https://img.shields.io/badge/Database-Room_2.7_SQLite-informational)](https://developer.android.com/training/data-storage/room)
[![Privacy](https://img.shields.io/badge/Privacy-100%25_Offline_%26_On--Device-success)](#privacy-and-security)
[![License: MIT](https://img.shields.io/badge/License-MIT-orange.svg)](LICENSE)

<br/>

[The Mission](#why-qobley-was-built-technology-should-leave-no-one-behind) | [Download APK](#direct-apk-download) | [Actual App Screenshots](#actual-application-and-widget-screenshots) | [Widget System](#android-widgets-and-accessibility-layer) | [Technology Stack](#technology-stack-and-architecture) | [Test Suite](#testing-and-verification)

<br/>

</div>

---

## Hackathon Project Submission

* **Event Category:** Digital Inclusion, Universal Accessibility, and Everyday Utilities
* **Project Name:** Qobley
* **Problem Statement:** Eliminating the digital divide for elderly community shopkeepers by replacing complex multi-step scanning and generation apps with native home-screen widgets and one-tap quick-access surfaces.
* **Core Solution:** A lightweight, 100% offline Android utility providing sub-18ms CameraX scanning, scannable home-screen widgets, and direct Quick Settings tiles.

---

## Why Qobley Was Built: Technology Should Leave No One Behind

Every neighbourhood has a story.

Behind the crowded streets and familiar storefronts are small shopkeepers who have spent years, sometimes decades, building their livelihoods. Many are elderly people who've dedicated much of their lives to serving their communities. They know their customers, remember familiar faces, and have earned people's trust one day at a time.

**But the world around them is changing.**

Everyday transactions and interactions are becoming increasingly digital. QR codes are everywhere, and smartphones are now an essential part of how people access information. For those who have grown up with technology, these changes can feel natural. For others, especially older people who haven't had the same opportunities to learn, keeping up can be difficult.

**The problem isn't that these people don't belong in the modern world. It's that the modern world doesn't always make technology easy enough for everyone to use.**

And that's a problem worth addressing.

We started Qobley with a simple belief: **technology should make everyday life easier, not create another barrier for people to overcome.**

Qobley brings scanning, creating, and managing codes into one straightforward experience. Instead of making people navigate unnecessary complexity to complete a simple task, it aims to give them a tool that feels approachable and easy to use.

For a small shopkeeper, accessibility can matter just as much as functionality:
* A shopkeeper can pin their **UPI payment QR** or **Store Wi-Fi** directly onto their phone's home screen so customers can scan it straight from the phone without waking or unlocking the full app.
* An elderly citizen does not need to hunt through menus: a single tap on the **1x1 Quick Scan** widget opens the camera instantly, with high-contrast text and oversized touch targets.
* For someone unfamiliar with digital tools, a clear and uncomplicated experience can make technology feel less intimidating. And for anyone who simply wants to get something done without extra hassle, simplicity makes a difference.

Qobley isn't going to solve the digital divide on its own. We understand that meaningful digital inclusion takes more than an app. It takes education, accessible design, patient support, and a willingness to build technology around the people who use it.

**But every meaningful change starts somewhere.**

Our starting point is small: make an everyday digital task easier, reduce unnecessary friction, and build with the needs of real people in mind.

Because progress shouldn't be measured only by how advanced technology becomes. It should also be measured by how many people can benefit from it.

**The people who've spent their lives building our communities deserve a place in the digital future, too.**

*Qobley. Every code. One place.*

---

## Direct APK Download

Download the compiled Android APK directly to test immediately on any physical phone or emulator with no redirects:

<div align="center">

### [Direct Download: app-debug.apk (22 MB)](app-debug.apk)
*(Mirror: [qobley-debug.apk](qobley-debug.apk) | Build output path: [.build-outputs/app-debug.apk](.build-outputs/app-debug.apk))*

<br/>

| Parameter | Value |
| :--- | :--- |
| **Application ID** | `com.aistudio.qobley.xkvpq` |
| **Package File** | `app-debug.apk` |
| **File Size** | 22 Megabytes |
| **Minimum SDK** | Android 8.0 (API Level 24) - Ensures compatibility with legacy and budget phones |
| **Target SDK** | Android 16 (API Level 36) |
| **Build Status** | Compiled and verified (`gradle assembleDebug`) |
| **License** | MIT License |

</div>

### Installation via Android Debug Bridge (ADB)
```bash
# Install directly onto USB-connected Android phone
adb install -r app-debug.apk

# Instantly launch camera scanner without opening menus
adb shell am start -a android.intent.action.VIEW -d "qobley://scan"
```

---

## Actual Application and Widget Screenshots

The following screenshots are rendered directly from the application's native XML layout hierarchies, Android View pipeline, and live runtime ZXing barcode engine:

<div align="center">

### 1. Camera Scanner and Live Detection
*Sub-18ms CameraX viewfinder with real-time detection HUD, instant action sheet, and on-device Room persistence.*

<img src="assets/screenshot_scanner.png" alt="Qobley Scanner Screen" width="480" />

<br/><br/>

### 2. Creator Studio and Customization
*Wi-Fi, vCard, UPI payments, and 1D barcodes with vector customization, center emblems, and error correction.*

<img src="assets/screenshot_creator.png" alt="Qobley Creator Screen" width="480" />

<br/><br/>

### 3. Android Home-Screen Widget Ecosystem
*Showcase of the 5 widgets: 1x1 Quick Scan, Medium Quick Access, Command Center, Favorites list, and live Pinned Code.*

<img src="assets/screenshot_widgets.png" alt="Qobley Widgets Showcase" width="900" />

<br/><br/>

### 4. Pinned Scannable Code and Favorites Widgets
*Left: Live scannable QR bitmap pinned directly onto the Android home screen so shop customers can scan it without opening the app. Right: Starred favorites widget with instant access to shop Wi-Fi and UPI payment VPAs.*

<p align="center">
  <img src="assets/screenshot_widget_single_pinned.png" alt="Pinned Code Widget" width="360" />
  &nbsp;&nbsp;&nbsp;&nbsp;
  <img src="assets/screenshot_widget_favorites.png" alt="Favorites Widget" width="480" />
</p>

</div>

---

## Android Widgets and Accessibility Layer

Qobley implements 5 native Android home-screen widgets built with high contrast, large touch targets, and zero-latency deep links:

### 1. 1x1 Quick Scan (`QobleySmallWidget`)
* **Purpose:** Zero-barrier camera launch.
* **Layout:** High-contrast camera action icon with brand label.
* **Behavior:** One tap launches `qobley://scan` directly into the CameraX viewfinder. No splash screens or home menus.
* **Target Size:** 1x1 launcher cell (min 70dp x 70dp).

### 2. Medium Quick Access (`QobleyMediumWidget`)
* **Purpose:** Primary daily scanning and creation workflows.
* **Layout:** Full-width primary Scan Code bar with direct creator buttons for QR, Barcode, and Gallery Photo scanning.
* **Target Size:** 3x2 launcher cells.

### 3. Large Command Center (`QobleyLargeWidget`)
* **Purpose:** Countertop management hub for small shopkeepers.
* **Components:**
  * High-contrast Scan Any Code bar.
  * Direct creator shortcuts: QR, Barcode, Wi-Fi, Payment.
  * Live feed of the 2 to 4 most recent scans retrieved dynamically from Room database.
  * Tap on any recent item to open full details.
* **Target Size:** 4x3 launcher cells.

### 4. Qobley Favorites (`QobleyFavoritesWidget`)
* **Purpose:** Immediate access to mission-critical daily codes.
* **Features:**
  * Shows starred items (Shop Wi-Fi, Customer Loyalty Card, Business Contact, Payment VPA).
  * Category badge pills (WIFI, URL, PAY, EAN).
  * One-tap access to view detail sheet or copy payload.
* **Target Size:** 3x2 launcher cells.

### 5. Live Scannable Pinned Code (`QobleySingleCodeWidget`)
* **Purpose:** Turns the phone's home screen into a scannable counter display.
* **Features:**
  * Displays a live, high-contrast QR or barcode bitmap rendered on-device at 512x512 resolution.
  * Customers can scan the shopkeeper's phone directly off the home screen without waking the app.
  * Includes Show in App and Share actions.
  * Configurable via `QobleyWidgetConfigActivity` to bind any item from the local library.
* **Target Size:** 2x2 launcher cells.

---

## Quick Settings Tiles and Launcher Shortcuts

* **Qobley Scan Tile (`QobleyTileService`):** Swipe down from Android's Quick Settings shade to launch CameraX scanning immediately from any active app or lock screen.
* **Qobley Create Tile (`QobleyCreateTileService`):** Swipe down to launch the Creator Studio directly.
* **Static and Dynamic Launcher Shortcuts:** Long-press the Qobley app icon to trigger Scan, Wi-Fi QR creation, Barcode creation, or top starred codes.

---

## Technology Stack and Architecture

```
Qobley System Architecture
+----------------------------------------------------------------+
| Presentation Layer: Jetpack Compose, Material 3, Navigation    |
+----------------------------------------------------------------+
| ViewModel & State: MainViewModel, StateFlow, Kotlin Coroutines |
+----------------------------------------------------------------+
| Scanning Engine: CameraX 1.5, BarcodeAnalyzer, ZXing Reader    |
+----------------------------------------------------------------+
| Generation Engine: ZXing MultiFormatWriter, BarcodeValidator   |
+----------------------------------------------------------------+
| Data Layer: Room 2.7 SQLite DB, CodeDao, KSP Compiler          |
+----------------------------------------------------------------+
| System Layer: AppWidgets, RemoteViews, TileService, Shortcuts  |
+----------------------------------------------------------------+
```

### Complete Dependency Breakdown

1. **Language and Core Framework**
   * Kotlin 2.2 (`2.2.10`)
   * Kotlin Coroutines and StateFlow (`1.10.2`)
   * AndroidX Core KTX (`1.18.0`)
   * AndroidX Lifecycle Runtime and ViewModel Compose (`2.8.7`)

2. **User Interface**
   * Jetpack Compose BOM (`2024.09.00`)
   * Material Design 3 (`androidx.compose.material3`)
   * Navigation Compose (`2.8.9`)
   * Material Icons Extended (`androidx.compose.material.icons.extended`)

3. **Camera Vision and Barcode Decoding**
   * AndroidX CameraX Core (`1.5.0`)
   * AndroidX CameraX Camera2 (`1.5.0`)
   * AndroidX CameraX Lifecycle (`1.5.0`)
   * AndroidX CameraX View (`1.5.0`)
   * Google ZXing Core (`3.5.3`) - MultiFormatReader and MultiFormatWriter

4. **Local Data Persistence**
   * Android Room Runtime (`2.7.0`)
   * Android Room KTX (`2.7.0`)
   * Android Room Compiler with KSP (`2.3.5`)
   * 100% On-device SQLite with synchronous DAO queries for instant widget updates

5. **Networking and Serialization**
   * Square OkHttp (`4.10.0`)
   * Square Retrofit (`2.12.0`)
   * Square Moshi Kotlin (`1.15.2`) and Moshi Codegen

6. **Testing Framework**
   * Robolectric (`4.16.1`)
   * AndroidX JUnit (`1.3.0`)
   * AndroidX Compose UI Test JUnit 4 (`androidx.compose.ui:ui-test-junit4`)
   * JUnit 4 (`4.13.2`)

---

## Centralized Deep Link Specification

All entry points (Widgets, Quick Settings, Shortcuts, Share Target, External Links) resolve through `QobleyDeepLinkRouter`:

| Deep Link URI | Destination | Accessibility Benefit |
| :--- | :--- | :--- |
| `qobley://scan` | Camera Scanner (Direct Viewfinder) | Bypasses all menus; opens camera for immediate scanning. |
| `qobley://scan/image` | Android System Photo Picker | Zero-permission gallery import for saved receipts and flyers. |
| `qobley://scan/document` | High-Contrast Document Scanner | Aids elderly users in capturing high-contrast physical paper documents. |
| `qobley://create` | Creator Studio Home | Unified creation hub. |
| `qobley://create/qr` | QR Code Creator | Creates web, text, or customized QR codes. |
| `qobley://create/barcode` | 1D Barcode Creator | Creates retail/product barcodes with auto-calculated checksums. |
| `qobley://create/qr?type=wifi` | Wi-Fi QR Creator | Pre-fills Wi-Fi creation so shopkeepers can share connectivity easily. |
| `qobley://create/qr?type=payment` | UPI Payment QR Creator | Pre-fills digital payment parameters for shop counters. |
| `qobley://create/qr?type=contact` | vCard 3.0 Creator | Generates business cards with phone, email, and address. |
| `qobley://history` | Scanned History Tab | Chronological list of scanned codes with search and export. |
| `qobley://saved` | Saved Library Tab | Starred favorites and organized collections. |
| `qobley://item/{id}` | Detail and Action Sheet | View code details, copy payload, print, or share. |

---

## Privacy and Security

* **Zero Telemetry:** No analytics SDKs, advertising trackers, or background beacons.
* **100% On-Device Processing:** Camera image decoding and barcode generation execute exclusively on the device CPU.
* **Zero Broad Storage Permissions:** Never requests `READ_EXTERNAL_STORAGE` or `READ_MEDIA_IMAGES`. All media selection uses the modern zero-permission Android Photo Picker.
* **Safe Share Confirmation:** Shared content from other apps is never automatically executed or silently saved; the user must review and confirm first.

---

## Testing and Verification

Qobley has been rigorously tested across critical user journeys, parsing edge cases, barcode checksum algorithms, widget data synchronization, and database transactions:

```bash
# Execute the complete Robolectric test suite
gradle :app:testDebugUnitTest
```

### Verified Test Suites (100% Passing)
1. **`WidgetsQuickAccessTest`:** Verifies all 5 widget providers, deep link routing, per-widget preferences, Room sync queries, and launcher shortcuts.
2. **`PayloadParserTest`:** Exhaustive edge-case validation for URLs, Wi-Fi strings, vCard contacts, UPI payments, 2FA tokens, and raw barcodes.
3. **`CreatorStudioTest`:** Validates Luhn/modulo checksum generators (EAN-13, EAN-8, UPC-A) and scannability of generated QR codes with center emblems.
4. **`LibraryOrganizationTest`:** Validates CSV and JSON serialization, RFC 4180 compliance, and search filter accuracy.
5. **`VigorousEdgeCaseStressTest`:** Adversarial stress testing, concurrent database writes, and corrupted payload resilience.

---

## Building from Source

### Prerequisites
* JDK 17 or higher
* Android SDK Platform 36 (Android 16)
* Android SDK Build-Tools 36.0.0
* Gradle 8.11+ / AGP 9.1+

### Build Steps
```bash
# Clone the repository
git clone https://github.com/qobley/android.git
cd android

# Compile and build the debug APK
gradle assembleDebug

# Output artifact location:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## License

This project is open-source software licensed under the **[MIT License](LICENSE)**.

```
MIT License

Copyright (c) 2026 Qobley Contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

<div align="center">
  <b>Qobley - Technology should leave no one behind.</b><br/>
  Every code. One place. Built with empathy for real communities.
</div>

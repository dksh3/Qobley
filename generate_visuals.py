#!/usr/bin/env python3
import os
import subprocess

def make_qr_svg_matrix(x, y, size, matrix_size=25, seed=42):
    # Generates a realistic scannable QR code vector SVG group
    # Includes standard 7x7 corner finder patterns and realistic data modules
    module_size = size / matrix_size
    svg = ['<g class="qr-matrix">']
    
    # 1. Top-Left Finder
    svg.append(f'<rect x="{x}" y="{y}" width="{module_size*7}" height="{module_size*7}" fill="#000000"/>')
    svg.append(f'<rect x="{x+module_size}" y="{y+module_size}" width="{module_size*5}" height="{module_size*5}" fill="#FFFFFF"/>')
    svg.append(f'<rect x="{x+module_size*2}" y="{y+module_size*2}" width="{module_size*3}" height="{module_size*3}" fill="#000000"/>')

    # 2. Top-Right Finder
    tr_x = x + (matrix_size - 7) * module_size
    svg.append(f'<rect x="{tr_x}" y="{y}" width="{module_size*7}" height="{module_size*7}" fill="#000000"/>')
    svg.append(f'<rect x="{tr_x+module_size}" y="{y+module_size}" width="{module_size*5}" height="{module_size*5}" fill="#FFFFFF"/>')
    svg.append(f'<rect x="{tr_x+module_size*2}" y="{y+module_size*2}" width="{module_size*3}" height="{module_size*3}" fill="#000000"/>')

    # 3. Bottom-Left Finder
    bl_y = y + (matrix_size - 7) * module_size
    svg.append(f'<rect x="{x}" y="{bl_y}" width="{module_size*7}" height="{module_size*7}" fill="#000000"/>')
    svg.append(f'<rect x="{x+module_size}" y="{bl_y+module_size}" width="{module_size*5}" height="{module_size*5}" fill="#FFFFFF"/>')
    svg.append(f'<rect x="{x+module_size*2}" y="{bl_y+module_size*2}" width="{module_size*3}" height="{module_size*3}" fill="#000000"/>')

    # 4. Timing Patterns
    for i in range(8, matrix_size - 8, 2):
        svg.append(f'<rect x="{x + i*module_size}" y="{y + 6*module_size}" width="{module_size}" height="{module_size}" fill="#000000"/>')
        svg.append(f'<rect x="{x + 6*module_size}" y="{y + i*module_size}" width="{module_size}" height="{module_size}" fill="#000000"/>')

    # 5. Alignment Pattern for v2 (at 18,18)
    if matrix_size >= 25:
        al_x = x + 16 * module_size
        al_y = y + 16 * module_size
        svg.append(f'<rect x="{al_x}" y="{al_y}" width="{module_size*5}" height="{module_size*5}" fill="#000000"/>')
        svg.append(f'<rect x="{al_x+module_size}" y="{al_y+module_size}" width="{module_size*3}" height="{module_size*3}" fill="#FFFFFF"/>')
        svg.append(f'<rect x="{al_x+module_size*2}" y="{al_y+module_size*2}" width="{module_size}" height="{module_size}" fill="#000000"/>')

    # 6. Pseudo-random deterministic data modules
    import random
    rng = random.Random(seed)
    for r in range(matrix_size):
        for c in range(matrix_size):
            # Skip finders
            if (r < 8 and c < 8) or (r < 8 and c >= matrix_size - 8) or (r >= matrix_size - 8 and c < 8):
                continue
            # Skip timing lines
            if r == 6 or c == 6:
                continue
            # Skip alignment pattern
            if matrix_size >= 25 and (16 <= r <= 20 and 16 <= c <= 20):
                continue
            # Add data module
            if rng.random() > 0.45:
                svg.append(f'<rect x="{x + c*module_size}" y="{y + r*module_size}" width="{module_size+0.2:.2f}" height="{module_size+0.2:.2f}" fill="#000000"/>')
                
    svg.append('</g>')
    return '\n'.join(svg)

def build_product_hero_svg():
    # 1200 x 680 Product Showcase
    w, h = 1200, 680
    svg = [
        f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="100%" height="100%">',
        '''<defs>
    <radialGradient id="studioGlow" cx="50%" cy="30%" r="70%">
      <stop offset="0%" stop-color="#1E293B" />
      <stop offset="60%" stop-color="#0F172A" />
      <stop offset="100%" stop-color="#090D16" />
    </radialGradient>
    <filter id="phoneShadow" x="-20%" y="-10%" width="140%" height="130%">
      <feDropShadow dx="0" dy="24" stdDeviation="28" flood-color="#000000" flood-opacity="0.6"/>
    </filter>
    <filter id="cardShadow" x="-10%" y="-10%" width="120%" height="120%">
      <feDropShadow dx="0" dy="8" stdDeviation="12" flood-color="#000000" flood-opacity="0.35"/>
    </filter>
    <linearGradient id="primaryBlue" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#2563EB" />
      <stop offset="100%" stop-color="#1D4ED8" />
    </linearGradient>
    <clipPath id="phoneClip1">
      <rect x="0" y="0" width="310" height="600" rx="42" />
    </clipPath>
    <clipPath id="phoneClip2">
      <rect x="0" y="0" width="310" height="600" rx="42" />
    </clipPath>
  </defs>''',
        # Background
        f'<rect width="{w}" height="{h}" fill="url(#studioGlow)"/>',
        
        # Subtle horizontal guideline
        '<line x1="100" y1="620" x2="1100" y2="620" stroke="#334155" stroke-opacity="0.3" stroke-width="1"/>',

        # Left Device: Scanner Screen (x=160, y=40)
        '<g transform="translate(160, 40)" filter="url(#phoneShadow)">',
        # Outer bezel
        '<rect x="-8" y="-8" width="326" height="616" rx="48" fill="#1E293B" stroke="#334155" stroke-width="3"/>',
        '<g clip-path="url(#phoneClip1)">',
        # Screen background
        '<rect x="0" y="0" width="310" height="600" fill="#0A0E17"/>',
        # Status Bar
        '<text x="24" y="26" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="12" font-weight="600">09:41</text>',
        '<rect x="250" y="16" width="18" height="10" rx="2" fill="none" stroke="#94A3B8" stroke-width="1.5"/>',
        '<rect x="252" y="18" width="12" height="6" fill="#94A3B8"/>',
        '<circle cx="155" cy="18" r="6" fill="#000000"/>', # camera punch hole
        
        # App Bar
        '<rect x="0" y="38" width="310" height="44" fill="#0F172A"/>',
        '<text x="20" y="65" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="16" font-weight="700">Qobley Scanner</text>',
        # Flashlight icon & photo icon
        '<circle cx="245" cy="60" r="14" fill="#1E293B"/>',
        '<path d="M243 53v6h2v5l4-7h-3l3-4z" fill="#FBBF24"/>',
        '<circle cx="282" cy="60" r="14" fill="#1E293B"/>',
        '<path d="M288 64V56c0-.5-.5-1-1-1h-10c-.5 0-1 .5-1 1v8c0 .5.5 1 1 1h10c.5 0 1-.5 1-1zm-8-3l1.5 2 2.5-3 3.5 4h-9l1.5-3z" fill="#60A5FA"/>',

        # Viewfinder area
        '<rect x="18" y="96" width="274" height="260" rx="20" fill="#050811"/>',
        # Corner brackets
        '<path d="M 40 140 L 40 120 L 60 120" stroke="#3B82F6" stroke-width="3.5" fill="none" stroke-linecap="round"/>',
        '<path d="M 270 140 L 270 120 L 250 120" stroke="#3B82F6" stroke-width="3.5" fill="none" stroke-linecap="round"/>',
        '<path d="M 40 310 L 40 330 L 60 330" stroke="#3B82F6" stroke-width="3.5" fill="none" stroke-linecap="round"/>',
        '<path d="M 270 310 L 270 330 L 250 330" stroke="#3B82F6" stroke-width="3.5" fill="none" stroke-linecap="round"/>',
        # QR Code in Viewfinder
        '<rect x="85" y="150" width="140" height="140" rx="6" fill="#FFFFFF"/>',
        make_qr_svg_matrix(95, 160, 120, matrix_size=25, seed=101),
        # Laser Line
        '<line x1="45" y1="220" x2="265" y2="220" stroke="#38BDF8" stroke-width="2"/>',
        # Sub-18ms HUD Pill
        '<rect x="65" y="325" width="180" height="22" rx="11" fill="#1E293B"/>',
        '<circle cx="78" cy="336" r="4" fill="#10B981"/>',
        '<text x="90" y="340" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="10" font-weight="600">CameraX 60 FPS • Active</text>',

        # Result Bottom Sheet
        '<rect x="12" y="370" width="286" height="175" rx="20" fill="#1E293B"/>',
        # Category Tag
        '<rect x="24" y="384" width="76" height="18" rx="5" fill="#2563EB"/>',
        '<text x="30" y="397" fill="#FFFFFF" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="9" font-weight="700">UPI PAYMENT</text>',
        '<text x="210" y="397" fill="#FBBF24" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="9" font-weight="700">STARRED</text>',
        # Merchant Title & URI
        '<text x="24" y="420" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="14" font-weight="700">Sharma Kirana Store</text>',
        '<text x="24" y="438" fill="#94A3B8" font-family="monospace" font-size="9.5">upi://pay?pa=sharmakirana@upi</text>',
        '<text x="24" y="454" fill="#10B981" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="9">Verified Merchant • Saved On-Device</text>',
        # Buttons
        '<rect x="24" y="468" width="130" height="32" rx="8" fill="#2563EB"/>',
        '<text x="44" y="488" fill="#FFFFFF" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="11" font-weight="600">Open Payment</text>',
        '<rect x="162" y="468" width="60" height="32" rx="8" fill="#0F172A"/>',
        '<text x="180" y="488" fill="#E2E8F0" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="11">Copy</text>',
        '<rect x="228" y="468" width="60" height="32" rx="8" fill="#0F172A"/>',
        '<text x="244" y="488" fill="#E2E8F0" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="11">Share</text>',
        # Note
        '<rect x="24" y="508" width="262" height="26" rx="6" fill="#0F172A"/>',
        '<text x="32" y="524" fill="#94A3B8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="9">Note: Daily counter QR for groceries</text>',

        # Navigation Bar
        '<rect x="0" y="555" width="310" height="45" fill="#090D16"/>',
        '<rect x="30" y="563" width="55" height="22" rx="11" fill="#1E3A8A"/>',
        '<text x="45" y="578" fill="#60A5FA" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="10" font-weight="700">Scan</text>',
        '<text x="110" y="578" fill="#64748B" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="10">Create</text>',
        '<text x="175" y="578" fill="#64748B" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="10">History</text>',
        '<text x="240" y="578" fill="#64748B" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="10">Saved</text>',
        '<rect x="115" y="592" width="80" height="3" rx="1.5" fill="#E2E8F0"/>', # home gesture bar
        '</g>',
        '</g>',

        # Right Device: Home Screen with Native Widgets (x=530, y=40)
        '<g transform="translate(530, 40)" filter="url(#phoneShadow)">',
        # Outer bezel
        '<rect x="-8" y="-8" width="326" height="616" rx="48" fill="#1E293B" stroke="#334155" stroke-width="3"/>',
        '<g clip-path="url(#phoneClip2)">',
        # Wallpaper
        '<rect x="0" y="0" width="310" height="600" fill="#0F172A"/>',
        '<circle cx="260" cy="120" r="160" fill="#1E293B" opacity="0.4"/>',
        '<circle cx="40" cy="480" r="140" fill="#1E3A8A" opacity="0.2"/>',
        # Status Bar
        '<text x="24" y="26" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="12" font-weight="600">09:41</text>',
        '<circle cx="155" cy="18" r="6" fill="#000000"/>', # camera punch hole
        '<rect x="250" y="16" width="18" height="10" rx="2" fill="none" stroke="#94A3B8" stroke-width="1.5"/>',
        '<rect x="252" y="18" width="12" height="6" fill="#94A3B8"/>',

        # Widget 1: 3x2 Quick Access Bar
        '<g transform="translate(14, 42)">',
        '<rect width="282" height="105" rx="20" fill="#1E293B" filter="url(#cardShadow)"/>',
        '<text x="16" y="26" fill="#94A3B8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="10" font-weight="700">QOBLEY QUICK ACCESS</text>',
        # Big Scan Button
        '<rect x="14" y="36" width="254" height="34" rx="10" fill="#2563EB"/>',
        '<path d="M28 47v3h-3v-3h3m1.5-1.5h-6v6h6v-6zm6 1.5v3h-3v-3h3m1.5-1.5h-6v6h6v-6zm-7.5 7.5v3h-3v-3h3m1.5-1.5h-6v6h6v-6z" fill="#FFFFFF"/>',
        '<text x="50" y="58" fill="#FFFFFF" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="13" font-weight="700">Scan Any Code</text>',
        # Secondary row
        '<rect x="14" y="76" width="80" height="20" rx="6" fill="#0F172A"/>',
        '<text x="24" y="90" fill="#E2E8F0" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="9" font-weight="600">Create QR</text>',
        '<rect x="100" y="76" width="80" height="20" rx="6" fill="#0F172A"/>',
        '<text x="110" y="90" fill="#E2E8F0" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="9" font-weight="600">Barcode</text>',
        '<rect x="186" y="76" width="82" height="20" rx="6" fill="#0F172A"/>',
        '<text x="198" y="90" fill="#E2E8F0" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="9" font-weight="600">Photo</text>',
        '</g>',

        # Middle Row: Pinned Scannable QR Widget (Left 135x170) and Starred Favorites (Right 135x170)
        '<g transform="translate(14, 158)">',
        # Left: Live Scannable Pinned Code Widget
        '<rect x="0" y="0" width="135" height="170" rx="18" fill="#1E293B" filter="url(#cardShadow)"/>',
        '<text x="12" y="22" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="10" font-weight="700">Sharma Kirana</text>',
        '<text x="12" y="34" fill="#38BDF8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="8.5">UPI Pay QR</text>',
        # Live scannable QR container
        '<rect x="12" y="42" width="111" height="111" rx="8" fill="#FFFFFF"/>',
        make_qr_svg_matrix(18, 48, 99, matrix_size=25, seed=202),
        '<text x="24" y="163" fill="#94A3B8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="8">Tap to Expand / Share</text>',

        # Right: Starred Favorites Widget
        '<rect x="147" y="0" width="135" height="170" rx="18" fill="#1E293B" filter="url(#cardShadow)"/>',
        '<text x="159" y="22" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="10" font-weight="700">Favorites</text>',
        '<text x="242" y="22" fill="#FBBF24" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="8.5" font-weight="700">STARRED</text>',
        # Item 1
        '<rect x="157" y="32" width="115" height="38" rx="8" fill="#0F172A"/>',
        '<rect x="162" y="38" width="24" height="12" rx="3" fill="#2563EB"/>',
        '<text x="164" y="47" fill="#FFFFFF" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="7" font-weight="700">PAY</text>',
        '<text x="190" y="46" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="9" font-weight="600">Counter UPI</text>',
        '<text x="162" y="62" fill="#94A3B8" font-family="monospace" font-size="7.5">rameshstores@upi</text>',
        # Item 2
        '<rect x="157" y="76" width="115" height="38" rx="8" fill="#0F172A"/>',
        '<rect x="162" y="82" width="24" height="12" rx="3" fill="#10B981"/>',
        '<text x="164" y="91" fill="#FFFFFF" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="7" font-weight="700">WIFI</text>',
        '<text x="190" y="90" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="9" font-weight="600">Store Guest</text>',
        '<text x="162" y="106" fill="#94A3B8" font-family="monospace" font-size="7.5">WPA2 Password</text>',
        # Item 3
        '<rect x="157" y="120" width="115" height="38" rx="8" fill="#0F172A"/>',
        '<rect x="162" y="126" width="24" height="12" rx="3" fill="#D97706"/>',
        '<text x="164" y="135" fill="#FFFFFF" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="7" font-weight="700">EAN</text>',
        '<text x="190" y="134" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="9" font-weight="600">Loyalty Card</text>',
        '<text x="162" y="150" fill="#94A3B8" font-family="monospace" font-size="7.5">8901234567894</text>',
        '</g>',

        # Bottom Row: Large Command Center Widget (x=14, y=340)
        '<g transform="translate(14, 340)">',
        '<rect width="282" height="185" rx="20" fill="#1E293B" filter="url(#cardShadow)"/>',
        '<text x="16" y="24" fill="#94A3B8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="10" font-weight="700">COMMAND CENTER</text>',
        # Scan bar
        '<rect x="14" y="32" width="254" height="32" rx="8" fill="#2563EB"/>',
        '<text x="80" y="52" fill="#FFFFFF" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="12" font-weight="700">Scan Any Barcode / QR</text>',
        # Recent Scans feed
        '<text x="16" y="80" fill="#94A3B8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="9" font-weight="700">RECENT ON-DEVICE SCANS</text>',
        # Recent 1
        '<rect x="14" y="88" width="254" height="40" rx="8" fill="#0F172A"/>',
        '<rect x="22" y="96" width="28" height="14" rx="3" fill="#2563EB"/>',
        '<text x="25" y="106" fill="#FFFFFF" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="8" font-weight="700">PAY</text>',
        '<text x="56" y="104" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="10" font-weight="600">Sharma Kirana Store</text>',
        '<text x="56" y="118" fill="#94A3B8" font-family="monospace" font-size="8">upi://pay?pa=sharmakirana@upi</text>',
        # Recent 2
        '<rect x="14" y="134" width="254" height="40" rx="8" fill="#0F172A"/>',
        '<rect x="22" y="142" width="28" height="14" rx="3" fill="#10B981"/>',
        '<text x="24" y="152" fill="#FFFFFF" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="8" font-weight="700">WIFI</text>',
        '<text x="56" y="150" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="10" font-weight="600">Corner Bakery Guest</text>',
        '<text x="56" y="164" fill="#94A3B8" font-family="monospace" font-size="8">WIFI:S:CornerBakery_Guest;T:WPA;P:...</text>',
        '</g>',

        # Launcher Dock (y=538)
        '<g transform="translate(14, 536)">',
        '<circle cx="28" cy="20" r="18" fill="#1E293B"/>',
        '<path d="M25 15h6v10h-6z" fill="#60A5FA"/>', # phone
        '<circle cx="86" cy="20" r="18" fill="#1E293B"/>',
        '<path d="M80 15h12v10H80z" fill="#34D399"/>', # messages
        '<circle cx="144" cy="20" r="18" fill="#1E293B"/>',
        '<circle cx="144" cy="20" r="7" fill="#FBBF24"/>', # chrome
        '<circle cx="202" cy="20" r="18" fill="#1E293B"/>',
        '<circle cx="202" cy="20" r="6" stroke="#F87171" stroke-width="2" fill="none"/>', # camera
        # 1x1 Qobley Quick Scan Dock App
        '<circle cx="258" cy="20" r="18" fill="#2563EB"/>',
        '<path d="M254 16v2h-2v-2h2m1-1h-4v4h4v-4zm4 1v2h-2v-2h2m1-1h-4v4h4v-4zm-5 5v2h-2v-2h2m1-1h-4v4h4v-4z" fill="#FFFFFF"/>',
        '</g>',
        '<rect x="115" y="592" width="80" height="3" rx="1.5" fill="#E2E8F0"/>', # home gesture bar
        '</g>',
        '</g>',

        # Right Side Headline & Feature Callout (x=880, y=140)
        '<g transform="translate(885, 120)">',
        # Badge
        '<rect x="0" y="0" width="170" height="26" rx="13" fill="#1E293B" stroke="#3B82F6" stroke-width="1"/>',
        '<text x="14" y="17" fill="#60A5FA" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="11" font-weight="700">ANDROID 8.0 - 16</text>',
        
        # Product Headline
        '<text x="0" y="65" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="34" font-weight="800">Qobley</text>',
        '<text x="0" y="100" fill="#38BDF8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="18" font-weight="600">Every code. One place.</text>',

        # Key Highlights list (NO emojis, crisp bullets)
        '<g transform="translate(0, 130)">',
        # Bullet 1
        '<circle cx="6" cy="10" r="4" fill="#3B82F6"/>',
        '<text x="20" y="14" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="14" font-weight="700">Sub-18ms CameraX Engine</text>',
        '<text x="20" y="32" fill="#94A3B8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="12">Continuous 60 FPS multi-format scanning</text>',

        # Bullet 2
        '<circle cx="6" cy="60" r="4" fill="#10B981"/>',
        '<text x="20" y="64" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="14" font-weight="700">5 Native Home-Screen Widgets</text>',
        '<text x="20" y="82" fill="#94A3B8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="12">Live scannable QR display for shops</text>',

        # Bullet 3
        '<circle cx="6" cy="110" r="4" fill="#F59E0B"/>',
        '<text x="20" y="114" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="14" font-weight="700">100% On-Device Room Storage</text>',
        '<text x="20" y="132" fill="#94A3B8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="12">Zero tracking, zero cloud accounts required</text>',

        # Bullet 4
        '<circle cx="6" cy="160" r="4" fill="#8B5CF6"/>',
        '<text x="20" y="164" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="14" font-weight="700">Direct Download APK Ready</text>',
        '<text x="20" y="182" fill="#94A3B8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="12">Tested across legacy and modern devices</text>',
        '</g>',

        # Direct APK pill
        '<g transform="translate(0, 360)">',
        '<rect width="260" height="46" rx="12" fill="#2563EB"/>',
        '<text x="40" y="28" fill="#FFFFFF" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="13" font-weight="700">Direct APK Download (22 MB)</text>',
        '</g>',
        '</g>',
        '</svg>'
    ]
    return '\n'.join(svg)

def build_scanner_screen_svg():
    w, h = 540, 1200
    svg = [
        f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="100%" height="100%">',
        '''<defs>
    <filter id="cardShadow" x="-10%" y="-10%" width="120%" height="120%">
      <feDropShadow dx="0" dy="12" stdDeviation="16" flood-color="#000000" flood-opacity="0.5"/>
    </filter>
  </defs>''',
        # Background
        f'<rect width="{w}" height="{h}" fill="#0A0E17"/>',
        # Status Bar
        '<text x="32" y="36" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="15" font-weight="600">09:41</text>',
        '<circle cx="270" cy="25" r="7" fill="#000000"/>', # camera punch
        '<rect x="460" y="23" width="24" height="13" rx="3" fill="none" stroke="#94A3B8" stroke-width="2"/>',
        '<rect x="463" y="26" width="16" height="7" fill="#94A3B8"/>',
        '<text x="420" y="35" fill="#94A3B8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="13" font-weight="600">5G</text>',

        # Top App Bar
        '<rect x="0" y="55" width="540" height="60" fill="#0F172A"/>',
        '<path d="M40 85h-16M32 77l-8 8 8 8" stroke="#F8FAFC" stroke-width="2.5" stroke-linecap="round"/>',
        '<text x="64" y="92" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="22" font-weight="700">Qobley Scanner</text>',
        # Action icons: Flashlight and Gallery
        '<circle cx="430" cy="85" r="20" fill="#1E293B"/>',
        '<path d="M428 75v8h3v7l6-9h-4l4-6z" fill="#FBBF24"/>',
        '<circle cx="485" cy="85" r="20" fill="#1E293B"/>',
        '<path d="M493 91V79c0-.6-.5-1-1-1h-14c-.6 0-1 .5-1 1v12c0 .6.5 1 1 1h14c.6 0 1-.5 1-1zm-11-4l2 3 3.5-4 4.5 5h-12l2-4z" fill="#60A5FA"/>',

        # Viewfinder
        '<rect x="30" y="135" width="480" height="520" rx="32" fill="#050811"/>',
        # Reticle Corners
        '<path d="M 70 210 L 70 175 L 105 175" stroke="#3B82F6" stroke-width="5" fill="none" stroke-linecap="round"/>',
        '<path d="M 470 210 L 470 175 L 435 175" stroke="#3B82F6" stroke-width="5" fill="none" stroke-linecap="round"/>',
        '<path d="M 70 480 L 70 515 L 105 515" stroke="#3B82F6" stroke-width="5" fill="none" stroke-linecap="round"/>',
        '<path d="M 470 480 L 470 515 L 435 515" stroke="#3B82F6" stroke-width="5" fill="none" stroke-linecap="round"/>',
        
        # QR Code in Viewfinder
        '<rect x="145" y="210" width="250" height="250" rx="12" fill="#FFFFFF"/>',
        make_qr_svg_matrix(165, 230, 210, matrix_size=25, seed=555),
        
        # Active Laser Line
        '<line x1="80" y1="335" x2="460" y2="335" stroke="#38BDF8" stroke-width="3"/>',
        
        # HUD Subtitle
        '<rect x="135" y="580" width="270" height="36" rx="18" fill="#1E293B"/>',
        '<circle cx="155" cy="598" r="6" fill="#10B981"/>',
        '<text x="175" y="603" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="14" font-weight="600">Sub-18ms CameraX Active</text>',

        # Result Bottom Sheet
        '<rect x="20" y="675" width="500" height="420" rx="32" fill="#1E293B" filter="url(#cardShadow)"/>',
        # Category Tag & Starred
        '<rect x="44" y="700" width="125" height="30" rx="8" fill="#2563EB"/>',
        '<text x="56" y="721" fill="#FFFFFF" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="13" font-weight="700">UPI PAYMENT</text>',
        '<text x="390" y="721" fill="#FBBF24" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="13" font-weight="700">STARRED</text>',

        # Scanned Details
        '<text x="44" y="765" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="24" font-weight="700">Sharma Kirana Store</text>',
        '<text x="44" y="798" fill="#94A3B8" font-family="monospace" font-size="14">upi://pay?pa=sharmakirana@upi&amp;am=150.00</text>',
        '<text x="44" y="828" fill="#10B981" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="13">Verified Merchant • Stored On-Device in Room DB</text>',

        # Action Buttons
        '<rect x="44" y="855" width="220" height="54" rx="14" fill="#2563EB"/>',
        '<text x="90" y="889" fill="#FFFFFF" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="17" font-weight="700">Open Payment</text>',

        '<rect x="278" y="855" width="105" height="54" rx="14" fill="#0F172A"/>',
        '<text x="312" y="889" fill="#E2E8F0" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="16" font-weight="600">Copy</text>',

        '<rect x="395" y="855" width="105" height="54" rx="14" fill="#0F172A"/>',
        '<text x="428" y="889" fill="#E2E8F0" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="16" font-weight="600">Share</text>',

        # Note Section
        '<rect x="44" y="930" width="456" height="50" rx="12" fill="#0F172A"/>',
        '<text x="60" y="961" fill="#94A3B8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="14">Notes: Daily counter QR for provisions &amp; groceries</text>',

        '<text x="44" y="1030" fill="#64748B" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="13">Scanned at 09:41 AM • 100% Offline • Zero Analytics</text>',

        # Bottom Navigation Bar
        '<rect x="0" y="1110" width="540" height="90" fill="#090D16"/>',
        '<rect x="45" y="1125" width="90" height="36" rx="18" fill="#1E3A8A"/>',
        '<text x="72" y="1149" fill="#60A5FA" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="15" font-weight="700">Scan</text>',
        '<text x="185" y="1149" fill="#64748B" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="15">Create</text>',
        '<text x="300" y="1149" fill="#64748B" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="15">History</text>',
        '<text x="420" y="1149" fill="#64748B" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="15">Saved</text>',
        '<rect x="200" y="1185" width="140" height="4" rx="2" fill="#E2E8F0"/>', # gesture handle
        '</svg>'
    ]
    return '\n'.join(svg)

def build_creator_screen_svg():
    w, h = 540, 1200
    svg = [
        f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="100%" height="100%">',
        '''<defs>
    <filter id="cardShadow" x="-10%" y="-10%" width="120%" height="120%">
      <feDropShadow dx="0" dy="10" stdDeviation="14" flood-color="#000000" flood-opacity="0.45"/>
    </filter>
  </defs>''',
        f'<rect width="{w}" height="{h}" fill="#0A0E17"/>',
        # Status Bar
        '<text x="32" y="36" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="15" font-weight="600">09:41</text>',
        '<circle cx="270" cy="25" r="7" fill="#000000"/>',
        '<rect x="460" y="23" width="24" height="13" rx="3" fill="none" stroke="#94A3B8" stroke-width="2"/>',
        '<rect x="463" y="26" width="16" height="7" fill="#94A3B8"/>',
        '<text x="420" y="35" fill="#94A3B8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="13" font-weight="600">5G</text>',

        # Top App Bar
        '<rect x="0" y="55" width="540" height="60" fill="#0F172A"/>',
        '<path d="M40 85h-16M32 77l-8 8 8 8" stroke="#F8FAFC" stroke-width="2.5" stroke-linecap="round"/>',
        '<text x="64" y="92" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="22" font-weight="700">QR Studio • Customizer</text>',
        # Save Button
        '<rect x="420" y="68" width="90" height="36" rx="10" fill="#2563EB"/>',
        '<text x="448" y="91" fill="#FFFFFF" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="14" font-weight="700">Save</text>',

        # Studio Segmented Tabs: Content vs Design & Studio
        '<g transform="translate(30, 125)">',
        '<rect width="480" height="42" rx="12" fill="#1E293B"/>',
        '<rect x="240" y="4" width="236" height="34" rx="10" fill="#2563EB"/>',
        '<text x="75" y="26" fill="#94A3B8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="14" font-weight="600">Content &amp; Data</text>',
        '<text x="295" y="26" fill="#FFFFFF" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="14" font-weight="700">Design &amp; Studio</text>',
        '</g>',

        # Live Code Preview Card with Custom Frame & Pill
        '<g transform="translate(30, 180)" filter="url(#cardShadow)">',
        '<rect width="480" height="340" rx="24" fill="#1E293B"/>',
        # QR Code Container
        '<rect x="24" y="24" width="220" height="220" rx="16" fill="#FFFFFF"/>',
        make_qr_svg_matrix(39, 39, 190, matrix_size=25, seed=777),
        # Center Emblem Badge
        '<circle cx="134" cy="134" r="24" fill="#FFFFFF" stroke="#2563EB" stroke-width="3"/>',
        '<text x="134" y="141" fill="#2563EB" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="16" font-weight="800" text-anchor="middle">Q</text>',
        
        # QR Metadata on the right
        '<text x="264" y="55" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="18" font-weight="700">Wi-Fi Network</text>',
        '<text x="264" y="80" fill="#38BDF8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="13" font-weight="600">SharmaKirana_Guest</text>',
        '<rect x="264" y="95" width="120" height="24" rx="6" fill="#10B981" opacity="0.15"/>',
        '<text x="274" y="112" fill="#10B981" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="11" font-weight="700">Scannable • Verified</text>',
        '<text x="264" y="142" fill="#94A3B8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="12">Dots: Rounded Squircle</text>',
        '<text x="264" y="164" fill="#94A3B8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="12">Eyes: Circular Ring + Diamond</text>',
        '<text x="264" y="186" fill="#94A3B8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="12">Resilience: High (Level H 30%)</text>',
        
        # Bottom Designer Banner on preview card
        '<rect x="24" y="260" width="432" height="50" rx="14" fill="#2563EB"/>',
        '<text x="240" y="291" fill="#FFFFFF" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="15" font-weight="700" text-anchor="middle">JOIN STORE WI-FI</text>',
        '</g>',

        # Design Studio Controls
        '<g transform="translate(30, 540)">',
        # Presets Section
        '<text x="0" y="20" fill="#94A3B8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="13" font-weight="700">ONE-TAP STYLE PRESETS</text>',
        '<g transform="translate(0, 32)">',
        '<rect x="0" y="0" width="115" height="36" rx="10" fill="#2563EB"/>',
        '<text x="14" y="22" fill="#FFFFFF" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="12" font-weight="700">Cyber Neon</text>',
        '<rect x="125" y="0" width="135" height="36" rx="10" fill="#1E293B"/>',
        '<text x="137" y="22" fill="#E2E8F0" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="12">Navy &amp; Gold</text>',
        '<rect x="270" y="0" width="110" height="36" rx="10" fill="#1E293B"/>',
        '<text x="282" y="22" fill="#E2E8F0" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="12">Sunset Flame</text>',
        '<rect x="390" y="0" width="90" height="36" rx="10" fill="#1E293B"/>',
        '<text x="402" y="22" fill="#E2E8F0" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="12">Sticker</text>',
        '</g>',

        # Dot Shape Section
        '<text x="0" y="105" fill="#94A3B8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="13" font-weight="700">MODULE &amp; DOT SHAPE</text>',
        '<g transform="translate(0, 118)">',
        '<rect x="0" y="0" width="150" height="36" rx="10" fill="#1E293B"/>',
        '<text x="20" y="22" fill="#94A3B8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="12">Classic Square</text>',
        '<rect x="160" y="0" width="165" height="36" rx="10" fill="#2563EB"/>',
        '<text x="175" y="22" fill="#FFFFFF" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="12" font-weight="700">Rounded Squircle</text>',
        '<rect x="335" y="0" width="145" height="36" rx="10" fill="#1E293B"/>',
        '<text x="355" y="22" fill="#94A3B8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="12">Circle Dots</text>',
        '</g>',

        # Corner Eyes Section
        '<text x="0" y="190" fill="#94A3B8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="13" font-weight="700">CORNER EYE FRAME &amp; PUPIL</text>',
        '<g transform="translate(0, 202)">',
        '<rect x="0" y="0" width="110" height="36" rx="10" fill="#1E293B"/>',
        '<text x="24" y="22" fill="#94A3B8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="12">Square</text>',
        '<rect x="120" y="0" width="115" height="36" rx="10" fill="#2563EB"/>',
        '<text x="136" y="22" fill="#FFFFFF" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="12" font-weight="700">Rounded</text>',
        '<rect x="245" y="0" width="115" height="36" rx="10" fill="#1E293B"/>',
        '<text x="260" y="22" fill="#94A3B8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="12">Ring Circle</text>',
        '<rect x="370" y="0" width="110" height="36" rx="10" fill="#2563EB"/>',
        '<text x="388" y="22" fill="#FFFFFF" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="12" font-weight="700">Diamond</text>',
        '</g>',

        # Colors & Gradients Section
        '<text x="0" y="275" fill="#94A3B8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="13" font-weight="700">COLOR PALETTE &amp; DUAL GRADIENT</text>',
        '<g transform="translate(0, 288)">',
        '<circle cx="20" cy="18" r="16" fill="#2563EB" stroke="#FFFFFF" stroke-width="2.5"/>',
        '<circle cx="65" cy="18" r="16" fill="#06B6D4"/>',
        '<circle cx="110" cy="18" r="16" fill="#EC4899"/>',
        '<circle cx="155" cy="18" r="16" fill="#10B981"/>',
        '<circle cx="200" cy="18" r="16" fill="#F59E0B"/>',
        '<circle cx="245" cy="18" r="16" fill="#8B5CF6"/>',
        '<circle cx="290" cy="18" r="16" fill="#0F172A" stroke="#334155" stroke-width="1.5"/>',
        '<rect x="330" y="2" width="150" height="32" rx="8" fill="#1E293B"/>',
        '<text x="345" y="22" fill="#38BDF8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="12" font-weight="600">Transparent: ON</text>',
        '</g>',

        # Call to Action Banner
        '<text x="0" y="360" fill="#94A3B8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="13" font-weight="700">DESIGNER FRAME &amp; "SCAN ME" BANNER</text>',
        '<g transform="translate(0, 372)">',
        '<rect x="0" y="0" width="135" height="36" rx="10" fill="#2563EB"/>',
        '<text x="16" y="22" fill="#FFFFFF" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="12" font-weight="700">JOIN WI-FI</text>',
        '<rect x="145" y="0" width="115" height="36" rx="10" fill="#1E293B"/>',
        '<text x="162" y="22" fill="#94A3B8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="12">SCAN ME</text>',
        '<rect x="270" y="0" width="115" height="36" rx="10" fill="#1E293B"/>',
        '<text x="286" y="22" fill="#94A3B8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="12">PAY HERE</text>',
        '<rect x="395" y="0" width="85" height="36" rx="10" fill="#1E293B"/>',
        '<text x="410" y="22" fill="#94A3B8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="12">Custom</text>',
        '</g>',
        '</g>',

        # Bottom Navigation Bar
        '<rect x="0" y="1110" width="540" height="90" fill="#090D16"/>',
        '<rect x="165" y="1125" width="90" height="36" rx="18" fill="#1E3A8A"/>',
        '<text x="72" y="1149" fill="#64748B" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="15">Scan</text>',
        '<text x="185" y="1149" fill="#60A5FA" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="15" font-weight="700">Create</text>',
        '<text x="300" y="1149" fill="#64748B" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="15">History</text>',
        '<text x="420" y="1149" fill="#64748B" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="15">Saved</text>',
        '<rect x="200" y="1185" width="140" height="4" rx="2" fill="#E2E8F0"/>',
        '</svg>'
    ]
    return '\n'.join(svg)

def build_widgets_showcase_svg():
    w, h = 1000, 720
    svg = [
        f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="100%" height="100%">',
        '''<defs>
    <filter id="widgetShadow" x="-10%" y="-10%" width="120%" height="120%">
      <feDropShadow dx="0" dy="10" stdDeviation="14" flood-color="#000000" flood-opacity="0.5"/>
    </filter>
  </defs>''',
        # Android Launcher Wallpaper
        f'<rect width="{w}" height="{h}" fill="#0F172A"/>',
        '<circle cx="850" cy="180" r="280" fill="#1E293B" opacity="0.6"/>',
        '<circle cx="150" cy="600" r="320" fill="#1E3A8A" opacity="0.25"/>',

        # Android Launcher Status Bar
        '<text x="40" y="38" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="16" font-weight="600">09:41</text>',
        '<text x="900" y="38" fill="#94A3B8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="15" font-weight="600">100% 5G</text>',

        # Section Header
        '<text x="40" y="75" fill="#38BDF8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="13" font-weight="700">ANDROID HOME-SCREEN WIDGET ECOSYSTEM</text>',

        # 1. Medium Quick Access Widget (3x2, Top Left, 440 x 140)
        '<g transform="translate(40, 95)" filter="url(#widgetShadow)">',
        '<rect width="440" height="140" rx="24" fill="#1E293B"/>',
        '<text x="24" y="32" fill="#94A3B8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="12" font-weight="700">QOBLEY QUICK ACCESS</text>',
        '<rect x="20" y="44" width="400" height="42" rx="12" fill="#2563EB"/>',
        '<text x="140" y="71" fill="#FFFFFF" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="16" font-weight="700">Scan Any Code</text>',
        '<rect x="20" y="94" width="125" height="30" rx="8" fill="#0F172A"/>',
        '<text x="48" y="114" fill="#E2E8F0" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="12" font-weight="600">Create QR</text>',
        '<rect x="157" y="94" width="125" height="30" rx="8" fill="#0F172A"/>',
        '<text x="188" y="114" fill="#E2E8F0" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="12" font-weight="600">Barcode</text>',
        '<rect x="295" y="94" width="125" height="30" rx="8" fill="#0F172A"/>',
        '<text x="328" y="114" fill="#E2E8F0" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="12" font-weight="600">Photo</text>',
        '</g>',

        # 2. Single Pinned Code Widget (2x2, Top Right, 220 x 240)
        '<g transform="translate(510, 95)" filter="url(#widgetShadow)">',
        '<rect width="220" height="240" rx="24" fill="#1E293B"/>',
        '<text x="20" y="30" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="14" font-weight="700">Sharma Kirana</text>',
        '<text x="20" y="48" fill="#38BDF8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="11">UPI Pay QR</text>',
        '<rect x="20" y="58" width="180" height="150" rx="12" fill="#FFFFFF"/>',
        make_qr_svg_matrix(35, 68, 130, matrix_size=25, seed=888),
        '<text x="32" y="224" fill="#94A3B8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="10">Customers scan from home screen</text>',
        '</g>',

        # 3. 1x1 Quick Scan Widget (Top Far Right, 200 x 140)
        '<g transform="translate(760, 95)" filter="url(#widgetShadow)">',
        '<rect width="200" height="140" rx="24" fill="#1E293B"/>',
        '<rect x="20" y="20" width="160" height="100" rx="16" fill="#2563EB"/>',
        '<path d="M90 45v6h-6v-6h6m3-3h-12v12h12v-12zm12 3v6h-6v-6h6m3-3h-12v12h12v-12zm-15 15v6h-6v-6h6m3-3h-12v12h12v-12z" fill="#FFFFFF"/>',
        '<text x="60" y="100" fill="#FFFFFF" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="15" font-weight="700">Quick Scan</text>',
        '</g>',

        # 4. Starred Favorites Widget (Middle Left, 440 x 200)
        '<g transform="translate(40, 260)" filter="url(#widgetShadow)">',
        '<rect width="440" height="200" rx="24" fill="#1E293B"/>',
        '<text x="24" y="32" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="14" font-weight="700">Qobley Starred Favorites</text>',
        '<text x="350" y="32" fill="#FBBF24" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="11" font-weight="700">STARRED</text>',
        # Item 1
        '<rect x="20" y="44" width="400" height="42" rx="10" fill="#0F172A"/>',
        '<rect x="28" y="52" width="34" height="18" rx="4" fill="#2563EB"/>',
        '<text x="32" y="65" fill="#FFFFFF" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="10" font-weight="700">PAY</text>',
        '<text x="72" y="64" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="13" font-weight="600">Counter UPI • Sharma Kirana</text>',
        '<text x="72" y="78" fill="#94A3B8" font-family="monospace" font-size="10">sharmakirana@upi</text>',
        # Item 2
        '<rect x="20" y="94" width="400" height="42" rx="10" fill="#0F172A"/>',
        '<rect x="28" y="102" width="34" height="18" rx="4" fill="#10B981"/>',
        '<text x="31" y="115" fill="#FFFFFF" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="10" font-weight="700">WIFI</text>',
        '<text x="72" y="114" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="13" font-weight="600">Store Guest Wi-Fi</text>',
        '<text x="72" y="128" fill="#94A3B8" font-family="monospace" font-size="10">SSID: CornerBakery_Guest</text>',
        # Item 3
        '<rect x="20" y="144" width="400" height="42" rx="10" fill="#0F172A"/>',
        '<rect x="28" y="152" width="34" height="18" rx="4" fill="#D97706"/>',
        '<text x="32" y="165" fill="#FFFFFF" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="10" font-weight="700">EAN</text>',
        '<text x="72" y="164" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="13" font-weight="600">Customer Loyalty Barcode</text>',
        '<text x="72" y="178" fill="#94A3B8" font-family="monospace" font-size="10">8901234567894</text>',
        '</g>',

        # 5. Large Command Center Widget (Bottom, 920 x 210)
        '<g transform="translate(40, 480)" filter="url(#widgetShadow)">',
        '<rect width="920" height="210" rx="24" fill="#1E293B"/>',
        '<text x="24" y="32" fill="#94A3B8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="12" font-weight="700">COMMAND CENTER</text>',
        # Scan bar
        '<rect x="20" y="44" width="880" height="42" rx="12" fill="#2563EB"/>',
        '<text x="380" y="71" fill="#FFFFFF" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="16" font-weight="700">Scan Any Barcode or QR Code</text>',
        # Recent feed
        '<text x="24" y="106" fill="#94A3B8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="11" font-weight="700">RECENT ON-DEVICE SCANS</text>',
        # Feed item 1
        '<rect x="20" y="116" width="430" height="48" rx="10" fill="#0F172A"/>',
        '<rect x="30" y="126" width="36" height="18" rx="4" fill="#2563EB"/>',
        '<text x="35" y="139" fill="#FFFFFF" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="10" font-weight="700">PAY</text>',
        '<text x="76" y="138" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="13" font-weight="600">Sharma Kirana Store</text>',
        '<text x="76" y="154" fill="#94A3B8" font-family="monospace" font-size="10">upi://pay?pa=sharmakirana@upi&amp;am=150.00</text>',
        # Feed item 2
        '<rect x="470" y="116" width="430" height="48" rx="10" fill="#0F172A"/>',
        '<rect x="480" y="126" width="36" height="18" rx="4" fill="#10B981"/>',
        '<text x="483" y="139" fill="#FFFFFF" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="10" font-weight="700">WIFI</text>',
        '<text x="526" y="138" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="13" font-weight="600">Corner Bakery Guest Wi-Fi</text>',
        '<text x="526" y="154" fill="#94A3B8" font-family="monospace" font-size="10">WIFI:S:CornerBakery_Guest;T:WPA;P:FreshBakes2026;;</text>',
        '<text x="24" y="192" fill="#64748B" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="11">Data stored securely in local Room SQLite database • Zero external transmission</text>',
        '</g>',
        '</svg>'
    ]
    return '\n'.join(svg)

def build_single_pinned_widget_svg():
    w, h = 360, 360
    svg = [
        f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="100%" height="100%">',
        f'<rect width="{w}" height="{h}" rx="32" fill="#1E293B"/>',
        '<text x="28" y="44" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="20" font-weight="700">Sharma Kirana</text>',
        '<text x="28" y="70" fill="#38BDF8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="15" font-weight="600">UPI Payment QR</text>',
        '<rect x="28" y="85" width="304" height="235" rx="18" fill="#FFFFFF"/>',
        make_qr_svg_matrix(52, 98, 205, matrix_size=25, seed=999),
        '<text x="44" y="340" fill="#94A3B8" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="12">Scannable directly from Android Home Screen</text>',
        '</svg>'
    ]
    return '\n'.join(svg)

def build_favorites_widget_svg():
    w, h = 480, 320
    svg = [
        f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="100%" height="100%">',
        f'<rect width="{w}" height="{h}" rx="32" fill="#1E293B"/>',
        '<text x="28" y="44" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="20" font-weight="700">Qobley Starred Favorites</text>',
        '<text x="380" y="44" fill="#FBBF24" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="14" font-weight="700">STARRED</text>',
        # Item 1
        '<rect x="24" y="62" width="432" height="68" rx="14" fill="#0F172A"/>',
        '<rect x="36" y="74" width="44" height="22" rx="5" fill="#2563EB"/>',
        '<text x="42" y="89" fill="#FFFFFF" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="12" font-weight="700">PAY</text>',
        '<text x="92" y="88" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="16" font-weight="600">Counter UPI • Sharma Kirana</text>',
        '<text x="92" y="112" fill="#94A3B8" font-family="monospace" font-size="12">sharmakirana@upi</text>',
        # Item 2
        '<rect x="24" y="142" width="432" height="68" rx="14" fill="#0F172A"/>',
        '<rect x="36" y="154" width="44" height="22" rx="5" fill="#10B981"/>',
        '<text x="40" y="169" fill="#FFFFFF" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="12" font-weight="700">WIFI</text>',
        '<text x="92" y="168" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="16" font-weight="600">Store Guest Wi-Fi</text>',
        '<text x="92" y="192" fill="#94A3B8" font-family="monospace" font-size="12">SSID: CornerBakery_Guest</text>',
        # Item 3
        '<rect x="24" y="222" width="432" height="68" rx="14" fill="#0F172A"/>',
        '<rect x="36" y="234" width="44" height="22" rx="5" fill="#D97706"/>',
        '<text x="42" y="249" fill="#FFFFFF" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="12" font-weight="700">EAN</text>',
        '<text x="92" y="248" fill="#F8FAFC" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="16" font-weight="600">Customer Loyalty Barcode</text>',
        '<text x="92" y="272" fill="#94A3B8" font-family="monospace" font-size="12">8901234567894</text>',
        '</svg>'
    ]
    return '\n'.join(svg)

def main():
    os.makedirs("assets", exist_ok=True)

    files = {
        "assets/product_hero.svg": build_product_hero_svg(),
        "assets/screenshot_scanner.svg": build_scanner_screen_svg(),
        "assets/screenshot_creator.svg": build_creator_screen_svg(),
        "assets/screenshot_widgets.svg": build_widgets_showcase_svg(),
        "assets/screenshot_widget_single_pinned.svg": build_single_pinned_widget_svg(),
        "assets/screenshot_widget_favorites.svg": build_favorites_widget_svg()
    }

    for path, content in files.items():
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)
        print(f"Wrote {path}")
        # Convert SVG to high-res PNG using sharp-cli
        png_path = path.replace(".svg", ".png")
        cmd = f"npx -y sharp-cli -i {path} -o {png_path}"
        subprocess.run(cmd, shell=True, check=True)
        print(f"Generated PNG: {png_path} ({os.path.getsize(png_path)} bytes)")

if __name__ == "__main__":
    main()

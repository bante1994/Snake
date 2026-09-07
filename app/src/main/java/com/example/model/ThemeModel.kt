package com.example.model

import androidx.compose.ui.graphics.Color

data class RetroTheme(
    val id: String,
    val name: String,
    val era: String,
    val background: Color,
    val boardBackground: Color,
    val gridColor: Color,
    val snakeHead: Color,
    val snakeBody: Color,
    val snakeEye: Color,
    val foodRed: Color,
    val foodGolden: Color,
    val hudText: Color,
    val accent: Color,
    val crtScanlines: Boolean = true
)

object RetroThemes {
    val CYBERPUNK = RetroTheme(
        id = "cyberpunk",
        name = "Neon Cyberpunk",
        era = "Neo Tokyo 2099",
        background = Color(0xFF0D0221),
        boardBackground = Color(0xFF190033),
        gridColor = Color(0x3000D2FF),
        snakeHead = Color(0xFF00FFE0),
        snakeBody = Color(0xFF00A3FF),
        snakeEye = Color(0xFF0D0221),
        foodRed = Color(0xFFFF007F),
        foodGolden = Color(0xFFFFD700),
        hudText = Color(0xFF00FFE0),
        accent = Color(0xFFFF007F),
        crtScanlines = true
    )

    val COBALT_LASER = RetroTheme(
        id = "cobalt_laser",
        name = "Cobalt Laser",
        era = "Arcade Blue '86",
        background = Color(0xFF080F1E),
        boardBackground = Color(0xFF0F1D38),
        gridColor = Color(0x303B82F6),
        snakeHead = Color(0xFF38BDF8),
        snakeBody = Color(0xFF2563EB),
        snakeEye = Color(0xFF080F1E),
        foodRed = Color(0xFFEF4444),
        foodGolden = Color(0xFFFBBF24),
        hudText = Color(0xFFBAE6FD),
        accent = Color(0xFF38BDF8),
        crtScanlines = false
    )

    val AMBER_CRT = RetroTheme(
        id = "amber_crt",
        name = "Amber CRT",
        era = "VT220 Phosphor",
        background = Color(0xFF140D00),
        boardBackground = Color(0xFF241500),
        gridColor = Color(0x28FFB000),
        snakeHead = Color(0xFFFFCC33),
        snakeBody = Color(0xFFFF9900),
        snakeEye = Color(0xFF241500),
        foodRed = Color(0xFFFF5500),
        foodGolden = Color(0xFFFFF0AA),
        hudText = Color(0xFFFFB000),
        accent = Color(0xFFFFCC33),
        crtScanlines = true
    )

    val SYNTHWAVE = RetroTheme(
        id = "synthwave",
        name = "Synthwave Sunset",
        era = "Outrun 1984",
        background = Color(0xFF180826),
        boardBackground = Color(0xFF2E0F46),
        gridColor = Color(0x30FF007F),
        snakeHead = Color(0xFFFF7700),
        snakeBody = Color(0xFFFF3377),
        snakeEye = Color(0xFF180826),
        foodRed = Color(0xFFE000AA),
        foodGolden = Color(0xFFFFDE59),
        hudText = Color(0xFFFF77AA),
        accent = Color(0xFFFF7700),
        crtScanlines = true
    )

    val CRIMSON_ARCADE = RetroTheme(
        id = "crimson",
        name = "Crimson Arcade",
        era = "Blood Dragon '87",
        background = Color(0xFF121216),
        boardBackground = Color(0xFF1E1E26),
        gridColor = Color(0x28FF3344),
        snakeHead = Color(0xFFFF3344),
        snakeBody = Color(0xFFFF6644),
        snakeEye = Color(0xFF121216),
        foodRed = Color(0xFFCC2288),
        foodGolden = Color(0xFFFFCC00),
        hudText = Color(0xFFF1F5F9),
        accent = Color(0xFFFF3344),
        crtScanlines = true
    )

    val MONOCHROME_OLED = RetroTheme(
        id = "monochrome",
        name = "OLED Pure Pixel",
        era = "Minimalist Noir",
        background = Color(0xFF000000),
        boardBackground = Color(0xFF121214),
        gridColor = Color(0x20FFFFFF),
        snakeHead = Color(0xFFFFFFFF),
        snakeBody = Color(0xFFA0A0A0),
        snakeEye = Color(0xFF000000),
        foodRed = Color(0xFFFFFFFF),
        foodGolden = Color(0xFFD4D4D8),
        hudText = Color(0xFFF4F4F5),
        accent = Color(0xFFFFFFFF),
        crtScanlines = false
    )

    val ALL_THEMES = listOf(CYBERPUNK, COBALT_LASER, AMBER_CRT, SYNTHWAVE, CRIMSON_ARCADE, MONOCHROME_OLED)

    fun getThemeById(id: String): RetroTheme =
        ALL_THEMES.find { it.id == id } ?: CYBERPUNK
}

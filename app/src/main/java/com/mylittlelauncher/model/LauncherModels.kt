package com.mylittlelauncher.model

import android.content.pm.ResolveInfo

enum class LauncherThemePreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val isDark: Boolean
) {
    SOFT_MINT("soft_mint", "Soft Mint Luxury", "Mint pastel, sage & ivory hangat", false),
    WARM_PEACH("warm_peach", "Warm Peach Blossom", "Peach lembut, krem susu & terakota", false),
    LAVENDER_DREAM("lavender_dream", "Lavender Lullaby", "Ungu pastel tenang & putih mutiara", false),
    MIDNIGHT_SAGE("midnight_sage", "Midnight Forest", "AMOLED gelap elegan dengan aksen sage", true)
}

enum class IconShapeOption(val id: String, val label: String) {
    SQUIRCLE("squircle", "Squircle"),
    CIRCLE("circle", "Circle"),
    ROUNDED("rounded", "Rounded")
}

enum class AppCategory(val label: String) {
    ALL("Semua"),
    FAVORITES("Favorit"),
    COMMUNICATION("Komunikasi"),
    MEDIA("Hiburan & Foto"),
    TOOLS("Alat & Utilitas"),
    OTHER("Lainnya")
}

data class LauncherApp(
    val label: String,
    val packageName: String,
    val className: String,
    val resolveInfo: ResolveInfo,
    val category: AppCategory
)

enum class LauncherPage {
    HOME,
    DRAWER,
    LOCK,
    SETTINGS
}

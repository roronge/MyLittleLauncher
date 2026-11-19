package com.mylittlelauncher.data

import android.content.Context
import android.net.Uri
import com.mylittlelauncher.model.IconShapeOption
import com.mylittlelauncher.model.LauncherThemePreset

class LauncherPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("my_little_launcher_v3", Context.MODE_PRIVATE)

    fun getPhotoUri(): Uri? = prefs.getString("photo_uri", null)?.let(Uri::parse)

    fun setPhotoUri(uri: Uri?) {
        if (uri == null) prefs.edit().remove("photo_uri").apply()
        else prefs.edit().putString("photo_uri", uri.toString()).apply()
    }

    fun getChildName(): String = prefs.getString("child_name", "Axcell") ?: "Axcell"
    fun setChildName(name: String) = prefs.edit().putString("child_name", name.ifBlank { "Axcell" }).apply()

    fun getBirthDateIso(): String = prefs.getString("birth_date", "2026-08-13") ?: "2026-08-13"
    fun setBirthDateIso(iso: String) = prefs.edit().putString("birth_date", iso).apply()

    fun getHomeQuote(): String = prefs.getString("home_quote", "Little moments, forever.") ?: "Little moments, forever."
    fun setHomeQuote(quote: String) = prefs.edit().putString("home_quote", quote.ifBlank { "Little moments, forever." }).apply()

    fun getThemePreset(): LauncherThemePreset {
        val id = prefs.getString("theme_preset", LauncherThemePreset.SOFT_MINT.id)
        return LauncherThemePreset.entries.find { it.id == id } ?: LauncherThemePreset.SOFT_MINT
    }

    fun setThemePreset(preset: LauncherThemePreset) {
        prefs.edit().putString("theme_preset", preset.id).apply()
    }

    fun getIconShape(): IconShapeOption {
        val id = prefs.getString("icon_shape", IconShapeOption.SQUIRCLE.id)
        return IconShapeOption.entries.find { it.id == id } ?: IconShapeOption.SQUIRCLE
    }

    fun setIconShape(shape: IconShapeOption) {
        prefs.edit().putString("icon_shape", shape.id).apply()
    }

    fun getDrawerColumns(): Int = prefs.getInt("drawer_columns", 4).coerceIn(3, 5)
    fun setDrawerColumns(cols: Int) = prefs.edit().putInt("drawer_columns", cols.coerceIn(3, 5)).apply()

    fun getDockPackages(): List<String> {
        val raw = prefs.getString("dock_packages", "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split("|").filter { it.isNotBlank() }
    }

    fun setDockPackages(packages: List<String>) {
        prefs.edit().putString("dock_packages", packages.take(4).joinToString("|")).apply()
    }

    fun getPinnedHomePackages(): List<String> {
        val raw = prefs.getString("pinned_home_packages", "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split("|").filter { it.isNotBlank() }
    }

    fun setPinnedHomePackages(packages: List<String>) {
        prefs.edit().putString("pinned_home_packages", packages.take(8).joinToString("|")).apply()
    }

    fun getHiddenPackages(): Set<String> {
        return prefs.getStringSet("hidden_packages", emptySet()) ?: emptySet()
    }

    fun setHiddenPackages(packages: Set<String>) {
        prefs.edit().putStringSet("hidden_packages", packages).apply()
    }

    fun getFavoritePackages(): Set<String> {
        return prefs.getStringSet("favorite_packages", emptySet()) ?: emptySet()
    }

    fun setFavoritePackages(packages: Set<String>) {
        prefs.edit().putStringSet("favorite_packages", packages).apply()
    }

    fun isDoubleTapLockTipShown(): Boolean = prefs.getBoolean("double_tap_tip", true)
    fun setDoubleTapLockTipShown(show: Boolean) = prefs.edit().putBoolean("double_tap_tip", show).apply()
}

package com.mylittlelauncher.data

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.mylittlelauncher.model.AppCategory
import com.mylittlelauncher.model.LauncherApp
import java.util.Locale

object AppRepository {

    fun loadInstalledApps(context: Context): List<LauncherApp> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, 0)
            .filter { it.activityInfo.packageName != context.packageName }
            .map { resolveInfo ->
                val pkg = resolveInfo.activityInfo.packageName
                val label = resolveInfo.loadLabel(pm).toString()
                val category = detectCategory(context, pkg, label)
                LauncherApp(
                    label = label,
                    packageName = pkg,
                    className = resolveInfo.activityInfo.name,
                    resolveInfo = resolveInfo,
                    category = category
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase(Locale.getDefault()) }
    }

    private fun detectCategory(context: Context, packageName: String, label: String): AppCategory {
        val lowerPkg = packageName.lowercase(Locale.ROOT)
        val lowerLabel = label.lowercase(Locale.ROOT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appCategory = runCatching {
                context.packageManager.getApplicationInfo(packageName, 0).category
            }.getOrDefault(ApplicationInfo.CATEGORY_UNDEFINED)

            when (appCategory) {
                ApplicationInfo.CATEGORY_SOCIAL -> return AppCategory.COMMUNICATION
                ApplicationInfo.CATEGORY_AUDIO,
                ApplicationInfo.CATEGORY_VIDEO,
                ApplicationInfo.CATEGORY_IMAGE,
                ApplicationInfo.CATEGORY_GAME -> return AppCategory.MEDIA
                ApplicationInfo.CATEGORY_PRODUCTIVITY,
                ApplicationInfo.CATEGORY_MAPS -> return AppCategory.TOOLS
            }
        }

        return when {
            listOf("whatsapp", "telegram", "dialer", "contacts", "mms", "message", "mail", "gmail", "instagram", "facebook", "threads", "tiktok", "line", "discord").any {
                lowerPkg.contains(it) || lowerLabel.contains(it)
            } -> AppCategory.COMMUNICATION

            listOf("camera", "gallery", "photos", "youtube", "spotify", "music", "video", "netflix", "album", "miui.player", "miui.gallery").any {
                lowerPkg.contains(it) || lowerLabel.contains(it)
            } -> AppCategory.MEDIA

            listOf("settings", "calculator", "clock", "deskclock", "calendar", "files", "filemanager", "chrome", "browser", "maps", "notes", "compass", "weather", "scanner").any {
                lowerPkg.contains(it) || lowerLabel.contains(it)
            } -> AppCategory.TOOLS

            else -> AppCategory.OTHER
        }
    }

    fun launchApp(context: Context, packageName: String) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            runCatching { context.startActivity(launchIntent) }
        }
    }

    fun openAppInfo(context: Context, packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    fun uninstallApp(context: Context, packageName: String) {
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    fun openDefaultLauncherSettings(context: Context) {
        val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    fun openClockApp(context: Context) {
        val intent = Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    fun openCalendarApp(context: Context) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_APP_CALENDAR)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }
}

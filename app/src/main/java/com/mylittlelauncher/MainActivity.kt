package com.mylittlelauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.mylittlelauncher.data.AppRepository
import com.mylittlelauncher.data.LauncherPreferences
import com.mylittlelauncher.model.LauncherApp
import com.mylittlelauncher.model.LauncherPage
import com.mylittlelauncher.ui.screens.AppDrawerScreen
import com.mylittlelauncher.ui.screens.HomeScreen
import com.mylittlelauncher.ui.screens.LockScreenPreview
import com.mylittlelauncher.ui.screens.SettingsScreen
import com.mylittlelauncher.ui.theme.MyLittleLauncherTheme
import kotlinx.coroutines.delay
import java.time.LocalDateTime

class MainActivity : ComponentActivity() {

    private var onHomeKeyPressed: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        setContent {
            val context = LocalContext.current
            val prefs = remember { LauncherPreferences(context) }

            var selectedPhoto by remember { mutableStateOf(prefs.getPhotoUri()) }
            var childName by remember { mutableStateOf(prefs.getChildName()) }
            var birthDateIso by remember { mutableStateOf(prefs.getBirthDateIso()) }
            var homeQuote by remember { mutableStateOf(prefs.getHomeQuote()) }
            var themePreset by remember { mutableStateOf(prefs.getThemePreset()) }
            var iconShape by remember { mutableStateOf(prefs.getIconShape()) }
            var drawerColumns by remember { mutableIntStateOf(prefs.getDrawerColumns()) }

            var dockPackages by remember { mutableStateOf(prefs.getDockPackages()) }
            var pinnedHomePackages by remember { mutableStateOf(prefs.getPinnedHomePackages()) }
            var hiddenPackages by remember { mutableStateOf(prefs.getHiddenPackages()) }
            var favoritePackages by remember { mutableStateOf(prefs.getFavoritePackages()) }

            var page by remember { mutableStateOf(LauncherPage.HOME) }
            var autoFocusDrawerSearch by remember { mutableStateOf(false) }
            var now by remember { mutableStateOf(LocalDateTime.now()) }

            val allApps = remember {
                mutableStateListOf<LauncherApp>().apply {
                    addAll(AppRepository.loadInstalledApps(context))
                }
            }

            // Return to Home when Home button is pressed while already in launcher
            SideEffect {
                onHomeKeyPressed = {
                    autoFocusDrawerSearch = false
                    page = LauncherPage.HOME
                }
            }

            // Real-time package install / uninstall / update listener
            DisposableEffect(Unit) {
                val receiver = object : BroadcastReceiver() {
                    override fun onReceive(ctx: Context?, intent: Intent?) {
                        allApps.clear()
                        allApps.addAll(AppRepository.loadInstalledApps(context))
                    }
                }
                val filter = IntentFilter().apply {
                    addAction(Intent.ACTION_PACKAGE_ADDED)
                    addAction(Intent.ACTION_PACKAGE_REMOVED)
                    addAction(Intent.ACTION_PACKAGE_CHANGED)
                    addDataScheme("package")
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
                } else {
                    @Suppress("DEPRECATION")
                    context.registerReceiver(receiver, filter)
                }
                onDispose {
                    runCatching { context.unregisterReceiver(receiver) }
                }
            }

            // Live clock ticker
            LaunchedEffect(Unit) {
                while (true) {
                    now = LocalDateTime.now()
                    delay(1000L)
                }
            }

            val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                if (uri != null) {
                    runCatching {
                        context.contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    }
                    selectedPhoto = uri
                    prefs.setPhotoUri(uri)
                }
            }

            val resolvedDockApps = remember(allApps.toList(), dockPackages) {
                if (dockPackages.isEmpty()) {
                    allApps.take(4)
                } else {
                    dockPackages.mapNotNull { pkg -> allApps.find { it.packageName == pkg } }
                        .ifEmpty { allApps.take(4) }
                }
            }

            val resolvedPinnedApps = remember(allApps.toList(), pinnedHomePackages) {
                pinnedHomePackages.mapNotNull { pkg -> allApps.find { it.packageName == pkg } }
            }

            MyLittleLauncherTheme(preset = themePreset) {
                AnimatedContent(
                    targetState = page,
                    transitionSpec = {
                        when {
                            targetState == LauncherPage.DRAWER -> {
                                (slideInVertically(tween(240)) { it / 4 } + fadeIn(tween(200))) togetherWith
                                    fadeOut(tween(150))
                            }
                            initialState == LauncherPage.DRAWER && targetState == LauncherPage.HOME -> {
                                fadeIn(tween(180)) togetherWith
                                    (slideOutVertically(tween(220)) { it / 4 } + fadeOut(tween(150)))
                            }
                            else -> fadeIn(tween(200)) togetherWith fadeOut(tween(150))
                        }
                    },
                    label = "launcher-navigation"
                ) { target ->
                    when (target) {
                        LauncherPage.HOME -> HomeScreen(
                            photo = selectedPhoto,
                            time = now,
                            childName = childName,
                            birthDateIso = birthDateIso,
                            homeQuote = homeQuote,
                            iconShape = iconShape,
                            dockApps = resolvedDockApps,
                            pinnedHomeApps = resolvedPinnedApps,
                            favoritePackages = favoritePackages,
                            onOpenDrawer = { focusSearch ->
                                autoFocusDrawerSearch = focusSearch
                                page = LauncherPage.DRAWER
                            },
                            onOpenLockPreview = { page = LauncherPage.LOCK },
                            onOpenSettings = { page = LauncherPage.SETTINGS },
                            onEditPhotoOrMilestone = { page = LauncherPage.SETTINGS },
                            onToggleFavorite = { pkg ->
                                favoritePackages = if (pkg in favoritePackages) favoritePackages - pkg else favoritePackages + pkg
                                prefs.setFavoritePackages(favoritePackages)
                            },
                            onTogglePinHome = { pkg ->
                                pinnedHomePackages = if (pkg in pinnedHomePackages) {
                                    pinnedHomePackages - pkg
                                } else {
                                    (pinnedHomePackages + pkg).take(8)
                                }
                                prefs.setPinnedHomePackages(pinnedHomePackages)
                            },
                            onLaunch = { AppRepository.launchApp(context, it.packageName) }
                        )

                        LauncherPage.DRAWER -> AppDrawerScreen(
                            apps = allApps,
                            favoritePackages = favoritePackages,
                            pinnedHomePackages = pinnedHomePackages,
                            hiddenPackages = hiddenPackages,
                            drawerColumns = drawerColumns,
                            iconShape = iconShape,
                            autoFocusSearch = autoFocusDrawerSearch,
                            onBack = {
                                autoFocusDrawerSearch = false
                                page = LauncherPage.HOME
                            },
                            onOpenSettings = { page = LauncherPage.SETTINGS },
                            onToggleFavorite = { pkg ->
                                favoritePackages = if (pkg in favoritePackages) favoritePackages - pkg else favoritePackages + pkg
                                prefs.setFavoritePackages(favoritePackages)
                            },
                            onTogglePinHome = { pkg ->
                                pinnedHomePackages = if (pkg in pinnedHomePackages) {
                                    pinnedHomePackages - pkg
                                } else {
                                    (pinnedHomePackages + pkg).take(8)
                                }
                                prefs.setPinnedHomePackages(pinnedHomePackages)
                            },
                            onHideApp = { pkg ->
                                hiddenPackages = hiddenPackages + pkg
                                prefs.setHiddenPackages(hiddenPackages)
                            },
                            onLaunch = { AppRepository.launchApp(context, it.packageName) }
                        )

                        LauncherPage.LOCK -> LockScreenPreview(
                            photo = selectedPhoto,
                            time = now,
                            childName = childName,
                            birthDateIso = birthDateIso,
                            homeQuote = homeQuote,
                            onUnlockPreview = { page = LauncherPage.HOME }
                        )

                        LauncherPage.SETTINGS -> SettingsScreen(
                            allApps = allApps,
                            childName = childName,
                            birthDateIso = birthDateIso,
                            homeQuote = homeQuote,
                            themePreset = themePreset,
                            iconShape = iconShape,
                            drawerColumns = drawerColumns,
                            dockPackages = dockPackages,
                            hiddenPackages = hiddenPackages,
                            hasCustomPhoto = selectedPhoto != null,
                            onBack = { page = LauncherPage.HOME },
                            onChoosePhoto = { photoPicker.launch(arrayOf("image/*")) },
                            onResetPhoto = {
                                selectedPhoto = null
                                prefs.setPhotoUri(null)
                            },
                            onSaveProfile = { name, birthIso, quote ->
                                childName = name.ifBlank { "Axcell" }
                                birthDateIso = birthIso
                                homeQuote = quote.ifBlank { "Little moments, forever." }
                                prefs.setChildName(childName)
                                prefs.setBirthDateIso(birthDateIso)
                                prefs.setHomeQuote(homeQuote)
                            },
                            onSelectTheme = {
                                themePreset = it
                                prefs.setThemePreset(it)
                            },
                            onSelectIconShape = {
                                iconShape = it
                                prefs.setIconShape(it)
                            },
                            onSelectDrawerColumns = {
                                drawerColumns = it
                                prefs.setDrawerColumns(it)
                            },
                            onUpdateDockPackages = {
                                dockPackages = it
                                prefs.setDockPackages(it)
                            },
                            onRestoreHiddenPackage = { pkg ->
                                hiddenPackages = hiddenPackages - pkg
                                prefs.setHiddenPackages(hiddenPackages)
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == Intent.ACTION_MAIN && intent.hasCategory(Intent.CATEGORY_HOME)) {
            onHomeKeyPressed?.invoke()
        }
    }
}

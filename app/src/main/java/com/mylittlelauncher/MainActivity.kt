package com.mylittlelauncher

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Mint = Color(0xFFDCEFE5)
private val Sage = Color(0xFFB7D7C5)
private val Ivory = Color(0xFFF8F7F2)
private val Ink = Color(0xFF29332F)
private val Muted = Color(0xFF65736B)
private val Glass = Color(0xEFFFFFFF)

data class LauncherApp(
    val label: String,
    val packageName: String,
    val resolveInfo: ResolveInfo
)

private enum class LauncherPage { HOME, DRAWER, LOCK, SETTINGS }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
            View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        setContent { SoftMintLauncher() }
    }
}

@Composable
private fun SoftMintLauncher() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("soft_mint_preferences", Context.MODE_PRIVATE) }
    var selectedPhoto by remember {
        mutableStateOf(prefs.getString("photo_uri", null)?.let(Uri::parse))
    }
    var page by remember { mutableStateOf(LauncherPage.HOME) }
    var search by remember { mutableStateOf("") }
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    var showPhotoMenu by remember { mutableStateOf(false) }
    val apps = remember { mutableStateListOf<LauncherApp>().apply { addAll(loadApps(context)) } }

    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(1000L)
        }
    }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            selectedPhoto = uri
            prefs.edit().putString("photo_uri", uri.toString()).apply()
        }
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Ink,
            onPrimary = Color.White,
            secondary = Sage,
            background = Ivory,
            surface = Ivory,
            onSurface = Ink
        )
    ) {
        AnimatedContent(
            targetState = page,
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
            label = "launcher-pages"
        ) { target ->
            when (target) {
                LauncherPage.HOME -> HomeScreen(
                    photo = selectedPhoto,
                    time = now,
                    onOpenDrawer = { page = LauncherPage.DRAWER },
                    onOpenLockPreview = { page = LauncherPage.LOCK },
                    onOpenSettings = { page = LauncherPage.SETTINGS },
                    onPhotoLongPress = { showPhotoMenu = true },
                    apps = apps,
                    onLaunch = { launchApp(context, it) }
                )
                LauncherPage.DRAWER -> AppDrawer(
                    apps = apps,
                    search = search,
                    onSearch = { search = it },
                    onBack = { page = LauncherPage.HOME },
                    onLaunch = { launchApp(context, it) }
                )
                LauncherPage.LOCK -> LockScreenPreview(
                    photo = selectedPhoto,
                    time = now,
                    onUnlockPreview = { page = LauncherPage.HOME }
                )
                LauncherPage.SETTINGS -> SettingsScreen(
                    onBack = { page = LauncherPage.HOME },
                    onChoosePhoto = { photoPicker.launch(arrayOf("image/*")) },
                    onResetPhoto = {
                        selectedPhoto = null
                        prefs.edit().remove("photo_uri").apply()
                    },
                    hasCustomPhoto = selectedPhoto != null
                )
            }
        }

        if (showPhotoMenu) {
            AlertDialog(
                onDismissRequest = { showPhotoMenu = false },
                title = { Text("Foto kenangan") },
                text = { Text("Gunakan foto lain dari galeri untuk tampilan launcher.") },
                confirmButton = {
                    TextButton(onClick = {
                        showPhotoMenu = false
                        photoPicker.launch(arrayOf("image/*"))
                    }) { Text("Pilih foto") }
                },
                dismissButton = {
                    TextButton(onClick = { showPhotoMenu = false }) { Text("Batal") }
                }
            )
        }
    }
}

@Composable
private fun HomeScreen(
    photo: Uri?,
    time: LocalDateTime,
    onOpenDrawer: () -> Unit,
    onOpenLockPreview: () -> Unit,
    onOpenSettings: () -> Unit,
    onPhotoLongPress: () -> Unit,
    apps: List<LauncherApp>,
    onLaunch: (LauncherApp) -> Unit
) {
    val context = LocalContext.current
    val hour = time.format(DateTimeFormatter.ofPattern("HH:mm"))
    val date = time.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("id", "ID")))

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Mint, Ivory, Color(0xFFEAF1EB))))) {
        AsyncImage(
            model = photo ?: R.drawable.baby_photo,
            contentDescription = "Foto anak",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                0f to Color(0x55313C36),
                0.35f to Color.Transparent,
                0.70f to Color.Transparent,
                1f to Color(0xAA25332D)
            )
        ))

        Column(
            modifier = Modifier.fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("MY LITTLE WORLD", color = Color.White.copy(alpha = .88f),
                        fontSize = 10.sp, letterSpacing = 2.2.sp, fontWeight = FontWeight.SemiBold)
                    Text("A little piece of home", color = Color.White,
                        fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                }
            }

            Spacer(Modifier.height(22.dp))
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                Text(hour, color = Color.White, fontSize = 64.sp,
                    fontWeight = FontWeight.ExtraLight, letterSpacing = (-3).sp)
                Text(date.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("id", "ID")) else it.toString() },
                    color = Color.White.copy(alpha = .94f), fontSize = 14.sp)
            }

            Spacer(Modifier.weight(1f))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Surface(color = Color.White.copy(alpha = .82f), shape = RoundedCornerShape(50)) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Cloud, null, tint = Ink, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Soft Mint", color = Ink, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    Text("Little moments, forever.", color = Color.White,
                        fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                Surface(
                    onClick = onOpenLockPreview,
                    color = Color.White.copy(alpha = .78f),
                    shape = CircleShape,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Lock, contentDescription = "Lock screen preview", tint = Ink)
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Surface(
                color = Color(0xEFFFFFFF),
                shape = RoundedCornerShape(28.dp),
                shadowElevation = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(horizontal = 9.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val shortcuts = apps.take(4)
                    if (shortcuts.isEmpty()) {
                        DockButton(Icons.Default.Apps, "Apps", onOpenDrawer)
                    } else {
                        shortcuts.forEach { app ->
                            AppIcon(app, Modifier.weight(1f), onClick = { onLaunch(app) }, labelVisible = false)
                        }
                    }
                    Box(Modifier.width(1.dp).height(34.dp).background(Sage))
                    DockButton(Icons.Default.Apps, "All apps", onOpenDrawer)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("SWIPE UP OR TAP ALL APPS", color = Color.White,
                fontSize = 9.sp, letterSpacing = 1.6.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onOpenDrawer))
        }
    }
}

@Composable
private fun AppDrawer(
    apps: List<LauncherApp>,
    search: String,
    onSearch: (String) -> Unit,
    onBack: () -> Unit,
    onLaunch: (LauncherApp) -> Unit
) {
    val filtered = remember(apps, search) {
        apps.filter { it.label.contains(search, ignoreCase = true) }
    }
    Column(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Mint, Ivory)))
            .statusBarsPadding().navigationBarsPadding().padding(horizontal = 20.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("YOUR APPS", fontSize = 10.sp, color = Muted, letterSpacing = 2.sp)
                Text("App Library", fontSize = 30.sp, color = Ink, fontWeight = FontWeight.Light)
            }
            IconButton(onClick = onBack) { Icon(Icons.Default.Home, "Home", tint = Ink) }
        }
        OutlinedTextField(
            value = search,
            onValueChange = onSearch,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Search apps") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            shape = RoundedCornerShape(22.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Sage,
                unfocusedBorderColor = Color(0xFFD5E2D9),
                focusedContainerColor = Color.White.copy(alpha = .8f),
                unfocusedContainerColor = Color.White.copy(alpha = .8f)
            )
        )
        Spacer(Modifier.height(16.dp))
        Text("${filtered.size} APPLICATIONS", color = Muted, fontSize = 10.sp,
            letterSpacing = 1.5.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))
        LazyVerticalGrid(
            columns = GridCells.Adaptive(76.dp),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(filtered, key = { it.packageName }) { app ->
                AppIcon(app, Modifier.fillMaxWidth(), onClick = { onLaunch(app) }, labelVisible = true)
            }
        }
    }
}

@Composable
private fun AppIcon(
    app: LauncherApp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    labelVisible: Boolean
) {
    val context = LocalContext.current
    Column(
        modifier = modifier.clip(RoundedCornerShape(18.dp)).clickable(onClick = onClick)
            .padding(vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(54.dp).shadow(5.dp, RoundedCornerShape(17.dp)),
            shape = RoundedCornerShape(17.dp),
            color = Color.White.copy(alpha = .96f)
        ) {
            val drawable = remember(app.packageName) {
                runCatching { context.packageManager.getApplicationIcon(app.packageName) }.getOrNull()
            }
            if (drawable != null) {
                AndroidView(
                    factory = { ctx ->
                        ImageView(ctx).apply {
                            setImageDrawable(drawable)
                            scaleType = ImageView.ScaleType.FIT_CENTER
                            setPadding(9, 9, 9, 9)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        if (labelVisible) {
            Spacer(Modifier.height(6.dp))
            Text(app.label, color = Ink, fontSize = 10.sp, maxLines = 1,
                overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun DockButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(Modifier.clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = Ink, modifier = Modifier.size(25.dp))
        Text(label, color = Muted, fontSize = 9.sp)
    }
}

@Composable
private fun LockScreenPreview(photo: Uri?, time: LocalDateTime, onUnlockPreview: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Ink).clickable(onClick = onUnlockPreview)) {
        AsyncImage(
            model = photo ?: R.drawable.baby_photo,
            contentDescription = "Lock screen wallpaper preview",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0x66313C36), Color(0x22313C36), Color(0xCC18231E)))
        ))
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
                .padding(horizontal = 28.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("THURSDAY · MY LITTLE WORLD", color = Color.White.copy(alpha = .9f),
                fontSize = 10.sp, letterSpacing = 2.sp)
            Spacer(Modifier.height(48.dp))
            Text(time.format(DateTimeFormatter.ofPattern("HH:mm")),
                color = Color.White, fontSize = 76.sp, fontWeight = FontWeight.ExtraLight,
                letterSpacing = (-3).sp)
            Text(time.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("id", "ID"))),
                color = Color.White, fontSize = 14.sp)
            Spacer(Modifier.weight(1f))
            Surface(color = Color.White.copy(alpha = .20f), shape = RoundedCornerShape(22.dp)) {
                Row(Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("A little piece of home", color = Color.White, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(24.dp))
            Surface(color = Color.White.copy(alpha = .88f), shape = CircleShape,
                modifier = Modifier.size(58.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Fingerprint, null, tint = Ink, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("TAP TO RETURN", color = Color.White, fontSize = 9.sp, letterSpacing = 2.sp)
        }
    }
}

@Composable
private fun SettingsScreen(
    onBack: () -> Unit,
    onChoosePhoto: () -> Unit,
    onResetPhoto: () -> Unit,
    hasCustomPhoto: Boolean
) {
    Column(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Mint, Ivory)))
            .statusBarsPadding().navigationBarsPadding().padding(22.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.Home, "Back", tint = Ink) }
            Text("Personalization", color = Ink, fontSize = 26.sp, fontWeight = FontWeight.Light)
        }
        Spacer(Modifier.height(22.dp))
        Text("APPEARANCE", color = Muted, fontSize = 10.sp, letterSpacing = 2.sp)
        Spacer(Modifier.height(10.dp))
        Surface(color = Glass, shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Wallpaper, null, tint = Ink)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Home & lock wallpaper", color = Ink, fontWeight = FontWeight.Medium)
                        Text("Choose a photo from your device", color = Muted, fontSize = 12.sp)
                    }
                }
                Button(onClick = onChoosePhoto, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Ink),
                    shape = RoundedCornerShape(16.dp)) {
                    Icon(Icons.Default.CameraAlt, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Choose photo")
                }
                if (hasCustomPhoto) {
                    TextButton(onClick = onResetPhoto) { Text("Restore original baby photo", color = Muted) }
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Surface(color = Glass, shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(18.dp)) {
                Text("SOFT MINT LUXURY", color = Ink, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text("A calm palette inspired by the original family photo: soft mint, sage, ivory and warm peach.",
                    color = Muted, fontSize = 13.sp, lineHeight = 19.sp)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(Mint, Sage, Ivory, Color(0xFFE9C9B8), Ink).forEach {
                        Box(Modifier.weight(1f).height(30.dp).clip(RoundedCornerShape(9.dp)).background(it))
                    }
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("DEVICE COMPATIBILITY", color = Muted, fontSize = 10.sp, letterSpacing = 2.sp)
        Spacer(Modifier.height(8.dp))
        Text("Designed for Android phones including POCO F3. Some system features such as secure lock screen and Quick Settings remain controlled by Android / HyperOS.",
            color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
    }
}

private fun loadApps(context: Context): List<LauncherApp> {
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return context.packageManager.queryIntentActivities(intent, 0)
        .map {
            LauncherApp(
                label = it.loadLabel(context.packageManager).toString(),
                packageName = it.activityInfo.packageName,
                resolveInfo = it
            )
        }
        .distinctBy { it.packageName }
        .sortedBy { it.label.lowercase(Locale.getDefault()) }
}

private fun launchApp(context: Context, app: LauncherApp) {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
    if (launchIntent != null) context.startActivity(launchIntent)
}

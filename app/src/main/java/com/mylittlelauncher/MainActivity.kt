package com.mylittlelauncher

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import android.content.pm.ResolveInfo
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.isSystemInDarkTheme

private val Ink = Color(0xFF25252B)
private val Muted = Color(0xFF777781)
private val Canvas = Color(0xFFF7F6F3)
private val Accent = Color(0xFFB69A8B)

data class LauncherApp(
    val label: String,
    val packageName: String,
    val resolveInfo: ResolveInfo
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LauncherRoot() }
    }
}

@Composable
private fun LauncherRoot() {
    val context = LocalContext.current
    val activity = context as Activity
    val prefs = remember { context.getSharedPreferences("launcher_prefs", 0) }
    var wallpaperUri by remember { mutableStateOf(prefs.getString("wallpaper_uri", null)?.let(Uri::parse)) }
    var allApps by remember { mutableStateOf(loadApps(context)) }
    var showDrawer by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var showSettings by remember { mutableStateOf(false) }

    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            wallpaperUri = uri
            prefs.edit().putString("wallpaper_uri", uri.toString()).apply()
        }
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Ink,
            onPrimary = Color.White,
            background = Canvas,
            surface = Color.White,
            onSurface = Ink
        )
    ) {
        Box(Modifier.fillMaxSize().background(Canvas)) {
            if (wallpaperUri != null) {
                AsyncImage(
                    model = wallpaperUri,
                    contentDescription = "Foto wallpaper",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.24f)))
            } else {
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(Color(0xFFF0E9E4), Canvas, Color(0xFFECE8E2)))
                    )
                )
            }

            Column(
                modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
                    .padding(horizontal = 22.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Pengaturan",
                            tint = if (wallpaperUri != null) Color.White else Ink)
                    }
                }

                Spacer(Modifier.height(34.dp))
                Text(
                    text = java.time.LocalTime.now().let { "%02d:%02d".format(it.hour, it.minute) },
                    color = if (wallpaperUri != null) Color.White else Ink,
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-2).sp
                )
                Text(
                    text = java.time.LocalDate.now().let {
                        it.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM"))
                    },
                    color = if (wallpaperUri != null) Color.White.copy(alpha = .9f) else Muted,
                    fontSize = 14.sp
                )

                Spacer(Modifier.weight(1f))

                if (!showDrawer) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val favorites = allApps.take(4)
                        favorites.forEach { app ->
                            AppShortcut(app, Modifier.weight(1f)) { launchApp(context, app) }
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Surface(
                        onClick = { showDrawer = true },
                        color = Color.White.copy(alpha = if (wallpaperUri != null) .88f else 1f),
                        shape = RoundedCornerShape(28.dp),
                        shadowElevation = 8.dp,
                        modifier = Modifier.fillMaxWidth().height(58.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center) {
                            Icon(Icons.Default.Apps, contentDescription = null, tint = Ink)
                            Spacer(Modifier.width(10.dp))
                            Text("Semua aplikasi", color = Ink, fontWeight = FontWeight.Medium)
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        shape = RoundedCornerShape(28.dp),
                        color = Color(0xFFFDFCFB).copy(alpha = .96f)
                    ) {
                        Column(Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Aplikasi", fontSize = 23.sp, fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f))
                                TextButton(onClick = { showDrawer = false; query = "" }) {
                                    Text("Tutup")
                                }
                            }
                            OutlinedTextField(
                                value = query,
                                onValueChange = { query = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = { Text("Cari aplikasi") },
                                leadingIcon = { Icon(Icons.Default.Search, null) },
                                shape = RoundedCornerShape(18.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            val filtered = allApps.filter { it.label.contains(query, true) }
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(4),
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(filtered, key = { it.packageName }) { app ->
                                    AppShortcut(app, Modifier.fillMaxWidth()) {
                                        launchApp(context, app)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))
                Text(
                    "A little piece of home",
                    color = if (wallpaperUri != null) Color.White.copy(alpha = .9f) else Muted,
                    fontSize = 11.sp,
                    letterSpacing = 1.2.sp
                )
            }

            if (showSettings) {
                AlertDialog(
                    onDismissRequest = { showSettings = false },
                    title = { Text("Personalisasi") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Gunakan foto favorit sebagai wallpaper launcher.")
                            OutlinedButton(
                                onClick = { showSettings = false; pickPhoto.launch(arrayOf("image/*")) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.CameraAlt, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Pilih foto dari galeri")
                            }
                            TextButton(
                                onClick = {
                                    wallpaperUri = null
                                    prefs.edit().remove("wallpaper_uri").apply()
                                    showSettings = false
                                }
                            ) { Text("Hapus foto wallpaper") }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showSettings = false }) { Text("Selesai") }
                    }
                )
            }
        }
    }
}

@Composable
private fun AppShortcut(app: LauncherApp, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = modifier.clip(RoundedCornerShape(18.dp)).clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(54.dp),
            shape = RoundedCornerShape(17.dp),
            color = Color.White.copy(alpha = .94f),
            shadowElevation = 3.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                val drawable = remember(app.packageName) {
                    runCatching { context.packageManager.getApplicationIcon(app.packageName) }.getOrNull()
                }
                if (drawable != null) {
                    androidx.compose.ui.viewinterop.AndroidView(
                        factory = { android.widget.ImageView(it).apply {
                            setImageDrawable(drawable)
                            scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                            setPadding(9, 9, 9, 9)
                        } },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            app.label,
            color = if (isSystemInDarkTheme()) Color.White else Ink,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}

private fun loadApps(context: android.content.Context): List<LauncherApp> {
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
        .sortedBy { it.label.lowercase() }
}

private fun launchApp(context: android.content.Context, app: LauncherApp) {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
    if (launchIntent != null) context.startActivity(launchIntent)
}

package com.mylittlelauncher.ui.screens

import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.BatteryManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mylittlelauncher.R
import com.mylittlelauncher.data.AppRepository
import com.mylittlelauncher.model.IconShapeOption
import com.mylittlelauncher.model.LauncherApp
import com.mylittlelauncher.ui.components.LauncherAppTile
import com.mylittlelauncher.ui.theme.LocalLauncherPalette
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    photo: Uri?,
    time: LocalDateTime,
    childName: String,
    birthDateIso: String,
    homeQuote: String,
    iconShape: IconShapeOption,
    dockApps: List<LauncherApp>,
    pinnedHomeApps: List<LauncherApp>,
    favoritePackages: Set<String>,
    onOpenDrawer: (focusSearch: Boolean) -> Unit,
    onOpenLockPreview: () -> Unit,
    onOpenSettings: () -> Unit,
    onEditPhotoOrMilestone: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onTogglePinHome: (String) -> Unit,
    onLaunch: (LauncherApp) -> Unit
) {
    val context = LocalContext.current
    val palette = LocalLauncherPalette.current
    val pagerState = rememberPagerState(pageCount = { 2 })

    val batteryPct = remember(time.minute) {
        val bm = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = bm?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = bm?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        if (level >= 0 && scale > 0) (level * 100 / scale) else 100
    }

    val milestoneText = remember(birthDateIso, time.toLocalDate()) {
        computeBabyAgeBadge(birthDateIso, time.toLocalDate())
    }

    val greeting = remember(time.hour) {
        when (time.hour) {
            in 4..10 -> "Selamat pagi"
            in 11..14 -> "Selamat siang"
            in 15..17 -> "Selamat sore"
            else -> "Selamat malam"
        }
    }

    val hourText = time.format(DateTimeFormatter.ofPattern("HH:mm"))
    val dateText = time.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("id", "ID")))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("id", "ID")) else it.toString() }

    val pulseAnim = rememberInfiniteTransition(label = "hint-pulse")
    val hintAlpha by pulseAnim.animateFloat(
        initialValue = 0.65f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "hint-alpha"
    )

    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(palette.bgTop, palette.bgBottom)))
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { dragAccumulator = 0f },
                    onVerticalDrag = { _, dragAmount -> dragAccumulator += dragAmount },
                    onDragEnd = {
                        if (dragAccumulator < -95f) {
                            onOpenDrawer(false)
                        } else if (dragAccumulator > 110f) {
                            onOpenDrawer(true)
                        }
                    }
                )
            }
            .combinedClickable(
                onClick = {},
                onLongClick = onEditPhotoOrMilestone
            )
    ) {
        // Fullscreen Wallpaper
        AsyncImage(
            model = photo ?: R.drawable.baby_photo,
            contentDescription = "Wallpaper Keluarga",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Subtle Scrim for crisp legibility on top & bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color(0x6619231E),
                        0.32f to Color.Transparent,
                        0.66f to Color.Transparent,
                        1.0f to Color(0xBA141E19)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Top Status Bar Row (Greeting + Battery + Settings)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.28f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color(0xFFFFB5A7),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "$greeting · $childName",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                Surface(
                    color = Color.Black.copy(alpha = 0.28f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.BatteryChargingFull,
                            contentDescription = null,
                            tint = Color(0xFFB7E4C7),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "$batteryPct%",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                Surface(
                    onClick = onOpenSettings,
                    color = Color.Black.copy(alpha = 0.30f),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "Pengaturan",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Interactive Clock & Date Header (Tap clock -> Alarms, Tap date -> Calendar)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = hourText,
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 66.sp,
                        fontWeight = FontWeight.ExtraLight,
                        letterSpacing = (-3).sp,
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.35f),
                            offset = Offset(0f, 4f),
                            blurRadius = 12f
                        )
                    ),
                    modifier = Modifier.clickable { AppRepository.openClockApp(context) }
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { AppRepository.openCalendarApp(context) }
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = dateText,
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            // Horizontal Pager: Page 0 = Clean Photo Focus, Page 1 = Pinned Shortcuts & Quick Search
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                if (pageIndex == 0) {
                    // Page 0: Clean canvas so baby's face shines unobstructed
                    Box(Modifier.fillMaxSize())
                } else {
                    // Page 1: Workspace Shortcuts Grid + Quick Search Pill
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 18.dp),
                        verticalArrangement = Arrangement.Top
                    ) {
                        Surface(
                            onClick = { onOpenDrawer(true) },
                            color = palette.glassSurface,
                            shape = RoundedCornerShape(22.dp),
                            border = BorderStroke(1.dp, palette.glassBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Search, null, tint = palette.accent, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = "Cari aplikasi terpasang...",
                                    color = palette.muted,
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(Icons.Default.GridView, null, tint = palette.muted, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Surface(
                            color = Color.Black.copy(alpha = 0.32f),
                            shape = RoundedCornerShape(26.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "APLIKASI DISEMATKAN",
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 10.sp,
                                        letterSpacing = 1.6.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "Tahan ikon di laci untuk edit",
                                        color = Color.White.copy(alpha = 0.65f),
                                        fontSize = 10.sp
                                    )
                                }
                                Spacer(Modifier.height(12.dp))

                                if (pinnedHomeApps.isEmpty()) {
                                    Text(
                                        text = "Belum ada aplikasi disematkan. Buka App Library lalu tekan lama ikon untuk menyematkan ke sini.",
                                        color = Color.White.copy(alpha = 0.82f),
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp
                                    )
                                } else {
                                    val rows = pinnedHomeApps.chunked(4)
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        rows.forEach { rowApps ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                rowApps.forEach { app ->
                                                    LauncherAppTile(
                                                        app = app,
                                                        iconShape = iconShape,
                                                        modifier = Modifier.weight(1f),
                                                        iconSize = 50.dp,
                                                        showLabel = true,
                                                        labelColor = Color.White,
                                                        isFavorite = app.packageName in favoritePackages,
                                                        isPinnedHome = true,
                                                        onClick = { onLaunch(app) },
                                                        onToggleFavorite = { onToggleFavorite(app.packageName) },
                                                        onTogglePinHome = { onTogglePinHome(app.packageName) }
                                                    )
                                                }
                                                repeat(4 - rowApps.size) {
                                                    Spacer(Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Page Indicator Dots
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(2) { idx ->
                    val active = pagerState.currentPage == idx
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(6.dp)
                            .width(if (active) 18.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (active) Color.White else Color.White.copy(alpha = 0.45f))
                    )
                }
            }

            // Milestone Pill + Lock Preview Shortcut
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        onClick = onEditPhotoOrMilestone,
                        color = palette.glassSurface,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, palette.glassBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ChildCare,
                                contentDescription = null,
                                tint = palette.accent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = milestoneText,
                                color = palette.ink,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Text(
                        text = homeQuote,
                        color = Color.White.copy(alpha = 0.94f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.width(12.dp))

                Surface(
                    onClick = onOpenLockPreview,
                    color = palette.glassSurface,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, palette.glassBorder),
                    shadowElevation = 6.dp,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Preview Lock Screen",
                            tint = palette.ink,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Floating Glass Dock
            Surface(
                color = palette.glassSurface,
                shape = RoundedCornerShape(30.dp),
                border = BorderStroke(1.dp, palette.glassBorder),
                shadowElevation = 14.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (dockApps.isEmpty()) {
                        DockAction(Icons.Default.Apps, "All Apps") { onOpenDrawer(false) }
                    } else {
                        dockApps.take(4).forEach { app ->
                            LauncherAppTile(
                                app = app,
                                iconShape = iconShape,
                                modifier = Modifier.weight(1f),
                                iconSize = 48.dp,
                                showLabel = false,
                                onClick = { onLaunch(app) }
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .width(1.dp)
                            .height(32.dp)
                            .background(palette.accentSoft)
                    )

                    DockAction(Icons.Default.Apps, "Apps") { onOpenDrawer(false) }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Swipe Up / Search Hint Bar
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { onOpenDrawer(false) }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = hintAlpha),
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "GESER KE ATAS UNTUK SEMUA APLIKASI",
                    color = Color.White.copy(alpha = hintAlpha),
                    fontSize = 9.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun DockAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val palette = LocalLauncherPalette.current
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = label, tint = palette.ink, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(2.dp))
        Text(label, color = palette.muted, fontSize = 9.sp, fontWeight = FontWeight.Medium)
    }
}

fun computeBabyAgeBadge(birthDateIso: String, today: LocalDate): String {
    val birth = runCatching { LocalDate.parse(birthDateIso) }.getOrNull() ?: return "Little World"
    if (birth.isAfter(today)) return "Menanti hari kelahiran"

    val period = Period.between(birth, today)
    val totalDays = ChronoUnit.DAYS.between(birth, today)
    return when {
        period.years >= 1 -> "${period.years} thn ${period.months} bln · $totalDays hari"
        period.months >= 1 -> "${period.months} bulan ${period.days} hari"
        else -> "$totalDays hari bersama"
    }
}

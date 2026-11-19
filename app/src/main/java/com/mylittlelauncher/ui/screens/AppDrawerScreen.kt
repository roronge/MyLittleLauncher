package com.mylittlelauncher.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mylittlelauncher.data.AppRepository
import com.mylittlelauncher.model.AppCategory
import com.mylittlelauncher.model.IconShapeOption
import com.mylittlelauncher.model.LauncherApp
import com.mylittlelauncher.ui.components.LauncherAppTile
import com.mylittlelauncher.ui.theme.LocalLauncherPalette
import kotlinx.coroutines.launch

@Composable
fun AppDrawerScreen(
    apps: List<LauncherApp>,
    favoritePackages: Set<String>,
    pinnedHomePackages: List<String>,
    hiddenPackages: Set<String>,
    drawerColumns: Int,
    iconShape: IconShapeOption,
    autoFocusSearch: Boolean,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onTogglePinHome: (String) -> Unit,
    onHideApp: (String) -> Unit,
    onLaunch: (LauncherApp) -> Unit
) {
    val context = LocalContext.current
    val palette = LocalLauncherPalette.current
    var search by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(AppCategory.ALL) }
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    BackHandler(onBack = onBack)

    LaunchedEffect(autoFocusSearch) {
        if (autoFocusSearch) {
            runCatching { focusRequester.requestFocus() }
        }
    }

    val visibleApps = remember(apps, hiddenPackages) {
        apps.filter { it.packageName !in hiddenPackages }
    }

    val filteredApps = remember(visibleApps, search, selectedCategory, favoritePackages) {
        visibleApps.filter { app ->
            val matchesQuery = search.isBlank() ||
                app.label.contains(search, ignoreCase = true) ||
                app.packageName.contains(search, ignoreCase = true)

            val matchesCategory = when (selectedCategory) {
                AppCategory.ALL -> true
                AppCategory.FAVORITES -> app.packageName in favoritePackages
                else -> app.category == selectedCategory
            }
            matchesQuery && matchesCategory
        }
    }

    val availableLetters = remember(filteredApps) {
        filteredApps.mapNotNull { it.label.firstOrNull()?.uppercaseChar() }
            .filter { it in 'A'..'Z' }
            .distinct()
            .sorted()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(palette.bgTop, palette.bgBottom)))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "APP LIBRARY",
                    fontSize = 10.sp,
                    color = palette.muted,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Semua Aplikasi",
                    fontSize = 28.sp,
                    color = palette.ink,
                    fontWeight = FontWeight.Light
                )
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Pengaturan", tint = palette.ink)
            }
            Surface(
                onClick = onBack,
                color = palette.glassSurface,
                shape = CircleShape,
                border = BorderStroke(1.dp, palette.glassBorder),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Home, contentDescription = "Beranda", tint = palette.ink, modifier = Modifier.size(20.dp))
                }
            }
        }

        // Search Field with Clear Button
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            singleLine = true,
            placeholder = { Text("Cari nama aplikasi...", color = palette.muted, fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = palette.accent) },
            trailingIcon = {
                if (search.isNotEmpty()) {
                    IconButton(onClick = { search = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Hapus pencarian", tint = palette.muted)
                    }
                }
            },
            shape = RoundedCornerShape(22.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = palette.ink,
                unfocusedTextColor = palette.ink,
                focusedBorderColor = palette.accent,
                unfocusedBorderColor = palette.accentSoft,
                focusedContainerColor = palette.glassSurface,
                unfocusedContainerColor = palette.glassSurface
            )
        )

        Spacer(Modifier.height(10.dp))

        // Smart Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppCategory.entries.forEach { category ->
                val selected = selectedCategory == category
                FilterChip(
                    selected = selected,
                    onClick = { selectedCategory = category },
                    label = {
                        Text(
                            text = category.label,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = palette.accent,
                        selectedLabelColor = if (palette.isDark) Color(0xFF101916) else Color.White,
                        containerColor = palette.glassSurface,
                        labelColor = palette.ink
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = palette.accentSoft,
                        selectedBorderColor = palette.accent
                    )
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredApps.size} APLIKASI",
                color = palette.muted,
                fontSize = 10.sp,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Tahan ikon untuk opsi cepat",
                color = palette.muted,
                fontSize = 10.sp
            )
        }

        Spacer(Modifier.height(8.dp))

        // Main App Grid + A-Z Fast Scrubber Rail
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (filteredApps.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedCategory == AppCategory.FAVORITES)
                            "Belum ada aplikasi favorit.\nTekan lama ikon aplikasi mana saja untuk menambahkannya."
                        else
                            "Tidak ada aplikasi yang cocok dengan \"$search\"",
                        color = palette.muted,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            } else {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(drawerColumns),
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 24.dp, end = 4.dp)
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        LauncherAppTile(
                            app = app,
                            iconShape = iconShape,
                            modifier = Modifier.fillMaxWidth(),
                            iconSize = if (drawerColumns >= 5) 48.dp else 54.dp,
                            showLabel = true,
                            isFavorite = app.packageName in favoritePackages,
                            isPinnedHome = app.packageName in pinnedHomePackages,
                            onClick = { onLaunch(app) },
                            onToggleFavorite = { onToggleFavorite(app.packageName) },
                            onTogglePinHome = { onTogglePinHome(app.packageName) },
                            onToggleHide = { onHideApp(app.packageName) },
                            onOpenInfo = { AppRepository.openAppInfo(context, app.packageName) }
                        )
                    }
                }
            }

            // A-Z Fast Alphabet Rail
            if (availableLetters.size > 4 && search.isBlank()) {
                Column(
                    modifier = Modifier
                        .padding(start = 4.dp, bottom = 16.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(palette.glassSurface)
                        .padding(horizontal = 5.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    availableLetters.forEach { letter ->
                        Text(
                            text = letter.toString(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.accent,
                            modifier = Modifier
                                .clickable {
                                    val targetIndex = filteredApps.indexOfFirst {
                                        it.label.firstOrNull()?.uppercaseChar() == letter
                                    }
                                    if (targetIndex >= 0) {
                                        coroutineScope.launch {
                                            gridState.animateScrollToItem(targetIndex)
                                        }
                                    }
                                }
                                .padding(vertical = 1.5.dp, horizontal = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

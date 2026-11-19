package com.mylittlelauncher.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mylittlelauncher.data.AppRepository
import com.mylittlelauncher.model.IconShapeOption
import com.mylittlelauncher.model.LauncherApp
import com.mylittlelauncher.model.LauncherThemePreset
import com.mylittlelauncher.ui.components.GlassCard
import com.mylittlelauncher.ui.theme.LocalLauncherPalette
import com.mylittlelauncher.ui.theme.paletteFor

@Composable
fun SettingsScreen(
    allApps: List<LauncherApp>,
    childName: String,
    birthDateIso: String,
    homeQuote: String,
    themePreset: LauncherThemePreset,
    iconShape: IconShapeOption,
    drawerColumns: Int,
    dockPackages: List<String>,
    hiddenPackages: Set<String>,
    hasCustomPhoto: Boolean,
    onBack: () -> Unit,
    onChoosePhoto: () -> Unit,
    onResetPhoto: () -> Unit,
    onSaveProfile: (name: String, birthIso: String, quote: String) -> Unit,
    onSelectTheme: (LauncherThemePreset) -> Unit,
    onSelectIconShape: (IconShapeOption) -> Unit,
    onSelectDrawerColumns: (Int) -> Unit,
    onUpdateDockPackages: (List<String>) -> Unit,
    onRestoreHiddenPackage: (String) -> Unit
) {
    val context = LocalContext.current
    val palette = LocalLauncherPalette.current
    BackHandler(onBack = onBack)

    var nameInput by remember(childName) { mutableStateOf(childName) }
    var birthInput by remember(birthDateIso) { mutableStateOf(birthDateIso) }
    var quoteInput by remember(homeQuote) { mutableStateOf(homeQuote) }
    var showDockPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(palette.bgTop, palette.bgBottom)))
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                onClick = onBack,
                color = palette.glassSurface,
                shape = CircleShape,
                border = BorderStroke(1.dp, palette.glassBorder),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = palette.ink)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("PERSONALISASI", color = palette.muted, fontSize = 10.sp, letterSpacing = 2.sp)
                Text("Pengaturan Launcher", color = palette.ink, fontSize = 24.sp, fontWeight = FontWeight.Light)
            }
        }

        // 1. Default Home Launcher Banner
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.HomeWork, contentDescription = null, tint = palette.accent, modifier = Modifier.size(26.dp))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Jadikan Launcher Utama", color = palette.ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("Atur My Little Launcher sebagai Home default di POCO / HyperOS", color = palette.muted, fontSize = 12.sp)
                }
                Button(
                    onClick = { AppRepository.openDefaultLauncherSettings(context) },
                    colors = ButtonDefaults.buttonColors(containerColor = palette.accent),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Atur", fontSize = 12.sp)
                }
            }
        }

        // 2. Wallpaper & Memory Photo
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Wallpaper, null, tint = palette.accent)
                    Spacer(Modifier.width(10.dp))
                    Text("Foto Wallpaper Utama", color = palette.ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
                Text(
                    "Gunakan foto keluarga dari galeri atau tetap memakai foto bawaan.",
                    color = palette.muted,
                    fontSize = 12.sp
                )
                Button(
                    onClick = onChoosePhoto,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = palette.accent),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.PhotoLibrary, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Pilih Foto dari Galeri")
                }
                if (hasCustomPhoto) {
                    TextButton(onClick = onResetPhoto, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text("Kembalikan Foto Bawaan", color = palette.muted)
                    }
                }
            }
        }

        // 3. Baby Milestone & Custom Quote
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ChildCare, null, tint = palette.accent)
                    Spacer(Modifier.width(10.dp))
                    Text("Kartu Milestone & Kutipan", color = palette.ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Nama Si Kecil") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )
                OutlinedTextField(
                    value = birthInput,
                    onValueChange = { birthInput = it },
                    label = { Text("Tanggal Lahir (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )
                OutlinedTextField(
                    value = quoteInput,
                    onValueChange = { quoteInput = it },
                    label = { Text("Kutipan di Home Screen") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )
                Button(
                    onClick = { onSaveProfile(nameInput.trim(), birthInput.trim(), quoteInput.trim()) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = palette.accent),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Simpan Milestone")
                }
            }
        }

        // 4. Theme Presets (4 Palettes including AMOLED Dark)
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Palette, null, tint = palette.accent)
                    Spacer(Modifier.width(10.dp))
                    Text("Tema Warna", color = palette.ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
                LauncherThemePreset.entries.forEach { preset ->
                    val p = paletteFor(preset)
                    val selected = themePreset == preset
                    Surface(
                        onClick = { onSelectTheme(preset) },
                        color = if (selected) palette.accent.copy(alpha = 0.14f) else Color.Transparent,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, if (selected) palette.accent else palette.accentSoft.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(p.bgTop, p.accent, p.ink).forEach { c ->
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(c)
                                            .border(1.dp, Color.White, CircleShape)
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(preset.title, color = palette.ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(preset.subtitle, color = palette.muted, fontSize = 11.sp)
                            }
                            if (selected) {
                                Icon(Icons.Default.CheckCircle, null, tint = palette.accent)
                            }
                        }
                    }
                }
            }
        }

        // 5. Icon Shape, Grid Columns & Dock Customization
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.GridView, null, tint = palette.accent)
                    Spacer(Modifier.width(10.dp))
                    Text("Tampilan Ikon & Dock", color = palette.ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }

                Text("Bentuk Ikon", color = palette.muted, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconShapeOption.entries.forEach { option ->
                        val active = iconShape == option
                        FilterChip(
                            selected = active,
                            onClick = { onSelectIconShape(option) },
                            label = { Text(option.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = palette.accent,
                                selectedLabelColor = if (palette.isDark) Color(0xFF101916) else Color.White
                            )
                        )
                    }
                }

                Text("Jumlah Kolom App Library: $drawerColumns Kolom", color = palette.muted, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(3, 4, 5).forEach { cols ->
                        val active = drawerColumns == cols
                        FilterChip(
                            selected = active,
                            onClick = { onSelectDrawerColumns(cols) },
                            label = { Text("$cols Kolom") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = palette.accent,
                                selectedLabelColor = if (palette.isDark) Color(0xFF101916) else Color.White
                            )
                        )
                    }
                }

                OutlinedButton(
                    onClick = { showDockPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, palette.accent)
                ) {
                    Icon(Icons.Default.Dock, null, tint = palette.accent)
                    Spacer(Modifier.width(8.dp))
                    Text("Pilih 4 Aplikasi Utama di Dock (${dockPackages.size}/4)", color = palette.ink)
                }
            }
        }

        // 6. Hidden Apps Manager
        if (hiddenPackages.isNotEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Aplikasi Disembunyikan (${hiddenPackages.size})", color = palette.ink, fontWeight = FontWeight.SemiBold)
                    hiddenPackages.forEach { pkg ->
                        val label = allApps.find { it.packageName == pkg }?.label ?: pkg
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, color = palette.ink, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            TextButton(onClick = { onRestoreHiddenPackage(pkg) }) {
                                Text("Tampilkan", color = palette.accent)
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))
    }

    if (showDockPicker) {
        DockSelectionDialog(
            allApps = allApps,
            selectedPackages = dockPackages,
            onDismiss = { showDockPicker = false },
            onConfirm = {
                onUpdateDockPackages(it)
                showDockPicker = false
            }
        )
    }
}

@Composable
private fun DockSelectionDialog(
    allApps: List<LauncherApp>,
    selectedPackages: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    val palette = LocalLauncherPalette.current
    var current by remember { mutableStateOf(selectedPackages) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.cardSurface,
        title = { Text("Pilih Maksimal 4 Aplikasi Dock", color = palette.ink, fontSize = 17.sp) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                allApps.forEach { app ->
                    val checked = app.packageName in current
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                current = if (checked) {
                                    current - app.packageName
                                } else if (current.size < 4) {
                                    current + app.packageName
                                } else {
                                    current
                                }
                            }
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = null,
                            colors = CheckboxDefaults.colors(checkedColor = palette.accent)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(app.label, color = palette.ink, fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(current) }) {
                Text("Simpan (${current.size}/4)", color = palette.accent, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = palette.muted)
            }
        }
    )
}

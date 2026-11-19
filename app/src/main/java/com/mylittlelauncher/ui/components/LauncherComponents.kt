package com.mylittlelauncher.ui.components

import android.widget.ImageView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.mylittlelauncher.model.IconShapeOption
import com.mylittlelauncher.model.LauncherApp
import com.mylittlelauncher.ui.theme.LocalLauncherPalette

fun iconShapeToCompose(option: IconShapeOption): Shape = when (option) {
    IconShapeOption.SQUIRCLE -> RoundedCornerShape(18.dp)
    IconShapeOption.CIRCLE -> CircleShape
    IconShapeOption.ROUNDED -> RoundedCornerShape(12.dp)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LauncherAppTile(
    app: LauncherApp,
    iconShape: IconShapeOption,
    modifier: Modifier = Modifier,
    iconSize: Dp = 54.dp,
    showLabel: Boolean = true,
    labelColor: Color = LocalLauncherPalette.current.ink,
    isFavorite: Boolean = false,
    isPinnedHome: Boolean = false,
    onClick: () -> Unit,
    onToggleFavorite: (() -> Unit)? = null,
    onTogglePinHome: (() -> Unit)? = null,
    onToggleHide: (() -> Unit)? = null,
    onOpenInfo: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val palette = LocalLauncherPalette.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 500f),
        label = "icon-bounce"
    )
    var menuExpanded by remember { mutableStateOf(false) }
    val shape = iconShapeToCompose(iconShape)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .scale(scale)
                .clip(RoundedCornerShape(18.dp))
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                    onLongClick = {
                        if (onToggleFavorite != null || onTogglePinHome != null || onOpenInfo != null) {
                            menuExpanded = true
                        }
                    }
                )
                .padding(vertical = 4.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                Surface(
                    modifier = Modifier
                        .size(iconSize)
                        .shadow(6.dp, shape, ambientColor = Color.Black.copy(alpha = 0.14f)),
                    shape = shape,
                    color = if (palette.isDark) Color(0xFF23322D) else Color.White.copy(alpha = 0.96f),
                    border = BorderStroke(1.dp, palette.glassBorder)
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
                                    setPadding(16, 16, 16, 16)
                                }
                            },
                            update = { it.setImageDrawable(drawable) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                if (isFavorite && showLabel) {
                    Box(
                        modifier = Modifier
                            .offset(x = 2.dp, y = (-2).dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(palette.accent)
                            .border(1.5.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(9.dp)
                        )
                    }
                }
            }

            if (showLabel) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = app.label,
                    color = labelColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            containerColor = palette.cardSurface
        ) {
            Text(
                text = app.label,
                color = palette.ink,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            HorizontalDivider(color = palette.accentSoft.copy(alpha = 0.5f))

            if (onToggleFavorite != null) {
                DropdownMenuItem(
                    text = { Text(if (isFavorite) "Hapus dari Favorit" else "Tambah ke Favorit", color = palette.ink) },
                    leadingIcon = { Icon(Icons.Default.Favorite, null, tint = palette.accent) },
                    onClick = {
                        menuExpanded = false
                        onToggleFavorite()
                    }
                )
            }
            if (onTogglePinHome != null) {
                DropdownMenuItem(
                    text = { Text(if (isPinnedHome) "Lepas dari Home Screen" else "Sematkan di Home Screen", color = palette.ink) },
                    leadingIcon = { Icon(Icons.Default.PushPin, null, tint = palette.accent) },
                    onClick = {
                        menuExpanded = false
                        onTogglePinHome()
                    }
                )
            }
            if (onToggleHide != null) {
                DropdownMenuItem(
                    text = { Text("Sembunyikan Aplikasi", color = palette.ink) },
                    leadingIcon = { Icon(Icons.Default.VisibilityOff, null, tint = palette.muted) },
                    onClick = {
                        menuExpanded = false
                        onToggleHide()
                    }
                )
            }
            if (onOpenInfo != null) {
                DropdownMenuItem(
                    text = { Text("Info Aplikasi", color = palette.ink) },
                    leadingIcon = { Icon(Icons.Default.Info, null, tint = palette.muted) },
                    onClick = {
                        menuExpanded = false
                        onOpenInfo()
                    }
                )
            }
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(26.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = LocalLauncherPalette.current
    Surface(
        modifier = modifier,
        shape = shape,
        color = palette.glassSurface,
        border = BorderStroke(1.dp, palette.glassBorder),
        shadowElevation = 10.dp
    ) {
        Column(content = content)
    }
}

package com.mylittlelauncher.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mylittlelauncher.R
import com.mylittlelauncher.ui.theme.LocalLauncherPalette
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun LockScreenPreview(
    photo: Uri?,
    time: LocalDateTime,
    childName: String,
    birthDateIso: String,
    homeQuote: String,
    onUnlockPreview: () -> Unit
) {
    val palette = LocalLauncherPalette.current
    BackHandler(onBack = onUnlockPreview)

    val dayUpper = time.format(DateTimeFormatter.ofPattern("EEEE", Locale("id", "ID"))).uppercase(Locale.ROOT)
    val dateFormatted = time.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("id", "ID")))
    val milestone = remember(birthDateIso, time.toLocalDate()) {
        computeBabyAgeBadge(birthDateIso, time.toLocalDate())
    }

    val breathe = rememberInfiniteTransition(label = "fp-breathe")
    val fpScale by breathe.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "fp-scale"
    )

    var swipeY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141E19))
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { swipeY = 0f },
                    onVerticalDrag = { _, drag -> swipeY += drag },
                    onDragEnd = {
                        if (swipeY < -80f) onUnlockPreview()
                    }
                )
            }
            .clickable(onClick = onUnlockPreview)
    ) {
        AsyncImage(
            model = photo ?: R.drawable.baby_photo,
            contentDescription = "Lock screen wallpaper",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x7716221C),
                            Color(0x2216221C),
                            Color(0xD9101915)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$dayUpper · MY LITTLE WORLD",
                color = Color.White.copy(alpha = 0.90f),
                fontSize = 10.sp,
                letterSpacing = 2.2.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(38.dp))

            Text(
                text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
                color = Color.White,
                fontSize = 80.sp,
                fontWeight = FontWeight.ExtraLight,
                letterSpacing = (-3).sp
            )

            Text(
                text = dateFormatted,
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 15.sp
            )

            Spacer(Modifier.height(16.dp))

            Surface(
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.ChildCare, null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Text(
                        text = "$childName · $milestone",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Surface(
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.24f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Favorite, null, tint = Color(0xFFFFB5A7), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(text = homeQuote, color = Color.White, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(26.dp))

            Surface(
                color = palette.glassSurface,
                shape = CircleShape,
                shadowElevation = 12.dp,
                modifier = Modifier
                    .size(62.dp)
                    .scale(fpScale)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Fingerprint,
                        contentDescription = "Unlock Preview",
                        tint = palette.accent,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "KETUK ATAU GESER KE ATAS UNTUK KEMBALI",
                color = Color.White.copy(alpha = 0.88f),
                fontSize = 9.sp,
                letterSpacing = 1.8.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

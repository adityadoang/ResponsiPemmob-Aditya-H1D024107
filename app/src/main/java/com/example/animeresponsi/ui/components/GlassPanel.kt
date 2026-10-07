package com.example.animeresponsi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Komponen kotak panel dengan efek kaca transparan (Glassmorphism).
// Dipakai untuk membungkus kotak search, dropdown genre, dan tabel detail.
@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit, // Isi komponen di dalam panel kaca
) {
    val shape = RoundedCornerShape(24.dp) // Sudut melengkung
    Box(
        modifier = modifier
            .shadow(18.dp, shape, ambientColor = Color(0xFF7DDCF5).copy(alpha = 0.28f)) // Efek pendaran cahaya (glow) biru neon
            .clip(shape) // Potong isi panel mengikuti lengkungan sudut
            .background(
                // Gradasi warna gelap semi-transparan menyerupai kaca
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF455F77).copy(alpha = 0.94f),
                        Color(0xFF1B344B).copy(alpha = 0.92f),
                        Color(0xFF102538).copy(alpha = 0.94f),
                    ),
                ),
            )
            .border(1.dp, Color.White.copy(alpha = 0.48f), shape), // Garis tepi tipis putih semi-transparan pantulan kaca
    ) {
        // Efek kilau cahaya di bagian atas panel kaca
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(36.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.16f), Color.Transparent),
                    ),
                ),
        )
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onSurface,
            content = { content() },
        )
    }
}


package com.example.animeresponsi.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

// Skema susunan warna tema gelap (Dark Mode) aplikasi AnimeResponsi.
private val AnimeResponsiDarkColorScheme = darkColorScheme(
    primary = GlassBlue, // Warna aksen utama (biru es kaca)
    onPrimary = Navy950, // Warna teks di atas tombol primary
    primaryContainer = Navy800, // Warna latar belakang header atas
    onPrimaryContainer = TextPrimary, // Warna teks di dalam container header
    secondary = AccentRed, // Warna aksen kedua (merah koral)
    onSecondary = Navy950,
    secondaryContainer = AccentRedContainer, // Warna latar badge tipe anime
    onSecondaryContainer = OnAccentRedContainer, // Warna teks badge tipe anime
    tertiary = ScoreGold, // Warna aksen ketiga (emas untuk nilai skor pengguna)
    onTertiary = Navy950,
    background = Navy950, // Warna latar belakang layar (navy pekat)
    onBackground = TextPrimary, // Warna teks utama
    surface = DarkSurfaceVariant, // Warna kartu dan panel
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary, // Warna teks label sekunder
    outline = DarkOutline, // Warna garis pembatas tipis
    error = ErrorRed, // Warna teks indikator error
    onError = OnErrorRed,
)

// Pembungkus tema utama aplikasi.
// Menggabungkan skema warna gelap, font (typography), dan memastikan teks default selalu kontras dan jelas terbaca.
@Composable
fun AnimeResponsiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AnimeResponsiDarkColorScheme,
        typography = AnimeResponsiTypography,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides AnimeResponsiDarkColorScheme.onBackground,
            content = content,
        )
    }
}


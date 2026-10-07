package com.example.animeresponsi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage

// Komponen penampil gambar poster anime
// Otomatis menangani kondisi gambar saat sedang didownload dan saat gagal dimuat.
@Composable
fun PosterImage(
    imageUrl: String?, // Link gambar dari internet (bisa null)
    title: String, // Judul anime untuk pembaca layar (TalkBack / Aksesibilitas)
    modifier: Modifier = Modifier,
) {
    SubcomposeAsyncImage(
        model = imageUrl,
        contentDescription = "Poster $title",
        contentScale = ContentScale.Crop, // Potong gambar secara proporsional agar pas memenuhi ruang
        modifier = modifier,
        loading = {
            // Tampilan saat gambar masih dalam proses unduh
            PosterPlaceholder(showProgress = true)
        },
        error = {
            // Tampilan pengganti jika link gambar rusak atau internet mati
            PosterPlaceholder(showProgress = false)
        },
    )
}

// Kotak pengganti poster jika sedang loading atau jika gambar gagal diunduh.
@Composable
private fun PosterPlaceholder(showProgress: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (showProgress) {
                // Lingkaran loading saat sedang mendownload gambar
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.secondary,
                    strokeWidth = 2.dp,
                )
            } else {
                // Huruf "A" dan teks "POSTER" sebagai penanda visual jika gambar tidak ada
                Text(
                    text = "A",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    text = "POSTER",
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}


package com.example.animeresponsi.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.animeresponsi.data.model.AnimeSummary
import java.util.Locale

// Kartu anime pada daftar LazyColumn di halaman Home.
// Menampilkan: poster, tipe (TV/Movie), judul, skor pengguna, dan rating usia dengan label terpisah (syarat PRD).
@Composable
fun AnimeCard(
    anime: AnimeSummary, // Data anime yang ditampilkan
    onClick: () -> Unit, // Aksi saat kartu diklik (buka layar Detail)
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.38f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Row(
            modifier = Modifier.background(
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.12f), Color.Transparent),
                ),
            ),
        ) {
            PosterImage(
                imageUrl = anime.imageUrl,
                title = anime.title,
                modifier = Modifier
                    .width(104.dp)
                    .height(148.dp)
                    .clip(RoundedCornerShape(topStart = 21.dp, bottomStart = 21.dp)),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Badge kecil penanda tipe serial (misal: "TV", "Movie")
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(5.dp),
                ) {
                    Text(
                        text = anime.type ?: "Tipe tidak tersedia",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Judul anime (maksimal 2 baris agar rapi, selebihnya dipotong "...")
                Text(
                    text = anime.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.weight(1f))

                // Label 1: Skor pengguna (format "8.4 / 10")
                LabeledValue(
                    label = "Skor pengguna",
                    value = anime.score?.let { String.format(Locale.US, "%.1f / 10", it) }
                        ?: "Belum ada skor",
                )
                // Label 2: Rating usia penonton (misal "PG-13")
                LabeledValue(
                    label = "Rating usia",
                    value = anime.rating ?: "Rating usia tidak tersedia",
                )
            }
        }
    }
}

// Komponen pembantu untuk menampilkan teks label di atas dan nilainya di bawah,
@Composable
private fun LabeledValue(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Titik bulat kecil berwarna emas
        Surface(
            modifier = Modifier
                .padding(top = 5.dp)
                .size(6.dp),
            color = MaterialTheme.colorScheme.tertiary,
            shape = RoundedCornerShape(50),
        ) {}
        Column {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}


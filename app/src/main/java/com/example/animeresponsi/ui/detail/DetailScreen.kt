package com.example.animeresponsi.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.animeresponsi.data.model.AnimeDetail
import com.example.animeresponsi.ui.components.GlassPanel
import com.example.animeresponsi.ui.components.PosterImage
import java.util.Locale

// Layar Detail anime yang menampilkan 6 elemen wajib dari dokumen responsi:
// 1. Judul, 2. Genre, 3. Rating/Skor, 4. Episode, 5. Status, dan 6. Sinopsis.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    state: DetailUiState, // Data tampilan layar detail dari ViewModel
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background,
                    ),
                ),
            ),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Detail anime") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali ke Home",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.86f),
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
    ) { innerPadding ->
        when {
            state.isLoading -> DetailLoading(modifier = Modifier.padding(innerPadding))
            state.errorMessage != null -> DetailError(
                message = state.errorMessage,
                onRetry = onRetry,
                modifier = Modifier.padding(innerPadding),
            )
            state.animeDetail != null -> DetailContent(
                detail = state.animeDetail,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

// Tampilan animasi mutar (loading) saat sedang mengambil data detail dari server.
@Composable
private fun DetailLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Text(
                "Memuat detail anime…",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

// Tampilan pesan kesalahan saat gagal mengambil data detail, lengkap dengan tombol "Coba lagi".
@Composable
private fun DetailError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Detail belum tersedia",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = message,
            modifier = Modifier.padding(top = 8.dp, bottom = 18.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(onClick = onRetry) {
            Text("Coba lagi")
        }
    }
}

// Konten utama detail anime saat data berhasil dimuat:
// Menampilkan poster rasio 2:3, judul tebal, skor pengguna & rating usia, tabel fakta, dan teks sinopsis.
@Composable
private fun DetailContent(
    detail: AnimeDetail,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item(key = "poster") {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                PosterImage(
                    imageUrl = detail.imageUrl,
                    title = detail.title,
                    modifier = Modifier
                        .widthIn(max = 220.dp)
                        .fillMaxWidth(0.62f)
                        .aspectRatio(2f / 3f)
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(24.dp)),
                )
            }
        }
        item(key = "title") {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "ANIME #${detail.malId}",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = detail.title,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
        item(key = "score") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HighlightValue(
                    label = "SKOR PENGGUNA",
                    value = detail.score?.let { String.format(Locale.US, "%.1f / 10", it) }
                        ?: "Belum ada skor",
                    modifier = Modifier.weight(1f),
                )
                HighlightValue(
                    label = "RATING USIA",
                    value = detail.rating ?: "Tidak tersedia",
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item(key = "facts") {
            GlassPanel(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    DetailRow(
                        label = "Genre",
                        value = detail.genres.takeIf(List<String>::isNotEmpty)?.joinToString()
                            ?: "Genre tidak tersedia",
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    DetailRow(
                        label = "Episode",
                        value = detail.episodes?.toString() ?: "Belum diketahui",
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    DetailRow(
                        label = "Status",
                        value = detail.status ?: "Status tidak tersedia",
                    )
                }
            }
        }
        item(key = "synopsis") {
            GlassPanel(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "SINOPSIS",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = detail.synopsis ?: "Sinopsis belum tersedia",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

// Kotak panel kaca kecil untuk menyorot skor rating pengguna dan batasan usia secara berdampingan.
@Composable
private fun HighlightValue(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    GlassPanel(modifier = modifier) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

// Baris tabel informasi detail (Genre, Episode, Status):
// Kolom kiri (34%) untuk nama label, kolom kanan (66%) untuk isi nilai faktanya.
@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 15.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(0.34f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge,
        )
        Text(
            text = value,
            modifier = Modifier.weight(0.66f),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

package com.example.animeresponsi.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.animeresponsi.R
import com.example.animeresponsi.data.model.AnimeSummary
import com.example.animeresponsi.data.model.Genre
import com.example.animeresponsi.ui.components.AnimeCard
import com.example.animeresponsi.ui.components.GlassPanel
import com.example.animeresponsi.ui.components.PosterImage
import java.util.Locale
import kotlinx.coroutines.delay

// Layar utama aplikasi (Home Screen).
// Komponen ini stateless (hanya menerima data state dan aksi callback, tanpa logika bisnis sendiri).
@Composable
fun HomeScreen(
    state: HomeUiState, // Data tampilan layar Home dari ViewModel
    onQueryChange: (String) -> Unit, // Dipanggil saat mengetik judul di search bar
    onSearch: () -> Unit, // Dipanggil saat tombol "Cari" ditekan
    onGenreSelected: (Int?) -> Unit, // Dipanggil saat memilih genre di dropdown
    onRetryAnime: () -> Unit, // Dipanggil saat tombol coba lagi ditekan pada error list
    onRetryGenres: () -> Unit, // Dipanggil saat tombol coba lagi ditekan pada error genre
    onRetryFeatured: () -> Unit, // Dipanggil saat tombol coba lagi ditekan pada carousel
    onLoadMore: () -> Unit, // Dipanggil saat tombol "Muat halaman berikutnya" ditekan
    onAnimeClick: (Int) -> Unit, // Dipanggil saat kartu anime diklik untuk membuka detail
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
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
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 28.dp),
        ) {
            item(key = "featured_search") {
                FeaturedSearch(
                    state = state,
                    onQueryChange = onQueryChange,
                    onSearch = onSearch,
                    onRetryFeatured = onRetryFeatured,
                    onAnimeClick = onAnimeClick,
                )
            }
            item(key = "genre") {
                GlassPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                ) {
                    GenrePicker(
                        selectedGenreId = state.selectedGenreId,
                        genres = state.genres,
                        isLoading = state.isGenreLoading,
                        errorMessage = state.genreErrorMessage,
                        onGenreSelected = onGenreSelected,
                        onRetry = onRetryGenres,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    )
                }
            }

            when {
                state.isLoading -> item(key = "loading") {
                    StatusPanel(
                        title = "Mengambil data anime",
                        message = "Sebentar, daftar anime sedang dimuat dari Tenrai.",
                        showProgress = true,
                    )
                }

                state.errorMessage != null -> item(key = "error") {
                    StatusPanel(
                        title = "Data belum dapat dimuat",
                        message = state.errorMessage,
                        actionLabel = "Coba lagi",
                        onAction = onRetryAnime,
                    )
                }

                state.hasLoadedAnime && state.animeList.isEmpty() -> item(key = "empty") {
                    StatusPanel(
                        title = "Anime tidak ditemukan",
                        message = "Coba judul yang lebih singkat atau pilih Semua genre.",
                    )
                }

                state.hasLoadedAnime -> {
                    item(key = "result_header") {
                        ResultHeader(resultCount = state.animeList.size, page = state.currentPage)
                    }
                    items(
                        items = state.animeList,
                        key = { anime -> anime.malId },
                    ) { anime ->
                        AnimeCard(
                            anime = anime,
                            onClick = { onAnimeClick(anime.malId) },
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                        )
                    }
                    if (state.hasNextPage || state.isLoadingMore || state.loadMoreErrorMessage != null) {
                        item(key = "load_more") {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                state.loadMoreErrorMessage?.let { message ->
                                    Text(message, color = MaterialTheme.colorScheme.error)
                                }
                                Button(onClick = onLoadMore, enabled = !state.isLoadingMore) {
                                    if (state.isLoadingMore) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp,
                                        )
                                    } else {
                                        Text("Muat halaman berikutnya")
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

// Carousel anime unggulan di bagian atas layar Home.
// Otomatis bergeser ke anime berikutnya setiap 5 detik (LaunchedEffect + delay 5.000 ms).
@Composable
private fun FeaturedSearch(
    state: HomeUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onRetryFeatured: () -> Unit,
    onAnimeClick: (Int) -> Unit,
) {
    val featured = state.featuredAnime
    val pagerState = rememberPagerState(pageCount = { featured.size })
    LaunchedEffect(featured.map(AnimeSummary::malId)) {
        if (featured.isNotEmpty()) pagerState.scrollToPage(0)
        while (featured.size > 1) {
            delay(5_000)
            pagerState.animateScrollToPage((pagerState.currentPage + 1) % featured.size)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(440.dp)
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
    ) {
        if (featured.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.secondaryContainer,
                            ),
                        ),
                    ),
            )
        } else {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                val anime = featured[page]
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(onClickLabel = "Buka detail ${anime.title}") {
                            onAnimeClick(anime.malId)
                        },
                ) {
                    PosterImage(
                        imageUrl = anime.imageUrl,
                        title = anime.title,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF07111F).copy(alpha = 0.76f),
                                        Color(0xFF07111F).copy(alpha = 0.46f),
                                        Color(0xFF07111F).copy(alpha = 0.96f),
                                    ),
                                ),
                            ),
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 24.dp, end = 24.dp, bottom = 150.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "ANIME DENGAN SKOR TERTINGGI",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Text(
                            text = anime.title,
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "Skor pengguna ${String.format(Locale.US, "%.1f / 10", anime.score)}",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.app_name),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        if (featured.isEmpty()) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = if (state.isFeaturedLoading) "Memuat anime teratas…" else "Anime teratas belum tersedia",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                )
                if (state.featuredErrorMessage != null) {
                    OutlinedButton(onClick = onRetryFeatured) { Text("Coba lagi") }
                }
            }
        } else if (featured.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 24.dp, top = 36.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                featured.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .width(if (index == pagerState.currentPage) 18.dp else 6.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (index == pagerState.currentPage) Color.White
                                else Color.White.copy(alpha = 0.38f),
                            ),
                    )
                }
            }
        }

        SearchOverlay(
            query = state.query,
            onQueryChange = onQueryChange,
            onSearch = onSearch,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
        )
    }
}

// Kotak pencarian judul anime yang melayang di atas panel kaca bagian bawah carousel.
// Menutup keyboard di layar secara otomatis begitu tombol "Cari" ditekan.
@Composable
private fun SearchOverlay(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    GlassPanel(modifier = modifier) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "TEMUKAN ANIME",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Cari judul anime") },
                    placeholder = { Text("Contoh: Major") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        focusManager.clearFocus()
                        onSearch()
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.72f),
                        focusedPlaceholderColor = Color.White.copy(alpha = 0.9f),
                        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.9f),
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.95f),
                        cursorColor = MaterialTheme.colorScheme.primary,
                    ),
                )
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onSearch()
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 15.dp, vertical = 16.dp),
                ) {
                    Text("Cari", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

// Menu dropdown untuk memilih filter genre yang dimuat secara dinamis dari API Tenrai.
@Composable
private fun GenrePicker(
    selectedGenreId: Int?,
    genres: List<Genre>,
    isLoading: Boolean,
    errorMessage: String?,
    onGenreSelected: (Int?) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = genres.firstOrNull { it.malId == selectedGenreId }?.name ?: "Semua genre"

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Filter genre",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelLarge,
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 13.dp),
            ) {
                Text(
                    text = if (isLoading) "Memuat genre…" else selectedName,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Buka pilihan genre")
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.9f),
            ) {
                DropdownMenuItem(
                    text = { Text("Semua genre", color = MaterialTheme.colorScheme.onSurface) },
                    onClick = {
                        expanded = false
                        onGenreSelected(null)
                    },
                )
                genres.forEach { genre ->
                    DropdownMenuItem(
                        text = { Text(genre.name, color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            expanded = false
                            onGenreSelected(genre.malId)
                        },
                    )
                }
            }
        }
        if (errorMessage != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = errorMessage,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedButton(onClick = onRetry) {
                    Text("Muat ulang")
                }
            }
        }
    }
}

// Baris info judul daftar: menampilkan jumlah anime yang ditemukan dan nomor halaman aktif.
@Composable
private fun ResultHeader(resultCount: Int, page: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Column {
            Text(
                "DAFTAR ANIME · A–Z",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = "$resultCount anime",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge,
            )
        }
        Text(
            text = "Hingga halaman $page",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

// Panel kotak serbaguna bertema kaca untuk menampilkan 3 kondisi:
// 1. Sedang loading (animasi lingkaran mutar)
// 2. Terjadi error (pesan kesalahan + tombol Coba lagi)
// 3. Hasil pencarian kosong (anime tidak ditemukan)
@Composable
private fun StatusPanel(
    title: String,
    message: String,
    showProgress: Boolean = false,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    GlassPanel(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (showProgress) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (actionLabel != null && onAction != null) {
                Button(onClick = onAction) {
                    Text(actionLabel)
                }
            }
        }
    }
}

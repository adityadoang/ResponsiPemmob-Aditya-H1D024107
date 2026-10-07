package com.example.animeresponsi.ui.home

import com.example.animeresponsi.data.model.AnimeSummary
import com.example.animeresponsi.data.model.Genre

// Kondisi tampilan layar Home (State).
// Setiap ada nilai yang berubah di sini, Jetpack Compose otomatis memperbarui tampilan layar HP (Recomposition).
data class HomeUiState(
    val query: String = "", // Teks ketikan di kolom pencarian
    val selectedGenreId: Int? = null, // ID genre yang sedang dipilih (null artinya "Semua genre")
    val genres: List<Genre> = emptyList(), // Daftar pilihan genre untuk menu dropdown
    val animeList: List<AnimeSummary> = emptyList(), // Daftar kartu anime yang muncul di layar
    val featuredAnime: List<AnimeSummary> = emptyList(), // Daftar 5 anime terbaik untuk carousel atas
    val isFeaturedLoading: Boolean = false, // true saat carousel sedang memuat data
    val featuredErrorMessage: String? = null, // Pesan error jika carousel gagal dimuat
    val isLoading: Boolean = false, // true saat daftar anime utama sedang loading awal
    val isLoadingMore: Boolean = false, // true saat tombol "Muat halaman berikutnya" sedang bekerja
    val hasNextPage: Boolean = false, // true jika masih ada halaman berikutnya di server
    val currentPage: Int = 0, // Nomor halaman yang sedang aktif ditampilkan
    val loadMoreErrorMessage: String? = null, // Pesan error jika gagal memuat halaman berikutnya
    val hasLoadedAnime: Boolean = false, // Penanda apakah data anime sudah pernah berhasil diambil
    val errorMessage: String? = null, // Pesan error utama jika daftar anime gagal dimuat
    val isGenreLoading: Boolean = false, // true saat daftar dropdown genre sedang didownload
    val genreErrorMessage: String? = null, // Pesan error jika daftar genre gagal dimuat
)



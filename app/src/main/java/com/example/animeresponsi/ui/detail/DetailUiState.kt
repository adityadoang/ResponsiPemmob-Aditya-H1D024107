package com.example.animeresponsi.ui.detail

import com.example.animeresponsi.data.model.AnimeDetail

// Kondisi tampilan layar Detail (State).
data class DetailUiState(
    val animeDetail: AnimeDetail? = null, // Data lengkap anime yang sedang dilihat (null saat masih loading atau gagal)
    val isLoading: Boolean = false, // true saat sedang mengambil data detail dari server
    val errorMessage: String? = null, // Pesan error jika gagal memuat detail anime
)



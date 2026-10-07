package com.example.animeresponsi.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.animeresponsi.data.repository.AnimeRepository
import com.example.animeresponsi.ui.toAnimeDetailMessage
import java.util.concurrent.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Otak pengatur logika data untuk layar Detail anime (MVVM ViewModel).
class DetailViewModel(
    private val malId: Int, // ID anime yang dikirim dari halaman Home lewat navigasi
    private val repository: AnimeRepository, // Sumber pengambil data
) : ViewModel() {
    // State internal yang hanya bisa diubah dari dalam ViewModel ini.
    private val _uiState = MutableStateFlow(DetailUiState())

    // State publik yang dibaca oleh DetailScreen (read-only).
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        // Cek dulu ID animenya: kalau valid (> 0), langsung ambil data dari server.
        // Kalau ID tidak valid (misal -1), langsung pasang pesan error tanpa buang kuota panggil API.
        if (malId > 0) {
            loadDetail()
        } else {
            _uiState.value = DetailUiState(errorMessage = "ID anime tidak valid.")
        }
    }

    // Dipanggil saat tombol "Coba lagi" ditekan jika sebelumnya gagal memuat detail.
    fun retry() {
        if (malId > 0) loadDetail()
    }

    // Mengambil data detail anime dari server menggunakan coroutine di latar belakang.
    private fun loadDetail() {
        // Pasang status loading dan bersihkan pesan error lama
        _uiState.update { it.copy(animeDetail = null, isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val detail = repository.getAnimeDetail(malId)
                _uiState.value = DetailUiState(animeDetail = detail)
            } catch (cancellation: CancellationException) {
                throw cancellation // Jangan halangi coroutine yang dibatalkan oleh sistem Android
            } catch (error: Throwable) {
                // Terjemahkan error teknis menjadi pesan bahasa Indonesia yang mudah dimengerti user
                _uiState.value = DetailUiState(errorMessage = error.toAnimeDetailMessage())
            }
        }
    }

    // Factory untuk membuat instance DetailViewModel dengan menyuntikkan malId dan repository.
    class Factory(
        private val malId: Int,
        private val repository: AnimeRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(DetailViewModel::class.java))
            return DetailViewModel(malId, repository) as T
        }
    }
}


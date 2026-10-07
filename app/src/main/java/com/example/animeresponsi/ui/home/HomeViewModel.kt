package com.example.animeresponsi.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.animeresponsi.data.repository.AnimeRepository
import com.example.animeresponsi.ui.toAnimeListMessage
import com.example.animeresponsi.ui.toGenreMessage
import java.util.concurrent.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Otak pengatur logika data untuk layar Home (MVVM ViewModel).
// Berkomunikasi dengan repository dan menyiapkan data siap saji untuk tampilan Compose.
class HomeViewModel(
    private val repository: AnimeRepository,
) : ViewModel() {
    // State internal yang hanya bisa diubah dari dalam ViewModel ini saja.
    private val _uiState = MutableStateFlow(HomeUiState())

    // State publik yang dibaca oleh HomeScreen (hanya bisa dibaca / read-only).
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Menyimpan coroutine pencarian yang sedang berjalan, supaya bisa dibatalkan jika ada pencarian baru.
    private var listJob: Job? = null

    // Nomor versi request untuk mencegah respon lama yang lambat menimpa hasil pencarian terbaru.
    private var requestVersion = 0L

    // Menyimpan query & genre terakhir yang dicari, digunakan untuk tombol "Coba lagi".
    private var lastRequest = SearchParams()

    init {
        // Begitu layar Home terbuka, otomatis langsung ambil data awal:
        loadAnime(lastRequest) // 1. Muat 20 anime default (A-Z)
        loadFeatured() // 2. Muat 5 anime rating tertinggi untuk carousel atas
        loadGenres() // 3. Muat daftar genre untuk menu dropdown
    }

    // Dipanggil setiap kali pengguna mengetik di kolom pencarian.
    // Hanya mengupdate teks di layar tanpa langsung panggil API (supaya hemat kuota dan tidak spam server).
    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    // Dipanggil saat tombol "Cari" ditekan atau user menekan Enter di keyboard.
    fun search() {
        val state = _uiState.value
        loadAnime(
            SearchParams(
                query = state.query.trim().ifEmpty { null },
                genreId = state.selectedGenreId,
            ),
        )
    }

    // Dipanggil saat pengguna memilih salah satu genre di menu dropdown.
    fun selectGenre(genreId: Int?) {
        _uiState.update { it.copy(selectedGenreId = genreId) }
        loadAnime(
            SearchParams(
                query = _uiState.value.query.trim().ifEmpty { null },
                genreId = genreId,
            ),
        )
    }

    // Mengulang pencarian daftar anime jika sebelumnya terjadi error.
    fun retryAnime() {
        loadAnime(lastRequest)
    }

    // Memuat 20 anime berikutnya saat tombol "Muat halaman berikutnya" ditekan (fitur paginasi).
    fun loadMore() {
        val state = _uiState.value
        // Cegah request jika: belum pernah muat data, tidak ada halaman lagi, atau sedang loading
        if (!state.hasLoadedAnime || !state.hasNextPage || state.isLoading || state.isLoadingMore) return
        val nextPage = state.currentPage + 1
        val thisRequest = requestVersion
        val params = lastRequest
        _uiState.update { it.copy(isLoadingMore = true, loadMoreErrorMessage = null) }
        listJob = viewModelScope.launch {
            try {
                val result = repository.searchAnime(params.query, params.genreId, nextPage)
                currentCoroutineContext().ensureActive()
                // Pastikan hanya request terbaru yang berhak memperbarui tampilan
                if (thisRequest == requestVersion) {
                    _uiState.update {
                        it.copy(
                            // Gabungkan data lama dengan data baru, lalu buang jika ada ID yang kembar
                            animeList = (it.animeList + result.items).distinctBy { anime -> anime.malId },
                            currentPage = nextPage,
                            hasNextPage = result.hasNextPage,
                            isLoadingMore = false,
                        )
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Throwable) {
                if (thisRequest == requestVersion) {
                    _uiState.update {
                        it.copy(isLoadingMore = false, loadMoreErrorMessage = error.toAnimeListMessage())
                    }
                }
            }
        }
    }

    // Mengulang download daftar genre jika sebelumnya gagal.
    fun retryGenres() {
        loadGenres()
    }

    // Fungsi inti untuk mengambil halaman pertama daftar anime dari server.
    private fun loadAnime(params: SearchParams) {
        listJob?.cancel() // Batalkan pencarian sebelumnya yang belum selesai
        lastRequest = params
        val thisRequest = ++requestVersion

        // Pasang status loading dan bersihkan pesan error lama
        _uiState.update {
            it.copy(
                animeList = emptyList(),
                isLoading = true,
                isLoadingMore = false,
                hasNextPage = false,
                currentPage = 0,
                loadMoreErrorMessage = null,
                hasLoadedAnime = false,
                errorMessage = null,
            )
        }

        listJob = viewModelScope.launch {
            try {
                val result = repository.searchAnime(params.query, params.genreId, 1)
                currentCoroutineContext().ensureActive()
                if (thisRequest == requestVersion) {
                    _uiState.update {
                        it.copy(
                            animeList = result.items,
                            hasNextPage = result.hasNextPage,
                            currentPage = 1,
                            isLoading = false,
                            hasLoadedAnime = true,
                            errorMessage = null,
                        )
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Throwable) {
                if (thisRequest == requestVersion) {
                    _uiState.update {
                        it.copy(
                            animeList = emptyList(),
                            isLoading = false,
                            hasLoadedAnime = false,
                            errorMessage = error.toAnimeListMessage(),
                        )
                    }
                }
            }
        }
    }

    // Mengambil 5 anime rating tertinggi dari server untuk ditampilkan di carousel header atas.
    private fun loadFeatured() {
        _uiState.update { it.copy(isFeaturedLoading = true, featuredErrorMessage = null) }
        viewModelScope.launch {
            try {
                val featured = repository.getTopAnime()
                _uiState.update {
                    it.copy(featuredAnime = featured, isFeaturedLoading = false)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Throwable) {
                _uiState.update {
                    it.copy(isFeaturedLoading = false, featuredErrorMessage = error.toAnimeListMessage())
                }
            }
        }
    }

    // Mengulang pemuatan carousel atas jika sebelumnya gagal.
    fun retryFeatured() {
        loadFeatured()
    }

    // Mengambil daftar semua genre anime dari server untuk mengisi menu dropdown filter.
    private fun loadGenres() {
        _uiState.update { it.copy(isGenreLoading = true, genreErrorMessage = null) }
        viewModelScope.launch {
            try {
                val genres = repository.getGenres()
                _uiState.update {
                    it.copy(
                        genres = genres,
                        isGenreLoading = false,
                        genreErrorMessage = null,
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Throwable) {
                _uiState.update {
                    it.copy(
                        isGenreLoading = false,
                        genreErrorMessage = error.toGenreMessage(),
                    )
                }
            }
        }
    }

    // Data pembantu untuk menyimpan parameter query dan genre aktif
    private data class SearchParams(
        val query: String? = null,
        val genreId: Int? = null,
    )

    // Factory untuk membuat instance HomeViewModel dengan menyuntikkan AnimeRepository ke konstruktornya.
    class Factory(
        private val repository: AnimeRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(HomeViewModel::class.java))
            return HomeViewModel(repository) as T
        }
    }
}


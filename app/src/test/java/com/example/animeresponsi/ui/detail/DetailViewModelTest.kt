package com.example.animeresponsi.ui.detail

import com.example.animeresponsi.MainDispatcherRule
import com.example.animeresponsi.data.model.AnimeDetail
import com.example.animeresponsi.data.model.AnimeSummary
import com.example.animeresponsi.data.model.Genre
import com.example.animeresponsi.data.repository.AnimeRepository
import com.example.animeresponsi.data.repository.AnimePage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/**
 * Pengujian unit untuk [DetailViewModel] memverifikasi pemuatan data berdasarkan malId
 * dan penanganan ID tidak valid tanpa melakukan request ke repository.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    /**
     * Memverifikasi pemuatan detail sukses saat ID anime valid (> 0).
     */
    @Test
    fun `loads detail for selected mal id`() = runTest {
        val repository = DetailRepository()

        val viewModel = DetailViewModel(malId = 42, repository = repository)
        advanceUntilIdle()

        assertEquals(42, repository.requestedId)
        assertEquals("Major", viewModel.uiState.value.animeDetail?.title)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    /**
     * Memverifikasi bahwa ID anime <= 0 langsung menghasilkan pesan error tanpa memanggil repository.
     */
    @Test
    fun `invalid id stays on detail error without calling repository`() = runTest {
        val repository = DetailRepository()

        val viewModel = DetailViewModel(malId = -1, repository = repository)
        advanceUntilIdle()

        assertNull(repository.requestedId)
        assertEquals("ID anime tidak valid.", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }
}


private class DetailRepository : AnimeRepository {
    var requestedId: Int? = null

    override suspend fun searchAnime(query: String?, genreId: Int?, page: Int): AnimePage =
        AnimePage(emptyList(), false)

    override suspend fun getTopAnime(): List<AnimeSummary> = emptyList()

    override suspend fun getGenres(): List<Genre> = emptyList()

    override suspend fun getAnimeDetail(malId: Int): AnimeDetail {
        requestedId = malId
        return AnimeDetail(
            malId = malId,
            title = "Major",
            genres = listOf("Sports"),
            score = 8.2,
            rating = "PG-13",
            episodes = 26,
            status = "Finished Airing",
            synopsis = "Synopsis",
            imageUrl = null,
        )
    }
}

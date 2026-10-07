package com.example.animeresponsi.ui.home

import com.example.animeresponsi.MainDispatcherRule
import com.example.animeresponsi.data.model.AnimeDetail
import com.example.animeresponsi.data.model.AnimeSummary
import com.example.animeresponsi.data.model.Genre
import com.example.animeresponsi.data.repository.AnimeRepository
import com.example.animeresponsi.data.repository.AnimePage
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/**
 * Pengujian unit untuk [HomeViewModel] memverifikasi isolasi error genre,
 * pengiriman query dan filter genre, pencegahan race condition respon lama, dan paginasi loadMore.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    /**
     * Memverifikasi bahwa kegagalan request genre tidak membatalkan pemuatan daftar anime utama.
     */
    @Test
    fun `initial list succeeds even when genre request fails`() = runTest {
        val repository = ScriptedRepository().apply {
            defaultResult = listOf(summary(1, "Major"))
            genreError = IOException("offline")
        }

        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        assertEquals(listOf("Major"), viewModel.uiState.value.animeList.map { it.title })
        assertNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(
            "Genre gagal dimuat karena layanan Tenrai tidak dapat dijangkau.",
            viewModel.uiState.value.genreErrorMessage,
        )
    }

    /**
     * Memverifikasi bahwa memilih genre akan memicu pencarian baru dengan teks query yang di-trim.
     */
    @Test
    fun `selecting genre sends trimmed query and genre in one request`() = runTest {
        val repository = ScriptedRepository()
        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        viewModel.onQueryChange("  major  ")
        viewModel.selectGenre(30)
        advanceUntilIdle()

        assertEquals(SearchCall("major", 30, 1), repository.calls.last())
        assertEquals(30, viewModel.uiState.value.selectedGenreId)
    }

    /**
     * Memverifikasi perlindungan race condition: respons lama yang terlambat selesai tidak boleh menimpa hasil pencarian terbaru.
     */
    @Test
    fun `late old response cannot replace newest search`() = runTest {
        val repository = ScriptedRepository()
        val oldResult = CompletableDeferred<AnimePage>()
        repository.searchBlock = { query, _, _ ->
            when (query) {
                "A" -> withContext(NonCancellable) { oldResult.await() }
                "B" -> AnimePage(listOf(summary(2, "Result B")), false)
                else -> AnimePage(emptyList(), false)
            }
        }
        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        viewModel.onQueryChange("A")
        viewModel.search()
        runCurrent()
        viewModel.onQueryChange("B")
        viewModel.search()
        runCurrent()

        assertEquals(listOf("Result B"), viewModel.uiState.value.animeList.map { it.title })

        oldResult.complete(AnimePage(listOf(summary(1, "Result A")), false))
        advanceUntilIdle()

        assertEquals(listOf("Result B"), viewModel.uiState.value.animeList.map { it.title })
    }

    /**
     * Memverifikasi fungsi paginasi loadMore: menambahkan anime unik ke daftar dan mempertahankan carousel featured.
     */
    @Test
    fun `load more appends unique anime and keeps featured independent`() = runTest {
        val repository = ScriptedRepository().apply {
            featuredResult = listOf(summary(9, "Top Anime"))
            searchBlock = { _, _, page ->
                if (page == 1) AnimePage(listOf(summary(1, "Alpha")), true)
                else AnimePage(listOf(summary(1, "Alpha"), summary(2, "Beta")), false)
            }
        }
        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()
        viewModel.loadMore()
        advanceUntilIdle()

        assertEquals(listOf("Alpha", "Beta"), viewModel.uiState.value.animeList.map { it.title })
        assertEquals(listOf("Top Anime"), viewModel.uiState.value.featuredAnime.map { it.title })
        assertEquals(2, viewModel.uiState.value.currentPage)
        assertFalse(viewModel.uiState.value.hasNextPage)
        assertEquals(SearchCall(null, null, 2), repository.calls.last())
    }
}


private data class SearchCall(val query: String?, val genreId: Int?, val page: Int)

private class ScriptedRepository : AnimeRepository {
    var defaultResult: List<AnimeSummary> = emptyList()
    var featuredResult: List<AnimeSummary> = emptyList()
    var genreError: Throwable? = null
    val calls = mutableListOf<SearchCall>()
    var searchBlock: (suspend (String?, Int?, Int) -> AnimePage)? = null

    override suspend fun searchAnime(query: String?, genreId: Int?, page: Int): AnimePage {
        calls += SearchCall(query, genreId, page)
        return searchBlock?.invoke(query, genreId, page) ?: AnimePage(defaultResult, false)
    }

    override suspend fun getTopAnime(): List<AnimeSummary> = featuredResult

    override suspend fun getGenres(): List<Genre> {
        genreError?.let { throw it }
        return listOf(Genre(30, "Sports"))
    }

    override suspend fun getAnimeDetail(malId: Int): AnimeDetail = error("Not used")
}

private fun summary(id: Int, title: String) = AnimeSummary(
    malId = id,
    title = title,
    type = "TV",
    score = 8.0,
    rating = "PG-13",
    imageUrl = null,
)

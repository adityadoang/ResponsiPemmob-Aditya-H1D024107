package com.example.animeresponsi.data.repository

import com.example.animeresponsi.data.model.AnimeDetailResponse
import com.example.animeresponsi.data.model.AnimeDto
import com.example.animeresponsi.data.model.AnimeListResponse
import com.example.animeresponsi.data.model.GenreDto
import com.example.animeresponsi.data.model.GenreListResponse
import com.example.animeresponsi.data.model.ImagesDto
import com.example.animeresponsi.data.model.JpgImageDto
import com.example.animeresponsi.data.model.PaginationDto
import com.example.animeresponsi.data.model.TitleDto
import com.example.animeresponsi.data.remote.TenraiApiService
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Pengujian unit untuk [NetworkAnimeRepository] memvalidasi mapping DTO ke domain model,
 * penyaringan data tidak valid, deduplikasi, dan mekanisme in-memory caching genre.
 */
class NetworkAnimeRepositoryTest {
    /**
     * Memverifikasi pemetaan judul default, penghapusan ID null/invalid, dan deduplikasi ID anime.
     */
    @Test
    fun `search maps default title removes invalid ids and deduplicates`() = runTest {
        val valid = animeDto(
            id = 1,
            title = "Fallback",
            titles = listOf(TitleDto(type = "Default", title = "Major")),
        )
        val api = FakeTenraiApi(
            listResponse = AnimeListResponse(
                data = listOf(valid, valid.copy(title = "Duplicate"), animeDto(id = null)),
                pagination = PaginationDto(true),
            ),
        )

        val result = NetworkAnimeRepository(api).searchAnime(query = "major", genreId = 30, page = 2)

        assertEquals(1, result.items.size)
        assertEquals("Major", result.items.single().title)
        assertEquals(1, result.items.single().malId)
        assertEquals("https://example.com/poster.jpg", result.items.single().imageUrl)
        assertEquals(true, result.hasNextPage)
        assertEquals("major", api.lastQuery)
        assertEquals(30, api.lastGenreId)
        assertEquals(2, api.lastPage)
        assertEquals("title", api.lastOrderBy)
        assertEquals("asc", api.lastSort)
    }

    /**
     * Memverifikasi bahwa field bernilai null dari API dipertahankan sebagai null agar UI menampilkan fallback teks yang jujur.
     */
    @Test
    fun `detail keeps null fields so ui can show honest placeholders`() = runTest {
        val api = FakeTenraiApi(
            detailResponse = AnimeDetailResponse(
                data = animeDto(
                    id = 7,
                    title = " ",
                    titles = emptyList(),
                    score = null,
                    genres = emptyList(),
                ),
            ),
        )

        val detail = NetworkAnimeRepository(api).getAnimeDetail(7)

        assertEquals("Judul tidak tersedia", detail.title)
        assertEquals(emptyList<String>(), detail.genres)
        assertNull(detail.score)
        assertNull(detail.episodes)
        assertNull(detail.synopsis)
    }

    /**
     * Memverifikasi pemfilteran anime pada carousel: hanya menyertakan anime dengan skor > 0 dan mengurutkannya dari skor tertinggi.
     */
    @Test
    fun `featured anime uses top endpoint and keeps scored entries only`() = runTest {
        val api = FakeTenraiApi(
            topResponse = AnimeListResponse(
                data = listOf(
                    animeDto(id = 1, title = "Lower", score = 8.0),
                    animeDto(id = 2, title = "Highest", score = 9.5),
                    animeDto(id = 3, title = "Unscored", score = 0.0),
                ),
            ),
        )

        val featured = NetworkAnimeRepository(api).getTopAnime()

        assertEquals(listOf("Highest", "Lower"), featured.map { it.title })
        assertEquals(10, api.lastTopLimit)
        assertEquals(true, api.lastTopSfw)
    }

    /**
     * Memverifikasi penghapusan genre invalid, pengurutan alfabetis A-Z, dan cache memori (hanya 1 network call).
     */
    @Test
    fun `genres discard invalid values sort and cache in memory`() = runTest {
        val api = FakeTenraiApi(
            genreResponse = GenreListResponse(
                data = listOf(
                    GenreDto(2, "Sports"),
                    GenreDto(null, "Invalid"),
                    GenreDto(1, "Action"),
                    GenreDto(2, "Sports duplicate"),
                ),
            ),
        )
        val repository = NetworkAnimeRepository(api)

        val first = repository.getGenres()
        val second = repository.getGenres()

        assertEquals(listOf("Action", "Sports"), first.map { it.name })
        assertEquals(first, second)
        assertEquals(1, api.genreCalls)
    }
}


private class FakeTenraiApi(
    private val listResponse: AnimeListResponse = AnimeListResponse(emptyList()),
    private val topResponse: AnimeListResponse = AnimeListResponse(emptyList()),
    private val detailResponse: AnimeDetailResponse = AnimeDetailResponse(animeDto(id = 1)),
    private val genreResponse: GenreListResponse = GenreListResponse(emptyList()),
) : TenraiApiService {
    var lastQuery: String? = null
    var lastGenreId: Int? = null
    var lastPage: Int? = null
    var lastOrderBy: String? = null
    var lastSort: String? = null
    var lastTopLimit: Int? = null
    var lastTopSfw: Boolean? = null
    var genreCalls: Int = 0

    override suspend fun searchAnime(
        query: String?,
        genreId: Int?,
        page: Int,
        limit: Int,
        orderBy: String,
        sort: String,
    ): AnimeListResponse {
        lastQuery = query
        lastGenreId = genreId
        lastPage = page
        lastOrderBy = orderBy
        lastSort = sort
        return listResponse
    }

    override suspend fun getTopAnime(limit: Int, sfw: Boolean): AnimeListResponse {
        lastTopLimit = limit
        lastTopSfw = sfw
        return topResponse
    }

    override suspend fun getGenres(filter: String): GenreListResponse {
        genreCalls++
        return genreResponse
    }

    override suspend fun getAnimeDetail(id: Int): AnimeDetailResponse = detailResponse
}

private fun animeDto(
    id: Int?,
    title: String? = "Fallback title",
    titles: List<TitleDto>? = null,
    score: Double? = 8.4,
    genres: List<GenreDto>? = listOf(GenreDto(30, "Sports")),
): AnimeDto = AnimeDto(
    malId = id,
    titles = titles,
    title = title,
    type = "TV",
    score = score,
    rating = null,
    episodes = null,
    status = null,
    synopsis = null,
    genres = genres,
    images = ImagesDto(JpgImageDto("https://example.com/poster.jpg")),
)

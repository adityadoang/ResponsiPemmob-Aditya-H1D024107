package com.example.animeresponsi.data.repository

import com.example.animeresponsi.data.model.AnimeDetail
import com.example.animeresponsi.data.model.AnimeDto
import com.example.animeresponsi.data.model.AnimeSummary
import com.example.animeresponsi.data.model.Genre
import com.example.animeresponsi.data.remote.TenraiApiService

// Kontrak penyedia data anime (Interface).
// Memisahkan urusan UI/ViewModel dari urusan teknis pemanggilan jaringan internet.
interface AnimeRepository {
    // Mengambil daftar anime sesuai query pencarian, genre, dan nomor halaman paginasi.
    suspend fun searchAnime(query: String?, genreId: Int?, page: Int): AnimePage

    // Mengambil anime dengan skor tertinggi untuk dipajang di carousel atas Home.
    suspend fun getTopAnime(): List<AnimeSummary>

    // Mengambil daftar seluruh genre anime untuk dropdown filter (dilengkapi cache memori).
    suspend fun getGenres(): List<Genre>

    // Mengambil data detail lengkap 1 anime berdasarkan ID (mal_id).
    suspend fun getAnimeDetail(malId: Int): AnimeDetail
}

// Wadah pembungkus hasil pencarian anime beserta info apakah masih ada halaman berikutnya.
data class AnimePage(
    val items: List<AnimeSummary>, // Daftar anime di halaman ini
    val hasNextPage: Boolean, // true jika masih bisa muat halaman berikutnya
)

// Implementasi nyata AnimeRepository yang langsung menghubungi Tenrai REST API.
class NetworkAnimeRepository(
    private val api: TenraiApiService,
) : AnimeRepository {
    // Variabel penyimpan genre di memori RAM supaya aplikasi tidak berulang kali download genre yang sama.
    @Volatile
    private var cachedGenres: List<Genre>? = null

    // Memanggil API pencarian anime, membersihkan data yang rusak, dan membuang ID yang duplikat.
    override suspend fun searchAnime(query: String?, genreId: Int?, page: Int): AnimePage {
        val response = api.searchAnime(query = query, genreId = genreId, page = page)
        // orEmpty() mencegah crash kalau server kirim null.
        // mapNotNull mengubah DTO jadi model rapi, sekaligus membuang data invalid.
        // distinctBy memastikan tidak ada 2 kartu dengan ID yang sama di LazyColumn.
        val items = response.data.orEmpty()
            .mapNotNull(AnimeDto::toSummaryOrNull)
            .distinctBy(AnimeSummary::malId)
        return AnimePage(
            items = items,
            hasNextPage = response.pagination?.hasNextPage == true && page < 1000,
        )
    }

    // Mengambil kandidat anime teratas dari API, buang yang skornya 0, lalu ambil 5 anime terbaik untuk carousel.
    override suspend fun getTopAnime(): List<AnimeSummary> =
        api.getTopAnime()
            .data.orEmpty()
            .mapNotNull(AnimeDto::toSummaryOrNull)
            .filter { (it.score ?: 0.0) > 0.0 }
            .distinctBy(AnimeSummary::malId)
            .sortedByDescending { it.score }
            .take(5)

    // Mengambil daftar genre. Kalau sudah pernah download, ambil langsung dari memori RAM (cache).
    override suspend fun getGenres(): List<Genre> {
        // Cek apakah data genre sudah ada di cache memori
        cachedGenres?.let { return it }

        // Kalau belum ada, download dari server, validasi data, lalu urutkan nama genre dari A ke Z
        return api.getGenres()
            .data
            .orEmpty()
            .mapNotNull { genre ->
                val id = genre.malId?.takeIf { it > 0 } ?: return@mapNotNull null
                val name = genre.name?.trim()?.takeIf(String::isNotEmpty) ?: return@mapNotNull null
                Genre(malId = id, name = name)
            }
            .distinctBy(Genre::malId)
            .sortedBy(Genre::name)
            .also { cachedGenres = it } // Simpan ke cache untuk panggilan berikutnya
    }

    // Mengambil informasi lengkap 1 anime untuk ditampilkan di layar Detail.
    override suspend fun getAnimeDetail(malId: Int): AnimeDetail {
        val anime = api.getAnimeDetail(malId).data
            ?: throw IllegalStateException("Respons detail tidak berisi data")
        return anime.toDetail(malId)
    }
}

// Fungsi pembantu untuk mengubah DTO mentah dari server menjadi model AnimeSummary siap pakai.
// Mengembalikan null kalau ID anime <= 0 (otomatis dibuang dari daftar tampilan).
internal fun AnimeDto.toSummaryOrNull(): AnimeSummary? {
    val id = malId?.takeIf { it > 0 } ?: return null
    return AnimeSummary(
        malId = id,
        title = preferredTitle(),
        type = type.cleanOrNull(),
        score = score,
        rating = rating.cleanOrNull(),
        imageUrl = images?.jpg?.imageUrl.cleanOrNull(),
    )
}

// Fungsi pembantu untuk mengubah DTO mentah menjadi model AnimeDetail untuk layar Detail.
internal fun AnimeDto.toDetail(requestedId: Int): AnimeDetail = AnimeDetail(
    malId = malId?.takeIf { it > 0 } ?: requestedId,
    title = preferredTitle(),
    genres = genres.orEmpty().mapNotNull { it.name.cleanOrNull() }.distinct(),
    score = score,
    rating = rating.cleanOrNull(),
    episodes = episodes,
    status = status.cleanOrNull(),
    synopsis = synopsis.cleanOrNull(),
    imageUrl = images?.jpg?.imageUrl.cleanOrNull(),
)

// Menentukan judul anime terbaik:
// 1. Cari judul bertipe "Default"
// 2. Kalau tidak ada, pakai judul standar 'title'
// 3. Kalau tetap kosong, beri teks aman "Judul tidak tersedia" agar UI tidak kosong
private fun AnimeDto.preferredTitle(): String =
    titles.orEmpty()
        .firstOrNull { it.type.equals("Default", ignoreCase = true) }
        ?.title
        .cleanOrNull()
        ?: title.cleanOrNull()
        ?: "Judul tidak tersedia"

// Membersihkan spasi di awal/akhir string dan mengubah teks kosong ("") jadi null,
// supaya teks pengganti default di UI bisa aktif dengan benar.
private fun String?.cleanOrNull(): String? = this?.trim()?.takeIf(String::isNotEmpty)


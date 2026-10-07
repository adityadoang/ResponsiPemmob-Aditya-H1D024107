package com.example.animeresponsi.data.remote

import com.example.animeresponsi.data.model.AnimeDetailResponse
import com.example.animeresponsi.data.model.AnimeListResponse
import com.example.animeresponsi.data.model.GenreListResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Daftar fungsi endpoint API .
interface TenraiApiService {
    // Mengambil daftar anime dengan pencarian judul, filter genre, dan paginasi (halaman demi halaman).
    @GET("anime")
    suspend fun searchAnime(
        @Query("q") query: String?, // Kata kunci pencarian (bisa null kalau sedang tidak mencari)
        @Query("genres") genreId: Int?, // ID genre yang dipilih (bisa null kalau ingin semua genre)
        @Query("page") page: Int = 1, // Nomor halaman yang ingin diambil (mulai dari halaman 1)
        @Query("limit") limit: Int = 20, // Ambil 20 anime per request (standar optimal agar loading cepat)
        @Query("order_by") orderBy: String = "title", // Diurutkan berdasarkan judul anime
        @Query("sort") sort: String = "asc",
    ): AnimeListResponse

    // Mengambil anime dengan rating paling tinggi
    @GET("top/anime")
    suspend fun getTopAnime(
        @Query("limit") limit: Int = 10,
    ): AnimeListResponse

    // Mengambil daftar semua genre resmi untuk mengisi pilihan dropdown filter.
    @GET("genres/anime")
    suspend fun getGenres(
        @Query("filter") filter: String = "genres", // Ambil kategori genre utama saja
    ): GenreListResponse

    // Mengambil data detail lengkap 1 anime berdasarkan ID (mal_id).
    @GET("anime/{id}")
    suspend fun getAnimeDetail(
        @Path("id") id: Int, // ID anime yang disisipkan langsung ke dalam alamat URL
    ): AnimeDetailResponse
}


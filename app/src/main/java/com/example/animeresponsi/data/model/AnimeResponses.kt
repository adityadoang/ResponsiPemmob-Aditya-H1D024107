package com.example.animeresponsi.data.model

import com.google.gson.annotations.SerializedName

// Model penampung respons JSON saat mengambil daftar anime (/anime dan /top/anime).
data class AnimeListResponse(
    val data: List<AnimeDto>?, // Daftar anime mentah dari server
    val pagination: PaginationDto? = null, // Info halaman untuk tahu masih ada halaman berikutnya atau tidak
)

// Data info halaman dari server.
data class PaginationDto(
    @SerializedName("has_next_page") val hasNextPage: Boolean?, // true jika masih ada halaman berikutnya
)

// Model penampung respons JSON saat mengambil detail 1 anime (/anime/{id}).
data class AnimeDetailResponse(
    val data: AnimeDto?, // Objek anime tunggal dari server
)

// Model penampung respons JSON saat mengambil daftar genre (/genres/anime).
data class GenreListResponse(
    val data: List<GenreDto>?, // Daftar semua genre dari server
)

// Model mentah data anime langsung dari struktur JSON server.
// Semua field sengaja dibuat nullable (?) supaya aplikasi tidak pernah crash jika server mengirimkan data kosong (null).
data class AnimeDto(
    @SerializedName("mal_id") val malId: Int?, // ID anime MyAnimeList
    val titles: List<TitleDto>?, // Daftar variasi judul (Jepang, Inggris, Default, dll.)
    val title: String?, // Judul standar bawaan server
    val type: String?, // Tipe serial (TV, Movie, OVA, dll.)
    val score: Double?, // Skor rata-rata dari pengguna
    val rating: String?, // Klasifikasi batasan umur (PG-13, R-17+, dll.)
    val episodes: Int?, // Jumlah total episode
    val status: String?, // Status tayang (Finished Airing / Currently Airing)
    val synopsis: String?, // Ringkasan alur cerita / sinopsis
    val genres: List<GenreDto>?, // Daftar kategori genre anime ini
    val images: ImagesDto?, // Struktur pembungkus link gambar poster
)

// Model pembantu untuk alternatif judul anime.
data class TitleDto(
    val type: String?, // Jenis judul, misal: "Default", "English", "Japanese"
    val title: String?, // Teks judulnya
)

// Model mentah data genre dari server.
data class GenreDto(
    @SerializedName("mal_id") val malId: Int?, // ID angka kategori genre
    val name: String?, // Nama genre, misal: "Sports" atau "Action"
)

// Pembungkus link gambar dari server.
data class ImagesDto(
    val jpg: JpgImageDto?, // Format gambar JPEG
)

// Link gambar poster spesifik.
data class JpgImageDto(
    @SerializedName("image_url") val imageUrl: String?, // URL gambar poster online
)



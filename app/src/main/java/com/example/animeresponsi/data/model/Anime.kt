package com.example.animeresponsi.data.model

// Model ringkas anime khusus untuk kartu item di halaman Home.
data class AnimeSummary(
    val malId: Int, // ID unik MyAnimeList, dipakai untuk key LazyColumn dan parameter navigasi
    val title: String, // Judul anime (selalu ada nilai, kalau dari server kosong sudah disiapkan teks aman)
    val type: String?, // Tipe tayangan (TV/Movie). Dibuat nullable karena ada anime yang tipenya belum tercatat
    val score: Double?, // Skor dari user (0-10). Nullable karena anime baru rilis belum punya rating
    val rating: String?, // Batasan usia penonton (PG-13/R). Nullable kalau belum ada klasifikasi umur
    val imageUrl: String?, // URL link poster anime yang akan di-load oleh Coil. Nullable jika belum ada gambar
)

// Model lengkap anime untuk layar Detail, memuat 6 komponen wajib dari modul responsi.
data class AnimeDetail(
    val malId: Int, // ID unik anime
    val title: String, // 1. Judul anime
    val genres: List<String>, // 2. Daftar genre anime (misal: Action, Sports)
    val score: Double?, // 3a. Skor nilai pengguna (0.0 - 10.0)
    val rating: String?, // 3b. Klasifikasi rating usia penonton (misal: PG-13)
    val episodes: Int?, // 4. Total jumlah episode
    val status: String?, // 5. Status tayang (Finished Airing / Currently Airing)
    val synopsis: String?, // 6. Ringkasan jalan cerita / sinopsis
    val imageUrl: String?, // URL poster anime untuk tampilan besar di detail
)

// Model genre untuk item pilihan di menu dropdown filter.
data class Genre(
    val malId: Int, // ID angka genre yang dikirim ke API (contoh: 1 untuk Action, 30 untuk Sports)
    val name: String, // Nama genre yang dibaca oleh pengguna di layar (contoh: "Sports")
)


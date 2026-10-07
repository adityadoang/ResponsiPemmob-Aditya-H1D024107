package com.example.animeresponsi.ui

import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import retrofit2.HttpException

// Mengubah error teknis sistem saat mengambil daftar anime menjadi kalimat bahasa Indonesia yang ramah pengguna.
internal fun Throwable.toAnimeListMessage(): String = when (this) {
    is HttpException -> when (code()) {
        429 -> "Terlalu banyak permintaan. Tunggu sebentar lalu coba lagi." // Kena batas limit request server (rate-limit)
        in 500..599 -> "Server Tenrai sedang bermasalah. Coba lagi nanti." // Masalah internal server
        else -> "Data anime belum dapat dimuat. Coba lagi."
    }

    is SocketTimeoutException -> "Layanan Tenrai tidak merespons. Coba lagi nanti." // Waktu koneksi habis (timeout)
    is UnknownHostException -> "Alamat layanan Tenrai tidak dapat ditemukan." // Tidak ada internet atau DNS tidak merespons
    is ConnectException, is SSLException -> "Tidak dapat terhubung ke layanan Tenrai." // Gagal handshake koneksi aman SSL
    is IOException -> "Layanan Tenrai tidak dapat dijangkau. Koneksi internet Anda mungkin tetap normal." // Gangguan jaringan umum
    else -> "Data anime belum dapat dimuat. Coba lagi."
}

// Mengubah error teknis saat mengambil pilihan genre menjadi pesan yang mudah dimengerti.
internal fun Throwable.toGenreMessage(): String = when (this) {
    is HttpException -> if (code() == 429) {
        "Batas permintaan tercapai. Coba muat genre lagi nanti."
    } else {
        "Genre belum dapat dimuat."
    }

    is SocketTimeoutException -> "Layanan Tenrai tidak merespons saat memuat genre."
    is UnknownHostException -> "Alamat layanan Tenrai tidak dapat ditemukan."
    is ConnectException, is SSLException -> "Tidak dapat terhubung ke layanan Tenrai."
    is IOException -> "Genre gagal dimuat karena layanan Tenrai tidak dapat dijangkau."
    else -> "Genre belum dapat dimuat."
}

// Mengubah error teknis saat mengambil detail 1 anime menjadi pesan yang informatif untuk pengguna.
internal fun Throwable.toAnimeDetailMessage(): String = when (this) {
    is HttpException -> when (code()) {
        404 -> "Detail anime tidak ditemukan." // Anime dengan ID tersebut tidak ada di server
        429 -> "Terlalu banyak permintaan. Tunggu sebentar lalu coba lagi."
        in 500..599 -> "Server Tenrai sedang bermasalah. Coba lagi nanti."
        else -> "Detail anime belum dapat dimuat."
    }

    is SocketTimeoutException -> "Layanan Tenrai tidak merespons. Coba lagi nanti."
    is UnknownHostException -> "Alamat layanan Tenrai tidak dapat ditemukan."
    is ConnectException, is SSLException -> "Tidak dapat terhubung ke layanan Tenrai."
    is IOException -> "Layanan Tenrai tidak dapat dijangkau. Koneksi internet Anda mungkin tetap normal."
    else -> "Detail anime belum dapat dimuat."
}


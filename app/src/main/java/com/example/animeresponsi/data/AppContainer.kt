package com.example.animeresponsi.data

import com.example.animeresponsi.data.remote.ApiClient
import com.example.animeresponsi.data.repository.AnimeRepository
import com.example.animeresponsi.data.repository.NetworkAnimeRepository

// Wadah penyedia dependensi aplikasi (Dependency Injection)
class AppContainer {
    // Objek repository yang bertugas mengambil data anime dari internet.
    // Dibuat 'lazy' dan bertipe interface AnimeRepository agar kodenya rapi dan mudah diuji (unit testing).
    val animeRepository: AnimeRepository by lazy {
        NetworkAnimeRepository(ApiClient.service)
    }
}


package com.example.animeresponsi.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // Alamat server Tenrai API v4 .
    private const val BASE_URL = "https://api.tenrai.org/v1/"

    // Dibuat 'lazy' supaya koneksi baru dibangun saat aplikasi mulai mengambil data.
    val service: TenraiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Mengubah teks JSON dari server jadi objek data Kotlin otomatis
            .build()
            .create(TenraiApiService::class.java)
    }
}



package com.example.animeresponsi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.animeresponsi.data.AppContainer
import com.example.animeresponsi.ui.navigation.AnimeNavHost
import com.example.animeresponsi.ui.theme.AnimeResponsiTheme

// Pintu masuk utama aplikasi Android (Single Activity).
// Menggunakan Jetpack Compose murni, jadi tidak memakai layout XML sama sekali.
class MainActivity : ComponentActivity() {
    // Wadah penyedia repository (Dependency Injection manual tanpa library tambahan).
    // Dibuat 'lazy' supaya objek baru dibikin di memori saat pertama kali dipanggil.
    private val appContainer by lazy { AppContainer() }

    // Fungsi bawaan Android yang otomatis berjalan pertama kali saat aplikasi dibuka.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContent bertugas menampilkan tampilan Jetpack Compose ke layar perangkat.
        setContent {
            // Pasang tema gelap kustom aplikasi.
            AnimeResponsiTheme {
                // Jalankan pengatur rute navigasi dan oper data repository ke dalamnya.
                AnimeNavHost(repository = appContainer.animeRepository)
            }
        }
    }
}


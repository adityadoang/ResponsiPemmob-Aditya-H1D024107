package com.example.animeresponsi.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.animeresponsi.data.repository.AnimeRepository
import com.example.animeresponsi.ui.detail.DetailScreen
import com.example.animeresponsi.ui.detail.DetailViewModel
import com.example.animeresponsi.ui.home.HomeScreen
import com.example.animeresponsi.ui.home.HomeViewModel

// Rute navigasi: Aplikasi ini dibatasi tepat 2 layar  (Home dan Detail).
private const val HOME_ROUTE = "home"
private const val DETAIL_ROUTE = "detail/{malId}"
private const val MAL_ID_ARGUMENT = "malId"

// Pengatur perpindahan halaman utama dengan Navigation Compose.
@Composable
fun AnimeNavHost(repository: AnimeRepository) {
    // Pengendali navigasi untuk maju atau kembali ke halaman sebelumnya.
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = HOME_ROUTE, // Layar pertama yang dibuka adalah Home
    ) {
        // Halaman 1: Home Screen
        composable(HOME_ROUTE) {
            // Siapkan ViewModel untuk layar Home
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModel.Factory(repository),
            )
            // Ambil state data secara reaktif dan aman dari siklus hidup (lifecycle) Android
            val state = homeViewModel.uiState.collectAsStateWithLifecycle().value

            HomeScreen(
                state = state,
                onQueryChange = homeViewModel::onQueryChange,
                onSearch = homeViewModel::search,
                onGenreSelected = homeViewModel::selectGenre,
                onRetryAnime = homeViewModel::retryAnime,
                onRetryGenres = homeViewModel::retryGenres,
                onRetryFeatured = homeViewModel::retryFeatured,
                onLoadMore = homeViewModel::loadMore,
                onAnimeClick = { malId ->
                    // Buka layar detail sesuai ID anime yang diklik.
                    // launchSingleTop = true mencegah layar detail terbuka dobel jika diklik berkali-kali dengan cepat.
                    navController.navigate("detail/$malId") {
                        launchSingleTop = true
                    }
                },
            )
        }

        // Halaman 2: Detail Screen (Menerima parameter ID anime bertipe Integer dari URL)
        composable(
            route = DETAIL_ROUTE,
            arguments = listOf(
                navArgument(MAL_ID_ARGUMENT) { type = NavType.IntType },
            ),
        ) { backStackEntry ->
            // Ambil ID anime dari argumen navigasi (default -1 kalau tidak ditemukan)
            val malId = backStackEntry.arguments?.getInt(MAL_ID_ARGUMENT) ?: -1
            // Siapkan ViewModel Detail dengan menyuntikkan malId dan repository
            val detailViewModel: DetailViewModel = viewModel(
                factory = DetailViewModel.Factory(malId, repository),
            )
            val state = detailViewModel.uiState.collectAsStateWithLifecycle().value

            DetailScreen(
                state = state,
                onBack = navController::navigateUp, // Aksi tombol panah kembali ke halaman Home
                onRetry = detailViewModel::retry,
            )
        }
    }
}


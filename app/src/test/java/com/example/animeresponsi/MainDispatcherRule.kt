package com.example.animeresponsi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * JUnit Rule untuk mengganti [Dispatchers.Main] dengan [StandardTestDispatcher] saat pengujian unit.
 * Diperlukan karena lingkungan JVM murni tidak memiliki Main Looper Android (Android Main Thread).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    /**
     * Dijalankan sebelum setiap metode pengujian dieksekusi.
     * Mengatur Main Dispatcher coroutine ke test dispatcher.
     */
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    /**
     * Dijalankan setelah metode pengujian selesai dieksekusi.
     * Mereset Main Dispatcher kembali ke keadaan semula untuk mengisolasi tes berikutnya.
     */
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}


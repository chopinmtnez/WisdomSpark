package com.albertowisdom.wisdomspark

import android.app.Application
import android.util.Log
import com.albertowisdom.wisdomspark.ads.AdMobManager
import com.albertowisdom.wisdomspark.data.repository.QuoteRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Aplicación principal de WisdomSpark con inicialización de AdMob
 */
@HiltAndroidApp
class WisdomSparkApplication : Application() {

    @Inject
    lateinit var quoteRepository: QuoteRepository
    
    @Inject
    lateinit var adMobManager: AdMobManager

    private val applicationScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Log.d("WisdomSparkApp", "🚀 Iniciando WisdomSpark...")
        
        // Inicializar AdMob
        initializeAdMob()
        
        // Inicializar base de datos
        initializeDatabase()
    }

    /**
     * Inicializar AdMob SDK
     */
    private fun initializeAdMob() {
        try {
            adMobManager.initialize(this)
            Log.d("WisdomSparkApp", "✅ AdMob inicializado exitosamente")
        } catch (e: Exception) {
            Log.e("WisdomSparkApp", "❌ Error inicializando AdMob: ${e.message}")
        }
    }

    /**
     * Inicializar base de datos con Google Sheets sync
     */
    private fun initializeDatabase() {
        applicationScope.launch {
            try {
                quoteRepository.initializeQuotes(forceSync = false)
                Log.d("WisdomSparkApp", "✅ Base de datos inicializada")
            } catch (e: Exception) {
                Log.e("WisdomSparkApp", "❌ Error inicializando base de datos: ${e.message}")
            }
        }
    }
}

package com.albertowisdom.wisdomspark

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.albertowisdom.wisdomspark.ads.AdMobManager
import com.albertowisdom.wisdomspark.data.repository.QuoteRepository
import com.albertowisdom.wisdomspark.utils.NotificationService
import com.albertowisdom.wisdomspark.workers.QuoteSyncWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Aplicación principal de WisdomSpark con inicialización de AdMob
 */
@HiltAndroidApp
class WisdomSparkApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var quoteRepository: QuoteRepository
    
    @Inject
    lateinit var adMobManager: AdMobManager
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    @Inject
    lateinit var notificationService: NotificationService

    private val applicationScope = CoroutineScope(Dispatchers.IO)
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        Log.d("WisdomSparkApp", "🚀 Iniciando WisdomSpark...")
        
        // Inicializar AdMob
        initializeAdMob()
        
        // Inicializar base de datos
        initializeDatabase()
        
        // Configurar sincronización periódica
        setupPeriodicSync()
        
        // Inicializar servicio de notificaciones
        initializeNotifications()
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
     * Inicializar base de datos con sincronización automática
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
    
    /**
     * Configurar sincronización periódica cada 6 horas
     */
    private fun setupPeriodicSync() {
        try {
            val syncWorkRequest = PeriodicWorkRequestBuilder<QuoteSyncWorker>(
                repeatInterval = 6,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            ).build()
            
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "quote_sync_work",
                ExistingPeriodicWorkPolicy.KEEP,
                syncWorkRequest
            )
            
            Log.d("WisdomSparkApp", "✅ Sincronización periódica configurada (cada 6 horas)")
        } catch (e: Exception) {
            Log.e("WisdomSparkApp", "❌ Error configurando sincronización periódica: ${e.message}")
        }
    }
    
    /**
     * Inicializar servicio de notificaciones diarias
     */
    private fun initializeNotifications() {
        try {
            notificationService.initialize()
            Log.d("WisdomSparkApp", "✅ Servicio de notificaciones inicializado")
        } catch (e: Exception) {
            Log.e("WisdomSparkApp", "❌ Error inicializando notificaciones: ${e.message}")
        }
    }
}

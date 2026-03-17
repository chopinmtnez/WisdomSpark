package com.albertowisdom.wisdomspark

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration as WorkConfiguration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.albertowisdom.wisdomspark.ads.AdMobManager
import com.albertowisdom.wisdomspark.data.preferences.UserPreferences
import com.albertowisdom.wisdomspark.security.PlayIntegrityManager
import com.albertowisdom.wisdomspark.data.repository.QuoteRepository
import com.albertowisdom.wisdomspark.utils.LocaleHelper
import com.albertowisdom.wisdomspark.utils.NotificationService
import com.albertowisdom.wisdomspark.workers.QuoteSyncWorker
import com.albertowisdom.wisdomspark.premium.billing.BillingManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Aplicación principal de WisdomSpark con inicialización de AdMob
 */
@HiltAndroidApp
class WisdomSparkApplication : Application(), WorkConfiguration.Provider {

    @Inject
    lateinit var quoteRepository: QuoteRepository
    
    @Inject
    lateinit var adMobManager: AdMobManager
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    @Inject
    lateinit var notificationService: NotificationService

    @Inject
    lateinit var userPreferences: UserPreferences
    
    @Inject
    lateinit var playIntegrityManager: PlayIntegrityManager
    
    @Inject 
    lateinit var workManager: WorkManager
    
    @Inject
    lateinit var billingManager: BillingManager

    private val applicationScope = CoroutineScope(Dispatchers.IO)
    
    override val workManagerConfiguration: WorkConfiguration
        get() = WorkConfiguration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        Log.d("WisdomSparkApp", "🚀 Iniciando WisdomSpark...")
        
        // Limpiar trabajos antiguos de WorkManager
        cleanupOldWorkers()
        
        // Inicializar AdMob
        initializeAdMob()
        
        // Inicializar sistema de billing Premium
        initializeBilling()
        
        // Inicializar base de datos
        initializeDatabase()
        
        // Configurar sincronización periódica (después de un delay para asegurar limpieza)
        applicationScope.launch {
            kotlinx.coroutines.delay(1000) // 1 segundo de delay
            setupPeriodicSync()
        }
        
        // Inicializar servicio de notificaciones (después de un delay)
        applicationScope.launch {
            kotlinx.coroutines.delay(1500) // 1.5 segundos de delay
            initializeNotifications()
            
        }
        
        // Sincronizar idioma de DataStore a SharedPreferences
        syncLanguagePreferences()
        
        // Verificar integridad de la aplicación
        verifyAppIntegrity()
    }

    /**
     * Limpiar trabajos antiguos de WorkManager para evitar conflictos
     * con la nueva configuración de inyección de dependencias
     */
    private fun cleanupOldWorkers() {
        try {
            Log.d("WisdomSparkApp", "🧹 Limpiando trabajos antiguos de WorkManager...")
            
            // Usar una instancia temporal para limpiar trabajos antiguos
            val tempWorkManager = WorkManager.getInstance(this)
            
            // Cancelar todos los trabajos existentes 
            tempWorkManager.cancelAllWork()
            
            // Específicamente cancelar trabajos conocidos por nombre
            tempWorkManager.cancelUniqueWork("daily_wisdom_notifications")
            tempWorkManager.cancelUniqueWork("quote_sync_work")
            tempWorkManager.cancelUniqueWork("quote_sync_work_simple")
            tempWorkManager.cancelUniqueWork("quote_sync_work_production")
            
            // Cancelar por tags
            tempWorkManager.cancelAllWorkByTag("wisdom_daily_notifications")
            tempWorkManager.cancelAllWorkByTag("simple_daily_notifications")
            tempWorkManager.cancelAllWorkByTag("simple_quote_sync")
            tempWorkManager.cancelAllWorkByTag("test_notification")
            tempWorkManager.cancelAllWorkByTag("test_notification_delayed")
            tempWorkManager.cancelAllWorkByTag("test_notification_simple")
            tempWorkManager.cancelAllWorkByTag("test_notification_delayed_simple")
            tempWorkManager.cancelAllWorkByTag("test_production_notification")
            
            // Opcional: también limpiar la base de datos de WorkManager si es posible
            try {
                tempWorkManager.pruneWork()
            } catch (e: Exception) {
                Log.w("WisdomSparkApp", "No se pudo limpiar la base de datos de WorkManager: ${e.message}")
            }
            
            Log.d("WisdomSparkApp", "✅ Trabajos antiguos de WorkManager limpiados")
        } catch (e: Exception) {
            Log.e("WisdomSparkApp", "❌ Error limpiando trabajos de WorkManager: ${e.message}")
        }
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
     * TEMPORALMENTE DESHABILITADO para no interferir con notificaciones
     */
    private fun setupPeriodicSync() {
        try {
            Log.d("WisdomSparkApp", "⚠️ Sincronización periódica temporalmente deshabilitada")
            Log.d("WisdomSparkApp", "ℹ️ QuoteSyncWorker causa errores que afectan notificaciones diarias")
            
            // DESHABILITADO TEMPORALMENTE - El QuoteSyncWorker está causando errores
            // que impiden la ejecución de notificaciones diarias
            /*
            val syncWorkRequest = PeriodicWorkRequestBuilder<QuoteSyncWorker>(
                repeatInterval = 6,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            ).build()
            
            workManager.enqueueUniquePeriodicWork(
                "quote_sync_work_production",
                ExistingPeriodicWorkPolicy.KEEP,
                syncWorkRequest
            )
            */
            
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

    /**
     * Sincronizar idioma de DataStore a SharedPreferences
     */
    private fun syncLanguagePreferences() {
        applicationScope.launch {
            try {
                // Primero verificar si ya hay un idioma guardado en SharedPreferences
                val currentSavedLanguage = LocaleHelper.getCurrentSavedLanguage(this@WisdomSparkApplication)
                if (currentSavedLanguage != null) {
                    Log.d("WisdomSparkApp", "✅ Idioma ya guardado en SharedPreferences: $currentSavedLanguage")
                    // Sincronizar con DataStore para consistencia
                    userPreferences.setAppLanguage(currentSavedLanguage)
                } else {
                    // Si no hay idioma en SharedPreferences, usar el de DataStore
                    val savedLanguage = userPreferences.appLanguage.first()
                    LocaleHelper.saveLanguage(this@WisdomSparkApplication, savedLanguage)
                    Log.d("WisdomSparkApp", "✅ Idioma sincronizado desde DataStore: $savedLanguage")
                }
            } catch (e: Exception) {
                Log.e("WisdomSparkApp", "❌ Error sincronizando idioma: ${e.message}")
                // Si hay error, usar idioma del sistema
                val systemLanguage = LocaleHelper.getSystemLanguage(this@WisdomSparkApplication)
                LocaleHelper.saveLanguage(this@WisdomSparkApplication, systemLanguage)
                try {
                    userPreferences.setAppLanguage(systemLanguage)
                } catch (e2: Exception) {
                    Log.e("WisdomSparkApp", "Error guardando idioma del sistema: ${e2.message}")
                }
            }
        }
    }

    /**
     * Verificar integridad de la aplicación con Play Integrity API
     */
    private fun verifyAppIntegrity() {
        applicationScope.launch {
            try {
                Log.d("WisdomSparkApp", "🔐 Iniciando verificación de integridad...")
                
                // Verificar info de Play Services primero
                val playServicesInfo = playIntegrityManager.getPlayServicesInfo()
                if (!playServicesInfo.isAvailable) {
                    Log.w("WisdomSparkApp", "⚠️ Play Services no disponible: ${playServicesInfo.errorString}")
                }
                
                // Realizar verificación básica de integridad
                val integrityCheck = playIntegrityManager.performBasicIntegrityCheck()
                
                if (integrityCheck) {
                    Log.d("WisdomSparkApp", "✅ Verificación de integridad exitosa")
                    // La app puede funcionar normalmente
                } else {
                    Log.w("WisdomSparkApp", "⚠️ Verificación de integridad falló - funcionalidad limitada")
                    // Implementar lógica para funcionalidad limitada si es necesario
                    handleIntegrityFailure()
                }
            } catch (e: Exception) {
                Log.e("WisdomSparkApp", "❌ Error en verificación de integridad: ${e.message}", e)
                // Manejar error gracefully
                handleIntegrityError(e)
            }
        }
    }
    
    /**
     * Manejar fallo en la verificación de integridad
     */
    private fun handleIntegrityFailure() {
        // Aquí puedes implementar lógica para:
        // - Mostrar advertencias al usuario
        // - Limitar ciertas funcionalidades
        // - Reportar a analytics
        Log.w("WisdomSparkApp", "🚫 Funcionalidad limitada debido a fallo de integridad")
    }
    
    /**
     * Manejar errores en la verificación de integridad
     */
    private fun handleIntegrityError(error: Exception) {
        // Manejo graceful de errores
        Log.e("WisdomSparkApp", "❌ Error en sistema de integridad: ${error.message}")
        // La app continúa funcionando normalmente en caso de errores
    }
    
    /**
     * Inicializar sistema de billing Premium
     */
    private fun initializeBilling() {
        try {
            Log.d("WisdomSparkApp", "💳 Inicializando sistema de billing Premium...")
            
            // El BillingManager se conecta automáticamente cuando se crea
            // No necesitamos hacer nada más aquí, solo asegurarnos de que está disponible
            
            Log.d("WisdomSparkApp", "✅ Sistema de billing inicializado correctamente")
        } catch (e: Exception) {
            Log.e("WisdomSparkApp", "❌ Error inicializando billing: ${e.message}", e)
            // La app continúa funcionando normalmente sin Premium
        }
    }


    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.updateBaseContextLocale(base))
    }
}

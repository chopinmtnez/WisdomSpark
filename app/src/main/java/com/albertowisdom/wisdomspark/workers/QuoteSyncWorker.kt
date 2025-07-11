package com.albertowisdom.wisdomspark.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.albertowisdom.wisdomspark.data.repository.QuoteRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker para sincronización periódica de citas
 */
@HiltWorker
class QuoteSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val quoteRepository: QuoteRepository
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        private const val TAG = "QuoteSyncWorker"
    }
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔄 Iniciando sincronización automática")
            
            // Intentar sincronizar con forzar actualización
            val syncResult = quoteRepository.forceSyncWithGoogleSheets()
            
            Log.d(TAG, "✅ Sincronización automática completada: $syncResult")
            Result.success()
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en sincronización automática: ${e.message}", e)
            // Reintentar en caso de error
            Result.retry()
        }
    }
}
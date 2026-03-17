package com.albertowisdom.wisdomspark.data.managers

import android.util.Log
import com.albertowisdom.wisdomspark.data.preferences.UserPreferences
import com.albertowisdom.wisdomspark.data.remote.repository.SyncResult
import com.albertowisdom.wisdomspark.data.repository.QuoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class LanguageManager @Inject constructor(
    private val quoteRepository: QuoteRepository,
    private val userPreferences: UserPreferences
) {

    companion object {
        private const val TAG = "LanguageManager"
    }

    private val languageChangeMutex = Mutex()
    private val _languageChangeInProgress = MutableStateFlow(false)
    val languageChangeInProgress: StateFlow<Boolean> = _languageChangeInProgress.asStateFlow()
    
    private val _lastSyncResult = MutableStateFlow<SyncResult?>(null)
    val lastSyncResult: StateFlow<SyncResult?> = _lastSyncResult.asStateFlow()
    
    /**
     * Centralized language change coordinator that prevents race conditions
     * and ensures proper sequencing of database operations
     * FIXED: Sync database FIRST, then update preference to avoid race conditions
     */
    suspend fun changeLanguage(newLanguage: String): SyncResult {
        return languageChangeMutex.withLock {
            try {
                Log.d(TAG, "Starting language change to '$newLanguage'")
                _languageChangeInProgress.value = true
                
                // 1. Sync database FIRST to ensure data is ready
                Log.d(TAG, "Starting database sync for language '$newLanguage'")
                val syncResult = quoteRepository.syncDatabasePreservingFavorites(newLanguage)
                
                if (syncResult is SyncResult.Success) {
                    Log.d(TAG, "Database sync successful for '$newLanguage'")
                    
                    // 2. Only update language preference AFTER successful sync
                    userPreferences.setAppLanguage(newLanguage)
                    Log.d(TAG, "Language preference updated to '$newLanguage' after successful sync")
                    
                    // 3. Additional stabilization delay to ensure UI can read new data
                    kotlinx.coroutines.delay(1000)
                    Log.d(TAG, "Additional stabilization delay completed")
                    
                } else {
                    Log.e(TAG, "Database sync failed: ${syncResult}")
                    // Don't update preference if sync failed
                }
                
                _lastSyncResult.value = syncResult
                Log.d(TAG, "Language change completed for '$newLanguage'")
                
                return syncResult
                
            } catch (e: Exception) {
                Log.e(TAG, "Error during language change: ${e.message}")
                val errorResult = SyncResult.Error("Language change failed: ${e.message}")
                _lastSyncResult.value = errorResult
                return errorResult
                
            } finally {
                _languageChangeInProgress.value = false
            }
        }
    }
    
    /**
     * Checks if a language change is currently in progress
     */
    fun isLanguageChangeInProgress(): Boolean {
        return _languageChangeInProgress.value
    }
    
    /**
     * Waits for any ongoing language change to complete
     */
    suspend fun waitForLanguageChangeCompletion() {
        if (_languageChangeInProgress.value) {
            Log.d(TAG, "Waiting for language change to complete...")
            while (_languageChangeInProgress.value) {
                delay(100)
            }
            Log.d(TAG, "Language change completed")
        }
    }
    
    /**
     * Forces a database resync for the current language
     * Useful for troubleshooting or manual refresh
     */
    suspend fun forceDatabaseResync(): SyncResult {
        return languageChangeMutex.withLock {
            try {
                _languageChangeInProgress.value = true
                Log.d(TAG, "Forcing database resync...")
                
                val currentLanguage = userPreferences.getAppLanguage()
                val syncResult = quoteRepository.syncDatabasePreservingFavorites(currentLanguage)
                
                if (syncResult is SyncResult.Success) {
                    delay(2000) // Wait for stabilization
                }
                
                _lastSyncResult.value = syncResult
                return syncResult
                
            } catch (e: Exception) {
                val errorResult = SyncResult.Error("Force resync failed: ${e.message}")
                _lastSyncResult.value = errorResult
                return errorResult
                
            } finally {
                _languageChangeInProgress.value = false
            }
        }
    }
}
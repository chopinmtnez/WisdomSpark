package com.albertowisdom.wisdomspark.security

import android.content.Context
import android.util.Log
import com.google.android.play.core.integrity.IntegrityManager
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import com.google.android.play.core.integrity.IntegrityTokenResponse
import com.google.android.gms.tasks.Task
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.ConnectionResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.io.OutputStreamWriter
import java.io.BufferedReader
import java.io.InputStreamReader
import org.json.JSONObject
import java.security.SecureRandom
import android.util.Base64

/**
 * Gestor centralizado de Play Integrity API para WisdomSpark
 * Verifica la integridad de la aplicación y del dispositivo
 */
@Singleton
class PlayIntegrityManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val integrityManager: IntegrityManager by lazy {
        IntegrityManagerFactory.create(context)
    }

    companion object {
        private const val TAG = "PlayIntegrityManager"
        
        // Cloud Project Number from Google Cloud Console
        private const val CLOUD_PROJECT_NUMBER = 728036649384L
        
        // Backend URL para verificación de tokens
        // CAMBIAR ESTA URL POR TU SERVIDOR REAL
        private const val BACKEND_URL = "https://albertomartinezmartin.com/verify_integrity.php"
        
        // Error codes for Play Integrity
        private const val INTEGRITY_TOKEN_REQUEST_FAILED = -1
        private const val PLAY_SERVICES_NOT_AVAILABLE = -2
        private const val NETWORK_ERROR = -3
        private const val PLAY_STORE_NOT_FOUND = -4
        
        // Nonce for integrity verification (should be unique per request)
        // Must be at least 16 bytes before base64 encoding
        private fun generateNonce(): ByteArray {
            // Generar 24 bytes aleatorios (más de los 16 requeridos)
            val randomBytes = ByteArray(24)
            SecureRandom().nextBytes(randomBytes)
            
            // Añadir timestamp para uniqueness adicional
            val timestamp = System.currentTimeMillis()
            val timestampBytes = timestamp.toString().toByteArray()
            
            // Combinar random bytes con timestamp para crear un nonce único
            return randomBytes + timestampBytes.take(8).toByteArray() // Mantener tamaño manejable
        }
    }

    /**
     * Realiza verificación de integridad de la aplicación
     */
    suspend fun verifyIntegrity(): IntegrityResult {
        return try {
            Log.d(TAG, "🔐 Iniciando verificación de integridad...")
            
            val nonceBytes = generateNonce()
            val nonceBase64 = Base64.encodeToString(nonceBytes, Base64.URL_SAFE or Base64.NO_WRAP)
            Log.d(TAG, "🔑 Nonce generado: ${nonceBytes.size} bytes, base64=${nonceBase64.take(20)}...")
            
            val integrityTokenRequest = IntegrityTokenRequest.builder()
                .setCloudProjectNumber(CLOUD_PROJECT_NUMBER)
                .setNonce(nonceBase64)
                .build()

            val response: IntegrityTokenResponse = integrityManager
                .requestIntegrityToken(integrityTokenRequest)
                .awaitTask()

            val token = response.token()
            
            if (token.isNotEmpty()) {
                Log.d(TAG, "✅ Token de integridad obtenido exitosamente")
                Log.d(TAG, "🎫 Token length: ${token.length}")
                Log.d(TAG, "🎫 Token preview: ${token.take(100)}...")
                
                // Analizar estructura del token
                val tokenParts = token.split(".")
                Log.d(TAG, "🎫 Token parts count: ${tokenParts.size}")
                for (i in tokenParts.indices) {
                    Log.d(TAG, "🎫 Part $i length: ${tokenParts[i].length}")
                    Log.d(TAG, "🎫 Part $i preview: ${tokenParts[i].take(30)}...")
                }
                
                // En un entorno de producción, deberías enviar este token a tu servidor
                // para verificarlo con la API de Google Play Integrity
                val verificationResult = verifyTokenOnServer(token, nonceBase64)
                
                IntegrityResult.Success(
                    token = token,
                    nonce = nonceBase64,
                    serverVerification = verificationResult
                )
            } else {
                Log.w(TAG, "⚠️ Token de integridad vacío")
                IntegrityResult.Error(
                    code = INTEGRITY_TOKEN_REQUEST_FAILED,
                    message = "Token de integridad vacío"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en verificación de integridad: ${e.message}", e)
            
            val errorCode = when {
                e.message?.contains("PLAY_SERVICES_NOT_AVAILABLE") == true -> 
                    PLAY_SERVICES_NOT_AVAILABLE
                e.message?.contains("NETWORK_ERROR") == true -> 
                    NETWORK_ERROR
                else -> INTEGRITY_TOKEN_REQUEST_FAILED
            }
            
            IntegrityResult.Error(
                code = errorCode,
                message = e.message ?: "Error desconocido en verificación de integridad"
            )
        }
    }

    /**
     * Verifica si el dispositivo cumple con los estándares de integridad básicos
     */
    suspend fun performBasicIntegrityCheck(): Boolean {
        return try {
            val result = verifyIntegrity()
            when (result) {
                is IntegrityResult.Success -> {
                    Log.d(TAG, "✅ Verificación básica de integridad exitosa")
                    true
                }
                is IntegrityResult.Error -> {
                    Log.w(TAG, "⚠️ Verificación básica de integridad falló: ${result.message}")
                    // En algunos casos, podrías permitir el uso de la app con funcionalidad limitada
                    shouldAllowLimitedFunctionality(result.code)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en verificación básica: ${e.message}", e)
            false
        }
    }

    /**
     * Determina si la app debe funcionar con limitaciones según el error de integridad
     */
    private fun shouldAllowLimitedFunctionality(errorCode: Int): Boolean {
        return when (errorCode) {
            NETWORK_ERROR -> {
                // Permitir uso offline si no hay conexión
                Log.d(TAG, "🔄 Permitiendo uso offline debido a error de red")
                true
            }
            PLAY_SERVICES_NOT_AVAILABLE -> {
                // Permitir uso básico si Play Services no está disponible
                Log.d(TAG, "🔄 Permitiendo uso básico sin Play Services")
                true
            }
            PLAY_STORE_NOT_FOUND -> {
                // Dispositivo sin Play Store (ej: emulador)
                Log.d(TAG, "🔄 Dispositivo sin Play Store detectado")
                true
            }
            else -> {
                // Para otros errores, no permitir funcionalidad
                Log.w(TAG, "🚫 Integridad comprometida, bloqueando funcionalidad")
                false
            }
        }
    }

    /**
     * Verifica el token en el servidor backend PHP
     */
    private suspend fun verifyTokenOnServer(token: String, nonce: String): ServerVerificationResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🌐 Verificando token en servidor backend...")
                
                // Crear JSON request
                val jsonRequest = JSONObject().apply {
                    put("token", token)
                    put("nonce", nonce)
                    put("package_name", context.packageName)
                    put("timestamp", System.currentTimeMillis())
                }
                
                Log.d(TAG, "📤 Enviando nonce al servidor: $nonce")
                Log.d(TAG, "📤 JSON request: ${jsonRequest.toString()}")
                
                // Hacer llamada HTTP
                val response = makeHttpRequest(BACKEND_URL, jsonRequest.toString())
                Log.d(TAG, "📥 Respuesta del servidor: $response")
                
                // Parsear respuesta
                val jsonResponse = JSONObject(response)
                val isValid = jsonResponse.optBoolean("verified", false)
                
                if (isValid) {
                    Log.d(TAG, "✅ Token verificado exitosamente en servidor")
                    ServerVerificationResult(
                        isValid = true,
                        deviceIntegrity = jsonResponse.optString("device_integrity", "VERIFIED"),
                        appIntegrity = jsonResponse.optString("app_integrity", "VERIFIED"),
                        accountDetails = jsonResponse.optString("account_details", "VERIFIED")
                    )
                } else {
                    Log.w(TAG, "⚠️ Token rechazado por servidor: ${jsonResponse.optString("error", "Unknown error")}")
                    ServerVerificationResult(
                        isValid = false,
                        deviceIntegrity = jsonResponse.optString("device_integrity", "FAILED"),
                        appIntegrity = jsonResponse.optString("app_integrity", "FAILED"),
                        accountDetails = jsonResponse.optString("account_details", "FAILED")
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error conectando con servidor: ${e.message}")
                // Fallback a verificación local si el servidor falla
                performLocalTokenValidation(token, nonce)
            }
        }
    }
    
    /**
     * Hacer petición HTTP al backend PHP
     */
    private suspend fun makeHttpRequest(url: String, jsonData: String): String {
        return withContext(Dispatchers.IO) {
            val connection = URL(url).openConnection() as HttpURLConnection
            try {
                connection.apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("User-Agent", "WisdomSpark/1.0")
                    doOutput = true
                    connectTimeout = 10000
                    readTimeout = 10000
                }
                
                // Enviar datos
                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(jsonData)
                    writer.flush()
                }
                
                // Leer respuesta
                val responseCode = connection.responseCode
                val inputStream = if (responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }
                
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            } finally {
                connection.disconnect()
            }
        }
    }
    
    /**
     * Fallback: Validación local básica si el servidor no responde
     */
    private suspend fun performLocalTokenValidation(token: String, nonce: String): ServerVerificationResult {
        return try {
            Log.d(TAG, "🔄 Usando verificación local de fallback...")
            
            // Verificaciones básicas del token
            val isTokenValid = token.isNotEmpty() && token.length > 100
            val isNonceValid = nonce.isNotEmpty()
            
            Log.d(TAG, "Token válido: $isTokenValid, Nonce válido: $isNonceValid")
            
            if (isTokenValid && isNonceValid) {
                ServerVerificationResult(
                    isValid = true,
                    deviceIntegrity = "LOCAL_VERIFICATION",
                    appIntegrity = "LOCAL_VERIFICATION",
                    accountDetails = "LOCAL_VERIFICATION"
                )
            } else {
                ServerVerificationResult(
                    isValid = false,
                    deviceIntegrity = "LOCAL_FAILED",
                    appIntegrity = "LOCAL_FAILED",
                    accountDetails = "LOCAL_FAILED"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en validación local: ${e.message}")
            ServerVerificationResult(
                isValid = false,
                deviceIntegrity = "ERROR",
                appIntegrity = "ERROR",
                accountDetails = "ERROR"
            )
        }
    }

    /**
     * Obtiene información sobre el estado de Play Services
     */
    fun getPlayServicesInfo(): PlayServicesInfo {
        return try {
            // Verificar disponibilidad de Play Services
            val googleApiAvailability = GoogleApiAvailability.getInstance()
            val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
            
            PlayServicesInfo(
                isAvailable = resultCode == ConnectionResult.SUCCESS,
                resultCode = resultCode,
                errorString = googleApiAvailability.getErrorString(resultCode)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo info de Play Services: ${e.message}")
            PlayServicesInfo(
                isAvailable = false,
                resultCode = -1,
                errorString = e.message ?: "Error desconocido"
            )
        }
    }
}

/**
 * Extension function to convert Google Play Services Task to suspend function
 */
private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
    addOnCompleteListener { task ->
        if (task.exception == null) {
            if (task.isCanceled) {
                cont.cancel()
            } else {
                cont.resume(task.result)
            }
        } else {
            cont.resumeWithException(task.exception!!)
        }
    }
}

/**
 * Resultado de la verificación de integridad
 */
sealed class IntegrityResult {
    data class Success(
        val token: String,
        val nonce: String,
        val serverVerification: ServerVerificationResult
    ) : IntegrityResult()

    data class Error(
        val code: Int,
        val message: String
    ) : IntegrityResult()
}

/**
 * Resultado de la verificación en el servidor
 */
data class ServerVerificationResult(
    val isValid: Boolean,
    val deviceIntegrity: String,
    val appIntegrity: String,
    val accountDetails: String
)

/**
 * Información sobre Play Services
 */
data class PlayServicesInfo(
    val isAvailable: Boolean,
    val resultCode: Int,
    val errorString: String
)
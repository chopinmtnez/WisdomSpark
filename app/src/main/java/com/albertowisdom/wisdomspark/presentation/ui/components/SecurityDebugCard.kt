package com.albertowisdom.wisdomspark.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.albertowisdom.wisdomspark.presentation.ui.screens.settings.SecurityStatusViewModel
import com.albertowisdom.wisdomspark.security.IntegrityResult
import com.albertowisdom.wisdomspark.security.PlayServicesInfo

/**
 * Componente de debugging para Play Integrity API
 * Solo visible en modo debug
 */
@Composable
fun SecurityDebugCard(
    modifier: Modifier = Modifier,
    viewModel: SecurityStatusViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    
    // Solo mostrar en debug builds
    val isDebugBuild = remember {
        try {
            (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        } catch (e: Exception) {
            false
        }
    }
    
    if (!isDebugBuild) {
        return
    }
    
    val uiState by viewModel.uiState.collectAsState()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "🔍 Play Integrity Debug",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                )
            }
            
            Divider(color = MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
            
            // Botones de prueba
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.checkSecurity() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("🔄 Test Full", fontSize = 12.sp)
                }
                
                Button(
                    onClick = { viewModel.checkBasicIntegrity() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("⚡ Test Basic", fontSize = 12.sp)
                }
            }
            
            // Estado actual
            when (uiState) {
                is SecurityStatusViewModel.SecurityUiState.Loading -> {
                    DebugInfoRow("Estado", "🔄 Verificando...")
                }
                is SecurityStatusViewModel.SecurityUiState.Success -> {
                    DebugSuccessInfo(
                        integrityResult = (uiState as SecurityStatusViewModel.SecurityUiState.Success).integrityResult,
                        playServicesInfo = (uiState as SecurityStatusViewModel.SecurityUiState.Success).playServicesInfo
                    )
                }
                is SecurityStatusViewModel.SecurityUiState.Error -> {
                    DebugErrorInfo(error = (uiState as SecurityStatusViewModel.SecurityUiState.Error).error)
                }
            }
            
            // Información del dispositivo
            DebugDeviceInfo()
        }
    }
}

@Composable
private fun DebugSuccessInfo(
    integrityResult: IntegrityResult?,
    playServicesInfo: PlayServicesInfo
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "✅ SUCCESS STATE",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
        
        // Play Services Info
        DebugInfoRow("Play Services", if (playServicesInfo.isAvailable) "✅ Available" else "❌ Not Available")
        DebugInfoRow("PS Result Code", playServicesInfo.resultCode.toString())
        if (!playServicesInfo.isAvailable) {
            DebugInfoRow("PS Error", playServicesInfo.errorString)
        }
        
        // Integrity Result Info
        when (integrityResult) {
            is IntegrityResult.Success -> {
                DebugInfoRow("Integrity", "✅ Success")
                DebugInfoRow("Token Length", integrityResult.token.length.toString())
                DebugInfoRow("Nonce", integrityResult.nonce.take(20) + "...")
                DebugInfoRow("Device Integrity", integrityResult.serverVerification.deviceIntegrity)
                DebugInfoRow("App Integrity", integrityResult.serverVerification.appIntegrity)
                DebugInfoRow("Account Details", integrityResult.serverVerification.accountDetails)
                DebugInfoRow("Server Valid", integrityResult.serverVerification.isValid.toString())
            }
            is IntegrityResult.Error -> {
                DebugInfoRow("Integrity", "❌ Error")
                DebugInfoRow("Error Code", integrityResult.code.toString())
                DebugInfoRow("Error Message", integrityResult.message)
            }
            null -> {
                DebugInfoRow("Integrity", "⚠️ Null Result")
            }
        }
    }
}

@Composable
private fun DebugErrorInfo(error: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "❌ ERROR STATE",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        )
        
        Text(
            text = error,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.error
            )
        )
    }
}

@Composable
private fun DebugDeviceInfo() {
    val context = LocalContext.current
    
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "📱 DEVICE INFO",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        
        DebugInfoRow("Package", context.packageName)
        DebugInfoRow("Device Model", android.os.Build.MODEL ?: "Unknown")
        DebugInfoRow("Android Version", android.os.Build.VERSION.RELEASE ?: "Unknown")
        DebugInfoRow("API Level", android.os.Build.VERSION.SDK_INT.toString())
        
        // Información de Google Play Services simplificada
        val gmsStatus = remember { 
            try {
                val availability = com.google.android.gms.common.GoogleApiAvailability.getInstance()
                val result = availability.isGooglePlayServicesAvailable(context)
                result == com.google.android.gms.common.ConnectionResult.SUCCESS
            } catch (e: Exception) {
                false
            }
        }
        
        DebugInfoRow("GMS Available", gmsStatus)
    }
}

@Composable
private fun DebugInfoRow(
    label: String,
    value: Any,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            maxLines = 2
        )
    }
}
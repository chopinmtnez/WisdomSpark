package com.albertowisdom.wisdomspark.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.albertowisdom.wisdomspark.premium.debug.PremiumDebugHelper
import com.albertowisdom.wisdomspark.premium.billing.BillingManager
import com.albertowisdom.wisdomspark.premium.manager.PremiumFeatureManager
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Tarjeta de debug para funcionalidades Premium
 * Solo visible en builds de desarrollo
 */
@Composable
fun PremiumDebugCard(
    modifier: Modifier = Modifier,
    billingManager: BillingManager = hiltViewModel<PremiumDebugViewModel>().billingManager
) {
    val context = LocalContext.current
    
    // Estados locales
    var isTestingMode by remember { mutableStateOf(PremiumDebugHelper.isTestingModeEnabled(context)) }
    var isPremium by remember { mutableStateOf(billingManager.isPremium()) }
    val isBillingConnected by billingManager.isConnected.collectAsState()
    
    // Actualizar estado premium cuando cambien las condiciones
    LaunchedEffect(isTestingMode, isBillingConnected) {
        isPremium = billingManager.isPremium()
    }
    
    // Solo mostrar en builds de debug o si el modo testing está activo
    if (!isTestingMode && !isDebugBuild()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
                    MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                
                Column {
                    Text(
                        text = "🧪 DEBUG: Sistema Premium",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Solo para desarrollo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Estado actual
            InfoRow(
                label = "Premium Activo",
                value = if (isPremium) "✅ SÍ" else "❌ NO",
                valueColor = if (isPremium) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.error
            )
            
            InfoRow(
                label = "Modo Testing",
                value = if (isTestingMode) "🧪 ACTIVO" else "⚙️ DESACTIVADO",
                valueColor = if (isTestingMode) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurface
            )
            
            InfoRow(
                label = "Billing Conectado",
                value = if (isBillingConnected) "🔗 SÍ" else "❌ NO",
                valueColor = if (isBillingConnected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.error
            )
            
            InfoRow(
                label = "Características",
                value = if (isPremium) "8 disponibles" else "0 disponibles",
                valueColor = MaterialTheme.colorScheme.onSurface
            )

            // Botón para alternar modo testing
            Button(
                onClick = {
                    val newMode = PremiumDebugHelper.toggleTestingMode(context)
                    isTestingMode = newMode
                    isPremium = billingManager.isPremium()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTestingMode) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (isTestingMode) 
                        Icons.Default.Stop 
                    else 
                        Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isTestingMode) 
                        "Desactivar Testing" 
                    else 
                        "Activar Testing Premium",
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Warning
            if (isTestingMode) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "⚠️ El modo testing simula Premium sin compras reales",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

/**
 * Verificar si es un build de debug
 */
private fun isDebugBuild(): Boolean {
    return try {
        android.os.Build.VERSION.SDK_INT >= 33 // Simplificado para el ejemplo
    } catch (e: Exception) {
        false
    }
}

/**
 * ViewModel simplificado para acceso a dependencias
 */
@HiltViewModel
class PremiumDebugViewModel @Inject constructor(
    val billingManager: BillingManager
) : ViewModel()
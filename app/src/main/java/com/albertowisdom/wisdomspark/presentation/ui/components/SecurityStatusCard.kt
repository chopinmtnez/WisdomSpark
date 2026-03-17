package com.albertowisdom.wisdomspark.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.albertowisdom.wisdomspark.R
import com.albertowisdom.wisdomspark.presentation.ui.screens.settings.SecurityStatusViewModel
import com.albertowisdom.wisdomspark.security.IntegrityResult
import com.albertowisdom.wisdomspark.security.PlayServicesInfo

/**
 * Componente para mostrar el estado de seguridad e integridad de la aplicación
 */
@Composable
fun SecurityStatusCard(
    modifier: Modifier = Modifier,
    viewModel: SecurityStatusViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Text(
                    text = stringResource(R.string.security_status),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            when (uiState) {
                is SecurityStatusViewModel.SecurityUiState.Loading -> {
                    SecurityStatusLoading()
                }
                is SecurityStatusViewModel.SecurityUiState.Success -> {
                    SecurityStatusContent(
                        integrityResult = (uiState as SecurityStatusViewModel.SecurityUiState.Success).integrityResult,
                        playServicesInfo = (uiState as SecurityStatusViewModel.SecurityUiState.Success).playServicesInfo
                    )
                }
                is SecurityStatusViewModel.SecurityUiState.Error -> {
                    SecurityStatusError(
                        error = (uiState as SecurityStatusViewModel.SecurityUiState.Error).error,
                        onRetry = { viewModel.checkSecurity() }
                    )
                }
            }

            // Botón para verificar manualmente
            TextButton(
                onClick = { viewModel.checkSecurity() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.verify_security))
            }
        }
    }
}

@Composable
private fun SecurityStatusLoading() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp
        )
        Text(
            text = stringResource(R.string.verifying_security),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SecurityStatusContent(
    integrityResult: IntegrityResult?,
    playServicesInfo: PlayServicesInfo
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Play Services Status
        SecurityStatusItem(
            label = stringResource(R.string.play_services),
            status = if (playServicesInfo.isAvailable) {
                stringResource(R.string.available)
            } else {
                stringResource(R.string.not_available)
            },
            isPositive = playServicesInfo.isAvailable,
            icon = if (playServicesInfo.isAvailable) Icons.Default.CheckCircle else Icons.Default.Error
        )

        // App Integrity Status
        when (integrityResult) {
            is IntegrityResult.Success -> {
                SecurityStatusItem(
                    label = stringResource(R.string.app_integrity),
                    status = stringResource(R.string.verified),
                    isPositive = true,
                    icon = Icons.Default.VerifiedUser
                )
                
                SecurityStatusItem(
                    label = stringResource(R.string.device_integrity),
                    status = integrityResult.serverVerification.deviceIntegrity,
                    isPositive = integrityResult.serverVerification.deviceIntegrity.contains("VERIFIED") ||
                                integrityResult.serverVerification.deviceIntegrity.contains("JWE_ENCRYPTED_VALID") ||
                                integrityResult.serverVerification.deviceIntegrity.contains("VALID"),
                    icon = Icons.Default.Shield
                )
            }
            is IntegrityResult.Error -> {
                SecurityStatusItem(
                    label = stringResource(R.string.app_integrity),
                    status = stringResource(R.string.verification_failed),
                    isPositive = false,
                    icon = Icons.Default.Warning
                )
            }
            null -> {
                SecurityStatusItem(
                    label = stringResource(R.string.app_integrity),
                    status = stringResource(R.string.not_verified),
                    isPositive = false,
                    icon = Icons.AutoMirrored.Filled.Help
                )
            }
        }
    }
}

@Composable
private fun SecurityStatusError(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = stringResource(R.string.security_check_failed),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
        
        Text(
            text = error,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SecurityStatusItem(
    label: String,
    status: String,
    isPositive: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isPositive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                modifier = Modifier.size(16.dp)
            )
            
            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = if (isPositive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }
    }
}
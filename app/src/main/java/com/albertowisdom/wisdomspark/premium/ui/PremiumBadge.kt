package com.albertowisdom.wisdomspark.premium.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Badge que muestra el estado Premium del usuario
 */
@Composable
fun PremiumBadge(
    modifier: Modifier = Modifier,
    viewModel: PremiumViewModel = hiltViewModel(),
    size: PremiumBadgeSize = PremiumBadgeSize.Medium
) {
    val uiState by viewModel.uiState.collectAsState()
    
    if (uiState.isPremium) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(size.cornerRadius))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFD700),
                            Color(0xFFFFA500)
                        )
                    )
                )
                .padding(horizontal = size.horizontalPadding, vertical = size.verticalPadding),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(size.iconSpacing)
            ) {
                Text(
                    text = "👑",
                    fontSize = size.iconSize
                )
                
                if (size.showText) {
                    Text(
                        text = "Premium",
                        fontSize = size.textSize,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Badge Premium con texto personalizable
 */
@Composable
fun PremiumBadgeWithText(
    text: String,
    modifier: Modifier = Modifier,
    viewModel: PremiumViewModel = hiltViewModel(),
    size: PremiumBadgeSize = PremiumBadgeSize.Medium
) {
    val uiState by viewModel.uiState.collectAsState()
    
    if (uiState.isPremium) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(size.cornerRadius))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFD700),
                            Color(0xFFFFA500)
                        )
                    )
                )
                .padding(horizontal = size.horizontalPadding, vertical = size.verticalPadding),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(size.iconSpacing)
            ) {
                Text(
                    text = "👑",
                    fontSize = size.iconSize
                )
                
                Text(
                    text = text,
                    fontSize = size.textSize,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Indicador simple de Premium (solo corona)
 */
@Composable
fun PremiumIcon(
    modifier: Modifier = Modifier,
    viewModel: PremiumViewModel = hiltViewModel(),
    size: Float = 16f
) {
    val uiState by viewModel.uiState.collectAsState()
    
    if (uiState.isPremium) {
        Text(
            text = "👑",
            fontSize = size.sp,
            modifier = modifier
        )
    }
}

/**
 * Botón para navegar a Premium (solo visible si no es Premium)
 */
@Composable
fun PremiumUpgradeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PremiumViewModel = hiltViewModel(),
    text: String = "Upgrade a Premium"
) {
    val uiState by viewModel.uiState.collectAsState()
    
    if (!uiState.isPremium) {
        Button(
            onClick = onClick,
            modifier = modifier,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFD700),
                contentColor = Color.Black
            )
        ) {
            Text(
                text = "👑",
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Card que envuelve contenido Premium
 */
@Composable
fun PremiumFeatureCard(
    title: String,
    description: String,
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PremiumViewModel = hiltViewModel(),
    content: @Composable (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (uiState.isPremium) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = if (uiState.isPremium) null else 
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (uiState.isPremium) {
                    PremiumIcon()
                } else {
                    OutlinedButton(
                        onClick = onUpgradeClick,
                        modifier = Modifier.size(40.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("👑", fontSize = 16.sp)
                    }
                }
            }
            
            if (uiState.isPremium && content != null) {
                content()
            } else if (!uiState.isPremium) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Disponible con Premium",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Tamaños disponibles para el badge Premium
 */
enum class PremiumBadgeSize(
    val iconSize: androidx.compose.ui.unit.TextUnit,
    val textSize: androidx.compose.ui.unit.TextUnit,
    val horizontalPadding: androidx.compose.ui.unit.Dp,
    val verticalPadding: androidx.compose.ui.unit.Dp,
    val cornerRadius: androidx.compose.ui.unit.Dp,
    val iconSpacing: androidx.compose.ui.unit.Dp,
    val showText: Boolean
) {
    Small(
        iconSize = 12.sp,
        textSize = 10.sp,
        horizontalPadding = 6.dp,
        verticalPadding = 2.dp,
        cornerRadius = 4.dp,
        iconSpacing = 2.dp,
        showText = false
    ),
    Medium(
        iconSize = 16.sp,
        textSize = 12.sp,
        horizontalPadding = 8.dp,
        verticalPadding = 4.dp,
        cornerRadius = 6.dp,
        iconSpacing = 4.dp,
        showText = true
    ),
    Large(
        iconSize = 20.sp,
        textSize = 14.sp,
        horizontalPadding = 12.dp,
        verticalPadding = 6.dp,
        cornerRadius = 8.dp,
        iconSpacing = 6.dp,
        showText = true
    )
}
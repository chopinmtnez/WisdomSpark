package com.albertowisdom.wisdomspark.presentation.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.albertowisdom.wisdomspark.ads.AdMobManager
import com.albertowisdom.wisdomspark.presentation.ui.theme.*

/**
 * Versión simplificada del HomeScreen para debug
 */
@Composable
fun SimpleHomeScreen(
    adMobManager: AdMobManager,
    modifier: Modifier = Modifier
) {
    val backgroundGradient = Brush.linearGradient(
        colors = listOf(WisdomPearl, WisdomBeige, WisdomChampagne),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Text(
                text = "WisdomSpark ✨",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = WisdomCharcoal
                ),
                textAlign = TextAlign.Center
            )
            
            // Simple quote card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.9f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "\"El único modo de hacer un gran trabajo es amar lo que haces.\"",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            color = WisdomCharcoal
                        ),
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = "— Steve Jobs",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = WisdomTaupe,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = WisdomGold.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "🔥 Motivación",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = WisdomCharcoal,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { /* TODO */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WisdomGold
                            )
                        ) {
                            Text("❤️ Favorito")
                        }
                        
                        OutlinedButton(
                            onClick = { /* TODO */ },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = WisdomGold
                            )
                        ) {
                            Text("📤 Compartir")
                        }
                    }
                }
            }
            
            // Status debug
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = WisdomInfo.copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = "🐛 Modo Debug - SimpleHomeScreen funcionando correctamente",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = WisdomInfo,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

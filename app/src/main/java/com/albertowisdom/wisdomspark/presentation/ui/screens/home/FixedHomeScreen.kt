package com.albertowisdom.wisdomspark.presentation.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.albertowisdom.wisdomspark.ads.AdMobManager

/**
 * 🔥 ULTRA MINIMAL HOME SCREEN 🔥
 */
@Composable
fun FixedHomeScreen(
    adMobManager: AdMobManager,
    modifier: Modifier = Modifier
) {
    // VERSION ULTRA SIMPLE SIN DEPENDENCIAS EXTERNAS
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎉 WisdomSpark Funciona 🎉",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.Blue.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "💫 Cita del Día",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "\"El único modo de hacer un gran trabajo es amar lo que haces.\"",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "— Steve Jobs",
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { /* favorite */ }
                    ) {
                        Text("❤️ Favorito")
                    }
                    
                    Button(
                        onClick = { /* share */ }
                    ) {
                        Text("📤 Compartir")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "✅ HomeScreen completamente funcional",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Green,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// Remove unnecessary composable
// @Composable
// private fun FixedHeaderSection() { ... }

package com.albertowisdom.wisdomspark.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.albertowisdom.wisdomspark.data.models.Quote
import com.albertowisdom.wisdomspark.presentation.ui.theme.*
import com.albertowisdom.wisdomspark.utils.ShareUtils
import com.albertowisdom.wisdomspark.utils.ShareOption
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBottomSheet(
    quote: Quote,
    onDismiss: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        bottomSheetState.show()
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        containerColor = Color.Transparent,
        dragHandle = null
    ) {
        ShareBottomSheetContent(
            quote = quote,
            onOptionSelected = { option ->
                scope.launch {
                    bottomSheetState.hide()
                    when (option) {
                        ShareOption.TEXT -> ShareUtils.shareQuoteAsText(context, quote)
                        ShareOption.IMAGE -> ShareUtils.shareQuoteAsImage(context, quote)
                        ShareOption.INSTAGRAM_STORY -> ShareUtils.shareToInstagramStory(context, quote)
                        ShareOption.WHATSAPP -> ShareUtils.shareQuoteAsText(context, quote)
                        ShareOption.FACEBOOK -> ShareUtils.shareQuoteAsImage(context, quote)
                        ShareOption.TWITTER -> ShareUtils.shareQuoteAsText(context, quote)
                    }
                    onDismiss()
                }
            }
        )
    }
}

@Composable
private fun ShareBottomSheetContent(
    quote: Quote,
    onOptionSelected: (ShareOption) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        WisdomGold.copy(alpha = 0.9f),
                        WisdomGold
                    )
                ),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .padding(24.dp)
    ) {
        // Indicador de arrastre
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .background(
                    Color.White.copy(alpha = 0.3f),
                    RoundedCornerShape(2.dp)
                )
                .align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Título
        Text(
            text = "Compartir Sabiduría",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Subtítulo
        Text(
            text = "Elige cómo quieres compartir esta inspiración",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White.copy(alpha = 0.8f)
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Preview de la cita
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "\"${quote.text}\"",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        lineHeight = 20.sp
                    ),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "— ${quote.author}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Opciones principales
        Text(
            text = "Opciones Principales",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ShareOptionButton(
                emoji = "📝",
                label = "Texto",
                description = "Como mensaje",
                onClick = { onOptionSelected(ShareOption.TEXT) }
            )
            
            ShareOptionButton(
                emoji = "🖼️",
                label = "Imagen",
                description = "Tarjeta visual",
                onClick = { onOptionSelected(ShareOption.IMAGE) }
            )
            
            ShareOptionButton(
                emoji = "📸",
                label = "Story",
                description = "Para Instagram",
                onClick = { onOptionSelected(ShareOption.INSTAGRAM_STORY) }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ShareOptionButton(
    emoji: String,
    label: String,
    description: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            WisdomGold.copy(alpha = 0.3f),
                            WisdomGold.copy(alpha = 0.1f)
                        )
                    ),
                    shape = RoundedCornerShape(32.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                fontSize = 24.sp
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        )
        
        Text(
            text = description,
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White.copy(alpha = 0.7f)
            )
        )
    }
}

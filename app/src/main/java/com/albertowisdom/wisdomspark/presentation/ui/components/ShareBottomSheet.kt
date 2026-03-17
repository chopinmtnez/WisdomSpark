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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.albertowisdom.wisdomspark.R
import com.albertowisdom.wisdomspark.data.models.Quote
import com.albertowisdom.wisdomspark.presentation.ui.theme.*
import com.albertowisdom.wisdomspark.utils.ShareUtils
import com.albertowisdom.wisdomspark.utils.ShareOption
import com.albertowisdom.wisdomspark.utils.SharePlatform
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
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
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
                        ShareOption.WHATSAPP -> ShareUtils.shareToSpecificPlatform(context, quote, SharePlatform.WHATSAPP)
                        ShareOption.FACEBOOK -> ShareUtils.shareQuoteAsImage(context, quote)
                        ShareOption.TWITTER -> ShareUtils.shareToSpecificPlatform(context, quote, SharePlatform.TWITTER)
                        ShareOption.TIKTOK -> ShareUtils.shareToSpecificPlatform(context, quote, SharePlatform.TIKTOK)
                        ShareOption.LINKEDIN -> ShareUtils.shareToSpecificPlatform(context, quote, SharePlatform.LINKEDIN)
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
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                        MaterialTheme.colorScheme.primary
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
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    RoundedCornerShape(2.dp)
                )
                .align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Título
        Text(
            text = stringResource(R.string.share_wisdom_title),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtítulo
        Text(
            text = stringResource(R.string.share_wisdom_subtitle),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Preview de la cita
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "\"${quote.text}\"",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    ),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "— ${quote.author}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Opciones principales
        Text(
            text = stringResource(R.string.share_main_options),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ShareOptionButton(
                emoji = "📝",
                label = stringResource(R.string.share_option_text),
                description = stringResource(R.string.share_option_text_desc),
                onClick = { onOptionSelected(ShareOption.TEXT) }
            )

            ShareOptionButton(
                emoji = "🖼️",
                label = stringResource(R.string.share_option_image),
                description = stringResource(R.string.share_option_image_desc),
                onClick = { onOptionSelected(ShareOption.IMAGE) }
            )

            ShareOptionButton(
                emoji = "📸",
                label = stringResource(R.string.share_option_story),
                description = stringResource(R.string.share_option_story_desc),
                onClick = { onOptionSelected(ShareOption.INSTAGRAM_STORY) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Redes sociales específicas
        Text(
            text = stringResource(R.string.share_social_networks),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ShareOptionButton(
                emoji = "🎵",
                label = "TikTok",
                description = stringResource(R.string.share_option_tiktok_desc),
                onClick = { onOptionSelected(ShareOption.TIKTOK) }
            )

            ShareOptionButton(
                emoji = "💼",
                label = "LinkedIn",
                description = stringResource(R.string.share_option_linkedin_desc),
                onClick = { onOptionSelected(ShareOption.LINKEDIN) }
            )

            ShareOptionButton(
                emoji = "💬",
                label = "WhatsApp",
                description = stringResource(R.string.share_option_whatsapp_desc),
                onClick = { onOptionSelected(ShareOption.WHATSAPP) }
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
    val accessibilityLabel = "$label: $description"
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .semantics { contentDescription = accessibilityLabel }
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
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
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        
        Text(
            text = description,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        )
    }
}

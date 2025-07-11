package com.albertowisdom.wisdomspark.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.albertowisdom.wisdomspark.data.models.Quote
import com.albertowisdom.wisdomspark.presentation.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Colecciones de favoritos inteligentes - permite crear y gestionar colecciones temáticas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesCollectionSheet(
    quote: Quote,
    onDismiss: () -> Unit,
    onCollectionSelected: (String) -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Colecciones predefinidas inteligentes
    val smartCollections = listOf(
        SmartCollection(
            name = "Motivación Matutina",
            emoji = "🌅",
            description = "Para empezar el día con energía",
            color = MaterialTheme.colorScheme.primary
        ),
        SmartCollection(
            name = "Reflexiones Nocturnas",
            emoji = "🌙",
            description = "Para reflexionar antes de dormir",
            color = MaterialTheme.colorScheme.secondary
        ),
        SmartCollection(
            name = "Impulso Laboral",
            emoji = "💼",
            description = "Para momentos de trabajo intenso",
            color = MaterialTheme.colorScheme.tertiary
        ),
        SmartCollection(
            name = "Superación Personal",
            emoji = "🚀",
            description = "Para crecer como persona",
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        ),
        SmartCollection(
            name = "Momentos Difíciles",
            emoji = "💪",
            description = "Para superar obstáculos",
            color = MaterialTheme.colorScheme.error
        ),
        SmartCollection(
            name = "Inspiración Creativa",
            emoji = "🎨",
            description = "Para despertar la creatividad",
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
        )
    )
    
    var showNewCollectionDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        bottomSheetState.show()
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
        dragHandle = null
    ) {
        FavoritesCollectionContent(
            quote = quote,
            collections = smartCollections,
            onCollectionSelected = { collectionName ->
                scope.launch {
                    bottomSheetState.hide()
                    onCollectionSelected(collectionName)
                    onDismiss()
                }
            },
            onCreateNewCollection = {
                showNewCollectionDialog = true
            }
        )
    }
    
    // Diálogo para crear nueva colección
    if (showNewCollectionDialog) {
        NewCollectionDialog(
            onDismiss = { showNewCollectionDialog = false },
            onCollectionCreated = { collectionName ->
                showNewCollectionDialog = false
                onCollectionSelected(collectionName)
                onDismiss()
            }
        )
    }
}

@Composable
private fun FavoritesCollectionContent(
    quote: Quote,
    collections: List<SmartCollection>,
    onCollectionSelected: (String) -> Unit,
    onCreateNewCollection: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
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
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                    RoundedCornerShape(2.dp)
                )
                .align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Título
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Collections,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "Agregar a Colección",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Subtítulo
        Text(
            text = "Organiza tus citas favoritas en colecciones temáticas",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Preview de la cita
        QuotePreviewCard(quote = quote)
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Lista de colecciones
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(collections) { collection ->
                CollectionItemCard(
                    collection = collection,
                    onClick = { onCollectionSelected(collection.name) }
                )
            }
            
            // Botón para crear nueva colección
            item {
                CreateNewCollectionCard(
                    onClick = onCreateNewCollection
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun QuotePreviewCard(quote: Quote) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "\"${quote.text}\"",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onPrimary,
                    lineHeight = 20.sp
                ),
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "— ${quote.author}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                )
                
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
                    )
                ) {
                    Text(
                        text = quote.category,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CollectionItemCard(
    collection: SmartCollection,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji e indicador de color
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        collection.color.copy(alpha = 0.2f),
                        RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = collection.emoji,
                    fontSize = 24.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Información de la colección
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = collection.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
                
                Text(
                    text = collection.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                )
            }
            
            // Indicador de selección
            Icon(
                Icons.Default.BookmarkBorder,
                contentDescription = "Agregar a colección",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun CreateNewCollectionCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
                        RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Crear nueva colección",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Crear Nueva Colección",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
                
                Text(
                    text = "Personaliza tu organización de favoritos",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                )
            }
        }
    }
}

@Composable
private fun NewCollectionDialog(
    onDismiss: () -> Unit,
    onCollectionCreated: (String) -> Unit
) {
    var collectionName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Nueva Colección",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column {
                Text(
                    text = "Dale un nombre a tu nueva colección de favoritos",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = collectionName,
                    onValueChange = { collectionName = it },
                    label = { Text("Nombre de la colección") },
                    placeholder = { Text("Ej: Mis citas inspiradoras") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (collectionName.isNotBlank()) {
                        onCollectionCreated(collectionName)
                    }
                },
                enabled = collectionName.isNotBlank()
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

data class SmartCollection(
    val name: String,
    val emoji: String,
    val description: String,
    val color: Color
)
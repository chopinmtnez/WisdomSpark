package com.albertowisdom.wisdomspark.presentation.ui.screens.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.albertowisdom.wisdomspark.ads.AdMobManager
import com.albertowisdom.wisdomspark.ads.BannerAdView
import com.albertowisdom.wisdomspark.data.models.Quote
import com.albertowisdom.wisdomspark.presentation.ui.components.QuoteCard
import com.albertowisdom.wisdomspark.presentation.ui.components.ShareBottomSheet
import com.albertowisdom.wisdomspark.presentation.ui.theme.*
import com.albertowisdom.wisdomspark.utils.ShareUtils
import com.albertowisdom.wisdomspark.utils.getCategoryEmoji
import com.albertowisdom.wisdomspark.data.repository.QuoteRepository
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pantalla de detalle de categoría - muestra todas las citas de una categoría específica
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    categoryName: String,
    adMobManager: AdMobManager,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: CategoryDetailViewModel = hiltViewModel()
    
    // Estados para las nuevas funcionalidades
    var showShareBottomSheet by remember { mutableStateOf(false) }
    
    // Observar las citas de esta categoría
    val quotes by viewModel.quoteRepository.getQuotesByCategory(categoryName).collectAsState(initial = emptyList())
    var selectedQuoteForShare by remember { mutableStateOf<Quote?>(null) }
    var favoriteQuotes by remember { mutableStateOf(setOf<Long>()) }
    var showFavoriteAnimation by remember { mutableStateOf<Long?>(null) }
    
    // Mostrar el número de citas reales en el header
    val quotesCount = quotes.size
    
    // Cargar favoritos desde la base de datos
    LaunchedEffect(Unit) {
        viewModel.quoteRepository.getFavoriteQuotes().collect { favoritesList ->
            favoriteQuotes = favoritesList.map { it.id }.toSet()
        }
    }
    
    
    // Gradiente de fondo que respeta el tema
    val backgroundGradient = Brush.linearGradient(
        colors = getThemedGradientColors(),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header con botón de retroceso
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón de retroceso
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        shadowElevation = 4.dp,
                        onClick = onNavigateBack
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Volver",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Título de la categoría
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${getCategoryEmoji(categoryName)} $categoryName",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Text(
                            text = "$quotesCount citas disponibles",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
            
            // Lista de citas
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(
                    items = quotes,
                    key = { it.id }
                ) { quote ->
                    QuoteCard(
                        quote = quote.copy(isFavorite = favoriteQuotes.contains(quote.id)),
                        onFavoriteClick = {
                            // Smart favorites: marca como favorito con animación y guarda en BD
                            scope.launch {
                                try {
                                    viewModel.quoteRepository.toggleFavorite(quote)
                                    showFavoriteAnimation = quote.id
                                } catch (e: Exception) {
                                    // Error al guardar en BD
                                }
                            }
                        },
                        onShareClick = {
                            // Enhanced sharing con opciones múltiples
                            selectedQuoteForShare = quote
                            showShareBottomSheet = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Espaciado al final
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            // Banner Ad
            if (adMobManager.shouldShowAds()) {
                BannerAdView(
                    onAdLoaded = { adMobManager.onBannerAdLoaded() },
                    onAdFailedToLoad = { error -> 
                        adMobManager.onBannerAdFailedToLoad(error) 
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // Bottom Sheet para compartir
        if (showShareBottomSheet && selectedQuoteForShare != null) {
            ShareBottomSheet(
                quote = selectedQuoteForShare!!,
                onDismiss = { 
                    showShareBottomSheet = false 
                    selectedQuoteForShare = null
                }
            )
        }
        
        // Animación de favorito
        LaunchedEffect(showFavoriteAnimation) {
            if (showFavoriteAnimation != null) {
                delay(1000) // Duración de la animación
                showFavoriteAnimation = null
            }
        }
    }
}

/**
 * Genera citas de muestra para una categoría específica
 */
private fun generateSampleQuotesForCategory(categoryName: String): List<Quote> {
    return when (categoryName.lowercase()) {
        "motivación" -> listOf(
            Quote(1L, "El éxito es la suma de pequeños esfuerzos repetidos día tras día.", "Robert Collier", "Motivación", false),
            Quote(2L, "No esperes por el momento perfecto. Toma el momento y hazlo perfecto.", "Anónimo", "Motivación", false),
            Quote(3L, "Tu único límite eres tú mismo.", "Anónimo", "Motivación", false),
            Quote(4L, "Los sueños no funcionan a menos que tú lo hagas.", "John C. Maxwell", "Motivación", false),
            Quote(5L, "El fracaso es simplemente la oportunidad de comenzar de nuevo de forma más inteligente.", "Henry Ford", "Motivación", false)
        )
        "vida" -> listOf(
            Quote(6L, "La vida es 10% lo que te sucede y 90% cómo reaccionas a ello.", "Charles R. Swindoll", "Vida", false),
            Quote(7L, "No cuentes los días, haz que los días cuenten.", "Muhammad Ali", "Vida", false),
            Quote(8L, "La vida es como andar en bicicleta. Para mantener el equilibrio, debes seguir moviéndote.", "Albert Einstein", "Vida", false),
            Quote(9L, "La vida no se trata de encontrarte a ti mismo. Se trata de crearte a ti mismo.", "George Bernard Shaw", "Vida", false),
            Quote(10L, "En el fin, no recordaremos las palabras de nuestros enemigos, sino el silencio de nuestros amigos.", "Martin Luther King Jr.", "Vida", false)
        )
        "sueños" -> listOf(
            Quote(11L, "La mejor manera de predecir el futuro es creándolo.", "Peter Drucker", "Sueños", false),
            Quote(12L, "Todos nuestros sueños pueden hacerse realidad si tenemos el coraje de perseguirlos.", "Walt Disney", "Sueños", false),
            Quote(13L, "Un sueño no se convierte en realidad a través de la magia; necesita sudor, determinación y trabajo duro.", "Colin Powell", "Sueños", false),
            Quote(14L, "Si puedes soñarlo, puedes hacerlo.", "Walt Disney", "Sueños", false),
            Quote(15L, "Los sueños son extremadamente importantes. No puedes hacer nada sin ellos.", "George Lucas", "Sueños", false)
        )
        "éxito" -> listOf(
            Quote(16L, "Las oportunidades no suceden. Las creas.", "Chris Grosser", "Éxito", false),
            Quote(17L, "El éxito no es la clave de la felicidad. La felicidad es la clave del éxito.", "Albert Schweitzer", "Éxito", false),
            Quote(18L, "No tengas miedo de renunciar a lo bueno para ir por lo grandioso.", "John D. Rockefeller", "Éxito", false),
            Quote(19L, "El éxito es caminar de fracaso en fracaso sin perder el entusiasmo.", "Winston Churchill", "Éxito", false),
            Quote(20L, "El éxito no se mide por la posición que alcanzas en la vida, sino por los obstáculos que superas.", "Booker T. Washington", "Éxito", false)
        )
        "perseverancia" -> listOf(
            Quote(21L, "El único modo de hacer un gran trabajo es amar lo que haces.", "Steve Jobs", "Perseverancia", false),
            Quote(22L, "La perseverancia es la clave del éxito.", "Benjamin Franklin", "Perseverancia", false),
            Quote(23L, "No importa lo lento que vayas mientras no te detengas.", "Confucio", "Perseverancia", false),
            Quote(24L, "La persistencia puede cambiar el fracaso en un logro extraordinario.", "Matt Biondi", "Perseverancia", false),
            Quote(25L, "Muchos de los fracasos de la vida son de personas que no se dieron cuenta de lo cerca que estaban del éxito cuando se rindieron.", "Thomas Edison", "Perseverancia", false)
        )
        "felicidad" -> listOf(
            Quote(26L, "La felicidad no es algo hecho. Viene de tus propias acciones.", "Dalai Lama", "Felicidad", false),
            Quote(27L, "El secreto de la felicidad no es hacer siempre lo que se quiere, sino querer siempre lo que se hace.", "León Tolstói", "Felicidad", false),
            Quote(28L, "La felicidad es cuando lo que piensas, lo que dices y lo que haces están en armonía.", "Mahatma Gandhi", "Felicidad", false),
            Quote(29L, "No hay camino hacia la felicidad, la felicidad es el camino.", "Buda", "Felicidad", false),
            Quote(30L, "La verdadera felicidad es disfrutar del presente, sin depender ansiosamente del futuro.", "Séneca", "Felicidad", false)
        )
        else -> listOf(
            Quote(100L, "Esta categoría contiene sabiduría especial.", "Autor Desconocido", categoryName, false),
            Quote(101L, "Cada día es una nueva oportunidad para crecer.", "Anónimo", categoryName, false),
            Quote(102L, "La sabiduría comienza con la admiración.", "Sócrates", categoryName, false)
        )
    }
}
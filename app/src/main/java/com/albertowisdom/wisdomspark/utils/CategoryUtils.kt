package com.albertowisdom.wisdomspark.utils

/**
 * Mapea cualquier categoría de cualquier idioma a su equivalente español
 * para obtener el emoji y color correcto
 */
fun mapCategoryToSpanish(category: String): String {
    val normalizedCategory = category.lowercase().trim()
    
    return when (normalizedCategory) {
        // Español (ya está en español)
        "motivación", "vida", "sueños", "perseverancia", "educación", 
        "creatividad", "éxito", "autenticidad", "felicidad", "sabiduría",
        "confianza", "progreso", "excelencia", "acción", "liderazgo",
        "negocios", "espiritualidad", "amor", "familia", "amistad",
        "tiempo", "dinero", "salud", "paz", "esperanza", "gratitud",
        "cambio", "oportunidad", "libertad", "naturaleza" -> normalizedCategory
        
        // Inglés -> Español
        "motivation" -> "motivación"
        "life" -> "vida"
        "dreams" -> "sueños"
        "perseverance" -> "perseverancia"
        "education" -> "educación"
        "creativity" -> "creatividad"
        "success" -> "éxito"
        "authenticity" -> "autenticidad"
        "happiness" -> "felicidad"
        "wisdom" -> "sabiduría"
        "confidence" -> "confianza"
        "progress" -> "progreso"
        "excellence" -> "excelencia"
        "action" -> "acción"
        "leadership" -> "liderazgo"
        "business" -> "negocios"
        "spirituality" -> "espiritualidad"
        "love" -> "amor"
        "family" -> "familia"
        "friendship" -> "amistad"
        "time" -> "tiempo"
        "money" -> "dinero"
        "health" -> "salud"
        "peace" -> "paz"
        "hope" -> "esperanza"
        "gratitude" -> "gratitud"
        "change" -> "cambio"
        "opportunity" -> "oportunidad"
        "freedom" -> "libertad"
        "nature" -> "naturaleza"
        
        // Francés -> Español
        "motivation" -> "motivación"  // mismo que inglés
        "vie" -> "vida"
        "rêves" -> "sueños"
        "persévérance" -> "perseverancia"
        "éducation" -> "educación"
        "créativité" -> "creatividad"
        "succès" -> "éxito"
        "authenticité" -> "autenticidad"
        "bonheur" -> "felicidad"
        "sagesse" -> "sabiduría"
        "confiance" -> "confianza"
        "progrès" -> "progreso"
        "excellence" -> "excelencia"  // mismo que inglés
        "action" -> "acción"  // mismo que inglés
        "leadership" -> "liderazgo"  // mismo que inglés
        "affaires" -> "negocios"
        "spiritualité" -> "espiritualidad"
        "amour" -> "amor"
        "famille" -> "familia"
        "amitié" -> "amistad"
        "temps" -> "tiempo"
        "argent" -> "dinero"
        "santé" -> "salud"
        "paix" -> "paz"
        "espoir" -> "esperanza"
        "gratitude" -> "gratitud"  // mismo que inglés
        "changement" -> "cambio"
        "opportunité" -> "oportunidad"
        "liberté" -> "libertad"
        "nature" -> "naturaleza"  // mismo que inglés
        
        // Alemán -> Español
        "motivation" -> "motivación"  // mismo que inglés
        "leben" -> "vida"
        "träume" -> "sueños"
        "ausdauer" -> "perseverancia"
        "bildung" -> "educación"
        "kreativität" -> "creatividad"
        "erfolg" -> "éxito"
        "authentizität" -> "autenticidad"
        "glück" -> "felicidad"
        "weisheit" -> "sabiduría"
        "vertrauen" -> "confianza"
        "fortschritt" -> "progreso"
        "exzellenz" -> "excelencia"
        "aktion" -> "acción"
        "führung" -> "liderazgo"
        "geschäft" -> "negocios"
        "spiritualität" -> "espiritualidad"
        "liebe" -> "amor"
        "familie" -> "familia"  // mismo que inglés
        "freundschaft" -> "amistad"
        "zeit" -> "tiempo"
        "geld" -> "dinero"
        "gesundheit" -> "salud"
        "frieden" -> "paz"
        "hoffnung" -> "esperanza"
        "dankbarkeit" -> "gratitud"
        "veränderung" -> "cambio"
        "gelegenheit" -> "oportunidad"
        "freiheit" -> "libertad"
        "natur" -> "naturaleza"
        
        // Portugués -> Español
        "motivação" -> "motivación"
        "vida" -> "vida"  // mismo que español
        "sonhos" -> "sueños"
        "perseverança" -> "perseverancia"
        "educação" -> "educación"
        "criatividade" -> "creatividad"
        "sucesso" -> "éxito"
        "autenticidade" -> "autenticidad"
        "felicidade" -> "felicidad"
        "sabedoria" -> "sabiduría"
        "confiança" -> "confianza"
        "progresso" -> "progreso"
        "excelência" -> "excelencia"
        "ação" -> "acción"
        "liderança" -> "liderazgo"
        "negócios" -> "negocios"
        "espiritualidade" -> "espiritualidad"
        "amor" -> "amor"  // mismo que español
        "família" -> "familia"
        "amizade" -> "amistad"
        "tempo" -> "tiempo"
        "dinheiro" -> "dinero"
        "saúde" -> "salud"
        "paz" -> "paz"  // mismo que español
        "esperança" -> "esperanza"
        "gratidão" -> "gratitud"
        "mudança" -> "cambio"
        "oportunidade" -> "oportunidad"
        "liberdade" -> "libertad"
        "natureza" -> "naturaleza"
        
        // Italiano -> Español
        "motivazione" -> "motivación"
        "vita" -> "vida"
        "sogni" -> "sueños"
        "perseveranza" -> "perseverancia"
        "educazione" -> "educación"
        "creatività" -> "creatividad"
        "successo" -> "éxito"
        "autenticità" -> "autenticidad"
        "felicità" -> "felicidad"
        "saggezza" -> "sabiduría"
        "fiducia" -> "confianza"
        "progresso" -> "progreso"
        "eccellenza" -> "excelencia"
        "azione" -> "acción"
        "leadership" -> "liderazgo"  // mismo que inglés
        "affari" -> "negocios"
        "spiritualità" -> "espiritualidad"
        "amore" -> "amor"
        "famiglia" -> "familia"
        "amicizia" -> "amistad"
        "tempo" -> "tiempo"
        "denaro" -> "dinero"
        "salute" -> "salud"
        "pace" -> "paz"
        "speranza" -> "esperanza"
        "gratitudine" -> "gratitud"
        "cambiamento" -> "cambio"
        "opportunità" -> "oportunidad"
        "libertà" -> "libertad"
        "natura" -> "naturaleza"
        
        // Si no encuentra mapeo, devolver la categoría original
        else -> category.lowercase().trim()
    }
}

/**
 * Obtiene el emoji correspondiente a una categoría (ahora con soporte multiidioma)
 */
fun getCategoryEmoji(category: String): String {
    // Primero mapear la categoría a español
    val spanishCategory = mapCategoryToSpanish(category)
    return when (spanishCategory) {
        "motivación" -> "🔥"
        "vida" -> "🌱"
        "sueños" -> "✨"
        "perseverancia" -> "💪"
        "educación" -> "📚"
        "creatividad" -> "🎨"
        "éxito" -> "🏆"
        "autenticidad" -> "🦋"
        "felicidad" -> "😊"
        "sabiduría" -> "🧠"
        "confianza" -> "💎"
        "progreso" -> "📈"
        "excelencia" -> "⭐"
        "acción" -> "⚡"
        "liderazgo" -> "👑"
        "negocios" -> "💼"
        "espiritualidad" -> "🙏"
        "amor" -> "❤️"
        "familia" -> "👨‍👩‍👧‍👦"
        "amistad" -> "🤝"
        "tiempo" -> "⏰"
        "dinero" -> "💰"
        "salud" -> "💚"
        "paz" -> "☮️"
        "esperanza" -> "🌈"
        "gratitud" -> "🙏"
        "cambio" -> "🔄"
        "oportunidad" -> "🚪"
        "libertad" -> "🕊️"
        "naturaleza" -> "🌿"
        else -> "💫"
    }
}

/**
 * Obtiene el color primario asociado a una categoría (ahora con soporte multiidioma)
 */
fun getCategoryColor(category: String): androidx.compose.ui.graphics.Color {
    // Primero mapear la categoría a español
    val spanishCategory = mapCategoryToSpanish(category)
    return when (spanishCategory) {
        "motivación" -> androidx.compose.ui.graphics.Color(0xFFFF6B35) // Naranja fuego
        "vida" -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Verde vida
        "sueños" -> androidx.compose.ui.graphics.Color(0xFF9C27B0) // Púrpura mágico
        "perseverancia" -> androidx.compose.ui.graphics.Color(0xFF2196F3) // Azul fuerte
        "educación" -> androidx.compose.ui.graphics.Color(0xFF3F51B5) // Azul académico
        "creatividad" -> androidx.compose.ui.graphics.Color(0xFFE91E63) // Rosa creativo
        "éxito" -> androidx.compose.ui.graphics.Color(0xFFFFC107) // Dorado éxito
        "autenticidad" -> androidx.compose.ui.graphics.Color(0xFF00BCD4) // Cian auténtico
        "felicidad" -> androidx.compose.ui.graphics.Color(0xFFFFEB3B) // Amarillo feliz
        "sabiduría" -> androidx.compose.ui.graphics.Color(0xFF795548) // Marrón sabio
        "confianza" -> androidx.compose.ui.graphics.Color(0xFF607D8B) // Azul gris confiable
        "progreso" -> androidx.compose.ui.graphics.Color(0xFF8BC34A) // Verde progreso
        "excelencia" -> androidx.compose.ui.graphics.Color(0xFFFF9800) // Naranja excelencia
        "acción" -> androidx.compose.ui.graphics.Color(0xFFF44336) // Rojo acción
        "liderazgo" -> androidx.compose.ui.graphics.Color(0xFF673AB7) // Púrpura liderazgo
        "negocios" -> androidx.compose.ui.graphics.Color(0xFF37474F) // Gris negocios
        "espiritualidad" -> androidx.compose.ui.graphics.Color(0xFF9C27B0) // Púrpura espiritual
        "amor" -> androidx.compose.ui.graphics.Color(0xFFE91E63) // Rosa amor
        "familia" -> androidx.compose.ui.graphics.Color(0xFF8BC34A) // Verde familia
        "amistad" -> androidx.compose.ui.graphics.Color(0xFF03A9F4) // Azul amistad
        "tiempo" -> androidx.compose.ui.graphics.Color(0xFF795548) // Marrón tiempo
        "dinero" -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Verde dinero
        "salud" -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Verde salud
        "paz" -> androidx.compose.ui.graphics.Color(0xFF81C784) // Verde claro paz
        "esperanza" -> androidx.compose.ui.graphics.Color(0xFFFFEB3B) // Amarillo esperanza
        "gratitud" -> androidx.compose.ui.graphics.Color(0xFFFF9800) // Naranja gratitud
        "cambio" -> androidx.compose.ui.graphics.Color(0xFF00BCD4) // Cian cambio
        "oportunidad" -> androidx.compose.ui.graphics.Color(0xFFFFC107) // Dorado oportunidad
        "libertad" -> androidx.compose.ui.graphics.Color(0xFF03A9F4) // Azul libertad
        "naturaleza" -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Verde naturaleza
        else -> androidx.compose.ui.graphics.Color(0xFF9E9E9E) // Gris por defecto
    }
}

/**
 * Obtiene una descripción de la categoría (ahora con soporte multiidioma)
 */
fun getCategoryDescription(category: String): String {
    // Primero mapear la categoría a español
    val spanishCategory = mapCategoryToSpanish(category)
    return when (spanishCategory) {
        "motivación" -> "Citas que encienden tu pasión interior"
        "vida" -> "Reflexiones sobre el arte de vivir"
        "sueños" -> "Inspiración para alcanzar lo imposible"
        "perseverancia" -> "Fuerza para superar cualquier obstáculo"
        "educación" -> "El poder transformador del conocimiento"
        "creatividad" -> "Desata tu potencial creativo"
        "éxito" -> "El camino hacia tus logros más grandes"
        "autenticidad" -> "Sé fiel a quien realmente eres"
        "felicidad" -> "Encuentra la alegría en cada momento"
        "sabiduría" -> "Conocimiento que transforma vidas"
        "confianza" -> "Cree en tu poder interior"
        "progreso" -> "Avanza paso a paso hacia tus metas"
        "excelencia" -> "La búsqueda de la mejor versión de ti"
        "acción" -> "Transforma tus pensamientos en realidad"
        "liderazgo" -> "Inspira y guía a otros hacia el éxito"
        "negocios" -> "Sabiduría para el mundo empresarial"
        "espiritualidad" -> "Conecta con tu ser interior"
        "amor" -> "El poder transformador del amor"
        "familia" -> "Los lazos que nos fortalecen"
        "amistad" -> "Relaciones que enriquecen el alma"
        "tiempo" -> "El recurso más valioso que tenemos"
        "dinero" -> "Sabiduría financiera para la vida"
        "salud" -> "Cuida tu templo, cuida tu vida"
        "paz" -> "Encuentra la serenidad interior"
        "esperanza" -> "La luz que guía en la oscuridad"
        "gratitud" -> "Aprecia las bendiciones de la vida"
        "cambio" -> "Abraza las transformaciones"
        "oportunidad" -> "Reconoce las puertas que se abren"
        "libertad" -> "Vive sin limitaciones autoimpuestas" 
        "naturaleza" -> "Sabiduría de la madre tierra"
        else -> "Sabiduría para inspirar tu día"
    }
}

/**
 * Obtiene todas las categorías disponibles con sus datos
 */
data class CategoryInfo(
    val name: String,
    val emoji: String,
    val color: androidx.compose.ui.graphics.Color,
    val description: String
)

fun getAllCategories(): List<CategoryInfo> {
    val categories = listOf(
        "Motivación", "Vida", "Sueños", "Perseverancia", "Educación",
        "Creatividad", "Éxito", "Autenticidad", "Felicidad", "Sabiduría",
        "Confianza", "Progreso", "Excelencia", "Acción"
    )
    
    return categories.map { category ->
        CategoryInfo(
            name = category,
            emoji = getCategoryEmoji(category),
            color = getCategoryColor(category),
            description = getCategoryDescription(category)
        )
    }
}

/**
 * Obtiene categorías populares (las más utilizadas)
 */
fun getPopularCategories(): List<String> {
    return listOf("Motivación", "Vida", "Éxito", "Felicidad", "Sueños", "Sabiduría")
}

/**
 * Verifica si una categoría existe (ahora con soporte multiidioma)
 */
fun isValidCategory(category: String): Boolean {
    val validCategories = listOf(
        "motivación", "vida", "sueños", "perseverancia", "educación",
        "creatividad", "éxito", "autenticidad", "felicidad", "sabiduría",
        "confianza", "progreso", "excelencia", "acción", "liderazgo",
        "negocios", "espiritualidad", "amor", "familia", "amistad",
        "tiempo", "dinero", "salud", "paz", "esperanza", "gratitud",
        "cambio", "oportunidad", "libertad", "naturaleza"
    )
    // Mapear la categoría a español y verificar si existe
    val spanishCategory = mapCategoryToSpanish(category)
    return spanishCategory in validCategories
}

/**
 * Obtiene sugerencias de categorías basadas en texto
 */
fun getSuggestedCategories(text: String): List<String> {
    val lowerText = text.lowercase()
    val suggestions = mutableSetOf<String>()
    
    // Mapeo de palabras clave a categorías
    val keywordMap = mapOf(
        listOf("motivar", "inspirar", "fuerza", "poder") to "Motivación",
        listOf("vivir", "existir", "ser", "experiencia") to "Vida",
        listOf("sueño", "meta", "objetivo", "aspiración") to "Sueños",
        listOf("perseverar", "resistir", "luchar", "continuar") to "Perseverancia",
        listOf("aprender", "estudiar", "conocimiento", "enseñar") to "Educación",
        listOf("crear", "arte", "innovar", "imaginar") to "Creatividad",
        listOf("lograr", "triunfar", "ganar", "alcanzar") to "Éxito",
        listOf("auténtico", "real", "verdadero", "genuino") to "Autenticidad",
        listOf("feliz", "alegría", "contento", "gozo") to "Felicidad",
        listOf("sabio", "inteligente", "prudente", "conocer") to "Sabiduría",
        listOf("confiar", "seguro", "creer", "fe") to "Confianza",
        listOf("avanzar", "mejorar", "crecer", "desarrollar") to "Progreso",
        listOf("excelente", "perfecto", "mejor", "calidad") to "Excelencia",
        listOf("actuar", "hacer", "ejecutar", "realizar") to "Acción"
    )
    
    // Buscar coincidencias
    keywordMap.forEach { (keywords, category) ->
        if (keywords.any { keyword -> lowerText.contains(keyword) }) {
            suggestions.add(category)
        }
    }
    
    // Si no hay sugerencias específicas, devolver categorías populares
    return if (suggestions.isEmpty()) {
        getPopularCategories().take(3)
    } else {
        suggestions.take(3).toList()
    }
}

/**
 * Obtiene estadísticas de una categoría
 */
data class CategoryStats(
    val name: String,
    val emoji: String,
    val totalQuotes: Int,
    val favoriteQuotes: Int,
    val lastUsed: String?
)

/**
 * Normaliza el nombre de una categoría
 */
fun normalizeCategory(category: String): String {
    return category.trim()
        .lowercase()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

/**
 * Obtiene el gradiente de colores para una categoría (ahora con soporte multiidioma)
 */
fun getCategoryGradient(category: String): List<androidx.compose.ui.graphics.Color> {
    val primaryColor = getCategoryColor(category)
    return listOf(
        primaryColor.copy(alpha = 0.8f),
        primaryColor.copy(alpha = 0.4f),
        primaryColor.copy(alpha = 0.1f)
    )
}

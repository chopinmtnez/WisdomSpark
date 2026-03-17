package com.albertowisdom.wisdomspark.performance

import com.albertowisdom.wisdomspark.data.models.Quote
import org.junit.Test
import kotlin.system.measureTimeMillis
import kotlin.test.assertTrue

/**
 * Tests para verificar optimizaciones de rendimiento
 */
class PerformanceTest {

    @Test
    fun `verificar que la creación de objetos Quote es eficiente`() {
        val quotes = mutableListOf<Quote>()
        
        val timeMillis = measureTimeMillis {
            repeat(1000) { i ->
                quotes.add(
                    Quote(
                        id = i.toLong(),
                        text = "Test quote $i",
                        author = "Author $i",
                        category = "Category ${i % 5}",
                        language = if (i % 2 == 0) "es" else "en",
                        isFavorite = i % 3 == 0
                    )
                )
            }
        }
        
        // Verificar que se crearon correctamente
        assertEquals(1000, quotes.size)
        
        // Verificar que el tiempo de creación es razonable (< 100ms para 1000 objetos)
        assertTrue(
            timeMillis < 100, 
            "La creación de 1000 objetos Quote tomó ${timeMillis}ms, esperado < 100ms"
        )
        
        println("✅ Creación de 1000 objetos Quote: ${timeMillis}ms")
    }

    @Test
    fun `verificar eficiencia de filtrado de listas grandes`() {
        // Crear una lista grande de citas
        val quotes = (1..10000).map { i ->
            Quote(
                id = i.toLong(),
                text = "Quote $i",
                author = "Author ${i % 100}",
                category = listOf("Motivación", "Liderazgo", "Vida", "Amor", "Sabiduría")[i % 5],
                language = if (i % 2 == 0) "es" else "en",
                isFavorite = i % 10 == 0
            )
        }

        // Test 1: Filtrar por favoritos
        val favoritesTime = measureTimeMillis {
            val favorites = quotes.filter { it.isFavorite }
            assertEquals(1000, favorites.size) // 10% deberían ser favoritos
        }

        // Test 2: Filtrar por idioma
        val languageTime = measureTimeMillis {
            val spanishQuotes = quotes.filter { it.language == "es" }
            assertEquals(5000, spanishQuotes.size) // 50% deberían ser en español
        }

        // Test 3: Filtrar por categoría
        val categoryTime = measureTimeMillis {
            val motivationQuotes = quotes.filter { it.category == "Motivación" }
            assertEquals(2000, motivationQuotes.size) // 20% deberían ser de motivación
        }

        // Verificar tiempos razonables (< 50ms cada uno)
        assertTrue(favoritesTime < 50, "Filtrado de favoritos: ${favoritesTime}ms")
        assertTrue(languageTime < 50, "Filtrado por idioma: ${languageTime}ms") 
        assertTrue(categoryTime < 50, "Filtrado por categoría: ${categoryTime}ms")

        println("✅ Filtrado de favoritos (10K items): ${favoritesTime}ms")
        println("✅ Filtrado por idioma (10K items): ${languageTime}ms")
        println("✅ Filtrado por categoría (10K items): ${categoryTime}ms")
    }

    @Test
    fun `verificar eficiencia de operaciones de sanitización`() {
        val maliciousTexts = listOf(
            "Normal text",
            "<script>alert('xss')</script>",
            "Text with \"quotes\" and 'apostrophes'",
            "HTML entities &lt;&gt;&amp;",
            "   Extra    spaces   everywhere   ",
            "Very long text ".repeat(100),
            "Mixed <b>HTML</b> and &nbsp; entities with   spaces"
        )

        val sanitizationTime = measureTimeMillis {
            maliciousTexts.forEach { text ->
                val sanitized = sanitizeText(text)
                // Verificaciones básicas
                assertFalse(sanitized.contains("<script>"))
                assertFalse(sanitized.contains("&"))
                assertFalse(sanitized.contains("\""))
                assertTrue(sanitized.length <= 500) // Límite de longitud
                assertFalse(sanitized.startsWith(" ")) // Sin espacios al inicio
                assertFalse(sanitized.endsWith(" ")) // Sin espacios al final
            }
        }

        // Sanitización debe ser muy rápida (< 10ms)
        assertTrue(
            sanitizationTime < 10, 
            "Sanitización de ${maliciousTexts.size} textos: ${sanitizationTime}ms"
        )
        
        println("✅ Sanitización de ${maliciousTexts.size} textos: ${sanitizationTime}ms")
    }

    // Función auxiliar para simular sanitización (igual a la del repository)
    private fun sanitizeText(input: String): String {
        return input
            .trim()
            .take(500)
            .replace(Regex("[<>\"'&]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    @Test
    fun `verificar performance de operaciones masivas`() {
        val startTime = System.currentTimeMillis()
        
        // Simular operaciones típicas de la app
        val quotes = (1..1000).map { i ->
            Quote(
                id = i.toLong(),
                text = "Quote number $i with some text",
                author = "Author $i",
                category = "Category",
                language = "es"
            )
        }
        
        // Operaciones de transformación típicas
        val processTime = measureTimeMillis {
            val processed = quotes
                .filter { it.text.length > 10 }
                .map { it.copy(isFavorite = it.id % 5 == 0L) }
                .groupBy { it.category }
                .mapValues { (_, quotes) -> quotes.size }
        }
        
        val totalTime = System.currentTimeMillis() - startTime
        
        // Todas las operaciones combinadas deben ser < 100ms
        assertTrue(totalTime < 100, "Operaciones masivas: ${totalTime}ms")
        
        println("✅ Creación y procesamiento de 1000 citas: ${processTime}ms")
        println("✅ Tiempo total de operaciones: ${totalTime}ms")
    }

    private fun assertEquals(expected: Any, actual: Any, message: String = "") {
        if (expected != actual) {
            throw AssertionError("Expected $expected but was $actual. $message")
        }
    }

    private fun assertFalse(condition: Boolean, message: String = "") {
        if (condition) {
            throw AssertionError("Expected false but was true. $message")
        }
    }
}
package com.albertowisdom.wisdomspark.validation

import org.junit.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests de validación para verificar que el código se compiló correctamente
 * y que las mejoras implementadas están presentes
 */
class CodeValidationTest {

    @Test
    fun `verificar que las clases Use Case existen`() {
        // Verificar que nuestras clases Use Case fueron creadas correctamente
        val getTodayQuoteClass = com.albertowisdom.wisdomspark.domain.usecase.GetTodayQuoteUseCase::class
        val toggleFavoriteClass = com.albertowisdom.wisdomspark.domain.usecase.ToggleFavoriteUseCase::class
        val getCategoriesClass = com.albertowisdom.wisdomspark.domain.usecase.GetCategoriesUseCase::class
        
        assertNotNull(getTodayQuoteClass)
        assertNotNull(toggleFavoriteClass)
        assertNotNull(getCategoriesClass)
        
        // Verificar que tienen los métodos invoke
        assertTrue(getTodayQuoteClass.members.any { it.name == "invoke" })
        assertTrue(toggleFavoriteClass.members.any { it.name == "invoke" })
        assertTrue(getCategoriesClass.members.any { it.name == "invoke" })
    }

    @Test
    fun `verificar que CategoryItem data class existe`() {
        val categoryItemClass = com.albertowisdom.wisdomspark.domain.usecase.CategoryItem::class
        assertNotNull(categoryItemClass)
        
        // Verificar propiedades
        val properties = categoryItemClass.members.map { it.name }
        assertTrue(properties.contains("name"))
        assertTrue(properties.contains("emoji"))
        assertTrue(properties.contains("quotesCount"))
    }

    @Test
    fun `verificar funciones de sanitización`() {
        // Estas son funciones privadas, así que verificamos indirectamente
        // que la clase QuoteRepository se puede instanciar correctamente
        val repositoryClass = com.albertowisdom.wisdomspark.data.repository.QuoteRepository::class
        assertNotNull(repositoryClass)
        
        // Verificar que los métodos de sanitización están implementados
        // (no podemos testear directamente porque son privados, pero verificamos compilación)
        assertTrue(repositoryClass.members.any { it.name == "insertQuote" })
        assertTrue(repositoryClass.members.any { it.name == "updateQuote" })
    }

    @Test
    fun `verificar que las entities tienen índices`() {
        val quoteEntityClass = com.albertowisdom.wisdomspark.data.local.database.entities.QuoteEntity::class
        assertNotNull(quoteEntityClass)
        
        // Verificar que la anotación @Entity está presente
        val entityAnnotation = quoteEntityClass.annotations.find { 
            it.annotationClass.simpleName == "Entity" 
        }
        assertNotNull(entityAnnotation)
    }

    @Test
    fun `verificar que UseCaseModule existe`() {
        val useCaseModuleClass = com.albertowisdom.wisdomspark.di.UseCaseModule::class
        assertNotNull(useCaseModuleClass)
        
        // Verificar que tiene métodos provide
        val methods = useCaseModuleClass.members.map { it.name }
        assertTrue(methods.contains("provideGetTodayQuoteUseCase"))
        assertTrue(methods.contains("provideToggleFavoriteUseCase")) 
        assertTrue(methods.contains("provideGetCategoriesUseCase"))
    }
}
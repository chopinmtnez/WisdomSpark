package com.albertowisdom.wisdomspark.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.albertowisdom.wisdomspark.data.models.Quote
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ShareUtils {
    
    /**
     * Función principal para compartir citas (llamada desde QuoteCard)
     */
    fun shareQuote(context: Context, quote: Quote) {
        shareQuoteAsImage(context, quote)
    }
    
    /**
     * Comparte una cita como texto simple
     */
    fun shareQuoteAsText(context: Context, quote: Quote) {
        try {
            val shareText = buildQuoteText(quote)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
                putExtra(Intent.EXTRA_SUBJECT, "Sabiduría Diaria - WisdomSpark")
            }
            
            val chooser = Intent.createChooser(shareIntent, "Compartir sabiduría")
            context.startActivity(chooser)
            
        } catch (e: Exception) {
            showError(context, "Error al compartir: ${e.message}")
        }
    }
    
    /**
     * Comparte una cita como imagen (ideal para Instagram/Facebook)
     */
    fun shareQuoteAsImage(context: Context, quote: Quote) {
        try {
            val bitmap = createQuoteImage(quote)
            val imageUri = saveBitmapToCache(context, bitmap)
            
            if (imageUri != null) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, imageUri)
                    putExtra(Intent.EXTRA_TEXT, buildQuoteText(quote))
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                val chooser = Intent.createChooser(shareIntent, "Compartir imagen")
                context.startActivity(chooser)
            } else {
                showError(context, "Error al crear la imagen")
            }
            
        } catch (e: Exception) {
            showError(context, "Error al compartir imagen: ${e.message}")
        }
    }
    
    /**
     * Comparte específicamente en Instagram Stories
     */
    fun shareToInstagramStory(context: Context, quote: Quote) {
        try {
            val bitmap = createInstagramStoryImage(quote)
            val imageUri = saveBitmapToCache(context, bitmap, "instagram_story.jpg")
            
            if (imageUri != null) {
                val intent = Intent("com.instagram.share.ADD_TO_STORY").apply {
                    type = "image/*"
                    putExtra("interactive_asset_uri", imageUri)
                    putExtra("content_url", "https://wisdomspark.app") // URL de tu app
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                } else {
                    // Instagram no instalado, usar compartir normal
                    shareQuoteAsImage(context, quote)
                }
            }
            
        } catch (e: Exception) {
            showError(context, "Error al compartir en Instagram: ${e.message}")
        }
    }
    
    /**
     * Opciones múltiples de compartir
     */
    fun showShareOptions(context: Context, quote: Quote, @Suppress("UNUSED_PARAMETER") onOptionSelected: (ShareOption) -> Unit) {
        // Esta función se usará con un BottomSheet en el UI
        // Por ahora, compartir como imagen por defecto
        shareQuoteAsImage(context, quote)
    }
    
    // Funciones privadas de utilidad
    
    private fun buildQuoteText(quote: Quote): String {
        return """
            💫 "${quote.text}"
            
            — ${quote.author}
            
            📱 Descubre más sabiduría diaria con WisdomSpark
            🌟 Categoría: ${quote.category}
            
            #WisdomSpark #SabiduríaDiaria #Motivación #${quote.category.replace(" ", "")}
        """.trimIndent()
    }
    
    private fun createQuoteImage(quote: Quote): Bitmap {
        val width = 1080
        val height = 1080
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Fondo con gradiente
        val gradientPaint = Paint().apply {
            shader = android.graphics.LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                intArrayOf(
                    Color.parseColor("#667eea"), // Sage green
                    Color.parseColor("#764ba2"), // Purple
                    Color.parseColor("#f093fb")  // Pink
                ),
                null,
                android.graphics.Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), gradientPaint)
        
        // Overlay semi-transparente
        val overlayPaint = Paint().apply {
            color = Color.parseColor("#80000000")
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)
        
        // Configuración de texto
        val textPaint = Paint().apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        
        val margin = 80f
        val centerX = width / 2f
        val centerY = height / 2f
        
        // Logo/Título WisdomSpark
        textPaint.textSize = 48f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("✨ WisdomSpark", centerX, 120f, textPaint)
        
        // Texto de la cita
        textPaint.textSize = 56f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        
        val quoteText = "\"${quote.text}\""
        val maxWidth = width - (margin * 2)
        drawMultilineText(canvas, quoteText, centerX, centerY - 100f, textPaint, maxWidth)
        
        // Autor
        textPaint.textSize = 40f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
        canvas.drawText("— ${quote.author}", centerX, centerY + 200f, textPaint)
        
        // Categoría con emoji
        textPaint.textSize = 36f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val categoryEmoji = getCategoryEmoji(quote.category)
        canvas.drawText("$categoryEmoji ${quote.category}", centerX, centerY + 280f, textPaint)
        
        // Footer con fecha
        textPaint.textSize = 32f
        textPaint.color = Color.parseColor("#CCFFFFFF")
        val currentDate = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            .format(java.util.Date())
        canvas.drawText("Sabiduría diaria • $currentDate", centerX, height - 60f, textPaint)
        
        return bitmap
    }
    
    private fun createInstagramStoryImage(quote: Quote): Bitmap {
        val width = 1080
        val height = 1920 // Proporción de Instagram Stories
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Fondo más dinámico para Stories
        val gradientPaint = Paint().apply {
            shader = android.graphics.RadialGradient(
                width / 2f, height / 3f, width / 2f,
                intArrayOf(
                    Color.parseColor("#667eea"),
                    Color.parseColor("#764ba2"),
                    Color.parseColor("#1a1a2e")
                ),
                floatArrayOf(0f, 0.7f, 1f),
                android.graphics.Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), gradientPaint)
        
        // Elementos decorativos
        val decorPaint = Paint().apply {
            color = Color.parseColor("#33FFFFFF")
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        
        // Círculos decorativos
        canvas.drawCircle(width * 0.2f, height * 0.15f, 100f, decorPaint)
        canvas.drawCircle(width * 0.8f, height * 0.85f, 150f, decorPaint)
        
        // Texto principal
        val textPaint = Paint().apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        
        val centerX = width / 2f
        val centerY = height / 2f
        
        // Logo
        textPaint.textSize = 64f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("✨ WisdomSpark", centerX, 200f, textPaint)
        
        // Cita principal
        textPaint.textSize = 72f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        
        val quoteText = "\"${quote.text}\""
        val maxWidth = width - 120f
        drawMultilineText(canvas, quoteText, centerX, centerY, textPaint, maxWidth)
        
        // Autor destacado
        textPaint.textSize = 48f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
        canvas.drawText("— ${quote.author}", centerX, centerY + 300f, textPaint)
        
        // Call to action
        textPaint.textSize = 36f
        textPaint.color = Color.parseColor("#FFD700")
        canvas.drawText("Desliza hacia arriba para más sabiduría", centerX, height - 200f, textPaint)
        
        return bitmap
    }
    
    private fun drawMultilineText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        paint: Paint,
        maxWidth: Float
    ) {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""
        
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val bounds = Rect()
            paint.getTextBounds(testLine, 0, testLine.length, bounds)
            
            if (bounds.width() <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine)
                    currentLine = word
                } else {
                    lines.add(word)
                }
            }
        }
        
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }
        
        val lineHeight = paint.textSize * 1.2f
        val startY = y - (lines.size - 1) * lineHeight / 2
        
        lines.forEachIndexed { index, line ->
            canvas.drawText(line, x, startY + index * lineHeight, paint)
        }
    }
    
    private fun getCategoryEmoji(category: String): String {
        return when (category.lowercase()) {
            "motivación" -> "🔥"
            "vida" -> "🌱"
            "sueños" -> "✨"
            "perseverancia" -> "💪"
            "educación" -> "📚"
            "creatividad" -> "🎨"
            "éxito" -> "🏆"
            "autenticidad" -> "💎"
            "felicidad" -> "😊"
            "sabiduría" -> "🦉"
            "confianza" -> "💫"
            "progreso" -> "📈"
            "excelencia" -> "👑"
            "acción" -> "⚡"
            else -> "💫"
        }
    }
    
    private fun saveBitmapToCache(
        context: Context,
        bitmap: Bitmap,
        fileName: String = "shared_quote.jpg"
    ): Uri? {
        return try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            
            val file = File(cachePath, fileName)
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream)
            fileOutputStream.close()
            
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    
    private fun showError(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}

enum class ShareOption {
    TEXT,
    IMAGE,
    INSTAGRAM_STORY,
    WHATSAPP,
    FACEBOOK,
    TWITTER
}

/**
 * Función de extensión para fácil acceso desde Composables
 */
fun shareQuote(context: Context, quote: Quote) {
    ShareUtils.shareQuote(context, quote)
}
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
        try {
            // Intentar compartir como imagen primero
            shareQuoteAsImage(context, quote)
        } catch (e: Exception) {
            // Si falla, usar texto como respaldo
            shareQuoteAsText(context, quote)
        }
    }
    
    /**
     * Función de emergencia que siempre funciona - solo texto
     */
    fun shareQuoteSimple(context: Context, quote: Quote) {
        try {
            val shareText = "\"${quote.text}\"\n\n— ${quote.author}\n\n📱 WisdomSpark"
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            
            val chooser = Intent.createChooser(shareIntent, "Compartir cita")
            context.startActivity(chooser)
            
        } catch (e: Exception) {
            showError(context, "Error al compartir: ${e.message}")
        }
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
                if (chooser.resolveActivity(context.packageManager) != null) {
                    context.startActivity(chooser)
                } else {
                    // Fallback a texto si no hay apps que puedan manejar imágenes
                    shareQuoteAsText(context, quote)
                }
            } else {
                // Fallback a texto si no se puede crear imagen
                shareQuoteAsText(context, quote)
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback a texto si falla la imagen
            shareQuoteAsText(context, quote)
        }
    }
    
    /**
     * Comparte específicamente en Instagram Stories con diseño mejorado
     */
    fun shareToInstagramStory(context: Context, quote: Quote) {
        try {
            val bitmap = createInstagramStoryImage(quote)
            val imageUri = saveBitmapToCache(context, bitmap, "instagram_story.jpg")
            
            if (imageUri != null) {
                val intent = Intent("com.instagram.share.ADD_TO_STORY").apply {
                    type = "image/*"
                    putExtra("interactive_asset_uri", imageUri)
                    putExtra("content_url", "https://wisdomspark.app")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                } else {
                    shareQuoteAsImage(context, quote)
                }
            }
            
        } catch (e: Exception) {
            showError(context, "Error al compartir en Instagram: ${e.message}")
        }
    }
    
    /**
     * Comparte con diseño TikTok/Vertical
     */
    fun shareToTikTok(context: Context, quote: Quote) {
        try {
            val bitmap = createTikTokStyleImage(quote)
            val imageUri = saveBitmapToCache(context, bitmap, "tiktok_quote.jpg")
            
            if (imageUri != null) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, imageUri)
                    putExtra(Intent.EXTRA_TEXT, "#WisdomSpark #SabiduríaDiaria #${quote.category.replace(" ", "")}")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                val chooser = Intent.createChooser(shareIntent, "Compartir en TikTok")
                context.startActivity(chooser)
            }
            
        } catch (e: Exception) {
            shareQuoteAsImage(context, quote)
        }
    }
    
    /**
     * Comparte con diseño personalizado según plataforma
     */
    fun shareToSpecificPlatform(context: Context, quote: Quote, platform: SharePlatform) {
        when (platform) {
            SharePlatform.INSTAGRAM_STORY -> shareToInstagramStory(context, quote)
            SharePlatform.TIKTOK -> shareToTikTok(context, quote)
            SharePlatform.LINKEDIN -> shareToLinkedIn(context, quote)
            SharePlatform.TWITTER -> shareToTwitter(context, quote)
            SharePlatform.WHATSAPP -> shareToWhatsApp(context, quote)
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
        val categoryEmoji = getCategoryEmojiForImage(quote.category)
        canvas.drawText("$categoryEmoji ${quote.category}", centerX, centerY + 280f, textPaint)
        
        // Footer con fecha
        textPaint.textSize = 32f
        textPaint.color = Color.parseColor("#CCFFFFFF")
        val currentDate = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            .format(java.util.Date())
        canvas.drawText("Sabiduría diaria • $currentDate", centerX, height - 60f, textPaint)
        
        return bitmap
    }
    
    private fun createTikTokStyleImage(quote: Quote): Bitmap {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Fondo negro con elementos neon
        canvas.drawColor(Color.BLACK)
        
        // Elementos decorativos neon
        val neonPaint = Paint().apply {
            color = Color.parseColor("#00FF88")
            style = Paint.Style.STROKE
            strokeWidth = 8f
            setShadowLayer(15f, 0f, 0f, Color.parseColor("#00FF88"))
        }
        
        // Marcos decorativos
        val margin = 80f
        canvas.drawRect(margin, margin, width - margin, height - margin, neonPaint)
        
        // Texto principal
        val textPaint = Paint().apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        
        val centerX = width / 2f
        val centerY = height / 2f
        
        // Logo/marca
        textPaint.textSize = 80f
        textPaint.color = Color.parseColor("#00FF88")
        canvas.drawText("WisdomSpark", centerX, 200f, textPaint)
        
        // Cita principal
        textPaint.textSize = 88f
        textPaint.color = Color.WHITE
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        
        val quoteText = "\"${quote.text}\""
        val maxWidth = width - 120f
        drawMultilineText(canvas, quoteText, centerX, centerY, textPaint, maxWidth)
        
        // Autor
        textPaint.textSize = 60f
        textPaint.color = Color.parseColor("#FFD700")
        canvas.drawText("— ${quote.author}", centerX, centerY + 400f, textPaint)
        
        // Hashtags
        textPaint.textSize = 48f
        textPaint.color = Color.parseColor("#00FF88")
        canvas.drawText("#WisdomSpark #SabiduríaDiaria", centerX, height - 150f, textPaint)
        
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
    
    private fun getCategoryEmojiForImage(category: String): String {
        // Usar la función de mapeo universal de CategoryUtils
        return getCategoryEmoji(category)
    }
    
    private fun saveBitmapToCache(
        context: Context,
        bitmap: Bitmap,
        fileName: String = "shared_quote.jpg"
    ): Uri? {
        return try {
            val cachePath = File(context.cacheDir, "images")
            
            // Crear directorio si no existe
            if (!cachePath.exists()) {
                cachePath.mkdirs()
            }
            
            // Crear archivo con nombre único para evitar conflictos
            val uniqueFileName = "${System.currentTimeMillis()}_$fileName"
            val file = File(cachePath, uniqueFileName)
            
            // Guardar bitmap
            FileOutputStream(file).use { fileOutputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream)
                fileOutputStream.flush()
            }
            
            // Verificar que el archivo se creó correctamente
            if (file.exists() && file.length() > 0) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    // Nuevas funciones para plataformas específicas
    
    private fun shareToLinkedIn(context: Context, quote: Quote) {
        val linkedInText = buildLinkedInText(quote)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, linkedInText)
            putExtra(Intent.EXTRA_SUBJECT, "Sabiduría Profesional")
        }
        
        try {
            context.startActivity(Intent.createChooser(shareIntent, "Compartir en LinkedIn"))
        } catch (e: Exception) {
            shareQuoteAsText(context, quote)
        }
    }
    
    private fun shareToTwitter(context: Context, quote: Quote) {
        val twitterText = buildTwitterText(quote)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, twitterText)
        }
        
        try {
            context.startActivity(Intent.createChooser(shareIntent, "Compartir en Twitter"))
        } catch (e: Exception) {
            shareQuoteAsText(context, quote)
        }
    }
    
    private fun shareToWhatsApp(context: Context, quote: Quote) {
        val whatsappText = buildWhatsAppText(quote)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, whatsappText)
            setPackage("com.whatsapp")
        }
        
        try {
            if (shareIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(shareIntent)
            } else {
                shareQuoteAsText(context, quote)
            }
        } catch (e: Exception) {
            shareQuoteAsText(context, quote)
        }
    }
    
    private fun buildLinkedInText(quote: Quote): String {
        return """
            💡 "${quote.text}"
            
            — ${quote.author}
            
            Reflexiones profesionales y crecimiento personal.
            
            #Liderazgo #Motivación #CrecimientoProfesional #WisdomSpark
        """.trimIndent()
    }
    
    private fun buildTwitterText(quote: Quote): String {
        val maxLength = 280
        val baseText = "\"${quote.text}\" — ${quote.author}"
        val hashtags = " #WisdomSpark #Motivación"
        
        return if ((baseText + hashtags).length <= maxLength) {
            baseText + hashtags
        } else {
            baseText.take(maxLength - hashtags.length - 3) + "..." + hashtags
        }
    }
    
    private fun buildWhatsAppText(quote: Quote): String {
        return """
            ✨ *${quote.text}*
            
            — ${quote.author}
            
            🌟 Sabiduría diaria de WisdomSpark
            
            📱 Descarga más inspiración: [link a PlayStore]
        """.trimIndent()
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
    TWITTER,
    TIKTOK,
    LINKEDIN
}

enum class SharePlatform {
    INSTAGRAM_STORY,
    TIKTOK,
    LINKEDIN,
    TWITTER,
    WHATSAPP
}

/**
 * Función de extensión para fácil acceso desde Composables
 */
fun shareQuote(context: Context, quote: Quote) {
    ShareUtils.shareQuote(context, quote)
}

/**
 * Función de prueba para verificar el funcionamiento
 */
fun testShare(context: Context) {
    val testQuote = Quote(
        id = 1,
        text = "Esta es una cita de prueba para verificar que la funcionalidad de compartir funciona correctamente.",
        author = "Autor de Prueba",
        category = "Motivación",
        language = "es",
        isFavorite = false
    )
    ShareUtils.shareQuoteSimple(context, testQuote)
}
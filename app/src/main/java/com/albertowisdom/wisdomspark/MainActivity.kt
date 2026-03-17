package com.albertowisdom.wisdomspark

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.albertowisdom.wisdomspark.ads.AdMobManager
import com.albertowisdom.wisdomspark.data.managers.LanguageManager
import com.albertowisdom.wisdomspark.data.preferences.UserPreferences
import com.albertowisdom.wisdomspark.presentation.ui.WisdomSparkApp
import com.albertowisdom.wisdomspark.presentation.ui.theme.WisdomSparkTheme
import com.albertowisdom.wisdomspark.utils.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * MainActivity con Edge-to-Edge corregido
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var adMobManager: AdMobManager
    
    @Inject
    lateinit var userPreferences: UserPreferences
    
    @Inject
    lateinit var languageManager: LanguageManager
    
    @Inject
    lateinit var notificationHelper: com.albertowisdom.wisdomspark.utils.NotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configurar edge-to-edge con mejor manejo de insets
        setupEdgeToEdge()
        
        // Inicializar AdMob
        adMobManager.initialize(this)
        
        // Verificar si se abrió desde notificación
        val shouldOpenDailyQuote = intent?.getBooleanExtra("open_daily_quote", false) ?: false
        
        setContent {
            // Observar dark mode preference
            val isDarkMode by userPreferences.isDarkModeEnabled.collectAsState(initial = false)
            
            WisdomSparkTheme(
                darkTheme = isDarkMode
            ) {
                WisdomSparkApp(
                    adMobManager = adMobManager,
                    userPreferences = userPreferences,
                    languageManager = languageManager,
                    notificationHelper = notificationHelper,
                    shouldOpenDailyQuote = shouldOpenDailyQuote
                )
            }
        }
    }
    
    private fun setupEdgeToEdge() {
        // Configurar edge-to-edge con mejor control de insets
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.apply {
            // Configurar para que los anuncios intersticiales funcionen correctamente
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.updateBaseContextLocale(newBase))
    }

    override fun onResume() {
        super.onResume()
        // AdMobManager ya está inicializado en Application
    }
}

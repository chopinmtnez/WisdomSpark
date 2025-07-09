package com.albertowisdom.wisdomspark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.albertowisdom.wisdomspark.ads.AdMobManager
import com.albertowisdom.wisdomspark.data.preferences.UserPreferences
import com.albertowisdom.wisdomspark.presentation.ui.WisdomSparkApp
import com.albertowisdom.wisdomspark.presentation.ui.theme.WisdomSparkTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * MainActivity con Edge-to-Edge corregido
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var adMobManager: AdMobManager
    
    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-Edge restaurado ahora que el layout está corregido
        enableEdgeToEdge()
        
        // Inicializar AdMob
        adMobManager.initialize(this)
        
        setContent {
            // Observar dark mode preference
            val isDarkMode by userPreferences.isDarkModeEnabled.collectAsState(initial = false)
            
            WisdomSparkTheme(
                darkTheme = isDarkMode
            ) {
                WisdomSparkApp(
                    adMobManager = adMobManager,
                    userPreferences = userPreferences
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // AdMobManager ya está inicializado en Application
    }
}

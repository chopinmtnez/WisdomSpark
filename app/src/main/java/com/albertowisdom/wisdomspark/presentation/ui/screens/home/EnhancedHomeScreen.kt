@file:OptIn(ExperimentalAnimationApi::class)

package com.albertowisdom.wisdomspark.presentation.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.albertowisdom.wisdomspark.ads.AdMobManager
import com.albertowisdom.wisdomspark.data.preferences.UserPreferences
import com.albertowisdom.wisdomspark.presentation.ui.screens.home.HomeScreen
import com.albertowisdom.wisdomspark.presentation.ui.screens.home.SwipeableHomeScreen
import com.albertowisdom.wisdomspark.presentation.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Enhanced HomeScreen que puede alternar entre modo clásico y swipeable
 * Incluye toggle flotante y transiciones suaves entre modos
 */
@Composable
fun EnhancedHomeScreen(
    adMobManager: AdMobManager,
    userPreferences: UserPreferences,
    viewModel: HomeViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    
    // Estado del modo swipeable
    val isSwipeableMode by userPreferences.isSwipeableModeEnabled.collectAsState(initial = false)
    val isHapticEnabled by userPreferences.isHapticFeedbackEnabled.collectAsState(initial = true)
    
    // Animación de transición entre modos
    val transition = updateTransition(targetState = isSwipeableMode, label = "modeTransition")
    
    val backgroundAlpha by transition.animateFloat(
        transitionSpec = { tween(600, easing = FastOutSlowInEasing) },
        label = "backgroundAlpha"
    ) { swipeable ->
        if (swipeable) 1f else 0.95f
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = getWisdomGradientColors().map { it.copy(alpha = backgroundAlpha) }
                )
            )
    ) {
        // Contenido principal con transición
        transition.AnimatedContent(
            transitionSpec = {
                if (targetState) {
                    // Transición hacia swipeable mode
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(600, easing = FastOutSlowInEasing)
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(600, easing = FastOutSlowInEasing)
                    )
                } else {
                    // Transición hacia modo clásico
                    slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(600, easing = FastOutSlowInEasing)
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(600, easing = FastOutSlowInEasing)
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { swipeableMode ->
            if (swipeableMode) {
                SwipeableHomeScreen(
                    adMobManager = adMobManager,
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                HomeScreen(
                    adMobManager = adMobManager,
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        // Toggle flotante con animación
        ModeToggleButton(
            isSwipeableMode = isSwipeableMode,
            onToggle = { newMode ->
                if (isHapticEnabled) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                scope.launch {
                    userPreferences.setSwipeableMode(newMode)
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )
        
        // Indicador de modo actual
        ModeIndicator(
            isSwipeableMode = isSwipeableMode,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )
    }
}

@Composable
private fun ModeToggleButton(
    isSwipeableMode: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // Animaciones para el toggle
    val backgroundColor by animateColorAsState(
        targetValue = if (isSwipeableMode) WisdomGold else Color.White,
        animationSpec = tween(300),
        label = "backgroundColor"
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (isSwipeableMode) Color.White else WisdomCharcoal,
        animationSpec = tween(300),
        label = "contentColor"
    )
    
    val rotation by animateFloatAsState(
        targetValue = if (isSwipeableMode) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "rotation"
    )

    Surface(
        modifier = modifier
            .size(56.dp),
        shape = CircleShape,
        color = backgroundColor,
        shadowElevation = 8.dp,
        onClick = { onToggle(!isSwipeableMode) }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = if (isSwipeableMode) Icons.AutoMirrored.Filled.ViewList else Icons.Filled.SwapHoriz,
                contentDescription = if (isSwipeableMode) "Modo Lista" else "Modo Swipe",
                tint = contentColor,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer { 
                        rotationY = rotation
                    }
            )
        }
    }
}

@Composable
private fun ModeIndicator(
    isSwipeableMode: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = tween(600, delayMillis = 300)
        ) + fadeIn(animationSpec = tween(600, delayMillis = 300)),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.9f),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = if (isSwipeableMode) WisdomGold else WisdomSuccess,
                            shape = CircleShape
                        )
                )
                
                Text(
                    text = if (isSwipeableMode) "Swipe" else "Clásico",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = WisdomCharcoal
                )
            }
        }
    }
}

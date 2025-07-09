package com.albertowisdom.wisdomspark.presentation.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.albertowisdom.wisdomspark.presentation.ui.theme.*

/**
 * FloatingActionButton premium con animaciones spring y efectos visuales
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WisdomFloatingActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    size: Dp = 56.dp,
    containerColor: Color = WisdomGold,
    contentColor: Color = WisdomCharcoal,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val haptic = LocalHapticFeedback.current
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Animaciones con spring physics
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fabScale"
    )
    
    val elevation by animateFloatAsState(
        targetValue = if (isPressed) 4.dp.value else 12.dp.value,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fabElevation"
    )
    
    val glowAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.4f else 0.2f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fabGlow"
    )
    
    // Gradiente del botón
    val fabGradient = Brush.radialGradient(
        colors = listOf(
            containerColor.copy(alpha = 1f),
            containerColor.copy(alpha = 0.9f),
            containerColor.copy(alpha = 0.95f)
        ),
        radius = size.value * 1.2f
    )
    
    // Glow effect
    val glowGradient = Brush.radialGradient(
        colors = listOf(
            containerColor.copy(alpha = glowAlpha),
            Color.Transparent
        ),
        radius = size.value * 1.5f
    )
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Glow background
        Box(
            modifier = Modifier
                .size(size + 16.dp)
                .clip(RoundedCornerShape(50))
                .background(glowGradient)
        )
        
        // Main FAB
        Surface(
            onClick = {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                onClick()
            },
            modifier = Modifier
                .size(size)
                .scale(scale),
            shape = RoundedCornerShape(50),
            color = Color.Transparent,
            shadowElevation = elevation.dp,
            border = BorderStroke(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.3f),
                        Color.White.copy(alpha = 0.1f)
                    )
                )
            ),
            interactionSource = interactionSource
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(fabGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Extended FAB con texto y animaciones elegantes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WisdomExtendedFloatingActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    expanded: Boolean = true,
    containerColor: Color = WisdomGold,
    contentColor: Color = WisdomCharcoal,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val haptic = LocalHapticFeedback.current
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Animaciones
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "extendedFabScale"
    )
    
    val width by animateDpAsState(
        targetValue = if (expanded) 160.dp else 56.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "extendedFabWidth"
    )
    
    // Gradientes
    val fabGradient = Brush.linearGradient(
        colors = listOf(
            containerColor,
            containerColor.copy(alpha = 0.9f),
            containerColor
        )
    )
    
    Surface(
        onClick = {
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier
            .height(56.dp)
            .width(width)
            .scale(scale),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        shadowElevation = 8.dp,
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.3f),
                    Color.White.copy(alpha = 0.1f)
                )
            )
        ),
        interactionSource = interactionSource
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(fabGradient),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
                
                AnimatedVisibility(
                    visible = expanded,
                    enter = slideInHorizontally(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) + fadeIn(),
                    exit = slideOutHorizontally() + fadeOut()
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelLarge,
                        color = contentColor
                    )
                }
            }
        }
    }
}

/**
 * Mini FAB con animaciones spring
 */
@Composable
fun WisdomMiniFloatingActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    containerColor: Color = WisdomGold,
    contentColor: Color = WisdomCharcoal,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    WisdomFloatingActionButton(
        onClick = onClick,
        icon = icon,
        modifier = modifier,
        contentDescription = contentDescription,
        size = 40.dp,
        containerColor = containerColor,
        contentColor = contentColor,
        interactionSource = interactionSource
    )
}

/**
 * FAB con badge animado
 */
@Composable
fun WisdomBadgedFloatingActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    badgeCount: Int,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    showBadge: Boolean = badgeCount > 0,
    containerColor: Color = WisdomGold,
    contentColor: Color = WisdomCharcoal,
    badgeColor: Color = WisdomCoral
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        WisdomFloatingActionButton(
            onClick = onClick,
            icon = icon,
            contentDescription = contentDescription,
            containerColor = containerColor,
            contentColor = contentColor
        )
        
        // Badge animado
        AnimatedVisibility(
            visible = showBadge,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessHigh
                )
            ) + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Surface(
                modifier = Modifier
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(20.dp),
                shape = RoundedCornerShape(50),
                color = badgeColor,
                border = BorderStroke(
                    width = 2.dp,
                    color = Color.White
                )
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(2.dp)
                    )
                }
            }
        }
    }
}

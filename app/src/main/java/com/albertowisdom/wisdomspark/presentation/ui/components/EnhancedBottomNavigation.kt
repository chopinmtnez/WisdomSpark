package com.albertowisdom.wisdomspark.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.albertowisdom.wisdomspark.presentation.navigation.Screen

/**
 * Enhanced bottom navigation component with smooth animations and haptic feedback
 */
@Composable
fun EnhancedBottomNavigation(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navigationItems.forEach { item ->
                    NavigationItem(
                        item = item,
                        isSelected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(Screen.Home.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NavigationItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "nav_item_scale"
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = spring(
            stiffness = Spring.StiffnessMedium
        ),
        label = "nav_item_color"
    )
    
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (isSelected) 0.15f else 0f,
        animationSpec = spring(
            stiffness = Spring.StiffnessMedium
        ),
        label = "nav_item_bg_alpha"
    )

    Box(
        modifier = Modifier
            .selectable(
                selected = isSelected,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                },
                role = Role.Tab,
                interactionSource = interactionSource,
                indication = null
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background for selected item
        Box(
            modifier = Modifier
                .size(64.dp, 40.dp)
                .clip(RoundedCornerShape(20.dp))
                .alpha(backgroundAlpha)
                .background(contentColor)
        )
        
        // Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scale)
        ) {
            // Emoji or Icon
            Text(
                text = if (isSelected) item.selectedEmoji else item.emoji,
                fontSize = 24.sp,
                color = contentColor
            )
            
            // Label
            Text(
                text = item.title,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = contentColor,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/**
 * Data class for bottom navigation items
 */
private data class BottomNavItem(
    val route: String,
    val title: String,
    val emoji: String,
    val selectedEmoji: String
)

/**
 * List of navigation items
 */
private val navigationItems = listOf(
    BottomNavItem(
        route = Screen.Home.route,
        title = "Inicio",
        emoji = "🏠",
        selectedEmoji = "🏠"
    ),
    BottomNavItem(
        route = Screen.Categories.route,
        title = "Categorías",
        emoji = "📂",
        selectedEmoji = "📁"
    ),
    BottomNavItem(
        route = Screen.Favorites.route,
        title = "Favoritos",
        emoji = "💝",
        selectedEmoji = "❤️"
    ),
    BottomNavItem(
        route = Screen.Settings.route,
        title = "Ajustes",
        emoji = "⚙️",
        selectedEmoji = "⚙️"
    )
)

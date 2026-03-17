package com.albertowisdom.wisdomspark.presentation.ui.components

import android.util.Log
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.albertowisdom.wisdomspark.data.preferences.UserPreferences
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.albertowisdom.wisdomspark.presentation.navigation.Screen
import androidx.compose.ui.res.stringResource
import com.albertowisdom.wisdomspark.R

private const val TAG = "BottomNavigation"

/**
 * Enhanced bottom navigation component with smooth animations and haptic feedback
 */
@Composable
fun EnhancedBottomNavigation(
    navController: NavController,
    modifier: Modifier = Modifier,
    userPreferences: UserPreferences? = null
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Obtener el estado de las preferencias de feedback háptico
    val isHapticEnabled by (userPreferences?.isHapticFeedbackEnabled?.collectAsState(initial = true) ?: remember { mutableStateOf(true) })
    
    // Obtener los items de navegación localizados
    val navigationItems = getNavigationItems()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
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
                                Log.d(TAG, "Navigating from '$currentRoute' to '${item.route}'")
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            } else {
                                Log.d(TAG, "Already on '${item.route}', no navigation needed")
                            }
                        },
                        isHapticEnabled = isHapticEnabled
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
    onClick: () -> Unit,
    isHapticEnabled: Boolean = true
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
                    if (isHapticEnabled) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
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
 * Get navigation items with localized titles
 */
@Composable
private fun getNavigationItems(): List<BottomNavItem> {
    return listOf(
        BottomNavItem(
            route = Screen.Home.route,
            title = stringResource(R.string.today_quote),
            emoji = "🏠",
            selectedEmoji = "🏠"
        ),
        BottomNavItem(
            route = Screen.Categories.route,
            title = stringResource(R.string.categories),
            emoji = "📂",
            selectedEmoji = "📁"
        ),
        BottomNavItem(
            route = Screen.Favorites.route,
            title = stringResource(R.string.favorites),
            emoji = "💝",
            selectedEmoji = "❤️"
        ),
        BottomNavItem(
            route = Screen.Settings.route,
            title = stringResource(R.string.settings),
            emoji = "⚙️",
            selectedEmoji = "⚙️"
        )
    )
}

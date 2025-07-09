package com.albertowisdom.wisdomspark.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.albertowisdom.wisdomspark.presentation.ui.theme.*

/**
 * Shimmer effect premium para loading states
 */
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    isLoading: Boolean = true
) {
    if (!isLoading) return

    val shimmerColors = listOf(
        WisdomChampagne.copy(alpha = 0.6f),
        WisdomGold.copy(alpha = 0.3f),
        WisdomChampagne.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(translateAnim.value, translateAnim.value)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(brush)
    )
}

/**
 * Skeleton screen para QuoteCard
 */
@Composable
fun QuoteCardSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Category skeleton
            ShimmerEffect(
                modifier = Modifier
                    .width(80.dp)
                    .height(24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quote text skeleton
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) {
                    ShimmerEffect(
                        modifier = Modifier
                            .fillMaxWidth(if (it == 2) 0.7f else 1f)
                            .height(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Author skeleton
            ShimmerEffect(
                modifier = Modifier
                    .width(120.dp)
                    .height(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons skeleton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(2) {
                    ShimmerEffect(
                        modifier = Modifier
                            .size(48.dp)
                    )
                }
            }
        }
    }
}

/**
 * Skeleton para lista de categorías
 */
@Composable
fun CategoryGridSkeleton(
    modifier: Modifier = Modifier,
    itemCount: Int = 6
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Título skeleton
        ShimmerEffect(
            modifier = Modifier
                .width(200.dp)
                .height(32.dp)
        )

        // Grid skeleton
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            for (i in 0 until itemCount step 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(2) { index ->
                        if (i + index < itemCount) {
                            ShimmerEffect(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(120.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Skeleton para lista de favoritos
 */
@Composable
fun FavoriteItemSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShimmerEffect(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(16.dp)
                )
                ShimmerEffect(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(14.dp)
                )
                ShimmerEffect(
                    modifier = Modifier
                        .width(80.dp)
                        .height(12.dp)
                )
            }

            ShimmerEffect(
                modifier = Modifier
                    .size(40.dp)
            )
        }
    }
}
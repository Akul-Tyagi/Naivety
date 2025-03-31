package com.abundance.naivety.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RatingBar(
    rating: Float,
    modifier: Modifier = Modifier,
    starSize: Int = 24,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(5) { index ->
            val isSelected = index < rating
            val starColor by animateColorAsState(
                targetValue = if (isSelected) tint else Color.Gray.copy(alpha = 0.5f),
                label = "starColor"
            )
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.1f else 0.9f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "starScale"
            )

            Icon(
                imageVector = if (isSelected) Icons.Default.Star else Icons.Default.StarOutline,
                contentDescription = "Rating ${index + 1}",
                tint = starColor,
                modifier = Modifier
                    .size(starSize.dp)
                    .scale(scale)
                    .padding(2.dp)
            )
        }
    }
}

@Composable
fun RatingSection(
    rating: Float,
    onRatingChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
    interactive: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.primary,
    starSize: Int = 24
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(5) { index ->
            if (interactive) {
                val isSelected = index < rating
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.1f else 0.9f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "interactiveStarScale"
                )

                IconButton(
                    onClick = { onRatingChanged(index + 1f) },
                    modifier = Modifier.size(starSize.dp)
                ) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.Star else Icons.Default.StarOutline,
                        contentDescription = "Rate ${index + 1}",
                        tint = if (isSelected) tint else Color.Gray.copy(alpha = 0.5f),
                        modifier = Modifier.scale(scale)
                    )
                }
            } else {
                Icon(
                    imageVector = if (index < rating) Icons.Default.Star else Icons.Default.StarOutline,
                    contentDescription = "Rating ${index + 1}",
                    tint = tint,
                    modifier = Modifier.size(starSize.dp)
                )
            }
        }
    }
}

@Composable
fun BookRatingSection(
    averageRating: Float,
    ratingsCount: Int,
    onRatingChanged: (Float) -> Unit = {},
    modifier: Modifier = Modifier,
    interactive: Boolean = false
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (ratingsCount > 0) {
                Text(
                    text = if (interactive) "Rate This Book (Soon)" else "Book Rating",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background( color = MaterialTheme.colorScheme.surface)
                        .padding(vertical = 12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (interactive) {
                            RatingSection(
                                rating = averageRating,
                                onRatingChanged = onRatingChanged,
                                starSize = 30,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            RatingBar(
                                rating = averageRating,
                                starSize = 26,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = "$averageRating ($ratingsCount ${if (ratingsCount == 1) "rating" else "ratings"})",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp),
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                Text(
                    text = if (interactive) "Be the First to Rate" else "No Ratings Yet",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (interactive) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background( color = MaterialTheme.colorScheme.onSurface)
                            .padding(vertical = 12.dp)
                    ) {
                        RatingSection(
                            rating = 0f,
                            onRatingChanged = onRatingChanged,
                            starSize = 30,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                } else {
                    Text(
                        text = "No ratings available yet",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
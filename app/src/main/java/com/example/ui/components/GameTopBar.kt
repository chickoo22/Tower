package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.DifficultyMode
import kotlinx.coroutines.delay

@Composable
fun GameTopBar(
    score: Int,
    allTimeBest: Int,
    isNewRecord: Boolean = false,
    onBack: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") currentMode: DifficultyMode = DifficultyMode.HARD
) {
    val displayBest = maxOf(score, allTimeBest)

    // Animated score punch on increment
    var lastScore by remember { mutableIntStateOf(score) }
    var punchTrigger by remember { mutableStateOf(false) }

    LaunchedEffect(score) {
        if (score > lastScore) {
            punchTrigger = true
            lastScore = score
            delay(140)
            punchTrigger = false
        } else {
            lastScore = score
        }
    }

    val scoreScale by animateFloatAsState(
        targetValue = if (punchTrigger) 1.25f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "score_pop_anim"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Circular White Back Button (<)
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(6.dp, shape = CircleShape, spotColor = Color(0x4D000000))
                .clip(CircleShape)
                .background(Color.White)
                .clickable(onClick = onBack)
                .testTag("game_back_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                contentDescription = "Back",
                tint = Color(0xFF1E293B),
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 2.dp)
            )
        }

        // Center: Two Dark Glassmorphic Badges (SCORE & BEST HIGH SCORE)
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Current Score badge
            Box(
                modifier = Modifier
                    .width(116.dp)
                    .height(52.dp)
                    .shadow(8.dp, shape = RoundedCornerShape(16.dp), spotColor = Color(0x55000000))
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF1E1738).copy(alpha = 0.94f),
                                Color(0xFF0F0B1E).copy(alpha = 0.97f)
                            )
                        )
                    )
                    .border(
                        BorderStroke(
                            1.2.dp,
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.28f),
                                    Color.White.copy(alpha = 0.08f)
                                )
                            )
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0xFFA5B4FC),
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "SCORE",
                            color = Color(0xFFC7D2FE),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp
                        )
                    }
                    Text(
                        text = "$score",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 25.sp,
                        modifier = Modifier.scale(scoreScale)
                    )
                }
            }

            // High Score badge with Trophy and Vibrant Record accent
            Box(
                modifier = Modifier
                    .width(116.dp)
                    .height(52.dp)
                    .shadow(8.dp, shape = RoundedCornerShape(16.dp), spotColor = Color(0x55000000))
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isNewRecord) {
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF382305).copy(alpha = 0.95f),
                                    Color(0xFF1F1102).copy(alpha = 0.98f)
                                )
                            )
                        } else {
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF28132A).copy(alpha = 0.94f),
                                    Color(0xFF130917).copy(alpha = 0.97f)
                                )
                            )
                        }
                    )
                    .border(
                        if (isNewRecord) {
                            BorderStroke(
                                1.5.dp,
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFFFFD700),
                                        Color(0xFFFF9100),
                                        Color(0xFFFFD700)
                                    )
                                )
                            )
                        } else {
                            BorderStroke(
                                1.2.dp,
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFFFF4081).copy(alpha = 0.45f),
                                        Color.White.copy(alpha = 0.10f)
                                    )
                                )
                            )
                        },
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            tint = if (isNewRecord) Color(0xFFFFD700) else Color(0xFFFF4081),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isNewRecord) "NEW RECORD" else "BEST",
                            color = if (isNewRecord) Color(0xFFFFE066) else Color(0xFFFF7597),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        )
                    }
                    Text(
                        text = "$displayBest",
                        color = if (isNewRecord) Color(0xFFFFD700) else Color(0xFFFF7597),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 25.sp
                    )
                }
            }
        }

        // Right: Circular White Reload/Restart Button (⟳)
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(6.dp, shape = CircleShape, spotColor = Color(0x4D000000))
                .clip(CircleShape)
                .background(Color.White)
                .clickable(onClick = onRestart)
                .testTag("game_restart_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Restart",
                tint = Color(0xFF1E293B),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


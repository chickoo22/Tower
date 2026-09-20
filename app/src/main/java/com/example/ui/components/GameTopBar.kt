package com.example.ui.components

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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.DifficultyMode

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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Circular White Back Button (<) matching reference screenshot
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable(onClick = onBack)
                .testTag("game_back_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                contentDescription = "Back",
                tint = Color(0xFF222B38),
                modifier = Modifier
                    .size(19.dp)
                    .padding(end = 2.dp)
            )
        }

        // Center: Two Dark Rounded Badges (SCORE & THIS WEEK High Score)
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Current Score badge
            Box(
                modifier = Modifier
                    .width(108.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF161129).copy(alpha = 0.88f))
                    .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "SCORE",
                        color = Color.White.copy(alpha = 0.80f),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "$score",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 23.sp
                    )
                }
            }

            // High Score badge with Crown and Vibrant Pink text matching reference screenshot
            Box(
                modifier = Modifier
                    .width(108.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF161129).copy(alpha = 0.88f))
                    .then(
                        if (isNewRecord) Modifier.background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFFFF4081).copy(alpha = 0.35f),
                                    Color(0xFFFFD700).copy(alpha = 0.35f)
                                )
                            )
                        ) else Modifier
                    )
                    .border(
                        1.dp,
                        if (isNewRecord) Color(0xFFFFD700).copy(alpha = 0.7f) else Color.White.copy(alpha = 0.10f),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp),
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
                            text = if (isNewRecord) "NEW RECORD" else "THIS WEEK",
                            color = if (isNewRecord) Color(0xFFFFD700) else Color(0xFFFF4081),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = "$displayBest",
                        color = if (isNewRecord) Color(0xFFFFD700) else Color(0xFFFF4081),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 23.sp
                    )
                }
            }
        }

        // Right: Circular White Reload/Restart Button (⟳) matching reference screenshot
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable(onClick = onRestart)
                .testTag("game_restart_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Restart",
                tint = Color(0xFF222B38),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


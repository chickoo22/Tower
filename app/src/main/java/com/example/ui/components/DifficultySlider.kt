package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.engine.DifficultyMode

@Composable
fun DifficultySlider(
    currentMode: DifficultyMode,
    onModeSelected: (DifficultyMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = listOf(
        DifficultyMode.EASY,
        DifficultyMode.MEDIUM,
        DifficultyMode.HARD,
        DifficultyMode.EXTRA_HARD
    )
    val targetIndex = modes.indexOf(currentMode).coerceAtLeast(0).toFloat()

    val animatedFraction by animateFloatAsState(
        targetValue = targetIndex / (modes.size - 1).toFloat(),
        animationSpec = spring(stiffness = 500f, dampingRatio = 0.85f),
        label = "slider_fraction"
    )

    val activeColor by animateColorAsState(
        targetValue = currentMode.themeColor,
        label = "slider_color"
    )

    // Outer white capsule pill container matching image.png
    Box(
        modifier = modifier
            .width(260.dp)
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Color.White)
            .padding(horizontal = 6.dp, vertical = 6.dp)
            .testTag("difficulty_slider")
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFEEF2F6)) // Light blue-gray track as in image.png
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val ratio = (offset.x / size.width).coerceIn(0f, 1f)
                        val step = (ratio * modes.size).toInt().coerceIn(0, modes.size - 1)
                        onModeSelected(modes[step])
                    }
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, _ ->
                        val ratio = (change.position.x / size.width).coerceIn(0f, 1f)
                        val step = (ratio * modes.size).toInt().coerceIn(0, modes.size - 1)
                        if (modes[step] != currentMode) {
                            onModeSelected(modes[step])
                        }
                    }
                }
        ) {
            val totalWidth = maxWidth
            val knobSize = 38.dp
            val travelDistance = totalWidth - knobSize

            // Left filled active track in difficulty color
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(knobSize / 2f + travelDistance * animatedFraction)
                    .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                    .background(activeColor)
            )

            // Circular knob with crisp white outline ring matching image.png
            Box(
                modifier = Modifier
                    .offset(x = travelDistance * animatedFraction)
                    .size(knobSize)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(3.5.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(activeColor)
                )
            }
        }
    }
}

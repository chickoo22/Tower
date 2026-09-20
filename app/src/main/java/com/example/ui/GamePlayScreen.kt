package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.DifficultyMode
import com.example.engine.TowerEngine
import com.example.ui.components.GameCanvas
import com.example.ui.components.GameOverOverlay
import com.example.ui.components.GameTopBar
import com.example.ui.components.RewardedAdOverlay

@Composable
fun GamePlayScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val engine = viewModel.engine
    val score by engine.score.collectAsState()
    val combo by engine.combo.collectAsState()
    val gamePhase by engine.gamePhase.collectAsState()
    val isPerfectFlash by engine.isPerfectFlash.collectAsState()
    val coinsEarned by engine.coinsEarnedThisRun.collectAsState()
    val isNewHighScore by engine.isNewHighScore.collectAsState()
    val currentMode by viewModel.currentMode.collectAsState()
    val globalBest by viewModel.globalBest.collectAsState()
    val adState by viewModel.monetizationManager.adState.collectAsState()

    BackHandler {
        viewModel.restartGame()
    }

    var frameTick by remember { mutableLongStateOf(0L) }

    // 60FPS Game loop
    LaunchedEffect(Unit) {
        var lastTime = System.nanoTime()
        while (true) {
            withFrameNanos { now ->
                val dtNanos = now - lastTime
                lastTime = now
                val dtSec = (dtNanos / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                engine.update(dtSec)
                frameTick = now
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 3D Isometric Canvas
        GameCanvas(
            engine = engine,
            frameTick = frameTick,
            onTap = { viewModel.onScreenTap() }
        )

        // Perfect Stack Flash Effect
        AnimatedVisibility(
            visible = isPerfectFlash,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.25f))
            )
        }

        // Top Bar (Level score & Pink This Week record badge)
        GameTopBar(
            currentMode = currentMode,
            score = score,
            allTimeBest = globalBest,
            isNewRecord = isNewHighScore,
            onBack = { viewModel.backToMenu() },
            onRestart = { viewModel.restartGame() },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
        )

        // Floating Combo / Perfect Score Badge
        AnimatedVisibility(
            visible = combo > 0,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 74.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.90f),
                border = BorderStroke(1.5.dp, if (combo > 2) Color(0xFFFF5C8D) else Color(0xFFFFD152)),
                shadowElevation = 8.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (combo == 1) "✨ PERFECT! ✨" else "🔥 COMBO x$combo! 🔥",
                        color = if (combo > 2) Color(0xFFFF5C8D) else Color(0xFFFFD152),
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }

        // Floating High Score Celebration Badge
        AnimatedVisibility(
            visible = isNewHighScore,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = if (combo > 0) 122.dp else 74.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E1B4B).copy(alpha = 0.92f),
                border = BorderStroke(1.5.dp, Color(0xFFFFD700)),
                shadowElevation = 10.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "👑 NEW HIGH SCORE! 👑",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }

        // Game Over Overlay
        GameOverOverlay(
            gamePhase = gamePhase,
            score = score,
            highScore = globalBest,
            coinsEarned = coinsEarned,
            currentMode = currentMode,
            onSaveMe = { viewModel.triggerSaveMeWithAd() },
            onNoThanks = { engine.onNoThanksClicked() },
            onWatchAdDoubleCoins = { coins -> viewModel.watchAdForDoubleCoins(coins) },
            onRestart = { viewModel.restartGame() },
            onBackToMenu = { viewModel.backToMenu() }
        )

        // Rewarded Video Ad Overlay
        RewardedAdOverlay(
            adState = adState,
            onDismissEarly = { viewModel.monetizationManager.dismissAdEarly() }
        )
    }
}

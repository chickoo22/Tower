package com.example.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.DifficultyMode
import com.example.ui.components.DifficultyAvatar
import com.example.ui.components.DifficultySlider
import com.example.ui.components.LeaderboardDialog
import com.example.ui.components.PrivacyPolicyDialog
import com.example.ui.components.RewardedAdOverlay
import com.example.ui.components.SettingsDialog
import com.example.ui.components.ShopDialog

@Composable
fun MenuScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentMode by viewModel.currentMode.collectAsState()
    val profile by viewModel.playerProfile.collectAsState()
    val easyBest by viewModel.easyBest.collectAsState()
    val mediumBest by viewModel.mediumBest.collectAsState()
    val hardBest by viewModel.hardBest.collectAsState()
    val extraHardBest by viewModel.extraHardBest.collectAsState()
    val globalBest by viewModel.globalBest.collectAsState()
    val topScores by viewModel.topScores.collectAsState()
    val adState by viewModel.monetizationManager.adState.collectAsState()

    val showShop by viewModel.showShopDialog.collectAsState()
    val showSettings by viewModel.showSettingsDialog.collectAsState()
    val showPrivacy by viewModel.showPrivacyDialog.collectAsState()
    val showLeaderboard by viewModel.showLeaderboardDialog.collectAsState()

    var showHelpDialog by remember { mutableStateOf(false) }

    val activeColor by animateColorAsState(
        targetValue = currentMode.themeColor,
        label = "menu_btn_color"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0C121E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Section matching image.png (Dark Midnight Navy Header)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Row: Settings (Left), Shop Pill (Center), Leaderboard (Right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Settings Gear Button (Circular white with pink gear icon matching image.png)
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { viewModel.setShowSettings(true) }
                            .testTag("menu_settings_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFFF43F5E), // Vibrant pink
                            modifier = Modifier.size(23.dp)
                        )
                    }

                    // Shop Pill with Pink Outline (matching image.png)
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = Color.White,
                        border = BorderStroke(1.5.dp, Color(0xFFF43F5E)),
                        modifier = Modifier
                            .clickable { viewModel.setShowShop(true) }
                            .testTag("menu_coins_badge")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(text = "🛍️", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Shop (${profile.coins})",
                                color = Color(0xFFF43F5E),
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Leaderboard Button (Circular white with grid icon matching image.png)
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { viewModel.setShowLeaderboard(true) }
                            .testTag("menu_star_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = "Leaderboard",
                            tint = Color(0xFF334155),
                            modifier = Modifier.size(23.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title: TOWER BUILDER (matching image.png)
                Text(
                    text = "TOWER BUILDER",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Subtitle description (matching image.png)
                Text(
                    text = "Time your taps to stack the slabs. Stack as high as you can to build the tallest skyscraper!",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3 Level preview blocks matching layout in image.png
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("1", "61", "181").forEachIndexed { idx, lvl ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E293B))
                                .border(
                                    width = 1.dp,
                                    color = if (idx == 2) activeColor else Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "L$lvl",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (idx == 2) activeColor else Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main White Bottom Sheet Panel (matching image.png)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp))
                    .background(Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Monster Avatar matching image.png
                        DifficultyAvatar(
                            mode = currentMode,
                            sizeDp = 115.dp,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Difficulty Title: e.g. "EXTRA HARD" (matching image.png)
                        Text(
                            text = currentMode.title,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = activeColor,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Level Range: e.g. "Levels 181 - 240" (matching image.png)
                        Text(
                            text = currentMode.levelRange,
                            color = Color(0xFF64748B),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Custom Slider matching image.png
                        DifficultySlider(
                            currentMode = currentMode,
                            onModeSelected = { viewModel.setDifficultyMode(it) }
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Daily Challenge Card (matching image.png layout & colors)
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color(0xFFFFFBEB),
                            border = BorderStroke(1.5.dp, Color(0xFFF59E0B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFEF3C7)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "🏆", fontSize = 18.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "DAILY CHALLENGE",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp,
                                            color = Color(0xFF1E293B)
                                        )
                                        Text(
                                            text = "Today's Tower • +100 Coins",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                Button(
                                    onClick = { viewModel.startGame() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(38.dp)
                                ) {
                                    Text(
                                        text = "PLAY",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Bottom Action Row: [PLAY] and [?] (matching image.png)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Main PLAY Button with Level & Mode Subtitle
                        Button(
                            onClick = { viewModel.startGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = activeColor),
                            shape = RoundedCornerShape(22.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(68.dp)
                                .testTag("play_button")
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "PLAY",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Level ${currentMode.levelNumber} • ${currentMode.displayHeader}",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.82f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // "?" Help Button
                        Button(
                            onClick = { showHelpDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C5EF8)),
                            shape = RoundedCornerShape(22.dp),
                            modifier = Modifier
                                .size(68.dp)
                                .testTag("help_button")
                        ) {
                            Text(
                                text = "?",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Dialogs
        if (showShop) {
            ShopDialog(
                profile = profile,
                onDismiss = { viewModel.setShowShop(false) },
                onSelectTheme = { theme -> viewModel.selectTheme(theme) },
                onUnlockTheme = { theme -> viewModel.purchaseTheme(theme) },
                onWatchAdForCoins = { viewModel.watchAdForBonusCoins() },
                onUnlockVipPass = { viewModel.unlockVipPass() }
            )
        }

        if (showSettings) {
            SettingsDialog(
                profile = profile,
                onToggleSound = { viewModel.toggleSound() },
                onToggleHaptics = { viewModel.toggleHaptics() },
                onOpenPrivacyPolicy = {
                    viewModel.setShowSettings(false)
                    viewModel.setShowPrivacy(true)
                },
                onDismiss = { viewModel.setShowSettings(false) }
            )
        }

        if (showPrivacy) {
            PrivacyPolicyDialog(
                onDismiss = { viewModel.setShowPrivacy(false) }
            )
        }

        if (showLeaderboard) {
            LeaderboardDialog(
                globalBest = globalBest,
                easyBest = easyBest,
                mediumBest = mediumBest,
                hardBest = hardBest,
                extraHardBest = extraHardBest,
                topScores = topScores,
                onDismiss = { viewModel.setShowLeaderboard(false) }
            )
        }

        // How to play dialog
        if (showHelpDialog) {
            AlertDialog(
                onDismissRequest = { showHelpDialog = false },
                title = {
                    Text(
                        text = "How to Play",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("• Tap anywhere on the screen when the moving block lines up with the tower.")
                        Text("• Harder levels move faster; speed also increases as the tower grows taller!")
                        Text("• Slices will trim any overhang that falls outside the tower.")
                        Text("• Perfect placements create melodic notes and earn bonus coins.")
                        Text("• Use earned coins in the Shop to unlock Cyberpunk, Sunset, and Galaxy themes!")
                        Text("• Watch a sponsored video if you miss for a 'Save Me' extra life!")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showHelpDialog = false }) {
                        Text("GOT IT!", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // Rewarded Video Ad Overlay
        RewardedAdOverlay(
            adState = adState,
            onDismissEarly = { viewModel.monetizationManager.dismissAdEarly() }
        )
    }
}

@Composable
fun MenuScreen(
    currentMode: DifficultyMode,
    onModeChange: (DifficultyMode) -> Unit,
    onPlayClick: () -> Unit,
    easyBest: Int,
    mediumBest: Int,
    hardBest: Int,
    totalGames: Int,
    modifier: Modifier = Modifier
) {
    var showHelpDialog by remember { mutableStateOf(false) }
    var showStatsDialog by remember { mutableStateOf(false) }

    val activeColor by animateColorAsState(
        targetValue = currentMode.themeColor,
        label = "menu_btn_color"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF533B78),
                        Color(0xFF6A4C93),
                        Color(0xFF8B64B8),
                        Color(0xFFFBF7EE)
                    ),
                    startY = 0f,
                    endY = 750f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.95f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(text = "🪙", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "50",
                            color = Color(0xFFD97706),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TOWER",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Time your taps to stack blocks. Build the tallest tower you can!",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { showStatsDialog = true }
                        .testTag("menu_star_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Stats",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(Color(0xFFFBF7EE))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DifficultyAvatar(
                            mode = currentMode,
                            sizeDp = 115.dp,
                            modifier = Modifier.padding(top = 10.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = currentMode.title,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = activeColor,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        DifficultySlider(
                            currentMode = currentMode,
                            onModeSelected = onModeChange
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 28.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onPlayClick,
                            colors = ButtonDefaults.buttonColors(containerColor = activeColor),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .testTag("play_button")
                        ) {
                            Text(
                                text = "PLAY",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Button(
                            onClick = { showHelpDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7D5BA6)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .size(64.dp)
                                .testTag("help_button")
                        ) {
                            Text(
                                text = "?",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("How to Play", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("• Tap anywhere on the screen when the moving block lines up with the tower.")
                    Text("• Slices will trim any overhang that falls outside the tower.")
                    Text("• Perfect placements create musical chords and reward combos.")
                    Text("• High combos can expand your block back!")
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("GOT IT!", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showStatsDialog) {
        LeaderboardDialog(
            globalBest = maxOf(easyBest, mediumBest, hardBest),
            easyBest = easyBest,
            mediumBest = mediumBest,
            hardBest = hardBest,
            topScores = emptyList(),
            onDismiss = { showStatsDialog = false }
        )
    }
}

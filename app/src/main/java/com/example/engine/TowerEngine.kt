package com.example.engine

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class TowerEngine(
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager,
    private val onGameOverRecorded: (mode: DifficultyMode, score: Int, coinsEarned: Int) -> Unit
) {
    var mode: DifficultyMode = DifficultyMode.EASY
        private set

    var currentTheme: GameTheme = GameTheme.CLASSIC
        private set

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _combo = MutableStateFlow(0)
    val combo: StateFlow<Int> = _combo.asStateFlow()

    private val _coinsEarnedThisRun = MutableStateFlow(0)
    val coinsEarnedThisRun: StateFlow<Int> = _coinsEarnedThisRun.asStateFlow()

    private val _gamePhase = MutableStateFlow<GamePhase>(GamePhase.Playing)
    val gamePhase: StateFlow<GamePhase> = _gamePhase.asStateFlow()

    private val _isPerfectFlash = MutableStateFlow(false)
    val isPerfectFlash: StateFlow<Boolean> = _isPerfectFlash.asStateFlow()

    private val _isNewHighScore = MutableStateFlow(false)
    val isNewHighScore: StateFlow<Boolean> = _isNewHighScore.asStateFlow()

    // Tower blocks list
    val stackedBlocks = mutableListOf<Block3D>()
    val fallingPieces = mutableListOf<FallingPiece3D>()
    val comboParticles = mutableListOf<ComboParticle>()
    val shootingStars = mutableListOf<ShootingStar>()
    val confettiParticles = mutableListOf<ConfettiParticle>()
    val shockwaveRings = mutableListOf<ShockwaveRing>()

    // Active moving block
    var movingAxis: Axis = Axis.Y
    var movingDir: Float = 1f
    var movingOffset: Float = 0f
    var movingSizeX: Float = 180f
    var movingSizeY: Float = 180f
    var movingZTop: Float = 13f
    var movingColor: Color = Color.Magenta
    private var oscillationPhase: Float = -Math.PI.toFloat() / 2f

    // Camera
    var cameraY: Float = 0f
    var targetCameraY: Float = 0f

    // High score tracking
    var previousHighScore: Int = 0
        private set
    var hasTriggeredHighScoreConfetti: Boolean = false
        private set

    // Save Me state
    var hasUsedSaveMe: Boolean = false

    val blockHeight = 24f
    private var baseTravelRange = 385f

    init {
        startNewGame(DifficultyMode.HARD, GameTheme.CLASSIC, 0)
    }

    fun startNewGame(
        difficulty: DifficultyMode,
        theme: GameTheme = GameTheme.CLASSIC,
        initialHighScore: Int = 0
    ) {
        mode = difficulty
        currentTheme = theme
        previousHighScore = initialHighScore
        hasTriggeredHighScoreConfetti = false
        _isNewHighScore.value = false
        _score.value = 0
        _combo.value = 0
        _coinsEarnedThisRun.value = 0
        _gamePhase.value = GamePhase.Playing
        _isPerfectFlash.value = false
        hasUsedSaveMe = false

        stackedBlocks.clear()
        fallingPieces.clear()
        comboParticles.clear()
        shootingStars.clear()
        confettiParticles.clear()
        shockwaveRings.clear()

        val initialSize = 310f // Generous, bold tower size matching reference screenshot
        // Grounded monolith base pillar (starts at z = 0 and extends deep down into the mist)
        val baseColor = ColorPaletteGenerator.getColorForLayer(0, currentTheme)
        val basePillar = Block3D(
            cx = 0f,
            cy = 0f,
            sizeX = initialSize,
            sizeY = initialSize,
            zTop = 0f,
            height = 1500f, // Deep grounded foundation pillar extending well off screen
            baseColor = baseColor
        )
        stackedBlocks.add(basePillar)

        // Setup first moving block on layer 1 directly on top of the base pillar
        movingAxis = Axis.Y // Alternates: Y (right diagonal), then X (left diagonal)
        movingDir = 1f
        movingSizeX = initialSize
        movingSizeY = initialSize
        movingZTop = blockHeight
        movingColor = ColorPaletteGenerator.getColorForLayer(1, currentTheme)
        baseTravelRange = initialSize * 1.15f + 30f
        oscillationPhase = -Math.PI.toFloat() / 2f
        movingOffset = -baseTravelRange

        cameraY = 0f
        targetCameraY = 0f
    }

    fun setTheme(theme: GameTheme) {
        currentTheme = theme
    }

    fun setPreviousHighScore(highScore: Int) {
        previousHighScore = highScore
    }

    fun onScreenTap() {
        if (_gamePhase.value != GamePhase.Playing) return
        val topBlock = stackedBlocks.lastOrNull() ?: return

        if (movingAxis == Axis.X) {
            val delta = movingOffset // offset from topBlock.cx
            val tolerance = mode.perfectTolerance

            if (abs(delta) <= tolerance) {
                handlePerfectStack(topBlock)
            } else if (abs(delta) < topBlock.sizeX) {
                handleSlicedStackX(topBlock, delta)
            } else {
                handleMiss()
            }
        } else { // Axis.Y
            val delta = movingOffset // offset from topBlock.cy
            val tolerance = mode.perfectTolerance

            if (abs(delta) <= tolerance) {
                handlePerfectStack(topBlock)
            } else if (abs(delta) < topBlock.sizeY) {
                handleSlicedStackY(topBlock, delta)
            } else {
                handleMiss()
            }
        }
    }

    private fun handlePerfectStack(topBlock: Block3D) {
        val newScore = _score.value + 1
        _score.value = newScore
        val newCombo = _combo.value + 1
        _combo.value = newCombo

        // Coin reward for perfect stack
        val coinsAdded = mode.coinMultiplier * 2
        _coinsEarnedThisRun.value += coinsAdded

        soundManager.playStackSound(newCombo)
        hapticManager.triggerPerfect()
        _isPerfectFlash.value = true

        // Check if player broke their personal high score!
        checkHighScoreCelebration(newScore)

        // Combo bonus restores block size
        var newSizeX = topBlock.sizeX
        var newSizeY = topBlock.sizeY
        if (newCombo >= 6 && newCombo % 3 == 0) {
            val maxLimit = mode.initialSize
            if (newSizeX < maxLimit) newSizeX = (newSizeX + 14f).coerceAtMost(maxLimit)
            if (newSizeY < maxLimit) newSizeY = (newSizeY + 14f).coerceAtMost(maxLimit)
        }

        val placedBlock = Block3D(
            cx = topBlock.cx,
            cy = topBlock.cy,
            sizeX = newSizeX,
            sizeY = newSizeY,
            zTop = movingZTop,
            height = blockHeight,
            baseColor = movingColor
        )
        stackedBlocks.add(placedBlock)

        // Spawn expanding shockwave ring on the placed block's perimeter
        shockwaveRings.add(
            ShockwaveRing(
                cx = placedBlock.cx,
                cy = placedBlock.cy,
                zTop = placedBlock.zTop,
                sizeX = placedBlock.sizeX,
                sizeY = placedBlock.sizeY,
                color = Color.White
            )
        )

        // Add visual text particle
        comboParticles.add(
            ComboParticle(
                cx = placedBlock.cx,
                cy = placedBlock.cy,
                zTop = placedBlock.zTop + 10f,
                text = if (newCombo > 1) "PERFECT! x$newCombo (+${coinsAdded}🪙)" else "PERFECT! (+${coinsAdded}🪙)"
            )
        )

        prepareNextMovingBlock(placedBlock)
    }

    private fun handleSlicedStackX(topBlock: Block3D, delta: Float) {
        val newScore = _score.value + 1
        _score.value = newScore
        _combo.value = 0

        val coinsAdded = mode.coinMultiplier
        _coinsEarnedThisRun.value += coinsAdded

        soundManager.playSliceSound()
        hapticManager.triggerSlice()

        // Check if player broke their personal high score!
        checkHighScoreCelebration(newScore)

        val overhangSize = abs(delta)
        val overlapSize = topBlock.sizeX - overhangSize

        if (overlapSize < 4f) {
            handleMiss()
            return
        }

        val overlapCx = topBlock.cx + (delta / 2f)
        val placedBlock = Block3D(
            cx = overlapCx,
            cy = topBlock.cy,
            sizeX = overlapSize,
            sizeY = topBlock.sizeY,
            zTop = movingZTop,
            height = blockHeight,
            baseColor = movingColor
        )
        stackedBlocks.add(placedBlock)

        // Create falling overhang with masonry physics
        val overhangCx = if (delta > 0) {
            overlapCx + (overlapSize / 2f) + (overhangSize / 2f)
        } else {
            overlapCx - (overlapSize / 2f) - (overhangSize / 2f)
        }

        fallingPieces.add(
            FallingPiece3D(
                cx = overhangCx,
                cy = topBlock.cy,
                sizeX = overhangSize,
                sizeY = topBlock.sizeY,
                zTop = movingZTop,
                height = blockHeight,
                baseColor = movingColor,
                vz = -1.2f,
                vx = if (delta > 0) 0.55f else -0.55f,
                vy = 0f,
                rotationSpeed = if (delta > 0) 0.032f else -0.032f
            )
        )

        prepareNextMovingBlock(placedBlock)
    }

    private fun handleSlicedStackY(topBlock: Block3D, delta: Float) {
        val newScore = _score.value + 1
        _score.value = newScore
        _combo.value = 0

        val coinsAdded = mode.coinMultiplier
        _coinsEarnedThisRun.value += coinsAdded

        soundManager.playSliceSound()
        hapticManager.triggerSlice()

        // Check if player broke their personal high score!
        checkHighScoreCelebration(newScore)

        val overhangSize = abs(delta)
        val overlapSize = topBlock.sizeY - overhangSize

        if (overlapSize < 4f) {
            handleMiss()
            return
        }

        val overlapCy = topBlock.cy + (delta / 2f)
        val placedBlock = Block3D(
            cx = topBlock.cx,
            cy = overlapCy,
            sizeX = topBlock.sizeX,
            sizeY = overlapSize,
            zTop = movingZTop,
            height = blockHeight,
            baseColor = movingColor
        )
        stackedBlocks.add(placedBlock)

        // Create falling overhang with masonry physics
        val overhangCy = if (delta > 0) {
            overlapCy + (overlapSize / 2f) + (overhangSize / 2f)
        } else {
            overlapCy - (overlapSize / 2f) - (overhangSize / 2f)
        }

        fallingPieces.add(
            FallingPiece3D(
                cx = topBlock.cx,
                cy = overhangCy,
                sizeX = topBlock.sizeX,
                sizeY = overhangSize,
                zTop = movingZTop,
                height = blockHeight,
                baseColor = movingColor,
                vz = -1.2f,
                vx = 0f,
                vy = if (delta > 0) 0.55f else -0.55f,
                rotationSpeed = if (delta > 0) 0.032f else -0.032f
            )
        )

        prepareNextMovingBlock(placedBlock)
    }

    private fun checkHighScoreCelebration(newScore: Int) {
        if (!hasTriggeredHighScoreConfetti) {
            val isRecordBreak = (previousHighScore > 0 && newScore > previousHighScore) ||
                                (previousHighScore == 0 && newScore >= 5)
            if (isRecordBreak) {
                hasTriggeredHighScoreConfetti = true
                _isNewHighScore.value = true
                soundManager.playHighScoreSound()
                hapticManager.triggerHighScoreCelebration()
                triggerHighScoreConfetti()
            }
        }
    }

    fun triggerHighScoreConfetti(width: Float = 1080f, height: Float = 2200f) {
        val celebratoryColors = listOf(
            Color(0xFFFFD700), // Gold
            Color(0xFFFF5C8D), // Neon Pink
            Color(0xFF00F5FF), // Electric Cyan
            Color(0xFFA855F7), // Royal Violet
            Color(0xFF10B981), // Emerald Green
            Color(0xFFFF7A00), // Coral Orange
            Color(0xFFFFFFFF)  // Sparkling White
        )

        // Left cannon burst (shooting up and right)
        for (i in 0 until 40) {
            val color = celebratoryColors[Random.nextInt(celebratoryColors.size)]
            val shape = ConfettiShape.entries[Random.nextInt(ConfettiShape.entries.size)]
            val angle = (-Random.nextFloat() * 0.7f - 0.45f)
            val speed = 14f + Random.nextFloat() * 16f
            confettiParticles.add(
                ConfettiParticle(
                    x = width * 0.12f + Random.nextFloat() * 50f,
                    y = height * 0.88f + Random.nextFloat() * 60f,
                    vx = cos(angle) * speed + Random.nextFloat() * 3f,
                    vy = sin(angle) * speed,
                    color = color,
                    size = 7f + Random.nextFloat() * 8f,
                    shapeType = shape,
                    rotation = Random.nextFloat() * 360f,
                    rotationSpeed = (Random.nextFloat() - 0.5f) * 14f,
                    flutterPhase = Random.nextFloat() * 6.28f,
                    flutterSpeed = 0.12f + Random.nextFloat() * 0.12f,
                    alpha = 1f,
                    maxAge = 240f
                )
            )
        }

        // Right cannon burst (shooting up and left)
        for (i in 0 until 40) {
            val color = celebratoryColors[Random.nextInt(celebratoryColors.size)]
            val shape = ConfettiShape.entries[Random.nextInt(ConfettiShape.entries.size)]
            val angle = (-Math.PI.toFloat() + Random.nextFloat() * 0.7f + 0.45f)
            val speed = 14f + Random.nextFloat() * 16f
            confettiParticles.add(
                ConfettiParticle(
                    x = width * 0.88f - Random.nextFloat() * 50f,
                    y = height * 0.88f + Random.nextFloat() * 60f,
                    vx = cos(angle) * speed - Random.nextFloat() * 3f,
                    vy = sin(angle) * speed,
                    color = color,
                    size = 7f + Random.nextFloat() * 8f,
                    shapeType = shape,
                    rotation = Random.nextFloat() * 360f,
                    rotationSpeed = (Random.nextFloat() - 0.5f) * 14f,
                    flutterPhase = Random.nextFloat() * 6.28f,
                    flutterSpeed = 0.12f + Random.nextFloat() * 0.12f,
                    alpha = 1f,
                    maxAge = 240f
                )
            )
        }

        // Center fountain burst
        for (i in 0 until 35) {
            val color = celebratoryColors[Random.nextInt(celebratoryColors.size)]
            val shape = ConfettiShape.entries[Random.nextInt(ConfettiShape.entries.size)]
            val vx = (Random.nextFloat() - 0.5f) * 12f
            val vy = -17f - Random.nextFloat() * 11f
            confettiParticles.add(
                ConfettiParticle(
                    x = width * 0.5f + (Random.nextFloat() - 0.5f) * 90f,
                    y = height * 0.58f,
                    vx = vx,
                    vy = vy,
                    color = color,
                    size = 6f + Random.nextFloat() * 7f,
                    shapeType = shape,
                    rotation = Random.nextFloat() * 360f,
                    rotationSpeed = (Random.nextFloat() - 0.5f) * 12f,
                    flutterPhase = Random.nextFloat() * 6.28f,
                    flutterSpeed = 0.14f,
                    alpha = 1f,
                    maxAge = 220f
                )
            )
        }
    }

    private fun handleMiss() {
        val topBlock = stackedBlocks.lastOrNull() ?: return
        soundManager.playGameOverSound()
        hapticManager.triggerGameOver()

        // Moving block falls down
        val missCx = if (movingAxis == Axis.X) topBlock.cx + movingOffset else topBlock.cx
        val missCy = if (movingAxis == Axis.Y) topBlock.cy + movingOffset else topBlock.cy

        fallingPieces.add(
            FallingPiece3D(
                cx = missCx,
                cy = missCy,
                sizeX = movingSizeX,
                sizeY = movingSizeY,
                zTop = movingZTop,
                height = blockHeight,
                baseColor = movingColor,
                vz = -2f,
                vx = if (movingAxis == Axis.X) movingDir * 2f else 0f,
                vy = if (movingAxis == Axis.Y) movingDir * 2f else 0f,
                rotationSpeed = 0.08f
            )
        )

        // Prompt Save Me if not used yet; otherwise final game over
        if (!hasUsedSaveMe) {
            _gamePhase.value = GamePhase.GameOverPrompt
        } else {
            finalizeGameOver()
        }
    }

    fun onSaveMeClicked() {
        hasUsedSaveMe = true
        soundManager.playSaveMeSound()
        hapticManager.triggerTap()

        val topBlock = stackedBlocks.lastOrNull() ?: return
        // Bonus expansion to give the player a fair recovery
        val rescuedSizeX = (topBlock.sizeX * 1.3f).coerceAtMost(mode.initialSize)
        val rescuedSizeY = (topBlock.sizeY * 1.3f).coerceAtMost(mode.initialSize)

        val updatedTop = Block3D(
            cx = topBlock.cx,
            cy = topBlock.cy,
            sizeX = rescuedSizeX,
            sizeY = rescuedSizeY,
            zTop = topBlock.zTop,
            height = topBlock.height,
            baseColor = topBlock.baseColor
        )
        stackedBlocks[stackedBlocks.lastIndex] = updatedTop

        // Resume game
        _gamePhase.value = GamePhase.Playing
        prepareNextMovingBlock(updatedTop)
    }

    fun onNoThanksClicked() {
        finalizeGameOver()
    }

    private fun finalizeGameOver() {
        _gamePhase.value = GamePhase.GameOverFinal
        onGameOverRecorded(mode, _score.value, _coinsEarnedThisRun.value)
    }

    private fun prepareNextMovingBlock(topBlock: Block3D) {
        movingAxis = if (movingAxis == Axis.X) Axis.Y else Axis.X
        movingSizeX = topBlock.sizeX
        movingSizeY = topBlock.sizeY
        movingZTop = topBlock.zTop + blockHeight
        val currentFloor = _score.value + 1
        movingColor = ColorPaletteGenerator.getColorForLayer(currentFloor, currentTheme)

        baseTravelRange = (if (movingAxis == Axis.X) topBlock.sizeX else topBlock.sizeY) * 1.25f + 32f
        oscillationPhase = -Math.PI.toFloat() / 2f
        movingOffset = -baseTravelRange
    }

    // Called on every frame (60fps loop)
    fun update(deltaTimeFraction: Float) {
        if (_gamePhase.value == GamePhase.Playing) {
            // Harmonic sinusoidal gliding: calibrated to match reference video (~1.05s half-period)
            val baseFrequency = 2.95f // rad/s (~1.06s sweep)
            val heightBonus = (_score.value * 0.015f).coerceAtMost(0.9f)
            val angularFrequency = baseFrequency + heightBonus
            oscillationPhase += angularFrequency * deltaTimeFraction
            movingOffset = baseTravelRange * sin(oscillationPhase)
        }

        // Update falling pieces
        val iter = fallingPieces.iterator()
        while (iter.hasNext()) {
            val piece = iter.next()
            piece.vz -= 0.65f * deltaTimeFraction * 60f // Realistic masonry gravity
            piece.zTop += piece.vz * deltaTimeFraction * 60f
            piece.cx += piece.vx * deltaTimeFraction * 60f
            piece.cy += piece.vy * deltaTimeFraction * 60f
            piece.rotation += piece.rotationSpeed * deltaTimeFraction * 60f
            piece.alpha -= 0.015f * deltaTimeFraction * 60f

            if (piece.alpha <= 0f || piece.zTop < -600f) {
                iter.remove()
            }
        }

        // Update shockwave rings on perfect stacks
        val ringIter = shockwaveRings.iterator()
        while (ringIter.hasNext()) {
            val ring = ringIter.next()
            ring.expansion += 3.2f * deltaTimeFraction * 60f
            ring.alpha = (1f - (ring.expansion / ring.maxExpansion)).coerceIn(0f, 1f)
            if (ring.expansion >= ring.maxExpansion) {
                ringIter.remove()
            }
        }

        // Update high score celebratory confetti particles
        val cIter = confettiParticles.iterator()
        while (cIter.hasNext()) {
            val p = cIter.next()
            p.vy += 0.32f * deltaTimeFraction * 60f // Gentle air gravity
            p.vx *= (1f - 0.015f * deltaTimeFraction * 60f) // Wind drag
            p.x += p.vx * deltaTimeFraction * 60f
            p.y += p.vy * deltaTimeFraction * 60f
            p.rotation += p.rotationSpeed * deltaTimeFraction * 60f
            p.flutterPhase += p.flutterSpeed * deltaTimeFraction * 60f
            p.age += 1f * deltaTimeFraction * 60f
            if (p.age > p.maxAge * 0.65f) {
                p.alpha = (1f - ((p.age - p.maxAge * 0.65f) / (p.maxAge * 0.35f))).coerceIn(0f, 1f)
            }
            if (p.alpha <= 0f || p.y > 2800f) {
                cIter.remove()
            }
        }

        // Update combo particles
        val particleIter = comboParticles.iterator()
        while (particleIter.hasNext()) {
            val p = particleIter.next()
            p.yOffset -= 2.2f * deltaTimeFraction * 60f
            p.alpha -= 0.02f * deltaTimeFraction * 60f
            if (p.alpha <= 0f) {
                particleIter.remove()
            }
        }

        // Occasional shooting star across the sky
        if (Random.nextFloat() < 0.008f && shootingStars.size < 2) {
            shootingStars.add(
                ShootingStar(
                    x = Random.nextFloat() * 400f,
                    y = Random.nextFloat() * 200f + 60f,
                    vx = 5f + Random.nextFloat() * 4f,
                    vy = 3f + Random.nextFloat() * 2.5f,
                    length = 70f + Random.nextFloat() * 40f
                )
            )
        }

        val starIter = shootingStars.iterator()
        while (starIter.hasNext()) {
            val star = starIter.next()
            star.x += star.vx * deltaTimeFraction * 60f
            star.y += star.vy * deltaTimeFraction * 60f
            star.progress += 1f * deltaTimeFraction * 60f
            if (star.progress >= star.maxLifetime) {
                starIter.remove()
            }
        }

        // Camera smoothly follows the top layer
        targetCameraY = (_score.value * blockHeight).toFloat()
        cameraY += (targetCameraY - cameraY) * (0.09f * deltaTimeFraction * 60f)

        // Clear flash effect
        if (_isPerfectFlash.value) {
            _isPerfectFlash.value = false
        }
    }
}

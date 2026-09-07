package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.audio.RetroAudioSynthesizer
import com.example.data.AppDatabase
import com.example.data.DailyRetentionChallenge
import com.example.data.LeaderboardEntry
import com.example.data.LeaderboardRepository
import com.example.data.MatchEntity
import com.example.data.SquadVoiceState
import com.example.data.TournamentChallenge
import com.example.data.UserProfileEntity
import com.example.model.DeathReason
import com.example.model.Direction
import com.example.model.Food
import com.example.model.GameState
import com.example.model.Point
import com.example.model.RetroTheme
import com.example.model.RetroThemes
import com.example.model.WallMode
import com.example.util.AntiCheatEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.random.Random

class SnakeGameViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val matchDao = database.matchDao()
    private val profileDao = database.profileDao()
    private val leaderboardRepo = LeaderboardRepository(matchDao, profileDao)
    val audioSynthesizer = RetroAudioSynthesizer()

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    val GRID_WIDTH = 20
    val GRID_HEIGHT = 20

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _selectedTheme = MutableStateFlow(RetroThemes.CYBERPUNK)
    val selectedTheme: StateFlow<RetroTheme> = _selectedTheme.asStateFlow()

    private val _selectedWallMode = MutableStateFlow(WallMode.WALL)
    val selectedWallMode: StateFlow<WallMode> = _selectedWallMode.asStateFlow()

    fun getGridDimensionsForLevel(level: Int): Pair<Int, Int> {
        return when (level) {
            1 -> Pair(14, 16)
            2 -> Pair(16, 18)
            3 -> Pair(18, 20)
            4 -> Pair(20, 22)
            5 -> Pair(22, 25)
            else -> Pair(24, 28)
        }
    }

    val userProfile: StateFlow<UserProfileEntity> = profileDao.getProfile()
        .map { it ?: UserProfileEntity() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfileEntity()
        )

    val matchHistory: StateFlow<List<MatchEntity>> = matchDao.getAllMatches()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val topScores: StateFlow<List<MatchEntity>> = matchDao.getTopMatches(10)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val communityLeaderboard: StateFlow<List<LeaderboardEntry>> = leaderboardRepo.communityScores
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LeaderboardRepository.defaultLeaderboard
        )

    val activeTournaments: StateFlow<List<TournamentChallenge>> = leaderboardRepo.activeTournaments
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LeaderboardRepository.defaultTournaments
        )

    val squadVoiceState: StateFlow<SquadVoiceState> = leaderboardRepo.voiceState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SquadVoiceState()
        )

    private val _dailyChallenges = MutableStateFlow(
        listOf(
            DailyRetentionChallenge("c1", "Pixel Appetite", "Eat 20 retro apples in any session", 12, 20, "APPETITE_BADGE", false),
            DailyRetentionChallenge("c2", "Speed Demon", "Reach Speed Level 4 in a single run", 2, 4, "TURBO_PIN", false),
            DailyRetentionChallenge("c3", "Golden Hunter", "Collect 3 golden bonus apples", 1, 3, "AURUM_RELIC", false),
            DailyRetentionChallenge("c4", "Tournament Challenger", "Join and submit 1 tournament run", 1, 1, "CHALLENGER_CREST", true)
        )
    )
    val dailyChallenges: StateFlow<List<DailyRetentionChallenge>> = _dailyChallenges.asStateFlow()

    private var gameJob: Job? = null
    private var randomGenerator: Random = Random(System.currentTimeMillis())

    init {
        viewModelScope.launch {
            profileDao.getProfile().collect { profile ->
                if (profile != null) {
                    _selectedTheme.value = RetroThemes.getThemeById(profile.selectedThemeId)
                    val mode = try {
                        WallMode.valueOf(profile.wallMode)
                    } catch (e: Exception) {
                        WallMode.WALL
                    }
                    _selectedWallMode.value = mode
                    _gameState.update { it.copy(highScore = profile.highScore, wallMode = mode) }
                    audioSynthesizer.isEnabled = profile.soundEnabled
                } else {
                    profileDao.insertOrUpdateProfile(UserProfileEntity())
                }
            }
        }
    }

    fun setWallMode(mode: WallMode) {
        _selectedWallMode.value = mode
        _gameState.update { it.copy(wallMode = mode) }
        audioSynthesizer.playButtonClick()
        triggerHaptic(20)
        viewModelScope.launch {
            profileDao.updateWallMode(mode.name)
        }
    }

    fun startGame(tournamentSeed: Long? = null) {
        gameJob?.cancel()
        val seed = tournamentSeed ?: System.currentTimeMillis()
        randomGenerator = Random(seed)

        val (initialGridW, initialGridH) = getGridDimensionsForLevel(1)
        val centerX = initialGridW / 2
        val centerY = initialGridH / 2
        val initialSnake = listOf(
            Point(centerX, centerY),
            Point(centerX, centerY + 1),
            Point(centerX, centerY + 2)
        )
        val initialFood = spawnFood(initialSnake, initialGridW, initialGridH)

        _gameState.value = GameState(
            snake = initialSnake,
            direction = Direction.UP,
            nextDirection = Direction.UP,
            food = initialFood,
            gridWidth = initialGridW,
            gridHeight = initialGridH,
            wallMode = _selectedWallMode.value,
            score = 0,
            highScore = userProfile.value.highScore,
            applesEaten = 0,
            goldenApplesEaten = 0,
            speedLevel = 1,
            scoreMultiplier = 1.0f,
            isPlaying = true,
            isPaused = false,
            isGameOver = false,
            deathReason = null,
            moveCount = 0,
            startTimeMs = System.currentTimeMillis(),
            durationSeconds = 0,
            tournamentSeed = seed,
            levelUpAnnouncement = "LEVEL 1 • ARENA ${initialGridW}x${initialGridH}"
        )

        audioSynthesizer.playSpeedUp()
        triggerHaptic(50)
        startLoop()
    }

    fun togglePause() {
        if (_gameState.value.isGameOver || !_gameState.value.isPlaying) return
        val newPaused = !_gameState.value.isPaused
        _gameState.update { it.copy(isPaused = newPaused) }
        audioSynthesizer.playButtonClick()
    }

    fun changeDirection(newDir: Direction) {
        val current = _gameState.value
        if (current.isGameOver || current.isPaused) return
        if (!current.direction.isOpposite(newDir)) {
            _gameState.update { it.copy(nextDirection = newDir) }
            triggerHaptic(15)
        }
    }

    private fun startLoop() {
        gameJob = viewModelScope.launch {
            while (isActive) {
                val state = _gameState.value
                if (state.isPlaying && !state.isPaused && !state.isGameOver) {
                    val delayMs = calculateDelay(state.speedLevel)
                    delay(delayMs)
                    tick()
                } else {
                    delay(100)
                }
            }
        }
    }

    private fun calculateDelay(speedLevel: Int): Long {
        return when (speedLevel) {
            1 -> 175L
            2 -> 145L
            3 -> 120L
            4 -> 95L
            5 -> 75L
            else -> 60L
        }
    }

    private fun calculateSpeedLevel(apples: Int): Pair<Int, Float> {
        return when {
            apples >= 35 -> Pair(6, 2.8f)
            apples >= 25 -> Pair(5, 2.2f)
            apples >= 16 -> Pair(4, 1.8f)
            apples >= 9 -> Pair(3, 1.4f)
            apples >= 4 -> Pair(2, 1.2f)
            else -> Pair(1, 1.0f)
        }
    }

    private fun tick() {
        val current = _gameState.value
        val dir = current.nextDirection
        val head = current.snake.first()
        var nextX = head.x + dir.dx
        var nextY = head.y + dir.dy
        val gridW = current.gridWidth
        val gridH = current.gridHeight

        // Check Wall Collision or Wrap Around
        if (current.wallMode == WallMode.WALL) {
            if (nextX < 0 || nextX >= gridW || nextY < 0 || nextY >= gridH) {
                handleGameOver(DeathReason.WALL_COLLISION)
                return
            }
        } else {
            // Wall-less Mode: wrap seamlessly through edges
            nextX = (nextX + gridW) % gridW
            nextY = (nextY + gridH) % gridH
        }

        val newHead = Point(nextX, nextY)

        // Check Self Collision
        val bodyWithoutTail = current.snake.dropLast(1)
        if (bodyWithoutTail.contains(newHead)) {
            handleGameOver(DeathReason.SELF_COLLISION)
            return
        }

        val newSnake = mutableListOf(newHead)
        var newScore = current.score
        var newApples = current.applesEaten
        var newGolden = current.goldenApplesEaten
        var food = current.food
        val now = System.currentTimeMillis()

        // Check if golden apple expired
        if (food.isGolden && now > food.expiresAtMs) {
            food = spawnFood(current.snake, gridW, gridH)
        }

        val ateFood = (newHead == food.point)

        // Calculate level and screen size scaling
        val (speedLvl, mult) = calculateSpeedLevel(if (ateFood && !food.isGolden) newApples + 1 else newApples)
        val (scaledGridW, scaledGridH) = getGridDimensionsForLevel(speedLvl)
        var announcement = current.levelUpAnnouncement

        if (speedLvl > current.speedLevel) {
            audioSynthesizer.playSpeedUp()
            triggerHaptic(100)
            announcement = "LEVEL $speedLvl: SCREEN EXPANDED (${scaledGridW}x${scaledGridH})!"
        }

        if (ateFood) {
            if (food.isGolden) {
                newGolden++
                newScore += (40 * current.scoreMultiplier).toInt()
                audioSynthesizer.playGoldenApple()
                triggerHaptic(80)
            } else {
                newApples++
                newScore += (10 * current.scoreMultiplier).toInt()
                audioSynthesizer.playAppleEat()
                triggerHaptic(40)
            }

            newSnake.addAll(current.snake) // grow
            food = spawnFood(newSnake, scaledGridW, scaledGridH)
        } else {
            newSnake.addAll(current.snake.dropLast(1))
            audioSynthesizer.playMoveTick()
        }

        val duration = ((now - current.startTimeMs) / 1000).toInt()

        _gameState.update {
            it.copy(
                snake = newSnake,
                direction = dir,
                food = food,
                gridWidth = scaledGridW,
                gridHeight = scaledGridH,
                score = newScore,
                highScore = max(newScore, it.highScore),
                applesEaten = newApples,
                goldenApplesEaten = newGolden,
                speedLevel = speedLvl,
                scoreMultiplier = mult,
                moveCount = it.moveCount + 1,
                durationSeconds = duration,
                levelUpAnnouncement = announcement
            )
        }
    }

    private fun spawnFood(snake: List<Point>, gridW: Int = _gameState.value.gridWidth, gridH: Int = _gameState.value.gridHeight): Food {
        val occupied = snake.toSet()
        val freeSpots = mutableListOf<Point>()
        for (x in 0 until gridW) {
            for (y in 0 until gridH) {
                val p = Point(x, y)
                if (!occupied.contains(p)) {
                    freeSpots.add(p)
                }
            }
        }

        if (freeSpots.isEmpty()) {
            return Food(Point(0, 0))
        }

        val target = freeSpots[randomGenerator.nextInt(freeSpots.size)]
        val isGolden = randomGenerator.nextFloat() < 0.22f
        val expiration = if (isGolden) System.currentTimeMillis() + 8000L else 0L

        return Food(
            point = target,
            isGolden = isGolden,
            expiresAtMs = expiration,
            pointsValue = if (isGolden) 50 else 10
        )
    }

    private fun handleGameOver(reason: DeathReason) {
        val current = _gameState.value
        val now = System.currentTimeMillis()
        val duration = max(1, ((now - current.startTimeMs) / 1000).toInt())

        val antiCheatReport = AntiCheatEngine.isScoreLegitimate(
            score = current.score,
            applesEaten = current.applesEaten,
            movesCount = current.moveCount,
            durationSeconds = duration
        )

        val token = AntiCheatEngine.generateVerificationToken(
            score = current.score,
            applesEaten = current.applesEaten,
            goldenApplesEaten = current.goldenApplesEaten,
            movesCount = current.moveCount,
            durationSeconds = duration,
            seed = current.tournamentSeed
        )

        _gameState.update {
            it.copy(
                isGameOver = true,
                isPlaying = false,
                deathReason = reason,
                durationSeconds = duration,
                antiCheatToken = token,
                antiCheatVerified = antiCheatReport.isClean
            )
        }

        audioSynthesizer.playGameOver()
        triggerHaptic(200)

        // Persist to Room
        viewModelScope.launch {
            val match = MatchEntity(
                score = current.score,
                applesEaten = current.applesEaten,
                goldenApplesEaten = current.goldenApplesEaten,
                speedLevelReached = current.speedLevel,
                durationSeconds = duration,
                deathReason = reason.name,
                themeId = _selectedTheme.value.id,
                wallMode = current.wallMode.name,
                antiCheatVerified = antiCheatReport.isClean,
                antiCheatToken = token
            )
            matchDao.insertMatch(match)

            // Update user profile
            val currentProfile = userProfile.value
            val newHighScore = max(currentProfile.highScore, current.score)
            val updatedProfile = currentProfile.copy(
                highScore = newHighScore,
                totalGamesPlayed = currentProfile.totalGamesPlayed + 1,
                totalApplesEaten = currentProfile.totalApplesEaten + current.applesEaten,
                totalPlaytimeSeconds = currentProfile.totalPlaytimeSeconds + duration,
                integrityPercent = if (antiCheatReport.isClean) 100 else 85
            )
            profileDao.insertOrUpdateProfile(updatedProfile)

            // Submit score to leaderboard
            leaderboardRepo.submitUserScore(
                playerTag = currentProfile.gamerTag,
                score = current.score,
                apples = current.applesEaten,
                speedTier = "SPEED_LVL_${current.speedLevel}",
                token = token,
                isVerified = antiCheatReport.isClean
            )
        }
    }

    fun setTheme(theme: RetroTheme) {
        _selectedTheme.value = theme
        audioSynthesizer.playButtonClick()
        viewModelScope.launch {
            profileDao.updateTheme(theme.id)
        }
    }

    fun updateGamerTag(newTag: String) {
        val cleanTag = newTag.trim().uppercase().take(16)
        if (cleanTag.isNotEmpty()) {
            viewModelScope.launch {
                profileDao.updateGamerTag(cleanTag)
            }
        }
    }

    fun updateAvatar(avatarId: String) {
        viewModelScope.launch {
            profileDao.updateAvatar(avatarId)
        }
    }

    fun updateSettings(sound: Boolean, vibe: Boolean, scanlines: Boolean) {
        audioSynthesizer.isEnabled = sound
        viewModelScope.launch {
            profileDao.updateSettings(sound, vibe, scanlines)
        }
    }

    fun toggleVoiceMic() {
        leaderboardRepo.toggleMic()
        audioSynthesizer.playButtonClick()
    }

    fun switchVoiceChannel(name: String) {
        leaderboardRepo.switchChannel(name)
        audioSynthesizer.playButtonClick()
    }

    fun clearMatchHistory() {
        viewModelScope.launch {
            matchDao.clearHistory()
        }
    }

    private fun triggerHaptic(durationMs: Long) {
        if (!userProfile.value.vibrationEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }
}

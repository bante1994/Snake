package com.example.ui

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SquadVoiceState
import com.example.data.UserProfileEntity
import com.example.model.GameState
import com.example.model.RetroTheme
import com.example.model.WallMode
import com.example.util.AntiCheatEngine
import com.example.viewmodel.SnakeGameViewModel

@Composable
fun ArcadeScreen(
    viewModel: SnakeGameViewModel,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val theme by viewModel.selectedTheme.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val voiceState by viewModel.squadVoiceState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // HUD Top Header
            ArcadeHudHeader(
                gameState = gameState,
                theme = theme,
                soundEnabled = profile.soundEnabled,
                onToggleSound = {
                    viewModel.updateSettings(
                        sound = !profile.soundEnabled,
                        vibe = profile.vibrationEnabled,
                        scanlines = profile.scanlinesEnabled
                    )
                }
            )

            // Wall Mode & Arena Size Selector Bar
            WallModeToggleBar(
                currentMode = gameState.wallMode,
                gridWidth = gameState.gridWidth,
                gridHeight = gameState.gridHeight,
                theme = theme,
                onSelectMode = { mode -> viewModel.setWallMode(mode) }
            )

            // Dynamic Speed & Multiplier Bar
            SpeedLevelIndicator(
                speedLevel = gameState.speedLevel,
                gridWidth = gameState.gridWidth,
                gridHeight = gameState.gridHeight,
                multiplier = gameState.scoreMultiplier,
                isGoldenActive = gameState.food.isGolden,
                goldenExpiresAt = gameState.food.expiresAtMs,
                theme = theme
            )

            // Game Play Canvas
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                GameCanvas(
                    gameState = gameState,
                    theme = theme,
                    scanlinesEnabled = profile.scanlinesEnabled,
                    onDirectionChange = { dir -> viewModel.changeDirection(dir) },
                    modifier = Modifier.padding(2.dp)
                )

                // Start overlay if not yet started
                if (!gameState.isPlaying && !gameState.isGameOver) {
                    StartGameOverlay(
                        theme = theme,
                        highScore = profile.highScore,
                        gamerTag = profile.gamerTag,
                        currentWallMode = gameState.wallMode,
                        gridWidth = gameState.gridWidth,
                        gridHeight = gameState.gridHeight,
                        onSelectWallMode = { mode -> viewModel.setWallMode(mode) },
                        onStart = { viewModel.startGame() }
                    )
                }

                // Pause banner
                if (gameState.isPaused && !gameState.isGameOver) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(theme.boardBackground.copy(alpha = 0.92f))
                            .border(2.dp, theme.accent, RoundedCornerShape(8.dp))
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "GAME PAUSED",
                            color = theme.hudText,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            // Compact Integrated Voice Radio Bar
            SquadRadioWidget(
                voiceState = voiceState,
                theme = theme,
                onToggleMic = { viewModel.toggleVoiceMic() }
            )

            // Tactile D-Pad and Action Buttons
            ArcadeControls(
                theme = theme,
                isPlaying = gameState.isPlaying,
                isPaused = gameState.isPaused,
                onDirection = { dir -> viewModel.changeDirection(dir) },
                onTogglePause = { viewModel.togglePause() },
                onRestart = { viewModel.startGame() }
            )
        }

        // Game Over Overlay
        AnimatedVisibility(
            visible = gameState.isGameOver,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            GameOverDialog(
                gameState = gameState,
                theme = theme,
                profile = profile,
                onPlayAgain = { viewModel.startGame() },
                onShareScore = {
                    val tournamentCode = AntiCheatEngine.formatTournamentSeed(gameState.tournamentSeed)
                    val shareText = "🎮 Just scored ${gameState.score} in Retro Snake!\n" +
                            "Speed Level: ${gameState.speedLevel} | Apples: ${gameState.applesEaten}\n" +
                            "Anti-Cheat Token: ${gameState.antiCheatToken}\n" +
                            "Play my tournament seed: $tournamentCode\n" +
                            "Can you beat my verified clean run?"
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Retro Snake Score"))
                }
            )
        }
    }
}

@Composable
fun ArcadeHudHeader(
    gameState: GameState,
    theme: RetroTheme,
    soundEnabled: Boolean,
    onToggleSound: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Current Score
        Column {
            Text(
                text = "SCORE",
                color = theme.hudText.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "%05d".format(gameState.score),
                color = theme.hudText,
                fontSize = 26.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold
            )
        }

        // High Score
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(theme.boardBackground.copy(alpha = 0.8f))
                .border(1.dp, theme.accent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = "High Score",
                tint = theme.foodGolden,
                modifier = Modifier.size(18.dp)
            )
            Column {
                Text(
                    text = "BEST",
                    color = theme.hudText.copy(alpha = 0.7f),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "%05d".format(gameState.highScore),
                    color = theme.foodGolden,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Sound Toggle
        IconButton(
            onClick = onToggleSound,
            modifier = Modifier.testTag("btn_toggle_sound")
        ) {
            Icon(
                imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                contentDescription = if (soundEnabled) "Mute Audio" else "Unmute Audio",
                tint = theme.hudText,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun WallModeToggleBar(
    currentMode: WallMode,
    gridWidth: Int,
    gridHeight: Int,
    theme: RetroTheme,
    onSelectMode: (WallMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(theme.boardBackground.copy(alpha = 0.88f))
            .border(1.dp, theme.gridColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isWall = (currentMode == WallMode.WALL)
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isWall) theme.accent else Color.Transparent,
                border = if (isWall) null else BorderStroke(0.5.dp, theme.gridColor),
                modifier = Modifier
                    .clickable { onSelectMode(WallMode.WALL) }
                    .testTag("btn_mode_wall")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = if (isWall) Color.Black else theme.hudText.copy(alpha = 0.7f),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "WALLS",
                        color = if (isWall) Color.Black else theme.hudText.copy(alpha = 0.7f),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            val isWallLess = (currentMode == WallMode.WALL_LESS)
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isWallLess) theme.snakeHead else Color.Transparent,
                border = if (isWallLess) null else BorderStroke(0.5.dp, theme.gridColor),
                modifier = Modifier
                    .clickable { onSelectMode(WallMode.WALL_LESS) }
                    .testTag("btn_mode_wallless")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AllInclusive,
                        contentDescription = null,
                        tint = if (isWallLess) Color.Black else theme.hudText.copy(alpha = 0.7f),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "WALL-LESS",
                        color = if (isWallLess) Color.Black else theme.hudText.copy(alpha = 0.7f),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Screen / Arena Dimension Indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(theme.background.copy(alpha = 0.5f))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ZoomOutMap,
                contentDescription = null,
                tint = theme.foodGolden,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "SCREEN ${gridWidth}x${gridHeight}",
                color = theme.foodGolden,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
fun SpeedLevelIndicator(
    speedLevel: Int,
    gridWidth: Int,
    gridHeight: Int,
    multiplier: Float,
    isGoldenActive: Boolean,
    goldenExpiresAt: Long,
    theme: RetroTheme,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Speed Level with bars
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = "Speed Level",
                tint = theme.snakeHead,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "LVL $speedLevel",
                color = theme.hudText,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )

            // Speed gauge blocks
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                for (i in 1..6) {
                    val isActive = i <= speedLevel
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isActive) theme.snakeHead else theme.boardBackground)
                            .border(0.5.dp, theme.gridColor, RoundedCornerShape(2.dp))
                    )
                }
            }
        }

        // Golden Apple or Multiplier Badge
        if (isGoldenActive) {
            val secondsLeft = ((goldenExpiresAt - System.currentTimeMillis()).coerceAtLeast(0L) / 1000).toInt()
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(theme.foodGolden)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "GOLDEN +50 (${secondsLeft}s)",
                    color = Color.Black,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(theme.boardBackground)
                    .border(1.dp, theme.accent.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${"%.1f".format(multiplier)}x PTS",
                    color = theme.accent,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun SquadRadioWidget(
    voiceState: SquadVoiceState,
    theme: RetroTheme,
    onToggleMic: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(theme.boardBackground.copy(alpha = 0.85f))
            .border(1.dp, theme.gridColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Radio Waveform bars
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (vol in voiceState.volumeLevels) {
                    val barHeight = (vol * 16).coerceIn(4f, 16f).dp
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(barHeight)
                            .background(if (!voiceState.isMicMuted) theme.snakeHead else Color.Gray)
                    )
                }
            }

            Column {
                Text(
                    text = "TEAM RADIO",
                    color = theme.hudText.copy(alpha = 0.6f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp
                )
                Text(
                    text = voiceState.channelName,
                    color = theme.hudText,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        // Mic Toggle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (voiceState.isMicMuted) Color(0xFF552222) else Color(0xFF0F3B66))
                .clickable(onClick = onToggleMic)
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .testTag("btn_radio_mic")
        ) {
            Icon(
                imageVector = if (voiceState.isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = "Squad Radio Mic",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = if (voiceState.isMicMuted) "MUTED" else "LIVE",
                color = Color.White,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun StartGameOverlay(
    theme: RetroTheme,
    highScore: Int,
    gamerTag: String,
    currentWallMode: WallMode,
    gridWidth: Int,
    gridHeight: Int,
    onSelectWallMode: (WallMode) -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.boardBackground.copy(alpha = 0.94f)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .padding(18.dp)
            .border(2.dp, theme.snakeHead, RoundedCornerShape(16.dp))
            .shadow(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "RETRO SNAKE",
                color = theme.snakeHead,
                fontSize = 24.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )

            Text(
                text = "PILOT: $gamerTag",
                color = theme.hudText,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )

            // Wall Mode Selection Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.background.copy(alpha = 0.5f))
                    .border(1.dp, theme.gridColor, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "SELECT BORDER MODE:",
                    color = theme.hudText.copy(alpha = 0.7f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isWall = (currentWallMode == WallMode.WALL)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isWall) theme.accent else theme.boardBackground,
                        border = BorderStroke(1.dp, if (isWall) theme.accent else theme.gridColor),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectWallMode(WallMode.WALL) }
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = if (isWall) Color.Black else theme.hudText,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "WALLS",
                                color = if (isWall) Color.Black else theme.hudText,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "Fatal Borders",
                                color = if (isWall) Color.Black.copy(alpha = 0.7f) else theme.hudText.copy(alpha = 0.6f),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp
                            )
                        }
                    }

                    val isWallLess = (currentWallMode == WallMode.WALL_LESS)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isWallLess) theme.snakeHead else theme.boardBackground,
                        border = BorderStroke(1.dp, if (isWallLess) theme.snakeHead else theme.gridColor),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectWallMode(WallMode.WALL_LESS) }
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AllInclusive,
                                contentDescription = null,
                                tint = if (isWallLess) Color.Black else theme.hudText,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "WALL-LESS",
                                color = if (isWallLess) Color.Black else theme.hudText,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "Wrap Screen",
                                color = if (isWallLess) Color.Black.copy(alpha = 0.7f) else theme.hudText.copy(alpha = 0.6f),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            // Dynamic Arena Screen Scale Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(theme.boardBackground)
                    .border(0.5.dp, theme.foodGolden.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomOutMap,
                    contentDescription = null,
                    tint = theme.foodGolden,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Screen size scales up with level ($gridWidth x $gridHeight initial)!",
                    color = theme.hudText.copy(alpha = 0.85f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(
                    containerColor = theme.snakeHead,
                    contentColor = theme.background
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_start_play")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "START MISSION",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun GameOverDialog(
    gameState: GameState,
    theme: RetroTheme,
    profile: UserProfileEntity,
    onPlayAgain: () -> Unit,
    onShareScore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.boardBackground.copy(alpha = 0.96f)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .padding(20.dp)
            .border(2.dp, theme.foodRed, RoundedCornerShape(16.dp))
            .shadow(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "SESSION TERMINATED",
                color = theme.foodRed,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                letterSpacing = 1.5.sp
            )

            Text(
                text = gameState.deathReason?.label ?: "Grid Collision",
                color = theme.hudText.copy(alpha = 0.8f),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )

            // Mode & Screen Dimension Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(theme.background.copy(alpha = 0.8f))
                    .border(0.5.dp, theme.gridColor, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (gameState.wallMode == WallMode.WALL) Icons.Default.Security else Icons.Default.AllInclusive,
                        contentDescription = null,
                        tint = if (gameState.wallMode == WallMode.WALL) theme.accent else theme.snakeHead,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "MODE: ${gameState.wallMode.label}",
                        color = theme.hudText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomOutMap,
                        contentDescription = null,
                        tint = theme.foodGolden,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "FINAL ARENA: ${gameState.gridWidth}x${gameState.gridHeight}",
                        color = theme.foodGolden,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Stats Matrix
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.background.copy(alpha = 0.6f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("FINAL SCORE", color = theme.hudText.copy(alpha = 0.6f), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text("${gameState.score}", color = theme.hudText, fontSize = 18.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("APPLES", color = theme.hudText.copy(alpha = 0.6f), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text("${gameState.applesEaten}", color = theme.accent, fontSize = 18.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("MAX SPEED", color = theme.hudText.copy(alpha = 0.6f), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text("LVL ${gameState.speedLevel}", color = theme.snakeHead, fontSize = 18.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }

            // Anti-Cheat Shield Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (gameState.antiCheatVerified) Color(0xFF0C243B) else Color(0xFF4A1A1A))
                    .border(1.dp, if (gameState.antiCheatVerified) Color(0xFF00D2FF) else Color(0xFFFF4444), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Anti Cheat",
                        tint = if (gameState.antiCheatVerified) Color(0xFF00D2FF) else Color(0xFFFF4444),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (gameState.antiCheatVerified) "ANTI-CHEAT VERIFIED" else "INTEGRITY WARNING",
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
                Text(
                    text = gameState.antiCheatToken.take(16),
                    color = Color.White.copy(alpha = 0.7f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp
                )
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Share Tournament Challenge
                Button(
                    onClick = onShareScore,
                    colors = ButtonDefaults.buttonColors(containerColor = theme.accent, contentColor = theme.background),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_share_score")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("CHALLENGE", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Play Again
                Button(
                    onClick = onPlayAgain,
                    colors = ButtonDefaults.buttonColors(containerColor = theme.snakeHead, contentColor = theme.background),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_play_again")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("REPLAY", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

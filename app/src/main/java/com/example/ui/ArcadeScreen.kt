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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Compact HUD Header (Score, Level, Best, Sound) - does not overlap playing area
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

            // Maximized Game Play Canvas (Full height, unobstructed visibility for snake & food)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                GameCanvas(
                    gameState = gameState,
                    theme = theme,
                    scanlinesEnabled = profile.scanlinesEnabled,
                    onDirectionChange = { dir -> viewModel.changeDirection(dir) },
                    modifier = Modifier.fillMaxSize()
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

            // Minimal, low-profile bottom action bar (Replaces bulky controls & radios)
            ArcadeBottomBar(
                isPlaying = gameState.isPlaying,
                isPaused = gameState.isPaused,
                wallMode = gameState.wallMode,
                theme = theme,
                onTogglePause = { viewModel.togglePause() },
                onRestart = { viewModel.startGame() },
                onToggleWallMode = {
                    val nextMode = if (gameState.wallMode == WallMode.WALL) com.example.model.WallMode.WALL_LESS else com.example.model.WallMode.WALL
                    viewModel.setWallMode(nextMode)
                }
            )
        }

        // Game Over Overlay
        AnimatedVisibility(
            visible = gameState.isGameOver && !gameState.isAdShowing,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            GameOverDialog(
                gameState = gameState,
                theme = theme,
                profile = profile,
                onWatchAdToContinue = { viewModel.startWatchAdForRevive() },
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

        // Rewarded Ad Revive Modal
        AnimatedVisibility(
            visible = gameState.isAdShowing,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            RewardedAdModal(
                gameState = gameState,
                profile = profile,
                theme = theme,
                onAdCompleted = { viewModel.completeAdAndRevive() },
                onSkipAd = { viewModel.dismissAdWithoutRevive() }
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
    Surface(
        color = theme.boardBackground.copy(alpha = 0.95f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, theme.gridColor),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Current Score
            Column {
                Text(
                    text = "SCORE",
                    color = theme.hudText.copy(alpha = 0.65f),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "%05d".format(gameState.score),
                    color = theme.hudText,
                    fontSize = 22.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Center: Speed Level & Multiplier / Golden or Shield status
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "LVL ${gameState.speedLevel}",
                        color = theme.accent,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "• ${"%.1f".format(gameState.scoreMultiplier)}x",
                        color = theme.snakeHead,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (gameState.food.isGolden) {
                    val goldenSecs = maxOf(0, ((gameState.food.expiresAtMs - System.currentTimeMillis() + 999) / 1000).toInt())
                    Text(
                        text = "GOLDEN +40 (${goldenSecs}s)",
                        color = theme.foodGolden,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold
                    )
                } else {
                    val isShield = gameState.invulnerableUntilMs > System.currentTimeMillis()
                    if (isShield) {
                        val shieldSecs = ((gameState.invulnerableUntilMs - System.currentTimeMillis() + 999) / 1000).toInt()
                        Text(
                            text = "SHIELD: ${shieldSecs}s",
                            color = theme.accent,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = if (gameState.wallMode == WallMode.WALL) "WALLS ACTIVE" else "WALLS WRAP",
                            color = theme.hudText.copy(alpha = 0.6f),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Right: Best High Score & Sound Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "BEST",
                        color = theme.foodGolden.copy(alpha = 0.7f),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "%05d".format(gameState.highScore),
                        color = theme.foodGolden,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onToggleSound,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("btn_toggle_sound")
                ) {
                    Icon(
                        imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                        contentDescription = if (soundEnabled) "Mute Audio" else "Unmute Audio",
                        tint = if (soundEnabled) theme.snakeHead else theme.hudText.copy(alpha = 0.45f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
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
                text = "AROOTX",
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
    onWatchAdToContinue: () -> Unit,
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

            // Rewarded Ad Revive Option (Continue with last score from where died)
            if (gameState.canReviveWithAd) {
                Surface(
                    color = Color(0xFF102820),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, theme.foodGolden),
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircleFilled,
                                contentDescription = null,
                                tint = theme.foodGolden,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "WATCH AD TO CONTINUE",
                                color = theme.foodGolden,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = "${5 - gameState.revivesUsed} left",
                                color = theme.hudText.copy(alpha = 0.7f),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                        }

                        Text(
                            text = "Revive exactly where you crashed with your last score (${gameState.score} PTS) and +3.5s cyber shield!",
                            color = theme.hudText.copy(alpha = 0.9f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )

                        Button(
                            onClick = onWatchAdToContinue,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = theme.foodGolden,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_watch_ad_revive")
                        ) {
                            Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "REVIVE NOW (${gameState.score} PTS)",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
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

@Composable
fun RewardedAdModal(
    gameState: GameState,
    profile: UserProfileEntity,
    theme: RetroTheme,
    onAdCompleted: () -> Unit,
    onSkipAd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF091218)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(2.dp, theme.accent, RoundedCornerShape(16.dp))
            .shadow(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with Ad Mob Tag & Close/Skip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        tint = theme.foodGolden,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "ADMOB REWARDED AD",
                        color = theme.foodGolden,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )
                }

                IconButton(
                    onClick = onSkipAd,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("btn_skip_ad")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Skip Ad",
                        tint = theme.hudText.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Backend Configured Status Badge
            Surface(
                color = theme.boardBackground,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.8.dp, theme.foodGolden.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SPONSORED REVIVE STREAM",
                        color = theme.foodGolden,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "REWARD: +3.5S SHIELD",
                        color = theme.accent,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Simulated Video / Commercial Player Frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF03080E))
                    .border(1.dp, theme.snakeHead.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        tint = theme.snakeHead,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "RETRO CYBER REWARD ARCADE",
                        color = theme.hudText,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Revive with score: ${gameState.score} • Level ${gameState.speedLevel}",
                        color = theme.hudText.copy(alpha = 0.7f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.03f))
                )
            }

            // Progress bar
            val progress = ((5 - gameState.adCountdownSeconds).coerceIn(0, 5)) / 5f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (gameState.adCountdownSeconds == 0) theme.snakeHead else theme.foodGolden,
                trackColor = theme.boardBackground
            )

            // Timer / Reward Ready Status
            if (gameState.adCountdownSeconds > 0) {
                Text(
                    text = "REWARD UNLOCKS IN ${gameState.adCountdownSeconds} SECONDS...",
                    color = theme.hudText,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "✓ AD COMPLETED • REWARD UNLOCKED",
                        color = theme.snakeHead,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp
                    )
                }
            }

            // Action Button
            Button(
                onClick = onAdCompleted,
                enabled = (gameState.adCountdownSeconds == 0),
                colors = ButtonDefaults.buttonColors(
                    containerColor = theme.snakeHead,
                    contentColor = Color.Black,
                    disabledContainerColor = theme.boardBackground,
                    disabledContentColor = theme.hudText.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_claim_revive_reward")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (gameState.adCountdownSeconds == 0) "RESUME FROM WHERE YOU CRASHED (${gameState.score} PTS)" else "PLAYING SPONSOR AD...",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

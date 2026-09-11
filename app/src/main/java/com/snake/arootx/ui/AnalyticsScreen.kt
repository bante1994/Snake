package com.snake.arootx.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.snake.arootx.data.DailyRetentionChallenge
import com.snake.arootx.data.MatchEntity
import com.snake.arootx.data.UserProfileEntity
import com.snake.arootx.model.RetroTheme
import com.snake.arootx.viewmodel.SnakeGameViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

@Composable
fun AnalyticsScreen(
    viewModel: SnakeGameViewModel,
    modifier: Modifier = Modifier
) {
    val theme by viewModel.selectedTheme.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val matchHistory by viewModel.matchHistory.collectAsStateWithLifecycle()
    val dailyChallenges by viewModel.dailyChallenges.collectAsStateWithLifecycle()

    val totalMatches = matchHistory.size
    val avgScore = if (totalMatches > 0) matchHistory.sumOf { it.score } / totalMatches else 0
    val wallDeaths = matchHistory.count { it.deathReason.contains("WALL") }
    val selfDeaths = matchHistory.count { it.deathReason.contains("SELF") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "MATCH ANALYTICS",
                    color = theme.hudText,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Telemetry, Retention Streaks & Performance Insights",
                    color = theme.hudText.copy(alpha = 0.65f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Daily Retention & Streak Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.boardBackground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, theme.accent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Daily Streak",
                                tint = Color(0xFFFF5500),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "DAY ${profile.streakDays} STREAK ACTIVE",
                                color = theme.hudText,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(theme.snakeHead.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "RETENTION 100%",
                                color = theme.snakeHead,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "Daily Quests & Milestones:",
                        color = theme.hudText.copy(alpha = 0.7f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )

                    dailyChallenges.forEach { challenge ->
                        DailyChallengeRow(challenge = challenge, theme = theme)
                    }
                }
            }
        }

        // Key Performance Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricKpiCard(
                    title = "TOTAL RUNS",
                    value = "${profile.totalGamesPlayed}",
                    subText = "offline + online",
                    color = theme.snakeHead,
                    theme = theme,
                    modifier = Modifier.weight(1f)
                )
                MetricKpiCard(
                    title = "AVG SCORE",
                    value = "$avgScore",
                    subText = "lifetime mean",
                    color = theme.accent,
                    theme = theme,
                    modifier = Modifier.weight(1f)
                )
                MetricKpiCard(
                    title = "TOTAL APPLES",
                    value = "${profile.totalApplesEaten}",
                    subText = "eaten in grid",
                    color = theme.foodGolden,
                    theme = theme,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Score Progression Chart
        if (matchHistory.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = theme.boardBackground),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, theme.gridColor, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Timeline, contentDescription = null, tint = theme.snakeHead, modifier = Modifier.size(16.dp))
                            Text(
                                text = "RECENT MATCH SCORES",
                                color = theme.hudText,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        ScoreHistoryChart(
                            matches = matchHistory.take(12).reversed(),
                            theme = theme,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )
                    }
                }
            }
        }

        // Death Reason Breakdown
        if (totalMatches > 0) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = theme.boardBackground),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, theme.gridColor, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "INCIDENT ANALYSIS (CAUSE OF TERMINATION)",
                            color = theme.hudText,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Wall Crash: $wallDeaths (${if (totalMatches > 0) (wallDeaths * 100 / totalMatches) else 0}%)",
                                color = theme.foodRed,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "Self Collision: $selfDeaths (${if (totalMatches > 0) (selfDeaths * 100 / totalMatches) else 0}%)",
                                color = theme.accent,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Detailed Match History Header & Clear Action
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MATCH LOG (${matchHistory.size})",
                    color = theme.hudText.copy(alpha = 0.8f),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )

                if (matchHistory.isNotEmpty()) {
                    Button(
                        onClick = { viewModel.clearMatchHistory() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = theme.foodRed),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear History", modifier = Modifier.size(16.dp))
                        Text("CLEAR", fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                    }
                }
            }
        }

        // Match log rows
        if (matchHistory.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No matches logged yet.\nPlay your first round in the Arcade!",
                        color = theme.hudText.copy(alpha = 0.5f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            items(matchHistory) { match ->
                MatchHistoryRow(match = match, theme = theme)
            }
        }
    }
}

@Composable
fun MetricKpiCard(
    title: String,
    value: String,
    subText: String,
    color: Color,
    theme: RetroTheme,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.boardBackground),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.border(1.dp, theme.gridColor, RoundedCornerShape(10.dp))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                color = theme.hudText.copy(alpha = 0.6f),
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                color = color,
                fontFamily = FontFamily.Monospace,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = subText,
                color = theme.hudText.copy(alpha = 0.5f),
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp
            )
        }
    }
}

@Composable
fun DailyChallengeRow(
    challenge: DailyRetentionChallenge,
    theme: RetroTheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(theme.background.copy(alpha = 0.5f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = if (challenge.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (challenge.isCompleted) theme.snakeHead else theme.hudText.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
            Column {
                Text(
                    text = challenge.title,
                    color = theme.hudText,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Text(
                    text = challenge.description,
                    color = theme.hudText.copy(alpha = 0.6f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp
                )
            }
        }

        Text(
            text = "${challenge.currentProgress}/${challenge.maxProgress}",
            color = if (challenge.isCompleted) theme.snakeHead else theme.accent,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}

@Composable
fun ScoreHistoryChart(
    matches: List<MatchEntity>,
    theme: RetroTheme,
    modifier: Modifier = Modifier
) {
    if (matches.isEmpty()) return

    val maxScore = max(100, matches.maxOfOrNull { it.score } ?: 100)

    Canvas(modifier = modifier) {
        val barWidth = (size.width / (matches.size * 1.6f)).coerceIn(12f, 32f)
        val spacing = (size.width - (matches.size * barWidth)) / (matches.size + 1)

        matches.forEachIndexed { i, match ->
            val x = spacing + i * (barWidth + spacing)
            val barHeight = (match.score.toFloat() / maxScore) * size.height * 0.85f
            val y = size.height - barHeight

            // Draw Bar
            drawRoundRect(
                color = theme.snakeHead,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Top cap
            drawRoundRect(
                color = theme.accent,
                topLeft = Offset(x, y),
                size = Size(barWidth, 6f),
                cornerRadius = CornerRadius(2f, 2f)
            )
        }
    }
}

@Composable
fun MatchHistoryRow(
    match: MatchEntity,
    theme: RetroTheme
) {
    val dateStr = remember(match.timestamp) {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(match.timestamp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(theme.boardBackground)
            .border(0.5.dp, theme.gridColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "${match.score} PTS",
                color = theme.hudText,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = "${match.applesEaten} apples • Lvl ${match.speedLevelReached} • ${if (match.wallMode == "WALL_LESS") "WALL-LESS" else "WALLS"} • ${match.durationSeconds}s",
                color = theme.hudText.copy(alpha = 0.6f),
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = dateStr,
                color = theme.hudText.copy(alpha = 0.5f),
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp
            )
            Text(
                text = match.antiCheatToken.take(14),
                color = if (match.antiCheatVerified) theme.snakeHead else theme.foodRed,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

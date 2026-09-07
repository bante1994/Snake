package com.example.ui

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.LeaderboardEntry
import com.example.data.LeaderboardRegion
import com.example.data.TournamentChallenge
import com.example.model.RetroTheme
import com.example.util.AntiCheatEngine
import com.example.viewmodel.SnakeGameViewModel

@Composable
fun LeaderboardScreen(
    viewModel: SnakeGameViewModel,
    onPlayTournament: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme by viewModel.selectedTheme.collectAsStateWithLifecycle()
    val leaderboard by viewModel.communityLeaderboard.collectAsStateWithLifecycle()
    val tournaments by viewModel.activeTournaments.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedRegionIndex by remember { mutableIntStateOf(0) }
    val regions = LeaderboardRegion.values()
    val currentRegion = regions[selectedRegionIndex]

    var showTournamentInput by remember { mutableStateOf(false) }
    var tournamentCodeInput by remember { mutableStateOf("") }

    val filteredLeaderboard = remember(leaderboard, currentRegion) {
        if (currentRegion == LeaderboardRegion.GLOBAL) {
            leaderboard
        } else {
            leaderboard.filter { it.region == currentRegion.code || it.region == "ALL" }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "GLOBAL RANKINGS",
                    color = theme.hudText,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Anti-Cheat 2.0 Cryptographic Integrity",
                    color = theme.hudText.copy(alpha = 0.65f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Enter Seed Code Button
            IconButton(
                onClick = { showTournamentInput = !showTournamentInput },
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.boardBackground)
                    .border(1.dp, theme.accent, RoundedCornerShape(8.dp))
                    .testTag("btn_toggle_seed_input")
            ) {
                Icon(
                    imageVector = Icons.Default.VpnKey,
                    contentDescription = "Enter Tournament Code",
                    tint = theme.accent
                )
            }
        }

        // Tournament Seed Input Drawer
        AnimatedVisibility(visible = showTournamentInput) {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.boardBackground),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, theme.accent, RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "JOIN FRIEND CHALLENGE / SEED",
                        color = theme.hudText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = tournamentCodeInput,
                            onValueChange = { tournamentCodeInput = it },
                            placeholder = { Text("#RNK-XXXX", color = theme.hudText.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = theme.hudText,
                                unfocusedTextColor = theme.hudText,
                                focusedBorderColor = theme.accent,
                                unfocusedBorderColor = theme.gridColor
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("input_tournament_seed")
                        )
                        Button(
                            onClick = {
                                if (tournamentCodeInput.isNotBlank()) {
                                    val seed = AntiCheatEngine.parseTournamentCode(tournamentCodeInput)
                                    onPlayTournament(seed)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.snakeHead, contentColor = theme.background),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("JOIN", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Regional Filter Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedRegionIndex,
            containerColor = theme.boardBackground,
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedRegionIndex]),
                    color = theme.accent
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
        ) {
            regions.forEachIndexed { index, region ->
                Tab(
                    selected = selectedRegionIndex == index,
                    onClick = { selectedRegionIndex = index },
                    text = {
                        Text(
                            text = region.label.uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (selectedRegionIndex == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp,
                            color = if (selectedRegionIndex == index) theme.accent else theme.hudText.copy(alpha = 0.6f)
                        )
                    }
                )
            }
        }

        // Leaderboard List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Active Tournament Feature Card
            item {
                Text(
                    text = "COMMUNITY TOURNAMENTS",
                    color = theme.hudText.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                )
            }

            items(tournaments) { tourney ->
                TournamentCard(
                    tournament = tourney,
                    theme = theme,
                    onPlay = { onPlayTournament(tourney.seed) },
                    onShare = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "🏆 Challenge alert in Retro Snake!\n" +
                                        "Tournament: ${tourney.title}\n" +
                                        "Target Score: ${tourney.targetScore}\n" +
                                        "Tournament Code: ${tourney.code}\n" +
                                        "Can you beat the leaderboard?"
                            )
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Tournament"))
                    }
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "COMPETITIVE STANDINGS (${currentRegion.label})",
                    color = theme.hudText.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            items(filteredLeaderboard) { entry ->
                LeaderboardRow(entry = entry, theme = theme)
            }
        }
    }
}

@Composable
fun TournamentCard(
    tournament: TournamentChallenge,
    theme: RetroTheme,
    onPlay: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.boardBackground),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, theme.gridColor, RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = tournament.code,
                        color = theme.accent,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• ${tournament.participants} players",
                        color = theme.hudText.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = tournament.title,
                    color = theme.hudText,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Target: ${tournament.targetScore} pts | By ${tournament.creatorTag}",
                    color = theme.hudText.copy(alpha = 0.7f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onShare) {
                    Icon(Icons.Default.Share, contentDescription = "Share Tournament", tint = theme.accent, modifier = Modifier.size(18.dp))
                }
                Button(
                    onClick = onPlay,
                    colors = ButtonDefaults.buttonColors(containerColor = theme.snakeHead, contentColor = theme.background),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("PLAY", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LeaderboardRow(
    entry: LeaderboardEntry,
    theme: RetroTheme
) {
    val rankColor = when (entry.rank) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> theme.hudText.copy(alpha = 0.7f)
    }

    val isUser = entry.isCurrentUser

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isUser) theme.accent.copy(alpha = 0.18f) else theme.boardBackground)
            .border(
                width = if (isUser) 1.5.dp else 0.5.dp,
                color = if (isUser) theme.accent else theme.gridColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank & Tag
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "#%02d".format(entry.rank),
                color = rankColor,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = entry.playerTag,
                        color = if (isUser) theme.accent else theme.hudText,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    if (entry.isVerified) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Anti-Cheat Verified",
                            tint = Color(0xFF00D2FF),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Text(
                    text = "${entry.tierRank} • ${entry.apples} apples • ${entry.speedTier}",
                    color = theme.hudText.copy(alpha = 0.6f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            }
        }

        // Score
        Text(
            text = "%04d".format(entry.score),
            color = if (entry.rank <= 3) theme.foodGolden else theme.hudText,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp
        )
    }
}

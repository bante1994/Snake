package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.SnakeGameViewModel

enum class AppNavTab(val label: String, val icon: ImageVector, val tag: String) {
    ARCADE("Arcade", Icons.Default.Gamepad, "tab_arcade"),
    LEADERBOARD("Rankings", Icons.Default.EmojiEvents, "tab_leaderboard"),
    ANALYTICS("Analytics", Icons.Default.Analytics, "tab_analytics"),
    PROFILE("Profile", Icons.Default.Person, "tab_profile")
}

@Composable
fun RetroSnakeApp(
    viewModel: SnakeGameViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(AppNavTab.ARCADE) }
    val theme by viewModel.selectedTheme.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .statusBarsPadding(),
        bottomBar = {
            NavigationBar(
                containerColor = theme.boardBackground,
                contentColor = theme.hudText,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("main_navigation_bar")
            ) {
                AppNavTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            viewModel.audioSynthesizer.playButtonClick()
                            selectedTab = tab
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.label.uppercase(),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = theme.background,
                            selectedTextColor = theme.snakeHead,
                            indicatorColor = theme.snakeHead,
                            unselectedIconColor = theme.hudText.copy(alpha = 0.5f),
                            unselectedTextColor = theme.hudText.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                AppNavTab.ARCADE -> {
                    ArcadeScreen(viewModel = viewModel)
                }
                AppNavTab.LEADERBOARD -> {
                    LeaderboardScreen(
                        viewModel = viewModel,
                        onPlayTournament = { seed ->
                            selectedTab = AppNavTab.ARCADE
                            viewModel.startGame(tournamentSeed = seed)
                        }
                    )
                }
                AppNavTab.ANALYTICS -> {
                    AnalyticsScreen(viewModel = viewModel)
                }
                AppNavTab.PROFILE -> {
                    ProfileScreen(viewModel = viewModel)
                }
            }
        }
    }
}

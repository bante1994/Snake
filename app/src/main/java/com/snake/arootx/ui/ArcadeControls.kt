package com.snake.arootx.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snake.arootx.model.Direction
import com.snake.arootx.model.RetroTheme
import com.snake.arootx.model.WallMode

/**
 * Compact, low-profile bottom action bar.
 * Replaces bulky touchpads and radio panels to maximize playing area.
 */
@Composable
fun ArcadeBottomBar(
    isPlaying: Boolean,
    isPaused: Boolean,
    wallMode: WallMode,
    theme: RetroTheme,
    onTogglePause: () -> Unit,
    onRestart: () -> Unit,
    onToggleWallMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Subtle Swipe Navigation Hint
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Swipe,
                contentDescription = null,
                tint = theme.accent,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "SWIPE: ⬆ ⬇ ⬅ ➡",
                color = theme.hudText.copy(alpha = 0.75f),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }

        // Action Controls: WALL MODE, PAUSE, RESTART
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Wall Mode Toggle Button
            Surface(
                onClick = onToggleWallMode,
                shape = RoundedCornerShape(8.dp),
                color = if (wallMode == WallMode.WALL) theme.accent.copy(alpha = 0.2f) else theme.snakeHead.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, if (wallMode == WallMode.WALL) theme.accent else theme.snakeHead),
                modifier = Modifier.testTag("btn_toggle_wall_mode")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (wallMode == WallMode.WALL) Icons.Default.Security else Icons.Default.AllInclusive,
                        contentDescription = null,
                        tint = if (wallMode == WallMode.WALL) theme.accent else theme.snakeHead,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = if (wallMode == WallMode.WALL) "WALLS" else "WRAP",
                        color = if (wallMode == WallMode.WALL) theme.accent else theme.snakeHead,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Pause / Resume Button
            ArcadeActionButton(
                label = if (isPaused) "RESUME" else "PAUSE",
                icon = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                color = theme.accent,
                textColor = theme.background,
                onClick = onTogglePause,
                testTag = "btn_pause_toggle"
            )

            // Quick Restart Button
            ArcadeActionButton(
                label = "RESTART",
                icon = Icons.Default.Refresh,
                color = theme.boardBackground,
                textColor = theme.hudText,
                borderColor = theme.gridColor,
                onClick = onRestart,
                testTag = "btn_restart_game"
            )
        }
    }
}

@Composable
fun ArcadeActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    textColor: Color,
    borderColor: Color? = null,
    onClick: () -> Unit,
    testTag: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(8.dp),
        color = if (isPressed) color.copy(alpha = 0.7f) else color,
        border = borderColor?.let { BorderStroke(1.dp, it) },
        modifier = Modifier
            .scale(if (isPressed) 0.95f else 1f)
            .testTag(testTag)
            .shadow(3.dp, RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                color = textColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

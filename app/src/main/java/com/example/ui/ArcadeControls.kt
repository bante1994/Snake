package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Direction
import com.example.model.RetroTheme

@Composable
fun ArcadeControls(
    theme: RetroTheme,
    isPlaying: Boolean,
    isPaused: Boolean,
    onDirection: (Direction) -> Unit,
    onTogglePause: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Virtual D-Pad
        DpadController(
            theme = theme,
            onDirection = onDirection
        )

        // Action Buttons (Pause, Restart, Turbo)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Pause / Resume
            ArcadeActionButton(
                label = if (isPaused) "RESUME" else "PAUSE",
                icon = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                color = theme.accent,
                textColor = theme.background,
                onClick = onTogglePause,
                testTag = "btn_pause_toggle"
            )

            // Quick Restart / Replay
            ArcadeActionButton(
                label = "RESTART",
                icon = Icons.Default.Refresh,
                color = theme.snakeHead,
                textColor = theme.background,
                onClick = onRestart,
                testTag = "btn_restart_game"
            )
        }
    }
}

@Composable
fun DpadController(
    theme: RetroTheme,
    onDirection: (Direction) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(150.dp)
            .shadow(6.dp, CircleShape)
            .clip(CircleShape)
            .background(theme.boardBackground.copy(alpha = 0.85f))
            .border(2.dp, theme.gridColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Up
        DpadButton(
            direction = Direction.UP,
            icon = Icons.Default.KeyboardArrowUp,
            theme = theme,
            onClick = { onDirection(Direction.UP) },
            modifier = Modifier.align(Alignment.TopCenter)
        )
        // Down
        DpadButton(
            direction = Direction.DOWN,
            icon = Icons.Default.KeyboardArrowDown,
            theme = theme,
            onClick = { onDirection(Direction.DOWN) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        // Left
        DpadButton(
            direction = Direction.LEFT,
            icon = Icons.Default.KeyboardArrowLeft,
            theme = theme,
            onClick = { onDirection(Direction.LEFT) },
            modifier = Modifier.align(Alignment.CenterStart)
        )
        // Right
        DpadButton(
            direction = Direction.RIGHT,
            icon = Icons.Default.KeyboardArrowRight,
            theme = theme,
            onClick = { onDirection(Direction.RIGHT) },
            modifier = Modifier.align(Alignment.CenterEnd)
        )

        // Center Pivot
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(theme.background)
                .border(1.dp, theme.accent.copy(alpha = 0.4f), CircleShape)
        )
    }
}

@Composable
fun DpadButton(
    direction: Direction,
    icon: ImageVector,
    theme: RetroTheme,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .size(48.dp)
            .scale(if (isPressed) 0.92f else 1f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isPressed) theme.snakeHead else theme.boardBackground.copy(alpha = 0.5f))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("dpad_${direction.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Move ${direction.name}",
            tint = if (isPressed) theme.background else theme.hudText,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun ArcadeActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    textColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(10.dp),
        color = if (isPressed) color.copy(alpha = 0.7f) else color,
        modifier = Modifier
            .scale(if (isPressed) 0.95f else 1f)
            .testTag(testTag)
            .shadow(4.dp, RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = textColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }
    }
}

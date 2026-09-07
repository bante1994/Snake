package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Direction
import com.example.model.Food
import com.example.model.GameState
import com.example.model.Point
import com.example.model.RetroTheme
import com.example.model.WallMode
import kotlin.math.abs
import kotlin.math.min

@Composable
fun GameCanvas(
    gameState: GameState,
    theme: RetroTheme,
    scanlinesEnabled: Boolean,
    onDirectionChange: (Direction) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val goldenPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "goldenPulse"
    )

    val boardAspectRatio = remember(gameState.gridWidth, gameState.gridHeight) {
        (gameState.gridWidth.toFloat() / gameState.gridHeight.toFloat()).coerceIn(0.75f, 1.2f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(boardAspectRatio)
            .shadow(12.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(theme.boardBackground)
            .pointerInput(gameState.isPlaying) {
                var totalDx = 0f
                var totalDy = 0f
                detectDragGestures(
                    onDragStart = {
                        totalDx = 0f
                        totalDy = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        totalDx += dragAmount.x
                        totalDy += dragAmount.y
                    },
                    onDragEnd = {
                        val minDistance = 24f
                        if (abs(totalDx) > abs(totalDy) && abs(totalDx) > minDistance) {
                            if (totalDx > 0) onDirectionChange(Direction.RIGHT)
                            else onDirectionChange(Direction.LEFT)
                        } else if (abs(totalDy) > minDistance) {
                            if (totalDy > 0) onDirectionChange(Direction.DOWN)
                            else onDirectionChange(Direction.UP)
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridW = gameState.gridWidth
            val gridH = gameState.gridHeight
            val cellSize = min(size.width / gridW, size.height / gridH)
            val boardW = gridW * cellSize
            val boardH = gridH * cellSize
            val offsetX = (size.width - boardW) / 2f
            val offsetY = (size.height - boardH) / 2f

            translate(left = offsetX, top = offsetY) {
                // 1. Draw Grid Lines
                drawGridLines(gridW, gridH, cellSize, theme.gridColor, boardW, boardH)

                // 2. Draw Food
                drawFood(gameState.food, cellSize, theme, goldenPulse)

                // 3. Draw Snake Body & Head
                drawSnake(gameState.snake, gameState.direction, cellSize, theme)

                // 4. Draw Active Perimeter (Wall vs Wall-Less)
                drawPerimeter(boardW, boardH, gameState.wallMode, theme)

                // 5. CRT Scanlines Overlay
                if (scanlinesEnabled && theme.crtScanlines) {
                    drawCrtOverlay(boardW, boardH)
                }
            }
        }

        // Animated Level Up & Screen Size Expansion Banner
        AnimatedVisibility(
            visible = !gameState.levelUpAnnouncement.isNullOrBlank(),
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp)
        ) {
            Surface(
                color = theme.boardBackground.copy(alpha = 0.94f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.5.dp, theme.accent),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomOutMap,
                        contentDescription = null,
                        tint = theme.snakeHead,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = gameState.levelUpAnnouncement ?: "",
                        color = theme.hudText,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Active Wall Mode pill indicator in bottom-left corner
        Surface(
            color = theme.background.copy(alpha = 0.85f),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(
                1.dp,
                if (gameState.wallMode == WallMode.WALL) theme.accent.copy(alpha = 0.6f) else theme.snakeHead.copy(alpha = 0.6f)
            ),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (gameState.wallMode == WallMode.WALL) Icons.Default.Security else Icons.Default.AllInclusive,
                    contentDescription = null,
                    tint = if (gameState.wallMode == WallMode.WALL) theme.accent else theme.snakeHead,
                    modifier = Modifier.size(11.dp)
                )
                Text(
                    text = "${gameState.wallMode.label} • ${gameState.gridWidth}x${gameState.gridHeight}",
                    color = theme.hudText.copy(alpha = 0.85f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun DrawScope.drawPerimeter(
    boardW: Float,
    boardH: Float,
    mode: WallMode,
    theme: RetroTheme
) {
    if (mode == WallMode.WALL) {
        // Solid Electric Perimeter
        drawRect(
            color = theme.accent.copy(alpha = 0.85f),
            topLeft = Offset.Zero,
            size = Size(boardW, boardH),
            style = Stroke(width = 3.5f)
        )

        // Corner hazard brackets
        val cornerLen = 14f
        // Top-left
        drawLine(theme.snakeHead, Offset(0f, 0f), Offset(cornerLen, 0f), strokeWidth = 5f)
        drawLine(theme.snakeHead, Offset(0f, 0f), Offset(0f, cornerLen), strokeWidth = 5f)
        // Top-right
        drawLine(theme.snakeHead, Offset(boardW, 0f), Offset(boardW - cornerLen, 0f), strokeWidth = 5f)
        drawLine(theme.snakeHead, Offset(boardW, 0f), Offset(boardW, cornerLen), strokeWidth = 5f)
        // Bottom-left
        drawLine(theme.snakeHead, Offset(0f, boardH), Offset(cornerLen, boardH), strokeWidth = 5f)
        drawLine(theme.snakeHead, Offset(0f, boardH), Offset(0f, boardH - cornerLen), strokeWidth = 5f)
        // Bottom-right
        drawLine(theme.snakeHead, Offset(boardW, boardH), Offset(boardW - cornerLen, boardH), strokeWidth = 5f)
        drawLine(theme.snakeHead, Offset(boardW, boardH), Offset(boardW, boardH - cornerLen), strokeWidth = 5f)
    } else {
        // Wall-less: Dashed Wrap-Around Boundary
        drawRect(
            color = theme.snakeHead.copy(alpha = 0.7f),
            topLeft = Offset.Zero,
            size = Size(boardW, boardH),
            style = Stroke(
                width = 2.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
            )
        )
    }
}

private fun DrawScope.drawGridLines(cols: Int, rows: Int, cellSize: Float, color: Color, boardW: Float, boardH: Float) {
    for (i in 0..cols) {
        val x = i * cellSize
        drawLine(
            color = color,
            start = Offset(x, 0f),
            end = Offset(x, boardH),
            strokeWidth = 1f
        )
    }
    for (j in 0..rows) {
        val y = j * cellSize
        drawLine(
            color = color,
            start = Offset(0f, y),
            end = Offset(boardW, y),
            strokeWidth = 1f
        )
    }
}

private fun DrawScope.drawFood(food: Food, cellSize: Float, theme: RetroTheme, pulse: Float) {
    val cx = food.point.x * cellSize + cellSize / 2f
    val cy = food.point.y * cellSize + cellSize / 2f
    val radius = (cellSize / 2.3f) * if (food.isGolden) pulse else 1f

    if (food.isGolden) {
        // Golden Apple Halo
        drawCircle(
            color = theme.foodGolden.copy(alpha = 0.35f),
            radius = radius * 1.5f,
            center = Offset(cx, cy)
        )
        // Golden Core
        drawCircle(
            color = theme.foodGolden,
            radius = radius,
            center = Offset(cx, cy)
        )
        // Sparkle glint
        drawCircle(
            color = Color.White,
            radius = radius * 0.3f,
            center = Offset(cx - radius * 0.3f, cy - radius * 0.3f)
        )
    } else {
        // Pixel Apple
        val padding = cellSize * 0.12f
        val rectSize = cellSize - (padding * 2)
        val corner = CornerRadius(cellSize * 0.25f, cellSize * 0.25f)

        drawRoundRect(
            color = theme.foodRed,
            topLeft = Offset(food.point.x * cellSize + padding, food.point.y * cellSize + padding),
            size = Size(rectSize, rectSize),
            cornerRadius = corner
        )

        // Stem pixel
        val stemSize = cellSize * 0.2f
        drawRect(
            color = Color(0xFF8B5A2B),
            topLeft = Offset(cx - stemSize / 2f, food.point.y * cellSize + 1f),
            size = Size(stemSize, stemSize)
        )

        // Apple Specular Shine
        drawCircle(
            color = Color.White.copy(alpha = 0.6f),
            radius = cellSize * 0.12f,
            center = Offset(cx - cellSize * 0.16f, cy - cellSize * 0.16f)
        )
    }
}

private fun DrawScope.drawSnake(
    snake: List<Point>,
    direction: Direction,
    cellSize: Float,
    theme: RetroTheme
) {
    if (snake.isEmpty()) return

    val pad = cellSize * 0.08f
    val segSize = cellSize - (pad * 2)
    val bodyCorner = CornerRadius(cellSize * 0.28f, cellSize * 0.28f)

    // Draw Body segments
    for (i in 1 until snake.size) {
        val segment = snake[i]
        val x = segment.x * cellSize + pad
        val y = segment.y * cellSize + pad

        val alphaFade = 1f - (i.toFloat() / (snake.size * 1.8f)).coerceIn(0f, 0.4f)
        drawRoundRect(
            color = theme.snakeBody.copy(alpha = alphaFade),
            topLeft = Offset(x, y),
            size = Size(segSize, segSize),
            cornerRadius = bodyCorner
        )

        // Inner retro pixel core
        val corePad = cellSize * 0.22f
        drawRoundRect(
            color = theme.snakeHead.copy(alpha = 0.3f),
            topLeft = Offset(segment.x * cellSize + corePad, segment.y * cellSize + corePad),
            size = Size(cellSize - corePad * 2, cellSize - corePad * 2),
            cornerRadius = CornerRadius(4f, 4f)
        )
    }

    // Draw Head
    val head = snake.first()
    val headX = head.x * cellSize + pad
    val headY = head.y * cellSize + pad
    drawRoundRect(
        color = theme.snakeHead,
        topLeft = Offset(headX, headY),
        size = Size(segSize, segSize),
        cornerRadius = CornerRadius(cellSize * 0.35f, cellSize * 0.35f)
    )

    // Draw Eyes looking in Direction
    val eyeRadius = cellSize * 0.12f
    val pupilRadius = cellSize * 0.06f
    val cx = head.x * cellSize + cellSize / 2f
    val cy = head.y * cellSize + cellSize / 2f

    val (eye1Offset, eye2Offset) = when (direction) {
        Direction.UP -> Pair(
            Offset(cx - cellSize * 0.22f, cy - cellSize * 0.18f),
            Offset(cx + cellSize * 0.22f, cy - cellSize * 0.18f)
        )
        Direction.DOWN -> Pair(
            Offset(cx - cellSize * 0.22f, cy + cellSize * 0.18f),
            Offset(cx + cellSize * 0.22f, cy + cellSize * 0.18f)
        )
        Direction.LEFT -> Pair(
            Offset(cx - cellSize * 0.18f, cy - cellSize * 0.22f),
            Offset(cx - cellSize * 0.18f, cy + cellSize * 0.22f)
        )
        Direction.RIGHT -> Pair(
            Offset(cx + cellSize * 0.18f, cy - cellSize * 0.22f),
            Offset(cx + cellSize * 0.18f, cy + cellSize * 0.22f)
        )
    }

    // Eye whites
    drawCircle(color = Color.White, radius = eyeRadius, center = eye1Offset)
    drawCircle(color = Color.White, radius = eyeRadius, center = eye2Offset)

    // Eye pupils
    drawCircle(color = theme.snakeEye, radius = pupilRadius, center = eye1Offset)
    drawCircle(color = theme.snakeEye, radius = pupilRadius, center = eye2Offset)

    // Flickering Tongue
    val tongueColor = Color(0xFFFF2244)
    val tongueLen = cellSize * 0.26f
    val tongueWidth = cellSize * 0.08f
    when (direction) {
        Direction.UP -> drawRect(
            color = tongueColor,
            topLeft = Offset(cx - tongueWidth / 2f, head.y * cellSize - tongueLen + pad),
            size = Size(tongueWidth, tongueLen)
        )
        Direction.DOWN -> drawRect(
            color = tongueColor,
            topLeft = Offset(cx - tongueWidth / 2f, head.y * cellSize + cellSize - pad),
            size = Size(tongueWidth, tongueLen)
        )
        Direction.LEFT -> drawRect(
            color = tongueColor,
            topLeft = Offset(head.x * cellSize - tongueLen + pad, cy - tongueWidth / 2f),
            size = Size(tongueLen, tongueWidth)
        )
        Direction.RIGHT -> drawRect(
            color = tongueColor,
            topLeft = Offset(head.x * cellSize + cellSize - pad, cy - tongueWidth / 2f),
            size = Size(tongueLen, tongueWidth)
        )
    }
}

private fun DrawScope.drawCrtOverlay(width: Float, height: Float) {
    // Scanlines
    var y = 0f
    val step = 6f
    val scanlineColor = Color.Black.copy(alpha = 0.18f)
    while (y < height) {
        drawLine(
            color = scanlineColor,
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 2f
        )
        y += step
    }

    // Subtle edge vignette
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f)),
            center = Offset(width / 2f, height / 2f),
            radius = width * 0.72f
        )
    )
}

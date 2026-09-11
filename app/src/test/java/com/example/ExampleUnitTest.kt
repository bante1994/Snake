package com.example

import com.example.model.Direction
import com.example.model.Point
import com.example.model.WallMode
import com.example.util.AntiCheatEngine
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun wallLessMode_wrapLogic_isCorrect() {
    val gridWidth = 16
    val gridHeight = 18

    // Test moving left past the left border (x = -1)
    val leftWrapX = ((-1 % gridWidth) + gridWidth) % gridWidth
    assertEquals(15, leftWrapX)

    // Test moving right past the right border (x = 16)
    val rightWrapX = ((16 % gridWidth) + gridWidth) % gridWidth
    assertEquals(0, rightWrapX)

    // Test moving up past top border (y = -1)
    val topWrapY = ((-1 % gridHeight) + gridHeight) % gridHeight
    assertEquals(17, topWrapY)

    // Test moving down past bottom border (y = 18)
    val bottomWrapY = ((18 % gridHeight) + gridHeight) % gridHeight
    assertEquals(0, bottomWrapY)
  }

  @Test
  fun wallMode_labelsAndDescriptions_areValid() {
    assertEquals("WALLS", WallMode.WALL.label)
    assertEquals("WALL-LESS", WallMode.WALL_LESS.label)
    assertTrue(WallMode.WALL.description.contains("fatal") || WallMode.WALL.description.contains("Fatal"))
    assertTrue(WallMode.WALL_LESS.description.contains("wrap") || WallMode.WALL_LESS.description.contains("Wrap"))
  }

  @Test
  fun antiCheatEngine_legitimacyCheck_validatesLegitimateGame() {
    val duration = 30
    val apples = 5
    val score = 50
    val moves = 35
    val token = AntiCheatEngine.generateVerificationToken(
      score = score,
      applesEaten = apples,
      goldenApplesEaten = 0,
      movesCount = moves,
      durationSeconds = duration,
      seed = 42L
    )
    assertNotNull(token)
    val report = AntiCheatEngine.isScoreLegitimate(
      score = score,
      applesEaten = apples,
      movesCount = moves,
      durationSeconds = duration
    )
    assertTrue(report.isClean)
  }
}

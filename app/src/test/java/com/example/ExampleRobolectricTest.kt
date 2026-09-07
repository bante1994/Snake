package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.AntiCheatEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Retro Snake", appName)
  }

  @Test
  fun `anti cheat verification token generates without recursion error`() {
    val token = AntiCheatEngine.generateVerificationToken(
      score = 120,
      applesEaten = 12,
      goldenApplesEaten = 1,
      movesCount = 65,
      durationSeconds = 30,
      seed = 9999L
    )
    assertNotNull(token)
    assertTrue(token.startsWith("ACS-VERIFIED-"))

    val report = AntiCheatEngine.isScoreLegitimate(
      score = 120,
      applesEaten = 12,
      movesCount = 65,
      durationSeconds = 30
    )
    assertTrue(report.isClean)
    assertEquals("VERIFIED_CLEAN", report.integrityRating)
  }
}

package com.snake.arootx.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.snake.arootx.config.AdMobConfig
import com.snake.arootx.model.RetroTheme
import com.snake.arootx.model.RetroThemes
import com.snake.arootx.model.WallMode
import com.snake.arootx.util.AdLoadStatus
import com.snake.arootx.util.AdMobManager
import com.snake.arootx.viewmodel.SnakeGameViewModel

@Composable
fun ProfileScreen(
    viewModel: SnakeGameViewModel,
    modifier: Modifier = Modifier
) {
    val theme by viewModel.selectedTheme.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val adStatus by AdMobManager.adStatus.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var isEditingTag by remember { mutableStateOf(false) }
    var gamerTagInput by remember(profile.gamerTag) { mutableStateOf(profile.gamerTag) }

    val avatars = listOf(
        Pair("snake_classic", "Classic Viper"),
        Pair("snake_cyber", "Cyber Nexus"),
        Pair("snake_matrix", "Matrix Ghost"),
        Pair("snake_crown", "Arcade King"),
        Pair("snake_amber", "Phosphor 84"),
        Pair("snake_synth", "Retro Wave")
    )

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
                    text = "PILOT PROFILE & THEMES",
                    color = theme.hudText,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Player Handle, Hardware Themes & Gameplay Preferences",
                    color = theme.hudText.copy(alpha = 0.65f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Profile Identity Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.boardBackground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, theme.accent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Avatar Icon Circle
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(theme.snakeHead)
                                    .border(2.dp, theme.accent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = profile.gamerTag.take(2),
                                    color = theme.background,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                )
                            }

                            if (isEditingTag) {
                                OutlinedTextField(
                                    value = gamerTagInput,
                                    onValueChange = { gamerTagInput = it.take(16) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = theme.hudText,
                                        unfocusedTextColor = theme.hudText,
                                        focusedBorderColor = theme.accent,
                                        unfocusedBorderColor = theme.gridColor
                                    ),
                                    modifier = Modifier.width(160.dp).testTag("input_gamer_tag")
                                )
                            } else {
                                Column {
                                    Text(
                                        text = profile.gamerTag,
                                        color = theme.hudText,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "High Score: ${profile.highScore} pts",
                                        color = theme.foodGolden,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // Edit / Save button
                        IconButton(
                            onClick = {
                                if (isEditingTag) {
                                    viewModel.updateGamerTag(gamerTagInput)
                                    isEditingTag = false
                                } else {
                                    isEditingTag = true
                                }
                            },
                            modifier = Modifier.testTag("btn_edit_profile_tag")
                        ) {
                            Icon(
                                imageVector = if (isEditingTag) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = if (isEditingTag) "Save Handle" else "Edit Handle",
                                tint = theme.accent
                            )
                        }
                    }

                    // Anti-Cheat Integrity Status
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0C243B))
                            .border(1.dp, Color(0xFF00D2FF), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFF00D2FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "ANTI-CHEAT INTEGRITY: ${profile.integrityPercent}% CLEAN",
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }

                    // Retro Avatar Selector
                    Text(
                        text = "CHOOSE AVATAR CALLSIGN:",
                        color = theme.hudText.copy(alpha = 0.7f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(avatars) { (id, label) ->
                            val isSelected = profile.avatarId == id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) theme.snakeHead.copy(alpha = 0.25f) else theme.background)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.5.dp,
                                        color = if (isSelected) theme.snakeHead else theme.gridColor,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.updateAvatar(id) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) theme.snakeHead else theme.hudText.copy(alpha = 0.7f),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        // Color Themes Selector (Game Boy, Cyberpunk, Amber CRT, etc.)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.boardBackground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, theme.gridColor, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = theme.accent, modifier = Modifier.size(18.dp))
                        Text(
                            text = "RETRO HARDWARE COLOR THEMES",
                            color = theme.hudText,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    RetroThemes.ALL_THEMES.forEach { retroTheme ->
                        val isSelected = theme.id == retroTheme.id
                        ThemeSelectorRow(
                            themeItem = retroTheme,
                            isSelected = isSelected,
                            onSelect = { viewModel.setTheme(retroTheme) }
                        )
                    }
                }
            }
        }

        // Arena Border Mode & Dynamic Screen Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.boardBackground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, theme.gridColor, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = theme.accent, modifier = Modifier.size(18.dp))
                        Text(
                            text = "GAMEPLAY BORDER & SCREEN SETTINGS",
                            color = theme.hudText,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Text(
                        text = "Choose your default border mechanics. In Wall mode, hitting edges is lethal. In Wall-less mode, the snake teleports through opposite boundaries.",
                        color = theme.hudText.copy(alpha = 0.7f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isWall = (profile.wallMode == WallMode.WALL.name)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isWall) theme.accent else theme.background,
                            border = BorderStroke(1.dp, if (isWall) theme.accent else theme.gridColor),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setWallMode(WallMode.WALL) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Security,
                                    contentDescription = null,
                                    tint = if (isWall) Color.Black else theme.hudText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Column {
                                    Text(
                                        "WALLS",
                                        color = if (isWall) Color.Black else theme.hudText,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        "Fatal Bounds",
                                        color = if (isWall) Color.Black.copy(alpha = 0.7f) else theme.hudText.copy(alpha = 0.6f),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }

                        val isWallLess = (profile.wallMode == WallMode.WALL_LESS.name)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isWallLess) theme.snakeHead else theme.background,
                            border = BorderStroke(1.dp, if (isWallLess) theme.snakeHead else theme.gridColor),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setWallMode(WallMode.WALL_LESS) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.AllInclusive,
                                    contentDescription = null,
                                    tint = if (isWallLess) Color.Black else theme.hudText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Column {
                                    Text(
                                        "WALL-LESS",
                                        color = if (isWallLess) Color.Black else theme.hudText,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        "Boundary Wrap",
                                        color = if (isWallLess) Color.Black.copy(alpha = 0.7f) else theme.hudText.copy(alpha = 0.6f),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }

                    // Dynamic Arena screen info
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(theme.background.copy(alpha = 0.5f))
                            .border(0.5.dp, theme.foodGolden.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.ZoomOutMap, contentDescription = null, tint = theme.foodGolden, modifier = Modifier.size(14.dp))
                        Text(
                            text = "Dynamic Grid: Screen size automatically expands from 16x18 up to 24x28 as your level advances!",
                            color = theme.hudText.copy(alpha = 0.85f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }

        // Hardware & Gameplay Switches (Sound, Haptic, Scanlines)
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
                        text = "HARDWARE SIMULATION SETTINGS",
                        color = theme.hudText,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )

                    // Sound switch
                    SettingToggleRow(
                        title = "8-Bit Retro Sound FX",
                        subtitle = "Synthesized real-time square & sawtooth waves",
                        checked = profile.soundEnabled,
                        theme = theme,
                        onCheckedChange = {
                            viewModel.updateSettings(
                                sound = it,
                                vibe = profile.vibrationEnabled,
                                scanlines = profile.scanlinesEnabled
                            )
                        }
                    )

                    // Vibration switch
                    SettingToggleRow(
                        title = "Tactile Haptic Feedback",
                        subtitle = "Vibration pulses on turn, bite & collision",
                        checked = profile.vibrationEnabled,
                        theme = theme,
                        onCheckedChange = {
                            viewModel.updateSettings(
                                sound = profile.soundEnabled,
                                vibe = it,
                                scanlines = profile.scanlinesEnabled
                            )
                        }
                    )

                    // CRT Scanlines switch
                    SettingToggleRow(
                        title = "Cathode CRT Scanlines",
                        subtitle = "Retro phosphor glow & raster scan overlay",
                        checked = profile.scanlinesEnabled,
                        theme = theme,
                        onCheckedChange = {
                            viewModel.updateSettings(
                                sound = profile.soundEnabled,
                                vibe = profile.vibrationEnabled,
                                scanlines = it
                            )
                        }
                    )
                }
            }
        }

        // Google AdMob Configuration & Live Testing Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.boardBackground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, theme.foodGolden.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            text = "GOOGLE ADMOB STATUS & LIVE TEST",
                            color = theme.foodGolden,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    // Credentials Preview
                    Surface(
                        color = theme.background.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(0.5.dp, theme.gridColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "App ID: ${AdMobConfig.ADMOB_APP_ID.take(30)}...",
                                color = theme.hudText.copy(alpha = 0.7f),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "Unit ID: ${AdMobConfig.ADMOB_REWARDED_AD_UNIT_ID.take(30)}...",
                                color = theme.hudText.copy(alpha = 0.7f),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Live Connection Status
                    val statusText = when (val s = adStatus) {
                        is AdLoadStatus.Ready -> if (s.isTestFallback) "✓ Google Ad Ready (Test Fallback: Live ID Pending Fill)" else "✓ Google Live Ad Ready"
                        is AdLoadStatus.Loading -> "⏳ Connecting to Google AdMob servers..."
                        is AdLoadStatus.Failed -> "⚠️ Error Code ${s.errorCode}: ${s.message}\n${s.explanation}"
                        is AdLoadStatus.Showing -> "▶ Fullscreen Video Ad Displaying"
                        is AdLoadStatus.Idle -> "Initializing / Preloading Ad..."
                    }
                    val statusColor = when (adStatus) {
                        is AdLoadStatus.Ready -> theme.snakeHead
                        is AdLoadStatus.Loading -> theme.accent
                        is AdLoadStatus.Failed -> theme.foodGolden
                        else -> theme.hudText
                    }

                    Text(
                        text = statusText,
                        color = statusColor,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )

                    // Note on 24-48h activation window for real credentials
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1B2838))
                            .padding(8.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF66C0F4),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Why Real Ads Don't Show Immediately: Fresh Ad Units take 24–48 hours for Google to start serving ads (Error Code 3: No Fill). Automatic Fallback is enabled so you can test the video ad immediately without getting blocked.",
                            color = Color(0xFFDCDEDF),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            lineHeight = 13.sp
                        )
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val activity = context.findActivity()
                                if (activity != null) {
                                    AdMobManager.showRewardedAd(
                                        activity = activity,
                                        onRewardEarned = {
                                            Toast.makeText(context, "🎉 Rewarded ad watched! Reward confirmed.", Toast.LENGTH_SHORT).show()
                                        },
                                        onAdDismissed = {
                                            // dismiss
                                        },
                                        onAdUnavailable = { reason ->
                                            Toast.makeText(context, "Ad not ready: $reason", Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.foodGolden, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("TEST AD NOW", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                AdMobManager.preloadAd(context)
                                Toast.makeText(context, "Requesting ad from Google AdMob...", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.accent, contentColor = theme.background),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("RELOAD AD", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
        }
    }
}

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@Composable
fun ThemeSelectorRow(
    themeItem: RetroTheme,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) themeItem.boardBackground else Color.Transparent)
            .border(
                width = if (isSelected) 1.5.dp else 0.5.dp,
                color = if (isSelected) themeItem.accent else Color.Gray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onSelect)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = themeItem.name,
                color = themeItem.hudText,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = themeItem.era,
                color = themeItem.hudText.copy(alpha = 0.6f),
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            )
        }

        // Color Swatches
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(Modifier.size(16.dp).clip(CircleShape).background(themeItem.snakeHead))
            Box(Modifier.size(16.dp).clip(CircleShape).background(themeItem.foodRed))
            Box(Modifier.size(16.dp).clip(CircleShape).background(themeItem.boardBackground))
        }
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    theme: RetroTheme,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = theme.hudText,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Text(
                text = subtitle,
                color = theme.hudText.copy(alpha = 0.6f),
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = theme.snakeHead,
                checkedTrackColor = theme.snakeHead.copy(alpha = 0.4f),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.DarkGray
            )
        )
    }
}

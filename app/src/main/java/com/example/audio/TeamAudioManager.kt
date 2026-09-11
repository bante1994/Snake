package com.example.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong

enum class TeamCalloutType {
    PING,
    LEVEL_UP,
    DANGER,
    CHEER,
    GOLDEN_FOOD,
    REVIVE,
    DEFEND,
    GENERAL
}

data class TeamCallout(
    val id: Long,
    val senderTag: String,
    val message: String,
    val timestampMs: Long,
    val type: TeamCalloutType
)

class TeamAudioManager(
    private val context: Context,
    private val audioSynthesizer: RetroAudioSynthesizer,
    private val scope: CoroutineScope
) {
    private val idCounter = AtomicLong(1L)

    private val _teamAudioEnabled = MutableStateFlow(true)
    val teamAudioEnabled = _teamAudioEnabled.asStateFlow()

    private val _isTtsReady = MutableStateFlow(false)
    val isTtsReady = _isTtsReady.asStateFlow()

    private val _activeSpeaker = MutableStateFlow<String?>(null)
    val activeSpeaker = _activeSpeaker.asStateFlow()

    private val _latestCallout = MutableStateFlow<TeamCallout?>(
        TeamCallout(
            id = 0L,
            senderTag = "VIPER_LEAD",
            message = "Alpha Squad comlink online. Swipe screen to steer.",
            timestampMs = System.currentTimeMillis(),
            type = TeamCalloutType.GENERAL
        )
    )
    val latestCallout = _latestCallout.asStateFlow()

    private val _recentCallouts = MutableStateFlow<List<TeamCallout>>(
        listOf(
            TeamCallout(0L, "VIPER_LEAD", "Alpha Squad comlink online. Swipe screen to steer.", System.currentTimeMillis(), TeamCalloutType.GENERAL)
        )
    )
    val recentCallouts = _recentCallouts.asStateFlow()

    private var textToSpeech: TextToSpeech? = null
    private var clearSpeakerJob: Job? = null
    private var lastSpokenTimeMs = 0L

    init {
        try {
            textToSpeech = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = textToSpeech?.setLanguage(Locale.US)
                    if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                        textToSpeech?.setPitch(1.15f)
                        textToSpeech?.setSpeechRate(1.08f)
                        _isTtsReady.value = true
                    }
                }
            }

            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    scope.launch(Dispatchers.Main) {
                        _activeSpeaker.value = null
                    }
                }
                override fun onError(utteranceId: String?) {
                    scope.launch(Dispatchers.Main) {
                        _activeSpeaker.value = null
                    }
                }
            })
        } catch (_: Exception) {
            _isTtsReady.value = false
        }
    }

    fun toggleTeamAudio(): Boolean {
        val newState = !_teamAudioEnabled.value
        _teamAudioEnabled.value = newState
        if (newState) {
            audioSynthesizer.playRadioChirp()
            broadcastCallout("HQ_COMMS", "Team audio channel active.", TeamCalloutType.GENERAL, speak = true)
        } else {
            audioSynthesizer.playRadioSquelch()
            textToSpeech?.stop()
            _activeSpeaker.value = null
        }
        return newState
    }

    fun setTeamAudioEnabled(enabled: Boolean) {
        _teamAudioEnabled.value = enabled
        if (!enabled) {
            textToSpeech?.stop()
            _activeSpeaker.value = null
        }
    }

    fun broadcastCallout(
        senderTag: String,
        message: String,
        type: TeamCalloutType,
        speak: Boolean = true
    ) {
        val callout = TeamCallout(
            id = idCounter.incrementAndGet(),
            senderTag = senderTag,
            message = message,
            timestampMs = System.currentTimeMillis(),
            type = type
        )

        _latestCallout.value = callout
        _recentCallouts.update { current ->
            (listOf(callout) + current).take(8)
        }

        if (!_teamAudioEnabled.value) return

        when (type) {
            TeamCalloutType.PING, TeamCalloutType.GOLDEN_FOOD -> audioSynthesizer.playTeamPing()
            TeamCalloutType.DANGER -> audioSynthesizer.playTeamWarning()
            TeamCalloutType.CHEER, TeamCalloutType.LEVEL_UP -> audioSynthesizer.playRadioChirp()
            else -> audioSynthesizer.playRadioSquelch()
        }

        if (speak && _isTtsReady.value) {
            val now = System.currentTimeMillis()
            // Avoid overlapping chatter if spoken too frequently
            if (now - lastSpokenTimeMs > 1800L || type == TeamCalloutType.REVIVE || type == TeamCalloutType.GOLDEN_FOOD) {
                lastSpokenTimeMs = now
                _activeSpeaker.value = senderTag
                try {
                    val utteranceId = "callout_${callout.id}"
                    textToSpeech?.speak(message, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
                } catch (_: Exception) {
                    _activeSpeaker.value = null
                }

                clearSpeakerJob?.cancel()
                clearSpeakerJob = scope.launch {
                    delay(3500)
                    _activeSpeaker.value = null
                }
            }
        }
    }

    fun onMatchStart() {
        val greetings = listOf(
            "Squad Alpha deployed. Clear the grid!",
            "Radio check loud and clear. Good hunting!",
            "Comms live. Swipe to steer!"
        )
        broadcastCallout("VIPER_LEAD", greetings.random(), TeamCalloutType.GENERAL, speak = true)
    }

    fun onAppleEaten(apples: Int, score: Int) {
        if (apples % 5 == 0) {
            val phrases = listOf(
                "Target secured! $score points and counting!",
                "Great trajectory! Keep feeding the viper!",
                "Perimeter is looking clear, stay focused!",
                "Solid steering, squad!"
            )
            val squadMates = listOf("NEO_GRID_99", "8BIT_GHOST", "CYBER_SYNTH")
            broadcastCallout(squadMates.random(), phrases.random(), TeamCalloutType.CHEER, speak = true)
        }
    }

    fun onGoldenAppleSpawned() {
        val phrases = listOf(
            "Golden relic detected on radar! Grab it fast!",
            "High value bonus target active! Intercept now!",
            "Golden apple inbound, team!"
        )
        broadcastCallout("8BIT_GHOST", phrases.random(), TeamCalloutType.GOLDEN_FOOD, speak = true)
    }

    fun onLevelUp(newLevel: Int, gridDims: String) {
        val phrases = listOf(
            "Arena expanded to $gridDims! Speed tier $newLevel engaged!",
            "Grid widening to $gridDims! Watch your acceleration!",
            "Arena grew to $gridDims! Full throttle squad!"
        )
        broadcastCallout("CYBER_SYNTH", phrases.random(), TeamCalloutType.LEVEL_UP, speak = true)
    }

    fun onDangerAlert() {
        val phrases = listOf(
            "Watch the boundary! Sharp turn!",
            "Caution! Wall proximity warning!",
            "Danger close! Swipe to dodge!"
        )
        broadcastCallout("NEO_GRID_99", phrases.random(), TeamCalloutType.DANGER, speak = true)
    }

    fun onGameOver(score: Int) {
        val phrases = listOf(
            "Snake crashed! Requesting squad revive!",
            "Collision detected! Watch ad to continue run!",
            "Grid signal lost at $score points. Stand by!"
        )
        broadcastCallout("VIPER_LEAD", phrases.random(), TeamCalloutType.GENERAL, speak = true)
    }

    fun onRevived() {
        val phrases = listOf(
            "Cyber shield deployed! Back in the fight squad!",
            "Revive confirmed! 3.5s shield active, roll out!",
            "We're back! Make this run count!"
        )
        broadcastCallout("VIPER_LEAD", phrases.random(), TeamCalloutType.REVIVE, speak = true)
    }

    fun sendUserCallout(type: TeamCalloutType, userTag: String) {
        val (userMsg, replyMate, replyMsg) = when (type) {
            TeamCalloutType.PING -> Triple(
                "Ping marked on sector!",
                "8BIT_GHOST",
                "Copy ping! Moving to intercept!"
            )
            TeamCalloutType.CHEER -> Triple(
                "Let's get that high score squad!",
                "NEO_GRID_99",
                "Behind you all the way! Let's roll!"
            )
            TeamCalloutType.DEFEND -> Triple(
                "Holding center line, watch flanks!",
                "CYBER_SYNTH",
                "Flanks covered! Clear path ahead!"
            )
            else -> Triple(
                "Radio check!",
                "VIPER_LEAD",
                "Read you 5 by 5, pilot!"
            )
        }

        broadcastCallout(userTag, userMsg, type, speak = false)
        scope.launch {
            delay(500)
            broadcastCallout(replyMate, replyMsg, type, speak = true)
        }
    }

    fun destroy() {
        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (_: Exception) {}
        textToSpeech = null
    }
}

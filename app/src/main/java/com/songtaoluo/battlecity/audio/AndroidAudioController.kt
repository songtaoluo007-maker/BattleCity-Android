package com.songtaoluo.battlecity.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.media.ToneGenerator
import java.io.Closeable

/**
 * Android audio backend.
 *
 * Short effects use SoundPool for concurrent playback. Music uses exactly one
 * MediaPlayer, avoiding the AVPlayer pool/reset races found in the HarmonyOS source.
 */
class AndroidAudioController(
    context: Context,
) : Closeable {
    private val appContext = context.applicationContext
    private val loadedSoundIds = mutableSetOf<Int>()
    private val soundPool: SoundPool
    private val soundIds: Map<AudioCue, Int>
    private val toneGenerator: ToneGenerator? = runCatching {
        ToneGenerator(AudioManager.STREAM_MUSIC, 58)
    }.getOrNull()

    private var musicPlayer: MediaPlayer? = null
    private var currentMusicTheme: MusicTheme? = null
    private var enabled = true
    private var effectsVolume = 0.78f
    private var musicVolume = 0.72f

    init {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setAudioAttributes(attributes)
            .setMaxStreams(8)
            .build()
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) loadedSoundIds += sampleId
        }
        soundIds = AudioCue.entries.mapNotNull { cue ->
            rawResourceId(cue.resourceStem)
                .takeIf { it != 0 }
                ?.let { resourceId -> cue to soundPool.load(appContext, resourceId, 1) }
        }.toMap()
    }

    fun setEnabled(value: Boolean) {
        enabled = value
        if (!value) stopMusic()
    }

    fun setEffectsVolume(value: Float) {
        effectsVolume = value.coerceIn(0f, 1f)
    }

    fun setMusicVolume(value: Float) {
        musicVolume = value.coerceIn(0f, 1f)
        runCatching { musicPlayer?.setVolume(musicVolume, musicVolume) }
    }

    fun play(cue: AudioCue) {
        if (!enabled) return
        val soundId = soundIds[cue]
        if (soundId != null && soundId in loadedSoundIds) {
            soundPool.play(soundId, effectsVolume, effectsVolume, 1, 0, 1f)
        } else if (cue !in setOf(AudioCue.VICTORY, AudioCue.DEFEAT)) {
            playFallback(cue)
        }
    }

    fun switchMusic(theme: MusicTheme, loop: Boolean = true) {
        if (!enabled || currentMusicTheme == theme) return

        stopMusic()
        val resourceId = resolveMusicResource(theme)
        if (resourceId == 0) return

        musicPlayer = runCatching {
            MediaPlayer.create(appContext, resourceId)?.apply {
                isLooping = loop
                setVolume(musicVolume, musicVolume)
                start()
            }
        }.getOrNull()
        if (musicPlayer != null) currentMusicTheme = theme
    }

    fun stopMusic() {
        currentMusicTheme = null
        val player = musicPlayer
        musicPlayer = null
        if (player != null) {
            runCatching { player.stop() }
            runCatching { player.release() }
        }
    }

    override fun close() {
        stopMusic()
        runCatching { soundPool.release() }
        runCatching { toneGenerator?.release() }
        loadedSoundIds.clear()
    }

    private fun resolveMusicResource(theme: MusicTheme): Int {
        val preferred = rawResourceId(theme.resourceStem)
        if (preferred != 0) return preferred
        if (theme != MusicTheme.NEUTRAL_MENU) {
            return rawResourceId(MusicTheme.NEUTRAL_MENU.resourceStem)
        }
        return 0
    }

    private fun rawResourceId(stem: String): Int =
        appContext.resources.getIdentifier(stem, "raw", appContext.packageName)

    private fun playFallback(cue: AudioCue) {
        val generator = toneGenerator ?: return
        val (tone, durationMs) = when (cue) {
            AudioCue.SHOOT -> ToneGenerator.TONE_PROP_BEEP to 55
            AudioCue.HIT -> ToneGenerator.TONE_PROP_ACK to 65
            AudioCue.EXPLOSION -> ToneGenerator.TONE_PROP_NACK to 150
            AudioCue.PICKUP -> ToneGenerator.TONE_PROP_ACK to 100
            AudioCue.COMMAND -> ToneGenerator.TONE_PROP_BEEP to 90
            AudioCue.SUPPORT -> ToneGenerator.TONE_PROP_ACK to 140
            AudioCue.VICTORY -> ToneGenerator.TONE_PROP_ACK to 240
            AudioCue.DEFEAT -> ToneGenerator.TONE_PROP_NACK to 260
        }
        runCatching { generator.startTone(tone, durationMs) }
    }
}

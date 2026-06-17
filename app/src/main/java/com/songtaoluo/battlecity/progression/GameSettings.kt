package com.songtaoluo.battlecity.progression

data class GameSettings(
    val musicEnabled: Boolean = true,
    val effectsEnabled: Boolean = true,
    val musicVolume: Float = 0.72f,
    val effectsVolume: Float = 0.78f,
    val vibrationEnabled: Boolean = true,
) {
    fun normalized(): GameSettings = copy(
        musicVolume = musicVolume.coerceIn(0f, 1f),
        effectsVolume = effectsVolume.coerceIn(0f, 1f),
    )
}

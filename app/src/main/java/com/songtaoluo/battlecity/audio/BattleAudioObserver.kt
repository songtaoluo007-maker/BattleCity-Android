package com.songtaoluo.battlecity.audio

import com.songtaoluo.battlecity.game.Bullet
import com.songtaoluo.battlecity.game.GameEngine
import com.songtaoluo.battlecity.game.ImpactEffect
import com.songtaoluo.battlecity.game.ImpactEffectKind
import java.util.Collections
import java.util.IdentityHashMap

/**
 * Converts public battle state transitions into audio cues without putting
 * Android audio classes inside the deterministic game engine.
 */
class BattleAudioObserver {
    private val knownBullets = identitySet<Bullet>()
    private val knownEffects = identitySet<ImpactEffect>()
    private var initialized = false
    private var previousPowerUpCount = 0
    private var previousCommandPoints = 0
    private var previousOrder: Any? = null
    private var smokeWasActive = false
    private var reconWasActive = false
    private var victoryWasActive = false
    private var defeatWasActive = false

    fun collect(engine: GameEngine): List<AudioCue> {
        if (!initialized) {
            initialized = true
            sync(engine)
            return emptyList()
        }

        val cues = linkedSetOf<AudioCue>()

        if (engine.bullets.any { it !in knownBullets }) {
            cues += AudioCue.SHOOT
        }

        engine.effects
            .asSequence()
            .filter { it !in knownEffects }
            .mapTo(cues) { effect ->
                when (effect.kind) {
                    ImpactEffectKind.SPARK,
                    ImpactEffectKind.HIT_FLASH,
                    -> AudioCue.HIT
                    ImpactEffectKind.DESTROY_FLASH -> AudioCue.EXPLOSION
                }
            }

        if (engine.powerUps.size < previousPowerUpCount &&
            engine.commandPoints > previousCommandPoints
        ) {
            cues += AudioCue.PICKUP
        }

        if (previousOrder != engine.squadOrder) {
            cues += AudioCue.COMMAND
        }

        val smokeActive = engine.smokeArea?.active == true
        val reconActive = engine.reconArea?.active == true
        if ((!smokeWasActive && smokeActive) || (!reconWasActive && reconActive)) {
            cues += AudioCue.SUPPORT
        }

        if (!victoryWasActive && engine.victory) cues += AudioCue.VICTORY
        if (!defeatWasActive && engine.gameOver) cues += AudioCue.DEFEAT

        sync(engine)
        return cues.toList()
    }

    fun reset(engine: GameEngine? = null) {
        initialized = engine != null
        knownBullets.clear()
        knownEffects.clear()
        previousPowerUpCount = 0
        previousCommandPoints = 0
        previousOrder = null
        smokeWasActive = false
        reconWasActive = false
        victoryWasActive = false
        defeatWasActive = false
        if (engine != null) sync(engine)
    }

    private fun sync(engine: GameEngine) {
        knownBullets.clear()
        knownBullets.addAll(engine.bullets)
        knownEffects.clear()
        knownEffects.addAll(engine.effects)
        previousPowerUpCount = engine.powerUps.size
        previousCommandPoints = engine.commandPoints
        previousOrder = engine.squadOrder
        smokeWasActive = engine.smokeArea?.active == true
        reconWasActive = engine.reconArea?.active == true
        victoryWasActive = engine.victory
        defeatWasActive = engine.gameOver
    }

    private fun <T> identitySet(): MutableSet<T> =
        Collections.newSetFromMap(IdentityHashMap<T, Boolean>())
}

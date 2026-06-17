package com.songtaoluo.battlecity.audio

import com.songtaoluo.battlecity.game.GameEngine
import com.songtaoluo.battlecity.game.ImpactEffectSystem
import com.songtaoluo.battlecity.game.PowerUp
import com.songtaoluo.battlecity.game.ScenarioCatalog
import com.songtaoluo.battlecity.game.Vec2
import com.songtaoluo.battlecity.model.PowerUpType
import com.songtaoluo.battlecity.model.SquadOrder
import com.songtaoluo.battlecity.model.SupportSkillType
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class BattleAudioObserverTest {
    @Test
    fun detectsNewShotAndVisualImpactEvents() {
        val engine = GameEngine(Random(41))
        val observer = BattleAudioObserver()
        observer.collect(engine)

        engine.fire()
        assertTrue(AudioCue.SHOOT in observer.collect(engine))

        engine.effects += ImpactEffectSystem.hitFlash(Vec2(50f, 50f))
        assertTrue(AudioCue.HIT in observer.collect(engine))

        engine.effects += ImpactEffectSystem.destroyFlash(Vec2(70f, 70f))
        assertTrue(AudioCue.EXPLOSION in observer.collect(engine))
    }

    @Test
    fun detectsCommandSupportAndPickupTransitions() {
        val engine = GameEngine(Random(42))
        val observer = BattleAudioObserver()
        observer.collect(engine)

        engine.setSquadOrder(SquadOrder.HOLD)
        assertTrue(AudioCue.COMMAND in observer.collect(engine))

        engine.useSupportSkill(SupportSkillType.SMOKE_SCREEN)
        assertTrue(AudioCue.SUPPORT in observer.collect(engine))

        engine.powerUps += PowerUp(
            PowerUpType.SHIELD,
            Vec2(engine.player.position.x, engine.player.position.y),
        )
        observer.collect(engine)
        engine.update(0f, null)
        assertTrue(AudioCue.PICKUP in observer.collect(engine))
    }

    @Test
    fun detectsMissionVictoryAndDefeatOnce() {
        val victoryScenario = ScenarioCatalog.kurskSovietDefense.copy(
            enemyBudget = 0,
            maxActiveEnemies = 0,
            enemySpawns = emptyList(),
            objective = ScenarioCatalog.kurskSovietDefense.objective.copy(
                requiredKills = 0,
                holdMs = 1,
            ),
        )
        val victoryEngine = GameEngine(Random(43), victoryScenario)
        val victoryObserver = BattleAudioObserver()
        victoryObserver.collect(victoryEngine)
        victoryEngine.update(0.001f, null)
        assertTrue(AudioCue.VICTORY in victoryObserver.collect(victoryEngine))
        assertTrue(AudioCue.VICTORY !in victoryObserver.collect(victoryEngine))

        val defeatEngine = GameEngine(Random(44))
        val defeatObserver = BattleAudioObserver()
        defeatObserver.collect(defeatEngine)
        defeatEngine.player.alive = false
        defeatEngine.update(0f, null)
        assertTrue(AudioCue.DEFEAT in defeatObserver.collect(defeatEngine))
    }
}

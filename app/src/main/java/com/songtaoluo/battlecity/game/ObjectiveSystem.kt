package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.ObjectiveState
import com.songtaoluo.battlecity.model.ObjectiveType
import kotlin.math.sqrt

enum class ObjectiveOutcome {
    ACTIVE,
    VICTORY,
    DEFEAT,
}

data class ObjectiveEvaluation(
    val outcome: ObjectiveOutcome,
    val killsComplete: Boolean,
    val positionComplete: Boolean,
    val timeComplete: Boolean,
    val remainingMs: Long,
)

object ObjectiveSystem {
    fun evaluate(
        objective: ObjectiveState,
        elapsedMs: Long,
        destroyedEnemies: Int,
        friendlies: List<Tank>,
        targetCenter: Vec2,
        playerAlive: Boolean,
        baseDestroyed: Boolean,
    ): ObjectiveEvaluation {
        val killsComplete = objective.requiredKills <= 0 ||
            destroyedEnemies >= objective.requiredKills
        val positionComplete = friendlies.any { friendly ->
            friendly.alive && distance(friendly.position, targetCenter) <= objective.radius
        }
        val timeComplete = objective.holdMs > 0 && elapsedMs >= objective.holdMs
        val remainingMs = (objective.holdMs - elapsedMs).coerceAtLeast(0L)

        val defeat = !playerAlive ||
            (baseDestroyed && objective.type in setOf(ObjectiveType.DEFEND, ObjectiveType.SURVIVE))
        val victory = when (objective.type) {
            ObjectiveType.BREAKTHROUGH -> killsComplete && positionComplete
            ObjectiveType.DESTROY -> killsComplete
            ObjectiveType.DEFEND -> !baseDestroyed && (timeComplete || killsComplete)
            ObjectiveType.SURVIVE -> !baseDestroyed && (timeComplete || killsComplete)
        }

        return ObjectiveEvaluation(
            outcome = when {
                defeat -> ObjectiveOutcome.DEFEAT
                victory -> ObjectiveOutcome.VICTORY
                else -> ObjectiveOutcome.ACTIVE
            },
            killsComplete = killsComplete,
            positionComplete = positionComplete,
            timeComplete = timeComplete,
            remainingMs = remainingMs,
        )
    }

    private fun distance(first: Vec2, second: Vec2): Float {
        val dx = first.x - second.x
        val dy = first.y - second.y
        return sqrt(dx * dx + dy * dy)
    }
}

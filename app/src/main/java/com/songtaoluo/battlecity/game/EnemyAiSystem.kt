package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.Direction
import com.songtaoluo.battlecity.model.TileType

object EnemyAiSystem {
    fun update(
        enemy: Tank,
        target: Tank,
        deltaSeconds: Float,
        tiles: List<List<TileType>>,
        blockers: List<Tank>,
        canSeeTarget: Boolean = true,
        fire: (Tank) -> Unit,
    ) = EnemyAiController.update(
        enemy,
        target,
        deltaSeconds,
        tiles,
        blockers,
        canSeeTarget,
        fire,
    )

    fun firingDirection(source: Tank, target: Tank): Direction? =
        EnemyAiController.firingDirection(source, target)

    fun preferredDirections(source: Tank, target: Tank): List<Direction> =
        EnemyAiController.preferredDirections(source, target)

    fun hasClearLineOfFire(
        source: Tank,
        target: Tank,
        tiles: List<List<TileType>>,
    ): Boolean = EnemyAiController.hasClearLineOfFire(source, target, tiles)

    fun distanceBetween(a: Tank, b: Tank): Float = EnemyAiController.distanceBetween(a, b)
}

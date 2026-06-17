package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.SquadOrder
import com.songtaoluo.battlecity.model.TileType

object AllyAiSystem {
    fun update(
        ally: Tank,
        player: Tank,
        enemies: List<Tank>,
        order: SquadOrder,
        deltaSeconds: Float,
        tiles: List<List<TileType>>,
        blockers: List<Tank>,
        objective: Vec2,
        fire: (Tank) -> Unit,
    ) = AllyAiController.update(
        ally,
        player,
        enemies,
        order,
        deltaSeconds,
        tiles,
        blockers,
        objective,
        fire,
    )

    fun formationPoint(ally: Tank, player: Tank): Vec2 =
        AllyAiController.formationPoint(ally, player)

    fun isOnFormationSide(ally: Tank, player: Tank): Boolean =
        AllyAiController.isOnFormationSide(ally, player)

    fun nearestEnemy(source: Tank, enemies: List<Tank>): Tank? =
        AllyAiController.nearestEnemy(source, enemies)
}

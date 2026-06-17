package com.songtaoluo.battlecity.model

data class GridPoint(
    val x: Int,
    val y: Int,
)

data class ObjectiveState(
    val type: ObjectiveType,
    val title: String,
    val detail: String,
    val targetX: Int,
    val targetY: Int,
    val radius: Float,
    val requiredKills: Int,
    val holdMs: Long,
    val complete: Boolean = false,
)

data class ScenarioData(
    val id: String,
    val name: String,
    val faction: Faction,
    val opponent: Faction,
    val operation: String,
    val historicalBackground: String,
    val briefing: String,
    val playerSpawn: GridPoint,
    val allySpawns: List<GridPoint>,
    val enemySpawns: List<GridPoint>,
    val allyVehicles: List<VehicleId>,
    val enemyVehicles: List<VehicleId>,
    val enemyBudget: Int,
    val maxActiveEnemies: Int,
    val objective: ObjectiveState,
    val map: List<String>,
)

package com.songtaoluo.battlecity.game

object GameConstants {
    const val BOARD_TILES = 17
    const val TILE_SIZE = 32f
    const val BOARD_SIZE = BOARD_TILES * TILE_SIZE
    const val TANK_SIZE = 26f
    const val SCOUT_SIZE = 24f
    const val PLAYER_ID = 1
    const val WALL_CLEARANCE = 2f
    const val TANK_MIN_SPACING = 4f
    const val WATER_SPEED_MULTIPLIER = 0.52f

    const val AI_BLOCKED_MS = 380f
    const val AI_FIRE_ALIGNMENT = 18f
    const val AI_DIRECTION_RECHECK_MIN_MS = 280f
    const val AI_DIRECTION_RECHECK_MAX_MS = 800f
    const val ENEMY_SPAWN_BASE_MS = 1180f

    const val PEN_RICOCHET_THRESHOLD = 0.5f
    const val PEN_ANGLE_BLEND = 0.4f

    const val HEAVY_KILL_REWARD = 360
    const val SCOUT_KILL_REWARD = 180
    const val DEFAULT_KILL_REWARD = 240

    const val BULLET_COLLISION_RADIUS = 6f
    const val POWER_UP_DROP_RATE = 0.35f
    const val PITY_KILL_THRESHOLD = 3
}

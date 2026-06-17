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
    const val BROKEN_TRACK_SPEED_MULTIPLIER = 0.35f
    const val MAX_MOVEMENT_SUBSTEP = 4f
    const val MIN_LEGAL_MOVE = 0.15f

    const val AI_BLOCKED_MS = 380f
    const val AI_FIRE_ALIGNMENT = 18f
    const val AI_DIRECTION_RECHECK_MIN_MS = 280f
    const val AI_DIRECTION_RECHECK_MAX_MS = 800f
    const val ENEMY_SPAWN_BASE_MS = 1180f
    const val ENEMY_SPAWN_MIN_MS = 620f
    const val ENEMY_SPAWN_KILL_REDUCTION_MS = 18f
    const val FORMATION_OFFSET = 58f
    const val FORMATION_TRAIL = 82f

    const val PEN_RICOCHET_THRESHOLD = 0.5f
    const val PEN_ANGLE_BLEND = 0.4f

    const val HEAVY_KILL_REWARD = 360
    const val SCOUT_KILL_REWARD = 180
    const val DEFAULT_KILL_REWARD = 240

    const val BULLET_COLLISION_RADIUS = 6f
    const val POWER_UP_DROP_RATE = 0.35f
    const val PITY_KILL_THRESHOLD = 3
    const val POWER_UP_MAX = 3
    const val POWER_UP_TTL_MS = 9000f
    const val POWER_UP_SPAWN_MIN_MS = 9000f
    const val POWER_UP_SPAWN_RANGE_MS = 6000f
    const val POWER_UP_SIZE = 24f
    const val POWER_UP_SHIELD_MS = 4200f
    const val POWER_UP_SPEED_MS = 4200f
    const val POWER_UP_SPEED_BONUS = 42f
    const val FREEZE_MS = 3200f
    const val FREEZE_TRACK_MS = 2500f
    const val MINE_TRACK_MS = 4000f

    const val SMOKE_SCREEN_MS = 3000f
    const val SMOKE_SCREEN_RADIUS = 56f
    const val SMOKE_VISION_LIMIT = 70f
    const val FOREST_VISION_LIMIT = 58f
    const val RECON_MS = 4800f
    const val RECON_RADIUS = 142f

    const val AIR_STRIKE_RADIUS = 58f
    const val AIR_STRIKE_HEAVY_DAMAGE = 3
    const val AIR_STRIKE_LIGHT_DAMAGE = 2
    const val AIR_STRIKE_TRACK_MS = 2600f
    const val AIR_STRIKE_DIRECT_RATIO = 0.48f

    const val SUPPORT_SMOKE_COOLDOWN_MS = 6000f
    const val SUPPORT_REPAIR_COOLDOWN_MS = 10000f
    const val SUPPORT_RECON_COOLDOWN_MS = 8000f
    const val SUPPORT_ARTILLERY_COOLDOWN_MS = 12000f
}

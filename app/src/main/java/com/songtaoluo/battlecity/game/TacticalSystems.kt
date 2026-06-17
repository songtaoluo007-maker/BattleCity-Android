package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.SupportSkillType
import com.songtaoluo.battlecity.model.TeamSide
import com.songtaoluo.battlecity.model.TileType
import kotlin.math.sqrt

data class TacticalArea(
    val center: Vec2,
    val radius: Float,
    var remainingMs: Float,
) {
    val active: Boolean
        get() = remainingMs > 0f

    fun contains(position: Vec2): Boolean {
        val dx = position.x - center.x
        val dy = position.y - center.y
        return sqrt(dx * dx + dy * dy) <= radius
    }
}

object TankStatusSystem {
    fun update(tank: Tank, deltaSeconds: Float) {
        val deltaMs = deltaSeconds * 1000f
        tank.cooldownMs = (tank.cooldownMs - deltaMs).coerceAtLeast(0f)
        tank.aiTimerMs = (tank.aiTimerMs - deltaMs).coerceAtLeast(0f)
        tank.blockedMs = (tank.blockedMs - deltaMs).coerceAtLeast(0f)
        tank.shieldMs = (tank.shieldMs - deltaMs).coerceAtLeast(0f)
        tank.trackBrokenMs = (tank.trackBrokenMs - deltaMs).coerceAtLeast(0f)
        tank.speedBoostMs = (tank.speedBoostMs - deltaMs).coerceAtLeast(0f)
    }

    fun updateArea(area: TacticalArea?, deltaSeconds: Float): TacticalArea? {
        area ?: return null
        area.remainingMs = (area.remainingMs - deltaSeconds * 1000f).coerceAtLeast(0f)
        return area.takeIf { it.active }
    }
}

object VisibilitySystem {
    fun updateSpotted(
        enemies: List<Tank>,
        player: Tank,
        allies: List<Tank>,
        tiles: List<List<TileType>>,
        smoke: TacticalArea?,
        recon: TacticalArea?,
    ) {
        enemies.forEach { enemy ->
            enemy.isSpotted = enemy.alive && (
                isRevealedByRecon(enemy, smoke, recon) ||
                    canSee(player, enemy, tiles, smoke, recon) ||
                    allies.any { ally -> canSee(ally, enemy, tiles, smoke, recon) }
                )
        }
    }

    fun canSee(
        source: Tank,
        target: Tank,
        tiles: List<List<TileType>>,
        smoke: TacticalArea?,
        recon: TacticalArea?,
    ): Boolean {
        if (!source.alive || !target.alive) return false
        val distance = EnemyAiSystem.distanceBetween(source, target)
        if (distance > source.visionRange) return false
        if (smoke?.active == true && smoke.contains(target.position) && distance > GameConstants.SMOKE_VISION_LIMIT) {
            return false
        }
        if (MovementSystem.tileAtCenter(target, tiles) == TileType.FOREST &&
            !source.scoutReveal &&
            !isRevealedByRecon(target, smoke, recon) &&
            distance > GameConstants.FOREST_VISION_LIMIT
        ) {
            return false
        }
        return true
    }

    private fun isRevealedByRecon(
        target: Tank,
        smoke: TacticalArea?,
        recon: TacticalArea?,
    ): Boolean = target.team == TeamSide.ENEMY &&
        recon?.active == true &&
        recon.contains(target.position) &&
        !(smoke?.active == true && smoke.contains(target.position))
}

object SupportSkillSystem {
    fun cost(skill: SupportSkillType): Int = when (skill) {
        SupportSkillType.ARTILLERY_BARRAGE -> 3
        SupportSkillType.RECON_FLARE,
        SupportSkillType.EMERGENCY_REPAIR,
        -> 2
        SupportSkillType.SMOKE_SCREEN -> 1
    }

    fun cooldown(skill: SupportSkillType): Float = when (skill) {
        SupportSkillType.ARTILLERY_BARRAGE -> GameConstants.SUPPORT_ARTILLERY_COOLDOWN_MS
        SupportSkillType.RECON_FLARE -> GameConstants.SUPPORT_RECON_COOLDOWN_MS
        SupportSkillType.EMERGENCY_REPAIR -> GameConstants.SUPPORT_REPAIR_COOLDOWN_MS
        SupportSkillType.SMOKE_SCREEN -> GameConstants.SUPPORT_SMOKE_COOLDOWN_MS
    }

    fun updateCooldowns(cooldowns: MutableMap<SupportSkillType, Float>, deltaSeconds: Float) {
        val deltaMs = deltaSeconds * 1000f
        SupportSkillType.entries.forEach { skill ->
            cooldowns[skill] = ((cooldowns[skill] ?: 0f) - deltaMs).coerceAtLeast(0f)
        }
    }

    fun canUse(
        skill: SupportSkillType,
        commandPoints: Int,
        cooldowns: Map<SupportSkillType, Float>,
    ): Boolean = commandPoints >= cost(skill) && (cooldowns[skill] ?: 0f) <= 0f

    fun artilleryDamage(distance: Float): Int =
        if (distance <= GameConstants.AIR_STRIKE_RADIUS * GameConstants.AIR_STRIKE_DIRECT_RATIO) {
            GameConstants.AIR_STRIKE_HEAVY_DAMAGE
        } else {
            GameConstants.AIR_STRIKE_LIGHT_DAMAGE
        }
}

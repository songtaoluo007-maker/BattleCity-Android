package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.Direction
import com.songtaoluo.battlecity.model.HitResult
import com.songtaoluo.battlecity.model.VehicleRole

data class HitResolution(
    val result: HitResult,
    val armor: Int,
    val damage: Int,
)

object CombatResolver {
    fun resolve(target: Tank, bullet: Bullet): HitResolution {
        val armor = armorForHit(target, bullet.direction)

        if (bullet.penetration < armor * GameConstants.PEN_RICOCHET_THRESHOLD) {
            return HitResolution(HitResult.RICOCHET, armor, damage = 0)
        }

        val damage = when {
            bullet.penetration >= armor -> bullet.power
            bullet.penetration + 18 >= armor -> 1
            else -> 0
        }

        val result = if (damage > 0) HitResult.HIT else HitResult.BLOCKED
        return HitResolution(result, armor, damage)
    }

    fun armorForHit(target: Tank, bulletDirection: Direction): Int = when {
        bulletDirection == target.direction.opposite() -> target.armorFront
        bulletDirection == target.direction -> target.armorRear
        else -> target.armorSide
    }

    fun rewardFor(tank: Tank): Int = when (VehicleCatalog.get(tank.vehicleId).role) {
        VehicleRole.HEAVY -> GameConstants.HEAVY_KILL_REWARD
        VehicleRole.SCOUT -> GameConstants.SCOUT_KILL_REWARD
        else -> GameConstants.DEFAULT_KILL_REWARD
    }
}

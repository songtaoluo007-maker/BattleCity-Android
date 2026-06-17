package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.Direction
import com.songtaoluo.battlecity.model.HitResult
import com.songtaoluo.battlecity.model.TankKind
import com.songtaoluo.battlecity.model.TeamSide
import com.songtaoluo.battlecity.model.VehicleId
import org.junit.Assert.assertEquals
import org.junit.Test

class CombatResolverTest {
    @Test
    fun frontHitUsesFrontArmorAndCanDealReducedDamage() {
        val target = targetTank(VehicleId.T34_76, Direction.DOWN)
        val bullet = playerBullet(VehicleId.PZ_IV_H, Direction.UP)

        val resolution = CombatResolver.resolve(target, bullet)

        assertEquals(86, resolution.armor)
        assertEquals(HitResult.HIT, resolution.result)
        assertEquals(1, resolution.damage)
    }

    @Test
    fun rearHitUsesRearArmorAndDealsFullDamage() {
        val target = targetTank(VehicleId.T34_76, Direction.DOWN)
        val bullet = playerBullet(VehicleId.PZ_IV_H, Direction.DOWN)

        val resolution = CombatResolver.resolve(target, bullet)

        assertEquals(38, resolution.armor)
        assertEquals(HitResult.HIT, resolution.result)
        assertEquals(2, resolution.damage)
    }

    @Test
    fun lowPenetrationRicochetsFromHeavyFrontArmor() {
        val target = targetTank(VehicleId.TIGER_II, Direction.UP)
        val bullet = playerBullet(VehicleId.PZ_III, Direction.DOWN)

        val resolution = CombatResolver.resolve(target, bullet)

        assertEquals(185, resolution.armor)
        assertEquals(HitResult.RICOCHET, resolution.result)
        assertEquals(0, resolution.damage)
    }

    @Test
    fun killRewardsRespectVehicleRole() {
        assertEquals(360, CombatResolver.rewardFor(targetTank(VehicleId.TIGER_I, Direction.UP)))
        assertEquals(180, CombatResolver.rewardFor(targetTank(VehicleId.T70, Direction.UP)))
        assertEquals(240, CombatResolver.rewardFor(targetTank(VehicleId.T34_76, Direction.UP)))
    }

    private fun targetTank(vehicleId: VehicleId, direction: Direction): Tank =
        VehicleCatalog.get(vehicleId).createTank(
            id = 9,
            position = Vec2(100f, 100f),
            team = TeamSide.ENEMY,
            kind = TankKind.BASIC,
            direction = direction,
        )

    private fun playerBullet(vehicleId: VehicleId, direction: Direction): Bullet {
        val spec = VehicleCatalog.get(vehicleId)
        return Bullet(
            ownerId = 1,
            team = TeamSide.PLAYER,
            faction = spec.faction,
            position = Vec2(100f, 100f),
            direction = direction,
            speed = spec.bulletSpeed,
            power = spec.bulletPower,
            penetration = spec.penetration,
        )
    }
}

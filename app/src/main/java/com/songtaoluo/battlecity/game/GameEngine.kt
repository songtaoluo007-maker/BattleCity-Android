package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.Direction
import com.songtaoluo.battlecity.model.HitResult
import com.songtaoluo.battlecity.model.TankKind
import com.songtaoluo.battlecity.model.TeamSide
import com.songtaoluo.battlecity.model.VehicleId
import kotlin.math.abs

class GameEngine {
    private val playerVehicle = VehicleCatalog.get(VehicleId.PZ_IV_H)
    private val enemyVehicle = VehicleCatalog.get(VehicleId.T34_76)

    val player: Tank = playerVehicle.createTank(
        id = GameConstants.PLAYER_ID,
        position = Vec2(180f, 360f),
        team = TeamSide.PLAYER,
        kind = TankKind.PLAYER,
        direction = Direction.UP,
    )

    var enemy: Tank = createEnemy()
        private set

    val bullets: MutableList<Bullet> = mutableListOf()

    var score: Int = 0
        private set

    var credits: Int = 1600
        private set

    var lastHitResult: HitResult? = null
        private set

    var combatMessage: String = "目标：击毁 T-34/76"
        private set

    fun update(deltaSeconds: Float, width: Float, height: Float, input: Direction?) {
        player.cooldownMs = (player.cooldownMs - deltaSeconds * 1000f).coerceAtLeast(0f)

        input?.let { direction ->
            player.direction = direction
            move(player, direction, player.speed * deltaSeconds)
        }

        clampToViewport(player, width, height)
        clampToViewport(enemy, width, height)

        bullets.forEach { bullet ->
            moveBullet(bullet, deltaSeconds)

            if (bullet.position.x !in 0f..width || bullet.position.y !in 0f..height) {
                bullet.active = false
            }

            if (bullet.active && bullet.team != enemy.team && enemy.alive && intersects(bullet, enemy)) {
                bullet.active = false
                val result = damageTank(enemy, bullet)
                lastHitResult = result
                combatMessage = when (result) {
                    HitResult.RICOCHET -> "跳弹：穿深 ${bullet.penetration}，未击穿装甲"
                    HitResult.BLOCKED -> "未击穿：仅造成表面损伤"
                    HitResult.HIT -> "命中：T-34/76 剩余 ${enemy.hp}/${enemy.maxHp} HP"
                    HitResult.DESTROY -> "目标摧毁，获得战果与军费"
                }
            }
        }

        bullets.removeAll { !it.active }
    }

    fun fire() {
        if (!player.alive || player.cooldownMs > 0f) return

        val muzzle = Vec2(player.position.x, player.position.y)
        val muzzleOffset = GameConstants.TANK_SIZE / 2f + 6f
        when (player.direction) {
            Direction.UP -> muzzle.y -= muzzleOffset
            Direction.DOWN -> muzzle.y += muzzleOffset
            Direction.LEFT -> muzzle.x -= muzzleOffset
            Direction.RIGHT -> muzzle.x += muzzleOffset
        }

        bullets += Bullet(
            ownerId = player.id,
            team = player.team,
            faction = player.faction,
            position = muzzle,
            direction = player.direction,
            speed = player.bulletSpeed,
            power = player.bulletPower,
            penetration = player.penetration,
        )
        player.cooldownMs = player.reloadMs.toFloat()
    }

    fun restartEnemy() {
        enemy = createEnemy()
        lastHitResult = null
        combatMessage = "新目标进入战场"
    }

    private fun createEnemy(): Tank = enemyVehicle.createTank(
        id = 2,
        position = Vec2(760f, 140f),
        team = TeamSide.ENEMY,
        kind = TankKind.BASIC,
        direction = Direction.DOWN,
    )

    private fun damageTank(target: Tank, bullet: Bullet): HitResult {
        val resolution = CombatResolver.resolve(target, bullet)
        if (resolution.result == HitResult.RICOCHET || resolution.result == HitResult.BLOCKED) {
            return resolution.result
        }

        target.hp -= resolution.damage
        if (target.hp > 0) return HitResult.HIT

        target.hp = 0
        target.alive = false
        val reward = CombatResolver.rewardFor(target)
        score += reward
        credits += reward / 4
        return HitResult.DESTROY
    }

    private fun intersects(bullet: Bullet, tank: Tank): Boolean {
        val hitRadius = GameConstants.TANK_SIZE / 2f + 4f
        return abs(bullet.position.x - tank.position.x) <= hitRadius &&
            abs(bullet.position.y - tank.position.y) <= hitRadius
    }

    private fun clampToViewport(tank: Tank, width: Float, height: Float) {
        val margin = GameConstants.TANK_SIZE / 2f + 2f
        tank.position.x = tank.position.x.coerceIn(margin, (width - margin).coerceAtLeast(margin))
        tank.position.y = tank.position.y.coerceIn(margin, (height - margin).coerceAtLeast(margin))
    }

    private fun moveBullet(bullet: Bullet, deltaSeconds: Float) {
        val distance = bullet.speed * deltaSeconds
        when (bullet.direction) {
            Direction.UP -> bullet.position.y -= distance
            Direction.DOWN -> bullet.position.y += distance
            Direction.LEFT -> bullet.position.x -= distance
            Direction.RIGHT -> bullet.position.x += distance
        }
    }

    private fun move(tank: Tank, direction: Direction, distance: Float) {
        when (direction) {
            Direction.UP -> tank.position.y -= distance
            Direction.DOWN -> tank.position.y += distance
            Direction.LEFT -> tank.position.x -= distance
            Direction.RIGHT -> tank.position.x += distance
        }
    }
}

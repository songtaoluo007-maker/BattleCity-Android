package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.Direction
import com.songtaoluo.battlecity.model.Faction
import com.songtaoluo.battlecity.model.PowerUpType
import com.songtaoluo.battlecity.model.TankKind
import com.songtaoluo.battlecity.model.TeamSide
import com.songtaoluo.battlecity.model.VehicleId
import com.songtaoluo.battlecity.model.VehicleSpec

data class Vec2(var x: Float, var y: Float)

data class Tank(
    val id: Int,
    val position: Vec2,
    val kind: TankKind,
    val vehicleId: VehicleId,
    val faction: Faction,
    val team: TeamSide,
    var direction: Direction,
    val speed: Float,
    var hp: Int,
    val maxHp: Int,
    val reloadMs: Long,
    val bulletSpeed: Float,
    val bulletPower: Int,
    val penetration: Int,
    val armorFront: Int,
    val armorSide: Int,
    val armorRear: Int,
    val visionRange: Float,
    val scoutReveal: Boolean,
    var cooldownMs: Float = 0f,
    var aiTimerMs: Float = 0f,
    var blockedMs: Float = 0f,
    var blockedDirection: Direction? = null,
    var shieldMs: Float = 0f,
    var trackBrokenMs: Float = 0f,
    var speedBoostMs: Float = 0f,
    var apcrShots: Int = 0,
    var isSpotted: Boolean = team != TeamSide.ENEMY,
    var alive: Boolean = true,
)

data class Bullet(
    val ownerId: Int,
    val team: TeamSide,
    val faction: Faction,
    val position: Vec2,
    val direction: Direction,
    val speed: Float,
    val power: Int,
    val penetration: Int,
    var active: Boolean = true,
)

data class PowerUp(
    val type: PowerUpType,
    val position: Vec2,
    var ttlMs: Float = GameConstants.POWER_UP_TTL_MS,
    val size: Float = GameConstants.POWER_UP_SIZE,
)

fun VehicleSpec.createTank(
    id: Int,
    position: Vec2,
    team: TeamSide,
    kind: TankKind,
    direction: Direction,
): Tank = Tank(
    id = id,
    position = position,
    kind = kind,
    vehicleId = this.id,
    faction = faction,
    team = team,
    direction = direction,
    speed = speed,
    hp = hp,
    maxHp = hp,
    reloadMs = reloadMs,
    bulletSpeed = bulletSpeed,
    bulletPower = bulletPower,
    penetration = penetration,
    armorFront = armorFront,
    armorSide = armorSide,
    armorRear = armorRear,
    visionRange = visionRange,
    scoutReveal = scoutReveal,
    isSpotted = team != TeamSide.ENEMY,
)

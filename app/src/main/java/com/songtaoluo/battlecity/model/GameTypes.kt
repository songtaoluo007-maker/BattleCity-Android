package com.songtaoluo.battlecity.model

enum class Direction(val wireValue: String) {
    UP("up"),
    DOWN("down"),
    LEFT("left"),
    RIGHT("right");

    fun opposite(): Direction = when (this) {
        UP -> DOWN
        DOWN -> UP
        LEFT -> RIGHT
        RIGHT -> LEFT
    }
}

enum class TileType(val code: Int) {
    EMPTY(0),
    BRICK(1),
    STEEL(2),
    WATER(3),
    FOREST(4),
    BASE(5),
    OBJECTIVE(6),
    VILLAGE(7),
    MINE(8),
}

enum class Faction(val wireValue: String, val displayName: String) {
    GERMAN("german", "德军装甲群"),
    SOVIET("soviet", "苏军防御线"),
    BRITISH("british", "英军装甲旅"),
    AMERICAN("american", "美军装甲师"),
}

enum class VehicleId(val wireValue: String) {
    PZ_IV_H("pz4h"),
    TIGER_I("tiger1"),
    FERDINAND("ferdinand"),
    PZ_III("pz3"),
    PANTHER("panther"),
    TIGER_II("tiger2"),
    T34_76("t3476"),
    T70("t70"),
    SU_152("su152"),
    T34_85("t3485"),
    IS_2("is2"),
    KV_1("kv1"),
    CHURCHILL("churchill"),
    CROMWELL("cromwell"),
    MATILDA_II("matilda2"),
    FIREFLY("firefly"),
    COMET("comet"),
    VALENTINE("valentine"),
    SHERMAN_M4("sherman_m4"),
    STUART("stuart"),
    PERSHING("pershing"),
    SHERMAN_EASY_8("sherman_easy8"),
    M10_WOLVERINE("m10_wolverine"),
    M18_HELLCAT("m18_hellcat");

    companion object {
        fun fromWireValue(value: String): VehicleId? = entries.firstOrNull { it.wireValue == value }
    }
}

enum class VehicleRole(val wireValue: String) {
    MEDIUM("medium"),
    HEAVY("heavy"),
    TANK_DESTROYER("tankDestroyer"),
    SCOUT("scout"),
    ASSAULT_GUN("assaultGun"),
}

enum class TeamSide(val wireValue: String) {
    PLAYER("player"),
    ALLY("ally"),
    ENEMY("enemy"),
}

enum class SquadOrder(val wireValue: String) {
    FOLLOW("follow"),
    HOLD("hold"),
    ASSAULT("assault"),
    FOCUS_FIRE("focusFire"),
}

enum class TankKind(val wireValue: String) {
    PLAYER("player"),
    ALLY("ally"),
    BASIC("basic"),
    FAST("fast"),
    HEAVY("heavy"),
}

enum class ObjectiveType(val wireValue: String) {
    BREAKTHROUGH("breakthrough"),
    DESTROY("destroy"),
    DEFEND("defend"),
    SURVIVE("survive"),
}

enum class GameMode(val wireValue: String) {
    LOGIN("login"),
    CAMPAIGN_SELECT("campaignSelect"),
    FACTION_SELECT("factionSelect"),
    GARAGE("garage"),
    BRIEFING("briefing"),
    LOADING("loading"),
    MENU("menu"),
    PLAYING("playing"),
    PAUSED("paused"),
    GAME_OVER("gameOver"),
    LEVEL_CLEAR("levelClear"),
    VICTORY("victory"),
}

enum class PowerUpType(val wireValue: String) {
    SHIELD("shield"),
    SPEED("speed"),
    FIRE("fire"),
    LIFE("life"),
    FREEZE("freeze"),
}

enum class EffectKind(val wireValue: String) {
    EXPLOSION("explosion"),
    SPAWN("spawn"),
    PICKUP("pickup"),
    HIT("hit"),
    SMOKE("smoke"),
    SPOT("spot"),
    AIR_STRIKE("airStrike"),
    BULLET_COLLISION("bulletCollision"),
    RICOCHET_TRACER("ricochetTracer"),
    RECON_PULSE("reconPulse"),
    RUBBLE("rubble"),
}

enum class HitResult(val wireValue: String) {
    HIT("hit"),
    DESTROY("destroy"),
    RICOCHET("ricochet"),
    BLOCKED("blocked"),
}

data class VehicleSpec(
    val id: VehicleId,
    val faction: Faction,
    val role: VehicleRole,
    val displayName: String,
    val shortName: String,
    val history: String,
    val price: Int,
    val speed: Float,
    val hp: Int,
    val reloadMs: Long,
    val bulletSpeed: Float,
    val bulletPower: Int,
    val penetration: Int,
    val armorFront: Int,
    val armorSide: Int,
    val armorRear: Int,
    val visionRange: Float,
    val scoutReveal: Boolean,
    val bodyColor: String,
    val darkColor: String,
    val trimColor: String,
)

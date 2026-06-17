package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.Direction
import com.songtaoluo.battlecity.model.GridPoint
import com.songtaoluo.battlecity.model.HitResult
import com.songtaoluo.battlecity.model.SquadOrder
import com.songtaoluo.battlecity.model.TankKind
import com.songtaoluo.battlecity.model.TeamSide
import com.songtaoluo.battlecity.model.TileType
import com.songtaoluo.battlecity.model.VehicleRole
import kotlin.math.abs
import kotlin.math.sqrt

class GameEngine {
    val scenario = ScenarioCatalog.kurskGermanBreakthrough
    val tiles: MutableList<MutableList<TileType>> = TileMapParser.parse(scenario.map)
        .map { it.toMutableList() }
        .toMutableList()

    private val playerVehicle = VehicleCatalog.get(scenario.allyVehicles.first())

    val player: Tank = playerVehicle.createTank(
        id = GameConstants.PLAYER_ID,
        position = gridToPosition(scenario.playerSpawn),
        team = TeamSide.PLAYER,
        kind = TankKind.PLAYER,
        direction = Direction.UP,
    )

    val allies: MutableList<Tank> = mutableListOf()
    val enemies: MutableList<Tank> = mutableListOf()
    val bullets: MutableList<Bullet> = mutableListOf()
    val effects: MutableList<ImpactEffect> = mutableListOf()

    var squadOrder: SquadOrder = SquadOrder.FOLLOW
        private set

    var score: Int = 0
        private set

    var credits: Int = 1600
        private set

    var destroyedEnemies: Int = 0
        private set

    var victory: Boolean = false
        private set

    var gameOver: Boolean = false
        private set

    var lastHitResult: HitResult? = null
        private set

    var combatMessage: String = scenario.objective.detail
        private set

    private var remainingEnemyBudget: Int = scenario.enemyBudget
    private var enemySpawnTimerMs: Float = GameConstants.ENEMY_SPAWN_BASE_MS
    private var nextEnemyId: Int = 2
    private var nextVehicleIndex: Int = 0
    private var nextSpawnIndex: Int = 0

    val enemiesLeft: Int
        get() = remainingEnemyBudget + enemies.count { it.alive }

    val alliesAlive: Int
        get() = allies.count { it.alive }

    init {
        spawnAllies()
        val initialCount = minOf(
            scenario.enemySpawns.size,
            scenario.maxActiveEnemies,
            remainingEnemyBudget,
        )
        repeat(initialCount) { spawnEnemy() }
    }

    fun update(deltaSeconds: Float, input: Direction?) {
        ImpactEffectSystem.update(effects, deltaSeconds)
        if (victory || gameOver) return

        updateCooldowns(deltaSeconds)
        updatePlayer(input, deltaSeconds)
        updateAllies(deltaSeconds)
        updateEnemies(deltaSeconds)
        updateBullets(deltaSeconds)
        allies.removeAll { !it.alive }
        enemies.removeAll { !it.alive }
        updateEnemySpawning(deltaSeconds)
        updateObjectiveState()
    }

    fun fire() {
        fireTank(player)
    }

    fun setSquadOrder(order: SquadOrder) {
        squadOrder = order
        combatMessage = when (order) {
            SquadOrder.FOLLOW -> "编队命令：跟随座车推进"
            SquadOrder.HOLD -> "编队命令：原地坚守并自由开火"
            SquadOrder.ASSAULT -> "编队命令：向敌军发起突击"
            SquadOrder.FOCUS_FIRE -> "编队命令：集中攻击最近目标"
        }
    }

    private fun updateCooldowns(deltaSeconds: Float) {
        val deltaMs = deltaSeconds * 1000f
        player.cooldownMs = (player.cooldownMs - deltaMs).coerceAtLeast(0f)
        allies.forEach { ally ->
            ally.cooldownMs = (ally.cooldownMs - deltaMs).coerceAtLeast(0f)
        }
        enemies.forEach { enemy ->
            enemy.cooldownMs = (enemy.cooldownMs - deltaMs).coerceAtLeast(0f)
        }
    }

    private fun updatePlayer(input: Direction?, deltaSeconds: Float) {
        if (!player.alive) return
        input?.let { direction ->
            player.direction = direction
            val speed = MovementSystem.effectiveSpeed(player, tiles)
            MovementSystem.tryMove(
                tank = player,
                direction = direction,
                distance = speed * deltaSeconds,
                tiles = tiles,
                blockers = allies + enemies,
            )
        }
    }

    private fun updateAllies(deltaSeconds: Float) {
        val blockers = buildList {
            add(player)
            addAll(allies)
            addAll(enemies)
        }
        val objective = gridToPosition(
            GridPoint(scenario.objective.targetX, scenario.objective.targetY),
        )
        allies.toList().forEach { ally ->
            AllyAiSystem.update(
                ally = ally,
                player = player,
                enemies = enemies,
                order = squadOrder,
                deltaSeconds = deltaSeconds,
                tiles = tiles,
                blockers = blockers,
                objective = objective,
                fire = ::fireTank,
            )
        }
    }

    private fun updateEnemies(deltaSeconds: Float) {
        val friendlies = buildList {
            if (player.alive) add(player)
            addAll(allies.filter { it.alive })
        }
        val blockers = buildList {
            add(player)
            addAll(allies)
            addAll(enemies)
        }
        enemies.toList().forEach { enemy ->
            val target = friendlies.minByOrNull { friendly ->
                EnemyAiSystem.distanceBetween(enemy, friendly)
            } ?: return@forEach
            EnemyAiSystem.update(
                enemy = enemy,
                target = target,
                deltaSeconds = deltaSeconds,
                tiles = tiles,
                blockers = blockers,
                fire = ::fireTank,
            )
        }
    }

    private fun updateBullets(deltaSeconds: Float) {
        bullets.forEach { bullet ->
            moveBullet(bullet, deltaSeconds)
            if (bullet.position.x !in 0f..GameConstants.BOARD_SIZE ||
                bullet.position.y !in 0f..GameConstants.BOARD_SIZE
            ) {
                bullet.active = false
            }
        }

        val collisions = BulletCollisionSystem.resolve(bullets)
        if (collisions.isNotEmpty()) {
            effects += collisions.map { ImpactEffectSystem.spark(it.position) }
            combatMessage = "炮弹空中相撞 ${collisions.size} 次"
        }

        bullets.forEach { bullet ->
            if (!bullet.active) return@forEach

            val impact = ProjectileSystem.resolveTileImpact(bullet, tiles)
            if (impact.consumed) {
                bullet.active = false
                effects += ImpactEffectSystem.spark(bullet.position)
                if (bullet.team != TeamSide.ENEMY) score += impact.scoreDelta
                combatMessage = when {
                    impact.baseHit -> "阵地基地遭到炮击"
                    impact.destroyedTile -> "障碍物已摧毁"
                    else -> "炮弹被装甲工事拦截"
                }
                return@forEach
            }

            val target = if (bullet.team == TeamSide.ENEMY) {
                buildList {
                    if (player.alive) add(player)
                    addAll(allies.filter { it.alive })
                }.firstOrNull { friendly ->
                    bullet.ownerId != friendly.id && intersects(bullet, friendly)
                }
            } else {
                enemies.firstOrNull { enemy ->
                    enemy.alive && bullet.ownerId != enemy.id && intersects(bullet, enemy)
                }
            }

            if (target != null) {
                bullet.active = false
                val result = damageTank(target, bullet)
                lastHitResult = result
                effects += when (result) {
                    HitResult.DESTROY -> ImpactEffectSystem.destroyFlash(target.position)
                    else -> ImpactEffectSystem.hitFlash(target.position)
                }
                combatMessage = hitMessage(target, bullet, result)
            }
        }
        bullets.removeAll { !it.active }
    }

    private fun updateEnemySpawning(deltaSeconds: Float) {
        if (remainingEnemyBudget <= 0 || enemies.size >= scenario.maxActiveEnemies) return

        enemySpawnTimerMs -= deltaSeconds * 1000f
        if (enemySpawnTimerMs <= 0f) {
            if (spawnEnemy()) {
                combatMessage = "敌军增援抵达：剩余兵力 $enemiesLeft"
            }
            enemySpawnTimerMs = GameConstants.ENEMY_SPAWN_BASE_MS
        }
    }

    private fun spawnAllies() {
        scenario.allySpawns.forEachIndexed { index, spawn ->
            val vehicleId = scenario.allyVehicles[index % scenario.allyVehicles.size]
            val spec = VehicleCatalog.get(vehicleId)
            allies += spec.createTank(
                id = 100 + index,
                position = gridToPosition(spawn),
                team = TeamSide.ALLY,
                kind = TankKind.ALLY,
                direction = Direction.UP,
            )
        }
    }

    private fun spawnEnemy(): Boolean {
        if (remainingEnemyBudget <= 0 || enemies.size >= scenario.maxActiveEnemies) return false

        val spawn = findOpenSpawn() ?: return false
        val vehicleId = scenario.enemyVehicles[nextVehicleIndex % scenario.enemyVehicles.size]
        val spec = VehicleCatalog.get(vehicleId)
        val tank = spec.createTank(
            id = nextEnemyId++,
            position = gridToPosition(spawn),
            team = TeamSide.ENEMY,
            kind = when (spec.role) {
                VehicleRole.SCOUT -> TankKind.FAST
                VehicleRole.HEAVY -> TankKind.HEAVY
                else -> TankKind.BASIC
            },
            direction = Direction.DOWN,
        )

        enemies += tank
        remainingEnemyBudget -= 1
        nextVehicleIndex += 1
        nextSpawnIndex = (nextSpawnIndex + 1) % scenario.enemySpawns.size
        return true
    }

    private fun findOpenSpawn(): GridPoint? {
        for (offset in scenario.enemySpawns.indices) {
            val index = (nextSpawnIndex + offset) % scenario.enemySpawns.size
            val spawn = scenario.enemySpawns[index]
            val position = gridToPosition(spawn)
            val occupied = enemies.any { enemy ->
                enemy.alive &&
                    abs(enemy.position.x - position.x) < GameConstants.TANK_SIZE + GameConstants.TANK_MIN_SPACING &&
                    abs(enemy.position.y - position.y) < GameConstants.TANK_SIZE + GameConstants.TANK_MIN_SPACING
            }
            if (!occupied) {
                nextSpawnIndex = index
                return spawn
            }
        }
        return null
    }

    private fun fireTank(tank: Tank) {
        if (!tank.alive || tank.cooldownMs > 0f) return

        val muzzle = Vec2(tank.position.x, tank.position.y)
        val muzzleOffset = GameConstants.TANK_SIZE / 2f + 6f
        when (tank.direction) {
            Direction.UP -> muzzle.y -= muzzleOffset
            Direction.DOWN -> muzzle.y += muzzleOffset
            Direction.LEFT -> muzzle.x -= muzzleOffset
            Direction.RIGHT -> muzzle.x += muzzleOffset
        }

        bullets += Bullet(
            ownerId = tank.id,
            team = tank.team,
            faction = tank.faction,
            position = muzzle,
            direction = tank.direction,
            speed = tank.bulletSpeed,
            power = tank.bulletPower,
            penetration = tank.penetration,
        )
        tank.cooldownMs = tank.reloadMs.toFloat()
    }

    private fun damageTank(target: Tank, bullet: Bullet): HitResult {
        val resolution = CombatResolver.resolve(target, bullet)
        if (resolution.result == HitResult.RICOCHET || resolution.result == HitResult.BLOCKED) {
            return resolution.result
        }

        target.hp -= resolution.damage
        if (target.hp > 0) return HitResult.HIT

        target.hp = 0
        target.alive = false
        if (target.team == TeamSide.ENEMY) {
            destroyedEnemies += 1
            val reward = CombatResolver.rewardFor(target)
            score += reward
            credits += reward / 4
        } else if (target.team == TeamSide.PLAYER) {
            gameOver = true
        }
        return HitResult.DESTROY
    }

    private fun hitMessage(target: Tank, bullet: Bullet, result: HitResult): String {
        val targetName = VehicleCatalog.get(target.vehicleId).shortName
        return when (result) {
            HitResult.RICOCHET -> "$targetName 跳弹：穿深 ${bullet.penetration} 未击穿"
            HitResult.BLOCKED -> "$targetName 未击穿：炮弹被装甲吸收"
            HitResult.HIT -> "$targetName 命中，剩余 ${target.hp}/${target.maxHp} HP"
            HitResult.DESTROY -> when (target.team) {
                TeamSide.PLAYER -> "座车被击毁，任务失败"
                TeamSide.ALLY -> "友军 $targetName 被击毁，编队剩余 $alliesAlive"
                TeamSide.ENEMY -> "$targetName 已摧毁，战果 $destroyedEnemies/${scenario.objective.requiredKills}"
            }
        }
    }

    private fun updateObjectiveState() {
        if (!player.alive) {
            gameOver = true
            return
        }

        val objectiveCenter = gridToPosition(
            GridPoint(scenario.objective.targetX, scenario.objective.targetY),
        )
        val friendlyInsideObjective = buildList {
            add(player)
            addAll(allies.filter { it.alive })
        }.any { friendly ->
            val dx = friendly.position.x - objectiveCenter.x
            val dy = friendly.position.y - objectiveCenter.y
            sqrt(dx * dx + dy * dy) <= scenario.objective.radius
        }
        val killsComplete = destroyedEnemies >= scenario.objective.requiredKills

        if (killsComplete && friendlyInsideObjective) {
            victory = true
            score += 1000
            credits += 420
            combatMessage = "突破完成：北部防线已被撕开"
        } else if (killsComplete) {
            combatMessage = "击毁目标已完成，让任意友军进入北部红色目标区"
        }
    }

    private fun gridToPosition(point: GridPoint): Vec2 = Vec2(
        x = point.x * GameConstants.TILE_SIZE + GameConstants.TILE_SIZE / 2f,
        y = point.y * GameConstants.TILE_SIZE + GameConstants.TILE_SIZE / 2f,
    )

    private fun intersects(bullet: Bullet, tank: Tank): Boolean {
        val hitRadius = GameConstants.TANK_SIZE / 2f + 4f
        return abs(bullet.position.x - tank.position.x) <= hitRadius &&
            abs(bullet.position.y - tank.position.y) <= hitRadius
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
}

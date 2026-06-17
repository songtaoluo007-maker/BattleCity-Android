package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.Direction
import com.songtaoluo.battlecity.model.GridPoint
import com.songtaoluo.battlecity.model.HitResult
import com.songtaoluo.battlecity.model.PowerUpType
import com.songtaoluo.battlecity.model.ScenarioData
import com.songtaoluo.battlecity.model.SquadOrder
import com.songtaoluo.battlecity.model.SupportSkillType
import com.songtaoluo.battlecity.model.TankKind
import com.songtaoluo.battlecity.model.TeamSide
import com.songtaoluo.battlecity.model.TileType
import com.songtaoluo.battlecity.model.VehicleId
import com.songtaoluo.battlecity.model.VehicleRole
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random

class GameEngine(
    private val random: Random = Random.Default,
    val scenario: ScenarioData = ScenarioCatalog.kurskGermanBreakthrough,
    selectedVehicleId: VehicleId? = null,
) {
    val tiles: MutableList<MutableList<TileType>> = TileMapParser.parse(scenario.map)
        .map { it.toMutableList() }
        .toMutableList()

    private val playerVehicle = VehicleCatalog.get(
        selectedVehicleId?.takeIf { it in scenario.allyVehicles }
            ?: scenario.allyVehicles.first(),
    )

    val player: Tank = playerVehicle.createTank(
        id = GameConstants.PLAYER_ID,
        position = gridToPosition(scenario.playerSpawn),
        team = TeamSide.PLAYER,
        kind = TankKind.PLAYER,
        direction = Direction.UP,
    ).apply {
        shieldMs = GameConstants.PLAYER_INITIAL_SHIELD_MS
    }

    val allies: MutableList<Tank> = mutableListOf()
    val enemies: MutableList<Tank> = mutableListOf()
    val bullets: MutableList<Bullet> = mutableListOf()
    val effects: MutableList<ImpactEffect> = mutableListOf()
    val powerUps: MutableList<PowerUp> = mutableListOf()

    val supportCooldowns: MutableMap<SupportSkillType, Float> =
        SupportSkillType.entries.associateWith { 0f }.toMutableMap()

    var squadOrder: SquadOrder = SquadOrder.FOLLOW
        private set

    var targetingMode: TargetingMode = TargetingMode.NONE
        private set

    var selectedFocusTargetId: Int? = null
        private set

    var commandPoints: Int = 3
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

    var frozenMs: Float = 0f
        private set

    var smokeArea: TacticalArea? = null
        private set

    var reconArea: TacticalArea? = null
        private set

    private var remainingEnemyBudget: Int = scenario.enemyBudget
    private var enemySpawnTimerMs: Float = GameConstants.ENEMY_SPAWN_BASE_MS
    private var powerUpSpawnTimerMs: Float = GameConstants.POWER_UP_SPAWN_MIN_MS
    private var nextEnemyId: Int = 2
    private var nextVehicleIndex: Int = 0
    private var nextSpawnIndex: Int = 0
    private var pityKills: Int = 0

    val enemiesLeft: Int
        get() = remainingEnemyBudget + enemies.count { it.alive }

    val alliesAlive: Int
        get() = allies.count { it.alive }

    val selectedFocusTarget: Tank?
        get() = selectedFocusTargetId?.let { id ->
            enemies.firstOrNull { it.id == id && it.alive && it.isSpotted }
        }

    init {
        spawnAllies()
        val initialCount = minOf(
            scenario.enemySpawns.size,
            scenario.maxActiveEnemies,
            remainingEnemyBudget,
        )
        repeat(initialCount) { spawnEnemy() }
        updateSpotting()
    }

    fun update(deltaSeconds: Float, input: Direction?) {
        ImpactEffectSystem.update(effects, deltaSeconds)
        updateTacticalTimers(deltaSeconds)
        if (victory || gameOver) {
            targetingMode = TargetingMode.NONE
            return
        }

        allTanks().forEach { tank -> TankStatusSystem.update(tank, deltaSeconds) }
        updatePlayer(input, deltaSeconds)
        updateSpotting()
        updateAllies(deltaSeconds)
        if (frozenMs <= 0f) updateEnemies(deltaSeconds)
        checkMineSteps()
        updateBullets(deltaSeconds)
        updatePowerUps(deltaSeconds)
        allies.removeAll { !it.alive }
        enemies.removeAll { !it.alive }
        updateEnemySpawning(deltaSeconds)
        updateTimedPowerUpSpawning(deltaSeconds)
        updateSpotting()
        updateObjectiveState()
    }

    fun fire() {
        if (targetingMode != TargetingMode.NONE) return
        fireTank(player)
    }

    fun setSquadOrder(order: SquadOrder) {
        squadOrder = order
        if (order == SquadOrder.FOCUS_FIRE) {
            targetingMode = TargetingMode.FOCUS_FIRE
            combatMessage = "集火选敌：点击一辆已侦察敌车"
            return
        }

        targetingMode = TargetingMode.NONE
        selectedFocusTargetId = null
        combatMessage = when (order) {
            SquadOrder.FOLLOW -> "编队命令：跟随座车推进"
            SquadOrder.HOLD -> "编队命令：原地坚守并自由开火"
            SquadOrder.ASSAULT -> "编队命令：向敌军发起突击"
            SquadOrder.FOCUS_FIRE -> error("Handled above")
        }
    }

    fun cancelTargeting() {
        if (targetingMode == TargetingMode.NONE) return
        targetingMode = TargetingMode.NONE
        combatMessage = "目标选择已取消"
    }

    fun handleBoardTap(boardX: Float, boardY: Float): Boolean = when (targetingMode) {
        TargetingMode.NONE -> false
        TargetingMode.ARTILLERY -> {
            val used = useArtilleryAt(boardX, boardY)
            if (used) targetingMode = TargetingMode.NONE
            used
        }
        TargetingMode.FOCUS_FIRE -> {
            val target = TargetingSystem.findSpottedEnemyAt(boardX, boardY, enemies)
            if (target == null) {
                combatMessage = "未选中可见敌车，请点击敌车识别环"
                false
            } else {
                selectedFocusTargetId = target.id
                squadOrder = SquadOrder.FOCUS_FIRE
                targetingMode = TargetingMode.NONE
                combatMessage = "集火目标：${tankName(target)}"
                true
            }
        }
    }

    fun canUseSupportSkill(skill: SupportSkillType): Boolean =
        !victory && !gameOver && SupportSkillSystem.canUse(skill, commandPoints, supportCooldowns)

    fun supportCooldownMs(skill: SupportSkillType): Float = supportCooldowns[skill] ?: 0f

    fun useSupportSkill(skill: SupportSkillType): Boolean {
        if (!canUseSupportSkill(skill)) {
            combatMessage = if (commandPoints < SupportSkillSystem.cost(skill)) {
                "指挥点不足：需要 ${SupportSkillSystem.cost(skill)} 点"
            } else {
                "支援尚在冷却"
            }
            return false
        }

        if (skill == SupportSkillType.ARTILLERY_BARRAGE) {
            if (enemies.none { it.alive }) {
                combatMessage = "炮击取消：当前没有敌军目标"
                return false
            }
            targetingMode = TargetingMode.ARTILLERY
            combatMessage = "炮击选点：点击战场指定覆盖中心"
            return true
        }

        targetingMode = TargetingMode.NONE
        val activated = when (skill) {
            SupportSkillType.ARTILLERY_BARRAGE -> error("Handled above")
            SupportSkillType.RECON_FLARE -> {
                reconArea = TacticalArea(
                    center = Vec2(player.position.x, player.position.y),
                    radius = GameConstants.RECON_RADIUS,
                    remainingMs = GameConstants.RECON_MS,
                )
                combatMessage = "侦察照明弹升空：区域内敌军已暴露"
                true
            }
            SupportSkillType.EMERGENCY_REPAIR -> {
                val repaired = repairMostDamagedFriendly()
                if (!repaired) combatMessage = "维修取消：编队没有受损车辆"
                repaired
            }
            SupportSkillType.SMOKE_SCREEN -> {
                smokeArea = TacticalArea(
                    center = Vec2(player.position.x, player.position.y),
                    radius = GameConstants.SMOKE_SCREEN_RADIUS,
                    remainingMs = GameConstants.SMOKE_SCREEN_MS,
                )
                combatMessage = "烟幕释放：区域内友军获得掩护"
                true
            }
        }

        if (!activated) return false
        commandPoints -= SupportSkillSystem.cost(skill)
        supportCooldowns[skill] = SupportSkillSystem.cooldown(skill)
        updateSpotting()
        return true
    }

    fun useArtilleryAt(boardX: Float, boardY: Float): Boolean {
        val skill = SupportSkillType.ARTILLERY_BARRAGE
        if (!canUseSupportSkill(skill)) return false
        val center = Vec2(
            boardX.coerceIn(GameConstants.TILE_SIZE, GameConstants.BOARD_SIZE - GameConstants.TILE_SIZE),
            boardY.coerceIn(GameConstants.TILE_SIZE, GameConstants.BOARD_SIZE - GameConstants.TILE_SIZE),
        )
        if (!applyArtillery(center)) return false
        commandPoints -= SupportSkillSystem.cost(skill)
        supportCooldowns[skill] = SupportSkillSystem.cooldown(skill)
        return true
    }

    private fun updateTacticalTimers(deltaSeconds: Float) {
        frozenMs = (frozenMs - deltaSeconds * 1000f).coerceAtLeast(0f)
        smokeArea = TankStatusSystem.updateArea(smokeArea, deltaSeconds)
        reconArea = TankStatusSystem.updateArea(reconArea, deltaSeconds)
        SupportSkillSystem.updateCooldowns(supportCooldowns, deltaSeconds)
    }

    private fun updatePlayer(input: Direction?, deltaSeconds: Float) {
        if (!player.alive || targetingMode != TargetingMode.NONE) return
        input?.let { direction ->
            player.direction = direction
            MovementSystem.tryMove(
                tank = player,
                direction = direction,
                distance = MovementSystem.effectiveSpeed(player, tiles) * deltaSeconds,
                tiles = tiles,
                blockers = allies + enemies,
            )
        }
    }

    private fun updateAllies(deltaSeconds: Float) {
        val blockers = allTanks()
        val objective = gridToPosition(
            GridPoint(scenario.objective.targetX, scenario.objective.targetY),
        )
        val focusedTarget = selectedFocusTarget
        val availableTargets = if (squadOrder == SquadOrder.FOCUS_FIRE && focusedTarget != null) {
            listOf(focusedTarget)
        } else {
            enemies
        }
        allies.toList().forEach { ally ->
            AllyAiSystem.update(
                ally = ally,
                player = player,
                enemies = availableTargets,
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
        val friendlies = friendlyTanks()
        val blockers = allTanks()
        enemies.toList().forEach { enemy ->
            val visibleTargets = friendlies.filter { friendly ->
                VisibilitySystem.canSee(enemy, friendly, tiles, smokeArea, reconArea)
            }
            val target = (visibleTargets.ifEmpty { listOf(player) }).minByOrNull { friendly ->
                EnemyAiSystem.distanceBetween(enemy, friendly)
            } ?: return@forEach
            EnemyAiSystem.update(
                enemy = enemy,
                target = target,
                deltaSeconds = deltaSeconds,
                tiles = tiles,
                blockers = blockers,
                canSeeTarget = target in visibleTargets,
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
                friendlyTanks().firstOrNull { friendly ->
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

    private fun updatePowerUps(deltaSeconds: Float) {
        val pickups = PowerUpSystem.update(powerUps, deltaSeconds, friendlyTanks())
        pickups.forEach { pickup -> applyPowerUp(pickup) }
    }

    private fun applyPowerUp(pickup: PowerUpPickup) {
        val collector = pickup.collector
        when (pickup.powerUp.type) {
            PowerUpType.SHIELD -> {
                collector.shieldMs = maxOf(collector.shieldMs, GameConstants.POWER_UP_SHIELD_MS)
                combatMessage = "${tankName(collector)} 获得临时护盾"
            }
            PowerUpType.SPEED -> {
                collector.speedBoostMs = maxOf(collector.speedBoostMs, GameConstants.POWER_UP_SPEED_MS)
                collector.trackBrokenMs = 0f
                combatMessage = "${tankName(collector)} 机动增强"
            }
            PowerUpType.FIRE -> {
                collector.apcrShots += 2
                combatMessage = "${tankName(collector)} 获得2发高速穿甲弹"
            }
            PowerUpType.LIFE -> {
                if (!repairMostDamagedFriendly()) {
                    collector.shieldMs = maxOf(
                        collector.shieldMs,
                        GameConstants.REPAIR_FALLBACK_SHIELD_MS,
                    )
                    combatMessage = "编队满血：维修补给转化为临时防护"
                }
            }
            PowerUpType.FREEZE -> {
                frozenMs = maxOf(frozenMs, GameConstants.FREEZE_MS)
                enemies.forEach { enemy ->
                    enemy.trackBrokenMs = maxOf(enemy.trackBrokenMs, GameConstants.FREEZE_TRACK_MS)
                }
                combatMessage = "敌军行动被冻结"
            }
        }
        commandPoints += 1
        effects += ImpactEffectSystem.hitFlash(pickup.powerUp.position)
    }

    private fun updateEnemySpawning(deltaSeconds: Float) {
        if (remainingEnemyBudget <= 0 || enemies.size >= scenario.maxActiveEnemies) return

        enemySpawnTimerMs -= deltaSeconds * 1000f
        if (enemySpawnTimerMs <= 0f) {
            if (spawnEnemy()) {
                combatMessage = "敌军增援抵达：剩余兵力 $enemiesLeft"
            }
            enemySpawnTimerMs = maxOf(
                GameConstants.ENEMY_SPAWN_MIN_MS,
                GameConstants.ENEMY_SPAWN_BASE_MS -
                    destroyedEnemies * GameConstants.ENEMY_SPAWN_KILL_REDUCTION_MS,
            )
        }
    }

    private fun updateTimedPowerUpSpawning(deltaSeconds: Float) {
        powerUpSpawnTimerMs -= deltaSeconds * 1000f
        if (powerUpSpawnTimerMs > 0f || powerUps.size >= GameConstants.POWER_UP_MAX) return

        val position = PowerUpSystem.findOpenSpawn(tiles, allTanks(), destroyedEnemies + powerUps.size)
        if (position != null) {
            powerUps += PowerUpSystem.create(position, destroyedEnemies + powerUps.size)
            combatMessage = "战场补给已投放"
        }
        powerUpSpawnTimerMs = GameConstants.POWER_UP_SPAWN_MIN_MS +
            random.nextFloat() * GameConstants.POWER_UP_SPAWN_RANGE_MS
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
        enemies += spec.createTank(
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
            val occupied = allTanks().any { tank ->
                tank.alive &&
                    abs(tank.position.x - position.x) < GameConstants.TANK_SIZE + GameConstants.TANK_MIN_SPACING &&
                    abs(tank.position.y - position.y) < GameConstants.TANK_SIZE + GameConstants.TANK_MIN_SPACING
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

        val apcr = tank.apcrShots > 0
        bullets += Bullet(
            ownerId = tank.id,
            team = tank.team,
            faction = tank.faction,
            position = muzzle,
            direction = tank.direction,
            speed = tank.bulletSpeed + if (apcr) 75f else 0f,
            power = tank.bulletPower,
            penetration = tank.penetration + if (apcr) 35 else 0,
        )
        if (apcr) tank.apcrShots -= 1
        tank.cooldownMs = tank.reloadMs.toFloat()
    }

    private fun damageTank(target: Tank, bullet: Bullet): HitResult {
        val resolution = CombatResolver.resolve(target, bullet)
        if (resolution.result == HitResult.RICOCHET || resolution.result == HitResult.BLOCKED) {
            return resolution.result
        }

        var damage = resolution.damage
        val protectedBySmoke = target.team != TeamSide.ENEMY && smokeArea?.contains(target.position) == true
        if (target.shieldMs > 0f || protectedBySmoke) {
            damage = (damage - 1).coerceAtLeast(0)
        }
        if (damage <= 0) return HitResult.BLOCKED

        target.hp -= damage
        if (target.hp > 0) return HitResult.HIT

        target.hp = 0
        target.alive = false
        if (target.id == selectedFocusTargetId) {
            selectedFocusTargetId = null
        }
        if (target.team == TeamSide.ENEMY) {
            destroyedEnemies += 1
            val reward = CombatResolver.rewardFor(target)
            score += reward
            credits += reward / 4
            commandPoints += 1
            pityKills += 1
            val guaranteed = pityKills >= GameConstants.PITY_KILL_THRESHOLD
            if (guaranteed || random.nextFloat() < GameConstants.POWER_UP_DROP_RATE) {
                powerUps += PowerUpSystem.create(target.position, destroyedEnemies + powerUps.size)
                pityKills = 0
            }
        } else if (target.team == TeamSide.PLAYER) {
            gameOver = true
            targetingMode = TargetingMode.NONE
        }
        return HitResult.DESTROY
    }

    private fun repairMostDamagedFriendly(): Boolean {
        val target = friendlyTanks()
            .filter { it.hp < it.maxHp }
            .maxByOrNull { it.maxHp - it.hp }
            ?: return false
        target.hp = minOf(target.maxHp, target.hp + 2)
        target.shieldMs = maxOf(target.shieldMs, GameConstants.REPAIR_FALLBACK_SHIELD_MS)
        effects += ImpactEffectSystem.hitFlash(target.position)
        combatMessage = "维修完成：${tankName(target)} 恢复至 ${target.hp}/${target.maxHp} HP"
        return true
    }

    private fun applyArtillery(center: Vec2): Boolean {
        if (enemies.none { it.alive }) {
            combatMessage = "炮击取消：当前没有敌军目标"
            return false
        }

        var hits = 0
        var destroys = 0
        enemies.filter { it.alive }.toList().forEach { enemy ->
            val strikeDistance = distance(enemy.position, center)
            if (strikeDistance <= GameConstants.AIR_STRIKE_RADIUS) {
                enemy.trackBrokenMs = maxOf(enemy.trackBrokenMs, GameConstants.AIR_STRIKE_TRACK_MS)
                val shell = Bullet(
                    ownerId = -1,
                    team = TeamSide.ALLY,
                    faction = player.faction,
                    position = Vec2(center.x, center.y),
                    direction = Direction.DOWN,
                    speed = 0f,
                    power = SupportSkillSystem.artilleryDamage(strikeDistance),
                    penetration = 150,
                )
                val result = damageTank(enemy, shell)
                effects += if (result == HitResult.DESTROY) {
                    destroys += 1
                    ImpactEffectSystem.destroyFlash(enemy.position)
                } else {
                    ImpactEffectSystem.hitFlash(enemy.position)
                }
                hits += 1
            }
        }
        bombDestructibleTiles(center)
        effects += ImpactEffectSystem.destroyFlash(center)
        combatMessage = "炮火覆盖：命中 $hits 辆，摧毁 $destroys 辆"
        return true
    }

    private fun bombDestructibleTiles(center: Vec2) {
        tiles.forEachIndexed { row, values ->
            values.forEachIndexed { column, tile ->
                if (tile != TileType.BRICK && tile != TileType.VILLAGE && tile != TileType.MINE) {
                    return@forEachIndexed
                }
                val tileCenter = Vec2(
                    column * GameConstants.TILE_SIZE + GameConstants.TILE_SIZE / 2f,
                    row * GameConstants.TILE_SIZE + GameConstants.TILE_SIZE / 2f,
                )
                if (distance(tileCenter, center) <= GameConstants.AIR_STRIKE_RADIUS) {
                    tiles[row][column] = TileType.EMPTY
                }
            }
        }
    }

    private fun checkMineSteps() {
        allTanks().forEach { tank ->
            if (!tank.alive || MovementSystem.tileAtCenter(tank, tiles) != TileType.MINE) return@forEach
            val column = (tank.position.x / GameConstants.TILE_SIZE).toInt()
                .coerceIn(0, GameConstants.BOARD_TILES - 1)
            val row = (tank.position.y / GameConstants.TILE_SIZE).toInt()
                .coerceIn(0, GameConstants.BOARD_TILES - 1)
            tiles[row][column] = TileType.EMPTY
            tank.trackBrokenMs = maxOf(tank.trackBrokenMs, GameConstants.MINE_TRACK_MS)
            effects += ImpactEffectSystem.destroyFlash(tank.position)
            combatMessage = "${tankName(tank)} 触雷，履带受损"
        }
    }

    private fun updateSpotting() {
        VisibilitySystem.updateSpotted(enemies, player, allies, tiles, smokeArea, reconArea)
        val selectedId = selectedFocusTargetId ?: return
        val target = enemies.firstOrNull { it.id == selectedId }
        if (target == null || !target.alive || !target.isSpotted) {
            selectedFocusTargetId = null
            if (squadOrder == SquadOrder.FOCUS_FIRE) {
                combatMessage = "集火目标丢失：点击集火重新选择"
            }
        }
    }

    private fun hitMessage(target: Tank, bullet: Bullet, result: HitResult): String {
        val targetName = tankName(target)
        return when (result) {
            HitResult.RICOCHET -> "$targetName 跳弹：穿深 ${bullet.penetration} 未击穿"
            HitResult.BLOCKED -> "$targetName 未击穿：护盾、烟幕或装甲吸收伤害"
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
            targetingMode = TargetingMode.NONE
            return
        }

        val objectiveCenter = gridToPosition(
            GridPoint(scenario.objective.targetX, scenario.objective.targetY),
        )
        val friendlyInsideObjective = friendlyTanks().any { friendly ->
            distance(friendly.position, objectiveCenter) <= scenario.objective.radius
        }
        val killsComplete = destroyedEnemies >= scenario.objective.requiredKills

        if (killsComplete && friendlyInsideObjective) {
            victory = true
            targetingMode = TargetingMode.NONE
            score += 1000
            credits += 420
            combatMessage = "突破完成：北部防线已被撕开"
        } else if (killsComplete) {
            combatMessage = "击毁目标已完成，让任意友军进入北部红色目标区"
        }
    }

    private fun friendlyTanks(): List<Tank> = buildList {
        if (player.alive) add(player)
        addAll(allies.filter { it.alive })
    }

    private fun allTanks(): List<Tank> = buildList {
        add(player)
        addAll(allies)
        addAll(enemies)
    }

    private fun tankName(tank: Tank): String = VehicleCatalog.get(tank.vehicleId).shortName

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
        val bulletDistance = bullet.speed * deltaSeconds
        when (bullet.direction) {
            Direction.UP -> bullet.position.y -= bulletDistance
            Direction.DOWN -> bullet.position.y += bulletDistance
            Direction.LEFT -> bullet.position.x -= bulletDistance
            Direction.RIGHT -> bullet.position.x += bulletDistance
        }
    }

    private fun distance(a: Vec2, b: Vec2): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return sqrt(dx * dx + dy * dy)
    }
}

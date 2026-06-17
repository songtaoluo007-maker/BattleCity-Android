package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.Direction
import com.songtaoluo.battlecity.model.SupportSkillType
import com.songtaoluo.battlecity.model.TankKind
import com.songtaoluo.battlecity.model.TeamSide
import com.songtaoluo.battlecity.model.TileType
import com.songtaoluo.battlecity.model.VehicleId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VisionAndSupportTest {
    @Test
    fun smokeHidesFriendlyAtLongRange() {
        val enemy = tank(2, 100f, 100f, TeamSide.ENEMY)
        val player = tank(1, 180f, 100f, TeamSide.PLAYER)
        val smoke = TacticalArea(Vec2(180f, 100f), 56f, 3000f)

        assertFalse(VisibilitySystem.canSee(enemy, player, board(), smoke, null))
    }

    @Test
    fun reconOnlyRevealsEnemyUnits() {
        val tiles = board()
        val player = tank(1, 100f, 100f, TeamSide.PLAYER)
        val enemy = tank(2, 180f, 100f, TeamSide.ENEMY)
        val ally = tank(100, 180f, 100f, TeamSide.ALLY)
        tiles[3][5] = TileType.FOREST
        val recon = TacticalArea(Vec2(180f, 100f), 142f, 4800f)

        assertTrue(VisibilitySystem.canSee(player, enemy, tiles, null, recon))
        assertFalse(VisibilitySystem.canSee(player, ally, tiles, null, recon))
    }

    @Test
    fun timersClampAtZero() {
        val cooldowns = SupportSkillType.entries.associateWith { 1000f }.toMutableMap()
        SupportSkillSystem.updateCooldowns(cooldowns, 1.5f)
        assertTrue(cooldowns.values.all { it == 0f })

        val player = tank(1, 100f, 100f, TeamSide.PLAYER).apply {
            shieldMs = 500f
            trackBrokenMs = 500f
            speedBoostMs = 500f
        }
        TankStatusSystem.update(player, 1f)
        assertEquals(0f, player.shieldMs, 0.001f)
        assertEquals(0f, player.trackBrokenMs, 0.001f)
        assertEquals(0f, player.speedBoostMs, 0.001f)
    }

    @Test
    fun supportCostsAreOrderedByImpact() {
        assertEquals(3, SupportSkillSystem.cost(SupportSkillType.ARTILLERY_BARRAGE))
        assertEquals(2, SupportSkillSystem.cost(SupportSkillType.RECON_FLARE))
        assertEquals(1, SupportSkillSystem.cost(SupportSkillType.SMOKE_SCREEN))
    }

    private fun board(): MutableList<MutableList<TileType>> =
        MutableList(GameConstants.BOARD_TILES) {
            MutableList(GameConstants.BOARD_TILES) { TileType.EMPTY }
        }

    private fun tank(id: Int, x: Float, y: Float, team: TeamSide): Tank =
        VehicleCatalog.get(VehicleId.PZ_IV_H).createTank(
            id = id,
            position = Vec2(x, y),
            team = team,
            kind = when (team) {
                TeamSide.PLAYER -> TankKind.PLAYER
                TeamSide.ALLY -> TankKind.ALLY
                TeamSide.ENEMY -> TankKind.BASIC
            },
            direction = Direction.UP,
        )
}

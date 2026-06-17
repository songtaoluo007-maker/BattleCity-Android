package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.Faction
import com.songtaoluo.battlecity.model.GridPoint
import com.songtaoluo.battlecity.model.ObjectiveState
import com.songtaoluo.battlecity.model.ObjectiveType
import com.songtaoluo.battlecity.model.ScenarioData
import com.songtaoluo.battlecity.model.VehicleId

object ScenarioCatalog {
    val kurskGermanBreakthrough = ScenarioData(
        id = "kursk-1943-german-breakthrough",
        name = "库尔斯克会战：装甲楔形攻势",
        faction = Faction.GERMAN,
        opponent = Faction.SOVIET,
        operation = "德军进攻线",
        historicalBackground = "1943年7月，德军发动堡垒行动，从南北两端夹击库尔斯克突出部，却遭遇苏军预设的纵深反坦克防线。",
        briefing = "突破苏军纵深防线，摧毁反坦克火力点，并让至少一辆友军坦克抵达北部目标区。",
        playerSpawn = GridPoint(8, 15),
        allySpawns = listOf(GridPoint(6, 15), GridPoint(10, 15)),
        enemySpawns = listOf(GridPoint(4, 0), GridPoint(8, 0), GridPoint(12, 0)),
        allyVehicles = listOf(VehicleId.PZ_IV_H, VehicleId.TIGER_I),
        enemyVehicles = listOf(VehicleId.T34_76, VehicleId.T70, VehicleId.SU_152),
        enemyBudget = 18,
        maxActiveEnemies = 5,
        objective = ObjectiveState(
            type = ObjectiveType.BREAKTHROUGH,
            title = "突破北部防线",
            detail = "击毁至少 8 辆敌车，并进入北部目标区。",
            targetX = 8,
            targetY = 1,
            radius = 44f,
            requiredKills = 8,
            holdMs = 0,
        ),
        map = listOf(
            "....FF.....FF....",
            "..B.S...O...S.B..",
            ".F.H..S...S..H.F.",
            "...B..F...F..B...",
            ".H..B..S.S..B..H.",
            "F...B.W.W.W.B...F",
            "..B.F.H...H.F.B..",
            "...H..S...S..H...",
            ".F..B.....B..F...",
            "..H.F.....F.H....",
            "...B.W...W.B.....",
            ".F..H.....H..F...",
            "..B..F...F..B....",
            "...S..H.H..S.....",
            "..B...........B..",
            ".....F.....F.....",
            "....FFFF.....FF..",
        ),
    )

    val kurskSovietDefense = ScenarioData(
        id = "kursk-1943-soviet-defense",
        name = "库尔斯克会战：纵深防御",
        faction = Faction.SOVIET,
        opponent = Faction.GERMAN,
        operation = "苏军防御线",
        historicalBackground = "苏军最高统帅部预判德军进攻方向，在库尔斯克突出部构筑多道纵深反坦克防御体系，准备先守后攻。",
        briefing = "守住鹰巢阵地，拖慢德军重装甲推进。撑过倒计时或击毁足够敌车即可反击胜利。",
        playerSpawn = GridPoint(8, 15),
        allySpawns = listOf(GridPoint(6, 15), GridPoint(10, 15)),
        enemySpawns = listOf(GridPoint(4, 0), GridPoint(8, 0), GridPoint(12, 0)),
        allyVehicles = listOf(VehicleId.T34_76, VehicleId.T70),
        enemyVehicles = listOf(VehicleId.PZ_IV_H, VehicleId.TIGER_I, VehicleId.FERDINAND),
        enemyBudget = 18,
        maxActiveEnemies = 5,
        objective = ObjectiveState(
            type = ObjectiveType.SURVIVE,
            title = "守住纵深阵地",
            detail = "守住 150 秒，或在阵地失守前击毁 10 辆敌车。",
            targetX = 8,
            targetY = 14,
            radius = 58f,
            requiredKills = 10,
            holdMs = 150_000,
        ),
        map = listOf(
            "....FF.....FF....",
            "..B.S.....S.B....",
            ".F.H...S...H.F...",
            "..S..B.....B..S..",
            "...F.H.W.H.F.....",
            ".B..S.....S..B...",
            "F...B.H.H.B...F..",
            "..H..B..O.B..H...",
            ".F..W.....W..F...",
            "..B.H.....H.B....",
            "..S..F...F..S....",
            ".B.H.......H.B...",
            ".F..B.....B..F...",
            "..H.F..B.BF.H....",
            "..F...SSESS...F..",
            ".....F.....F.....",
            "....FF.....FF....",
        ),
    )

    val all: List<ScenarioData> = listOf(kurskGermanBreakthrough, kurskSovietDefense)

    fun forFaction(faction: Faction): ScenarioData =
        all.firstOrNull { it.faction == faction } ?: kurskGermanBreakthrough
}

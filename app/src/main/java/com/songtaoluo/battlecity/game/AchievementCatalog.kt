package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.AchievementDefinition
import com.songtaoluo.battlecity.model.AchievementState
import com.songtaoluo.battlecity.model.Faction
import com.songtaoluo.battlecity.model.MedalTier

object AchievementCatalog {
    const val DEFAULT_TITLE = "见习车长"
    const val ACE_TITLE = "王牌车长"
    const val VETERAN_TITLE = "装甲老兵"
    const val HERO_TITLE = "装甲英雄"

    val all: List<AchievementDefinition> = listOf(
        AchievementDefinition(
            id = "first_blood",
            name = "二级铁十字勋章",
            category = "德军历史勋奖 · 1939",
            detail = "单局击毁1辆坦克",
            symbol = "铁",
            tier = MedalTier.BRONZE,
            requiredMatchKills = 1,
            nation = Faction.GERMAN,
        ),
        AchievementDefinition(
            id = "ace_bronze_3",
            name = "一级铁十字勋章",
            category = "德军历史勋奖 · 1939",
            detail = "单局击毁3辆坦克",
            symbol = "铁",
            tier = MedalTier.BRONZE,
            requiredMatchKills = 3,
            nation = Faction.GERMAN,
        ),
        AchievementDefinition(
            id = "ace_silver_5",
            name = "银质装甲突击章",
            category = "德军装甲兵勋奖",
            detail = "单局击毁5辆坦克",
            symbol = "突",
            tier = MedalTier.SILVER,
            requiredMatchKills = 5,
            nation = Faction.GERMAN,
        ),
        AchievementDefinition(
            id = "ace_gold_8",
            name = "德意志金质十字勋章",
            category = "德军历史勋奖 · 1941",
            detail = "单局击毁8辆坦克",
            symbol = "德",
            tier = MedalTier.GOLD,
            requiredMatchKills = 8,
            nation = Faction.GERMAN,
        ),
        AchievementDefinition(
            id = "ace_platinum_12",
            name = "橡叶骑士铁十字勋章",
            category = "德军高阶战功勋奖",
            detail = "单局击毁12辆坦克",
            symbol = "橡",
            tier = MedalTier.PLATINUM,
            requiredMatchKills = 12,
            nation = Faction.GERMAN,
        ),
        AchievementDefinition(
            id = "career_red_star",
            name = "红星勋章",
            category = "苏联军事勋章",
            detail = "生涯击毁10辆坦克",
            symbol = "星",
            tier = MedalTier.BRONZE,
            requiredTotalKills = 10,
            nation = Faction.SOVIET,
        ),
        AchievementDefinition(
            id = "career_patriotic_war",
            name = "二级卫国战争勋章",
            category = "苏联东方战线勋奖",
            detail = "生涯击毁30辆坦克",
            symbol = "卫",
            tier = MedalTier.SILVER,
            requiredTotalKills = 30,
            nation = Faction.SOVIET,
        ),
        AchievementDefinition(
            id = "career_guards",
            name = "亚历山大·涅夫斯基勋章",
            category = "苏军指挥功勋",
            detail = "生涯击毁60辆坦克",
            symbol = "涅",
            tier = MedalTier.GOLD,
            requiredTotalKills = 60,
            nation = Faction.SOVIET,
        ),
        AchievementDefinition(
            id = "career_hero",
            name = "苏联英雄（金星奖章）",
            category = "最高国家荣誉",
            detail = "生涯击毁100辆坦克",
            symbol = "星",
            tier = MedalTier.PLATINUM,
            requiredTotalKills = 100,
            nation = Faction.SOVIET,
        ),
        AchievementDefinition(
            id = "career_valor",
            name = "胜利勋章",
            category = "苏联最高军事勋章",
            detail = "生涯击毁150辆坦克",
            symbol = "胜",
            tier = MedalTier.PLATINUM,
            requiredTotalKills = 150,
            nation = Faction.SOVIET,
        ),
        AchievementDefinition(
            id = "british_military_cross",
            name = "军功十字勋章",
            category = "英联邦 · 1914年设立",
            detail = "生涯击毁10辆坦克",
            symbol = "英",
            tier = MedalTier.BRONZE,
            requiredTotalKills = 10,
            nation = Faction.BRITISH,
        ),
        AchievementDefinition(
            id = "british_distinguished_service",
            name = "杰出服务勋章",
            category = "英联邦 · 1886年设立",
            detail = "生涯击毁30辆坦克",
            symbol = "勋",
            tier = MedalTier.SILVER,
            requiredTotalKills = 30,
            nation = Faction.BRITISH,
        ),
        AchievementDefinition(
            id = "british_conspicuous_gallantry",
            name = "显赫英勇勋章",
            category = "英联邦 · 1855年设立",
            detail = "生涯击毁60辆坦克",
            symbol = "勇",
            tier = MedalTier.GOLD,
            requiredTotalKills = 60,
            nation = Faction.BRITISH,
        ),
        AchievementDefinition(
            id = "british_victoria_cross",
            name = "维多利亚十字勋章",
            category = "英联邦最高荣誉 · 1856年",
            detail = "生涯击毁100辆坦克",
            symbol = "维",
            tier = MedalTier.PLATINUM,
            requiredTotalKills = 100,
            nation = Faction.BRITISH,
        ),
        AchievementDefinition(
            id = "british_war_medal",
            name = "1939-1945 战争奖章",
            category = "英联邦 · 1945年设立",
            detail = "生涯击毁150辆坦克",
            symbol = "战",
            tier = MedalTier.PLATINUM,
            requiredTotalKills = 150,
            nation = Faction.BRITISH,
        ),
        AchievementDefinition(
            id = "american_bronze_star",
            name = "铜星勋章",
            category = "美军 · 1944年设立",
            detail = "生涯击毁10辆坦克",
            symbol = "铜",
            tier = MedalTier.BRONZE,
            requiredTotalKills = 10,
            nation = Faction.AMERICAN,
        ),
        AchievementDefinition(
            id = "american_silver_star",
            name = "银星勋章",
            category = "美军 · 1932年设立",
            detail = "生涯击毁30辆坦克",
            symbol = "银",
            tier = MedalTier.SILVER,
            requiredTotalKills = 30,
            nation = Faction.AMERICAN,
        ),
        AchievementDefinition(
            id = "american_distinguished_service",
            name = "杰出服役十字勋章",
            category = "美军 · 1918年设立",
            detail = "生涯击毁60辆坦克",
            symbol = "杰",
            tier = MedalTier.GOLD,
            requiredTotalKills = 60,
            nation = Faction.AMERICAN,
        ),
        AchievementDefinition(
            id = "american_medal_of_honor",
            name = "二战胜利奖章",
            category = "美军 · 1945年设立",
            detail = "生涯击毁100辆坦克",
            symbol = "胜",
            tier = MedalTier.PLATINUM,
            requiredTotalKills = 100,
            nation = Faction.AMERICAN,
        ),
        AchievementDefinition(
            id = "american_purple_heart",
            name = "紫心勋章",
            category = "美军 · 1782年设立",
            detail = "生涯击毁150辆坦克",
            symbol = "心",
            tier = MedalTier.PLATINUM,
            requiredTotalKills = 150,
            nation = Faction.AMERICAN,
        ),
    )

    fun definitionsFor(nation: Faction): List<AchievementDefinition> =
        all.filter { it.nation == nation }

    fun defaultStates(): List<AchievementState> =
        all.map { AchievementState(id = it.id) }

    fun normalize(states: List<AchievementState>): List<AchievementState> {
        val byId = states.associateBy { it.id }
        return all.map { definition -> byId[definition.id] ?: AchievementState(definition.id) }
    }

    fun mergeProgress(
        existing: List<AchievementState>,
        oneMatchKills: Int,
        totalKills: Int,
        unlockedAtEpochMs: Long,
    ): List<AchievementState> = normalize(existing).map { state ->
        if (state.unlocked) return@map state
        val definition = all.firstOrNull { it.id == state.id } ?: return@map state
        val matchGate = definition.requiredMatchKills == 0 || oneMatchKills >= definition.requiredMatchKills
        val careerGate = definition.requiredTotalKills == 0 || totalKills >= definition.requiredTotalKills
        if (matchGate && careerGate) {
            state.copy(unlocked = true, unlockedAtEpochMs = unlockedAtEpochMs)
        } else {
            state
        }
    }

    fun newlyUnlocked(
        before: List<AchievementState>,
        after: List<AchievementState>,
    ): List<AchievementDefinition> {
        val beforeUnlocked = normalize(before).filter { it.unlocked }.mapTo(mutableSetOf()) { it.id }
        val afterUnlocked = normalize(after).filter { it.unlocked }.mapTo(mutableSetOf()) { it.id }
        return all.filter { it.id in afterUnlocked && it.id !in beforeUnlocked }
    }

    fun resolveTitle(states: List<AchievementState>): String {
        val unlockedIds = normalize(states).filter { it.unlocked }.mapTo(mutableSetOf()) { it.id }
        if (unlockedIds.isEmpty()) return DEFAULT_TITLE
        val tiers = all.filter { it.id in unlockedIds }.mapTo(mutableSetOf()) { it.tier }
        return when {
            MedalTier.PLATINUM in tiers -> HERO_TITLE
            MedalTier.GOLD in tiers -> ACE_TITLE
            MedalTier.SILVER in tiers -> VETERAN_TITLE
            else -> ACE_TITLE
        }
    }
}

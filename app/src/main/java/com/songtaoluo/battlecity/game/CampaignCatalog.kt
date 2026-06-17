package com.songtaoluo.battlecity.game

import com.songtaoluo.battlecity.model.CampaignData

object CampaignCatalog {
    val all: List<CampaignData> = listOf(
        CampaignData(
            id = "moscow-1941",
            name = "莫斯科保卫战",
            subtitle = "1941 · 冬季反击",
            year = "1941",
            description = "台风行动的最后挣扎。零下四十度的严寒中，苏军冬季坦克碾过冻土发动决定性反击。",
            scenarioCount = 2,
            initiallyUnlocked = true,
            difficulty = "标准",
            iconColor = "#3A5A6E",
            alliedFaction = "苏军防线",
            axisFaction = "德军装甲群",
            order = 1,
            prerequisiteCampaignId = null,
        ),
        CampaignData(
            id = "stalingrad-1942",
            name = "斯大林格勒战役",
            subtitle = "1942 · 城市攻防",
            year = "1942",
            description = "废墟中的巷战。攻守双方在断壁残垣中殊死搏斗，伏尔加河畔的每一寸土地都浸透鲜血。",
            scenarioCount = 2,
            initiallyUnlocked = false,
            difficulty = "困难",
            iconColor = "#5A3A1E",
            alliedFaction = "苏军防线",
            axisFaction = "德军装甲群",
            order = 2,
            prerequisiteCampaignId = "moscow-1941",
        ),
        CampaignData(
            id = "el-alamein-1942",
            name = "阿拉曼战役",
            subtitle = "1942 · 沙漠之狐",
            year = "1942",
            description = "北非沙漠的决定性会战。蒙哥马利的英军装甲旅迎战隆美尔的非洲军团。",
            scenarioCount = 2,
            initiallyUnlocked = false,
            difficulty = "标准",
            iconColor = "#8A7A3E",
            alliedFaction = "英军装甲旅",
            axisFaction = "德军非洲军团",
            order = 3,
            prerequisiteCampaignId = "stalingrad-1942",
        ),
        CampaignData(
            id = "kursk-1943",
            name = "库尔斯克战役",
            subtitle = "1943 · 东方战线",
            year = "1943",
            description = "人类历史上最大规模的坦克会战。德军装甲楔形攻势对阵苏军纵深防御。",
            scenarioCount = 2,
            initiallyUnlocked = false,
            difficulty = "标准",
            iconColor = "#8E3A2B",
            alliedFaction = "苏军防御线",
            axisFaction = "德军装甲群",
            order = 4,
            prerequisiteCampaignId = "el-alamein-1942",
        ),
        CampaignData(
            id = "normandy-1944",
            name = "诺曼底登陆",
            subtitle = "1944 · 霸王行动",
            year = "1944",
            description = "D-Day 装甲突破。美军谢尔曼坦克群穿越诺曼底树篱地带，粉碎大西洋壁垒。",
            scenarioCount = 2,
            initiallyUnlocked = false,
            difficulty = "标准",
            iconColor = "#3A5A3E",
            alliedFaction = "美军装甲师",
            axisFaction = "德军防线",
            order = 5,
            prerequisiteCampaignId = "kursk-1943",
        ),
        CampaignData(
            id = "berlin-1945",
            name = "柏林战役",
            subtitle = "1945 · 最终攻势",
            year = "1945",
            description = "帝国首都的最后防线。苏军 IS-2 重型坦克的铁拳砸碎第三帝国的垂死挣扎。",
            scenarioCount = 2,
            initiallyUnlocked = false,
            difficulty = "极难",
            iconColor = "#2E1E0E",
            alliedFaction = "苏军装甲师",
            axisFaction = "德军防线",
            order = 6,
            prerequisiteCampaignId = "normandy-1944",
        ),
    ).sortedBy(CampaignData::order)

    fun get(id: String): CampaignData? = all.firstOrNull { it.id == id }

    fun nextAfter(id: String): CampaignData? {
        val current = get(id) ?: return null
        return all.firstOrNull { it.order == current.order + 1 }
    }
}

package com.songtaoluo.battlecity.ui.art

import com.songtaoluo.battlecity.model.VehicleId

data class AtlasRegion(val atlas: String, val x: Int, val y: Int, val width: Int, val height: Int)

object OriginalArtCatalog {
    val atlasParts: Map<String, List<String>> = mapOf(
        "tank" to listOf("tank_tiny.part00", "tank_tiny.part01"),
        "medal" to listOf("medal_tiny.part00", "medal_tiny.part01"),
        "campaign" to listOf("campaign_tiny.part00"),
        "result" to listOf("result_tiny.part00"),
    )

    private fun r(atlas: String, x: Int, y: Int, width: Int, height: Int) = AtlasRegion(atlas, x, y, width, height)

    val vehicles: Map<VehicleId, AtlasRegion> = mapOf(
        VehicleId.PZ_IV_H to r("tank", 2, 4, 68, 34),
        VehicleId.TIGER_I to r("tank", 74, 2, 68, 37),
        VehicleId.FERDINAND to r("tank", 146, 5, 68, 32),
        VehicleId.PZ_III to r("tank", 218, 4, 68, 34),
        VehicleId.PANTHER to r("tank", 290, 4, 68, 34),
        VehicleId.TIGER_II to r("tank", 362, 4, 68, 34),
        VehicleId.T34_76 to r("tank", 2, 46, 68, 33),
        VehicleId.T70 to r("tank", 74, 46, 68, 34),
        VehicleId.SU_152 to r("tank", 146, 46, 68, 33),
        VehicleId.T34_85 to r("tank", 218, 46, 68, 34),
        VehicleId.IS_2 to r("tank", 290, 46, 68, 34),
        VehicleId.KV_1 to r("tank", 362, 46, 68, 34),
        VehicleId.CHURCHILL to r("tank", 2, 88, 68, 34),
        VehicleId.CROMWELL to r("tank", 74, 88, 68, 34),
        VehicleId.MATILDA_II to r("tank", 146, 88, 68, 34),
        VehicleId.FIREFLY to r("tank", 218, 88, 68, 34),
        VehicleId.COMET to r("tank", 290, 88, 68, 34),
        VehicleId.VALENTINE to r("tank", 362, 88, 68, 34),
        VehicleId.SHERMAN_M4 to r("tank", 2, 130, 68, 34),
        VehicleId.STUART to r("tank", 74, 130, 68, 34),
        VehicleId.PERSHING to r("tank", 146, 130, 68, 34),
        VehicleId.SHERMAN_EASY_8 to r("tank", 218, 130, 68, 34),
        VehicleId.M10_WOLVERINE to r("tank", 290, 130, 68, 34),
        VehicleId.M18_HELLCAT to r("tank", 362, 130, 68, 34),
    )

    val campaigns: Map<String, AtlasRegion> = mapOf(
        "moscow-1941" to r("campaign", 0, 11, 112, 40),
        "stalingrad-1942" to r("campaign", 123, 0, 90, 63),
        "el-alamein-1942" to r("campaign", 224, 11, 112, 40),
        "kursk-1943" to r("campaign", 0, 63, 111, 63),
        "normandy-1944" to r("campaign", 112, 74, 112, 40),
        "berlin-1945" to r("campaign", 232, 63, 95, 63),
    )

    val medals: Map<String, AtlasRegion> = mapOf(
        "ace_bronze_3" to r("medal", 2, 2, 26, 26),
        "ace_bronze_3_locked" to r("medal", 32, 2, 26, 26),
        "ace_gold_8" to r("medal", 62, 2, 26, 26),
        "ace_gold_8_locked" to r("medal", 92, 2, 26, 26),
        "ace_platinum_12" to r("medal", 122, 2, 26, 26),
        "ace_platinum_12_locked" to r("medal", 152, 2, 26, 26),
        "ace_silver_5" to r("medal", 182, 2, 26, 26),
        "ace_silver_5_locked" to r("medal", 212, 2, 26, 26),
        "american_bronze_star" to r("medal", 2, 32, 26, 26),
        "american_bronze_star_locked" to r("medal", 32, 32, 26, 26),
        "american_distinguished_service" to r("medal", 62, 32, 26, 26),
        "american_distinguished_service_locked" to r("medal", 92, 32, 26, 26),
        "american_medal_of_honor" to r("medal", 122, 32, 26, 26),
        "american_medal_of_honor_locked" to r("medal", 152, 32, 26, 26),
        "american_purple_heart" to r("medal", 182, 32, 26, 26),
        "american_purple_heart_locked" to r("medal", 212, 32, 26, 26),
        "american_silver_star" to r("medal", 2, 62, 26, 26),
        "american_silver_star_locked" to r("medal", 32, 62, 26, 26),
        "british_conspicuous_gallantry" to r("medal", 62, 62, 26, 26),
        "british_conspicuous_gallantry_locked" to r("medal", 92, 62, 26, 26),
        "british_distinguished_service" to r("medal", 122, 62, 26, 26),
        "british_distinguished_service_locked" to r("medal", 152, 62, 26, 26),
        "british_military_cross" to r("medal", 182, 62, 26, 26),
        "british_military_cross_locked" to r("medal", 212, 62, 26, 26),
        "british_victoria_cross" to r("medal", 2, 92, 26, 26),
        "british_victoria_cross_locked" to r("medal", 32, 92, 26, 26),
        "british_war_medal" to r("medal", 62, 92, 26, 26),
        "british_war_medal_locked" to r("medal", 92, 92, 26, 26),
        "career_guards" to r("medal", 122, 92, 26, 26),
        "career_guards_locked" to r("medal", 152, 92, 26, 26),
        "career_hero" to r("medal", 182, 92, 26, 26),
        "career_hero_locked" to r("medal", 212, 92, 26, 26),
        "career_patriotic_war" to r("medal", 2, 122, 26, 26),
        "career_patriotic_war_locked" to r("medal", 32, 122, 26, 26),
        "career_red_star" to r("medal", 62, 122, 26, 26),
        "career_red_star_locked" to r("medal", 92, 122, 26, 26),
        "career_valor" to r("medal", 122, 122, 26, 26),
        "career_valor_locked" to r("medal", 152, 122, 26, 26),
        "first_blood" to r("medal", 182, 122, 26, 26),
        "first_blood_locked" to r("medal", 212, 122, 26, 26),
    )

    val results: Map<String, AtlasRegion> = mapOf(
        "victory" to r("result", 0, 0, 56, 100),
        "defeat" to r("result", 56, 0, 56, 100),
    )

    fun vehicle(id: VehicleId): AtlasRegion? = vehicles[id]
    fun campaign(id: String): AtlasRegion? = campaigns[id]
    fun medal(id: String, unlocked: Boolean): AtlasRegion? = medals[if (unlocked) id else "${id}_locked"]
    fun result(victory: Boolean): AtlasRegion? = results[if (victory) "victory" else "defeat"]
}

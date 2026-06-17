package com.songtaoluo.battlecity.ui

import com.songtaoluo.battlecity.model.Faction
import com.songtaoluo.battlecity.model.VehicleId

object OriginalVisualAssetCatalog {
    fun tankStem(id: VehicleId): String = when (id) {
        VehicleId.PZ_IV_H -> "tank_pz_iv_h"
        VehicleId.TIGER_I -> "tank_tiger_i"
        VehicleId.FERDINAND -> "tank_ferdinand"
        VehicleId.PZ_III -> "tank_pz_iii"
        VehicleId.PANTHER -> "tank_panther"
        VehicleId.TIGER_II -> "tank_tiger_ii"
        VehicleId.T34_76 -> "tank_t34_76"
        VehicleId.T70 -> "tank_t70"
        VehicleId.SU_152 -> "tank_su_152"
        VehicleId.T34_85 -> "tank_t34_85"
        VehicleId.IS_2 -> "tank_is2"
        VehicleId.KV_1 -> "tank_kv1"
        VehicleId.CHURCHILL -> "tank_churchill"
        VehicleId.CROMWELL -> "tank_cromwell"
        VehicleId.MATILDA_II -> "tank_matilda_ii"
        VehicleId.FIREFLY -> "tank_firefly"
        VehicleId.COMET -> "tank_comet"
        VehicleId.VALENTINE -> "tank_valentine"
        VehicleId.SHERMAN_M4 -> "tank_sherman_m4"
        VehicleId.STUART -> "tank_stuart"
        VehicleId.PERSHING -> "tank_pershing"
        VehicleId.SHERMAN_EASY_8 -> "tank_sherman_easy8"
        VehicleId.M10_WOLVERINE -> "tank_m10_wolverine"
        VehicleId.M18_HELLCAT -> "tank_m18_hellcat"
    }

    fun campaignStem(campaignId: String): String? = when (campaignId) {
        "moscow-1941" -> "campaign_moscow_1941"
        "stalingrad-1942" -> "campaign_stalingrad_1942"
        "el-alamein-1942" -> "campaign_el_alamein_1942"
        "kursk-1943" -> "campaign_kursk_1943"
        "normandy-1944" -> "campaign_normandy_1944"
        "berlin-1945" -> "campaign_berlin_1945"
        else -> null
    }

    fun factionFlagStem(faction: Faction): String = when (faction) {
        Faction.GERMAN -> "flag_germany_1935"
        Faction.SOVIET -> "flag_soviet_1936"
        Faction.BRITISH -> "flag_british"
        Faction.AMERICAN -> "flag_american"
    }

    fun resultStem(victory: Boolean): String =
        if (victory) "result_victory_archive" else "result_defeat_archive"

    val medalStems: List<String> = listOf(
        "medal_first_blood",
        "medal_tank_ace",
        "medal_steel_wall",
        "medal_blitzkrieg",
        "medal_survivor",
        "medal_sharpshooter",
        "medal_destroyer",
        "medal_commander",
        "medal_conqueror",
        "medal_legend",
        "medal_moscow",
        "medal_stalingrad",
        "medal_el_alamein",
        "medal_kursk",
        "medal_normandy",
        "medal_berlin",
        "medal_collector",
        "medal_veteran",
        "medal_iron_cross",
        "medal_hero",
    )
}

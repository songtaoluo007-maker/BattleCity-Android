package com.songtaoluo.battlecity.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.songtaoluo.battlecity.model.VehicleId

object OriginalArt {
    fun vehicleStem(id: VehicleId): String = when (id) {
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

    fun campaignStem(id: String): String = when (id) {
        "moscow-1941" -> "campaign_moscow_1941"
        "stalingrad-1942" -> "campaign_stalingrad_1942"
        "el-alamein-1942" -> "campaign_el_alamein_1942"
        "kursk-1943" -> "campaign_kursk_1943"
        "normandy-1944" -> "campaign_normandy_1944"
        "berlin-1945" -> "campaign_berlin_1945"
        else -> "menu_bg_campaign"
    }

    fun achievementStem(id: String, unlocked: Boolean): String =
        "medal_${id}" + if (unlocked) "" else "_locked"

    fun resultStem(victory: Boolean): String =
        if (victory) "result_victory_archive" else "result_defeat_archive"
}

@Composable
internal fun OriginalArtImage(
    stem: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    fallbackLabel: String = "",
    fallback: (@Composable BoxScope.() -> Unit)? = null,
) {
    val context = LocalContext.current
    val resourceId = remember(stem, context.packageName) {
        context.resources.getIdentifier(stem, "drawable", context.packageName)
    }

    if (resourceId != 0) {
        Image(
            painter = painterResource(resourceId),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier,
        )
    } else {
        Box(
            modifier = modifier.background(Color(0xFF252A24)),
            contentAlignment = Alignment.Center,
        ) {
            if (fallback != null) {
                fallback()
            } else if (fallbackLabel.isNotBlank()) {
                Text(
                    text = fallbackLabel,
                    color = Color(0xFFB8BCAE),
                    modifier = Modifier.padding(8.dp),
                )
            }
        }
    }
}

@Composable
internal fun MedalArt(
    achievementId: String,
    unlocked: Boolean,
    modifier: Modifier = Modifier,
    fallbackSymbol: String,
) {
    OriginalArtImage(
        stem = OriginalArt.achievementStem(achievementId, unlocked),
        contentDescription = achievementId,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .size(64.dp)
            .clip(RoundedCornerShape(8.dp)),
        fallbackLabel = if (unlocked) fallbackSymbol else "锁",
    )
}

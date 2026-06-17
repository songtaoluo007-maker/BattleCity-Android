package com.songtaoluo.battlecity.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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

    fun campaignStem(campaignId: String): String =
        "campaign_${campaignId.replace('-', '_')}"

    fun achievementStem(achievementId: String, unlocked: Boolean): String =
        "medal_${achievementId}" + if (unlocked) "" else "_locked"

    fun resultStem(victory: Boolean): String =
        if (victory) "result_victory_archive" else "result_defeat_archive"

    const val PROFILE_MENU = "menu_bg_profile"
    const val CAMPAIGN_MENU = "menu_bg_campaign"
    const val GARAGE_MENU = "menu_bg_factory"
    const val ACHIEVEMENT_MENU = "menu_bg_achievements"
}

@Composable
internal fun OriginalResourceImage(
    stem: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = 1f,
    colorFilter: ColorFilter? = null,
    fallback: @Composable (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val resourceId = remember(stem, context.packageName) {
        context.resources.getIdentifier(stem, "drawable", context.packageName)
            .takeIf { it != 0 }
            ?: context.resources.getIdentifier(stem, "mipmap", context.packageName)
    }
    if (resourceId != 0) {
        Image(
            painter = painterResource(resourceId),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            alpha = alpha.coerceIn(0f, 1f),
            colorFilter = colorFilter,
        )
    } else {
        fallback?.invoke()
    }
}

@Composable
internal fun OriginalArtBackground(
    stem: String,
    modifier: Modifier = Modifier,
    imageAlpha: Float = 0.32f,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier) {
        OriginalResourceImage(
            stem = stem,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = imageAlpha,
        )
        content()
    }
}

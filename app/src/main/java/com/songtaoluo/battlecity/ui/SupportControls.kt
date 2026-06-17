package com.songtaoluo.battlecity.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.songtaoluo.battlecity.game.GameEngine
import com.songtaoluo.battlecity.game.SupportSkillSystem
import com.songtaoluo.battlecity.game.TargetingMode
import com.songtaoluo.battlecity.model.SupportSkillType
import kotlin.math.ceil

@Composable
internal fun SupportControls(
    engine: GameEngine,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp), modifier = modifier) {
        SupportButton(engine, SupportSkillType.ARTILLERY_BARRAGE, "炮击", enabled)
        SupportButton(engine, SupportSkillType.RECON_FLARE, "侦察", enabled)
        SupportButton(engine, SupportSkillType.EMERGENCY_REPAIR, "维修", enabled)
        SupportButton(engine, SupportSkillType.SMOKE_SCREEN, "烟幕", enabled)
    }
}

@Composable
private fun SupportButton(
    engine: GameEngine,
    skill: SupportSkillType,
    label: String,
    controlsEnabled: Boolean,
) {
    val selectingArtillery = skill == SupportSkillType.ARTILLERY_BARRAGE &&
        engine.targetingMode == TargetingMode.ARTILLERY
    val cooldownSeconds = ceil(engine.supportCooldownMs(skill) / 1000f).toInt()
    val suffix = when {
        selectingArtillery -> " 选点中"
        cooldownSeconds > 0 -> " ${cooldownSeconds}s"
        else -> " ${SupportSkillSystem.cost(skill)}点"
    }
    Button(
        onClick = { engine.useSupportSkill(skill) },
        enabled = controlsEnabled && !selectingArtillery && engine.canUseSupportSkill(skill),
    ) {
        Text(label + suffix)
    }
}

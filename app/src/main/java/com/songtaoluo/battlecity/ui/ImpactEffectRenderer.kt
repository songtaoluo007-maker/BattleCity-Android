package com.songtaoluo.battlecity.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.songtaoluo.battlecity.game.ImpactEffect
import com.songtaoluo.battlecity.game.ImpactEffectKind

internal fun DrawScope.drawImpactEffect(effect: ImpactEffect) {
    val center = Offset(effect.position.x, effect.position.y)
    val fade = (1f - effect.progress).coerceIn(0f, 1f)

    when (effect.kind) {
        ImpactEffectKind.SPARK -> {
            val radius = 5f + effect.progress * 12f
            drawCircle(
                color = Color(0xFFFFE082).copy(alpha = fade),
                radius = radius,
                center = center,
                style = Stroke(width = 2f),
            )
            drawLine(
                color = Color.White.copy(alpha = fade),
                start = center + Offset(-radius, 0f),
                end = center + Offset(radius, 0f),
                strokeWidth = 2f,
            )
            drawLine(
                color = Color.White.copy(alpha = fade),
                start = center + Offset(0f, -radius),
                end = center + Offset(0f, radius),
                strokeWidth = 2f,
            )
        }

        ImpactEffectKind.HIT_FLASH -> {
            val radius = 7f + effect.progress * 8f
            drawCircle(
                color = Color(0xFFFFB74D).copy(alpha = fade),
                radius = radius,
                center = center,
            )
            drawCircle(
                color = Color.White.copy(alpha = fade),
                radius = radius * 0.35f,
                center = center,
            )
        }

        ImpactEffectKind.DESTROY_FLASH -> {
            val radius = 10f + effect.progress * 30f
            drawCircle(
                color = Color(0xFFFF7043).copy(alpha = fade * 0.55f),
                radius = radius,
                center = center,
            )
            drawCircle(
                color = Color(0xFFFFD54F).copy(alpha = fade),
                radius = radius * 0.55f,
                center = center,
                style = Stroke(width = 4f),
            )
        }
    }
}

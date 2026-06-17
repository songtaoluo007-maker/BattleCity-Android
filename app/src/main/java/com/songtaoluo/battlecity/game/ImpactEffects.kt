package com.songtaoluo.battlecity.game

enum class ImpactEffectKind {
    SPARK,
    HIT_FLASH,
    DESTROY_FLASH,
}

data class ImpactEffect(
    val kind: ImpactEffectKind,
    val position: Vec2,
    var remainingMs: Float,
    val durationMs: Float,
) {
    val progress: Float
        get() = (1f - remainingMs / durationMs).coerceIn(0f, 1f)
}

object ImpactEffectSystem {
    fun update(effects: MutableList<ImpactEffect>, deltaSeconds: Float) {
        val deltaMs = deltaSeconds * 1000f
        effects.forEach { effect ->
            effect.remainingMs = (effect.remainingMs - deltaMs).coerceAtLeast(0f)
        }
        effects.removeAll { it.remainingMs <= 0f }
    }

    fun spark(position: Vec2): ImpactEffect = ImpactEffect(
        kind = ImpactEffectKind.SPARK,
        position = Vec2(position.x, position.y),
        remainingMs = 220f,
        durationMs = 220f,
    )

    fun hitFlash(position: Vec2): ImpactEffect = ImpactEffect(
        kind = ImpactEffectKind.HIT_FLASH,
        position = Vec2(position.x, position.y),
        remainingMs = 180f,
        durationMs = 180f,
    )

    fun destroyFlash(position: Vec2): ImpactEffect = ImpactEffect(
        kind = ImpactEffectKind.DESTROY_FLASH,
        position = Vec2(position.x, position.y),
        remainingMs = 560f,
        durationMs = 560f,
    )
}

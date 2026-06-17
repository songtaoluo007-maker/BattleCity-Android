package com.songtaoluo.battlecity.game

enum class Direction { UP, DOWN, LEFT, RIGHT }

data class Vec2(var x: Float, var y: Float)

data class Tank(
    val position: Vec2,
    var direction: Direction,
    val speed: Float,
    var alive: Boolean = true,
)

data class Bullet(
    val position: Vec2,
    val direction: Direction,
    val speed: Float = 420f,
    var active: Boolean = true,
)

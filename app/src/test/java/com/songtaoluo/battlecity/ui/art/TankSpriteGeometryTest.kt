package com.songtaoluo.battlecity.ui.art

import com.songtaoluo.battlecity.model.Direction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TankSpriteGeometryTest {
    @Test
    fun originalRightFacingSpriteRotatesToEveryGameDirection() {
        assertEquals(0f, TankSpriteGeometry.rotationDegrees(Direction.RIGHT), 0f)
        assertEquals(90f, TankSpriteGeometry.rotationDegrees(Direction.DOWN), 0f)
        assertEquals(180f, TankSpriteGeometry.rotationDegrees(Direction.LEFT), 0f)
        assertEquals(270f, TankSpriteGeometry.rotationDegrees(Direction.UP), 0f)
    }

    @Test
    fun atlasTankKeepsTopDownAspectRatioOnBoard() {
        val size = TankSpriteGeometry.boardSize(sourceWidth = 68, sourceHeight = 34)

        assertEquals(38, size.width)
        assertEquals(19, size.height)
    }

    @Test
    fun unusualSourceRatiosAreClampedToPlayableBounds() {
        val tooThin = TankSpriteGeometry.boardSize(200, 10)
        val tooTall = TankSpriteGeometry.boardSize(20, 100)

        assertEquals(18, tooThin.height)
        assertEquals(25, tooTall.height)
        assertTrue(tooThin.width > tooThin.height)
        assertTrue(tooTall.width > tooTall.height)
    }
}

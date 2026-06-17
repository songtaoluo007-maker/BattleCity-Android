package com.songtaoluo.battlecity.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

class BoardViewportTest {
    @Test
    fun boardCenterMapsBackToLogicalCenter() {
        val viewport = BoardViewportCalculator.calculate(1200f, 800f)
        val point = viewport.toBoard(
            viewport.left + viewport.size / 2f,
            viewport.top + viewport.size / 2f,
        )

        assertNotNull(point)
        assertEquals(GameConstants.BOARD_SIZE / 2f, point!!.x, 0.001f)
        assertEquals(GameConstants.BOARD_SIZE / 2f, point.y, 0.001f)
    }

    @Test
    fun tapsOutsideRenderedBoardAreRejected() {
        val viewport = BoardViewportCalculator.calculate(1200f, 800f)

        assertNull(viewport.toBoard(viewport.left - 1f, viewport.top))
        assertNull(viewport.toBoard(viewport.left, viewport.top + viewport.size + 1f))
    }

    @Test
    fun logicalCornersRemainInsideBounds() {
        val viewport = BoardViewportCalculator.calculate(900f, 500f)
        val topLeft = viewport.toBoard(viewport.left, viewport.top)
        val bottomRight = viewport.toBoard(
            viewport.left + viewport.size,
            viewport.top + viewport.size,
        )

        assertEquals(0f, topLeft!!.x, 0.001f)
        assertEquals(0f, topLeft.y, 0.001f)
        assertEquals(GameConstants.BOARD_SIZE, bottomRight!!.x, 0.001f)
        assertEquals(GameConstants.BOARD_SIZE, bottomRight.y, 0.001f)
    }
}

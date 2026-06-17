package com.songtaoluo.battlecity.ui.art

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.songtaoluo.battlecity.model.Direction
import com.songtaoluo.battlecity.model.VehicleId
import kotlin.math.roundToInt

internal data class TankSpriteSize(
    val width: Int,
    val height: Int,
)

internal object TankSpriteGeometry {
    private const val LONG_AXIS = 38f
    private const val MIN_SHORT_AXIS = 18f
    private const val MAX_SHORT_AXIS = 25f

    fun rotationDegrees(direction: Direction): Float = when (direction) {
        Direction.RIGHT -> 0f
        Direction.DOWN -> 90f
        Direction.LEFT -> 180f
        Direction.UP -> 270f
    }

    fun boardSize(sourceWidth: Int, sourceHeight: Int): TankSpriteSize {
        if (sourceWidth <= 0 || sourceHeight <= 0) {
            return TankSpriteSize(LONG_AXIS.roundToInt(), 20)
        }
        val shortAxis = (LONG_AXIS * sourceHeight.toFloat() / sourceWidth.toFloat())
            .coerceIn(MIN_SHORT_AXIS, MAX_SHORT_AXIS)
        return TankSpriteSize(
            width = LONG_AXIS.roundToInt(),
            height = shortAxis.roundToInt(),
        )
    }
}

private object BattlefieldTankSpriteLoader {
    private val sprites = mutableMapOf<VehicleId, ImageBitmap>()
    private val atlases = mutableMapOf<String, Bitmap>()

    @Synchronized
    fun load(context: Context, vehicleId: VehicleId): ImageBitmap? {
        sprites[vehicleId]?.let { return it }
        val bitmap = decodeFullResolution(context, vehicleId)
            ?: decodeAtlasRegion(context, vehicleId)
            ?: return null
        return bitmap.asImageBitmap().also { sprites[vehicleId] = it }
    }

    private fun decodeFullResolution(context: Context, vehicleId: VehicleId): Bitmap? {
        val stem = OriginalArtNames.vehicle(vehicleId)
        val resourceId = context.resources.getIdentifier(stem, "drawable", context.packageName)
        if (resourceId == 0) return null
        return BitmapFactory.decodeResource(
            context.resources,
            resourceId,
            BitmapFactory.Options().apply { inScaled = false },
        )
    }

    private fun decodeAtlasRegion(context: Context, vehicleId: VehicleId): Bitmap? {
        val region = OriginalArtCatalog.vehicle(vehicleId) ?: return null
        val atlas = atlases[region.atlas] ?: decodeAtlas(context, region.atlas)?.also {
            atlases[region.atlas] = it
        } ?: return null
        if (
            region.x < 0 || region.y < 0 || region.width <= 0 || region.height <= 0 ||
            region.x + region.width > atlas.width || region.y + region.height > atlas.height
        ) {
            return null
        }
        return Bitmap.createBitmap(atlas, region.x, region.y, region.width, region.height)
    }

    private fun decodeAtlas(context: Context, atlasName: String): Bitmap? = runCatching {
        val parts = OriginalArtCatalog.atlasParts[atlasName].orEmpty()
        require(parts.isNotEmpty()) { "Unknown tank atlas: $atlasName" }
        val encoded = buildString {
            parts.forEach { part ->
                context.assets.open("original_art/$part").bufferedReader().use { reader ->
                    append(reader.readText())
                }
            }
        }
        val bytes = Base64.decode(encoded, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }.getOrNull()
}

@Composable
internal fun rememberBattlefieldTankSprites(
    vehicleIds: Set<VehicleId>,
): Map<VehicleId, ImageBitmap> {
    val context = LocalContext.current.applicationContext
    val orderedIds = vehicleIds.sortedBy { it.wireValue }
    return remember(context, orderedIds) {
        orderedIds.mapNotNull { id ->
            BattlefieldTankSpriteLoader.load(context, id)?.let { id to it }
        }.toMap()
    }
}

package com.songtaoluo.battlecity.ui.art

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext

private object OriginalArtLoader {
    private val atlases = mutableMapOf<String, Bitmap>()
    private val regions = mutableMapOf<AtlasRegion, Bitmap>()

    @Synchronized
    fun region(context: Context, region: AtlasRegion): Bitmap? {
        regions[region]?.let { return it }
        val atlas = atlases[region.atlas] ?: decodeAtlas(context, region.atlas)?.also {
            atlases[region.atlas] = it
        } ?: return null
        if (region.x < 0 || region.y < 0 || region.width <= 0 || region.height <= 0 ||
            region.x + region.width > atlas.width || region.y + region.height > atlas.height
        ) return null
        return Bitmap.createBitmap(atlas, region.x, region.y, region.width, region.height).also {
            regions[region] = it
        }
    }

    private fun decodeAtlas(context: Context, atlas: String): Bitmap? = runCatching {
        val parts = OriginalArtCatalog.atlasParts[atlas].orEmpty()
        require(parts.isNotEmpty()) { "Unknown original-art atlas: $atlas" }
        val encoded = buildString {
            parts.forEach { part ->
                context.assets.open("original_art/$part").bufferedReader().use { append(it.readText()) }
            }
        }
        val bytes = Base64.decode(encoded, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: error("Unable to decode original-art atlas: $atlas")
    }.getOrNull()
}

@Composable
fun OriginalArtImage(
    region: AtlasRegion?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = 1f,
) {
    if (region == null) return
    val context = LocalContext.current
    val bitmap = remember(region) { OriginalArtLoader.region(context, region) } ?: return
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        alpha = alpha,
    )
}

package com.songtaoluo.battlecity.ui.art

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource

@Composable
fun PreferredOriginalArtImage(
    resourceStem: String,
    region: AtlasRegion?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = 1f,
) {
    val context = LocalContext.current
    val resourceId = remember(resourceStem, context.packageName) {
        context.resources.getIdentifier(resourceStem, "drawable", context.packageName)
    }

    if (resourceId != 0) {
        Image(
            painter = painterResource(resourceId),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            alpha = alpha,
        )
    } else {
        OriginalArtImage(
            region = region,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            alpha = alpha,
        )
    }
}

package com.songtaoluo.battlecity.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun FrontEndLayout(content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize().background(Color(0xFF10150F))) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 30.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content,
        )
    }
}

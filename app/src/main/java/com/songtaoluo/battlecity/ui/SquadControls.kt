package com.songtaoluo.battlecity.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.songtaoluo.battlecity.model.SquadOrder

@Composable
internal fun SquadControls(
    current: SquadOrder,
    onChange: (SquadOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = modifier) {
        SquadButton("跟随", SquadOrder.FOLLOW, current, onChange)
        SquadButton("坚守", SquadOrder.HOLD, current, onChange)
        SquadButton("突击", SquadOrder.ASSAULT, current, onChange)
        SquadButton("集火", SquadOrder.FOCUS_FIRE, current, onChange)
    }
}

@Composable
private fun SquadButton(
    label: String,
    value: SquadOrder,
    current: SquadOrder,
    onChange: (SquadOrder) -> Unit,
) {
    Button(
        onClick = { onChange(value) },
        enabled = current != value,
    ) {
        Text(label)
    }
}

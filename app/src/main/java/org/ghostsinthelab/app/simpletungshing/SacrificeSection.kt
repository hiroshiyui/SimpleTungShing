package org.ghostsinthelab.app.simpletungshing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.ghostsinthelab.app.simpletungshing.data.SacrificeGroup

/**
 * Settings card choosing which [SacrificeGroup]s the month grid marks with a dot. Each
 * group is a whole-row checkbox so a screen reader names it by its label.
 */
@Composable
fun SacrificeSection(selected: Set<SacrificeGroup>, onChange: (Set<SacrificeGroup>) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(R.string.settings_sacrifice_days), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.settings_sacrifice_days_note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            for (group in SacrificeGroup.entries) {
                val checked = group in selected
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .toggleable(
                            value = checked,
                            role = Role.Checkbox,
                            onValueChange = { onChange(if (it) selected + group else selected - group) },
                        ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(checked = checked, onCheckedChange = null)
                    Text(stringResource(group.label), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

private val SacrificeGroup.label: Int
    get() = when (this) {
        SacrificeGroup.FIRST_AND_FIFTEENTH -> R.string.sacrifice_group_first_fifteenth
        SacrificeGroup.SECOND_AND_SIXTEENTH -> R.string.sacrifice_group_second_sixteenth
        SacrificeGroup.FESTIVALS -> R.string.sacrifice_group_festivals
    }

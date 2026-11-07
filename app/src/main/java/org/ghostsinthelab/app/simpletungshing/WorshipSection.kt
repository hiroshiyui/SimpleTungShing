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
import org.ghostsinthelab.app.simpletungshing.data.WorshipGroup

/**
 * Settings card choosing which [WorshipGroup]s the month grid marks with a dot. Each
 * group is a whole-row checkbox so a screen reader names it by its label.
 */
@Composable
fun WorshipSection(selected: Set<WorshipGroup>, onChange: (Set<WorshipGroup>) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(R.string.settings_worship_days), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.settings_worship_days_note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            for (group in WorshipGroup.entries) {
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

private val WorshipGroup.label: Int
    get() = when (this) {
        WorshipGroup.FIRST_AND_FIFTEENTH -> R.string.worship_group_first_fifteenth
        WorshipGroup.SECOND_AND_SIXTEENTH -> R.string.worship_group_second_sixteenth
        WorshipGroup.FESTIVALS -> R.string.worship_group_festivals
        WorshipGroup.DEITY_BIRTHDAYS -> R.string.worship_group_deity_birthdays
    }

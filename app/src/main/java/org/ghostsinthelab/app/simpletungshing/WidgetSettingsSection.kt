package org.ghostsinthelab.app.simpletungshing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.ghostsinthelab.app.simpletungshing.data.MoonMethod
import org.ghostsinthelab.app.simpletungshing.widget.BackgroundStyle
import org.ghostsinthelab.app.simpletungshing.widget.ContentAlignment
import org.ghostsinthelab.app.simpletungshing.widget.WidgetSettings
import kotlin.math.roundToInt

@Composable
fun WidgetSettingsSection(settings: WidgetSettings, onChange: (WidgetSettings) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.titleMedium)

            Label(stringResource(R.string.settings_background))
            Choices(
                options = listOf(
                    BackgroundStyle.SOLID to stringResource(R.string.style_solid),
                    BackgroundStyle.GRADIENT to stringResource(R.string.style_gradient),
                    BackgroundStyle.GLASS to stringResource(R.string.style_glass),
                ),
                selected = settings.style,
                onSelect = { onChange(settings.copy(style = it)) },
            )
            if (settings.style == BackgroundStyle.GLASS) Note(stringResource(R.string.style_glass_note))

            SettingSlider(
                value = settings.opacity,
                range = 0f..1f,
                steps = 19,
                label = { stringResource(R.string.settings_opacity, (it * 100).roundToInt()) },
                onCommit = { onChange(settings.copy(opacity = it)) },
            )

            Label(stringResource(R.string.settings_layout))
            DpSlider(
                value = settings.marginDp,
                range = WidgetSettings.MARGIN_RANGE,
                label = { stringResource(R.string.settings_margin, it) },
                onCommit = { onChange(settings.copy(marginDp = it)) },
            )
            DpSlider(
                value = settings.paddingDp,
                range = WidgetSettings.PADDING_RANGE,
                label = { stringResource(R.string.settings_padding, it) },
                onCommit = { onChange(settings.copy(paddingDp = it)) },
            )
            Note(stringResource(R.string.settings_alignment))
            Choices(
                options = listOf(
                    ContentAlignment.START to stringResource(R.string.align_start),
                    ContentAlignment.CENTER to stringResource(R.string.align_center),
                    ContentAlignment.END to stringResource(R.string.align_end),
                ),
                selected = settings.alignment,
                onSelect = { onChange(settings.copy(alignment = it)) },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Label(stringResource(R.string.settings_show_moon))
                Switch(
                    checked = settings.showMoon,
                    onCheckedChange = { onChange(settings.copy(showMoon = it)) },
                )
            }

            Label(stringResource(R.string.settings_moon_method))
            Choices(
                options = listOf(
                    MoonMethod.LUNAR_DAY to stringResource(R.string.moon_method_lunar),
                    MoonMethod.ASTRONOMICAL to stringResource(R.string.moon_method_astronomical),
                ),
                selected = settings.moonMethod,
                onSelect = { onChange(settings.copy(moonMethod = it)) },
            )
            Note(
                stringResource(
                    when (settings.moonMethod) {
                        MoonMethod.LUNAR_DAY -> R.string.moon_method_lunar_note
                        MoonMethod.ASTRONOMICAL -> R.string.moon_method_astronomical_note
                    },
                ),
            )
        }
    }
}

/** A slider that follows the thumb locally while dragging and saves once released. */
@Composable
private fun SettingSlider(
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    label: @Composable (Float) -> String,
    onCommit: (Float) -> Unit,
) {
    var dragging by remember { mutableStateOf<Float?>(null) }
    val shown = dragging ?: value
    Column {
        Note(label(shown))
        Slider(
            value = shown,
            onValueChange = { dragging = it },
            onValueChangeFinished = {
                dragging?.let(onCommit)
                dragging = null
            },
            valueRange = range,
            steps = steps,
        )
    }
}

/** Whole dp in steps of 2. */
@Composable
private fun DpSlider(value: Int, range: IntRange, label: @Composable (Int) -> String, onCommit: (Int) -> Unit) {
    SettingSlider(
        value = value.toFloat(),
        range = range.first.toFloat()..range.last.toFloat(),
        steps = (range.last - range.first) / 2 - 1,
        label = { label(it.roundToInt()) },
        onCommit = { onCommit(it.roundToInt()) },
    )
}

@Composable
private fun <T> Choices(options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (value, label) ->
            SegmentedButton(
                selected = value == selected,
                onClick = { onSelect(value) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
            ) { Text(label) }
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(text, style = MaterialTheme.typography.bodyLarge)
}

@Composable
private fun Note(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

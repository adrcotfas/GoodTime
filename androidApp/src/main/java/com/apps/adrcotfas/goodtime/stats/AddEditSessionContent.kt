/**
 *     Goodtime Productivity
 *     Copyright (C) 2025 Adrian Cotfas
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.apps.adrcotfas.goodtime.stats

import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.apps.adrcotfas.goodtime.bl.TimeUtils.formatToPrettyDateAndTime
import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.model.Session
import com.apps.adrcotfas.goodtime.ui.common.EditableNumberListItem
import compose.icons.EvaIcons
import compose.icons.evaicons.Outline
import compose.icons.evaicons.outline.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import java.time.format.TextStyle

@Composable
fun AddEditSessionContent(
    session: Session,
    labels: List<Label>,
    onOpenDatePicker: () -> Unit,
    onOpenTimePicker: () -> Unit,
    onOpenLabelSelector: () -> Unit,
    onUpdate: (Session) -> Unit,
    onValidate: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EditableNumberListItem(
            title = "Work duration",
            value = session.duration.toInt(),
            icon = {
                Icon(
                    modifier = Modifier.alpha(0f),
                    imageVector = Icons.Default.Close,
                    contentDescription = "Duration",
                )
            },
            restoreValueOnClearFocus = false,
            onValueChange = { onUpdate(session.copy(duration = it.toLong())) },
            onValueEmpty = { onValidate(!it) },
        )

        val context = LocalContext.current
        val locale = context.resources.configuration.locales[0]

        val daysOfWeekNames =
            DayOfWeekNames(DayOfWeek.entries.map { it.getDisplayName(TextStyle.SHORT, locale) })
        val monthNames =
            MonthNames(Month.entries.map { it.getDisplayName(TextStyle.SHORT, locale) })

        val (date, time) = session.timestamp.formatToPrettyDateAndTime(
            DateFormat.is24HourFormat(
                context,
            ),
            daysOfWeekNames,
            monthNames,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier
                    .weight(0.7f)
                    .clickable {
                        onOpenDatePicker()
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
            ) {
                Icon(
                    modifier = Modifier.padding(16.dp),
                    imageVector = EvaIcons.Outline.Clock,
                    contentDescription = "Time",
                )
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Row(
                modifier = Modifier
                    .height(48.dp)
                    .weight(0.3f)
                    .clickable {
                        onOpenTimePicker()
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    modifier = Modifier.padding(end = 24.dp),
                    text = time,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        if (labels.isNotEmpty()) {
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onOpenLabelSelector()
                    },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                leadingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Label,
                        contentDescription = "Label",
                    )
                },
                headlineContent = {
                    val colorIndex = labels.first { it.name == session.label }.colorIndex
                    LabelChip(session.label, colorIndex, onOpenLabelSelector)
                },
            )
        }

//        var enableInterruptions by rememberSaveable { mutableStateOf(session.interruptions > 0) }
//        EditableNumberListItem(
//            minValue = 0,
//            title = "Interruptions",
//            value = session.interruptions.toInt(),
//            enableSwitch = true,
//            switchValue = enableInterruptions,
//            onSwitchChange = {
//                enableInterruptions = it
//                onUpdate(session.copy(interruptions = if (it) session.interruptions else 0))
//            },
//            onValueChange = { onUpdate(session.copy(interruptions = it.toLong())) },
//            onValueEmpty = { onValidate(!it) },
//        )
    }
}
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
package com.apps.adrcotfas.goodtime.common

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.until
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

fun Duration.formatOverview(): String {
    val hours = this.inWholeHours
    val remMin = this.inWholeMinutes % 1.hours.inWholeMinutes

    return when {
        this.inWholeMinutes == 0L -> "0 min"
        else -> buildString {
            if (hours != 0L) append("${hours}h ")
            if (remMin != 0L) append("${remMin}min")
        }.trim()
    }
}

object Time {

    fun startOfToday(): Long {
        val (dateTime, timeZone) = currentDateTimeAndTimeZone()
        val startOfDay = dateTime.date.atStartOfDayIn(timeZone)
        return startOfDay.toEpochMilliseconds()
    }

    fun startOfThisWeekAdjusted(startDayOfWeek: DayOfWeek): Long {
        val (dateTime, timeZone) = currentDateTimeAndTimeZone()
        var date = dateTime.date
        while (date.dayOfWeek != startDayOfWeek) {
            date = date.minus(1, DateTimeUnit.DAY)
        }

        val startOfWeek = date.atStartOfDayIn(timeZone)
        return startOfWeek.toEpochMilliseconds()
    }

    fun startOfThisMonth(): Long {
        val (dateTime, timeZone) = currentDateTimeAndTimeZone()
        val date = LocalDate(
            dateTime.date.year,
            dateTime.date.month,
            1,
        )
        val startOfMonth = date.atStartOfDayIn(timeZone)
        return startOfMonth.toEpochMilliseconds()
    }

    fun thisYear(): Int {
        val dateTime = currentDateTimeAndTimeZone()
        return dateTime.first.year
    }

    fun thisMonth(): Int {
        val dateTime = currentDateTimeAndTimeZone()
        return dateTime.first.monthNumber
    }

    fun isoWeekNumber(): Int {
        val dateTime = currentDateTimeAndTimeZone().first
        val date = dateTime.date
        return date.isoWeekNumber()
    }

    private fun currentDateTimeAndTimeZone(): Pair<LocalDateTime, TimeZone> {
        val timeZone = TimeZone.currentSystemDefault()
        val now = Clock.System.now().toEpochMilliseconds()
        val currentInstant = Instant.fromEpochMilliseconds(now)
        return currentInstant.toLocalDateTime(timeZone) to timeZone
    }

    private fun LocalDate.isoWeekNumber(): Int {
        if (firstWeekInYearStart(year + 1) < this) return 1
        val currentYearStart = firstWeekInYearStart(year)
        val start = if (this < currentYearStart) firstWeekInYearStart(year - 1) else currentYearStart
        val currentCalendarWeek = start.until(this, DateTimeUnit.WEEK) + 1
        return currentCalendarWeek
    }

    private fun firstWeekInYearStart(year: Int): LocalDate {
        val jan1st = LocalDate(year, 1, 1)
        val previousMonday = jan1st.minus(jan1st.dayOfWeek.ordinal, DateTimeUnit.DAY)
        return if (jan1st.dayOfWeek <= DayOfWeek.THURSDAY) {
            previousMonday
        } else {
            previousMonday.plus(
                1,
                DateTimeUnit.WEEK,
            )
        }
    }
}

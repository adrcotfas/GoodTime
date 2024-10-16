package com.apps.adrcotfas.goodtime.bl

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime

//TODO: find a better name and place for this
object TimeUtils {
    fun Long.formatMilliseconds(minutesOnly: Boolean = false): String {
        val totalSeconds = (this / 1000).run { if (minutesOnly) this + 59 else this }
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val secondsString = if (seconds < 10) "0$seconds" else seconds.toString()
        val minutesString = if (minutes < 10) "0$minutes" else minutes.toString()
        return if (minutesOnly) {
            minutesString
        } else {
            "$minutesString:$secondsString"
        }
    }

    fun Long.formatForBackupFileName(): String {
        val instant = Instant.fromEpochMilliseconds(this)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val format = LocalDateTime.Format {
            year()
            char('-')
            monthNumber()
            char('-')
            dayOfMonth()
            char('-')
            hour()
            char('-')
            minute()
        }
        return format.format(dateTime)
    }

    fun Long.formatToIso8601(): String {
        val instant = Instant.fromEpochMilliseconds(this)

        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

        val format = LocalDateTime.Format {
            year()
            char('-')
            monthNumber()
            char('-')
            dayOfMonth()
            char('T')
            hour()
            char(':')
            minute()
            chars(":00")
        }
        return format.format(dateTime)
    }
}
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
package com.apps.adrcotfas.goodtime.fakes

import androidx.room.RoomRawQuery
import com.apps.adrcotfas.goodtime.data.local.LocalSession
import com.apps.adrcotfas.goodtime.data.local.SessionDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeSessionDao : SessionDao {

    private val sessions = MutableStateFlow<List<LocalSession>>(emptyList())

    override suspend fun insert(session: LocalSession): Long {
        sessions.value += session
        return sessions.value.size.toLong()
    }

    override suspend fun update(
        newTimestamp: Long,
        newDuration: Long,
        newInterruptions: Long,
        newLabel: String,
        newNotes: String,
        id: Long,
    ) {
        sessions.value = sessions.value.map {
            if (it.id == id) {
                it.copy(
                    timestamp = newTimestamp,
                    duration = newDuration,
                    interruptions = newInterruptions,
                    labelName = newLabel,
                    notes = newNotes,
                )
            } else {
                it
            }
        }
    }

    override suspend fun updateLabelByIds(newLabel: String, ids: List<Long>) {
        sessions.value = sessions.value.map {
            if (it.id in ids) {
                it.copy(labelName = newLabel)
            } else {
                it
            }
        }
    }

    override fun selectAll(): Flow<List<LocalSession>> {
        return sessions
    }

    override fun selectAfter(timestamp: Long): Flow<List<LocalSession>> {
        return sessions.map { sessions -> sessions.filter { it.timestamp > timestamp } }
    }

    override fun selectById(id: Long): Flow<LocalSession> {
        return sessions.map { sessions -> sessions.first { it.id == id } }
    }

    override fun selectByIsArchived(isArchived: Boolean): Flow<List<LocalSession>> {
        return sessions.map { sessions -> sessions.filter { it.isArchived == isArchived } }
    }

    override fun selectByLabel(labelName: String): Flow<List<LocalSession>> {
        return sessions.map { sessions -> sessions.filter { it.labelName == labelName } }
    }

    override fun selectByLabels(labelNames: List<String>): Flow<List<LocalSession>> {
        return sessions.map { sessions -> sessions.filter { it.labelName in labelNames } }
    }

    override suspend fun delete(id: Long) {
        sessions.value = sessions.value.filter { it.id != id }
    }

    override suspend fun deleteByIds(ids: List<Long>) {
        sessions.value = sessions.value.filter { it.id !in ids }
    }

    override suspend fun deleteAll() {
        sessions.value = emptyList()
    }

    override fun checkpoint(query: RoomRawQuery): Int {
        return 0
    }
}
package com.apps.adrcotfas.goodtime.bl

import co.touchlab.kermit.Logger
import com.apps.adrcotfas.goodtime.data.local.LocalDataRepository
import com.apps.adrcotfas.goodtime.data.model.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FinishedSessionsHandler(
    private val coroutineScope: CoroutineScope,
    private val repo: LocalDataRepository,
    private val log: Logger
) {
    fun updateSession(newSession: Session) {
        log.v { "Updating a finished session" }
        coroutineScope.launch {
            try {
                val lastSessionId = repo.selectLastInsertSessionId()
                log.v { "lastSessionId: $lastSessionId" }
                if (lastSessionId != null) {
                    val savedSession = repo.selectSessionById(lastSessionId).first()
                    if (savedSession.startTimestamp == newSession.startTimestamp) {
                        repo.updateSession(lastSessionId, newSession)
                    } else {
                        log.e{ "The last saved session timestamp does not match the updated one" }
                    }
                }
            } catch (e: Exception) {
                log.e { "Error updating work time at reset: $e" }
                when (e) {
                    !is NoSuchElementException -> throw e
                }
            }
        }
    }

    fun saveSession(session: Session) {
        log.i { "Saving session to stats: $session" }
        coroutineScope.launch {
            repo.insertSession(session)
        }
    }
}
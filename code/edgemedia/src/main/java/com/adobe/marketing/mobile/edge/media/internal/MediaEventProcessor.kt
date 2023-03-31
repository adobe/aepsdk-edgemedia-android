/*
  Copyright 2023 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.edge.media.internal

import androidx.annotation.VisibleForTesting
import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.edge.media.internal.MediaInternalConstants.LOG_TAG
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMMediaEvent
import com.adobe.marketing.mobile.services.Log
import java.util.UUID

internal class MediaEventProcessor(
    private val mediaState: MediaState,
    private val dispatcher: (event: Event) -> Unit
) {
    private val sourceTag = "MediaEventProcessor"
    private val sessionsMutex = Any()

    // Determines if a MediaSession can be removed from the queue
    private val isMediaSessionInactive = { mediaSession: MediaSession ->
        !mediaSession.isSessionActive && mediaSession.getQueueSize() == 0
    }

    @VisibleForTesting
    internal val mediaSessions: MutableMap<String, MediaSession> = mutableMapOf()

    /**
     * Creates new [MediaSession], assigning it a new random ID.
     * @return the session ID of the new [MediaSession]
     */
    fun createSession(): String {
        synchronized(sessionsMutex) {
            val sessionId = UUID.randomUUID().toString()
            val session = MediaRealTimeSession(sessionId, mediaState, dispatcher)
            mediaSessions[sessionId] = session
            Log.trace(LOG_TAG, sourceTag, "Created new session ($sessionId)")
            return sessionId
        }
    }

    /**
     * Ends the [MediaSession] with the given `sessionId`.
     * @param sessionId the ID of the [MediaSession] to end
     * @see [MediaSession.end]
     */
    fun endSession(sessionId: String) {
        synchronized(sessionsMutex) {
            val session = mediaSessions[sessionId]
            if (session != null) {
                session.end()
                if (isMediaSessionInactive(session)) {
                    mediaSessions.remove(sessionId)
                }
            } else {
                Log.trace(
                    LOG_TAG,
                    sourceTag,
                    "Cannot end media session as session ID ($sessionId) is invalid."
                )
            }
        }
    }

    /**
     * Queues the [XDMMediaEvent] for processing in the [MediaSession] with ID `sessionId`.
     * @param sessionId the ID of the [MediaSession] to queue the event
     * @param event the [XDMMediaEvent] to process
     * @see [MediaSession.queue]
     */
    fun processEvent(sessionId: String, event: XDMMediaEvent) {
        synchronized(sessionsMutex) {
            val session = mediaSessions[sessionId]
            if (session != null) {
                session.queue(event)
                Log.trace(
                    LOG_TAG,
                    sourceTag,
                    "Successfully queued event (${event.xdmData.eventType}) for session ($sessionId)"
                )
            } else {
                Log.trace(
                    LOG_TAG,
                    sourceTag,
                    "Cannot queue event (${event.xdmData.eventType}) as session ID ($sessionId) is invalid."
                )
            }
        }
    }

    /**
     * Sets the `backendSessionId` to the [MediaSession] with matching `requestEventId`.
     * @see [MediaSession.handleSessionUpdate]
     */
    fun notifyBackendSessionId(requestEventId: String, backendSessionId: String?) {
        synchronized(sessionsMutex) {
            mediaSessions.forEach { (_, session) ->
                session.handleSessionUpdate(requestEventId, backendSessionId)
            }

            // Session may be aborted if backend session ID is invalid
            mediaSessions.values.removeAll { isMediaSessionInactive(it) }
        }
    }

    /**
     * Notify [MediaSession] with matching `requestEventId` of the error response from
     * the backend service.
     * @see [MediaSession.handleErrorResponse]
     */
    fun notifyErrorResponse(requestEventId: String, data: Map<String, Any>) {
        synchronized(sessionsMutex) {
            mediaSessions.forEach { (_, session) ->
                session.handleErrorResponse(requestEventId, data)
            }

            // Session may be aborted on error response
            mediaSessions.values.removeAll { isMediaSessionInactive(it) }
        }
    }

    /**
     * Update the [MediaState] with the given `stateData` and notify all [MediaSession]s of
     * the state update.
     * @param stateData Map containing Configuration shared state data
     * @see [MediaSession.handleMediaStateUpdate]
     */
    fun updateMediaState(stateData: Map<String, Any>) {
        synchronized(sessionsMutex) {
            mediaState.updateState(stateData)
            mediaSessions.forEach { (_, session) ->
                session.handleMediaStateUpdate()
            }
        }
    }

    /**
     * Abort all the active [MediaSession]s.
     * @see [MediaSession.abort]
     */
    fun abortAllSessions() {
        synchronized(sessionsMutex) {
            mediaSessions.forEach { (_, session) ->
                session.abort()
            }

            // Remove all session after abort
            mediaSessions.clear()
        }
    }
}

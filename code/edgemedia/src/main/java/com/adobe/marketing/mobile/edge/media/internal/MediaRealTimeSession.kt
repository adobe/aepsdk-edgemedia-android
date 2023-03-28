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
import com.adobe.marketing.mobile.EventSource
import com.adobe.marketing.mobile.EventType
import com.adobe.marketing.mobile.edge.media.internal.MediaInternalConstants.LOG_TAG
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMMediaEvent
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMMediaEventType
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.util.StringUtils

internal class MediaRealTimeSession(
    id: String,
    state: MediaState,
    dispatchHandler: (Event) -> Unit
) : MediaSession(id, state, dispatchHandler) {

    private val sourceTag = "MediaRealTimeSession" // Log source tag

    // Session id for this session, set when handling sessionStart response from backend media server
    @VisibleForTesting
    internal var mediaBackendSessionId: String? = null
        set(value) {
            field = if (!StringUtils.isNullOrEmpty(value)) value else null
        }

    // Edge request id for this session, set when the sessionStart event is dispatched
    @VisibleForTesting
    internal var sessionStartEdgeRequestId: String? = null

    // List of events to be processed
    @VisibleForTesting
    internal val events: MutableList<XDMMediaEvent> = mutableListOf()

    /**
     * Handles media state update notifications by triggering the event processing loop.
     */
    override fun handleMediaStateUpdate() {
        processMediaEvents()
    }

    /**
     * Handles session end requests. Attempts to finish processing any queued events and calls the
     * session end closure if all events were processed.
     * @param sessionEndHandler closure called after session is successfully ended
     * @see [MediaSession.end]
     */
    override fun handleSessionEnd(sessionEndHandler: () -> Unit) {
        processMediaEvents()
        if (events.isEmpty()) {
            sessionEndHandler()
        }
    }

    /**
     * Handles session abort requests. Removes all queued events and calls the session end closure.
     * @param sessionAbortHandler closure called after session is successfully aborted
     * @see [MediaSession.abort]
     */
    override fun handleSessionAbort(sessionAbortHandler: () -> Unit) {
        events.clear()
        sessionAbortHandler()
    }

    /**
     * Queues the [XDMMediaEvent] and triggers the event processing loop.
     * @see [MediaSession.queue]
     */
    override fun handleQueueEvent(event: XDMMediaEvent) {
        events.add(event)
        processMediaEvents()
    }

    /**
     * Handles the media backend session id dispatched from the Edge extension.
     * If the backend session id is valid (not null or empty) then triggers the event processing
     * loop. Aborts the current session if the backend session id is invalid.
     *
     * @param requestEventId the [Edge] request event ID
     * @param backendSessionId the backend session ID for the current [MediaSession]
     *
     * @see [MediaSession.abort]
     */
    override fun handleSessionUpdate(requestEventId: String, backendSessionId: String?) {
        if (requestEventId != sessionStartEdgeRequestId) {
            return
        }

        mediaBackendSessionId = backendSessionId
        if (mediaBackendSessionId != null) {
            processMediaEvents()
        } else {
            abort()
        }
    }

    /**
     * Handles media backend error response dispatched from the Edge extension.
     * Handles errors of type `va-edge-0400-400` and code `400` by aborting the current session.
     *
     * @param requestEventId the [Edge] request event ID
     * @param data contains errors returned by the backend server
     *
     * @see [MediaSession.abort]
     */
    override fun handleErrorResponse(requestEventId: String, data: Map<String, Any>) {
        if (requestEventId != sessionStartEdgeRequestId) {
            return
        }

        val statusCode = data["status"]
        val errorType = data["type"]

        if (statusCode !is Long || errorType !is String) {
            return
        }

        if (statusCode == MediaInternalConstants.Edge.ERROR_CODE_400 && errorType == MediaInternalConstants.Edge.ERROR_TYPE_VA_EDGE_400) {
            Log.warning(LOG_TAG, sourceTag, "handleErrorResponse - Session $id: Aborting session as error occurred while dispatching session start request. $data")
            abort()
        }
    }

    /**
     * Processes queued [XDMMediaEvent]s.
     * If no backend session id is set and the event type is not `sessionStart`, then processing
     * is stopped until a valid backend session id is received.
     * Dispatches an experience event to the Edge extension for each successfully processed event.
     */
    private fun processMediaEvents() {
        if (!state.isValid) {
            Log.trace(LOG_TAG, sourceTag, "processMediaEvents - Session $id: Exiting as the required configuration is missing. Verify 'media.channel' and 'media.playerName' are configured.")
            return
        }

        while (events.isNotEmpty()) {
            val event = events.first()

            if (event.xdmData.eventType != XDMMediaEventType.SESSION_START && mediaBackendSessionId == null) {
                Log.trace(LOG_TAG, sourceTag, "processMediaEvents - Session $id: Exiting as the media session id is unavailable, will retry later.")
                return
            }

            attachMediaStateInfo(event)

            dispatchExperienceEvent(event, dispatchHandler)

            events.removeFirst()
        }
    }

    /**
     * Attaches the required [MediaState] information to the given [XDMMediaEvent].
     */
    private fun attachMediaStateInfo(event: XDMMediaEvent) {
        if (XDMMediaEventType.SESSION_START == event.xdmData.eventType) {
            event.xdmData.mediaCollection.sessionDetails?.playerName = state.mediaPlayerName
            event.xdmData.mediaCollection.sessionDetails?.appVersion = state.mediaAppVersion
            if (event.xdmData.mediaCollection.sessionDetails?.channel == null) {
                event.xdmData.mediaCollection.sessionDetails?.channel = state.mediaChannel
            }
        } else {
            event.xdmData.mediaCollection.sessionID = mediaBackendSessionId
            if (XDMMediaEventType.AD_START == event.xdmData.eventType) {
                event.xdmData.mediaCollection.advertisingDetails?.playerName = state.mediaPlayerName
            }
        }
    }

    /**
     * Dispatches a experience event to the Edge extension to send to the media backend service.
     */
    private fun dispatchExperienceEvent(mediaEvent: XDMMediaEvent, dispatcher: (event: Event) -> Unit) {
        val edgeEvent = Event.Builder(
            "Edge Media - ${XDMMediaEventType.getTypeString(mediaEvent.xdmData.eventType)}",
            EventType.EDGE,
            EventSource.REQUEST_CONTENT
        )
            .setEventData(mediaEvent.serializeToXDM())
            .build()

        if (XDMMediaEventType.SESSION_START == mediaEvent.xdmData.eventType) {
            sessionStartEdgeRequestId = edgeEvent.uniqueIdentifier
        }

        // Dispatch the media event to the eventhub to be sent to the backend service by the edge extension
        dispatcher(edgeEvent)
    }
}

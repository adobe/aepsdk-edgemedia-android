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
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMMediaEvent
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMMediaEventType
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.util.StringUtils

internal class MediaRealTimeSession(
    id: String,
    state: MediaState,
    dispatchHandler: (Event) -> Unit
) : MediaSession(id, state, dispatchHandler) {

    companion object {
        private const val LOG_TAG = MediaInternalConstants.LOG_TAG
        private const val SOURCE_TAG = "MediaRealTimeSession"
    }

    @VisibleForTesting
    internal var mediaBackendSessionId: String? = null
        set(value) {
            field = if (!StringUtils.isNullOrEmpty(value)) value else null
        }

    @VisibleForTesting
    internal var sessionStartEdgeRequestId: String? = null

    @VisibleForTesting
    internal val events: MutableList<XDMMediaEvent> = mutableListOf()

    override fun handleMediaStateUpdate() {
        processMediaEvents()
    }

    override fun handleSessionEnd() {
        processMediaEvents()
        if (events.isEmpty()) {
            sessionEndHandler()
        }
    }

    override fun handleSessionAbort() {
        events.clear()
        sessionEndHandler()
    }

    override fun handleQueueEvent(event: XDMMediaEvent) {
        events.add(event)
        processMediaEvents()
    }

    override fun handleSessionUpdate(requestEventId: String, backendSessionId: String?) {
        if (requestEventId != sessionStartEdgeRequestId) {
            return
        }

        mediaBackendSessionId = backendSessionId
        if (mediaBackendSessionId != null) {
            processMediaEvents()
        } else {
            abort(sessionEndHandler)
        }
    }

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
            Log.warning(LOG_TAG, SOURCE_TAG, "handleErrorResponse - Session $id: Aborting session as error occurred while dispatching session start request. $data")
            abort(sessionEndHandler)
        }
    }

    private fun processMediaEvents() {
        if (!state.isValid) {
            Log.trace(LOG_TAG, SOURCE_TAG, "processMediaEvents - Session $id: Exiting as the required configuration is missing. Verify 'media.channel' and 'media.playerName' are configured.")
            return
        }

        while (events.isNotEmpty()) {
            val event = events.first()

            if (event.xdmData?.eventType != XDMMediaEventType.SESSION_START && mediaBackendSessionId == null) {
                Log.trace(LOG_TAG, SOURCE_TAG, "processMediaEvents - Session $id: Exiting as the media session id is unavailable, will retry later.")
                return
            }

            attachMediaStateInfo(event)

            dispatchExperienceEvent(event, dispatchHandler)

            events.removeFirst()
        }
    }

    private fun attachMediaStateInfo(event: XDMMediaEvent) {
        if (XDMMediaEventType.SESSION_START == event.xdmData?.eventType) {
            event.xdmData?.mediaCollection?.sessionDetails?.playerName = state.mediaPlayerName
            event.xdmData?.mediaCollection?.sessionDetails?.appVersion = state.mediaAppVersion
            if (event.xdmData?.mediaCollection?.sessionDetails?.channel == null) {
                event.xdmData?.mediaCollection?.sessionDetails?.channel = state.mediaChannel
            }
        } else {
            event.xdmData?.mediaCollection?.sessionID = mediaBackendSessionId
            if (XDMMediaEventType.AD_START == event.xdmData?.eventType) {
                event.xdmData?.mediaCollection?.advertisingDetails?.playerName = state.mediaPlayerName
            }
        }
    }

    private fun dispatchExperienceEvent(mediaEvent: XDMMediaEvent, dispatcher: (event: Event) -> Unit) {
        val edgeEvent = Event.Builder(
            "Edge Media ${mediaEvent.xdmData?.eventType?.value}",
            EventType.EDGE,
            EventSource.REQUEST_CONTENT
        )
            .setEventData(mediaEvent.serializeToXDM())
            .build()

        if (XDMMediaEventType.SESSION_START == mediaEvent.xdmData?.eventType) {
            sessionStartEdgeRequestId = edgeEvent.uniqueIdentifier
        }

        // Dispatch the media event to the eventhub to be sent to the backend service by the edge extension
        dispatcher(edgeEvent)
    }
}

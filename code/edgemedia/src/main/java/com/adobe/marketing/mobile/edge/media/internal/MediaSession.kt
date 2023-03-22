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

/**
 * A Media Session
 * @property id unique identifier for this Media Session
 * @property state [MediaState] holding state data
 * @property dispatchHandler closure for dispatching [Event]s
 */
internal abstract class MediaSession(
    protected val id: String,
    protected val state: MediaState,
    protected val dispatchHandler: (event: Event) -> Unit
) {

    private val sourceTag = "MediaSession" // Log source tag

    internal var isSessionActive: Boolean = true
        @VisibleForTesting internal set

    /**
     * Queues the [XDMMediaEvent].
     * Operation fails if the current session was ended or aborted.
     *
     * @param event the [XDMMediaEvent] to queue
     */
    fun queue(event: XDMMediaEvent) {
        if (!isSessionActive) {
            Log.debug(LOG_TAG, sourceTag, "queue - failed to queue event. Media Session ($id) is inactive.")
            return
        }
        handleQueueEvent(event)
    }

    /**
     * Ends the current session.
     * @param sessionEndHandler closure called after session is successfully ended
     */
    fun end(sessionEndHandler: () -> Unit = { }) {
        if (!isSessionActive) {
            Log.debug(LOG_TAG, sourceTag, "end - failed to end session. Media Session ($id) is inactive.")
            return
        }

        isSessionActive = false
        handleSessionEnd(sessionEndHandler)
    }

    /**
     * Aborts the current session.
     * @param sessionAbortHandler closure called after session is successfully aborted
     */
    fun abort(sessionAbortHandler: () -> Unit = { }) {
        if (!isSessionActive) {
            Log.debug(LOG_TAG, sourceTag, "abort - failed to abort session. Media Session ($id) is inactive.")
            return
        }

        isSessionActive = false
        handleSessionAbort(sessionAbortHandler)
    }

    /**
     * Called when [MediaState] for this session is updated.
     */
    abstract fun handleMediaStateUpdate()

    /**
     * Ends the current session.
     * @param sessionEndHandler closure called after session is successfully ended
     */
    protected abstract fun handleSessionEnd(sessionEndHandler: () -> Unit)

    /**
     * Aborts the current session.
     * @param sessionAbortHandler closure called after session is successfully aborted
     */
    protected abstract fun handleSessionAbort(sessionAbortHandler: () -> Unit)

    /**
     * Queues the [XDMMediaEvent] for processing.
     * @param event [XDMMediaEvent] to be queued
     */
    protected abstract fun handleQueueEvent(event: XDMMediaEvent)

    /**
     * Handles response from server containing the session ID.
     * @param requestEventId the [Edge] request event ID
     * @param backendSessionId the backend session ID for the current [MediaSession]
     * @param sessionAbortHandler closure called if the session is aborted due to an invalid backendSessionId
     */
    abstract fun handleSessionUpdate(requestEventId: String, backendSessionId: String?, sessionAbortHandler: () -> Unit = { })

    /**
     * Handles error responses from the server.
     * @param requestEventId the [Edge] request event ID
     * @param data contains errors returned by the backend server
     * @param sessionAbortHandler closure called if the session is aborted due to the error response
     */
    abstract fun handleErrorResponse(requestEventId: String, data: Map<String, Any>, sessionAbortHandler: () -> Unit = { })
}

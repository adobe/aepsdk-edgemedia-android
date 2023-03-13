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

import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMMediaEvent
import com.adobe.marketing.mobile.services.Log

/**
 * A Media Session
 * @property id unique identifier for this Media Session
 * @property trackerSessionId unique identifier for the tracker session used for debugging
 * @property state [MediaState] holding state data
 * @property dispatchHandler closure for dispatching [Event]s
 */
internal abstract class MediaSession(
    protected val id: String,
    protected val state: MediaState,
    protected val dispatchHandler: (event: Event) -> Unit
) {

    companion object {
        private const val LOG_TAG = MediaInternalConstants.LOG_TAG
        private const val SOURCE_TAG = "MediaSession"
    }

    protected var isSessionActive: Boolean = true
    protected var sessionEndHandler: () -> Unit = {}

    /**
     * Queues the [XDMMediaEvent].
     * Operation fails if the current session was ended or aborted.
     *
     * @param event the [XDMMediaEvent] to queue
     */
    fun queue(event: XDMMediaEvent) {
        if (!isSessionActive) {
            Log.debug(LOG_TAG, SOURCE_TAG, "queue - failed to queue event. Media Session ($id) is inactive.")
            return
        }
        handleQueueEvent(event)
    }

    /**
     * Ends the current session.
     * @param sessionEndHandler closure called after session is successfully ended
     */
    fun end(sessionEndHandler: () -> Unit = {}) {
        if (!isSessionActive) {
            Log.debug(LOG_TAG, SOURCE_TAG, "end - failed to end session. Media Session ($id) is inactive.")
            return
        }

        this.sessionEndHandler = sessionEndHandler
        isSessionActive = false
        handleSessionEnd()
    }

    /**
     * Aborts the current session.
     * @param sessionEndHandler closure called after session is successfully aborted
     */
    fun abort(sessionEndHandler: () -> Unit = {}) {
        if (!isSessionActive) {
            Log.debug(LOG_TAG, SOURCE_TAG, "abort - failed to abort session. Media Session ($id) is inactive.")
            return
        }

        this.sessionEndHandler = sessionEndHandler
        isSessionActive = false
        handleSessionAbort()
    }

    /**
     * Called when [MediaState] for this session is updated.
     */
    abstract fun handleMediaStateUpdate()

    /**
     * Ends the current session.
     */
    abstract fun handleSessionEnd()

    /**
     * Aborts the current session.
     */
    abstract fun handleSessionAbort()

    /**
     * Queues the [XDMMediaEvent] for processing.
     * @param event [XDMMediaEvent] to be queued
     */
    abstract fun handleQueueEvent(event: XDMMediaEvent)

    /**
     * Handles response from server containing the session ID.
     * @param requestEventId the [Edge] request event ID
     * @param backendSessionId the backend session ID for the current [MediaSession]
     */
    abstract fun handleSessionUpdate(requestEventId: String, backendSessionId: String?)

    /**
     * Handles error responses from the server.
     * @param requestEventId the [Edge] request event ID
     * @param data contains errors returned by the backend server
     */
    abstract fun handleErrorResponse(requestEventId: String, data: Map<String, Any>)
}

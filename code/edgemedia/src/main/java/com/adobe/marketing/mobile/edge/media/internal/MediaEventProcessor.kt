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

internal class MediaEventProcessor(
    private val mediaState: MediaState,
    private val dispatcher: (event: Event) -> Unit
) {
    private val SOURCE_TAG = "MediaEventProcessor"

    @VisibleForTesting
    internal val mediaSessions: MutableMap<String, MediaSession> = mutableMapOf()

    fun notifyBackendSessionId(requestEventId: String, backendSessionId: String?) {
        mediaSessions.forEach { (sessionId, session) ->
            session.handleSessionUpdate(requestEventId, backendSessionId)
        }

        // Session may be aborted if backend session ID is invalid
        mediaSessions.values.removeAll { !it.isSessionActive }
    }

    fun notifyErrorResponse(requestEventId: String, data: Map<String, Any>) {
        mediaSessions.forEach { (sessionId, session) ->
            session.handleErrorResponse(requestEventId, data)
        }

        // Session may be aborted on error response
        mediaSessions.values.removeAll { !it.isSessionActive }
    }

    fun updateMediaState(stateData: Map<String, Any>) {
        mediaState.updateState(stateData)
        mediaSessions.forEach { (sessionId, session) ->
            session.handleMediaStateUpdate()
        }
    }

    fun abortAllSessions() {
        mediaSessions.forEach { (sessionId, session) ->
            session.abort()
        }

        // Remove all session after abort
        mediaSessions.clear()
    }
}

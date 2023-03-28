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

internal class SpyMediaSession(
    id: String,
    state: MediaState,
    dispatchHandler: (Event) -> Unit
) : MediaSession(
    id,
    state,
    dispatchHandler
) {

    var handleMediaStateUpdateCalled: Boolean = false
    override fun handleMediaStateUpdate() {
        handleMediaStateUpdateCalled = true
    }

    var handleSessionEndCalled: Boolean = false
    var handleSessionEndParamEndHandler: (() -> Unit)? = null
    override fun handleSessionEnd(sessionEndHandler: () -> Unit) {
        handleSessionEndCalled = true
        handleSessionEndParamEndHandler = sessionEndHandler
    }

    var handleSessionAbortCalled: Boolean = false
    var handleSessionAbortParamAbortHandler: (() -> Unit)? = null
    override fun handleSessionAbort(sessionAbortHandler: () -> Unit) {
        handleSessionAbortCalled = true
        handleSessionAbortParamAbortHandler = sessionAbortHandler
    }

    var handleQueueEventCalled: Boolean = false
    var handleQueueEventParamXDMMediaEvent: XDMMediaEvent? = null
    override fun handleQueueEvent(event: XDMMediaEvent) {
        handleQueueEventCalled = true
        handleQueueEventParamXDMMediaEvent = event
    }

    var handleSessionUpdateCalled: Boolean = false
    var handleSessionUpdateParamRequestEventId: String? = null
    var handleSessionUpdateParamBackendSessionId: String? = null
    override fun handleSessionUpdate(requestEventId: String, backendSessionId: String?) {
        handleSessionUpdateCalled = true
        handleSessionUpdateParamRequestEventId = requestEventId
        handleSessionUpdateParamBackendSessionId = backendSessionId
    }

    var handleErrorResponseCalled: Boolean = false
    var handleErrorResponseParamRequestEventId: String? = null
    var handleErrorResponseParamData: Map<String, Any>? = null
    override fun handleErrorResponse(requestEventId: String, data: Map<String, Any>) {
        handleErrorResponseCalled = true
        handleErrorResponseParamRequestEventId = requestEventId
        handleErrorResponseParamData = data
    }
}

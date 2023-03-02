/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.edge.media.internal;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.EventType;
import com.adobe.marketing.mobile.ExtensionApi;
import java.util.HashMap;
import java.util.Map;

class MediaSessionCreatedDispatcher {

    private final ExtensionApi extensionApi;

    MediaSessionCreatedDispatcher(final ExtensionApi extensionApi) {
        this.extensionApi = extensionApi;
    }

    void dispatchSessionCreatedEvent(
            final String clientSessionId, final String mediaBackendSessionId) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put(MediaInternalConstants.EventDataKeys.Tracker.SESSION_ID, clientSessionId);
        eventData.put(
                MediaInternalConstants.EventDataKeys.Tracker.BACKEND_SESSION_ID,
                mediaBackendSessionId);

        final Event event =
                new Event.Builder(
                                "Media::SessionCreated",
                                EventType.MEDIA,
                                MediaInternalConstants.Media.EVENT_NAME_SESSION_CREATED)
                        .setEventData(eventData)
                        .build();

        extensionApi.dispatch(event);
    }
}

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

package com.adobe.marketing.mobile.media.internal;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

class MediaSessionIDManager {
    private static final int MAX_ALLOWED_FAILURE = 2;
    private final Map<String, MediaSessionState> sessionsMap;
    private final Map<String, Integer> sessionFailuresMap;

    MediaSessionIDManager(final Set<String> persistedSessions) {
        // Reports session in the order of creation.
        sessionsMap = new LinkedHashMap<>();
        sessionFailuresMap = new HashMap<>();

        for (String sessionID : persistedSessions) {
            sessionsMap.put(sessionID, MediaSessionState.Complete);
        }
    }

    String startActiveSession() {
        String sessionId = UUID.randomUUID().toString();
        sessionsMap.put(sessionId, MediaSessionState.Active);
        return sessionId;
    }

    boolean isSessionActive(final String sessionID) {
        return sessionsMap.containsKey(sessionID)
                && sessionsMap.get(sessionID) == (MediaSessionState.Active);
    }

    void updateSessionState(final String sessionID, final MediaSessionState state) {
        if (sessionsMap.containsKey(sessionID)) {
            sessionsMap.put(sessionID, state);

            // Increase Failure count
            if (state == MediaSessionState.Failed) {
                int value =
                        sessionFailuresMap.get(sessionID) == null
                                ? 0
                                : sessionFailuresMap.get(sessionID);
                sessionFailuresMap.put(sessionID, value + 1);
            }
        }
    }

    String getSessionToReport() {
        String ret = null;

        // Returns session if complete or failed with fail count < MAX_ALLOWED_FAILURE
        for (Map.Entry<String, MediaSessionState> entry : sessionsMap.entrySet()) {
            MediaSessionState state = entry.getValue();

            if (state == MediaSessionState.Complete) {
                ret = entry.getKey();
                break;
            } else if (state == MediaSessionState.Failed) {
                int failureCount =
                        sessionFailuresMap.get(entry.getKey()) == null
                                ? 0
                                : sessionFailuresMap.get(entry.getKey());

                if (failureCount <= MAX_ALLOWED_FAILURE) {
                    ret = entry.getKey();
                    break;
                }
            }
        }

        return ret;
    }

    boolean shouldClearSession(final String sessionID) {

        if (!sessionsMap.containsKey(sessionID)) {
            return true;
        }

        MediaSessionState state = sessionsMap.get(sessionID);

        if (state == MediaSessionState.Reported || state == MediaSessionState.Invalid) {
            return true;
        }

        if (state == MediaSessionState.Failed) {
            int failureCount =
                    sessionFailuresMap.get(sessionID) == null
                            ? 0
                            : sessionFailuresMap.get(sessionID);
            return failureCount > MAX_ALLOWED_FAILURE;
        }

        return false;
    }

    void clear() {
        sessionsMap.clear();
        sessionFailuresMap.clear();
    }

    enum MediaSessionState {
        // Session absent in our store / exceeded retries.
        Invalid,
        // Session is currently being tracked.
        Active,
        // Session is complete and waiting to be reported.
        Complete,
        // Session is reported to backend and we will clear the hits from db.
        Reported,
        // Session failed to report to backend. We will clear the hits from db if we exceed retries.
        Failed
    }
}

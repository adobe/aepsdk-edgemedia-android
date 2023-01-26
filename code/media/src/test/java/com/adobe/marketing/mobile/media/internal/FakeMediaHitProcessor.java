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

package com.adobe.marketing.mobile.media.internal;

import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class FakeMediaHitProcessor implements MediaHitProcessor {
    private Map<String, List<MediaHit>> hits;
    private Map<String, Boolean> sessionEnded;
    private int counter = -1;
    private String currentSessionID = "-1";

    FakeMediaHitProcessor() {
        clear();
    }

    @Override
    public @NonNull String startSession() {
        currentSessionID = Integer.toString(++counter);
        hits.put(currentSessionID, new ArrayList<>());
        return currentSessionID;
    }

    @Override
    public void processHit(String sessionID, MediaHit hit) {
        List<MediaHit> hitForSession = hits.get(sessionID);
        hitForSession.add(hit);
    }

    @Override
    public void endSession(String sessionID) {
        sessionEnded.put(sessionID, true);
    }

    public String getActiveSession() {
        return currentSessionID;
    }

    MediaHit getHitFromActiveSession(int index) {
        return getHit(currentSessionID, index);
    }

    MediaHit getHit(String sessionID, int index) {
        if (!hits.containsKey(sessionID)) {
            return null;
        }

        if (index >= hits.get(sessionID).size()) {
            return null;
        }

        return hits.get(sessionID).get(index);
    }

    int hitCountfromActiveSession() {
        return hitCount(currentSessionID);
    }

    int hitCount(String sessionID) {
        if (!hits.containsKey(sessionID)) {
            return 0;
        }

        return hits.get(sessionID).size();
    }

    void clearHitsFromActionSession() {
        if (hits.containsKey(currentSessionID)) {
            hits.get(currentSessionID).clear();
        }
    }

    public int sessionCount() {
        return hits.size();
    }

    public boolean sessionEnded(String sessionId) {
        return sessionEnded.containsKey(sessionId);
    }

    public void clear() {
        hits = new HashMap<>();
        sessionEnded = new HashMap<>();
        counter = -1;
        currentSessionID = "-1";
        hits.clear();
    }
}

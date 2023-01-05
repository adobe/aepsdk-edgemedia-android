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

package com.adobe.marketing.mobile.media;

import java.util.HashMap;
import java.util.Map;

class MediaHit {
    private final String eventType;
    private final Map<String, Object> params;
    private final Map<String, String> customMetadata;
    private final Map<String, Object> qoeData;
    private final double playhead;
    private final long ts;

    MediaHit(
            final String eventType,
            final Map<String, Object> params,
            final Map<String, String> customMetadata,
            final Map<String, Object> qoeData,
            final double playhead,
            final long ts) {
        this.eventType = eventType;
        this.playhead = playhead;
        this.ts = ts;

        if (params != null) {
            this.params = new HashMap<>(params);
        } else {
            this.params = new HashMap<>();
        }

        if (customMetadata != null) {
            this.customMetadata = new HashMap<>(customMetadata);
        } else {
            this.customMetadata = new HashMap<>();
        }

        if (qoeData != null) {
            this.qoeData = new HashMap<>(qoeData);
        } else {
            this.qoeData = new HashMap<>();
        }
    }

    String getEventType() {
        return eventType;
    }

    Map<String, Object> getParams() {
        return params;
    }

    Map<String, String> getCustomMetadata() {
        return customMetadata;
    }

    Map<String, Object> getQoEData() {
        return qoeData;
    }

    double getPlayhead() {
        return playhead;
    }

    long getTimeStamp() {
        return ts;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof MediaHit)) {
            return false;
        }

        MediaHit other = (MediaHit) o;

        return (eventType.equals(other.eventType)
                && params.equals(other.params)
                && customMetadata.equals(other.customMetadata)
                && qoeData.equals(other.qoeData)
                && playhead == other.playhead
                && ts == other.ts);
    }
}

/*
  Copyright 2017 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.edge.media.internal;

public final class MediaTestConstants {

    private MediaTestConstants() {}

    static final class Media {
        static final String EVENT_TYPE = "com.adobe.eventType.edgeMedia";
        static final String EVENT_SOURCE_TRACKER_REQUEST = "com.adobe.eventSource.createTracker";
        static final String EVENT_SOURCE_TRACK_MEDIA = "com.adobe.eventSource.trackMedia";
        static final String EVENT_SOURCE_MEDIA_EDGE_SESSION = "media-analytics:new-session";

        static final String MEDIA_TYPE_VIDEO = "video";
        static final String MEDIA_TYPE_AUDIO = "audio";

        private Media() {}
    }

    static final class Configuration {
        static final String MEDIA_CHANNEL = "edgeMedia.channel";
        static final String MEDIA_PLAYER_NAME = "edgeMedia.playerName";
        static final String MEDIA_APP_VERSION = "edgeMedia.appVersion";

        private Configuration() {}
    }

    static final class Edge {
        static final String REQUEST_EVENT_ID = "requestEventId";
        static final String PAYLOAD = "payload";
        static final String SESSION_ID = "sessionId";

        private Edge() {}
    }

    static final class EventDataKeys {

        private EventDataKeys() {}

        static final class Config {
            static final String CHANNEL = "config.channel";
            static final String DOWNLOADED_CONTENT = "config.downloadedcontent";

            private Config() {}
        }

        static final class MediaInfo {
            static final String NAME = "media.name";
            static final String ID = "media.id";
            static final String LENGTH = "media.length";
            static final String MEDIA_TYPE = "media.type";
            static final String STREAM_TYPE = "media.streamtype";
            static final String RESUMED = "media.resumed";
            static final String PREROLL_TRACKING_WAITING_TIME = "media.prerollwaitingtime";
            static final String GRANULAR_AD_TRACKING = "media.granularadtracking";

            private MediaInfo() {}
        }

        static final class AdBreakInfo {
            static final String NAME = "adbreak.name";
            static final String START_TIME = "adbreak.starttime";
            static final String POSITION = "adbreak.position";

            private AdBreakInfo() {}
        }

        static final class AdInfo {
            static final String NAME = "ad.name";
            static final String ID = "ad.id";
            static final String LENGTH = "ad.length";
            static final String POSITION = "ad.position";

            private AdInfo() {}
        }

        static final class ChapterInfo {
            static final String NAME = "chapter.name";
            static final String POSITION = "chapter.position";
            static final String LENGTH = "chapter.length";
            static final String START_TIME = "chapter.starttime";

            private ChapterInfo() {}
        }

        static final class QoEInfo {
            static final String BITRATE = "qoe.bitrate";
            static final String STARTUP_TIME = "qoe.startuptime";
            static final String FPS = "qoe.fps";
            static final String DROPPED_FRAMES = "qoe.droppedframes";

            private QoEInfo() {}
        }

        static final class ErrorInfo {
            static final String ID = "error.id";
            static final String SOURCE = "error.source";

            private ErrorInfo() {}
        }

        static final class StateInfo {
            static final String STATE_NAME_KEY = "state.name";
            static final int STATE_LIMIT = 10;

            private StateInfo() {}
        }

        // Event Data Key Constants - Tracker
        static final class Tracker {
            static final String ID = "trackerid";
            static final String CREATED = "trackercreated";
            static final String SESSION_ID = "sessionid";
            static final String EVENT_NAME = "event.name";
            static final String EVENT_PARAM = "event.param";
            static final String EVENT_METADATA = "event.metadata";
            static final String EVENT_TIMESTAMP = "event.timestamp";
            static final String EVENT_INTERNAL = "event.internal";
            static final String PLAYHEAD = "time.playhead";
            static final String BACKEND_SESSION_ID = "mediaservice.sessionid";

            private Tracker() {}
        }
    }
}

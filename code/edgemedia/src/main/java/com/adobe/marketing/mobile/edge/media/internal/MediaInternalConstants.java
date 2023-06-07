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

final class MediaInternalConstants {
    static final String EXTENSION_NAME = "com.adobe.edge.media";
    static final String FRIENDLY_NAME = "Edge Media";
    static final String LOG_TAG = FRIENDLY_NAME;

    private MediaInternalConstants() {}

    static final class Media {
        static final String EVENT_SOURCE_MEDIA_EDGE_SESSION = "media-analytics:new-session";

        private Media() {}
    }

    static final class Configuration {
        static final String SHARED_STATE_NAME = "com.adobe.module.configuration";
        static final String MEDIA_CHANNEL = "edgeMedia.channel";
        static final String MEDIA_PLAYER_NAME = "edgeMedia.playerName";
        static final String MEDIA_APP_VERSION = "edgeMedia.appVersion";

        private Configuration() {}
    }

    static final class Edge {
        static final String REQUEST_EVENT_ID = "requestEventId";
        static final String PAYLOAD = "payload";
        static final String SESSION_ID = "sessionId";
        static final int ERROR_CODE_400 = 400;
        static final String ERROR_TYPE_VA_EDGE_400 =
                "https://ns.adobe.com/aep/errors/va-edge-0400-400";

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

        // Event Data Key Constants - EventName
        static final class MediaEventName {
            static final String SESSION_START = "sessionstart";
            static final String SESSION_END = "sessionend";
            static final String PLAY = "play";
            static final String PAUSE = "pause";
            static final String COMPLETE = "complete";
            static final String BUFFER_START = "bufferstart";
            static final String BUFFER_COMPLETE = "buffercomplete";
            static final String SEEK_START = "seekstart";
            static final String SEEK_COMPLETE = "seekcomplete";
            static final String AD_START = "adstart";
            static final String AD_COMPLETE = "adcomplete";
            static final String AD_SKIP = "adskip";
            static final String ADBREAK_START = "adbreakstart";
            static final String ADBREAK_COMPLETE = "adbreakcomplete";
            static final String CHAPTER_START = "chapterstart";
            static final String CHAPTER_COMPLETE = "chaptercomplete";
            static final String CHAPTER_SKIP = "chapterskip";
            static final String BITRATE_CHANGE = "bitratechange";
            static final String ERROR = "error";
            static final String QOE_UPDATE = "qoeupdate";
            static final String PLAYHEAD_UPDATE = "playheadupdate";
            static final String STATE_START = "statestart";
            static final String STATE_END = "stateend";

            private MediaEventName() {}
        }
    }

    static final class ErrorSource {
        static final String PLAYER = "player";

        private ErrorSource() {}
    }

    static final class PingInterval {
        static final int REALTIME_TRACKING_MS = 10 * 1000; // 10 sec

        private PingInterval() {}
    }
}

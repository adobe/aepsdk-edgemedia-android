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
    static final String LOG_TAG = "Media";
    static final String EXTENSION_NAME = "com.adobe.edge.media";
    static final String FRIENDLY_NAME = "Media";

    private MediaInternalConstants() {}

    static final class Media {
        static final String EVENT_SOURCE_TRACKER_REQUEST =
                "com.adobe.eventsource.media.requesttracker";
        static final String EVENT_SOURCE_TRACK_MEDIA = "com.adobe.eventsource.media.trackmedia";
        static final String EVENT_NAME_SESSION_CREATED =
                "com.adobe.eventsource.media.sessioncreated";
        static final String EVENT_SOURCE_MEDIA_EDGE_SESSION = "media-analytics:new-session";
        static final String EVENT_SOURCE_EDGE_ERROR_RESPONSE =
                "com.adobe.eventSource.errorResponseContent";

        private Media() {}
    }

    static final class Configuration {
        static final String SHARED_STATE_NAME = "com.adobe.module.configuration";
        static final String MEDIA_CHANNEL = "media.channel";
        static final String MEDIA_PLAYER_NAME = "media.playerName";
        static final String MEDIA_APP_VERSION = "media.appVersion";

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

        static final class StandardMediaMetadata {
            static final String SHOW = "a.media.show";
            static final String SEASON = "a.media.season";
            static final String EPISODE = "a.media.episode";
            static final String ASSET_ID = "a.media.asset";
            static final String GENRE = "a.media.genre";
            static final String FIRST_AIR_DATE = "a.media.airDate";
            static final String FIRST_DIGITAL_DATE = "a.media.digitalDate";
            static final String RATING = "a.media.rating";
            static final String ORIGINATOR = "a.media.originator";
            static final String NETWORK = "a.media.network";
            static final String SHOW_TYPE = "a.media.type";
            static final String AD_LOAD = "a.media.adLoad";
            static final String MVPD = "a.media.pass.mvpd";
            static final String AUTH = "a.media.pass.auth";
            static final String DAY_PART = "a.media.dayPart";
            static final String FEED = "a.media.feed";
            static final String STREAM_FORMAT = "a.media.format";
            static final String ARTIST = "a.media.artist";
            static final String ALBUM = "a.media.album";
            static final String LABEL = "a.media.label";
            static final String AUTHOR = "a.media.author";
            static final String STATION = "a.media.station";
            static final String PUBLISHER = "a.media.publisher";

            private StandardMediaMetadata() {}
        }

        static final class StandardAdMetadata {
            static final String ADVERTISER = "a.media.ad.advertiser";
            static final String CAMPAIGN_ID = "a.media.ad.campaign";
            static final String CREATIVE_ID = "a.media.ad.creative";
            static final String PLACEMENT_ID = "a.media.ad.placement";
            static final String SITE_ID = "a.media.ad.site";
            static final String CREATIVE_URL = "a.media.ad.creativeURL";

            private StandardAdMetadata() {}
        }
    }
}

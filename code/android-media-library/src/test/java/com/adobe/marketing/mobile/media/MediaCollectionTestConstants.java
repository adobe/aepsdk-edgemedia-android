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

package com.adobe.marketing.mobile.media;

class MediaCollectionTestConstants {
    static final class EventType {
        private EventType() {}

        static final String SESSION_START = "sessionStart";
        static final String SESSION_COMPLETE = "sessionComplete";
        static final String SESSION_END = "sessionEnd";

        static final String ADBREAK_START = "adBreakStart";
        static final String ADBREAK_COMPLETE = "adBreakComplete";

        static final String AD_START = "adStart";
        static final String AD_COMPLETE = "adComplete";
        static final String AD_SKIP = "adSkip";

        static final String CHAPTER_START = "chapterStart";
        static final String CHAPTER_COMPLETE = "chapterComplete";
        static final String CHAPTER_SKIP = "chapterSkip";

        static final String PLAY = "play";
        static final String PING = "ping";
        static final String BUFFER_START = "bufferStart";
        static final String PAUSE_START = "pauseStart";

        static final String BITRATE_CHANGE = "bitrateChange";
        static final String ERROR = "error";

        static final String STATE_START = "stateStart";
        static final String STATE_END = "stateEnd";
    }

    static final class Session {
        private Session() {}

        static final ParamTypeMapping APP_INSTALLATION_ID =
                new ParamTypeMapping("appInstallationId", ParamTypeMapping.Type.STRING);

        static final ParamTypeMapping ANALYTICS_TRACKING_SERVER =
                new ParamTypeMapping("analytics.trackingServer", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping ANALYTICS_RSID =
                new ParamTypeMapping("analytics.reportSuite", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping ANALYTICS_SSL =
                new ParamTypeMapping("analytics.enableSSL", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping ANALYTICS_VISITOR_ID =
                new ParamTypeMapping("analytics.visitorId", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping ANALYTICS_AID =
                new ParamTypeMapping("analytics.aid", ParamTypeMapping.Type.STRING);

        static final ParamTypeMapping VISITOR_MCORG_ID =
                new ParamTypeMapping("visitor.marketingCloudOrgId", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping VISITOR_MCUSER_ID =
                new ParamTypeMapping("visitor.marketingCloudUserId", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping VISITOR_AAM_LOC_HINT =
                new ParamTypeMapping("visitor.aamLocationHint", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping VISITOR_CUSTOMER_IDS =
                new ParamTypeMapping("visitor.customerIDs", ParamTypeMapping.Type.MAP);

        static final ParamTypeMapping VISITOR_CUSTOMER_KEY_ID =
                new ParamTypeMapping("id", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping VISITOR_CUSTOMER_KEY_AUTHSTATE =
                new ParamTypeMapping("authState", ParamTypeMapping.Type.INTEGER);

        static final ParamTypeMapping MEDIA_CHANNEL =
                new ParamTypeMapping("media.channel", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping MEDIA_PLAYER_NAME =
                new ParamTypeMapping("media.playerName", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping SDK_VERSION =
                new ParamTypeMapping("media.sdkVersion", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping MEDIA_VERSION =
                new ParamTypeMapping("media.version", ParamTypeMapping.Type.STRING);
        static String KEY_EVENT_TS = "key_eventts";
    }

    static final class Media {
        private Media() {}

        static final ParamTypeMapping ID =
                new ParamTypeMapping("media.id", ParamTypeMapping.Type.STRING);

        static final ParamTypeMapping NAME =
                new ParamTypeMapping("media.name", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping LENGTH =
                new ParamTypeMapping("media.length", ParamTypeMapping.Type.DOUBLE);
        static final ParamTypeMapping CONTENT_TYPE =
                new ParamTypeMapping("media.contentType", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping STREAM_TYPE =
                new ParamTypeMapping("media.streamType", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping PLAYER_NAME =
                new ParamTypeMapping("media.playerName", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping RESUME =
                new ParamTypeMapping("media.resume", ParamTypeMapping.Type.BOOLEAN);
        static final ParamTypeMapping DOWNLOADED =
                new ParamTypeMapping("media.downloaded", ParamTypeMapping.Type.BOOLEAN);

        static final ParamTypeMapping CHANNEL =
                new ParamTypeMapping("media.channel", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping PUBLISHER =
                new ParamTypeMapping("media.publisher", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping SDK_VERSION =
                new ParamTypeMapping("media.sdkVersion", ParamTypeMapping.Type.STRING);
    }

    static final class StandardMediaMetadata {
        private StandardMediaMetadata() {}

        static final ParamTypeMapping SHOW =
                new ParamTypeMapping("media.show", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping SEASON =
                new ParamTypeMapping("media.season", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping EPISODE =
                new ParamTypeMapping("media.episode", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping ASSET_ID =
                new ParamTypeMapping("media.assetId", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping GENRE =
                new ParamTypeMapping("media.genre", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping FIRST_AIR_DATE =
                new ParamTypeMapping("media.firstAirDate", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping FIRST_DIGITAL_DATE =
                new ParamTypeMapping("media.firstDigitalDate", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping RATING =
                new ParamTypeMapping("media.rating", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping ORIGINATOR =
                new ParamTypeMapping("media.originator", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping NETWORK =
                new ParamTypeMapping("media.network", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping SHOW_TYPE =
                new ParamTypeMapping("media.showType", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping AD_LOAD =
                new ParamTypeMapping("media.adLoad", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping MVPD =
                new ParamTypeMapping("media.pass.mvpd", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping AUTH =
                new ParamTypeMapping("media.pass.auth", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping DAY_PART =
                new ParamTypeMapping("media.dayPart", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping FEED =
                new ParamTypeMapping("media.feed", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping STREAM_FORMAT =
                new ParamTypeMapping("media.streamFormat", ParamTypeMapping.Type.STRING);

        static final ParamTypeMapping ARTIST =
                new ParamTypeMapping("media.artist", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping ALBUM =
                new ParamTypeMapping("media.album", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping LABEL =
                new ParamTypeMapping("media.label", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping AUTHOR =
                new ParamTypeMapping("media.author", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping STATION =
                new ParamTypeMapping("media.station", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping PUBLISHER =
                new ParamTypeMapping("media.publisher", ParamTypeMapping.Type.STRING);
    }

    static final class AdBreak {
        private AdBreak() {}

        static final ParamTypeMapping POD_FRIENDLY_NAME =
                new ParamTypeMapping("media.ad.podFriendlyName", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping POD_INDEX =
                new ParamTypeMapping("media.ad.podIndex", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping POD_SECOND =
                new ParamTypeMapping("media.ad.podSecond", ParamTypeMapping.Type.STRING);
    }

    static final class Ad {
        private Ad() {}

        static final ParamTypeMapping NAME =
                new ParamTypeMapping("media.ad.name", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping ID =
                new ParamTypeMapping("media.ad.id", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping LENGTH =
                new ParamTypeMapping("media.ad.length", ParamTypeMapping.Type.DOUBLE);
        static final ParamTypeMapping POD_POSITION =
                new ParamTypeMapping("media.ad.podPosition", ParamTypeMapping.Type.INTEGER);
        static final ParamTypeMapping PLAYER_NAME =
                new ParamTypeMapping("media.ad.playerName", ParamTypeMapping.Type.STRING);
    }

    static final class StandardAdMetadata {
        private StandardAdMetadata() {}

        static final ParamTypeMapping ADVERTISER =
                new ParamTypeMapping("media.ad.advertiser", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping CAMPAIGN_ID =
                new ParamTypeMapping("media.ad.campaignId", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping CREATIVE_ID =
                new ParamTypeMapping("media.ad.creativeId", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping SITE_ID =
                new ParamTypeMapping("media.ad.siteId", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping CREATIVE_URL =
                new ParamTypeMapping("media.ad.creativeURL", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping PLACEMENT_ID =
                new ParamTypeMapping("media.ad.placementId", ParamTypeMapping.Type.STRING);
    }

    static final class Chapter {
        private Chapter() {}

        static final ParamTypeMapping FRIENDLY_NAME =
                new ParamTypeMapping("media.chapter.friendlyName", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping LENGTH =
                new ParamTypeMapping("media.chapter.length", ParamTypeMapping.Type.DOUBLE);
        static final ParamTypeMapping OFFSET =
                new ParamTypeMapping("media.chapter.offset", ParamTypeMapping.Type.DOUBLE);
        static final ParamTypeMapping INDEX =
                new ParamTypeMapping("media.chapter.index", ParamTypeMapping.Type.INTEGER);
    }

    static final class QoE {
        private QoE() {}

        static final ParamTypeMapping BITRATE =
                new ParamTypeMapping("media.qoe.bitrate", ParamTypeMapping.Type.DOUBLE);
        static final ParamTypeMapping DROPPED_FRAMES =
                new ParamTypeMapping("media.qoe.droppedFrames", ParamTypeMapping.Type.DOUBLE);
        static final ParamTypeMapping FPS =
                new ParamTypeMapping("media.qoe.framesPerSecond", ParamTypeMapping.Type.DOUBLE);
        static final ParamTypeMapping STARTUP_TIME =
                new ParamTypeMapping("media.qoe.timeToStart", ParamTypeMapping.Type.DOUBLE);

        static final ParamTypeMapping ERROR_ID =
                new ParamTypeMapping("media.qoe.errorID", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping ERROR_SOURCE =
                new ParamTypeMapping("media.qoe.errorSource", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping ERROR_SOURCE_PLAYER =
                new ParamTypeMapping("player", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping ERROR_SOURCE_EXTERNAL =
                new ParamTypeMapping("external", ParamTypeMapping.Type.STRING);
    }

    static final class PlayerTime {
        private PlayerTime() {}

        static final ParamTypeMapping PLAYHEAD =
                new ParamTypeMapping("playhead", ParamTypeMapping.Type.DOUBLE);
        static final ParamTypeMapping TS = new ParamTypeMapping("ts", ParamTypeMapping.Type.LONG);
    }

    static final class Report {
        private Report() {}

        static final ParamTypeMapping EVENT_TYPE =
                new ParamTypeMapping("eventType", ParamTypeMapping.Type.STRING);
        static final ParamTypeMapping PARAMS =
                new ParamTypeMapping("params", ParamTypeMapping.Type.MAP);
        static final ParamTypeMapping QoE =
                new ParamTypeMapping("qoeData", ParamTypeMapping.Type.MAP);
        static final ParamTypeMapping CUSTOM_METADATA =
                new ParamTypeMapping("customMetadata", ParamTypeMapping.Type.MAP);
        static final ParamTypeMapping PLAYER_TIME =
                new ParamTypeMapping("playerTime", ParamTypeMapping.Type.MAP);
    }

    static final class State {
        private State() {}

        static final ParamTypeMapping STATE_NAME =
                new ParamTypeMapping("media.state.name", ParamTypeMapping.Type.STRING);
    }
}

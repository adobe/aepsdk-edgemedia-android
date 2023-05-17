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

package com.adobe.marketing.mobile.edge.media;

public class MediaConstants {

    private MediaConstants() {}
    /**
     * These constant strings define the stream type of the main content that is currently tracked.
     */
    public static final class StreamType {
        public static final String VOD = "vod";
        public static final String LIVE = "live";
        public static final String LINEAR = "linear";
        public static final String PODCAST = "podcast";
        public static final String AUDIOBOOK = "audiobook";
        public static final String AOD = "aod";

        private StreamType() {}
    }

    /** These constant strings define standard ad metadata keys. */
    public static final class AdMetadataKeys {
        public static final String ADVERTISER = "a.media.ad.advertiser";
        public static final String CAMPAIGN_ID = "a.media.ad.campaign";
        public static final String CREATIVE_ID = "a.media.ad.creative";
        public static final String CREATIVE_URL = "a.media.ad.creativeURL";
        public static final String PLACEMENT_ID = "a.media.ad.placement";
        public static final String SITE_ID = "a.media.ad.site";

        private AdMetadataKeys() {}
    }

    /** These constant strings define standard audio metadata keys. */
    public static final class AudioMetadataKeys {
        public static final String ALBUM = "a.media.album";
        public static final String ARTIST = "a.media.artist";
        public static final String AUTHOR = "a.media.author";
        public static final String LABEL = "a.media.label";
        public static final String PUBLISHER = "a.media.publisher";
        public static final String STATION = "a.media.station";

        private AudioMetadataKeys() {}
    }

    /** These constant strings define standard video metadata keys. */
    public static final class VideoMetadataKeys {
        public static final String AD_LOAD = "a.media.adLoad";
        public static final String ASSET_ID = "a.media.asset";
        public static final String AUTHORIZED = "a.media.pass.auth";
        public static final String DAY_PART = "a.media.dayPart";
        public static final String EPISODE = "a.media.episode";
        public static final String FEED = "a.media.feed";
        public static final String FIRST_AIR_DATE = "a.media.airDate";
        public static final String FIRST_DIGITAL_DATE = "a.media.digitalDate";
        public static final String GENRE = "a.media.genre";
        public static final String MVPD = "a.media.pass.mvpd";
        public static final String NETWORK = "a.media.network";
        public static final String ORIGINATOR = "a.media.originator";
        public static final String RATING = "a.media.rating";
        public static final String SEASON = "a.media.season";
        public static final String SHOW = "a.media.show";
        public static final String SHOW_TYPE = "a.media.type";
        public static final String STREAM_FORMAT = "a.media.format";

        private VideoMetadataKeys() {}
    }
    /** These constant strings define standard player states. */
    public static final class PlayerState {
        public static final String CLOSED_CAPTION = "closeCaption";
        public static final String FULLSCREEN = "fullScreen";
        public static final String IN_FOCUS = "inFocus";
        public static final String MUTE = "mute";
        public static final String PICTURE_IN_PICTURE = "pictureInPicture";

        private PlayerState() {}
    }

    /** These constant strings define configuration for tracker. */
    public static final class Config {
        public static final String CHANNEL = "config.channel";
        public static final String AD_PING_INTERVAL = "config.adpinginterval";
        public static final String MAIN_PING_INTERVAL = "config.mainpinginterval";

        private Config() {}
    }

    /** These constant strings define video/ad info keys used for MediaObject. */
    public static final class MediaObjectKey {

        /**
         * Constant defining explicit media resumed property. Set this to true on MediaObject if
         * resuming a previously closed session.
         */
        public static final String RESUMED = "media.resumed";

        /**
         * Constant defining the amount of time that MediaHeartbeat will wait between trackPlay and
         * trackAdStart before tracking the video start. If set, should be a Long value else it
         * would be ignored.
         */
        public static final String PREROLL_TRACKING_WAITING_TIME = "media.prerollwaitingtime";

        /**
         * Constant defining granular ad tracking property. Set this to true on MediaObject to
         * enable granular Ad tracking (1 second) pings.
         */
        public static final String GRANULAR_AD_TRACKING = "media.granularadtracking";

        private MediaObjectKey() {}
    }
}

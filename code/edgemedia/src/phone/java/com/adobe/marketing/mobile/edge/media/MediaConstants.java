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

    public static final class Config {
        public static final String CHANNEL = "config.channel";
        public static final String DOWNLOADED_CONTENT = "config.downloadedcontent";

        private Config() {}
    }
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
        public static final String ADVERTISER = "advertiser";
        public static final String CAMPAIGN_ID = "campaignID";
        public static final String CREATIVE_ID = "creativeID";
        public static final String CREATIVE_URL = "creativeURL";
        public static final String PLACEMENT_ID = "placementID";
        public static final String SITE_ID = "siteID";

        private AdMetadataKeys() {}
    }

    /** These constant strings define standard audio metadata keys. */
    public static final class AudioMetadataKeys {
        public static final String ALBUM = "album";
        public static final String ARTIST = "artist";
        public static final String AUTHOR = "author";
        public static final String LABEL = "label";
        public static final String PUBLISHER = "publisher";
        public static final String STATION = "station";

        private AudioMetadataKeys() {}
    }

    /** These constant strings define standard video metadata keys. */
    public static final class VideoMetadataKeys {
        public static final String AD_LOAD = "adLoad";
        public static final String ASSET_ID = "assetID";
        public static final String AUTHORIZED = "isAuthenticated";
        public static final String DAY_PART = "dayPart";
        public static final String EPISODE = "episode";
        public static final String FEED = "feed";
        public static final String FIRST_AIR_DATE = "firstAirDate";
        public static final String FIRST_DIGITAL_DATE = "firstDigitalDate";
        public static final String GENRE = "genre";
        public static final String MVPD = "mvpd";
        public static final String NETWORK = "network";
        public static final String ORIGINATOR = "originator";
        public static final String SEASON = "season";
        public static final String SHOW = "show";
        public static final String SHOW_TYPE = "showType";
        public static final String STREAM_FORMAT = "streamFormat";
        public static final String RATING = "rating";

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

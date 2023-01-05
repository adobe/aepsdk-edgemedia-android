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

import com.adobe.marketing.mobile.Media;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.util.DataReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

enum MediaType {
    Video,
    Audio
}

public class MediaObject {
    private static final String LOG_TAG = "MediaObject";

    public static HashMap<String, Object> createMediaInfo(
            final String id,
            final String name,
            final String streamType,
            final Media.MediaType mediaType,
            final double length) {
        MediaType mType = (mediaType == Media.MediaType.Video) ? MediaType.Video : MediaType.Audio;

        MediaInfo mediaInfo = MediaInfo.create(id, name, streamType, mType, length);

        if (mediaInfo == null) {
            Log.error(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "createTracker - Error creating media object");
            return new HashMap<>();
        }

        return mediaInfo.toObjectMap();
    }

    public static HashMap<String, Object> createAdBreakInfo(
            final String name, final long position, final double startTime) {
        AdBreakInfo adBreakInfo = AdBreakInfo.create(name, position, startTime);

        if (adBreakInfo == null) {
            Log.error(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "createAdBreakInfo - Error creating adBreak object");
            return new HashMap<>();
        }

        return adBreakInfo.toObjectMap();
    }

    public static HashMap<String, Object> createAdInfo(
            final String name, final String id, final long position, final double length) {
        AdInfo adInfo = AdInfo.create(id, name, position, length);

        if (adInfo == null) {
            Log.error(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "createAdInfo - Error creating ad object");
            return new HashMap<>();
        }

        return adInfo.toObjectMap();
    }

    public static HashMap<String, Object> createChapterInfo(
            final String name, final long position, final double startTime, final double length) {
        ChapterInfo chapterInfo = ChapterInfo.create(name, position, startTime, length);

        if (chapterInfo == null) {
            Log.error(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "createChapterInfo - Error creating chapter object");
            return new HashMap<>();
        }

        return chapterInfo.toObjectMap();
    }

    public static HashMap<String, Object> createQoEInfo(
            final double bitrate,
            final double droppedFrames,
            final double fps,
            final double startUpTime) {
        QoEInfo qoeInfo = QoEInfo.create(bitrate, droppedFrames, fps, startUpTime);

        if (qoeInfo == null) {
            Log.error(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "createQoEInfo - Error creating qoe object");
            return new HashMap<>();
        }

        return qoeInfo.toObjectMap();
    }

    public static HashMap<String, Object> createStateInfo(final String stateName) {
        StateInfo stateInfo = StateInfo.create(stateName);

        if (stateInfo == null) {
            Log.error(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "createStateInfo - Error creating state object");
            return new HashMap<>();
        }

        return stateInfo.toObjectMap();
    }

    public static boolean isValidMediaInfo(Map<String, Object> mediaInfo) {
        return MediaInfo.fromObjectMap(mediaInfo) != null;
    }
}

class MediaInfo {
    private static final String LOG_TAG = "MediaInfo";
    private static final String MEDIATYPEVIDEO = "video";
    private static final String MEDIATYPEAUDIO = "audio";
    private static final long DEFAULTPREROLLWAITTIME = 250;
    private static final boolean DEFAULT_GRANULAR_AD_TRACKING_ENABLED =
            false; // Default value, if ad tracking ping should be send every 1 sec.

    private final String id;
    private final String name;
    private final String streamType;
    private final MediaType mediaType;
    private final double length;
    private final boolean resumed;
    private final long prerollWaitTime;
    private final boolean isGranularAdTrackingEnabled;

    private MediaInfo(
            final String id,
            final String name,
            final String streamType,
            final MediaType mediaType,
            final double length,
            final boolean resumed,
            final long prerollWaitTime,
            final boolean isGranularAdTrackingEnabled) {
        this.id = id;
        this.name = name;
        this.streamType = streamType;
        this.mediaType = mediaType;
        this.length = length;
        this.resumed = resumed;
        this.prerollWaitTime = prerollWaitTime;
        this.isGranularAdTrackingEnabled = isGranularAdTrackingEnabled;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStreamType() {
        return streamType;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public String getMediaTypeString() {
        return mediaType == MediaType.Video ? MediaInfo.MEDIATYPEVIDEO : MediaInfo.MEDIATYPEAUDIO;
    }

    public double getLength() {
        return length;
    }

    public boolean isResumed() {
        return resumed;
    }

    public long getPrerollWaitTime() {
        return prerollWaitTime;
    }

    boolean isGranularAdTrackingEnabled() {
        return isGranularAdTrackingEnabled;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof MediaInfo)) {
            return false;
        }

        MediaInfo other = (MediaInfo) o;

        return (id.equals(other.id)
                && name.equals(other.name)
                && streamType.equals(other.streamType)
                && mediaType.equals(other.mediaType)
                && length == other.length
                && resumed == other.resumed
                && isGranularAdTrackingEnabled == other.isGranularAdTrackingEnabled);
    }

    private String getMediaTypeAsString() {
        return mediaType == MediaType.Video ? MEDIATYPEVIDEO : MEDIATYPEAUDIO;
    }

    public HashMap<String, Object> toObjectMap() {
        HashMap<String, Object> responseMap = new HashMap<>();
        responseMap.put(MediaInternalConstants.EventDataKeys.MediaInfo.ID, id);
        responseMap.put(MediaInternalConstants.EventDataKeys.MediaInfo.NAME, name);
        responseMap.put(MediaInternalConstants.EventDataKeys.MediaInfo.STREAM_TYPE, streamType);
        responseMap.put(
                MediaInternalConstants.EventDataKeys.MediaInfo.MEDIA_TYPE, getMediaTypeAsString());
        responseMap.put(MediaInternalConstants.EventDataKeys.MediaInfo.LENGTH, length);
        responseMap.put(MediaInternalConstants.EventDataKeys.MediaInfo.RESUMED, resumed);
        responseMap.put(
                MediaInternalConstants.EventDataKeys.MediaInfo.PREROLL_TRACKING_WAITING_TIME,
                prerollWaitTime);

        return responseMap;
    }

    @Override
    public String toString() {
        StringBuilder responseStringBuilder = new StringBuilder();
        responseStringBuilder
                .append("{")
                .append(" class: \"MediaInfo\",")
                .append(" id: ")
                .append("\"")
                .append(id)
                .append("\"")
                .append(" name: ")
                .append("\"")
                .append(name)
                .append("\"")
                .append(" length: ")
                .append(length)
                .append(" streamType: ")
                .append("\"")
                .append(streamType)
                .append("\"")
                .append(" mediaType: ")
                .append("\"")
                .append(getMediaTypeAsString())
                .append("\"")
                .append(" resumed: ")
                .append(resumed)
                .append(" prerollWaitTime: ")
                .append(prerollWaitTime)
                .append("}");

        return responseStringBuilder.toString();
    }

    public static MediaInfo fromObjectMap(final Map<String, Object> info) {
        if (info == null) {
            return null;
        }

        String name =
                DataReader.optString(
                        info, MediaInternalConstants.EventDataKeys.MediaInfo.NAME, null);

        String id =
                DataReader.optString(info, MediaInternalConstants.EventDataKeys.MediaInfo.ID, null);

        String streamType =
                DataReader.optString(
                        info, MediaInternalConstants.EventDataKeys.MediaInfo.STREAM_TYPE, null);

        String mediaTypeVal =
                DataReader.optString(
                        info, MediaInternalConstants.EventDataKeys.MediaInfo.MEDIA_TYPE, null);

        if (mediaTypeVal == null) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "fromObjectMap - Error parsing MediaInfo, invalid media type");
            return null;
        }

        MediaType mediaType;

        if (mediaTypeVal.equalsIgnoreCase(MEDIATYPEAUDIO)) {
            mediaType = MediaType.Audio;
        } else if (mediaTypeVal.equalsIgnoreCase(MEDIATYPEVIDEO)) {
            mediaType = MediaType.Video;
        } else {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "fromObjectMap - Error parsing MediaInfo, invalid media type");
            return null;
        }

        double length =
                DataReader.optDouble(
                        info, MediaInternalConstants.EventDataKeys.MediaInfo.LENGTH, -1);

        boolean resumed =
                DataReader.optBoolean(
                        info, MediaInternalConstants.EventDataKeys.MediaInfo.RESUMED, false);

        long prerollWaitTimeVal =
                DataReader.optLong(
                        info,
                        MediaInternalConstants.EventDataKeys.MediaInfo
                                .PREROLL_TRACKING_WAITING_TIME,
                        DEFAULTPREROLLWAITTIME);

        final boolean isGranularAdTrackingEnabled =
                DataReader.optBoolean(
                        info,
                        MediaInternalConstants.EventDataKeys.MediaInfo.GRANULAR_AD_TRACKING,
                        false);

        return create(
                id,
                name,
                streamType,
                mediaType,
                length,
                resumed,
                prerollWaitTimeVal,
                isGranularAdTrackingEnabled);
    }

    public static MediaInfo create(
            final String id,
            final String name,
            final String streamType,
            final MediaType mediaType,
            final double length) {
        return create(
                id,
                name,
                streamType,
                mediaType,
                length,
                false,
                DEFAULTPREROLLWAITTIME,
                DEFAULT_GRANULAR_AD_TRACKING_ENABLED);
    }

    public static MediaInfo create(
            final String id,
            final String name,
            final String streamType,
            final MediaType mediaType,
            final double length,
            final boolean resumed) {
        return create(
                id,
                name,
                streamType,
                mediaType,
                length,
                resumed,
                DEFAULTPREROLLWAITTIME,
                DEFAULT_GRANULAR_AD_TRACKING_ENABLED);
    }

    public static MediaInfo create(
            final String id,
            final String name,
            final String streamType,
            final MediaType mediaType,
            final double length,
            final boolean resumed,
            final long prerollWaitTime,
            final boolean isGranularAdTrackingEnabled) {
        if (id == null || id.length() == 0) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "create - Error creating MediaInfo, id must not be empty");
            return null;
        }

        if (name == null || name.length() == 0) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "create - Error creating MediaInfo, name must not be empty");
            return null;
        }

        if (streamType == null || streamType.length() == 0) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "create - Error creating MediaInfo, stream type must not be empty");
            return null;
        }

        if (length < 0) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "create - Error creating MediaInfo, length must not be less than zero");
            return null;
        }

        return new MediaInfo(
                id,
                name,
                streamType,
                mediaType,
                length,
                resumed,
                prerollWaitTime,
                isGranularAdTrackingEnabled);
    }
}

class AdInfo {
    private static final String LOG_TAG = "AdInfo";
    private final String id;
    private final String name;
    private final long position;
    private final double length;

    private AdInfo(final String id, final String name, final long position, final double length) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.length = length;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getPosition() {
        return position;
    }

    public double getLength() {
        return length;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof AdInfo)) {
            return false;
        }

        AdInfo other = (AdInfo) o;

        return (id.equals(other.id)
                && name.equals(other.name)
                && position == other.position
                && length == other.length);
    }

    public HashMap<String, Object> toObjectMap() {
        HashMap<String, Object> responseMap = new HashMap<>();
        responseMap.put(MediaInternalConstants.EventDataKeys.AdInfo.ID, id);
        responseMap.put(MediaInternalConstants.EventDataKeys.AdInfo.NAME, name);
        responseMap.put(MediaInternalConstants.EventDataKeys.AdInfo.POSITION, position);
        responseMap.put(MediaInternalConstants.EventDataKeys.AdInfo.LENGTH, length);

        return responseMap;
    }

    @Override
    public String toString() {
        StringBuilder responseStringBuilder = new StringBuilder();
        responseStringBuilder
                .append("{")
                .append(" class: \"AdInfo\",")
                .append(" id: ")
                .append("\"")
                .append(id)
                .append("\"")
                .append(" name: ")
                .append("\"")
                .append(name)
                .append("\"")
                .append(" position: ")
                .append(position)
                .append(" length: ")
                .append(length)
                .append("}");

        return responseStringBuilder.toString();
    }

    public static AdInfo create(
            final String id, final String name, final long position, final double length) {

        if (id == null || id.length() == 0) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "create - Error creating AdInfo, id must not be empty");
            return null;
        }

        if (name == null || name.length() == 0) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "create - Error creating AdInfo, name must not be empty");
            return null;
        }

        if (position < 1) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "create - Error creating AdInfo, position must be greater than zero");
            return null;
        }

        if (length < 0) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "create - Error creating AdInfo, length cannot be less than zero");
            return null;
        }

        return new AdInfo(id, name, position, length);
    }

    public static AdInfo fromObjectMap(final Map<String, Object> info) {
        if (info == null) {
            return null;
        }

        String id =
                DataReader.optString(info, MediaInternalConstants.EventDataKeys.AdInfo.ID, null);

        String name =
                DataReader.optString(info, MediaInternalConstants.EventDataKeys.AdInfo.NAME, null);

        long position =
                DataReader.optLong(info, MediaInternalConstants.EventDataKeys.AdInfo.POSITION, -1);

        double length =
                DataReader.optDouble(info, MediaInternalConstants.EventDataKeys.AdInfo.LENGTH, -1);

        return create(id, name, position, length);
    }
}

class AdBreakInfo {
    private static final String LOG_TAG = "AdBreakInfo";
    private final String name;
    private final long position;
    private final double startTime;

    private AdBreakInfo(final String name, final long position, final double startTime) {
        this.name = name;
        this.position = position;
        this.startTime = startTime;
    }

    public String getName() {
        return name;
    }

    public long getPosition() {
        return position;
    }

    public double getStartTime() {
        return startTime;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof AdBreakInfo)) {
            return false;
        }

        AdBreakInfo other = (AdBreakInfo) o;

        return (name.equals(other.name)
                && position == other.position
                && startTime == other.startTime);
    }

    public HashMap<String, Object> toObjectMap() {
        HashMap<String, Object> responseMap = new HashMap<>();
        responseMap.put(MediaInternalConstants.EventDataKeys.AdBreakInfo.NAME, name);
        responseMap.put(MediaInternalConstants.EventDataKeys.AdBreakInfo.POSITION, position);
        responseMap.put(MediaInternalConstants.EventDataKeys.AdBreakInfo.START_TIME, startTime);

        return responseMap;
    }

    @Override
    public String toString() {
        StringBuilder responseStringBuilder = new StringBuilder();
        responseStringBuilder
                .append("{")
                .append(" class: \"AdBreakInfo\",")
                .append(" name: ")
                .append("\"")
                .append(name)
                .append("\"")
                .append(" position: ")
                .append(position)
                .append(" startTime: ")
                .append(startTime)
                .append("}");

        return responseStringBuilder.toString();
    }

    public static AdBreakInfo create(
            final String name, final long position, final double startTime) {
        if (name == null || name.length() == 0) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "create - Error creating AdBreakInfo, name must not be empty");
            return null;
        }

        if (position < 1) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "create - Error creating AdBreakInfo, position must be greater than zero");
            return null;
        }

        if (startTime < 0) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "create - Error creating AdBreakInfo, start time must not be less than zero");
            return null;
        }

        return new AdBreakInfo(name, position, startTime);
    }

    public static AdBreakInfo fromObjectMap(final Map<String, Object> info) {
        if (info == null) {
            return null;
        }

        String name =
                DataReader.optString(
                        info, MediaInternalConstants.EventDataKeys.AdBreakInfo.NAME, null);

        long position =
                DataReader.optLong(
                        info, MediaInternalConstants.EventDataKeys.AdBreakInfo.POSITION, -1);

        double startTime =
                DataReader.optDouble(
                        info, MediaInternalConstants.EventDataKeys.AdBreakInfo.START_TIME, -1);

        return create(name, position, startTime);
    }
}

class ChapterInfo {
    private static final String LOG_TAG = "ChapterInfo";
    private final String name;
    private final long position;
    private final double startTime;
    private final double length;

    private ChapterInfo(
            final String name, final long position, final double startTime, final double length) {
        this.name = name;
        this.position = position;
        this.startTime = startTime;
        this.length = length;
    }

    public String getName() {
        return name;
    }

    public long getPosition() {
        return position;
    }

    public double getStartTime() {
        return startTime;
    }

    public double getLength() {
        return length;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof ChapterInfo)) {
            return false;
        }

        ChapterInfo other = (ChapterInfo) o;

        return (name.equals(other.name)
                && position == other.position
                && startTime == other.startTime
                && length == other.length);
    }

    public HashMap<String, Object> toObjectMap() {
        HashMap<String, Object> responseMap = new HashMap<String, Object>();
        responseMap.put(MediaInternalConstants.EventDataKeys.ChapterInfo.NAME, name);
        responseMap.put(MediaInternalConstants.EventDataKeys.ChapterInfo.POSITION, position);
        responseMap.put(MediaInternalConstants.EventDataKeys.ChapterInfo.START_TIME, startTime);
        responseMap.put(MediaInternalConstants.EventDataKeys.ChapterInfo.LENGTH, length);

        return responseMap;
    }

    @Override
    public String toString() {
        StringBuilder responseStringBuilder = new StringBuilder();
        responseStringBuilder
                .append("{")
                .append(" class: \"ChapterInfo\",")
                .append(" name: ")
                .append("\"")
                .append(name)
                .append("\"")
                .append(" position: ")
                .append(position)
                .append(" startTime: ")
                .append(startTime)
                .append(" length: ")
                .append(length)
                .append("}");
        return responseStringBuilder.toString();
    }

    public static ChapterInfo create(
            final String name, final long position, final double startTime, final double length) {
        if (name == null || name.length() == 0) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "create - Error creating ChapterInfo, name must not be empty");
            return null;
        }

        if (position < 1) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "create - Error creating ChapterInfo, position must be greater than zero");
            return null;
        }

        if (startTime < 0) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "create - Error creating ChapterInfo, start time must not be less than zero");
            return null;
        }

        if (length < 0) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "create - Error creating ChapterInfo, length must not be less than zero");
            return null;
        }

        return new ChapterInfo(name, position, startTime, length);
    }

    public static ChapterInfo fromObjectMap(final Map<String, Object> info) {
        if (info == null) {
            return null;
        }

        String name =
                DataReader.optString(
                        info, MediaInternalConstants.EventDataKeys.ChapterInfo.NAME, null);

        long position =
                DataReader.optLong(
                        info, MediaInternalConstants.EventDataKeys.ChapterInfo.POSITION, -1);

        double startTime =
                DataReader.optDouble(
                        info, MediaInternalConstants.EventDataKeys.ChapterInfo.START_TIME, -1);

        double length =
                DataReader.optDouble(
                        info, MediaInternalConstants.EventDataKeys.ChapterInfo.LENGTH, -1);

        return create(name, position, startTime, length);
    }
}

class QoEInfo {
    private static final String LOG_TAG = "QoEInfo";
    private final double bitrate;
    private final double droppedFrames;
    private final double fps;
    private final double startupTime;

    private QoEInfo(
            final double bitrate,
            final double droppedFrames,
            final double fps,
            final double startupTime) {
        this.bitrate = bitrate;
        this.droppedFrames = droppedFrames;
        this.fps = fps;
        this.startupTime = startupTime;
    }

    public double getBitrate() {
        return bitrate;
    }

    public double getDroppedFrames() {
        return droppedFrames;
    }

    public double getFPS() {
        return fps;
    }

    public double getStartupTime() {
        return startupTime;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof QoEInfo)) {
            return false;
        }

        QoEInfo other = (QoEInfo) o;

        return (bitrate == other.bitrate
                && droppedFrames == other.droppedFrames
                && fps == other.fps
                && startupTime == other.startupTime);
    }

    public HashMap<String, Object> toObjectMap() {
        HashMap<String, Object> responseMap = new HashMap<>();
        responseMap.put(MediaInternalConstants.EventDataKeys.QoEInfo.BITRATE, bitrate);
        responseMap.put(MediaInternalConstants.EventDataKeys.QoEInfo.DROPPED_FRAMES, droppedFrames);
        responseMap.put(MediaInternalConstants.EventDataKeys.QoEInfo.FPS, fps);
        responseMap.put(MediaInternalConstants.EventDataKeys.QoEInfo.STARTUP_TIME, startupTime);

        return responseMap;
    }

    @Override
    public String toString() {
        StringBuilder responseStringBuilder = new StringBuilder();
        responseStringBuilder
                .append("{")
                .append(" class: \"QoEInfo\",")
                .append(" bitrate: ")
                .append(bitrate)
                .append(" droppedFrames: ")
                .append(droppedFrames)
                .append(" fps: ")
                .append(fps)
                .append(" startupTime: ")
                .append(startupTime)
                .append("}");

        return responseStringBuilder.toString();
    }

    public static QoEInfo create(
            final double bitrate,
            final double droppedFrames,
            final double fps,
            final double startupTime) {
        if (bitrate < 0) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "create - Error creating QoEInfo, bitrate must not be less than zero");
            return null;
        }

        if (droppedFrames < 0) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "create - Error creating QoEInfo, dropped frames must not be less than zero");
            return null;
        }

        if (fps < 0) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "create - Error creating QoEInfo, fps must not be less than zero");
            return null;
        }

        if (startupTime < 0) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "create - Error creating QoEInfo, startup time must not be less than zero");
            return null;
        }

        return new QoEInfo(bitrate, droppedFrames, fps, startupTime);
    }

    public static QoEInfo fromObjectMap(final Map<String, Object> info) {
        if (info == null) {
            return null;
        }

        double bitrate =
                DataReader.optDouble(
                        info, MediaInternalConstants.EventDataKeys.QoEInfo.BITRATE, -1);

        double droppedFrames =
                DataReader.optDouble(
                        info, MediaInternalConstants.EventDataKeys.QoEInfo.DROPPED_FRAMES, -1);

        double fps =
                DataReader.optDouble(info, MediaInternalConstants.EventDataKeys.QoEInfo.FPS, -1);

        double startupTime =
                DataReader.optDouble(
                        info, MediaInternalConstants.EventDataKeys.QoEInfo.STARTUP_TIME, -1);

        return create(bitrate, droppedFrames, fps, startupTime);
    }
}

class StateInfo {
    private static final String LOG_TAG = "StateInfo";
    private final String stateName;

    private StateInfo(final String stateName) {
        this.stateName = stateName;
    }

    public String getStateName() {
        return stateName;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof StateInfo)) {
            return false;
        }

        StateInfo other = (StateInfo) o;

        return (stateName.equals(other.stateName));
    }

    public HashMap<String, Object> toObjectMap() {
        HashMap<String, Object> responseMap = new HashMap<>();
        responseMap.put(MediaInternalConstants.EventDataKeys.StateInfo.STATE_NAME_KEY, stateName);

        return responseMap;
    }

    @Override
    public String toString() {
        StringBuilder responseStringBuilder = new StringBuilder();
        responseStringBuilder
                .append("{")
                .append(" class: \"StateInfo\",")
                .append(" stateName: ")
                .append("\"")
                .append(stateName)
                .append("\"")
                .append("}");

        return responseStringBuilder.toString();
    }

    public static StateInfo create(final String stateName) {
        if (stateName == null || stateName.length() == 0) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "create - Error parsing StateInfo, state name cannot be empty");
            return null;
        }

        Pattern pattern = Pattern.compile("^[a-zA-Z0-9_.]{1,64}$");
        Matcher matcher = pattern.matcher(stateName);

        if (!matcher.find()) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "create - Error creating StateInfo, state name cannot contain special"
                            + " characters. Only alphabets, digits, '_' and '.' are allowed.");
            return null;
        }

        return new StateInfo(stateName);
    }

    public static StateInfo fromObjectMap(final Map<String, Object> info) {
        if (info == null) {
            return null;
        }

        String stateName =
                DataReader.optString(
                        info, MediaInternalConstants.EventDataKeys.StateInfo.STATE_NAME_KEY, null);

        return create(stateName);
    }
}

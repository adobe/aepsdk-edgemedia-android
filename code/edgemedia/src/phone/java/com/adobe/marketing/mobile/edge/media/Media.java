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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.adobe.marketing.mobile.Extension;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.edge.media.internal.MediaExtension;
import com.adobe.marketing.mobile.edge.media.internal.MediaObject;
import com.adobe.marketing.mobile.edge.media.internal.MediaPublicTracker;
import java.util.HashMap;
import java.util.Map;

public class Media {
    private static final String EXTENSION_VERSION = "2.0.0";

    private Media() {}

    public static final Class<? extends Extension> EXTENSION = MediaExtension.class;

    /**
     * Creates a media tracker instance that tracks the playback session.
     *
     * @return a media tracker instance
     */
    public static @NonNull MediaTracker createTracker() {
        return createTracker((Map<String, Object>) null);
    }

    /**
     * Creates a media tracker instance based on the optional configuration to track the playback
     * session.
     *
     * @param config optional configuration
     * @return A media tracker instance based on configuration
     */
    public static @NonNull MediaTracker createTracker(@Nullable final Map<String, Object> config) {
        return MediaPublicTracker.create(config, MobileCore::dispatchEvent);
    }

    /**
     * Returns the version for the {@code Media} extension
     *
     * @return The version string
     */
    public static @NonNull String extensionVersion() {
        return EXTENSION_VERSION;
    }

    /**
     * Creates an instance of the video info object
     *
     * @param name The name of the media
     * @param mediaId The unique identifier for the media
     * @param length The length of the media in seconds
     * @param streamType The stream type as a string. Use the pre-defined constants for vod, live,
     *     and linear content
     * @param mediaType Mediatype
     * @return A MediaObject instance representing the media.
     */
    @NonNull public static HashMap<String, Object> createMediaObject(
            @NonNull final String name,
            @NonNull final String mediaId,
            final double length,
            @NonNull final String streamType,
            @NonNull final MediaType mediaType) {
        return MediaObject.createMediaInfo(mediaId, name, streamType, mediaType, length);
    }

    /**
     * Creates an instance of the AdBreak info object
     *
     * @param name The name of the ad break
     * @param position The position of the ad break in the content starting with 1
     * @param startTime The start time of the ad break relative to the main media
     * @return A MediaObject instance representing the AdBreak.
     */
    @NonNull public static HashMap<String, Object> createAdBreakObject(
            @NonNull final String name, final long position, final double startTime) {
        return MediaObject.createAdBreakInfo(name, position, startTime);
    }

    /**
     * Creates an instance of the Ad info object
     *
     * @param name The name of the ad
     * @param adId The unique id for the ad
     * @param position The start position of the ad
     * @param length The length of the ad in seconds
     * @return A MediaObject instance representing the Ad.
     */
    @NonNull public static HashMap<String, Object> createAdObject(
            @NonNull final String name,
            @NonNull final String adId,
            final long position,
            final double length) {
        return MediaObject.createAdInfo(name, adId, position, length);
    }

    /**
     * Creates an instance of the Chapter info object
     *
     * @param name The name of the chapter
     * @param position The position of the chapter
     * @param length The length of the chapter in seconds
     * @param startTime The start of the chapter relative to the main media
     * @return A MediaObject instance representing the Chapter.
     */
    @NonNull public static HashMap<String, Object> createChapterObject(
            @NonNull final String name,
            final long position,
            final double length,
            final double startTime) {
        return MediaObject.createChapterInfo(name, position, startTime, length);
    }

    /**
     * Creates an instance of the QoS info object
     *
     * @param bitrate The bitrate of media in bits per second
     * @param startupTime The start up time of media in seconds
     * @param fps The current frames per second information
     * @param droppedFrames The number of dropped frames so far
     * @return A MediaObject instance representing the QoSObject.
     */
    @NonNull public static HashMap<String, Object> createQoEObject(
            final long bitrate,
            final double startupTime,
            final double fps,
            final long droppedFrames) {
        return MediaObject.createQoEInfo(bitrate, droppedFrames, fps, startupTime);
    }

    /**
     * Creates an instance of the state info object
     *
     * @param stateName The bitrate of media in bits per second
     * @return A MediaObject instance representing the state.
     */
    @NonNull public static HashMap<String, Object> createStateObject(@NonNull final String stateName) {
        return MediaObject.createStateInfo(stateName);
    }

    /** These enumeration values define the type of media */
    public enum MediaType {
        /** Constant defining media type for Video */
        Video,

        /** Constant defining media type for Audio */
        Audio
    }

    /** These enumeration values define the type of a tracking event */
    public enum Event {
        /** Constant defining event type for AdBreak start */
        AdBreakStart,

        /** Constant defining event type for AdBreak complete */
        AdBreakComplete,

        /** Constant defining event type for Ad start */
        AdStart,

        /** Constant defining event type for Ad complete */
        AdComplete,

        /** Constant defining event type for Ad skip */
        AdSkip,

        /** Constant defining event type for Chapter start */
        ChapterStart,

        /** Constant defining event type for Chapter complete */
        ChapterComplete,

        /** Constant defining event type for Chapter skip */
        ChapterSkip,

        /** Constant defining event type for Seek start */
        SeekStart,

        /** Constant defining event type for Seek complete */
        SeekComplete,

        /** Constant defining event type for Buffer start */
        BufferStart,

        /** Constant defining event type for Buffer complete */
        BufferComplete,

        /** Constant defining event type for change in Bitrate */
        BitrateChange,

        /** Constant defining event type for State start */
        StateStart,

        /** Constant defining event type for State end */
        StateEnd
    }
}

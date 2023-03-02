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

import java.util.HashMap;
import java.util.Map;

class MediaCollectionHelper {

    static final Map<String, ParamTypeMapping> standardMediaMetadataMapping = new HashMap<>();
    static final Map<String, ParamTypeMapping> standardAdMetadataMapping = new HashMap<>();

    static {
        standardMediaMetadataMapping.put(
                MediaInternalConstants.EventDataKeys.StandardMediaMetadata.SHOW,
                MediaCollectionConstants.StandardMediaMetadata.SHOW);
        standardMediaMetadataMapping.put(
                MediaInternalConstants.EventDataKeys.StandardMediaMetadata.SEASON,
                MediaCollectionConstants.StandardMediaMetadata.SEASON);
        standardMediaMetadataMapping.put(
                MediaInternalConstants.EventDataKeys.StandardMediaMetadata.EPISODE,
                MediaCollectionConstants.StandardMediaMetadata.EPISODE);
        standardMediaMetadataMapping.put(
                MediaInternalConstants.EventDataKeys.StandardMediaMetadata.ASSET_ID,
                MediaCollectionConstants.StandardMediaMetadata.ASSET_ID);
        standardMediaMetadataMapping.put(
                MediaInternalConstants.EventDataKeys.StandardMediaMetadata.GENRE,
                MediaCollectionConstants.StandardMediaMetadata.GENRE);
        standardMediaMetadataMapping.put(
                MediaInternalConstants.EventDataKeys.StandardMediaMetadata.FIRST_AIR_DATE,
                MediaCollectionConstants.StandardMediaMetadata.FIRST_AIR_DATE);
        standardMediaMetadataMapping.put(
                MediaInternalConstants.EventDataKeys.StandardMediaMetadata.FIRST_DIGITAL_DATE,
                MediaCollectionConstants.StandardMediaMetadata.FIRST_DIGITAL_DATE);
        standardMediaMetadataMapping.put(
                MediaInternalConstants.EventDataKeys.StandardMediaMetadata.RATING,
                MediaCollectionConstants.StandardMediaMetadata.RATING);
        standardMediaMetadataMapping.put(
                MediaInternalConstants.EventDataKeys.StandardMediaMetadata.ORIGINATOR,
                MediaCollectionConstants.StandardMediaMetadata.ORIGINATOR);
        standardMediaMetadataMapping.put(
                MediaInternalConstants.EventDataKeys.StandardMediaMetadata.NETWORK,
                MediaCollectionConstants.StandardMediaMetadata.NETWORK);
        standardMediaMetadataMapping.put(
                MediaInternalConstants.EventDataKeys.StandardMediaMetadata.SHOW_TYPE,
                MediaCollectionConstants.StandardMediaMetadata.SHOW_TYPE);
        standardMediaMetadataMapping.put(
                MediaInternalConstants.EventDataKeys.StandardMediaMetadata.AD_LOAD,
                MediaCollectionConstants.StandardMediaMetadata.AD_LOAD);
        standardMediaMetadataMapping.put(
                MediaInternalConstants.EventDataKeys.StandardMediaMetadata.MVPD,
                MediaCollectionConstants.StandardMediaMetadata.MVPD);
        standardMediaMetadataMapping.put(
                MediaInternalConstants.EventDataKeys.StandardMediaMetadata.AUTH,
                MediaCollectionConstants.StandardMediaMetadata.AUTH);
        standardMediaMetadataMapping.put(
                MediaInternalConstants.EventDataKeys.StandardMediaMetadata.DAY_PART,
                MediaCollectionConstants.StandardMediaMetadata.DAY_PART);
        standardMediaMetadataMapping.put(
                MediaInternalConstants.EventDataKeys.StandardMediaMetadata.FEED,
                MediaCollectionConstants.StandardMediaMetadata.FEED);
        standardMediaMetadataMapping.put(
                MediaInternalConstants.EventDataKeys.StandardMediaMetadata.STREAM_FORMAT,
                MediaCollectionConstants.StandardMediaMetadata.STREAM_FORMAT);
        standardMediaMetadataMapping.put(
                MediaInternalConstants.EventDataKeys.StandardMediaMetadata.ARTIST,
                MediaCollectionConstants.StandardMediaMetadata.ARTIST);
        standardMediaMetadataMapping.put(
                MediaInternalConstants.EventDataKeys.StandardMediaMetadata.ALBUM,
                MediaCollectionConstants.StandardMediaMetadata.ALBUM);
        standardMediaMetadataMapping.put(
                MediaInternalConstants.EventDataKeys.StandardMediaMetadata.LABEL,
                MediaCollectionConstants.StandardMediaMetadata.LABEL);
        standardMediaMetadataMapping.put(
                MediaInternalConstants.EventDataKeys.StandardMediaMetadata.AUTHOR,
                MediaCollectionConstants.StandardMediaMetadata.AUTHOR);
        standardMediaMetadataMapping.put(
                MediaInternalConstants.EventDataKeys.StandardMediaMetadata.STATION,
                MediaCollectionConstants.StandardMediaMetadata.STATION);
        standardMediaMetadataMapping.put(
                MediaInternalConstants.EventDataKeys.StandardMediaMetadata.PUBLISHER,
                MediaCollectionConstants.StandardMediaMetadata.PUBLISHER);

        standardAdMetadataMapping.put(
                MediaInternalConstants.EventDataKeys.StandardAdMetadata.ADVERTISER,
                MediaCollectionConstants.StandardAdMetadata.ADVERTISER);
        standardAdMetadataMapping.put(
                MediaInternalConstants.EventDataKeys.StandardAdMetadata.CAMPAIGN_ID,
                MediaCollectionConstants.StandardAdMetadata.CAMPAIGN_ID);
        standardAdMetadataMapping.put(
                MediaInternalConstants.EventDataKeys.StandardAdMetadata.CREATIVE_ID,
                MediaCollectionConstants.StandardAdMetadata.CREATIVE_ID);
        standardAdMetadataMapping.put(
                MediaInternalConstants.EventDataKeys.StandardAdMetadata.PLACEMENT_ID,
                MediaCollectionConstants.StandardAdMetadata.PLACEMENT_ID);
        standardAdMetadataMapping.put(
                MediaInternalConstants.EventDataKeys.StandardAdMetadata.SITE_ID,
                MediaCollectionConstants.StandardAdMetadata.SITE_ID);
        standardAdMetadataMapping.put(
                MediaInternalConstants.EventDataKeys.StandardAdMetadata.CREATIVE_URL,
                MediaCollectionConstants.StandardAdMetadata.CREATIVE_URL);
    }

    static boolean isStandardMetadata(final Map<String, ParamTypeMapping> dict, final String key) {
        return dict.containsKey(key);
    }

    static String getMediaCollectionKey(
            final Map<String, ParamTypeMapping> dict, final String key) {
        if (dict.containsKey(key)) {
            return dict.get(key).key;
        }

        return key;
    }

    static Map<String, Object> extractMediaParams(final MediaContext mediaContext) {
        Map<String, Object> retMap = new HashMap<>();

        MediaInfo mediaInfo = mediaContext.getMediaInfo();

        if (mediaInfo != null) {
            retMap.put(MediaCollectionConstants.Media.ID.key, mediaInfo.getId());
            retMap.put(MediaCollectionConstants.Media.NAME.key, mediaInfo.getName());
            retMap.put(MediaCollectionConstants.Media.LENGTH.key, mediaInfo.getLength());
            retMap.put(MediaCollectionConstants.Media.CONTENT_TYPE.key, mediaInfo.getStreamType());
            retMap.put(
                    MediaCollectionConstants.Media.STREAM_TYPE.key, mediaInfo.getMediaTypeString());
            retMap.put(MediaCollectionConstants.Media.RESUME.key, mediaInfo.isResumed());
        }

        Map<String, String> metadata = mediaContext.getMediaMetadata();

        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            if (isStandardMetadata(standardMediaMetadataMapping, entry.getKey())) {
                String newKey = getMediaCollectionKey(standardMediaMetadataMapping, entry.getKey());
                // Add string values for standard metadata
                retMap.put(newKey, entry.getValue());
            }
        }

        return retMap;
    }

    static Map<String, String> extractMediaMetadata(final MediaContext mediaContext) {
        Map<String, String> retMap = new HashMap<>();

        Map<String, String> metadata = mediaContext.getMediaMetadata();

        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            if (!isStandardMetadata(standardMediaMetadataMapping, entry.getKey())) {
                retMap.put(entry.getKey(), entry.getValue());
            }
        }

        return retMap;
    }

    static Map<String, Object> extractAdBreakParams(final MediaContext mediaContext) {
        Map<String, Object> retMap = new HashMap<>();

        AdBreakInfo adBreakInfo = mediaContext.getAdBreakInfo();

        if (adBreakInfo != null) {
            retMap.put(
                    MediaCollectionConstants.AdBreak.POD_FRIENDLY_NAME.key, adBreakInfo.getName());
            retMap.put(MediaCollectionConstants.AdBreak.POD_INDEX.key, adBreakInfo.getPosition());
            retMap.put(MediaCollectionConstants.AdBreak.POD_SECOND.key, adBreakInfo.getStartTime());
        }

        return retMap;
    }

    static Map<String, Object> extractAdParams(final MediaContext mediaContext) {
        Map<String, Object> retMap = new HashMap<>();

        AdInfo adInfo = mediaContext.getAdInfo();

        if (adInfo != null) {
            retMap.put(MediaCollectionConstants.Ad.NAME.key, adInfo.getName());
            retMap.put(MediaCollectionConstants.Ad.ID.key, adInfo.getId());
            retMap.put(MediaCollectionConstants.Ad.LENGTH.key, adInfo.getLength());
            retMap.put(MediaCollectionConstants.Ad.POD_POSITION.key, adInfo.getPosition());
        }

        Map<String, String> metadata = mediaContext.getAdMetadata();

        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            if (isStandardMetadata(standardAdMetadataMapping, entry.getKey())) {
                String newKey = getMediaCollectionKey(standardAdMetadataMapping, entry.getKey());
                retMap.put(newKey, entry.getValue());
            }
        }

        return retMap;
    }

    static Map<String, String> extractAdMetadata(final MediaContext mediaContext) {
        Map<String, String> retMap = new HashMap<>();

        Map<String, String> metadata = mediaContext.getAdMetadata();

        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            if (!isStandardMetadata(standardAdMetadataMapping, entry.getKey())) {
                retMap.put(entry.getKey(), entry.getValue());
            }
        }

        return retMap;
    }

    static Map<String, Object> extractChapterParams(final MediaContext mediaContext) {
        Map<String, Object> retMap = new HashMap<>();

        final ChapterInfo chapterInfo = mediaContext.getChapterInfo();

        if (chapterInfo != null) {
            retMap.put(MediaCollectionConstants.Chapter.FRIENDLY_NAME.key, chapterInfo.getName());
            retMap.put(MediaCollectionConstants.Chapter.LENGTH.key, chapterInfo.getLength());
            retMap.put(MediaCollectionConstants.Chapter.OFFSET.key, chapterInfo.getStartTime());
            retMap.put(MediaCollectionConstants.Chapter.INDEX.key, chapterInfo.getPosition());
        }

        return retMap;
    }

    static Map<String, String> extractChapterMetadata(final MediaContext mediaContext) {
        Map<String, String> retMap = new HashMap<>();
        Map<String, String> metadata = mediaContext.getChapterMetadata();

        if (metadata != null) {
            retMap.putAll(metadata);
        }

        return retMap;
    }

    static Map<String, Object> extractQoEData(final MediaContext mediaContext) {
        Map<String, Object> retMap = new HashMap<>();

        final QoEInfo qoeInfo = mediaContext.getQoEInfo();

        if (qoeInfo != null) {
            retMap.put(MediaCollectionConstants.QoE.BITRATE.key, (long) qoeInfo.getBitrate());
            retMap.put(
                    MediaCollectionConstants.QoE.DROPPED_FRAMES.key,
                    (long) qoeInfo.getDroppedFrames());
            retMap.put(MediaCollectionConstants.QoE.FPS.key, (long) qoeInfo.getFPS());
            retMap.put(
                    MediaCollectionConstants.QoE.STARTUP_TIME.key, (long) qoeInfo.getStartupTime());
        }

        return retMap;
    }
}

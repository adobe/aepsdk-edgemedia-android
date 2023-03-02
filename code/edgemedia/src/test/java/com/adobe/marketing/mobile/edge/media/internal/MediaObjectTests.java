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

package com.adobe.marketing.mobile.edge.media.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.adobe.marketing.mobile.AdobeCallback;
import com.adobe.marketing.mobile.util.CloneFailedException;
import com.adobe.marketing.mobile.util.EventDataUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class MediaObjectTests {

    private final Map<String, Object> validMediaInfo;
    private final Map<String, Object> validAdBreakInfo;
    private final Map<String, Object> validAdInfo;
    private final Map<String, Object> validChapterInfo;
    private final Map<String, Object> validQoEInfo;
    private final Map<String, Object> validStateInfo;

    private final List<Object> invalidTypes;
    private final List<Object> typesOtherThanNonEmptyString;
    private final List<Object> typesOtherThanNumber;
    private final List<Object> numberLessThanOne;
    private final List<Object> numberLessThanZero;
    private final List<Object> invalidStateInfo;

    public MediaObjectTests() {
        validMediaInfo = new HashMap<>();
        validMediaInfo.put(MediaTestConstants.EventDataKeys.MediaInfo.NAME, "name");
        validMediaInfo.put(MediaTestConstants.EventDataKeys.MediaInfo.ID, "id");
        validMediaInfo.put(MediaTestConstants.EventDataKeys.MediaInfo.LENGTH, 60.0);
        validMediaInfo.put(MediaTestConstants.EventDataKeys.MediaInfo.STREAM_TYPE, "vod");
        validMediaInfo.put(
                MediaTestConstants.EventDataKeys.MediaInfo.MEDIA_TYPE,
                MediaTestConstants.Media.MEDIA_TYPE_VIDEO);
        validMediaInfo.put(MediaTestConstants.EventDataKeys.MediaInfo.RESUMED, true);
        validMediaInfo.put(MediaTestConstants.EventDataKeys.MediaInfo.GRANULAR_AD_TRACKING, true);

        validAdBreakInfo = new HashMap<>();
        validAdBreakInfo.put(MediaTestConstants.EventDataKeys.AdBreakInfo.NAME, "name");
        validAdBreakInfo.put(MediaTestConstants.EventDataKeys.AdBreakInfo.POSITION, 2);
        validAdBreakInfo.put(MediaTestConstants.EventDataKeys.AdBreakInfo.START_TIME, 60.0);

        validAdInfo = new HashMap<>();
        validAdInfo.put(MediaTestConstants.EventDataKeys.AdInfo.NAME, "name");
        validAdInfo.put(MediaTestConstants.EventDataKeys.AdInfo.ID, "id");
        validAdInfo.put(MediaTestConstants.EventDataKeys.AdInfo.POSITION, 2);
        validAdInfo.put(MediaTestConstants.EventDataKeys.AdInfo.LENGTH, 60.0);

        validChapterInfo = new HashMap<>();
        validChapterInfo.put(MediaTestConstants.EventDataKeys.ChapterInfo.NAME, "name");
        validChapterInfo.put(MediaTestConstants.EventDataKeys.ChapterInfo.POSITION, 2);
        validChapterInfo.put(MediaTestConstants.EventDataKeys.ChapterInfo.LENGTH, 60.0);
        validChapterInfo.put(MediaTestConstants.EventDataKeys.ChapterInfo.START_TIME, 30.0);

        validQoEInfo = new HashMap<>();
        validQoEInfo.put(MediaTestConstants.EventDataKeys.QoEInfo.BITRATE, 1234567890.0);
        validQoEInfo.put(MediaTestConstants.EventDataKeys.QoEInfo.FPS, 23.5);
        validQoEInfo.put(MediaTestConstants.EventDataKeys.QoEInfo.DROPPED_FRAMES, 9876543210.0);
        validQoEInfo.put(MediaTestConstants.EventDataKeys.QoEInfo.STARTUP_TIME, 1.5);

        validStateInfo = new HashMap<>();
        validStateInfo.put(MediaTestConstants.EventDataKeys.StateInfo.STATE_NAME_KEY, "fullscreen");

        invalidTypes = new ArrayList<>();
        invalidTypes.add(null);
        invalidTypes.add("test");
        invalidTypes.add(false);
        invalidTypes.add(1);
        invalidTypes.add(1.0);
        invalidTypes.add(1L);
        invalidTypes.add(new ArrayList<String>());
        invalidTypes.add(new HashMap<String, String>());

        typesOtherThanNonEmptyString = new ArrayList<>();
        typesOtherThanNonEmptyString.add(null);
        typesOtherThanNonEmptyString.add(false);
        typesOtherThanNonEmptyString.add(1.0);
        typesOtherThanNonEmptyString.add(1);
        typesOtherThanNonEmptyString.add(1L);
        typesOtherThanNonEmptyString.add(new ArrayList<String>());
        typesOtherThanNonEmptyString.add(new HashMap<String, String>());

        typesOtherThanNumber = new ArrayList<>();
        typesOtherThanNumber.add("test");
        typesOtherThanNumber.add(null);
        typesOtherThanNumber.add(false);
        typesOtherThanNumber.add(new ArrayList<String>());
        typesOtherThanNumber.add(new HashMap<String, String>());

        numberLessThanOne = new ArrayList<>();
        numberLessThanOne.add(0.0);
        numberLessThanOne.add(0);
        numberLessThanOne.add(0L);
        numberLessThanOne.add(-1.0);
        numberLessThanOne.add(-1);
        numberLessThanOne.add(-1L);

        numberLessThanZero = new ArrayList<>();
        numberLessThanZero.add(-1.0);
        numberLessThanZero.add(-1);
        numberLessThanZero.add(-1L);

        invalidStateInfo = new ArrayList<>();
        invalidStateInfo.add("01234567890123456789012345678901234567890123456789012345678901234");
        invalidStateInfo.add("");
        invalidStateInfo.add("cc@");
        invalidStateInfo.add("!ad");
        invalidStateInfo.add("mu$$te");

        invalidStateInfo.add(new ArrayList<String>());
        invalidStateInfo.add(new HashMap<String, String>());
    }

    // Util
    public void loopReplace(
            final Map<String, Object> validInfo,
            final String key,
            final List<Object> objList,
            final AdobeCallback<Map<String, Object>> callback) {
        try {
            for (Object obj : objList) {
                Map<String, Object> info = EventDataUtils.clone(validInfo);
                info.put(key, obj);
                callback.call(info);
            }
        } catch (CloneFailedException e) {
            fail();
        }
    }

    // MediaInfo tests
    @Test
    public void MediaInfo_create_fail_withNullMap() {
        assertNull(MediaInfo.fromObjectMap(null));
    }

    @Test
    public void MediaInfo_create_pass_withValidInfo() {
        MediaInfo mediaInfo = MediaInfo.fromObjectMap(validMediaInfo);

        assertEquals("name", mediaInfo.getName());
        assertEquals("id", mediaInfo.getId());
        assertEquals(60.0, mediaInfo.getLength(), 0);
        assertEquals("vod", mediaInfo.getStreamType());
        assertEquals(MediaType.Video, mediaInfo.getMediaType());
        assertTrue(mediaInfo.isResumed());
        assertTrue(mediaInfo.isGranularAdTrackingEnabled());
    }

    @Test
    public void MediaInfo_create_fail_withMissingRequiredInfo() throws CloneFailedException {

        List<String> requiredKeys = new ArrayList<>();
        requiredKeys.add(MediaTestConstants.EventDataKeys.MediaInfo.NAME);
        requiredKeys.add(MediaTestConstants.EventDataKeys.MediaInfo.ID);
        requiredKeys.add(MediaTestConstants.EventDataKeys.MediaInfo.LENGTH);
        requiredKeys.add(MediaTestConstants.EventDataKeys.MediaInfo.STREAM_TYPE);
        requiredKeys.add(MediaTestConstants.EventDataKeys.MediaInfo.MEDIA_TYPE);

        for (String key : requiredKeys) {
            Map<String, Object> info = EventDataUtils.clone(validMediaInfo);
            info.remove(key);
            assertNull(MediaInfo.fromObjectMap(info));
        }
    }

    @Test
    public void MediaInfo_create_fail_withInvalidName() {
        loopReplace(
                validMediaInfo,
                MediaTestConstants.EventDataKeys.MediaInfo.NAME,
                typesOtherThanNonEmptyString,
                info -> assertNull(null));
    }

    @Test
    public void MediaInfo_create_fail_withInvalidID() {
        loopReplace(
                validMediaInfo,
                MediaTestConstants.EventDataKeys.MediaInfo.ID,
                typesOtherThanNonEmptyString,
                info -> assertNull(null));
    }

    @Test
    public void createMediaInfo_fail_withInvalidLength() {
        loopReplace(
                validMediaInfo,
                MediaTestConstants.EventDataKeys.MediaInfo.LENGTH,
                typesOtherThanNumber,
                info -> assertNull(null));
    }

    @Test
    public void MediaInfo_create_fail_withInvalidStreamType() {
        loopReplace(
                validMediaInfo,
                MediaTestConstants.EventDataKeys.MediaInfo.STREAM_TYPE,
                typesOtherThanNonEmptyString,
                info -> assertNull(null));
    }

    @Test
    public void MediaInfo_create_fail_withInvalidMediaType() {
        loopReplace(
                validMediaInfo,
                MediaTestConstants.EventDataKeys.MediaInfo.MEDIA_TYPE,
                invalidTypes,
                info -> assertNull(null));
    }

    @Test
    public void MediaInfo_create_withResumedSetToFalse() {
        List<Object> falseVariant = new ArrayList<>();
        falseVariant.add(new HashMap<String, Object>());
        falseVariant.add(new HashMap<String, String>());
        falseVariant.add(new ArrayList<String>());
        falseVariant.add(0);
        falseVariant.add("test");

        loopReplace(
                validMediaInfo,
                MediaTestConstants.EventDataKeys.MediaInfo.RESUMED,
                falseVariant,
                info -> {
                    MediaInfo mediaInfo = MediaInfo.fromObjectMap(info);
                    assertFalse(mediaInfo.isResumed());
                });
    }

    @Test
    public void MediaInfo_create_pass_isValidMediaInfoObject() {
        validMediaInfo.put(MediaTestConstants.EventDataKeys.MediaInfo.GRANULAR_AD_TRACKING, false);
        MediaInfo mediaInfo = MediaInfo.fromObjectMap(validMediaInfo);

        MediaInfo expectedMediaInfo =
                MediaInfo.create("id", "name", "vod", MediaType.Video, 60.0, true);

        assertEquals(expectedMediaInfo, mediaInfo);
    }

    @Test
    public void MediaInfo_equals_sameMediaInfoInstance_pass() {
        MediaInfo mediaInfo = MediaInfo.fromObjectMap(validMediaInfo);
        assertEquals(mediaInfo, mediaInfo);
    }

    @Test
    public void MediaInfo_equals_differentObjectType_fail() {
        MediaInfo mediaInfo = MediaInfo.fromObjectMap(validMediaInfo);

        Object differentObject = "notMediaInfoInstance";
        assertNotEquals(mediaInfo, differentObject);
    }

    @Test
    public void MediaInfo_equals_differentId_fail() {
        MediaInfo mediaInfo = MediaInfo.fromObjectMap(validMediaInfo);

        MediaInfo mediaInfo2 =
                MediaInfo.create("diff-id", "name", "vod", MediaType.Video, 60.0, true);
        assertNotEquals(mediaInfo, mediaInfo2);
    }

    @Test
    public void MediaInfo_equals_differentName_fail() {
        MediaInfo mediaInfo = MediaInfo.fromObjectMap(validMediaInfo);

        MediaInfo mediaInfo2 =
                MediaInfo.create("id", "diff-name", "vod", MediaType.Video, 60.0, true);
        assertNotEquals(mediaInfo, mediaInfo2);
    }

    @Test
    public void MediaInfo_equals_differentStreamType_fail() {
        MediaInfo mediaInfo = MediaInfo.fromObjectMap(validMediaInfo);

        MediaInfo mediaInfo2 =
                MediaInfo.create("id", "name", "not-vod", MediaType.Video, 60.0, true);
        assertNotEquals(mediaInfo, mediaInfo2);
    }

    @Test
    public void MediaInfo_equals_differentLength_fail() {
        MediaInfo mediaInfo = MediaInfo.fromObjectMap(validMediaInfo);

        MediaInfo mediaInfo2 = MediaInfo.create("id", "name", "vod", MediaType.Video, 6000.0, true);
        assertNotEquals(mediaInfo, mediaInfo2);
    }

    @Test
    public void MediaInfo_equals_differentResumedFlag_fail() {
        MediaInfo mediaInfo = MediaInfo.fromObjectMap(validMediaInfo);

        MediaInfo mediaInfo2 = MediaInfo.create("id", "name", "vod", MediaType.Video, 60.0, false);
        assertNotEquals(mediaInfo, mediaInfo2);
    }

    @Test
    public void MediaInfo_equals_differentMediaType_fail() {
        MediaInfo mediaInfo = MediaInfo.fromObjectMap(validMediaInfo);

        MediaInfo mediaInfo2 = MediaInfo.create("id", "name", "vod", MediaType.Audio, 60.0, true);
        assertNotEquals(mediaInfo, mediaInfo2);
    }

    @Test
    public void MediaInfo_equals_sameParameters_pass() {
        boolean defaultGranularAdTrackingEnabled = false;
        validMediaInfo.put(
                MediaTestConstants.EventDataKeys.MediaInfo.GRANULAR_AD_TRACKING,
                defaultGranularAdTrackingEnabled);
        MediaInfo mediaInfo = MediaInfo.fromObjectMap(validMediaInfo);

        MediaInfo mediaInfo2 =
                MediaInfo.create(
                        mediaInfo.getId(),
                        mediaInfo.getName(),
                        mediaInfo.getStreamType(),
                        mediaInfo.getMediaType(),
                        mediaInfo.getLength(),
                        mediaInfo.isResumed());
        MediaInfo mediaInfo3 =
                MediaInfo.create(
                        mediaInfo.getId(),
                        mediaInfo.getName(),
                        mediaInfo.getStreamType(),
                        mediaInfo.getMediaType(),
                        mediaInfo.getLength(),
                        mediaInfo.isResumed(),
                        100,
                        defaultGranularAdTrackingEnabled);
        MediaInfo mediaInfo4 =
                MediaInfo.create(
                        mediaInfo.getId(),
                        mediaInfo.getName(),
                        mediaInfo.getStreamType(),
                        mediaInfo.getMediaType(),
                        mediaInfo.getLength(),
                        mediaInfo.isResumed(),
                        mediaInfo3.getPrerollWaitTime(),
                        defaultGranularAdTrackingEnabled);
        assertEquals(mediaInfo, mediaInfo2);
        assertEquals(mediaInfo3, mediaInfo4);
    }

    @Test
    public void MediaInfo_toObjectMap() {
        MediaInfo mediaInfo = MediaInfo.create("id", "name", "vod", MediaType.Video, 60.0, true);
        Map<String, Object> mediaInfoMap = mediaInfo.toObjectMap();

        assertEquals(mediaInfoMap.get(MediaTestConstants.EventDataKeys.MediaInfo.NAME), "name");
        assertEquals(mediaInfoMap.get(MediaTestConstants.EventDataKeys.MediaInfo.ID), "id");
        assertEquals(
                (double) mediaInfoMap.get(MediaTestConstants.EventDataKeys.MediaInfo.LENGTH),
                60.0,
                0);
        assertEquals(
                mediaInfoMap.get(MediaTestConstants.EventDataKeys.MediaInfo.STREAM_TYPE), "vod");
        assertEquals(
                mediaInfoMap.get(MediaTestConstants.EventDataKeys.MediaInfo.MEDIA_TYPE),
                MediaTestConstants.Media.MEDIA_TYPE_VIDEO);
        assertEquals(mediaInfoMap.get(MediaTestConstants.EventDataKeys.MediaInfo.RESUMED), true);
        assertEquals(
                mediaInfoMap.get(
                        MediaTestConstants.EventDataKeys.MediaInfo.PREROLL_TRACKING_WAITING_TIME),
                250L);
    }

    @Test
    public void MediaInfo_toString() {

        String expected =
                "{"
                        + " class: \"MediaInfo\","
                        + " id: "
                        + "\""
                        + "id"
                        + "\""
                        + " name: "
                        + "\""
                        + "name"
                        + "\""
                        + " length: "
                        + 60.0
                        + " streamType: "
                        + "\""
                        + "vod"
                        + "\""
                        + " mediaType: "
                        + "\""
                        + "video"
                        + "\""
                        + " resumed: "
                        + true
                        + " prerollWaitTime: "
                        + 250
                        + "}";

        MediaInfo mediaInfo = MediaInfo.create("id", "name", "vod", MediaType.Video, 60.0, true);
        String actual = mediaInfo.toString();

        assertEquals(expected, actual);
    }

    // AdBreakInfo tests
    @Test
    public void AdBreakInfo_create_fail_withNullVariant() {
        assertNull(AdBreakInfo.fromObjectMap(null));
    }

    @Test
    public void AdBreakInfo_create_pass_withValidInfo() {
        AdBreakInfo adBreakInfo = AdBreakInfo.fromObjectMap(validAdBreakInfo);

        assertEquals("name", adBreakInfo.getName());
        assertEquals(2, adBreakInfo.getPosition());
        assertEquals(60.0, adBreakInfo.getStartTime(), 0);
    }

    @Test
    public void AdBreakInfo_create_fail_withMissingRequiredInfo() throws CloneFailedException {

        List<String> requiredKeys = new ArrayList<>();
        requiredKeys.add(MediaTestConstants.EventDataKeys.AdBreakInfo.NAME);
        requiredKeys.add(MediaTestConstants.EventDataKeys.AdBreakInfo.POSITION);
        requiredKeys.add(MediaTestConstants.EventDataKeys.AdBreakInfo.START_TIME);

        for (String key : requiredKeys) {
            Map<String, Object> info = EventDataUtils.clone(validAdBreakInfo);
            info.remove(key);
            assertNull(AdBreakInfo.fromObjectMap(info));
        }
    }

    @Test
    public void AdBreakInfo_create_fail_withInvalidName() {
        loopReplace(
                validAdBreakInfo,
                MediaTestConstants.EventDataKeys.AdBreakInfo.NAME,
                typesOtherThanNonEmptyString,
                info -> assertNull(AdBreakInfo.fromObjectMap(info)));
    }

    @Test
    public void AdBreakInfo_create_fail_withInvalidPosition() {
        loopReplace(
                validAdBreakInfo,
                MediaTestConstants.EventDataKeys.AdBreakInfo.POSITION,
                typesOtherThanNumber,
                info -> assertNull(AdBreakInfo.fromObjectMap(info)));
        loopReplace(
                validAdBreakInfo,
                MediaTestConstants.EventDataKeys.AdBreakInfo.POSITION,
                numberLessThanOne,
                info -> assertNull(AdBreakInfo.fromObjectMap(info)));
    }

    @Test
    public void AdBreakInfo_create_fail_withInvalidStartTime() {
        loopReplace(
                validAdBreakInfo,
                MediaTestConstants.EventDataKeys.AdBreakInfo.START_TIME,
                typesOtherThanNumber,
                info -> assertNull(AdBreakInfo.fromObjectMap(info)));
    }

    @Test
    public void AdBreakInfo_create_pass_isValidAdBreakInfoObject() {
        AdBreakInfo adBreakInfo = AdBreakInfo.fromObjectMap(validAdBreakInfo);

        AdBreakInfo expectedAdBreakInfo = AdBreakInfo.create("name", 2, 60.0);

        assertEquals(expectedAdBreakInfo, adBreakInfo);
    }

    @Test
    public void AdBreakInfo_equals_sameAdBreakInfoInstance_pass() {
        AdBreakInfo adBreakInfo = AdBreakInfo.fromObjectMap(validAdBreakInfo);
        AdBreakInfo adBreakInfo2 = adBreakInfo;
        assertEquals(adBreakInfo, adBreakInfo2);
    }

    @Test
    public void AdBreakInfo_equals_differentObjectType_fail() {
        AdBreakInfo adBreakInfo = AdBreakInfo.fromObjectMap(validAdBreakInfo);

        Object differentObject = "notAdBreakInfoInstance";
        assertNotEquals(adBreakInfo, differentObject);
    }

    @Test
    public void AdBreakInfo_equals_differentName_fail() {
        AdBreakInfo adBreakInfo = AdBreakInfo.fromObjectMap(validAdBreakInfo);

        AdBreakInfo adBreakInfo2 = AdBreakInfo.create("diff-name", 2, 60.0);
        assertNotEquals(adBreakInfo, adBreakInfo2);
    }

    @Test
    public void AdBreakInfo_equals__differentPosition_fail() {
        AdBreakInfo adBreakInfo = AdBreakInfo.fromObjectMap(validAdBreakInfo);

        AdBreakInfo adBreakInfo2 = AdBreakInfo.create("name", 0, 60.0);
        assertNotEquals(adBreakInfo, adBreakInfo2);
    }

    @Test
    public void AdBreakInfo_equals_differentStartTime_fail() {
        AdBreakInfo adBreakInfo = AdBreakInfo.fromObjectMap(validAdBreakInfo);

        AdBreakInfo adBreakInfo2 = AdBreakInfo.create("name", 2, 6000.0);
        assertNotEquals(adBreakInfo, adBreakInfo2);
    }

    @Test
    public void AdBreakInfo_equals_sameParameters_pass() {
        AdBreakInfo adBreakInfo = AdBreakInfo.fromObjectMap(validAdBreakInfo);

        AdBreakInfo adBreakInfo2 =
                AdBreakInfo.create(
                        adBreakInfo.getName(),
                        adBreakInfo.getPosition(),
                        adBreakInfo.getStartTime());
        assertEquals(adBreakInfo, adBreakInfo2);
    }

    @Test
    public void AdBreakInfo_toObjectMap() {
        AdBreakInfo adBreakInfo = AdBreakInfo.create("name", 2, 60.0);
        Map<String, Object> adBreakInfoMap = adBreakInfo.toObjectMap();

        assertEquals(adBreakInfoMap.get(MediaTestConstants.EventDataKeys.AdBreakInfo.NAME), "name");
        assertEquals(adBreakInfoMap.get(MediaTestConstants.EventDataKeys.AdBreakInfo.POSITION), 2L);
        assertEquals(
                (double)
                        adBreakInfoMap.get(MediaTestConstants.EventDataKeys.AdBreakInfo.START_TIME),
                60.0,
                0);
    }

    @Test
    public void AdBreakInfotoString() {

        String expected =
                "{"
                        + " class: \"AdBreakInfo\","
                        + " name: "
                        + "\""
                        + "name"
                        + "\""
                        + " position: "
                        + 2
                        + " startTime: "
                        + 60.0
                        + "}";

        AdBreakInfo adBreakInfo = AdBreakInfo.create("name", 2, 60.0);
        String actual = adBreakInfo.toString();

        assertEquals(expected, actual);
    }

    // AdInfo tests
    @Test
    public void AdInfo_create_fail_withNullInfo() {
        assertNull(AdInfo.fromObjectMap(null));
    }

    @Test
    public void AdInfo_create_pass_withValidInfo() {
        AdInfo adInfo = AdInfo.fromObjectMap(validAdInfo);

        assertEquals("id", adInfo.getId());
        assertEquals("name", adInfo.getName());
        assertEquals(2, adInfo.getPosition());
        assertEquals(60.0, adInfo.getLength(), 0);
    }

    @Test
    public void AdInfo_create_fail_withMissingRequiredInfo() throws CloneFailedException {

        List<String> requiredKeys = new ArrayList<>();

        requiredKeys.add(MediaTestConstants.EventDataKeys.AdInfo.ID);
        requiredKeys.add(MediaTestConstants.EventDataKeys.AdInfo.NAME);
        requiredKeys.add(MediaTestConstants.EventDataKeys.AdInfo.POSITION);
        requiredKeys.add(MediaTestConstants.EventDataKeys.AdInfo.LENGTH);

        for (String key : requiredKeys) {
            Map<String, Object> info = EventDataUtils.clone(validAdInfo);
            info.remove(key);
            assertNull(AdInfo.fromObjectMap(info));
        }
    }

    @Test
    public void AdInfo_create_fail_withInvalidID() {
        loopReplace(
                validAdInfo,
                MediaTestConstants.EventDataKeys.AdInfo.ID,
                typesOtherThanNonEmptyString,
                info -> assertNull(AdInfo.fromObjectMap(info)));
    }

    @Test
    public void AdInfo_create_fail_withInvalidName() {
        loopReplace(
                validAdInfo,
                MediaTestConstants.EventDataKeys.AdInfo.NAME,
                typesOtherThanNonEmptyString,
                info -> assertNull(AdInfo.fromObjectMap(info)));
    }

    @Test
    public void AdInfo_create_fail_withInvalidPosition() {
        loopReplace(
                validAdInfo,
                MediaTestConstants.EventDataKeys.AdInfo.POSITION,
                typesOtherThanNumber,
                info -> assertNull(AdInfo.fromObjectMap(info)));
        loopReplace(
                validAdInfo,
                MediaTestConstants.EventDataKeys.AdInfo.POSITION,
                numberLessThanOne,
                info -> assertNull(AdInfo.fromObjectMap(info)));
    }

    @Test
    public void AdInfo_create_fail_withInvalidLength() {
        loopReplace(
                validAdInfo,
                MediaTestConstants.EventDataKeys.AdInfo.LENGTH,
                typesOtherThanNumber,
                info -> assertNull(AdInfo.fromObjectMap(info)));
    }

    @Test
    public void AdInfo_create_pass_isValidAdInfoObject() {
        AdInfo adInfo = AdInfo.fromObjectMap(validAdInfo);

        AdInfo expectedAdInfo = AdInfo.create("id", "name", 2, 60.0);

        assertEquals(expectedAdInfo, adInfo);
    }

    @Test
    public void AdInfo_equals_sameAdInfoInstance_pass() {
        AdInfo adInfo = AdInfo.fromObjectMap(validAdInfo);
        AdInfo adInfo2 = adInfo;
        assertEquals(adInfo, adInfo2);
    }

    @Test
    public void AdInfo_equals_differentObjectType_fail() {
        AdInfo adInfo = AdInfo.fromObjectMap(validAdInfo);
        Object differentObject = "notAdBreakInfoInstance";
        assertNotEquals(adInfo, differentObject);
    }

    @Test
    public void AdInfo_equals_differentId_fail() {
        AdInfo adInfo = AdInfo.fromObjectMap(validAdInfo);
        AdInfo adInfo2 = AdInfo.create("diff-id", "name", 2, 60.0);
        assertNotEquals(adInfo, adInfo2);
    }

    @Test
    public void AdInfo_equals_differentName_fail() {
        AdInfo adInfo = AdInfo.fromObjectMap(validAdInfo);
        AdInfo adInfo2 = AdInfo.create("id", "diff-name", 2, 60.0);
        assertNotEquals(adInfo, adInfo2);
    }

    @Test
    public void AdInfo_equals_differentPosition_fail() {
        AdInfo adInfo = AdInfo.fromObjectMap(validAdInfo);
        AdInfo adInfo2 = AdInfo.create("id", "name", 12, 60.0);
        assertNotEquals(adInfo, adInfo2);
    }

    @Test
    public void AdInfo_equals_differentLength_fail() {
        AdInfo adInfo = AdInfo.fromObjectMap(validAdInfo);
        AdInfo adInfo2 = AdInfo.create("id", "name", 2, 121.121);
        assertNotEquals(adInfo, adInfo2);
    }

    @Test
    public void AdInfo_equals_sameParameters_pass() {
        AdInfo adInfo = AdInfo.fromObjectMap(validAdInfo);
        AdInfo adInfo2 =
                AdInfo.create(
                        adInfo.getId(), adInfo.getName(), adInfo.getPosition(), adInfo.getLength());
        assertEquals(adInfo, adInfo2);
    }

    @Test
    public void AdInfo_toObjectMap() {
        AdInfo adInfo = AdInfo.create("id", "name", 2, 60);
        Map<String, Object> adInfoMap = adInfo.toObjectMap();
        assertEquals(adInfoMap.get(MediaTestConstants.EventDataKeys.AdInfo.NAME), "name");
        assertEquals(adInfoMap.get(MediaTestConstants.EventDataKeys.AdInfo.ID), "id");
        assertEquals(adInfoMap.get(MediaTestConstants.EventDataKeys.AdInfo.POSITION), 2L);
        assertEquals(
                (double) adInfoMap.get(MediaTestConstants.EventDataKeys.AdInfo.LENGTH), 60.0, 0);
    }

    @Test
    public void AdInfo_toString() {

        String expected =
                "{"
                        + " class: \"AdInfo\","
                        + " id: "
                        + "\""
                        + "id"
                        + "\""
                        + " name: "
                        + "\""
                        + "name"
                        + "\""
                        + " position: "
                        + 2
                        + " length: "
                        + 60.0
                        + "}";

        AdInfo adInfo = AdInfo.create("id", "name", 2, 60.0);
        String actual = adInfo.toString();

        assertEquals(expected, actual);
    }

    // ChapterInfo tests
    @Test
    public void ChapterInfo_create_fail_withNullMap() {
        assertNull(ChapterInfo.fromObjectMap(null));
    }

    @Test
    public void ChapterInfo_create_pass_withValidInfo() {
        ChapterInfo chapterInfo = ChapterInfo.fromObjectMap(validChapterInfo);

        assertEquals("name", chapterInfo.getName());
        assertEquals(2, chapterInfo.getPosition());
        assertEquals(30.0, chapterInfo.getStartTime(), 0);
        assertEquals(60.0, chapterInfo.getLength(), 0);
    }

    @Test
    public void ChapterInfo_create_fail_withMissingRequiredInfo() throws CloneFailedException {
        List<String> requiredKeys = new ArrayList<>();

        requiredKeys.add(MediaTestConstants.EventDataKeys.ChapterInfo.NAME);
        requiredKeys.add(MediaTestConstants.EventDataKeys.ChapterInfo.POSITION);
        requiredKeys.add(MediaTestConstants.EventDataKeys.ChapterInfo.START_TIME);
        requiredKeys.add(MediaTestConstants.EventDataKeys.ChapterInfo.LENGTH);

        for (String key : requiredKeys) {
            Map<String, Object> info = EventDataUtils.clone(validChapterInfo);
            info.remove(key);
            assertNull(ChapterInfo.fromObjectMap(info));
        }
    }

    @Test
    public void ChapterInfo_create_fail_withInvalidName() {
        loopReplace(
                validChapterInfo,
                MediaTestConstants.EventDataKeys.ChapterInfo.NAME,
                typesOtherThanNonEmptyString,
                info -> assertNull(ChapterInfo.fromObjectMap(info)));
    }

    @Test
    public void ChapterInfo_create_fail_withInvalidPosition() {
        loopReplace(
                validChapterInfo,
                MediaTestConstants.EventDataKeys.ChapterInfo.POSITION,
                typesOtherThanNumber,
                info -> assertNull(ChapterInfo.fromObjectMap(info)));
        loopReplace(
                validChapterInfo,
                MediaTestConstants.EventDataKeys.ChapterInfo.POSITION,
                numberLessThanOne,
                info -> assertNull(ChapterInfo.fromObjectMap(info)));
    }

    @Test
    public void ChapterInfo_create_fail_withInvalidStartTime() {
        loopReplace(
                validChapterInfo,
                MediaTestConstants.EventDataKeys.ChapterInfo.START_TIME,
                typesOtherThanNumber,
                info -> assertNull(ChapterInfo.fromObjectMap(info)));
    }

    @Test
    public void ChapterInfo_create_fail_withInvalidLength() {
        loopReplace(
                validChapterInfo,
                MediaTestConstants.EventDataKeys.ChapterInfo.LENGTH,
                typesOtherThanNumber,
                info -> assertNull(ChapterInfo.fromObjectMap(info)));
    }

    @Test
    public void ChapterInfo_create_pass_isValidChapterInfoObject() {
        ChapterInfo chapterInfo = ChapterInfo.fromObjectMap(validChapterInfo);

        ChapterInfo expectedChapterInfo = ChapterInfo.create("name", 2, 30.0, 60.0);

        assertEquals(expectedChapterInfo, chapterInfo);
    }

    @Test
    public void ChapterInfo_equals_sameChapterInfoInstance_pass() {
        ChapterInfo chapterInfo = ChapterInfo.fromObjectMap(validChapterInfo);

        ChapterInfo chapterInfo2 = chapterInfo;
        assertEquals(chapterInfo, chapterInfo2);
    }

    @Test
    public void ChapterInfo_equals_differentObjectType_fail() {
        ChapterInfo chapterInfo = ChapterInfo.fromObjectMap(validChapterInfo);

        Object differentObject = "notAdBreakInfoInstance";
        assertNotEquals(chapterInfo, differentObject);
    }

    @Test
    public void ChapterInfo_equals_differentName_fail() {
        ChapterInfo chapterInfo = ChapterInfo.fromObjectMap(validChapterInfo);

        ChapterInfo chapterInfo2 = ChapterInfo.create("diff-name", 2, 30.0, 60.0);
        assertNotEquals(chapterInfo, chapterInfo2);
    }

    @Test
    public void ChapterInfo_equals_differentPosition_fail() {
        ChapterInfo chapterInfo = ChapterInfo.fromObjectMap(validChapterInfo);

        ChapterInfo chapterInfo2 = ChapterInfo.create("name", 4, 30.0, 60.0);
        assertNotEquals(chapterInfo, chapterInfo2);
    }

    @Test
    public void ChapterInfo_equals_differentStartTime_fail() {
        ChapterInfo chapterInfo = ChapterInfo.fromObjectMap(validChapterInfo);

        ChapterInfo chapterInfo2 = ChapterInfo.create("name", 2, 300.0, 60.0);
        assertNotEquals(chapterInfo, chapterInfo2);
    }

    @Test
    public void ChapterInfo_equals_differentLength_fail() {
        ChapterInfo chapterInfo = ChapterInfo.fromObjectMap(validChapterInfo);

        ChapterInfo chapterInfo2 = ChapterInfo.create("name", 2, 30.0, 6000.0);
        assertNotEquals(chapterInfo, chapterInfo2);
    }

    @Test
    public void ChapterInfo_equals_sameParameters_pass() {
        ChapterInfo chapterInfo = ChapterInfo.fromObjectMap(validChapterInfo);

        ChapterInfo chapterInfo2 = ChapterInfo.create("name", 2, 30.0, 60.0);
        assertEquals(chapterInfo, chapterInfo2);
    }

    @Test
    public void ChapterInfo_toObjectMap() {
        ChapterInfo chapterInfo = ChapterInfo.create("name", 2, 30.0, 60.0);
        Map<String, Object> chapterInfoMap = chapterInfo.toObjectMap();

        assertEquals(chapterInfoMap.get(MediaTestConstants.EventDataKeys.ChapterInfo.NAME), "name");
        assertEquals(chapterInfoMap.get(MediaTestConstants.EventDataKeys.ChapterInfo.POSITION), 2L);
        assertEquals(
                (double) chapterInfoMap.get(MediaTestConstants.EventDataKeys.ChapterInfo.LENGTH),
                60.0,
                0);
        assertEquals(
                (double)
                        chapterInfoMap.get(MediaTestConstants.EventDataKeys.ChapterInfo.START_TIME),
                30.0,
                0);
    }

    @Test
    public void ChapterInfo_toString() {

        String expected =
                "{"
                        + " class: \"ChapterInfo\","
                        + " name: "
                        + "\""
                        + "name"
                        + "\""
                        + " position: "
                        + 2
                        + " startTime: "
                        + 30.0
                        + " length: "
                        + 60.0
                        + "}";

        ChapterInfo chapterInfo = ChapterInfo.create("name", 2, 30.0, 60.0);
        String actual = chapterInfo.toString();

        assertEquals(expected, actual);
    }

    // QoEInfo tests
    @Test
    public void QoEInfo_create_fail_withNullMap() {
        assertNull(QoEInfo.fromObjectMap(null));
    }

    @Test
    public void QoEInfo_create_pass_withValidInfo() {
        QoEInfo qoEInfo = QoEInfo.fromObjectMap(validQoEInfo);

        assertEquals(1234567890.0, qoEInfo.getBitrate(), 0);
        assertEquals(9876543210.0, qoEInfo.getDroppedFrames(), 0);
        assertEquals(23.5, qoEInfo.getFPS(), 0);
        assertEquals(1.5, qoEInfo.getStartupTime(), 0);
    }

    @Test
    public void QoEInfo_create_fail_withMissingRequiredInfo() throws CloneFailedException {

        List<String> requiredKeys = new ArrayList<>();

        requiredKeys.add(MediaTestConstants.EventDataKeys.QoEInfo.BITRATE);
        requiredKeys.add(MediaTestConstants.EventDataKeys.QoEInfo.DROPPED_FRAMES);
        requiredKeys.add(MediaTestConstants.EventDataKeys.QoEInfo.FPS);
        requiredKeys.add(MediaTestConstants.EventDataKeys.QoEInfo.STARTUP_TIME);

        for (String key : requiredKeys) {
            Map<String, Object> info = EventDataUtils.clone(validQoEInfo);
            info.remove(key);
            assertNull(QoEInfo.fromObjectMap(info));
        }
    }

    @Test
    public void QoEInfo_create_fail_withInvalidBitrate() {
        loopReplace(
                validQoEInfo,
                MediaTestConstants.EventDataKeys.QoEInfo.BITRATE,
                typesOtherThanNumber,
                info -> assertNull(QoEInfo.fromObjectMap(info)));

        loopReplace(
                validQoEInfo,
                MediaTestConstants.EventDataKeys.QoEInfo.BITRATE,
                numberLessThanZero,
                info -> assertNull(QoEInfo.fromObjectMap(info)));
    }

    @Test
    public void QoEInfo_create_fail_withInvalidDroppedFrames() {
        loopReplace(
                validQoEInfo,
                MediaTestConstants.EventDataKeys.QoEInfo.DROPPED_FRAMES,
                typesOtherThanNumber,
                info -> assertNull(QoEInfo.fromObjectMap(info)));
        loopReplace(
                validQoEInfo,
                MediaTestConstants.EventDataKeys.QoEInfo.DROPPED_FRAMES,
                numberLessThanZero,
                info -> assertNull(QoEInfo.fromObjectMap(info)));
    }

    @Test
    public void QoEInfo_create_fail_withInvalidFPS() {
        loopReplace(
                validQoEInfo,
                MediaTestConstants.EventDataKeys.QoEInfo.FPS,
                typesOtherThanNumber,
                info -> assertNull(QoEInfo.fromObjectMap(info)));
        loopReplace(
                validQoEInfo,
                MediaTestConstants.EventDataKeys.QoEInfo.FPS,
                numberLessThanZero,
                info -> assertNull(QoEInfo.fromObjectMap(info)));
    }

    @Test
    public void QoEInfo_create_fail_withInvalidStartupTime() {
        loopReplace(
                validQoEInfo,
                MediaTestConstants.EventDataKeys.QoEInfo.STARTUP_TIME,
                typesOtherThanNumber,
                info -> assertNull(QoEInfo.fromObjectMap(info)));
        loopReplace(
                validQoEInfo,
                MediaTestConstants.EventDataKeys.QoEInfo.STARTUP_TIME,
                numberLessThanZero,
                info -> assertNull(QoEInfo.fromObjectMap(info)));
    }

    @Test
    public void QoEInfo_create_pass_isValidQoEInfoObject() {
        QoEInfo qoEInfo = QoEInfo.fromObjectMap(validQoEInfo);

        QoEInfo expectedQoEInfo = QoEInfo.create(1234567890, 9876543210.0, 23.5, 1.5);

        assertEquals(expectedQoEInfo, qoEInfo);
    }

    @Test
    public void QoEInfo_equals_sameQoEInfoInstance_pass() {
        QoEInfo qoEInfo = QoEInfo.fromObjectMap(validQoEInfo);

        QoEInfo qoEInfo2 = qoEInfo;
        assertEquals(qoEInfo, qoEInfo2);
    }

    @Test
    public void QoEInfo_equals_differentObjectType_fail() {
        QoEInfo qoEInfo = QoEInfo.fromObjectMap(validQoEInfo);

        Object differentObject = "notAdBreakInfoInstance";
        assertNotEquals(qoEInfo, differentObject);
    }

    @Test
    public void QoEInfo_equals_differentBitrate_fail() {
        QoEInfo qoEInfo = QoEInfo.fromObjectMap(validQoEInfo);

        QoEInfo qoEInfo2 = QoEInfo.create(111111, 9876543210.0, 23.5, 1.5);
        assertNotEquals(qoEInfo, qoEInfo2);
    }

    @Test
    public void QoEInfo_equals_differentDroppedFrames_fail() {

        QoEInfo qoEInfo = QoEInfo.fromObjectMap(validQoEInfo);

        QoEInfo qoEInfo2 = QoEInfo.create(1234567890, 9.0, 23.5, 1.5);
        assertNotEquals(qoEInfo, qoEInfo2);
    }

    @Test
    public void QoEInfo_equals_differentFPS_fail() {

        QoEInfo qoEInfo = QoEInfo.fromObjectMap(validQoEInfo);

        QoEInfo qoEInfo2 = QoEInfo.create(1234567890, 9876543210.0, 2.15, 1.5);
        assertNotEquals(qoEInfo, qoEInfo2);
    }

    @Test
    public void QoEInfo_equals_differentStartupTime_fail() {

        QoEInfo qoEInfo = QoEInfo.fromObjectMap(validQoEInfo);

        QoEInfo qoEInfo2 = QoEInfo.create(1234567890, 9876543210.0, 23.5, 111);
        assertNotEquals(qoEInfo, qoEInfo2);
    }

    @Test
    public void QoEInfo_equals_sameParameters_pass() {
        QoEInfo qoEInfo = QoEInfo.fromObjectMap(validQoEInfo);

        QoEInfo qoEInfo2 =
                QoEInfo.create(
                        qoEInfo.getBitrate(),
                        qoEInfo.getDroppedFrames(),
                        qoEInfo.getFPS(),
                        qoEInfo.getStartupTime());
        assertEquals(qoEInfo, qoEInfo2);
    }

    @Test
    public void QoeInfo_toObjectMap() {
        QoEInfo qoeInfo = QoEInfo.create(1234567890, 9876543210.0, 23.5, 1.5);
        Map<String, Object> qoEInfoMap = qoeInfo.toObjectMap();
        assertEquals(
                (double) qoEInfoMap.get(MediaTestConstants.EventDataKeys.QoEInfo.BITRATE),
                1234567890.0,
                0);
        assertEquals(
                (double) qoEInfoMap.get(MediaTestConstants.EventDataKeys.QoEInfo.FPS), 23.5, 0);
        assertEquals(
                (double) qoEInfoMap.get(MediaTestConstants.EventDataKeys.QoEInfo.DROPPED_FRAMES),
                9876543210.0,
                0);
        assertEquals(
                (double) qoEInfoMap.get(MediaTestConstants.EventDataKeys.QoEInfo.STARTUP_TIME),
                1.5,
                0);
    }

    @Test
    public void QoeInfo_toString() {

        String expected =
                "{"
                        + " class: \"QoEInfo\","
                        + " bitrate: "
                        + 1234567890.0
                        + " droppedFrames: "
                        + 9876543210.0
                        + " fps: "
                        + 23.5
                        + " startupTime: "
                        + 1.5
                        + "}";

        QoEInfo qoeInfo = QoEInfo.create(1234567890, 9876543210.0, 23.5, 1.5);
        String actual = qoeInfo.toString();

        assertEquals(expected, actual);
    }

    // StateInfo test

    @Test
    public void stateInfo_fail_InvalidSpecialCharacters() {
        loopReplace(
                validStateInfo,
                MediaTestConstants.EventDataKeys.StateInfo.STATE_NAME_KEY,
                invalidStateInfo,
                info -> assertNull(StateInfo.fromObjectMap(info)));
    }

    @Test
    public void StateInfo_create_pass_withValidInfo() {
        StateInfo stateInfo = StateInfo.fromObjectMap(validStateInfo);

        assertEquals(stateInfo.getStateName(), "fullscreen");

        String string64Char = "0123456789012345678901234567890123456789012345678901234567890123";
        Map<String, Object> string64charMap = new HashMap<>();
        string64charMap.put(
                MediaTestConstants.EventDataKeys.StateInfo.STATE_NAME_KEY, string64Char);
        stateInfo = StateInfo.fromObjectMap(string64charMap);

        assertEquals(stateInfo.getStateName(), string64Char);
    }

    @Test
    public void StateInfo_create_fail_withMissingRequiredInfo() throws CloneFailedException {
        List<String> requiredKeys = new ArrayList<>();

        requiredKeys.add(MediaTestConstants.EventDataKeys.StateInfo.STATE_NAME_KEY);

        for (String key : requiredKeys) {
            Map<String, Object> info = EventDataUtils.clone(validStateInfo);
            info.remove(key);
            assertNull(StateInfo.fromObjectMap(info));
        }
    }

    @Test
    public void StateInfo_equals_sameParameters_pass() {
        StateInfo stateInfo = StateInfo.fromObjectMap(validStateInfo);

        StateInfo stateInfo1 = StateInfo.create("fullscreen");
        assertEquals(stateInfo, stateInfo1);

        StateInfo stateInfo2 = StateInfo.create("cc");
        assertNotEquals(stateInfo, stateInfo2);
    }

    @Test
    public void StateInfo_toVariantMap() {
        Map<String, Object> stateInfoVariantMap = new HashMap<>();
        stateInfoVariantMap.put(
                MediaTestConstants.EventDataKeys.StateInfo.STATE_NAME_KEY, "fullscreen");

        StateInfo stateInfo = StateInfo.create("fullscreen");

        assertEquals(stateInfoVariantMap, stateInfo.toObjectMap());
    }

    @Test
    public void StateInfo_toString() {

        String expected =
                "{" + " class: \"StateInfo\"," + " stateName: " + "\"" + "fullscreen" + "\"" + "}";
        StateInfo stateInfo = StateInfo.create("fullscreen");
        String actual = stateInfo.toString();

        assertEquals(expected, actual);
    }
}

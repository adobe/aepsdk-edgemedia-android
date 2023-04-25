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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.adobe.marketing.mobile.AdobeCallback;
import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.edge.media.Media;
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMMediaEvent;
import com.adobe.marketing.mobile.util.CloneFailedException;
import com.adobe.marketing.mobile.util.DataReader;
import com.adobe.marketing.mobile.util.EventDataUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class MediaEventTrackerTests {
    Map<String, String> emptyMetadata;
    Map<String, Object> emptyParams;
    MediaInfo mediaInfo, mediaInfoDefaultPreroll, mediaInfoCustomPreroll;
    AdBreakInfo adBreakInfo1, adBreakInfo2;
    AdInfo adInfo1, adInfo2;
    ChapterInfo chapterInfo1, chapterInfo2;
    QoEInfo qoeInfo;
    StateInfo state1;
    Map<String, String> metadata;
    TestableMediaTrackerEventGenerator testableMediaTrackerEventGenerator;
    private final List<Event> generatedMediaEvents = new ArrayList<>();
    MediaEventTracker tracker;
    Map<String, String> denylistMetadata;
    Map<String, String> cleanedMetadata;

    private static final String KEY_INFO = "key_info";
    private static final String KEY_METADATA = "key_metadata";
    private static final String KEY_EVENT_TS = "key_eventts";
    private static final String KEY_SESSIONID = "key_sessionid";

    private final MediaEventProcessor mockEventProcessor;
    private final Map<String, List<XDMMediaEvent>> mockSessionMap;

    private int currSessionId = 0;

    public MediaEventTrackerTests() {

        emptyMetadata = new HashMap<>();
        emptyParams = new HashMap<>();

        boolean defaultGranularAdTrackingEnabled = false;
        mediaInfoCustomPreroll =
                MediaInfo.create(
                        "id",
                        "name",
                        "aod",
                        MediaType.Audio,
                        60,
                        false,
                        5000,
                        defaultGranularAdTrackingEnabled);
        mediaInfo =
                MediaInfo.create(
                        "id",
                        "name",
                        "aod",
                        MediaType.Audio,
                        60,
                        false,
                        0,
                        defaultGranularAdTrackingEnabled);
        mediaInfoDefaultPreroll = MediaInfo.create("id", "name", "aod", MediaType.Audio, 60, false);

        metadata = new HashMap<>();
        metadata.put("k1", "v1");

        denylistMetadata = new HashMap<>();
        denylistMetadata.put("vAlid_keY.12", "valid_value.@$%!2");
        denylistMetadata.put("inv@lidKey", "validValue123");
        denylistMetadata.put("valid_key", "");
        denylistMetadata.put("", "valid_@_Value");
        denylistMetadata.put("invalidKey!", "valid_value");
        denylistMetadata.put("invalidKey^", "valid_value");
        denylistMetadata.put("validKey", null);
        denylistMetadata.put(null, "validvalue");

        cleanedMetadata = new HashMap<>();
        cleanedMetadata.put("vAlid_keY.12", "valid_value.@$%!2");
        cleanedMetadata.put("valid_key", "");

        adBreakInfo1 = AdBreakInfo.create("adbreak1", 1, 10.0);
        adBreakInfo2 = AdBreakInfo.create("adbreak2", 2, 20.0);

        adInfo1 = AdInfo.create("ad1", "adname1", 1, 15.0);
        adInfo2 = AdInfo.create("ad2", "adname2", 2, 15.0);

        chapterInfo1 = ChapterInfo.create("chapter1", 1, 10.0, 30.0);
        chapterInfo2 = ChapterInfo.create("chapter2", 2, 10.0, 30.0);

        qoeInfo = QoEInfo.create(1.1, 2.2, 3.3, 4.4);

        state1 = StateInfo.create("mute");

        Map<String, Object> config = new HashMap<>();

        testableMediaTrackerEventGenerator =
                new TestableMediaTrackerEventGenerator(
                        "tracker0",
                        (Event event) -> {
                            generatedMediaEvents.add(event);
                        });
        mockEventProcessor = Mockito.mock(MediaEventProcessor.class);
        mockSessionMap = new HashMap<>();
        tracker = new MediaEventTracker(mockEventProcessor, config);

        setMocks();
    }

    private void setMocks() {
        Mockito.doAnswer(
                        new Answer<String>() {
                            @Override
                            public String answer(InvocationOnMock invocation) throws Throwable {
                                return String.valueOf(++currSessionId);
                            }
                        })
                .when(mockEventProcessor)
                .createSession();

        Mockito.doAnswer(
                        new Answer<Void>() {
                            @Override
                            public Void answer(final InvocationOnMock invocation) {
                                final Object[] args = invocation.getArguments();
                                addEventToMockSessionMap((String) args[0], (XDMMediaEvent) args[1]);
                                return null;
                            }
                        })
                .when(mockEventProcessor)
                .processEvent(Mockito.anyString(), Mockito.any());
    }

    private void addEventToMockSessionMap(String sessionId, XDMMediaEvent event) {
        List<XDMMediaEvent> eventList = mockSessionMap.getOrDefault(sessionId, new ArrayList<>());
        eventList.add(event);
        mockSessionMap.put(sessionId, eventList);
    }

    boolean trackerHandleAPI() {
        return tracker.track(getLastGeneratedEvent());
    }

    boolean trackerHandleAPI(Event event) {
        return tracker.track(event);
    }

    Event createEventWithModifiedData(Event event, AdobeCallback<Map<String, Object>> callback) {
        try {
            Map<String, Object> map = EventDataUtils.clone(event.getEventData());
            callback.call(map);

            return new Event.Builder(
                            "Media::CreateTrackerResponse",
                            MediaTestConstants.Media.EVENT_TYPE,
                            MediaTestConstants.Media.EVENT_SOURCE_TRACKER_REQUEST)
                    .setEventData(map)
                    .build();
        } catch (CloneFailedException ex) {
            fail();
        }

        return null;
    }

    Map<String, Object> getEventContextData(Event event) {
        Map<String, Object> eventData = event.getEventData();

        String eventName =
                DataReader.optString(
                        eventData, MediaInternalConstants.EventDataKeys.Tracker.EVENT_NAME, null);
        if (eventName == null) {
            return null;
        }

        MediaRuleName rule = MediaRuleName.create(eventName);
        if (rule == MediaRuleName.Invalid) {
            return null;
        }

        Map<String, Object> context = new HashMap<>();

        Object eventTS =
                eventData.get(MediaInternalConstants.EventDataKeys.Tracker.EVENT_TIMESTAMP);
        if (eventTS != null) {
            context.put(KEY_EVENT_TS, eventTS);
        } else {
            return null;
        }

        String sessionId =
                DataReader.optString(
                        eventData, MediaInternalConstants.EventDataKeys.Tracker.SESSION_ID, null);
        if (sessionId != null) {
            context.put(KEY_SESSIONID, sessionId);
        }

        Object params = eventData.get(MediaInternalConstants.EventDataKeys.Tracker.EVENT_PARAM);
        if (params != null) {
            context.put(KEY_INFO, params);
        }

        Map<String, String> metadata =
                DataReader.optStringMap(
                        eventData,
                        MediaInternalConstants.EventDataKeys.Tracker.EVENT_METADATA,
                        null);

        if (metadata != null) {
            context.put(KEY_METADATA, metadata);
        }
        return context;
    }

    private Event getLastGeneratedEvent() {
        return generatedMediaEvents.stream().reduce((first, second) -> second).orElse(null);
    }

    @Before
    public void setup() {
        generatedMediaEvents.clear();
    }

    @Test
    public void test_trackEvent_handleAbsentEventName() {
        testableMediaTrackerEventGenerator.trackSessionStart(mediaInfo.toObjectMap(), metadata);
        Event event = getLastGeneratedEvent();

        Event modifiedEvent =
                createEventWithModifiedData(
                        event,
                        map -> map.remove(MediaTestConstants.EventDataKeys.Tracker.EVENT_NAME));

        assertFalse(tracker.track(modifiedEvent));
    }

    @Test
    public void test_trackEvent_handleIncorrectEventName() {
        testableMediaTrackerEventGenerator.trackSessionStart(mediaInfo.toObjectMap(), metadata);
        Event event = getLastGeneratedEvent();

        Event modifiedEvent =
                createEventWithModifiedData(
                        event,
                        map ->
                                map.put(
                                        MediaTestConstants.EventDataKeys.Tracker.EVENT_NAME,
                                        "incorrectEventName"));
        assertFalse(tracker.track(modifiedEvent));

        modifiedEvent =
                createEventWithModifiedData(
                        event,
                        map -> map.put(MediaTestConstants.EventDataKeys.Tracker.EVENT_NAME, 1));
        assertFalse(tracker.track(modifiedEvent));
    }

    @Test
    public void test_trackEvent_handleAbsentTimeStamp() {
        testableMediaTrackerEventGenerator.trackSessionStart(mediaInfo.toObjectMap(), metadata);
        Event event = getLastGeneratedEvent();

        Event modifiedEvent =
                createEventWithModifiedData(
                        event,
                        map ->
                                map.remove(
                                        MediaTestConstants.EventDataKeys.Tracker.EVENT_TIMESTAMP));
        assertFalse(tracker.track(modifiedEvent));
    }

    @Test
    public void test_trackSessionStart_FailOtherAPIsBeforeStart() {
        testableMediaTrackerEventGenerator.trackPlay();
        assertFalse(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackPause();
        assertFalse(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackComplete();
        assertFalse(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackSessionEnd();
        assertFalse(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackError("error-id");
        assertFalse(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.BitrateChange, emptyParams, emptyMetadata);
        assertFalse(trackerHandleAPI());

        testableMediaTrackerEventGenerator.updateCurrentPlayhead(1.1);
        assertFalse(trackerHandleAPI());

        testableMediaTrackerEventGenerator.updateQoEObject(qoeInfo.toObjectMap());
        assertFalse(trackerHandleAPI());
    }

    @Test
    public void test_trackSessionStart_pass() {
        testableMediaTrackerEventGenerator.trackSessionStart(mediaInfo.toObjectMap(), metadata);
        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_trackSessionStart_withdenylistMetadata_pass() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), denylistMetadata);
        assertTrue(trackerHandleAPI());

        Map<String, String> ret = tracker.cleanMetadata(denylistMetadata);
        assertEquals(ret, cleanedMetadata);
    }

    @Test
    public void test_trackSessionStart_failIfAlreadyInSession() {
        testableMediaTrackerEventGenerator.trackSessionStart(mediaInfo.toObjectMap(), metadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackSessionStart(mediaInfo.toObjectMap(), metadata);
        assertFalse(trackerHandleAPI());
    }

    @Test
    public void test_trackSessionStart_FailInvalidMediaInfo() {
        testableMediaTrackerEventGenerator.trackSessionStart(emptyParams, emptyMetadata);
        assertFalse(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackSessionStart(null, null);
        assertFalse(trackerHandleAPI());
    }

    @Test
    public void test_trackSessionStart_nullMediaInfo_fail() {
        testableMediaTrackerEventGenerator.trackSessionStart(null, null);
        assertFalse(trackerHandleAPI());
    }

    @Test
    public void test_trackSessionEnd_Pass() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackSessionEnd();
        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_trackSessionEnd_failOtherAPIsAfterEnd() {
        testableMediaTrackerEventGenerator.trackSessionStart(mediaInfo.toObjectMap(), metadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackSessionEnd();
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackPlay();
        assertFalse(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackPause();
        assertFalse(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackComplete();
        assertFalse(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackSessionEnd();
        assertFalse(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackError("errorid");
        assertFalse(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.BitrateChange, emptyParams, emptyMetadata);
        assertFalse(trackerHandleAPI());

        testableMediaTrackerEventGenerator.updateCurrentPlayhead(1.1);
        assertFalse(trackerHandleAPI());

        testableMediaTrackerEventGenerator.updateQoEObject(qoeInfo.toObjectMap());
        assertFalse(trackerHandleAPI());
    }

    @Test
    public void test_trackComplete_pass() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackComplete();
        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_trackComplete_failOtherAPIsAfterComplete() {
        testableMediaTrackerEventGenerator.trackSessionStart(mediaInfo.toObjectMap(), metadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackComplete();
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackPlay();
        assertFalse(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackPause();
        assertFalse(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackComplete();
        assertFalse(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackSessionEnd();
        assertFalse(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackError("errorid");
        assertFalse(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.BitrateChange, emptyParams, emptyMetadata);
        assertFalse(trackerHandleAPI());

        testableMediaTrackerEventGenerator.updateCurrentPlayhead(1.1);
        assertFalse(trackerHandleAPI());

        testableMediaTrackerEventGenerator.updateQoEObject(qoeInfo.toObjectMap());
        assertFalse(trackerHandleAPI());
    }

    @Test
    public void test_trackError_pass() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackError("error_id");
        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_trackError_withNullErrorID_fail() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackError(null);
        assertFalse(trackerHandleAPI());
    }

    @Test
    public void test_trackError_withEmptyID_fail() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackError("");
        assertFalse(trackerHandleAPI());
    }

    @Test
    public void test_trackPlay_pass() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackPlay();
        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_trackPlay_pass_whileBuffering() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.BufferStart, emptyParams, emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackPlay();
        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_trackPlay_pass_whileSeeking() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.SeekStart, emptyParams, emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackPlay();
        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_trackPause_pass() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackPause();
        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_trackPause_fail_whileBuffering() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.BufferStart, emptyParams, emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackPause();
        assertFalse(trackerHandleAPI());
    }

    @Test
    public void test_trackPause_fail_whileSeeking() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.SeekStart, emptyParams, emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackPause();
        assertFalse(trackerHandleAPI());
    }

    @Test
    public void test_trackEventBitrateChange_pass() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.BitrateChange, emptyParams, emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(Media.Event.BitrateChange, null, null);
        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_trackEventBufferStart_pass() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.BufferStart, emptyParams, emptyMetadata);
        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_trackEventBufferStart_fail_whileBuffering() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.BufferStart, emptyParams, emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.BufferStart, emptyParams, emptyMetadata);
        assertFalse(trackerHandleAPI());
    }

    @Test
    public void test_trackEventBufferStart_fail_whileSeeking() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.SeekStart, emptyParams, emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.BufferStart, emptyParams, emptyMetadata);
        assertFalse(trackerHandleAPI());
    }

    @Test
    public void test_trackEventBufferComplete_pass() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.BufferStart, emptyParams, emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.BufferComplete, emptyParams, emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(Media.Event.BufferStart, null, null);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(Media.Event.BufferComplete, null, null);
        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_trackEventBufferComplete_notInBuffering_fail() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.BufferComplete, emptyParams, emptyMetadata);
        assertFalse(trackerHandleAPI());
    }

    @Test
    public void test_trackEventSeekStart_pass() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.SeekStart, emptyParams, emptyMetadata);
        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_trackEventSeekStart_fail_whileBuffering() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.BufferStart, emptyParams, emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.SeekStart, emptyParams, emptyMetadata);
        assertFalse(trackerHandleAPI());
    }

    @Test
    public void test_trackEventSeekStart_fail_whileSeeking() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.SeekStart, emptyParams, emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.SeekStart, emptyParams, emptyMetadata);
        assertFalse(trackerHandleAPI());
    }

    @Test
    public void test_trackEventSeekComplete_pass() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.SeekStart, emptyParams, emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.SeekComplete, emptyParams, emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(Media.Event.SeekStart, null, null);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(Media.Event.SeekComplete, null, null);
        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_trackEventSeekComplete_notInSeeking_fail() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.SeekComplete, emptyParams, emptyMetadata);
        assertFalse(trackerHandleAPI());
    }

    @Test
    public void test_trackEventAdBreakStart_pass() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdBreakStart, adBreakInfo1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_trackEventAdBreakStart_invalidInfo_fail() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdBreakStart, emptyParams, emptyMetadata);
        assertFalse(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(Media.Event.AdBreakStart, null, null);
        assertFalse(trackerHandleAPI());
    }

    @Test
    public void test_trackEventAdBreakStart_duplicateInfo_fail() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdBreakStart, adBreakInfo1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdBreakStart, adBreakInfo1.toObjectMap(), emptyMetadata);
        assertFalse(trackerHandleAPI());
    }

    @Test
    public void test_trackEventAdBreakStart_replaceAdBreakOutsideAd() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdBreakStart, adBreakInfo1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdBreakStart, adBreakInfo2.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_trackEventAdBreakStart_replaceAdBreaksInsideAd() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdBreakStart, adBreakInfo1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdStart, adInfo1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdBreakStart, adBreakInfo2.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_trackEventAdBreakComplete_pass() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdBreakStart, adBreakInfo1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdBreakComplete, emptyParams, emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdBreakStart, adBreakInfo1.toObjectMap(), null);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(Media.Event.AdBreakComplete, null, null);
        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_trackEventAdBreakComplete_withoutAdBreakStart_fail() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdBreakComplete, emptyParams, emptyMetadata);
        assertFalse(trackerHandleAPI());
    }

    @Test
    public void test_trackEventAdBreakComplete_invalidInfo_fail() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdBreakStart, emptyParams, emptyMetadata);
        assertFalse(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(Media.Event.AdBreakStart, null, null);
        assertFalse(trackerHandleAPI());
    }

    @Test
    public void test_trackEventAdBreakComplete_inAd_pass() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdBreakStart, adBreakInfo1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdStart, adInfo1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdBreakComplete, emptyParams, emptyMetadata);
        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_trackEventAdStart_pass() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdBreakStart, adBreakInfo1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdStart, adInfo1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_trackEventAdStart_nullMetadata_pass() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdBreakStart, adBreakInfo1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdStart, adInfo1.toObjectMap(), null);
        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_trackEventAdStart_withdenylistMetadata_pass() {
        testableMediaTrackerEventGenerator.trackSessionStart(mediaInfo.toObjectMap(), metadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdBreakStart, adBreakInfo1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdStart, adInfo1.toObjectMap(), denylistMetadata);
        assertTrue(trackerHandleAPI());

        Map<String, String> ret = tracker.cleanMetadata(denylistMetadata);
        assertEquals(ret, cleanedMetadata);
    }

    @Test
    public void test_trackEventAdStart_withoutAdBreakStart_fail() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdStart, adInfo1.toObjectMap(), emptyMetadata);
        assertFalse(trackerHandleAPI());
    }

    @Test
    public void test_trackEventAdStart_withInvalidAdInfo_fail() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdBreakStart, adBreakInfo1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdStart, emptyParams, emptyMetadata);
        assertFalse(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(Media.Event.AdStart, null, null);
        assertFalse(trackerHandleAPI());
    }

    @Test
    public void test_trackEventAdStart_withDuplicateAdInfo_fail() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdBreakStart, adBreakInfo1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdStart, adInfo1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdStart, adInfo1.toObjectMap(), emptyMetadata);
        assertFalse(trackerHandleAPI());
    }

    @Test
    public void test_trackEventAdStart_replaceAd_pass() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdBreakStart, adBreakInfo1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdStart, adInfo1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdStart, adInfo2.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_trackEventAdComplete_withoutAdBreakStart_fail() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdComplete, emptyParams, emptyMetadata);
        assertFalse(trackerHandleAPI());
    }

    @Test
    public void test_trackEventAdComplete_withoutAdStart_fail() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdBreakStart, adBreakInfo1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdComplete, emptyParams, emptyMetadata);
        assertFalse(trackerHandleAPI());
    }

    @Test
    public void test_trackEventAdComplete_pass() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdBreakStart, adBreakInfo1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdStart, adInfo1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdComplete, emptyParams, emptyMetadata);
        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_trackEventAdComplete_nullInfoMetadata_pass() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdBreakStart, adBreakInfo1.toObjectMap(), null);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdStart, adInfo1.toObjectMap(), null);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(Media.Event.AdComplete, null, null);
        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_trackEventAdSkip_withoutAdBreakStart_fail() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdSkip, emptyParams, emptyMetadata);
        assertFalse(trackerHandleAPI());
    }

    @Test
    public void test_trackEventAdSkip_withoutAdStart_fail() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdBreakStart, adBreakInfo1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdSkip, emptyParams, emptyMetadata);
        assertFalse(trackerHandleAPI());
    }

    @Test
    public void test_trackEventAdSkip_afterAdComplete_fail() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdBreakStart, adBreakInfo1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdStart, adInfo1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdComplete, emptyParams, emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdSkip, emptyParams, emptyMetadata);
        assertFalse(trackerHandleAPI());
    }

    @Test
    public void test_trackEventAdSkip_pass() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdBreakStart, adBreakInfo1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdStart, adInfo1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdSkip, emptyParams, emptyMetadata);
        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_trackEventAdSkip_nullInfoMetadata_pass() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdBreakStart, adBreakInfo1.toObjectMap(), null);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdStart, adInfo1.toObjectMap(), null);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(Media.Event.AdSkip, null, null);
        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_trackEventChapterStart_pass() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.ChapterStart, chapterInfo1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_trackEventChapterStart_withInvalidInfo_fail() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.ChapterStart, emptyParams, emptyMetadata);
        assertFalse(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(Media.Event.ChapterStart, null, null);
        assertFalse(trackerHandleAPI());
    }

    @Test
    public void test_trackEventChapterStart_withDuplicateInfo_fail() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.ChapterStart, chapterInfo1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.ChapterStart, chapterInfo1.toObjectMap(), emptyMetadata);
        assertFalse(trackerHandleAPI());
    }

    @Test
    public void test_trackEventChapterStart_replaceChapter() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.ChapterStart, chapterInfo1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.ChapterStart, chapterInfo2.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_trackEventChapterStart_withdenylistMetadata_Pass() {
        testableMediaTrackerEventGenerator.trackSessionStart(mediaInfo.toObjectMap(), metadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.ChapterStart, chapterInfo1.toObjectMap(), denylistMetadata);
        assertTrue(trackerHandleAPI());

        Map<String, String> ret = tracker.cleanMetadata(denylistMetadata);
        assertEquals(ret, cleanedMetadata);
    }

    @Test
    public void test_trackEventChapterComplete_withoutChapterStart_fail() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.ChapterComplete, emptyParams, emptyMetadata);
        assertFalse(trackerHandleAPI());
    }

    @Test
    public void test_trackEventChapterComplete_pass() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.ChapterStart, chapterInfo1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.ChapterComplete, emptyParams, emptyMetadata);
        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_trackEventChapterComplete_nullParamMetadata_pass() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.ChapterStart, chapterInfo1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(Media.Event.ChapterComplete, null, null);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.ChapterStart, chapterInfo1.toObjectMap(), null);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(Media.Event.ChapterComplete, null, null);
        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_trackEventChapterSkip_withoutChapterStart_fail() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.ChapterSkip, emptyParams, emptyMetadata);
        assertFalse(trackerHandleAPI());
    }

    @Test
    public void test_trackEventChapterSkip_afterChapterComplete_fail() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.ChapterStart, chapterInfo1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.ChapterComplete, emptyParams, emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.ChapterSkip, emptyParams, emptyMetadata);
        assertFalse(trackerHandleAPI());
    }

    @Test
    public void test_trackEventChapterSkip_pass() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.ChapterStart, chapterInfo1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.ChapterSkip, emptyParams, emptyMetadata);

        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.ChapterStart, chapterInfo1.toObjectMap(), null);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(Media.Event.ChapterSkip, null, null);
        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_trackEventChapterSkip_nullParamMetadata_pass() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.ChapterStart, chapterInfo1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(Media.Event.ChapterSkip, null, null);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.ChapterStart, chapterInfo1.toObjectMap(), null);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(Media.Event.ChapterSkip, null, null);
        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_updatePlayhead_pass() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.updateCurrentPlayhead(12);
        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_updateQoEInfo_pass() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.updateQoEObject(qoeInfo.toObjectMap());
        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_updateQoEInfo_withInvalidInfo_fail() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.updateQoEObject(emptyParams);
        assertFalse(trackerHandleAPI());

        testableMediaTrackerEventGenerator.updateQoEObject(null);
        assertFalse(trackerHandleAPI());
    }

    @Test
    public void test_trackStateStart_pass() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.StateStart, state1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_trackStateStart_withInvalidStateInfo_fail() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.StateStart, adBreakInfo1.toObjectMap(), emptyMetadata);
        assertFalse(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.StateStart, emptyParams, emptyMetadata);
        assertFalse(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(Media.Event.StateStart, null, null);
        assertFalse(trackerHandleAPI());
    }

    // Attempt to track same state name twice
    @Test
    public void test_trackStateStart_fail() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.StateStart, state1.toObjectMap(), emptyMetadata);

        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.StateStart, state1.toObjectMap(), emptyMetadata);

        assertFalse(trackerHandleAPI());
    }

    // Attempt to track an 11th state, should fail
    @Test
    public void test_trackStateStart_reachLimit() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        for (int i = 0; i < MediaTestConstants.EventDataKeys.StateInfo.STATE_LIMIT; i++) {
            StateInfo stateInfo = StateInfo.create(Integer.toString(i));

            testableMediaTrackerEventGenerator.trackEvent(
                    Media.Event.StateStart, stateInfo.toObjectMap(), emptyMetadata);

            assertTrue(trackerHandleAPI());
        }

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.StateStart, state1.toObjectMap(), emptyMetadata);

        assertFalse(trackerHandleAPI());
    }

    // Track 10 states, track 11th fail, end all 10 states, start all 10 states again
    @Test
    public void test_trackState_ReachLimit_And_Retrack() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        for (int i = 0; i < MediaTestConstants.EventDataKeys.StateInfo.STATE_LIMIT; i++) {
            StateInfo stateInfo = StateInfo.create(Integer.toString(i));

            testableMediaTrackerEventGenerator.trackEvent(
                    Media.Event.StateStart, stateInfo.toObjectMap(), emptyMetadata);

            assertTrue(trackerHandleAPI());
        }

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.StateStart, state1.toObjectMap(), emptyMetadata);

        assertFalse(trackerHandleAPI());

        for (int i = 0; i < MediaTestConstants.EventDataKeys.StateInfo.STATE_LIMIT; i++) {
            StateInfo stateInfo = StateInfo.create(Integer.toString(i));

            testableMediaTrackerEventGenerator.trackEvent(
                    Media.Event.StateEnd, stateInfo.toObjectMap(), emptyMetadata);

            assertTrue(trackerHandleAPI());

            testableMediaTrackerEventGenerator.trackEvent(
                    Media.Event.StateStart, stateInfo.toObjectMap(), emptyMetadata);

            assertTrue(trackerHandleAPI());
        }
    }

    @Test
    public void test_trackStateEnd_pass() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.StateStart, state1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.StateEnd, state1.toObjectMap(), emptyMetadata);

        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_trackStateEnd_withInvalidStateInfo_fail() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.StateEnd, adBreakInfo1.toObjectMap(), emptyMetadata);
        assertFalse(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.StateEnd, emptyParams, emptyMetadata);
        assertFalse(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(Media.Event.StateEnd, null, null);
        assertFalse(trackerHandleAPI());
    }

    // Attempt to end state without a corresponding start
    @Test
    public void test_trackStateEnd_fail() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.StateEnd, state1.toObjectMap(), emptyMetadata);

        assertFalse(trackerHandleAPI());
    }

    // toggle a state on and off
    @Test
    public void test_trackState_toggle() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.StateStart, state1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.StateEnd, state1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.StateStart, state1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.StateEnd, state1.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());
    }

    @Test
    public void test_trackState_newSession() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        for (int i = 0; i < MediaTestConstants.EventDataKeys.StateInfo.STATE_LIMIT; i++) {
            StateInfo stateInfo = StateInfo.create(Integer.toString(i));

            testableMediaTrackerEventGenerator.trackEvent(
                    Media.Event.StateStart, stateInfo.toObjectMap(), emptyMetadata);

            assertTrue(trackerHandleAPI());
        }

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.StateStart, state1.toObjectMap(), emptyMetadata);
        assertFalse(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackSessionEnd();
        assertTrue(trackerHandleAPI());

        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        assertTrue(trackerHandleAPI());

        for (int i = 0; i < MediaTestConstants.EventDataKeys.StateInfo.STATE_LIMIT; i++) {
            StateInfo stateInfo = StateInfo.create("state" + i);

            testableMediaTrackerEventGenerator.trackEvent(
                    Media.Event.StateStart, stateInfo.toObjectMap(), emptyMetadata);

            assertTrue(trackerHandleAPI());
        }
    }

    @Test
    public void test_trackState_idleExitReTrackStates() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfo.toObjectMap(), emptyMetadata);
        trackerHandleAPI();

        testableMediaTrackerEventGenerator.trackPlay();
        trackerHandleAPI();

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.StateStart, state1.toObjectMap(), emptyMetadata);
        trackerHandleAPI();

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.SeekStart, emptyParams, emptyMetadata);
        trackerHandleAPI();

        testableMediaTrackerEventGenerator.incrementCurrentTimestamp(31 * 60 * 1000);
        testableMediaTrackerEventGenerator.updateCurrentPlayhead(0);
        trackerHandleAPI();

        assertTrue(tracker.isTrackerIdle());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.SeekComplete, emptyParams, emptyMetadata);
        trackerHandleAPI();

        assertFalse(tracker.isTrackerIdle());

        Map<String, Object> context = new HashMap<>();
        context.put(KEY_INFO, state1.toObjectMap());
        assertTrue(tracker.isInTrackedState.call(null, context));
    }

    // Preroll tests
    @Test
    public void test_preroll_disabled() {
        testableMediaTrackerEventGenerator.trackSessionStart(mediaInfo.toObjectMap(), metadata);
        trackerHandleAPI();

        assertFalse(tracker.isInPrerollInterval());
    }

    @Test
    public void test_preroll_enabled_defaultInterval() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfoDefaultPreroll.toObjectMap(), metadata);
        trackerHandleAPI();

        assertTrue(tracker.isInPrerollInterval());
    }

    @Test
    public void test_preroll_enabled_customInterval() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfoCustomPreroll.toObjectMap(), metadata);
        trackerHandleAPI();

        assertTrue(tracker.isInPrerollInterval());
    }

    @Test
    public void test_preroll_exceedDefaultInterval() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfoDefaultPreroll.toObjectMap(), metadata);
        trackerHandleAPI();

        assertTrue(tracker.isInPrerollInterval());

        testableMediaTrackerEventGenerator.incrementCurrentTimestamp(200);
        testableMediaTrackerEventGenerator.updateCurrentPlayhead(0.2);
        trackerHandleAPI();

        testableMediaTrackerEventGenerator.incrementCurrentTimestamp(200);
        testableMediaTrackerEventGenerator.updateCurrentPlayhead(0.4);
        trackerHandleAPI();

        assertFalse(tracker.isInPrerollInterval());
    }

    @Test
    public void test_preroll_exceedCustomInterval() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfoDefaultPreroll.toObjectMap(), metadata);
        trackerHandleAPI();

        assertTrue(tracker.isInPrerollInterval());

        testableMediaTrackerEventGenerator.incrementCurrentTimestamp(2000);
        testableMediaTrackerEventGenerator.updateCurrentPlayhead(2);
        trackerHandleAPI();

        testableMediaTrackerEventGenerator.incrementCurrentTimestamp(5001);
        testableMediaTrackerEventGenerator.updateCurrentPlayhead(5);
        trackerHandleAPI();

        assertFalse(tracker.isInPrerollInterval());
    }

    @Test
    public void test_preroll_gotSessionEnd() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfoDefaultPreroll.toObjectMap(), metadata);
        trackerHandleAPI();

        assertTrue(tracker.isInPrerollInterval());

        testableMediaTrackerEventGenerator.trackSessionEnd();
        trackerHandleAPI();

        assertFalse(tracker.isInPrerollInterval());
    }

    @Test
    public void test_preroll_gotComplete() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfoDefaultPreroll.toObjectMap(), metadata);
        trackerHandleAPI();

        assertTrue(tracker.isInPrerollInterval());

        testableMediaTrackerEventGenerator.trackComplete();
        trackerHandleAPI();

        assertFalse(tracker.isInPrerollInterval());
    }

    @Test
    public void test_preroll_gotAdBreakStart() {
        testableMediaTrackerEventGenerator.trackSessionStart(
                mediaInfoDefaultPreroll.toObjectMap(), metadata);
        trackerHandleAPI();

        assertTrue(tracker.isInPrerollInterval());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.AdBreakStart, adBreakInfo1.toObjectMap(), emptyMetadata);
        trackerHandleAPI();

        assertFalse(tracker.isInPrerollInterval());
    }

    @Test
    public void test_preroll_reorderNoAdBreak() {
        PrerollQueuedRule queuedRule1 =
                new PrerollQueuedRule(MediaRuleName.Play.ordinal(), new HashMap<>());
        PrerollQueuedRule queuedRule2 =
                new PrerollQueuedRule(MediaRuleName.Pause.ordinal(), new HashMap<>());
        PrerollQueuedRule queuedRule3 =
                new PrerollQueuedRule(MediaRuleName.ChapterStart.ordinal(), new HashMap<>());

        List<PrerollQueuedRule> queuedRules = new ArrayList<>();
        queuedRules.add(queuedRule1);
        queuedRules.add(queuedRule2);
        queuedRules.add(queuedRule3);

        List<PrerollQueuedRule> actualReorderedPrerollRules =
                tracker.reorderPrerollRules(queuedRules);

        assertEquals(queuedRules, actualReorderedPrerollRules);
    }

    @Test
    public void test_preroll_reorderNoPlay() {
        PrerollQueuedRule queuedRule1 =
                new PrerollQueuedRule(MediaRuleName.Pause.ordinal(), new HashMap<>());
        PrerollQueuedRule queuedRule2 =
                new PrerollQueuedRule(MediaRuleName.AdBreakStart.ordinal(), new HashMap<>());
        PrerollQueuedRule queuedRule3 =
                new PrerollQueuedRule(MediaRuleName.AdStart.ordinal(), new HashMap<>());

        List<PrerollQueuedRule> queuedRules = new ArrayList<>();
        queuedRules.add(queuedRule1);
        queuedRules.add(queuedRule2);
        queuedRules.add(queuedRule3);

        List<PrerollQueuedRule> actualReorderedPrerollRules =
                tracker.reorderPrerollRules(queuedRules);

        assertEquals(queuedRules, actualReorderedPrerollRules);
    }

    @Test
    public void test_preroll_reorderPlayBeforeAdBreak() {
        PrerollQueuedRule queuedRule1 =
                new PrerollQueuedRule(MediaRuleName.Play.ordinal(), new HashMap<>());
        PrerollQueuedRule queuedRule2 =
                new PrerollQueuedRule(MediaRuleName.AdBreakStart.ordinal(), new HashMap<>());
        PrerollQueuedRule queuedRule3 =
                new PrerollQueuedRule(MediaRuleName.AdStart.ordinal(), new HashMap<>());

        List<PrerollQueuedRule> queuedRules = new ArrayList<>();
        queuedRules.add(queuedRule1);
        queuedRules.add(queuedRule2);
        queuedRules.add(queuedRule3);

        PrerollQueuedRule expectedQueuedRule1 =
                new PrerollQueuedRule(MediaRuleName.AdBreakStart.ordinal(), new HashMap<>());
        PrerollQueuedRule expectedQueuedRule2 =
                new PrerollQueuedRule(MediaRuleName.AdStart.ordinal(), new HashMap<>());

        List<PrerollQueuedRule> expectedQueuedRules = new ArrayList<>();
        expectedQueuedRules.add(expectedQueuedRule1);
        expectedQueuedRules.add(expectedQueuedRule2);

        List<PrerollQueuedRule> actualReorderedPrerollRules =
                tracker.reorderPrerollRules(queuedRules);

        assertEquals(expectedQueuedRules, actualReorderedPrerollRules);
    }

    @Test
    public void test_idleEnter() {
        testableMediaTrackerEventGenerator.trackSessionStart(mediaInfo.toObjectMap(), metadata);
        trackerHandleAPI();

        testableMediaTrackerEventGenerator.trackPause();
        trackerHandleAPI();

        assertFalse(tracker.isTrackerIdle());

        testableMediaTrackerEventGenerator.incrementCurrentTimestamp((20 * 60 * 1000));
        assertFalse(tracker.isTrackerIdle());

        testableMediaTrackerEventGenerator.incrementCurrentTimestamp((11 * 60 * 1000));
        testableMediaTrackerEventGenerator.trackPause();
        trackerHandleAPI();

        assertTrue(tracker.isTrackerIdle());
    }

    @Test
    public void test_idleExit() {
        testableMediaTrackerEventGenerator.trackSessionStart(mediaInfo.toObjectMap(), metadata);
        trackerHandleAPI();

        testableMediaTrackerEventGenerator.trackPlay();
        trackerHandleAPI();

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.SeekStart, emptyParams, emptyMetadata);
        trackerHandleAPI();

        assertFalse(tracker.isTrackerIdle());

        testableMediaTrackerEventGenerator.incrementCurrentTimestamp((31 * 60 * 1000));
        testableMediaTrackerEventGenerator.updateCurrentPlayhead(0);
        trackerHandleAPI();

        assertTrue(tracker.isTrackerIdle());

        testableMediaTrackerEventGenerator.trackEvent(
                Media.Event.SeekComplete, emptyParams, emptyMetadata);
        trackerHandleAPI();

        assertFalse(tracker.isTrackerIdle());
    }

    @Test
    public void test_getMetadata() {
        Map<String, Object> context = new HashMap<>();

        assertNull(tracker.getMetadata(null));
        assertNull(tracker.getMetadata(context));

        context.put(KEY_METADATA, null);
        assertNull(tracker.getMetadata(context));

        context.put(KEY_METADATA, "");
        assertNull(tracker.getMetadata(context));

        context.put(KEY_METADATA, metadata);
        Map<String, String> actualMetadata = tracker.getMetadata(context);
        assertNotNull(actualMetadata);
        assertEquals(metadata, actualMetadata);
    }

    @Test
    public void test_getPlayhead() {
        Map<String, Object> context = new HashMap<>();

        assertEquals(-1, tracker.getPlayhead(null), 0.0);
        assertEquals(-1, tracker.getPlayhead(context), 0.0);

        context.put(KEY_INFO, null);
        assertEquals(-1, tracker.getPlayhead(context), 0.0);

        context.put(KEY_INFO, "");
        assertEquals(-1, tracker.getPlayhead(context), 0.0);

        Map<String, Object> playheadMap = new HashMap<>();
        context.put(KEY_INFO, playheadMap);
        assertEquals(-1, tracker.getPlayhead(context), 0.0);

        playheadMap.put(MediaTestConstants.EventDataKeys.Tracker.PLAYHEAD, null);
        context.put(KEY_INFO, playheadMap);
        assertEquals(-1, tracker.getPlayhead(context), 0.0);

        playheadMap.put(MediaTestConstants.EventDataKeys.Tracker.PLAYHEAD, "");
        context.put(KEY_INFO, playheadMap);
        assertEquals(-1, tracker.getPlayhead(context), 0.0);

        playheadMap.put(MediaTestConstants.EventDataKeys.Tracker.PLAYHEAD, 1D);
        context.put(KEY_INFO, playheadMap);
        assertEquals(1, tracker.getPlayhead(context), 0.0);
    }

    @Test
    public void test_getRefTS() {
        Map<String, Object> context = new HashMap<>();

        assertEquals(-1, tracker.getRefTS(null), 0.0);
        assertEquals(-1, tracker.getRefTS(context), 0.0);

        context.put(KEY_EVENT_TS, null);
        assertEquals(-1, tracker.getRefTS(context), 0.0);

        context.put(KEY_EVENT_TS, "");
        assertEquals(-1, tracker.getRefTS(context), 0.0);

        context.put(KEY_EVENT_TS, 100);
        assertEquals(100, tracker.getRefTS(context), 0.0);
    }

    @Test
    public void test_getSessionId() {
        Map<String, Object> context = new HashMap<>();

        assertNull(tracker.getSessionId(null));
        assertNull(tracker.getSessionId(context));

        context.put(KEY_SESSIONID, null);
        assertNull(tracker.getSessionId(context));

        context.put(KEY_SESSIONID, "");
        assertEquals("", tracker.getSessionId(context));

        context.put(KEY_SESSIONID, "12345");
        assertEquals("12345", tracker.getSessionId(context));

        context.put(KEY_SESSIONID, 100L);
        assertNull(tracker.getSessionId(context));
    }

    @Test
    public void test_getError() {
        Map<String, Object> context = new HashMap<>();

        assertNull(tracker.getError(null));
        assertNull(tracker.getError(context));

        context.put(KEY_INFO, null);
        assertNull(tracker.getError(context));

        context.put(KEY_INFO, "");
        assertNull(tracker.getError(context));

        Map<String, Object> errorMap = new HashMap<>();
        context.put(KEY_INFO, errorMap);
        assertNull(tracker.getError(context));

        errorMap = new HashMap<>();
        errorMap.put(MediaTestConstants.EventDataKeys.ErrorInfo.ID, null);
        context.put(KEY_INFO, errorMap);
        assertNull(tracker.getError(context));

        errorMap.put(MediaTestConstants.EventDataKeys.ErrorInfo.ID, 1.0);
        context.put(KEY_INFO, errorMap);
        assertNull(tracker.getError(context));

        errorMap.put(MediaTestConstants.EventDataKeys.ErrorInfo.ID, "test-error");
        context.put(KEY_INFO, errorMap);
        assertNotNull(tracker.getError(context));

        assertEquals("test-error", tracker.getError(context));
    }

    @Test
    public void test_doesNotRestartIdleSession_after24hrTimeout() {
        testableMediaTrackerEventGenerator.setCurrentTimestamp(System.currentTimeMillis());
        testableMediaTrackerEventGenerator.trackSessionStart(mediaInfo.toObjectMap(), metadata);
        trackerHandleAPI();

        testableMediaTrackerEventGenerator.trackPause();
        trackerHandleAPI();
        // sessionStart, pause (sessionId = "1")
        assertEquals(2, mockSessionMap.get("1").size());

        testableMediaTrackerEventGenerator.incrementCurrentTimestamp(TimeUnit.DAYS.toMillis(1));
        testableMediaTrackerEventGenerator.updateCurrentPlayhead(0);
        trackerHandleAPI();

        // Assertions
        assertEquals(1, mockSessionMap.size());
        // sessionStart, pause, sessionEnd
        assertEquals(3, mockSessionMap.get("1").size());
    }

    @Test
    public void test_restartActiveSession_after24hrTimeout() {
        testableMediaTrackerEventGenerator.setCurrentTimestamp(System.currentTimeMillis());
        testableMediaTrackerEventGenerator.trackSessionStart(mediaInfo.toObjectMap(), metadata);
        trackerHandleAPI();

        testableMediaTrackerEventGenerator.trackPlay();
        trackerHandleAPI();

        // sessionStart, play (sessionId = "1")
        assertNotNull(mockSessionMap.get("1"));
        assertEquals(2, mockSessionMap.get("1").size());

        testableMediaTrackerEventGenerator.incrementCurrentTimestamp(TimeUnit.DAYS.toMillis(1));
        testableMediaTrackerEventGenerator.updateCurrentPlayhead(0);
        trackerHandleAPI();

        // verify sessionEnd was called for session "1"
        verify(mockEventProcessor).endSession("1");

        assertEquals(2, mockSessionMap.size());
        // sessionStart, play, sessionEnd (sessionId = "1")
        assertNotNull(mockSessionMap.get("1"));
        assertEquals(3, mockSessionMap.get("1").size());
        // sessionStart, play (sessionId = "2")
        assertNotNull(mockSessionMap.get("2"));
        assertEquals(2, mockSessionMap.get("2").size());
    }

    @Test
    public void test_closeIdleSession_after30mins() {
        testableMediaTrackerEventGenerator.setCurrentTimestamp(System.currentTimeMillis());
        testableMediaTrackerEventGenerator.trackSessionStart(mediaInfo.toObjectMap(), metadata);
        trackerHandleAPI();

        testableMediaTrackerEventGenerator.trackPause();
        trackerHandleAPI();

        // sessionStart, pause (sessionId = "1")
        assertNotNull(mockSessionMap.get("1"));
        assertEquals(2, mockSessionMap.get("1").size());

        testableMediaTrackerEventGenerator.incrementCurrentTimestamp(TimeUnit.MINUTES.toMillis(30));
        testableMediaTrackerEventGenerator.updateCurrentPlayhead(10);
        trackerHandleAPI();

        assertEquals(1, mockSessionMap.size()); // session count
        // sessionStart, pause, sessionEnd (sessionId = "1")
        assertEquals(3, mockSessionMap.get("1").size());
    }

    @Test
    public void test_doesNotCloseActiveSession_after30mins() {
        testableMediaTrackerEventGenerator.setCurrentTimestamp(System.currentTimeMillis());
        testableMediaTrackerEventGenerator.trackSessionStart(mediaInfo.toObjectMap(), metadata);
        trackerHandleAPI();

        testableMediaTrackerEventGenerator.trackPlay();
        trackerHandleAPI();

        testableMediaTrackerEventGenerator.incrementCurrentTimestamp(TimeUnit.HOURS.toMillis(1));
        testableMediaTrackerEventGenerator.updateCurrentPlayhead(10);
        trackerHandleAPI();

        assertEquals(1, mockSessionMap.size());
        verify(mockEventProcessor, times(0)).endSession("1");
    }

    @Test
    public void test_newSession_onResumingIdleTracker() {
        testableMediaTrackerEventGenerator.setCurrentTimestamp(System.currentTimeMillis());
        testableMediaTrackerEventGenerator.trackSessionStart(mediaInfo.toObjectMap(), metadata);
        trackerHandleAPI();

        testableMediaTrackerEventGenerator.trackPause();
        trackerHandleAPI();

        testableMediaTrackerEventGenerator.incrementCurrentTimestamp(TimeUnit.HOURS.toMillis(1));
        testableMediaTrackerEventGenerator.updateCurrentPlayhead(10);
        trackerHandleAPI();

        assertEquals(1, mockSessionMap.size());

        testableMediaTrackerEventGenerator.trackPlay();
        trackerHandleAPI();

        // verify session "1" was ended when resuming after idleTimeout
        verify(mockEventProcessor).endSession("1");

        assertEquals(2, mockSessionMap.size());
    }
}

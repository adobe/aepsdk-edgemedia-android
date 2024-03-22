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

package com.adobe.marketing.mobile.edge.media;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.util.DataReader;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class MediaTests {

    static final String EVENT_TYPE_MEDIA = "com.adobe.eventType.edgeMedia";
    static final String EVENT_SOURCE_TRACKER_REQUEST = "com.adobe.eventSource.createTracker";
    static final String TRACKER_ID = "trackerid";
    static final String TRACKER_EVENT_PARAM = "event.param";

    @Test
    public void test_createTrackerEventDataWithConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("booleanKey", true);
        config.put("stringKey", "string");
        config.put("integerKey", 12);
        config.put("doubleKey", 100.100);
        config.put("objectKey", new HashMap<String, String>());

        try (MockedStatic<MobileCore> mobileCoreMockedStatic =
                Mockito.mockStatic(MobileCore.class)) {
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            mobileCoreMockedStatic
                    .when(() -> MobileCore.dispatchEvent(eventCaptor.capture()))
                    .thenAnswer(Answers.RETURNS_DEFAULTS);

            MediaTracker tracker = Media.createTracker(config);
            assertNotNull(tracker);

            Event event = eventCaptor.getValue();
            assertEquals(EVENT_SOURCE_TRACKER_REQUEST, event.getSource());
            assertEquals(EVENT_TYPE_MEDIA, event.getType());

            String trackerId = DataReader.optString(event.getEventData(), TRACKER_ID, null);
            assertNotNull(trackerId);

            Map<String, Object> params = config;
            Map<String, Object> expectedEventData = new HashMap<>();
            expectedEventData.put(TRACKER_ID, trackerId);
            expectedEventData.put(TRACKER_EVENT_PARAM, params);

            assertEquals(expectedEventData, event.getEventData());
        }
    }

    @Test
    public void test_createTrackerEventData() {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic =
                Mockito.mockStatic(MobileCore.class)) {
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            mobileCoreMockedStatic
                    .when(() -> MobileCore.dispatchEvent(eventCaptor.capture()))
                    .thenAnswer(Answers.RETURNS_DEFAULTS);

            MediaTracker tracker = Media.createTracker();
            assertNotNull(tracker);

            Event event = eventCaptor.getValue();
            assertEquals(EVENT_SOURCE_TRACKER_REQUEST, event.getSource());
            assertEquals(EVENT_TYPE_MEDIA, event.getType());

            String trackerId = DataReader.optString(event.getEventData(), TRACKER_ID, null);
            assertNotNull(trackerId);

            Map<String, Object> expectedEventData = new HashMap<>();
            expectedEventData.put(TRACKER_ID, trackerId);

            assertEquals(expectedEventData, event.getEventData());
        }
    }

    @Test
    public void test_mediaObject() {
        Map<String, Object> actualObject =
                Media.createMediaObject("name", "id", 60, "vod", Media.MediaType.Video);
        Map<String, Object> expectedObject = new HashMap<>();
        {
            {
                expectedObject.put("media.name", "name");
                expectedObject.put("media.id", "id");
                expectedObject.put("media.length", 60);
                expectedObject.put("media.streamtype", "vod");
                expectedObject.put("media.type", "video");
                expectedObject.put("media.resumed", false);
                expectedObject.put("media.prerollwaitingtime", 250);
            }
        }
        ;
        assertEquals(expectedObject, actualObject);

        // invalid params
        Map<String, Object> invalidObject =
                Media.createMediaObject(null, "id", 60, "vod", Media.MediaType.Audio);
        assertEquals(new HashMap<String, Object>(), invalidObject);
    }

    @Test
    public void test_adbreakObject() {
        Map<String, Object> actualObject = Media.createAdBreakObject("name", 1, 60);
        Map<String, Object> expectedObject = new HashMap<>();
        {
            {
                expectedObject.put("adbreak.name", "name");
                expectedObject.put("adbreak.position", 1);
                expectedObject.put("adbreak.starttime", 60);
            }
        }
        ;
        assertEquals(expectedObject, actualObject);

        // invalid params
        Map<String, Object> invalidObject = Media.createAdBreakObject(null, 1, 60);
        assertEquals(new HashMap<String, Object>(), invalidObject);
    }

    @Test
    public void test_adObject() {
        Map<String, Object> actualObject = Media.createAdObject("name", "id", 1, 60);
        Map<String, Object> expectedObject = new HashMap<>();
        {
            {
                expectedObject.put("ad.name", "name");
                expectedObject.put("ad.id", "id");
                expectedObject.put("ad.position", 1);
                expectedObject.put("ad.length", 60);
            }
        }
        ;
        assertEquals(expectedObject, actualObject);

        // invalid params
        Map<String, Object> invalidObject = Media.createAdObject(null, "id", 1, 60);
        assertEquals(new HashMap<String, Object>(), invalidObject);
    }

    @Test
    public void test_chapterObject() {
        Map<String, Object> actualObject = Media.createChapterObject("name", 1, 60, 30);
        Map<String, Object> expectedObject = new HashMap<>();
        {
            {
                expectedObject.put("chapter.name", "name");
                expectedObject.put("chapter.position", 1);
                expectedObject.put("chapter.length", 60);
                expectedObject.put("chapter.starttime", 30);
            }
        }
        ;
        assertEquals(expectedObject, actualObject);

        // invalid params
        Map<String, Object> invalidObject = Media.createChapterObject(null, 1, 60, 30);
        assertEquals(new HashMap<String, Object>(), invalidObject);
    }

    @Test
    public void test_qoeObject() {
        Map<String, Object> actualObject = Media.createQoEObject(1, 2, 3, 4);
        Map<String, Object> expectedObject = new HashMap<>();
        {
            {
                expectedObject.put("qoe.bitrate", 1);
                expectedObject.put("qoe.startuptime", 2);
                expectedObject.put("qoe.fps", 3);
                expectedObject.put("qoe.droppedframes", 4);
            }
        }
        ;
        assertEquals(expectedObject, actualObject);

        // invalid params
        Map<String, Object> invalidObject = Media.createQoEObject(-1, 2, 3, 4);
        assertEquals(new HashMap<String, Object>(), invalidObject);
    }

    @Test
    public void test_stateObject() {
        Map<String, Object> actualObject = Media.createStateObject("state");
        Map<String, Object> expectedObject = new HashMap<>();
        {
            {
                expectedObject.put("state.name", "state");
            }
        }
        ;
        assertEquals(expectedObject, actualObject);

        // invalid params
        Map<String, Object> invalidObject = Media.createStateObject(null);
        assertEquals(new HashMap<String, Object>(), invalidObject);
    }
}

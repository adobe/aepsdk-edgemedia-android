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

package com.adobe.marketing.mobile;

import static junit.framework.TestCase.*;

import com.adobe.marketing.mobile.util.DataReader;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class MediaTests {

    static final String EVENT_SOURCE_TRACKER_REQUEST = "com.adobe.eventsource.media.requesttracker";
    static final String TRACKER_ID = "trackerid";
    static final String TRACKER_EVENT_PARAM = "event.param";

    @Test
    public void test_createTrackerEventDatawithConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("booleanKey", true);
        config.put("stringKey", "string");
        config.put("invalidKey1", 12);
        config.put("invalidKey2", 100.100);
        config.put("invalidKey3", new HashMap<String, String>());

        try (MockedStatic<MobileCore> mobileCoreMockedStatic =
                Mockito.mockStatic(MobileCore.class)) {
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            mobileCoreMockedStatic
                    .when(() -> MobileCore.dispatchEvent(eventCaptor.capture()))
                    .thenAnswer(Answers.RETURNS_DEFAULTS);

            MediaTracker tracker = Media.createTracker(config);

            Event event = eventCaptor.getValue();
            assertEquals(EVENT_SOURCE_TRACKER_REQUEST, event.getSource());
            assertEquals(EventType.MEDIA, event.getType());

            String trackerId = DataReader.optString(event.getEventData(), TRACKER_ID, null);
            assertNotNull(trackerId);

            Map<String, Object> params = new HashMap<>();
            params.put("booleanKey", true);
            params.put("stringKey", "string");
            Map<String, Object> expectedEventData = new HashMap<>();
            expectedEventData.put(TRACKER_ID, trackerId);
            expectedEventData.put(TRACKER_EVENT_PARAM, params);

            assertEquals(event.getEventData(), expectedEventData);
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

            Event event = eventCaptor.getValue();
            assertEquals(EVENT_SOURCE_TRACKER_REQUEST, event.getSource());
            assertEquals(EventType.MEDIA, event.getType());

            String trackerId = DataReader.optString(event.getEventData(), TRACKER_ID, null);
            assertNotNull(trackerId);

            Map<String, Object> params = new HashMap<>();
            Map<String, Object> expectedEventData = new HashMap<>();
            expectedEventData.put(TRACKER_ID, trackerId);
            expectedEventData.put(TRACKER_EVENT_PARAM, params);

            assertEquals(event.getEventData(), expectedEventData);
        }
    }
}

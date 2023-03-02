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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.EventType;
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.util.DataReader;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class MediaDispatcherSessionCreatedTests {
    private static String eventType = EventType.MEDIA;
    private static String eventSource = MediaInternalConstants.Media.EVENT_NAME_SESSION_CREATED;
    private static String name = "Media::SessionCreated";
    private MediaSessionCreatedDispatcher testDispatcher;
    private ExtensionApi extensionApi;

    @Before
    public void setUp() {
        extensionApi = mock(ExtensionApi.class);
        testDispatcher = new MediaSessionCreatedDispatcher(extensionApi);
    }

    @Test
    public void test_dispatchSessionCreated_createsValidEvent() {
        testDispatcher.dispatchSessionCreatedEvent("clientSessionId0", "mediaBackendSessionId0");

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(extensionApi, times(1)).dispatch(eventCaptor.capture());

        Event event = eventCaptor.getValue();
        assertEquals(name, event.getName());
        assertEquals(eventType, event.getType());
        assertEquals(eventSource, event.getSource());
        assertEquals(
                "clientSessionId0",
                DataReader.optString(
                        event.getEventData(),
                        MediaTestConstants.EventDataKeys.Tracker.SESSION_ID,
                        null));
        assertEquals(
                "mediaBackendSessionId0",
                DataReader.optString(
                        event.getEventData(),
                        MediaTestConstants.EventDataKeys.Tracker.BACKEND_SESSION_ID,
                        null));
    }

    @Test
    public void test_dispatchSessionCreated_createsWithoutDebugInfo() {
        testDispatcher.dispatchSessionCreatedEvent(null, "mediaBackendSessionId0");

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(extensionApi, times(1)).dispatch(eventCaptor.capture());

        Event event = eventCaptor.getValue();
        assertEquals(name, event.getName());
        assertEquals(eventType, event.getType());
        assertEquals(eventSource, event.getSource());
        assertNull(
                DataReader.optString(
                        event.getEventData(),
                        MediaTestConstants.EventDataKeys.Tracker.SESSION_ID,
                        null));
        assertEquals(
                "mediaBackendSessionId0",
                DataReader.optString(
                        event.getEventData(),
                        MediaTestConstants.EventDataKeys.Tracker.BACKEND_SESSION_ID,
                        null));
    }

    @Test
    public void test_dispatchSessionCreated_createsWithoutMediaServiceId() {
        testDispatcher.dispatchSessionCreatedEvent("clientSessionId0", null);

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(extensionApi, times(1)).dispatch(eventCaptor.capture());

        Event event = eventCaptor.getValue();
        assertEquals(name, event.getName());
        assertEquals(eventType, event.getType());
        assertEquals(eventSource, event.getSource());
        assertEquals(
                "clientSessionId0",
                DataReader.optString(
                        event.getEventData(),
                        MediaTestConstants.EventDataKeys.Tracker.SESSION_ID,
                        null));
        assertNull(
                DataReader.optString(
                        event.getEventData(),
                        MediaTestConstants.EventDataKeys.Tracker.BACKEND_SESSION_ID,
                        null));
    }

    @Test
    public void test_dispatchSessionCreated_createsWithBothInfoNull() {
        testDispatcher.dispatchSessionCreatedEvent(null, null);

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(extensionApi, times(1)).dispatch(eventCaptor.capture());

        Event event = eventCaptor.getValue();
        assertEquals(name, event.getName());
        assertEquals(eventType, event.getType());
        assertEquals(eventSource, event.getSource());
        assertNull(
                DataReader.optString(
                        event.getEventData(),
                        MediaTestConstants.EventDataKeys.Tracker.SESSION_ID,
                        null));
        assertNull(
                DataReader.optString(
                        event.getEventData(),
                        MediaTestConstants.EventDataKeys.Tracker.BACKEND_SESSION_ID,
                        null));
    }
}

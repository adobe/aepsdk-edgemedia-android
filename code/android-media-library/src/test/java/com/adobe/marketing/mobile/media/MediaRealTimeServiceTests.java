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

package com.adobe.marketing.mobile.media;

import static org.junit.Assert.*;

import com.adobe.marketing.mobile.services.Networking;
import com.adobe.marketing.mobile.services.ServiceProvider;
import org.junit.Test;
import org.mockito.Mockito;

public class MediaRealTimeServiceTests {
    class TestMediaRealTimeService extends MediaRealTimeService {

        TestMediaRealTimeService(MediaState mediaState, MediaSessionCreatedDispatcher dispatcher) {
            super(mediaState, dispatcher);
            stopTickTimer();
        }
    }

    MockMediaOfflineHits mockData;
    MediaState mediaState;
    TestMediaRealTimeService realTimeService;
    MockMediaSessionCreatedDispatcher mockDispatcherSessionCreated;

    public MediaRealTimeServiceTests() {
        ServiceProvider.getInstance().setNetworkService(Mockito.mock(Networking.class));
        mockData = new MockMediaOfflineHits();
        mediaState = new MediaState();
        mockDispatcherSessionCreated = new MockMediaSessionCreatedDispatcher(null);
        realTimeService = new TestMediaRealTimeService(mediaState, mockDispatcherSessionCreated);
    }

    @Test
    public void test_startSession_differentIds() {
        String session1 = realTimeService.startSession();
        String session2 = realTimeService.startSession();
        assertNotNull(session1);
        assertNotNull(session2);
        assertNotEquals(session1, session2);
    }

    @Test
    public void test_startSession_afterOptOut() {
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME,
                mockData.configSharedStateOptOut);

        realTimeService.notifyMobileStateChanges();

        assertNull(realTimeService.startSession());
    }

    @Test
    public void test_startSession_whenNotOptOut() {
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME,
                mockData.configSharedStateUnknown);
        realTimeService.notifyMobileStateChanges();

        String session1 = realTimeService.startSession();
        assertNotNull(session1);

        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME,
                mockData.configSharedStateOptIn);
        realTimeService.notifyMobileStateChanges();

        String session2 = realTimeService.startSession();
        assertNotNull(session2);
    }

    @Test
    public void test_processHit_nullHit() {
        try {
            realTimeService.processHit("1", null);
            realTimeService.processHit(null, mockData.sessionStart);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void test_processHit_inactiveSession() {
        try {
            realTimeService.processHit("1", mockData.sessionStart);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void test_processHit_activeSession() {
        String id = realTimeService.startSession();

        try {
            realTimeService.processHit(id, mockData.sessionStart);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void test_processHit_activeSession_afterOptOut() {
        String id = realTimeService.startSession();

        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME,
                mockData.configSharedStateOptOut);
        realTimeService.notifyMobileStateChanges();

        try {
            realTimeService.processHit(id, mockData.sessionStart);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void test_endSession_inactiveSession() {
        try {
            realTimeService.endSession("1");
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void test_endSession_activeSession() {
        String id = realTimeService.startSession();

        try {
            realTimeService.endSession(id);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void test_endSession_activeSession_afterOptOut() {
        String id = realTimeService.startSession();
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME,
                mockData.configSharedStateOptOut);
        realTimeService.notifyMobileStateChanges();

        try {
            realTimeService.endSession(id);
        } catch (Exception e) {
            fail();
        }
    }
}

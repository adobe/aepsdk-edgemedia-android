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

package com.adobe.marketing.mobile.edgemedia.internal;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.adobe.marketing.mobile.services.DeviceInforming;
import com.adobe.marketing.mobile.services.HttpConnecting;
import com.adobe.marketing.mobile.services.MockNetworkService;
import com.adobe.marketing.mobile.services.ServiceProvider;
import com.adobe.marketing.mobile.services.ServiceProviderExtension;
import java.util.HashMap;
import org.junit.Test;
import org.mockito.Mockito;

public class MediaSessionTests {
    MockMediaOfflineHits mockData;
    MediaState mediaState;
    MediaSession mediaSession;
    String mockSessionIdResponse =
            "/api/v1/sessions/0160be5e22b37eb526c085b82b782d757a328c2b0fa248cc83bd92db452722ac";
    MockMediaSessionCreatedDispatcher mockDispatcherSessionCreated;

    MockNetworkService mockNetworkService;
    DeviceInforming mockDeviceInfoServie;

    public MediaSessionTests() {
        mockNetworkService = new MockNetworkService();
        mockDeviceInfoServie = mock(DeviceInforming.class);
        ServiceProviderExtension.setDeviceInfoService(mockDeviceInfoServie);
        ServiceProvider.getInstance().setNetworkService(mockNetworkService);

        setNetworkConnectionStatus(DeviceInforming.ConnectionStatus.CONNECTED);

        mockData = new MockMediaOfflineHits();
        mediaState = new MediaState();
        mockDispatcherSessionCreated = new MockMediaSessionCreatedDispatcher(null);
        mediaSession = new MediaSession(mediaState, mockDispatcherSessionCreated);
    }

    void setNetworkConnectionStatus(DeviceInforming.ConnectionStatus status) {
        Mockito.when(mockDeviceInfoServie.getNetworkConnectionStatus()).thenReturn(status);
    }

    void expectNetworkDataAndRespond(final String jsonHits, final int responseCode) {
        expectNetworkDataAndRespond(jsonHits, responseCode, "");
    }

    void expectNetworkDataAndRespond(
            final String jsonHits, final int responseCode, String locationResponse) {
        mockNetworkService.setVerificationPredicate(
                (request) -> {
                    return mockData.compareReport(jsonHits, new String(request.getBody()));
                });

        if (responseCode == -1) {
            mockNetworkService.setResponse(null);
        } else {
            HttpConnecting connecting = mock(HttpConnecting.class);
            when(connecting.getResponseCode()).thenReturn(responseCode);
            when(connecting.getResponsePropertyValue("Location")).thenReturn(locationResponse);
            mockNetworkService.setResponse(connecting);
        }
    }

    void resetNetworkRequest() {
        mockNetworkService.reset();
    }

    boolean didSendNetworkRequest() {
        return mockNetworkService.connectAsyncCalled;
    }

    @Test
    public void test_queueHits_activeSession_pass() {
        mediaSession.queueHit(mockData.sessionStart);
        assertEquals(1, mediaSession.getQueueSize());
    }

    @Test
    public void test_queueHits_multipleHits_activeSession_pass() {
        mediaSession.queueHit(mockData.sessionStart);
        mediaSession.queueHit(mockData.play);
        mediaSession.queueHit(mockData.adBreakStart);
        mediaSession.queueHit(mockData.adStart);
        assertEquals(4, mediaSession.getQueueSize());
    }

    @Test
    public void test_queueHits_afterEnd_fail() {
        mediaSession.queueHit(mockData.sessionStart);
        assertEquals(1, mediaSession.getQueueSize());
        mediaSession.end();
        mediaSession.queueHit(mockData.sessionStart);
        assertEquals(1, mediaSession.getQueueSize());
    }

    @Test
    public void test_queueHits_afterAbort_fail() {
        mediaSession.queueHit(mockData.sessionStart);
        assertEquals(1, mediaSession.getQueueSize());
        mediaSession.abort();
        mediaSession.queueHit(mockData.sessionStart);
        assertEquals(0, mediaSession.getQueueSize());
    }

    @Test
    public void test_process_noHit() throws Exception {
        mediaSession.process();

        assertFalse(mockNetworkService.connectAsyncCalled);
    }

    @Test
    public void test_process_noNetworkNotConnection() throws Exception {
        setNetworkConnectionStatus(DeviceInforming.ConnectionStatus.DISCONNECTED);
        mediaSession.queueHit(mockData.sessionStart);

        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, mockData.configSharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, mockData.identitySharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Analytics.SHARED_STATE_NAME, mockData.analyticsSharedState);

        mediaSession.process();
        assertFalse(mockNetworkService.connectAsyncCalled);
    }

    @Test
    public void test_process_configurationSharedStateMissing() throws Exception {
        mediaSession.queueHit(mockData.sessionStart);

        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, mockData.identitySharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Analytics.SHARED_STATE_NAME, mockData.analyticsSharedState);

        mediaSession.process();
        assertFalse(mockNetworkService.connectAsyncCalled);
    }

    @Test
    public void test_process_identitySharedStateMissing() throws Exception {
        mediaSession.queueHit(mockData.sessionStart);

        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, mockData.configSharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Analytics.SHARED_STATE_NAME, mockData.analyticsSharedState);

        mediaSession.process();
        assertFalse(mockNetworkService.connectAsyncCalled);
    }

    @Test
    public void test_process_analyticsSharedStateMissing() throws Exception {
        mediaSession.queueHit(mockData.sessionStart);

        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, mockData.configSharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, mockData.identitySharedState);

        expectNetworkDataAndRespond(mockData.sessionStartJsonWithConfigurationIdentityState, 200);
        mediaSession.process();
        assertTrue(mockNetworkService.connectAsyncCalled);
    }

    @Test
    public void test_process_privacyOptOut() throws Exception {
        mediaSession.queueHit(mockData.sessionStart);

        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME,
                mockData.configSharedStateOptOut);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, mockData.identitySharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Analytics.SHARED_STATE_NAME, mockData.analyticsSharedState);

        mediaSession.process();
        assertFalse(mockNetworkService.connectAsyncCalled);
    }

    @Test
    public void test_process_privacyUnknown() throws Exception {
        mediaSession.queueHit(mockData.sessionStart);

        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME,
                mockData.configSharedStateUnknown);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, mockData.identitySharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Analytics.SHARED_STATE_NAME, mockData.analyticsSharedState);

        mediaSession.process();
        assertFalse(mockNetworkService.connectAsyncCalled);
    }

    @Test
    public void test_process_privacyOptIn() throws Exception {
        mediaSession.queueHit(mockData.sessionStart);

        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, mockData.configSharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, mockData.identitySharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Analytics.SHARED_STATE_NAME, mockData.analyticsSharedState);

        expectNetworkDataAndRespond(mockData.sessionStartJsonWithState, 404);

        mediaSession.process();
        assertTrue(didSendNetworkRequest());
    }

    @Test
    public void test_process_sessionStart_httpSuccessWithNoLocationHeader() throws Exception {
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, mockData.configSharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, mockData.identitySharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Analytics.SHARED_STATE_NAME, mockData.analyticsSharedState);

        mediaSession.queueHit(mockData.sessionStart);
        assertEquals(1, mediaSession.getQueueSize());

        expectNetworkDataAndRespond(mockData.sessionStartJsonWithState, 200);
        mediaSession.process();

        assertTrue(didSendNetworkRequest());
        assertNull(mediaSession.getSessionId());
        assertEquals(1, mediaSession.getQueueSize());
    }

    @Test
    public void test_process_sessionStart_httpSuccessWithInvalidLocationHeader() throws Exception {
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, mockData.configSharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, mockData.identitySharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Analytics.SHARED_STATE_NAME, mockData.analyticsSharedState);

        mediaSession.queueHit(mockData.sessionStart);
        assertEquals(1, mediaSession.getQueueSize());

        String invalidLocationString = "invalid";
        expectNetworkDataAndRespond(mockData.sessionStartJsonWithState, 201, invalidLocationString);
        mediaSession.process();

        assertTrue(didSendNetworkRequest());
        assertNull(mediaSession.getSessionId());
        assertEquals(1, mediaSession.getQueueSize());
    }

    @Test
    public void test_process_sessionStart_httpFailure() throws Exception {
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, mockData.configSharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, mockData.identitySharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Analytics.SHARED_STATE_NAME, mockData.analyticsSharedState);

        mediaSession.queueHit(mockData.sessionStart);
        assertEquals(1, mediaSession.getQueueSize());

        expectNetworkDataAndRespond(mockData.sessionStartJsonWithState, 404);
        mediaSession.process();

        assertTrue(didSendNetworkRequest());
        assertNull(mediaSession.getSessionId());
        assertEquals(1, mediaSession.getQueueSize());
    }

    @Test
    public void test_process_sessionStart_httpSuccess() throws Exception {
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, mockData.configSharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, mockData.identitySharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Analytics.SHARED_STATE_NAME, mockData.analyticsSharedState);

        mediaSession.queueHit(mockData.sessionStart);
        assertEquals(1, mediaSession.getQueueSize());

        expectNetworkDataAndRespond(mockData.sessionStartJsonWithState, 201, mockSessionIdResponse);
        mediaSession.process();

        assertTrue(didSendNetworkRequest());
        assertEquals(
                "0160be5e22b37eb526c085b82b782d757a328c2b0fa248cc83bd92db452722ac",
                mediaSession.getSessionId());
        assertEquals(0, mediaSession.getQueueSize());
    }

    @Test
    public void test_process_sessionStart_retry() throws Exception {
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, mockData.configSharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, mockData.identitySharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Analytics.SHARED_STATE_NAME, mockData.analyticsSharedState);

        mediaSession.queueHit(mockData.sessionStart);
        assertEquals(1, mediaSession.getQueueSize());

        expectNetworkDataAndRespond(mockData.sessionStartJsonWithState, 404);
        mediaSession.process();

        assertTrue(didSendNetworkRequest());
        assertNull(mediaSession.getSessionId());
        assertEquals(1, mediaSession.getQueueSize());

        // Valid http code but no location is considered a failure
        // 2nd
        resetNetworkRequest();
        expectNetworkDataAndRespond(mockData.sessionStartJsonWithState, 201, null);
        mediaSession.process();

        assertTrue(didSendNetworkRequest());
        assertNull(mediaSession.getSessionId());
        assertEquals(1, mediaSession.getQueueSize());

        // 3rd
        resetNetworkRequest();
        expectNetworkDataAndRespond(mockData.sessionStartJsonWithState, 404);
        mediaSession.process();

        assertTrue(didSendNetworkRequest());
        assertNull(mediaSession.getSessionId());
        assertEquals(0, mediaSession.getQueueSize());
    }

    @Test
    public void test_process_sessionStart_failure_dropOtherPings() throws Exception {
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, mockData.configSharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, mockData.identitySharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Analytics.SHARED_STATE_NAME, mockData.analyticsSharedState);

        mediaSession.queueHit(mockData.sessionStart);
        mediaSession.queueHit(mockData.play);
        mediaSession.queueHit(mockData.adBreakStart);

        assertEquals(3, mediaSession.getQueueSize());

        for (int retry = 1; retry <= 3; ++retry) {
            resetNetworkRequest();
            expectNetworkDataAndRespond(mockData.sessionStartJsonWithState, 404);
            mediaSession.process();

            assertTrue(didSendNetworkRequest());
            assertNull(mediaSession.getSessionId());

            if (retry < 3) {
                assertEquals(3, mediaSession.getQueueSize());
            } else {
                assertEquals(2, mediaSession.getQueueSize());
            }
        }

        resetNetworkRequest();
        mediaSession.process();

        assertFalse(didSendNetworkRequest());
        assertEquals(1, mediaSession.getQueueSize());

        resetNetworkRequest();
        mediaSession.process();

        assertFalse(didSendNetworkRequest());
        assertEquals(0, mediaSession.getQueueSize());
    }

    @Test
    public void test_process_sessionStart_success_sendOtherPings() throws Exception {
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, mockData.configSharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, mockData.identitySharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Analytics.SHARED_STATE_NAME, mockData.analyticsSharedState);

        mediaSession.queueHit(mockData.sessionStart);
        mediaSession.queueHit(mockData.play);
        mediaSession.queueHit(mockData.adBreakStart);

        assertEquals(3, mediaSession.getQueueSize());

        expectNetworkDataAndRespond(mockData.sessionStartJsonWithState, 201, mockSessionIdResponse);
        mediaSession.process();

        assertTrue(didSendNetworkRequest());
        assertEquals(
                "0160be5e22b37eb526c085b82b782d757a328c2b0fa248cc83bd92db452722ac",
                mediaSession.getSessionId());
        assertEquals(2, mediaSession.getQueueSize());

        resetNetworkRequest();
        expectNetworkDataAndRespond(mockData.playJson, 200);
        mediaSession.process();

        assertTrue(didSendNetworkRequest());
        assertEquals(1, mediaSession.getQueueSize());

        resetNetworkRequest();
        expectNetworkDataAndRespond(mockData.adBreakStartJson, 200);
        mediaSession.process();

        assertTrue(didSendNetworkRequest());
        assertEquals(0, mediaSession.getQueueSize());
    }

    @Test
    public void test_process_checkRequestContentType() throws Exception {
        mediaSession.queueHit(mockData.sessionStart);

        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, mockData.configSharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, mockData.identitySharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Analytics.SHARED_STATE_NAME, mockData.analyticsSharedState);

        mediaSession.process();

        HashMap<String, String> expectedRequestProperty = new HashMap<String, String>();
        expectedRequestProperty.put("Content-Type", "application/json");

        assertEquals(expectedRequestProperty, mockNetworkService.capturedRequest.getHeaders());
    }

    @Test
    public void test_process_checkRequestAssuranceToken() throws Exception {
        mediaSession.queueHit(mockData.sessionStart);

        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, mockData.configSharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, mockData.identitySharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Analytics.SHARED_STATE_NAME, mockData.analyticsSharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Assurance.SHARED_STATE_NAME, mockData.assuranceSharedState);

        mediaSession.process();

        HashMap<String, String> expectedRequestProperty = new HashMap<String, String>();
        expectedRequestProperty.put(
                MediaInternalConstants.Networking.HEADER_KEY_AEP_VALIDATION_TOKEN,
                "integrationId12345");
        expectedRequestProperty.put("Content-Type", "application/json");

        assertEquals(expectedRequestProperty, mockNetworkService.capturedRequest.getHeaders());
    }

    @Test
    public void test_process_sessionStartEvent_sessionCreateDispatched() throws Exception {
        mediaSession.queueHit(mockData.sessionStart);

        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, mockData.configSharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, mockData.identitySharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Analytics.SHARED_STATE_NAME, mockData.analyticsSharedState);

        expectNetworkDataAndRespond(mockData.sessionStartJsonWithState, 201, mockSessionIdResponse);

        mediaSession.process();
        assertTrue(didSendNetworkRequest());
        assertTrue(mockDispatcherSessionCreated.dispatchSessionCreatedWasCalled);
    }

    @Test
    public void test_process_NonSessionStartEvent_sessionCreateShouldNotDispatched()
            throws Exception {
        mediaSession.queueHit(mockData.sessionStart);

        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, mockData.configSharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, mockData.identitySharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Analytics.SHARED_STATE_NAME, mockData.analyticsSharedState);

        expectNetworkDataAndRespond(mockData.sessionStartJsonWithState, 201, mockSessionIdResponse);

        mediaSession.process();
        assertTrue(didSendNetworkRequest());
        assertTrue(mockDispatcherSessionCreated.dispatchSessionCreatedWasCalled);

        mockDispatcherSessionCreated.dispatchSessionCreatedWasCalled = false;

        mediaSession.queueHit(mockData.play);

        resetNetworkRequest();
        expectNetworkDataAndRespond(mockData.playJson, 201, mockSessionIdResponse);
        mediaSession.process();

        assertTrue(didSendNetworkRequest());
        assertFalse(mockDispatcherSessionCreated.dispatchSessionCreatedWasCalled);
    }
}

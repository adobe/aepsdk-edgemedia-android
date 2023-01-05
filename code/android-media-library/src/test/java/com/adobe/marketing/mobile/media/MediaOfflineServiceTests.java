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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.adobe.marketing.mobile.MobilePrivacyStatus;
import com.adobe.marketing.mobile.services.DeviceInforming;
import com.adobe.marketing.mobile.services.HttpConnecting;
import com.adobe.marketing.mobile.services.MockNetworkService;
import com.adobe.marketing.mobile.services.ServiceProvider;
import com.adobe.marketing.mobile.services.ServiceProviderExtension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.mockito.Mockito;

class MockDBService implements MediaDBService {
    HashMap<String, ArrayList<MediaHit>> hits = new HashMap<>();

    @Override
    public Set<String> getSessionIDs() {
        return hits.keySet();
    }

    @Override
    public List<MediaHit> getHits(String sessionID) {
        List<MediaHit> ret = hits.get(sessionID);
        return ret == null ? new ArrayList<>() : ret;
    }

    @Override
    public void deleteAllHits() {
        hits.clear();
    }

    @Override
    public boolean deleteHits(String sessionID) {
        hits.remove(sessionID);
        return true;
    }

    @Override
    public boolean persistHit(String sessionID, MediaHit hit) {
        List<MediaHit> list = hits.get(sessionID);
        if (list == null) {
            hits.put(sessionID, new ArrayList<>());
        }

        hits.get(sessionID).add(hit);
        return false;
    }

    public boolean sessionPresent(String sessionId) {
        return hits.containsKey(sessionId);
    }

    public boolean hitPresent(String sessionId, MediaHit hit) {
        if (!hits.containsKey(sessionId)) {
            return false;
        }

        return hits.get(sessionId).contains(hit);
    }

    public boolean isEmpty() {
        return hits.isEmpty();
    }
}

public class MediaOfflineServiceTests {
    MockMediaOfflineHits mockData;
    MediaState mediaState;
    MockDBService mediaDBService;
    MockMediaSessionCreatedDispatcher mockDispatcherSessionCreated;
    TestMediaOfflineService offlineService;

    MockNetworkService mockNetworkService;
    DeviceInforming mockDeviceInfoServie;

    public MediaOfflineServiceTests() {
        mockNetworkService = new MockNetworkService();
        mockDeviceInfoServie = mock(DeviceInforming.class);
        ServiceProviderExtension.setDeviceInfoService(mockDeviceInfoServie);
        ServiceProvider.getInstance().setNetworkService(mockNetworkService);

        setNetworkConnectionStatus(DeviceInforming.ConnectionStatus.CONNECTED);

        mockData = new MockMediaOfflineHits();
        mediaDBService = new MockDBService();
        mediaState = new MediaState();
        mockDispatcherSessionCreated = new MockMediaSessionCreatedDispatcher(null);
        offlineService =
                new TestMediaOfflineService(
                        mediaDBService, mediaState, mockDispatcherSessionCreated);
    }

    String sessionWithHits(final List<MediaHit> hits) {
        String sessionId = offlineService.startSession();

        for (MediaHit hit : hits) {
            offlineService.processHit(sessionId, hit);
        }

        offlineService.endSession(sessionId);
        return sessionId;
    }

    void setNetworkConnectionStatus(DeviceInforming.ConnectionStatus status) {
        Mockito.when(mockDeviceInfoServie.getNetworkConnectionStatus()).thenReturn(status);
    }

    void expectNetworkDataAndRespond(final List<String> jsonHits, final int responseCode) {
        expectNetworkDataAndRespond(jsonHits, responseCode, "");
    }

    void expectNetworkDataAndRespond(
            final List<String> jsonHits, final int responseCode, String locationResponse) {
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
    public void test_startSession_differentIds() {
        String id1 = offlineService.startSession();
        String id2 = offlineService.startSession();
        assertNotNull(id1);
        assertNotNull(id2);
        assertNotEquals(id1, id2);
    }

    @Test
    public void test_startSession_afterOptOut() {
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME,
                mockData.configSharedStateOptOut);
        assertNull(offlineService.startSession());
    }

    @Test
    public void test_processHit_nullHit() {
        try {
            offlineService.processHit("1", null);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void test_processHit_notActiveSession() {
        offlineService.processHit("random", mockData.sessionStart);
        assertFalse(mediaDBService.hitPresent("random", mockData.sessionStart));
    }

    @Test
    public void test_processHit_activeSession() {
        String id = offlineService.startSession();
        offlineService.processHit(id, mockData.sessionStart);

        assertTrue(mediaDBService.hitPresent(id, mockData.sessionStart));
    }

    @Test
    public void test_processHit_activeSession_afterOptOut() {
        String id = offlineService.startSession();

        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME,
                mockData.configSharedStateOptOut);
        offlineService.notifyMobileStateChanges();

        offlineService.processHit(id, mockData.sessionStart);
        assertFalse(mediaDBService.hitPresent(id, mockData.sessionStart));
    }

    @Test
    public void test_reportCompletedSessions_currentlySendingReport() {
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, mockData.configSharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, mockData.identitySharedState);
        offlineService.notifyMobileStateChanges();

        List<MediaHit> mediaHits = new ArrayList<>();
        mediaHits.add(mockData.sessionStart);
        mediaHits.add(mockData.complete);
        sessionWithHits(mediaHits);

        List<String> expectedReport = new ArrayList<>();
        expectedReport.add(mockData.sessionStartJsonWithConfigurationIdentityState);
        expectedReport.add(mockData.completeJson);
        expectNetworkDataAndRespond(expectedReport, 200);

        assertTrue(offlineService.reportCompletedSessions());
    }

    @Test
    public void test_reportCompletedSessions_OptUnknown() {
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, mockData.configSharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Analytics.SHARED_STATE_NAME, mockData.analyticsSharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, mockData.identitySharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME,
                mockData.configSharedStateUnknown);
        offlineService.notifyMobileStateChanges();

        List<MediaHit> mediaHits = new ArrayList<MediaHit>();
        mediaHits.add(mockData.sessionStart);
        mediaHits.add(mockData.complete);
        sessionWithHits(mediaHits);

        assertFalse(offlineService.reportCompletedSessions());

        // Opt-in
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME,
                mockData.configSharedStateOptIn);
        offlineService.notifyMobileStateChanges();

        List<String> expectedReport = new ArrayList<>();
        expectedReport.add(mockData.sessionStartJsonWithState);
        expectedReport.add(mockData.completeJson);
        expectNetworkDataAndRespond(expectedReport, 200);
        assertTrue(offlineService.reportCompletedSessions());
    }

    @Test
    public void test_reportCompletedSessions_Optout() {
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, mockData.configSharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Analytics.SHARED_STATE_NAME, mockData.analyticsSharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, mockData.identitySharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME,
                mockData.configSharedStateOptOut);
        offlineService.notifyMobileStateChanges();

        List<MediaHit> mediaHits = new ArrayList<MediaHit>();
        mediaHits.add(mockData.sessionStart);
        mediaHits.add(mockData.complete);
        sessionWithHits(mediaHits);

        assertFalse(offlineService.reportCompletedSessions());

        // Opt-in
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME,
                mockData.configSharedStateOptIn);
        offlineService.notifyMobileStateChanges();

        assertFalse(offlineService.reportCompletedSessions());
    }

    @Test
    public void test_reportCompletedSessions_AfterSessionEnd() {
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, mockData.configSharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, mockData.identitySharedState);
        offlineService.notifyMobileStateChanges();

        assertFalse(offlineService.reportCompletedSessions());

        String id = offlineService.startSession();
        assertFalse(offlineService.reportCompletedSessions());

        offlineService.processHit(id, mockData.sessionStart);
        assertFalse(offlineService.reportCompletedSessions());

        offlineService.processHit(id, mockData.complete);
        assertFalse(offlineService.reportCompletedSessions());

        offlineService.endSession(id);

        List<String> expectedReport = new ArrayList<>();
        expectedReport.add(mockData.sessionStartJsonWithConfigurationIdentityState);
        expectedReport.add(mockData.completeJson);
        expectNetworkDataAndRespond(expectedReport, 200);

        assertTrue(offlineService.reportCompletedSessions());
    }

    @Test
    public void test_reportCompletedSessions_noNetworkConnection() {
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, mockData.configSharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, mockData.identitySharedState);
        offlineService.notifyMobileStateChanges();

        List<MediaHit> mediaHits = new ArrayList<MediaHit>();
        mediaHits.add(mockData.sessionStart);
        mediaHits.add(mockData.complete);
        sessionWithHits(mediaHits);

        setNetworkConnectionStatus(DeviceInforming.ConnectionStatus.DISCONNECTED);
        assertFalse(offlineService.reportCompletedSessions());
    }

    @Test
    public void test_reportCompletedSessions_noTrackingInfo() {
        List<MediaHit> mediaHits = new ArrayList<MediaHit>();
        mediaHits.add(mockData.sessionStart);
        mediaHits.add(mockData.complete);
        sessionWithHits(mediaHits);

        assertFalse(offlineService.reportCompletedSessions());

        Map<String, Object> sharedState = new HashMap<>();
        sharedState.put(
                MediaTestConstants.Configuration.GLOBAL_PRIVACY,
                MobilePrivacyStatus.OPT_IN.getValue());
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, sharedState);
        offlineService.notifyMobileStateChanges();
        assertFalse(offlineService.reportCompletedSessions());

        sharedState.put(MediaTestConstants.Configuration.EXPERIENCE_CLOUD_ORGID, "org_id");
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, sharedState);
        offlineService.notifyMobileStateChanges();
        assertFalse(offlineService.reportCompletedSessions());

        sharedState.put(MediaTestConstants.Configuration.ANALYTICS_RSID, "rsid");
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, sharedState);
        offlineService.notifyMobileStateChanges();
        assertFalse(offlineService.reportCompletedSessions());

        sharedState.put(
                MediaTestConstants.Configuration.ANALYTICS_TRACKING_SERVER, "analytics_server");
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, sharedState);
        offlineService.notifyMobileStateChanges();
        assertFalse(offlineService.reportCompletedSessions());

        sharedState.put(
                MediaTestConstants.Configuration.MEDIA_COLLECTION_SERVER,
                "media_collection_server");
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, sharedState);
        offlineService.notifyMobileStateChanges();
        assertFalse(offlineService.reportCompletedSessions());

        sharedState.put(MediaTestConstants.Identity.MARKETING_VISITOR_ID, "mcid");
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, sharedState);
        offlineService.notifyMobileStateChanges();
        assertTrue(offlineService.reportCompletedSessions());
    }

    @Test
    public void test_reportCompletedSessions_incorrectHits() {
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, mockData.configSharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, mockData.identitySharedState);
        offlineService.notifyMobileStateChanges();

        List<MediaHit> mediaHits = new ArrayList<MediaHit>();
        mediaHits.add(mockData.play);
        mediaHits.add(mockData.complete);
        sessionWithHits(mediaHits);

        assertFalse(offlineService.reportCompletedSessions());
    }

    @Test
    public void test_reportCompletedSessions_reportSuccess() {
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, mockData.configSharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, mockData.identitySharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Analytics.SHARED_STATE_NAME, mockData.analyticsSharedState);
        offlineService.notifyMobileStateChanges();

        List<MediaHit> mediaHits = new ArrayList<MediaHit>();
        mediaHits.add(mockData.sessionStart);
        mediaHits.add(mockData.play);
        mediaHits.add(mockData.adBreakStart);
        mediaHits.add(mockData.adStart);
        mediaHits.add(mockData.adComplete);
        mediaHits.add(mockData.adBreakComplete);
        mediaHits.add(mockData.ping);
        mediaHits.add(mockData.complete);
        String sessionId = sessionWithHits(mediaHits);

        List<String> jsonHits = new ArrayList<String>();
        jsonHits.add(mockData.sessionStartJsonWithState);
        jsonHits.add(mockData.playJson);
        jsonHits.add(mockData.adBreakStartJson);
        jsonHits.add(mockData.adStartJsonWithState);
        jsonHits.add(mockData.adCompleteJson);
        jsonHits.add(mockData.adBreakCompleteJson);
        jsonHits.add(mockData.pingJson);
        jsonHits.add(mockData.completeJson);

        expectNetworkDataAndRespond(jsonHits, 200);
        assertTrue(offlineService.reportCompletedSessions());
        assertFalse(mediaDBService.sessionPresent(sessionId));
    }

    @Test
    public void test_reportCompletedSessions_reportSuccessNextSession() {
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, mockData.configSharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, mockData.identitySharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Analytics.SHARED_STATE_NAME, mockData.analyticsSharedState);
        offlineService.notifyMobileStateChanges();

        List<MediaHit> mediaHits = new ArrayList<MediaHit>();
        mediaHits.add(mockData.sessionStart);
        mediaHits.add(mockData.play);
        mediaHits.add(mockData.adBreakStart);
        mediaHits.add(mockData.adStart);
        mediaHits.add(mockData.adComplete);
        mediaHits.add(mockData.adBreakComplete);
        mediaHits.add(mockData.ping);
        mediaHits.add(mockData.complete);

        List<MediaHit> mediaHits2 = new ArrayList<MediaHit>();
        mediaHits2.add(mockData.sessionStart);
        mediaHits2.add(mockData.complete);

        List<MediaHit> mediaHits3 = new ArrayList<MediaHit>();
        mediaHits3.add(mockData.sessionStart);
        mediaHits3.add(mockData.ping);
        mediaHits3.add(mockData.complete);

        String session1 = sessionWithHits(mediaHits);
        String session2 = sessionWithHits(mediaHits2);
        String session3 = sessionWithHits(mediaHits3);

        List<String> jsonHits = new ArrayList<String>();
        jsonHits.add(mockData.sessionStartJsonWithState);
        jsonHits.add(mockData.playJson);
        jsonHits.add(mockData.adBreakStartJson);
        jsonHits.add(mockData.adStartJsonWithState);
        jsonHits.add(mockData.adCompleteJson);
        jsonHits.add(mockData.adBreakCompleteJson);
        jsonHits.add(mockData.pingJson);
        jsonHits.add(mockData.completeJson);

        expectNetworkDataAndRespond(jsonHits, 200);
        assertTrue(offlineService.reportCompletedSessions());
        assertFalse(mediaDBService.sessionPresent(session1));

        List<String> jsonHits2 = new ArrayList<String>();
        jsonHits2.add(mockData.sessionStartJsonWithState);
        jsonHits2.add(mockData.completeJson);

        expectNetworkDataAndRespond(jsonHits2, 200);
        assertTrue(offlineService.reportCompletedSessions());
        assertFalse(mediaDBService.sessionPresent(session2));

        List<String> jsonHits3 = new ArrayList<String>();
        jsonHits3.add(mockData.sessionStartJsonWithState);
        jsonHits3.add(mockData.pingJson);
        jsonHits3.add(mockData.completeJson);

        expectNetworkDataAndRespond(jsonHits3, 200);
        assertTrue(offlineService.reportCompletedSessions());
        assertFalse(mediaDBService.sessionPresent(session2));

        // No more sessions
        assertFalse(offlineService.reportCompletedSessions());
    }

    @Test
    public void test_reportCompletedSessions_reportNetworkFailure() {
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, mockData.configSharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, mockData.identitySharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Analytics.SHARED_STATE_NAME, mockData.analyticsSharedState);
        offlineService.notifyMobileStateChanges();

        List<MediaHit> mediaHits = new ArrayList<MediaHit>();
        mediaHits.add(mockData.sessionStart);
        mediaHits.add(mockData.ping);
        mediaHits.add(mockData.complete);
        String session = sessionWithHits(mediaHits);

        List<String> jsonHits = new ArrayList<String>();
        jsonHits.add(mockData.sessionStartJsonWithState);
        jsonHits.add(mockData.pingJson);
        jsonHits.add(mockData.completeJson);

        expectNetworkDataAndRespond(jsonHits, 404);
        assertTrue(offlineService.reportCompletedSessions());
        assertTrue(mediaDBService.sessionPresent(session));

        expectNetworkDataAndRespond(jsonHits, 200);
        assertTrue(offlineService.reportCompletedSessions());
        assertFalse(mediaDBService.sessionPresent(session));

        assertFalse(offlineService.reportCompletedSessions());
    }

    @Test
    public void test_reportCompletedSessions_reportNetworkExceedRetry() {
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, mockData.configSharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, mockData.identitySharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Analytics.SHARED_STATE_NAME, mockData.analyticsSharedState);
        offlineService.notifyMobileStateChanges();

        List<MediaHit> mediaHits = new ArrayList<MediaHit>();
        mediaHits.add(mockData.sessionStart);
        mediaHits.add(mockData.ping);
        mediaHits.add(mockData.complete);

        String session = sessionWithHits(mediaHits);

        List<String> jsonHits = new ArrayList<String>();
        jsonHits.add(mockData.sessionStartJsonWithState);
        jsonHits.add(mockData.pingJson);
        jsonHits.add(mockData.completeJson);

        // Failed count 1
        expectNetworkDataAndRespond(jsonHits, 404);
        assertTrue(offlineService.reportCompletedSessions());
        assertTrue(mediaDBService.sessionPresent(session));

        // Failed count 2
        expectNetworkDataAndRespond(jsonHits, 404);
        assertTrue(offlineService.reportCompletedSessions());
        assertTrue(mediaDBService.sessionPresent(session));

        // Failed count 3
        expectNetworkDataAndRespond(jsonHits, 404);
        assertTrue(offlineService.reportCompletedSessions());
        assertFalse(mediaDBService.sessionPresent(session));

        assertFalse(offlineService.reportCompletedSessions());
    }

    @Test
    public void test_reportCompletedSessions_reportMultipleSession_mixedResult() {
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, mockData.configSharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, mockData.identitySharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Analytics.SHARED_STATE_NAME, mockData.analyticsSharedState);
        offlineService.notifyMobileStateChanges();

        List<MediaHit> mediaHits = new ArrayList<MediaHit>();
        mediaHits.add(mockData.sessionStart);
        mediaHits.add(mockData.complete);
        String session1 = sessionWithHits(mediaHits);

        List<MediaHit> mediaHits2 = new ArrayList<MediaHit>();
        mediaHits2.add(mockData.sessionStart);
        mediaHits2.add(mockData.play);
        mediaHits2.add(mockData.complete);
        String session2 = sessionWithHits(mediaHits2);

        List<MediaHit> mediaHits3 = new ArrayList<MediaHit>();
        mediaHits3.add(mockData.sessionStart);
        mediaHits3.add(mockData.ping);
        mediaHits3.add(mockData.complete);
        String session3 = sessionWithHits(mediaHits3);

        // Session 1
        List<String> jsonHits = new ArrayList<String>();
        jsonHits.add(mockData.sessionStartJsonWithState);
        jsonHits.add(mockData.completeJson);

        // Session 1 -> fail
        expectNetworkDataAndRespond(jsonHits, 404);
        assertTrue(offlineService.reportCompletedSessions());
        assertTrue(mediaDBService.sessionPresent(session1));

        // Session 1 -> success
        expectNetworkDataAndRespond(jsonHits, 201);
        assertTrue(offlineService.reportCompletedSessions());
        assertFalse(mediaDBService.sessionPresent(session1));

        // Session 2
        List<String> jsonHits2 = new ArrayList<String>();
        jsonHits2.add(mockData.sessionStartJsonWithState);
        jsonHits2.add(mockData.playJson);
        jsonHits2.add(mockData.completeJson);

        // Session 2 -> fail count = 1
        expectNetworkDataAndRespond(jsonHits2, 404);
        assertTrue(offlineService.reportCompletedSessions());
        assertTrue(mediaDBService.sessionPresent(session2));

        // Session 2 -> fail count = 2
        expectNetworkDataAndRespond(jsonHits2, 404);
        assertTrue(offlineService.reportCompletedSessions());
        assertTrue(mediaDBService.sessionPresent(session2));

        // Session 2 -> fail count = 3
        expectNetworkDataAndRespond(jsonHits2, 404);
        assertTrue(offlineService.reportCompletedSessions());
        assertFalse(mediaDBService.sessionPresent(session2));

        // Session 3
        List<String> jsonHits3 = new ArrayList<String>();
        jsonHits3.add(mockData.sessionStartJsonWithState);
        jsonHits3.add(mockData.pingJson);
        jsonHits3.add(mockData.completeJson);

        // Session 3 -> success
        expectNetworkDataAndRespond(jsonHits3, 200);
        assertTrue(offlineService.reportCompletedSessions());
        assertFalse(mediaDBService.sessionPresent(session3));
    }

    @Test
    public void test_reportCompletedSessions_optOutBeforeSessionStart() {
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, mockData.configSharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, mockData.identitySharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Analytics.SHARED_STATE_NAME, mockData.analyticsSharedState);
        offlineService.notifyMobileStateChanges();

        // Opted-out
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME,
                mockData.configSharedStateOptOut);
        offlineService.notifyMobileStateChanges();

        List<MediaHit> mediaHits = new ArrayList<MediaHit>();
        mediaHits.add(mockData.sessionStart);
        mediaHits.add(mockData.play);
        mediaHits.add(mockData.adBreakStart);
        mediaHits.add(mockData.adStart);
        mediaHits.add(mockData.adComplete);
        mediaHits.add(mockData.adBreakComplete);
        mediaHits.add(mockData.ping);
        mediaHits.add(mockData.complete);
        sessionWithHits(mediaHits);

        assertTrue(mediaDBService.isEmpty());
    }

    @Test
    public void test_reportCompletedSessions_optOutMidSession() {
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, mockData.configSharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, mockData.identitySharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Analytics.SHARED_STATE_NAME, mockData.analyticsSharedState);
        offlineService.notifyMobileStateChanges();

        String sessionId = offlineService.startSession();
        offlineService.processHit(sessionId, mockData.sessionStart);
        offlineService.processHit(sessionId, mockData.play);
        offlineService.processHit(sessionId, mockData.adBreakStart);
        offlineService.processHit(sessionId, mockData.adStart);

        // Opted-out
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME,
                mockData.configSharedStateOptOut);
        offlineService.notifyMobileStateChanges();

        assertTrue(mediaDBService.isEmpty());

        offlineService.processHit(sessionId, mockData.adComplete);
        offlineService.processHit(sessionId, mockData.adBreakComplete);
        offlineService.processHit(sessionId, mockData.ping);
        offlineService.processHit(sessionId, mockData.complete);

        assertTrue(mediaDBService.isEmpty());
    }

    @Test
    public void test_reportCompletedSessions_optOutAfterSession() {
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, mockData.configSharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, mockData.identitySharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Analytics.SHARED_STATE_NAME, mockData.analyticsSharedState);
        offlineService.notifyMobileStateChanges();

        List<MediaHit> mediaHits = new ArrayList<MediaHit>();
        mediaHits.add(mockData.sessionStart);
        mediaHits.add(mockData.play);
        mediaHits.add(mockData.adBreakStart);
        mediaHits.add(mockData.adStart);
        mediaHits.add(mockData.adComplete);
        mediaHits.add(mockData.adBreakComplete);
        mediaHits.add(mockData.ping);
        mediaHits.add(mockData.complete);
        sessionWithHits(mediaHits);

        assertFalse(mediaDBService.isEmpty());

        // Opted-out
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME,
                mockData.configSharedStateOptOut);
        offlineService.notifyMobileStateChanges();

        assertTrue(mediaDBService.isEmpty());
    }

    @Test
    public void test_reportCompletedSessions_persistedFromPreviousLaunch() {
        List<MediaHit> mediaHits = new ArrayList<MediaHit>();
        mediaHits.add(mockData.sessionStart);
        mediaHits.add(mockData.play);
        mediaHits.add(mockData.adBreakStart);
        mediaHits.add(mockData.adStart);
        mediaHits.add(mockData.adComplete);
        mediaHits.add(mockData.adBreakComplete);
        mediaHits.add(mockData.ping);
        mediaHits.add(mockData.complete);
        String oldSession = sessionWithHits(mediaHits);

        // New launch
        mediaState = new MediaState();
        offlineService =
                new TestMediaOfflineService(
                        mediaDBService, mediaState, mockDispatcherSessionCreated);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, mockData.configSharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, mockData.identitySharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Analytics.SHARED_STATE_NAME, mockData.analyticsSharedState);

        List<String> jsonHits = new ArrayList<String>();
        jsonHits.add(mockData.sessionStartJsonWithState);
        jsonHits.add(mockData.playJson);
        jsonHits.add(mockData.adBreakStartJson);
        jsonHits.add(mockData.adStartJsonWithState);
        jsonHits.add(mockData.adCompleteJson);
        jsonHits.add(mockData.adBreakCompleteJson);
        jsonHits.add(mockData.pingJson);
        jsonHits.add(mockData.completeJson);

        expectNetworkDataAndRespond(jsonHits, 200);
        assertTrue(offlineService.reportCompletedSessions());
        assertTrue(mediaDBService.isEmpty());
    }

    @Test
    public void test_reportCompletedSessions_checkRequestContentType() {
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, mockData.configSharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, mockData.identitySharedState);
        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Analytics.SHARED_STATE_NAME, mockData.analyticsSharedState);
        offlineService.notifyMobileStateChanges();

        List<MediaHit> mediaHits = new ArrayList<MediaHit>();
        mediaHits.add(mockData.sessionStart);
        mediaHits.add(mockData.complete);
        sessionWithHits(mediaHits);

        assertTrue(offlineService.reportCompletedSessions());

        HashMap<String, String> expectedRequestProperty = new HashMap<String, String>();
        expectedRequestProperty.put("Content-Type", "application/json");
        assertEquals(expectedRequestProperty, mockNetworkService.capturedRequest.getHeaders());
    }
}

class TestMediaOfflineService extends MediaOfflineService {
    TestMediaOfflineService(
            MediaDBService mediaDBService,
            MediaState mediaState,
            final MediaSessionCreatedDispatcher dispatcher) {
        super(mediaDBService, mediaState, dispatcher);
        stopFlushTimer();
    }

    @Override
    void reportCompletedSessionsAsync() {}
}

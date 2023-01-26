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

package com.adobe.marketing.mobile.media.internal;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class MediaReportHelperTests {
    private MockMediaOfflineHits mockMediaOfflineHits;
    private MediaState mediaState;

    public MediaReportHelperTests() {
        mockMediaOfflineHits = new MockMediaOfflineHits();
        mediaState = new MediaState();
    }

    public void updateConfigurationSharedState() {
        Map<String, Object> configSharedState = new HashMap<>();
        configSharedState.put(MediaTestConstants.Configuration.EXPERIENCE_CLOUD_ORGID, "org_id");
        configSharedState.put(MediaTestConstants.Configuration.ANALYTICS_RSID, "rsid");
        configSharedState.put(
                MediaTestConstants.Configuration.ANALYTICS_TRACKING_SERVER, "analytics_server");
        configSharedState.put(
                MediaTestConstants.Configuration.MEDIA_COLLECTION_SERVER,
                "Media_collection_server");

        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, configSharedState);
    }

    public void updateIdentitySharedState() {
        Map<String, Object> identitySharedState = new HashMap<>();
        identitySharedState.put(MediaTestConstants.Identity.MARKETING_VISITOR_ID, "mid");

        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, identitySharedState);
    }

    @Test
    public void test_generateReport_NULLParams() {
        String res = MediaReportHelper.generateDownloadReport(null, null);
        assertEquals("", res);

        res = MediaReportHelper.generateDownloadReport(null, new ArrayList<MediaHit>());
        assertEquals("", res);

        res =
                MediaReportHelper.generateDownloadReport(
                        mockMediaOfflineHits.mediaState, new ArrayList<MediaHit>());
        assertEquals("", res);

        List<MediaHit> hitList = new ArrayList<MediaHit>();
        hitList.add(null);
        hitList.add(null);
        // null media state
        res = MediaReportHelper.generateDownloadReport(null, hitList);
        assertEquals("", res);

        // null media hit list
        res = MediaReportHelper.generateDownloadReport(mockMediaOfflineHits.mediaState, null);
        assertEquals("", res);

        // non-empty media hits list with null hits
        res = MediaReportHelper.generateDownloadReport(mockMediaOfflineHits.mediaState, hitList);
        assertEquals("", res);
    }

    @Test
    public void test_generateReport_emptyMediaState() {
        List<MediaHit> hits = new ArrayList<MediaHit>();
        hits.add(mockMediaOfflineHits.sessionStart);
        hits.add(mockMediaOfflineHits.play);
        hits.add(mockMediaOfflineHits.adBreakStart);
        hits.add(mockMediaOfflineHits.adStart);
        hits.add(mockMediaOfflineHits.adComplete);
        hits.add(mockMediaOfflineHits.adBreakComplete);
        hits.add(mockMediaOfflineHits.ping);
        hits.add(mockMediaOfflineHits.complete);

        List<String> expectedHits = new ArrayList<String>();
        expectedHits.add(mockMediaOfflineHits.sessionStartJson);
        expectedHits.add(mockMediaOfflineHits.playJson);
        expectedHits.add(mockMediaOfflineHits.adBreakStartJson);
        expectedHits.add(mockMediaOfflineHits.adStartJson);
        expectedHits.add(mockMediaOfflineHits.adCompleteJson);
        expectedHits.add(mockMediaOfflineHits.adBreakCompleteJson);
        expectedHits.add(mockMediaOfflineHits.pingJson);
        expectedHits.add(mockMediaOfflineHits.completeJson);

        String res =
                MediaReportHelper.generateDownloadReport(
                        mockMediaOfflineHits.mediaStateEmpty, hits);
        assertTrue(mockMediaOfflineHits.compareReport(expectedHits, res));
    }

    @Test
    public void test_generateReport_properMediaState() {
        List<MediaHit> hits = new ArrayList<MediaHit>();
        hits.add(mockMediaOfflineHits.sessionStart);
        hits.add(mockMediaOfflineHits.play);
        hits.add(mockMediaOfflineHits.adBreakStart);
        hits.add(mockMediaOfflineHits.adStart);
        hits.add(mockMediaOfflineHits.adComplete);
        hits.add(mockMediaOfflineHits.adBreakComplete);
        hits.add(mockMediaOfflineHits.ping);
        hits.add(mockMediaOfflineHits.complete);

        List<String> expectedHits = new ArrayList<String>();
        expectedHits.add(mockMediaOfflineHits.sessionStartJsonWithState);
        expectedHits.add(mockMediaOfflineHits.playJson);
        expectedHits.add(mockMediaOfflineHits.adBreakStartJson);
        expectedHits.add(mockMediaOfflineHits.adStartJsonWithState);
        expectedHits.add(mockMediaOfflineHits.adCompleteJson);
        expectedHits.add(mockMediaOfflineHits.adBreakCompleteJson);
        expectedHits.add(mockMediaOfflineHits.pingJson);
        expectedHits.add(mockMediaOfflineHits.completeJson);

        String res =
                MediaReportHelper.generateDownloadReport(mockMediaOfflineHits.mediaState, hits);
        assertTrue(mockMediaOfflineHits.compareReport(expectedHits, res));
    }

    @Test
    public void test_generateReport_sessionStart_channelPresent() {
        List<MediaHit> hits = new ArrayList<MediaHit>();
        hits.add(mockMediaOfflineHits.sessionStartChannel);
        hits.add(mockMediaOfflineHits.play);
        hits.add(mockMediaOfflineHits.ping);
        hits.add(mockMediaOfflineHits.complete);

        List<String> expectedHits = new ArrayList<String>();
        expectedHits.add(mockMediaOfflineHits.sessionStartChannelJson);
        expectedHits.add(mockMediaOfflineHits.playJson);
        expectedHits.add(mockMediaOfflineHits.pingJson);
        expectedHits.add(mockMediaOfflineHits.completeJson);

        String res =
                MediaReportHelper.generateDownloadReport(
                        mockMediaOfflineHits.mediaStateEmpty, hits);
        assertTrue(mockMediaOfflineHits.compareReport(expectedHits, res));
    }

    @Test
    public void test_generateReport_missingSessionStart() {
        List<MediaHit> hits = new ArrayList<MediaHit>();
        hits.add(mockMediaOfflineHits.play);
        hits.add(mockMediaOfflineHits.ping);
        hits.add(mockMediaOfflineHits.complete);

        List<String> expectedHits = new ArrayList<String>();
        expectedHits.add(mockMediaOfflineHits.sessionStartJson);
        expectedHits.add(mockMediaOfflineHits.playJson);
        expectedHits.add(mockMediaOfflineHits.pingJson);
        expectedHits.add(mockMediaOfflineHits.completeJson);

        String res =
                MediaReportHelper.generateDownloadReport(
                        mockMediaOfflineHits.mediaStateEmpty, hits);
        assertEquals("", res);
    }

    @Test
    public void test_generateReport_missingSessionEndOrComplete() {
        List<MediaHit> hits = new ArrayList<MediaHit>();
        hits.add(mockMediaOfflineHits.sessionStart);
        hits.add(mockMediaOfflineHits.play);
        hits.add(mockMediaOfflineHits.ping);

        List<String> expectedHits = new ArrayList<String>();
        expectedHits.add(mockMediaOfflineHits.sessionStartJson);
        expectedHits.add(mockMediaOfflineHits.playJson);
        expectedHits.add(mockMediaOfflineHits.pingJson);
        expectedHits.add(mockMediaOfflineHits.forceSessionEndJson);

        String res =
                MediaReportHelper.generateDownloadReport(
                        mockMediaOfflineHits.mediaStateEmpty, hits);
        assertTrue(mockMediaOfflineHits.compareReport(expectedHits, res));
    }

    @Test
    public void test_generateReport_dropHitsTillSessionStart() {
        List<MediaHit> hits = new ArrayList<MediaHit>();
        hits.add(mockMediaOfflineHits.play);
        hits.add(mockMediaOfflineHits.ping);
        hits.add(mockMediaOfflineHits.sessionStart);
        hits.add(mockMediaOfflineHits.play);
        hits.add(mockMediaOfflineHits.ping);
        hits.add(mockMediaOfflineHits.complete);

        List<String> expectedHits = new ArrayList<String>();
        expectedHits.add(mockMediaOfflineHits.sessionStartJson);
        expectedHits.add(mockMediaOfflineHits.playJson);
        expectedHits.add(mockMediaOfflineHits.pingJson);
        expectedHits.add(mockMediaOfflineHits.completeJson);

        String res =
                MediaReportHelper.generateDownloadReport(
                        mockMediaOfflineHits.mediaStateEmpty, hits);
        assertTrue(mockMediaOfflineHits.compareReport(expectedHits, res));
    }

    @Test
    public void test_generateReport_dropHitsAfterSessionEnd() {
        List<MediaHit> hits = new ArrayList<MediaHit>();
        hits.add(mockMediaOfflineHits.sessionStart);
        hits.add(mockMediaOfflineHits.play);
        hits.add(mockMediaOfflineHits.ping);
        hits.add(mockMediaOfflineHits.complete);
        hits.add(mockMediaOfflineHits.play);
        hits.add(mockMediaOfflineHits.ping);

        List<String> expectedHits = new ArrayList<String>();
        expectedHits.add(mockMediaOfflineHits.sessionStartJson);
        expectedHits.add(mockMediaOfflineHits.playJson);
        expectedHits.add(mockMediaOfflineHits.pingJson);
        expectedHits.add(mockMediaOfflineHits.completeJson);

        String res =
                MediaReportHelper.generateDownloadReport(
                        mockMediaOfflineHits.mediaStateEmpty, hits);
        assertTrue(mockMediaOfflineHits.compareReport(expectedHits, res));
    }

    @Test
    public void test_generateReport_locHintException() {
        List<MediaHit> hits = new ArrayList<MediaHit>();
        hits.add(mockMediaOfflineHits.sessionStart);
        hits.add(mockMediaOfflineHits.play);
        hits.add(mockMediaOfflineHits.ping);
        hits.add(mockMediaOfflineHits.complete);

        List<String> expectedHits = new ArrayList<String>();
        expectedHits.add(mockMediaOfflineHits.sessionStartJson);
        expectedHits.add(mockMediaOfflineHits.playJson);
        expectedHits.add(mockMediaOfflineHits.pingJson);
        expectedHits.add(mockMediaOfflineHits.completeJson);

        String res =
                MediaReportHelper.generateDownloadReport(
                        mockMediaOfflineHits.mediaStateLocHintException, hits);
        assertTrue(mockMediaOfflineHits.compareReport(expectedHits, res));
    }

    @Test
    public void test_hasTrackingParam_success() {
        updateConfigurationSharedState();
        updateIdentitySharedState();
        ReturnTuple ret = MediaReportHelper.hasTrackingParams(mediaState);
        assertTrue(ret.isSuccess());
        assertNull(ret.getError());
    }

    @Test
    public void test_hasTrackingParam_MEDIA_COLLECTION_SERVER_missing() {
        Map<String, Object> configSharedState = new HashMap<>();
        configSharedState.put(MediaTestConstants.Configuration.EXPERIENCE_CLOUD_ORGID, "org_id");
        configSharedState.put(MediaTestConstants.Configuration.ANALYTICS_RSID, "rsid");
        configSharedState.put(
                MediaTestConstants.Configuration.ANALYTICS_TRACKING_SERVER, "analytics_server");

        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, configSharedState);

        Map<String, Object> identitySharedState = new HashMap<>();
        identitySharedState.put(MediaTestConstants.Identity.MARKETING_VISITOR_ID, "mid");

        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, identitySharedState);

        ReturnTuple ret = MediaReportHelper.hasTrackingParams(mediaState);
        assertFalse(ret.isSuccess());
        assertEquals(MediaTestConstants.Configuration.MEDIA_COLLECTION_SERVER, ret.getError());
    }

    @Test
    public void test_hasTrackingParam_ANALYTICS_TRACKING_SERVER_missing() {
        Map<String, Object> configSharedState = new HashMap<>();
        configSharedState.put(MediaTestConstants.Configuration.EXPERIENCE_CLOUD_ORGID, "org_id");
        configSharedState.put(MediaTestConstants.Configuration.ANALYTICS_RSID, "rsid");
        configSharedState.put(
                MediaTestConstants.Configuration.MEDIA_COLLECTION_SERVER,
                "Media_collection_server");

        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, configSharedState);

        Map<String, Object> identitySharedState = new HashMap<>();
        identitySharedState.put(MediaTestConstants.Identity.MARKETING_VISITOR_ID, "mid");

        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, identitySharedState);

        ReturnTuple ret = MediaReportHelper.hasTrackingParams(mediaState);
        assertFalse(ret.isSuccess());
        assertEquals(MediaTestConstants.Configuration.ANALYTICS_TRACKING_SERVER, ret.getError());
    }

    @Test
    public void test_hasTrackingParam_ANALYTICS_RSID_missing() {
        Map<String, Object> configSharedState = new HashMap<>();
        configSharedState.put(MediaTestConstants.Configuration.EXPERIENCE_CLOUD_ORGID, "org_id");
        configSharedState.put(
                MediaTestConstants.Configuration.ANALYTICS_TRACKING_SERVER, "analytics_server");
        configSharedState.put(
                MediaTestConstants.Configuration.MEDIA_COLLECTION_SERVER,
                "Media_collection_server");

        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, configSharedState);

        Map<String, Object> identitySharedState = new HashMap<>();
        identitySharedState.put(MediaTestConstants.Identity.MARKETING_VISITOR_ID, "mid");

        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, identitySharedState);

        ReturnTuple ret = MediaReportHelper.hasTrackingParams(mediaState);
        assertFalse(ret.isSuccess());
        assertEquals(MediaTestConstants.Configuration.ANALYTICS_RSID, ret.getError());
    }

    @Test
    public void test_hasTrackingParam_EXPERIENCE_CLOUD_ORGID_missing() {
        Map<String, Object> configSharedState = new HashMap<>();
        configSharedState.put(MediaTestConstants.Configuration.ANALYTICS_RSID, "rsid");
        configSharedState.put(
                MediaTestConstants.Configuration.ANALYTICS_TRACKING_SERVER, "analytics_server");
        configSharedState.put(
                MediaTestConstants.Configuration.MEDIA_COLLECTION_SERVER,
                "Media_collection_server");

        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Configuration.SHARED_STATE_NAME, configSharedState);

        Map<String, Object> identitySharedState = new HashMap<>();
        identitySharedState.put(MediaTestConstants.Identity.MARKETING_VISITOR_ID, "mid");

        mediaState.notifyMobileStateChanges(
                MediaTestConstants.Identity.SHARED_STATE_NAME, identitySharedState);

        ReturnTuple ret = MediaReportHelper.hasTrackingParams(mediaState);
        assertFalse(ret.isSuccess());
        assertEquals(MediaTestConstants.Configuration.EXPERIENCE_CLOUD_ORGID, ret.getError());
    }

    @Test
    public void test_hasTrackingParam_MARKETING_VISITOR_ID_missing() {
        updateConfigurationSharedState();

        ReturnTuple ret = MediaReportHelper.hasTrackingParams(mediaState);
        assertFalse(ret.isSuccess());
        assertEquals(MediaTestConstants.Identity.MARKETING_VISITOR_ID, ret.getError());
    }
}

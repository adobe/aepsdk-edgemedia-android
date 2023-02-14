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

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

public class MediaSessionIDManagerTests {
    private MediaSessionIDManager mediaSessionIDManager;

    public MediaSessionIDManagerTests() {
        mediaSessionIDManager = new MediaSessionIDManager(new HashSet<String>());
    }

    @Test
    public void test_startActiveSession() {
        String id = mediaSessionIDManager.startActiveSession();
        assertNotNull(id);
    }

    @Test
    public void test_startMultipleActiveSession() {
        String id1 = mediaSessionIDManager.startActiveSession();
        String id2 = mediaSessionIDManager.startActiveSession();

        assertNotEquals(id1, id2);
        assertNotNull(id1);
        assertNotNull(id2);
    }

    @Test
    public void test_startActiveSessionWithPreviousSessions() {
        Set<String> prevSessions = new HashSet<>();
        prevSessions.add("1");
        prevSessions.add("12");

        mediaSessionIDManager = new MediaSessionIDManager(prevSessions);

        String reportID = mediaSessionIDManager.getSessionToReport();
        assertNotNull("1", reportID);

        mediaSessionIDManager.updateSessionState(
                reportID, MediaSessionIDManager.MediaSessionState.Reported);
        assertEquals("12", mediaSessionIDManager.getSessionToReport());
    }

    @Test
    public void test_isSessionActive() {
        String id = mediaSessionIDManager.startActiveSession();
        assertTrue(mediaSessionIDManager.isSessionActive(id));

        mediaSessionIDManager.updateSessionState(
                id, MediaSessionIDManager.MediaSessionState.Complete);
        assertFalse(mediaSessionIDManager.isSessionActive(id));
    }

    @Test
    public void test_updateSessionState() {
        String dummyID = "dummy";
        try {
            mediaSessionIDManager.updateSessionState(
                    dummyID, MediaSessionIDManager.MediaSessionState.Invalid);
            mediaSessionIDManager.updateSessionState(
                    dummyID, MediaSessionIDManager.MediaSessionState.Complete);
            mediaSessionIDManager.updateSessionState(
                    dummyID, MediaSessionIDManager.MediaSessionState.Reported);
            mediaSessionIDManager.updateSessionState(
                    dummyID, MediaSessionIDManager.MediaSessionState.Active);
            mediaSessionIDManager.updateSessionState(
                    dummyID, MediaSessionIDManager.MediaSessionState.Failed);
        } catch (Exception e) {
            fail();
        }

        String id = mediaSessionIDManager.startActiveSession();
        mediaSessionIDManager.updateSessionState(
                id, MediaSessionIDManager.MediaSessionState.Complete);
        assertFalse(mediaSessionIDManager.isSessionActive(id));
    }

    @Test
    public void test_getSessionToReportWithNoSession() {
        assertEquals(null, mediaSessionIDManager.getSessionToReport());

        String sessionId = mediaSessionIDManager.startActiveSession();
        assertNotNull(sessionId);

        mediaSessionIDManager.updateSessionState(
                sessionId, MediaSessionIDManager.MediaSessionState.Invalid);
        mediaSessionIDManager.updateSessionState(
                sessionId, MediaSessionIDManager.MediaSessionState.Reported);
        assertEquals(null, mediaSessionIDManager.getSessionToReport());
    }

    @Test
    public void test_getSessionReport_completedSession() {
        String id = mediaSessionIDManager.startActiveSession();
        mediaSessionIDManager.updateSessionState(
                id, MediaSessionIDManager.MediaSessionState.Complete);

        assertEquals(id, mediaSessionIDManager.getSessionToReport());
    }

    @Test
    public void test_getSessionReport_failedSession() {
        String id = mediaSessionIDManager.startActiveSession();

        mediaSessionIDManager.updateSessionState(
                id, MediaSessionIDManager.MediaSessionState.Failed);
        assertEquals(id, mediaSessionIDManager.getSessionToReport());

        mediaSessionIDManager.updateSessionState(
                id, MediaSessionIDManager.MediaSessionState.Failed);
        assertEquals(id, mediaSessionIDManager.getSessionToReport());

        mediaSessionIDManager.updateSessionState(
                id, MediaSessionIDManager.MediaSessionState.Failed);
        assertEquals(null, mediaSessionIDManager.getSessionToReport());
    }

    @Test
    public void test_shouldClearSession_activeSession() {
        String id = mediaSessionIDManager.startActiveSession();
        assertFalse(mediaSessionIDManager.shouldClearSession(id));
    }

    @Test
    public void test_shouldClearSession_invalidSession() {
        String id = mediaSessionIDManager.startActiveSession();
        mediaSessionIDManager.updateSessionState(
                id, MediaSessionIDManager.MediaSessionState.Invalid);
        assertTrue(mediaSessionIDManager.shouldClearSession(id));
    }

    @Test
    public void test_shouldClearSession_sessionNotPresent() {
        assertTrue(mediaSessionIDManager.shouldClearSession("100"));
    }

    @Test
    public void test_shouldClearSession_failedSession_exceedsMaxRetry() {
        String id = mediaSessionIDManager.startActiveSession();
        mediaSessionIDManager.updateSessionState(
                id, MediaSessionIDManager.MediaSessionState.Failed);
        assertFalse(mediaSessionIDManager.shouldClearSession(id));

        mediaSessionIDManager.updateSessionState(
                id, MediaSessionIDManager.MediaSessionState.Failed);
        assertFalse(mediaSessionIDManager.shouldClearSession(id));

        mediaSessionIDManager.updateSessionState(
                id, MediaSessionIDManager.MediaSessionState.Failed);
        assertTrue(mediaSessionIDManager.shouldClearSession(id));
    }

    @Test
    public void test_shouldClearSession_reportedSession() {
        String id = mediaSessionIDManager.startActiveSession();
        mediaSessionIDManager.updateSessionState(
                id, MediaSessionIDManager.MediaSessionState.Reported);
        assertTrue(mediaSessionIDManager.shouldClearSession(id));
    }

    @Test
    public void test_clear() {
        String id = mediaSessionIDManager.startActiveSession();
        mediaSessionIDManager.updateSessionState(
                id, MediaSessionIDManager.MediaSessionState.Complete);

        mediaSessionIDManager.clear();
        assertEquals(null, mediaSessionIDManager.getSessionToReport());
    }
}

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

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import com.adobe.marketing.mobile.services.MockAppContextService;
import com.adobe.marketing.mobile.services.ServiceProviderExtension;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.junit.Test;

public class MediaDBServiceTests {
    private static final String MEDIA_DATABASE = "com.adobe.module.media";

    private MediaDBService mediaDBService;
    private MediaHit mediaHit,
            mediaHitWithParam,
            mediaHitWithParamMetadata,
            mediaHitWithParamQOE,
            mediaHitWithQOE,
            mediaHitAllArguments;

    List<MediaHit> mediaHits;

    public MediaDBServiceTests() {
        Map<String, Object> params = new HashMap<>();
        params.put("pk1", "pv1");

        Map<String, String> metadata = new HashMap<>();
        metadata.put("mk1", "mv1");

        Map<String, Object> qoe = new HashMap<>();
        qoe.put("qk1", 1234567.89);

        Map<String, String> emptyStringMap = new HashMap<>();
        Map<String, Object> emptyObjectMap = new HashMap<>();

        mediaHits = new ArrayList<MediaHit>();

        mediaHit =
                new MediaHit(
                        "hit", emptyObjectMap, emptyStringMap, emptyObjectMap, 1.23, 1234567890);

        mediaHitWithQOE =
                new MediaHit("hitWithQOE", emptyObjectMap, emptyStringMap, qoe, 2.23, 1234567891);

        mediaHitWithParam =
                new MediaHit(
                        "hitWithParam", params, emptyStringMap, emptyObjectMap, 3.23, 1234567892);

        mediaHitWithParamQOE =
                new MediaHit("hitWithParamQOE", params, emptyStringMap, qoe, 4.23, 1234567893);

        mediaHitWithParamMetadata =
                new MediaHit(
                        "hitWithParamMetadata", params, metadata, emptyObjectMap, 5.23, 1234567894);

        mediaHitAllArguments =
                new MediaHit("hitWithAllArguments", params, metadata, qoe, 6.23, 1234567895);

        mediaHits.add(mediaHit);
        mediaHits.add(mediaHitWithQOE);
        mediaHits.add(mediaHitWithParam);
        mediaHits.add(mediaHitWithParamQOE);
        mediaHits.add(mediaHitWithParamMetadata);
        mediaHits.add(mediaHitAllArguments);

        Context context = ApplicationProvider.getApplicationContext();
        MockAppContextService mockAppContextService = new MockAppContextService();
        mockAppContextService.appContext = context;
        ServiceProviderExtension.setAppContextService(mockAppContextService);

        context.getApplicationContext().getDatabasePath(MEDIA_DATABASE).delete();

        mediaDBService = new MediaDBServiceImpl();
    }

    boolean mediaHitEqual(final MediaHit h1, final MediaHit h2) {
        do {
            if (!h1.getEventType().equals(h2.getEventType())) {
                break;
            }

            if (!h1.getParams().equals(h2.getParams())) {
                break;
            }

            if (!h1.getCustomMetadata().equals(h2.getCustomMetadata())) {
                break;
            }

            if (!h1.getQoEData().equals(h2.getQoEData())) {
                break;
            }

            if (Math.abs(h1.getPlayhead() - h2.getPlayhead()) > 0.00001) {
                break;
            }

            if (h1.getTimeStamp() != h2.getTimeStamp()) {
                break;
            }

            return true;
        } while (false);

        return false;
    }

    @Test
    public void test_persistHit_success() {
        for (MediaHit hit : mediaHits) {
            boolean res = mediaDBService.persistHit("1", hit);
            assertTrue(res);
        }
    }

    @Test
    public void test_persistHit_nullHit_fail() {
        boolean res = mediaDBService.persistHit("1", null);
        assertFalse(res);
    }

    @Test
    public void test_getSessionIDs_emptyDB() {
        Set<String> sessionIDs = mediaDBService.getSessionIDs();
        assertEquals(0, sessionIDs.size());
    }

    @Test
    public void test_getSessionIDs() {
        Set<String> expected = new HashSet<>();
        expected.add("1");

        mediaDBService.persistHit("1", mediaHit);
        assertEquals(expected, mediaDBService.getSessionIDs());

        mediaDBService.persistHit("2", mediaHit);
        expected.add("2");
        assertEquals(expected, mediaDBService.getSessionIDs());
    }

    @Test
    public void test_getHits_sessionAbsent() {
        List<MediaHit> hits = mediaDBService.getHits("1");
        assertTrue(hits.isEmpty());
    }

    @Test
    public void test_getHits_inOrder() {
        String sessionId = "1";

        for (MediaHit hit : mediaHits) {
            mediaDBService.persistHit(sessionId, hit);
        }

        List<MediaHit> hits = mediaDBService.getHits("1");

        for (int i = 0; i < hits.size(); i++) {
            assertTrue(mediaHitEqual(mediaHits.get(i), hits.get(i)));
        }
    }

    @Test
    public void test_getHits_multipleSessionsPresent() {
        Set<String> sessions = new HashSet<>();
        sessions.add("1");
        sessions.add("2");
        sessions.add("3");

        for (String session : sessions) {
            for (MediaHit hit : mediaHits) {
                mediaDBService.persistHit(session, hit);
            }
        }

        for (String session : sessions) {
            List<MediaHit> hits = mediaDBService.getHits(session);

            for (int i = 0; i < hits.size(); i++) {
                assertTrue(mediaHitEqual(mediaHits.get(i), hits.get(i)));
            }
        }
    }

    @Test
    public void test_deleteHits_sessionAbsent() {
        assertFalse(mediaDBService.deleteHits("1"));
    }

    @Test
    public void test_deleteHits_sessionPresent() {
        String sessionID = "1";

        for (MediaHit hit : mediaHits) {
            mediaDBService.persistHit(sessionID, hit);
        }

        assertTrue(mediaDBService.deleteHits(sessionID));

        List<MediaHit> hits = mediaDBService.getHits(sessionID);
        assertTrue(hits.isEmpty());
    }

    @Test
    public void test_deleteHits_multipleSessionsPresent() {
        Set<String> sessions = new HashSet<>();
        sessions.add("1");
        sessions.add("2");
        sessions.add("3");

        for (String session : sessions) {
            for (MediaHit hit : mediaHits) {
                mediaDBService.persistHit(session, hit);
            }
        }

        assertTrue(mediaDBService.deleteHits("1"));

        List<MediaHit> hits = mediaDBService.getHits("1");
        List<MediaHit> hits2 = mediaDBService.getHits("2");
        List<MediaHit> hits3 = mediaDBService.getHits("3");

        assertTrue(hits.isEmpty());
        assertTrue(hits2.size() == mediaHits.size());
        assertTrue(hits3.size() == mediaHits.size());

        assertTrue(mediaDBService.deleteHits("2"));
        hits2 = mediaDBService.getHits("2");
        hits3 = mediaDBService.getHits("3");

        assertTrue(hits2.isEmpty());
        assertTrue(hits3.size() == mediaHits.size());
    }

    @Test
    public void test_deleteAllHits_emptyDB() {
        try {
            mediaDBService.deleteAllHits();
        } catch (Exception e) {
            assertTrue(false);
        }
    }

    @Test
    public void test_deleteAllHits_multipleSessionsPresent() {
        Set<String> sessions = new HashSet<>();
        sessions.add("1");
        sessions.add("2");
        sessions.add("3");

        for (String session : sessions) {
            for (MediaHit hit : mediaHits) {
                mediaDBService.persistHit(session, hit);
            }
        }

        mediaDBService.deleteAllHits();

        for (String session : sessions) {
            List<MediaHit> hits = mediaDBService.getHits(session);
            assertTrue(hits.isEmpty());
        }
    }

    @Test
    public void test_deleteDeprecatedDatabaseFile() throws IOException {
        // Setup
        Context context = ApplicationProvider.getApplicationContext();
        final String DEPRECATED_2X_DB_FILE_NAME = "ADBMobileMedia.sqlite";
        File oldDB = new File(context.getCacheDir(), DEPRECATED_2X_DB_FILE_NAME);
        oldDB.createNewFile();

        File newDB = context.getDatabasePath(MEDIA_DATABASE);
        newDB.delete();

        // Before upgrade
        assertTrue(oldDB.exists());
        assertFalse(newDB.exists());

        MediaDBService dbService = new MediaDBServiceImpl();

        // After upgrade
        assertFalse(oldDB.exists());
        assertTrue(newDB.exists());
    }
}

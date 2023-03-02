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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class MediaHitTests {
    private MediaHit mediaHit;
    Map<String, Object> params;
    Map<String, String> metadata;
    Map<String, Object> qoe;
    double playhead = 1.23;
    long ts = 1234567890;

    public MediaHitTests() {
        params = new HashMap<>();
        params.put("pk1", "pv1");

        metadata = new HashMap<>();
        metadata.put("mk1", "mv1");

        qoe = new HashMap<>();
        qoe.put("qk1", 1234567.89);
    }

    @Test
    public void test_createMediaHit_withAllFieldsInvalidValues() {
        mediaHit = new MediaHit(null, null, null, null, -1, -1);

        assertNotNull(mediaHit);
        assertNull(mediaHit.getEventType());
        assertNotNull(mediaHit.getParams());
        assertNotNull(mediaHit.getCustomMetadata());
        assertNotNull(mediaHit.getQoEData());
        assertEquals(-1, mediaHit.getPlayhead(), 0.0);
        assertEquals(-1, mediaHit.getTimeStamp());
    }

    @Test
    public void test_createMediaHit_withEventType() {
        mediaHit = new MediaHit("hit", null, null, null, -1, -1);

        assertNotNull(mediaHit);
        assertNotNull(mediaHit.getEventType());
        assertEquals("hit", mediaHit.getEventType());
        assertNotNull(mediaHit.getParams());
        assertNotNull(mediaHit.getCustomMetadata());
        assertNotNull(mediaHit.getQoEData());
        assertEquals(-1, mediaHit.getPlayhead(), 0.0);
        assertEquals(-1, mediaHit.getTimeStamp());
    }

    @Test
    public void test_createMediaHit_withParam() {
        mediaHit = new MediaHit("hitWithParam", params, null, null, playhead, ts);

        assertNotNull(mediaHit);
        assertNotNull(mediaHit.getEventType());
        assertEquals("hitWithParam", mediaHit.getEventType());
        assertEquals(params, mediaHit.getParams());
        assertTrue(mediaHit.getCustomMetadata().isEmpty());
        assertTrue(mediaHit.getQoEData().isEmpty());
        assertEquals(playhead, mediaHit.getPlayhead(), 0.0);
        assertEquals(ts, mediaHit.getTimeStamp());
    }

    @Test
    public void test_createMediaHit_withParamMetadata() {
        mediaHit = new MediaHit("hitWithParamMetadata", params, metadata, null, playhead, ts);

        assertNotNull(mediaHit);
        assertNotNull(mediaHit.getEventType());
        assertEquals("hitWithParamMetadata", mediaHit.getEventType());
        assertEquals(params, mediaHit.getParams());
        assertEquals(metadata, mediaHit.getCustomMetadata());
        assertTrue(mediaHit.getQoEData().isEmpty());
        assertEquals(playhead, mediaHit.getPlayhead(), 0.0);
        assertEquals(ts, mediaHit.getTimeStamp());
    }

    @Test
    public void test_createMediaHit_withParamQOE() {
        mediaHit = new MediaHit("hitWithParamQOE", params, null, qoe, playhead, ts);

        assertNotNull(mediaHit);
        assertNotNull(mediaHit.getEventType());
        assertEquals("hitWithParamQOE", mediaHit.getEventType());
        assertEquals(params, mediaHit.getParams());
        assertTrue(mediaHit.getCustomMetadata().isEmpty());
        assertEquals(qoe, mediaHit.getQoEData());
        assertEquals(playhead, mediaHit.getPlayhead(), 0.0);
        assertEquals(ts, mediaHit.getTimeStamp());
    }

    @Test
    public void test_createMediaHit_withAllArguments() {
        mediaHit = new MediaHit("hitWithAllArguments", params, metadata, qoe, playhead, ts);

        assertNotNull(mediaHit);
        assertNotNull(mediaHit.getEventType());
        assertEquals("hitWithAllArguments", mediaHit.getEventType());
        assertEquals(params, mediaHit.getParams());
        assertEquals(metadata, mediaHit.getCustomMetadata());
        assertEquals(qoe, mediaHit.getQoEData());
        assertEquals(playhead, mediaHit.getPlayhead(), 0.0);
        assertEquals(ts, mediaHit.getTimeStamp());
    }

    @Test
    public void test_equals_sameObject_pass() {
        mediaHit = new MediaHit("hitWithAllArguments", params, metadata, qoe, playhead, ts);

        MediaHit mediaHit2 = mediaHit;
        assertEquals(mediaHit, mediaHit2);
    }

    @Test
    public void test_equals_differentObjectType_fail() {
        mediaHit = new MediaHit("hit", params, metadata, qoe, playhead, ts);

        Object invalidObject = "invalidObject";
        assertNotEquals(mediaHit, invalidObject);
    }

    @Test
    public void test_equals_differentObject_differentEventType_fail() {
        mediaHit = new MediaHit("hit", params, metadata, qoe, playhead, ts);
        params.put("k", "v");
        MediaHit mediaHit2 = new MediaHit("hit2", params, metadata, qoe, playhead, ts);

        assertNotEquals(mediaHit, mediaHit2);
    }

    @Test
    public void test_equals_differentObject_differentParams_fail() {
        mediaHit = new MediaHit("hit", params, metadata, qoe, playhead, ts);
        params.put("k", "v");
        MediaHit mediaHit2 = new MediaHit("hit", params, metadata, qoe, playhead, ts);

        assertNotEquals(mediaHit, mediaHit2);
    }

    @Test
    public void test_equals_differentObject_differentQoe_fail() {
        mediaHit = new MediaHit("hit", params, metadata, qoe, playhead, ts);
        qoe.put("k", "v");
        MediaHit mediaHit2 = new MediaHit("hit", params, metadata, qoe, playhead, ts);

        assertNotEquals(mediaHit, mediaHit2);
    }

    @Test
    public void test_equals_differentObject_differentMetadata_fail() {
        mediaHit = new MediaHit("hit", params, metadata, qoe, playhead, ts);
        metadata.put("k", "v");
        MediaHit mediaHit2 = new MediaHit("hit", params, metadata, qoe, playhead, ts);

        assertNotEquals(mediaHit, mediaHit2);
    }

    @Test
    public void test_equals_differentObject_sameValues_pass() {
        mediaHit = new MediaHit("hit", params, metadata, qoe, playhead, ts);
        MediaHit mediaHit2 = new MediaHit("hit", params, metadata, qoe, playhead, ts);

        assertEquals(mediaHit, mediaHit2);
    }
}

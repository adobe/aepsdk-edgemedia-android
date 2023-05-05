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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

interface TestCallback {
    String call();
}

public class MediaStateTests {

    MediaState mediaState;

    @Before
    public void setup() {
        mediaState = new MediaState();
    }

    public void testStringState(
            final String key, final TestCallback callback, final String defaultVal) {
        Map<String, Object> states = new HashMap<>();
        assertEquals(defaultVal, callback.call());

        mediaState.updateState(null);
        assertEquals(defaultVal, callback.call());

        // Invalid value should not change anything
        states.put(key, false);
        mediaState.updateState(states);
        assertEquals(defaultVal, callback.call());

        states.put(key, 1);
        assertEquals(defaultVal, callback.call());

        states.put(key, null);
        mediaState.updateState(states);
        assertEquals(defaultVal, callback.call());

        states.put(key, "");
        mediaState.updateState(states);
        assertEquals("", callback.call());

        states.put(key, "value1");
        mediaState.updateState(states);
        assertEquals("value1", callback.call());

        states.put(key, "value2");
        mediaState.updateState(states);
        assertEquals("value2", callback.call());

        // passing null for configuration won't change values
        mediaState.updateState(null);
        assertEquals("value2", callback.call());

        // passing empty for configuration won't change values
        mediaState.updateState(Collections.<String, Object>emptyMap());
        assertEquals("value2", callback.call());
    }

    @Test
    public void test_updateState_getMediaChannel() {
        testStringState(
                MediaTestConstants.Configuration.MEDIA_CHANNEL,
                new TestCallback() {
                    @Override
                    public String call() {
                        return mediaState.getMediaChannel();
                    }
                },
                null);
    }

    @Test
    public void test_updateState_getMediaPlayerName() {
        testStringState(
                MediaTestConstants.Configuration.MEDIA_PLAYER_NAME,
                new TestCallback() {
                    @Override
                    public String call() {
                        return mediaState.getMediaPlayerName();
                    }
                },
                null);
    }

    @Test
    public void test_updateState_getMediaAPPVersion() {
        testStringState(
                MediaTestConstants.Configuration.MEDIA_APP_VERSION,
                new TestCallback() {
                    @Override
                    public String call() {
                        return mediaState.getMediaAppVersion();
                    }
                },
                null);
    }

    @Test
    public void test_isValid_validPlayerName_validChannel_validAppVersion_isValidTrue() {
        Map<String, Object> states = new HashMap<>();
        states.put("edgeMedia.playerName", "name");
        states.put("edgeMedia.channel", "channel");
        states.put("edgeMedia.appVersion", "1.0.0");
        mediaState.updateState(states);

        assertTrue(mediaState.isValid());
    }

    @Test
    public void test_isValid_validPlayerName_validChannel_noAppVersion_isValidTrue() {
        Map<String, Object> states = new HashMap<>();
        states.put("edgeMedia.playerName", "name");
        states.put("edgeMedia.channel", "channel");
        mediaState.updateState(states);

        // Is valid if playerName and channel are valid, appVersion not considered
        assertTrue(mediaState.isValid());
    }

    @Test
    public void test_isValid_validPlayerName_noChannel_validAppVersion_isValidFalse() {
        Map<String, Object> states = new HashMap<>();
        states.put("edgeMedia.playerName", "name");
        states.put("edgeMedia.appVersion", "1.0.0");
        mediaState.updateState(states);

        assertFalse(mediaState.isValid());
    }

    @Test
    public void test_isValid_noPlayerName_validChannel_validAppVersion_isValidFalse() {
        Map<String, Object> states = new HashMap<>();
        states.put("edgeMedia.channel", "channel");
        states.put("edgeMedia.appVersion", "1.0.0");
        mediaState.updateState(states);

        assertFalse(mediaState.isValid());
    }

    @Test
    public void test_isValid_validPlayerName_emptyChannel_validAppVersion_isValidFalse() {
        Map<String, Object> states = new HashMap<>();
        states.put("edgeMedia.playerName", "name");
        states.put("edgeMedia.channel", "");
        states.put("edgeMedia.appVersion", "1.0.0");
        mediaState.updateState(states);

        assertFalse(mediaState.isValid());
    }

    @Test
    public void test_isValid_emptyPlayerName_validChannel_validAppVersion_isValidFalse() {
        Map<String, Object> states = new HashMap<>();
        states.put("edgeMedia.playerName", "");
        states.put("edgeMedia.channel", "channel");
        states.put("edgeMedia.appVersion", "1.0.0");
        mediaState.updateState(states);

        assertFalse(mediaState.isValid());
    }
}

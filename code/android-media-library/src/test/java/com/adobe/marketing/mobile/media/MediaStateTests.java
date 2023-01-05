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

import com.adobe.marketing.mobile.MobilePrivacyStatus;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

interface TestCallback {
    String call();
}

interface TestIntCallback {
    Integer call();
}

public class MediaStateTests {

    MediaState mediaState;

    @Before
    public void setup() {
        mediaState = new MediaState();
    }

    public void testStringState(
            final String moduleName, final String key, final TestCallback callback) {
        testStringState(moduleName, key, callback, "");
    }

    public void testStringState(
            final String moduleName,
            final String key,
            final TestCallback callback,
            final String defaultVal) {
        Map<String, Object> states = new HashMap<>();
        assertEquals(defaultVal, callback.call());

        mediaState.notifyMobileStateChanges(moduleName, null);
        assertEquals(defaultVal, callback.call());

        // Invalid value should not change anything
        states.put(key, false);
        mediaState.notifyMobileStateChanges(moduleName, states);
        assertEquals(defaultVal, callback.call());

        states.put(key, "");
        mediaState.notifyMobileStateChanges(moduleName, states);
        assertEquals(defaultVal, callback.call());

        states.put(key, 1);
        assertEquals(defaultVal, callback.call());

        states.put(key, "value1");
        mediaState.notifyMobileStateChanges(moduleName, states);
        assertEquals("value1", callback.call());

        states.put(key, null);
        mediaState.notifyMobileStateChanges(moduleName, states);
        assertEquals("value1", callback.call());

        states.put(key, "value2");
        mediaState.notifyMobileStateChanges(moduleName, states);
        assertEquals("value2", callback.call());

        // Empty String keeps the previous value
        states.put(key, "");
        mediaState.notifyMobileStateChanges(moduleName, states);
        assertEquals("value2", callback.call());
    }

    public void testIntegerState(
            final String moduleName,
            final String key,
            final TestIntCallback callback,
            final String defaultVal) {
        Map<String, Object> states = new HashMap<>();
        assertEquals(defaultVal, callback.call());

        mediaState.notifyMobileStateChanges(moduleName, null);
        assertEquals(defaultVal, callback.call());

        // Invalid value should not change anything
        states.put(key, false);
        mediaState.notifyMobileStateChanges(moduleName, states);
        assertEquals(defaultVal, callback.call());

        states.put(key, 1);
        assertEquals(defaultVal, callback.call());

        states.put(key, "value1");
        mediaState.notifyMobileStateChanges(moduleName, states);
        assertEquals(defaultVal, callback.call());

        states.put(key, null);
        mediaState.notifyMobileStateChanges(moduleName, states);
        assertEquals(defaultVal, callback.call());

        // valid
        states.put(key, "2");
        mediaState.notifyMobileStateChanges(moduleName, states);
        assertEquals(new Integer(2), callback.call());

        states.put(key, "value1");
        mediaState.notifyMobileStateChanges(moduleName, states);
        assertEquals(new Integer(2), callback.call());

        // valid
        states.put(key, "99");
        mediaState.notifyMobileStateChanges(moduleName, states);
        assertEquals(new Integer(99), callback.call());
    }

    @Test
    public void test_getPrivacyStatus() {
        String module = MediaTestConstants.Configuration.SHARED_STATE_NAME;
        Map<String, Object> states = new HashMap<>();
        Assert.assertEquals(MobilePrivacyStatus.UNKNOWN, mediaState.getPrivacyStatus());

        states.put(
                MediaTestConstants.Configuration.GLOBAL_PRIVACY,
                MobilePrivacyStatus.OPT_OUT.getValue());
        mediaState.notifyMobileStateChanges(module, states);
        assertEquals(MobilePrivacyStatus.OPT_OUT, mediaState.getPrivacyStatus());

        // Invalid values donot change
        states.put(MediaTestConstants.Configuration.GLOBAL_PRIVACY, true);
        mediaState.notifyMobileStateChanges(module, states);
        assertEquals(MobilePrivacyStatus.OPT_OUT, mediaState.getPrivacyStatus());

        states.put(
                MediaTestConstants.Configuration.GLOBAL_PRIVACY,
                MobilePrivacyStatus.UNKNOWN.getValue());
        mediaState.notifyMobileStateChanges(module, states);
        assertEquals(MobilePrivacyStatus.UNKNOWN, mediaState.getPrivacyStatus());

        states.put(
                MediaTestConstants.Configuration.GLOBAL_PRIVACY,
                MobilePrivacyStatus.OPT_IN.getValue());
        mediaState.notifyMobileStateChanges(module, states);
        assertEquals(MobilePrivacyStatus.OPT_IN, mediaState.getPrivacyStatus());
    }

    @Test
    public void test_isSSL() {
        assertTrue(mediaState.isSsl());
    }

    @Test
    public void test_getMCOrgID() {
        testStringState(
                MediaTestConstants.Configuration.SHARED_STATE_NAME,
                MediaTestConstants.Configuration.EXPERIENCE_CLOUD_ORGID,
                new TestCallback() {
                    @Override
                    public String call() {
                        return mediaState.getMcOrgId();
                    }
                },
                null);
    }

    @Test
    public void test_getAnalyticsRSID() {
        testStringState(
                MediaTestConstants.Configuration.SHARED_STATE_NAME,
                MediaTestConstants.Configuration.ANALYTICS_RSID,
                new TestCallback() {
                    @Override
                    public String call() {
                        return mediaState.getAnalyticsRsid();
                    }
                },
                null);
    }

    @Test
    public void test_getAnalyticsTrackingServer() {
        testStringState(
                MediaTestConstants.Configuration.SHARED_STATE_NAME,
                MediaTestConstants.Configuration.ANALYTICS_TRACKING_SERVER,
                new TestCallback() {
                    @Override
                    public String call() {
                        return mediaState.getAnalyticsTrackingServer();
                    }
                },
                null);
    }

    @Test
    public void test_getMediaTrackingServer() {
        testStringState(
                MediaTestConstants.Configuration.SHARED_STATE_NAME,
                MediaTestConstants.Configuration.MEDIA_TRACKING_SERVER,
                new TestCallback() {
                    @Override
                    public String call() {
                        return mediaState.getMediaTrackingServer();
                    }
                },
                null);
    }

    @Test
    public void test_getMediaCollectionServer() {
        testStringState(
                MediaTestConstants.Configuration.SHARED_STATE_NAME,
                MediaTestConstants.Configuration.MEDIA_COLLECTION_SERVER,
                new TestCallback() {
                    @Override
                    public String call() {
                        return mediaState.getMediaCollectionServer();
                    }
                },
                null);
    }

    @Test
    public void test_getMediaChannel() {
        testStringState(
                MediaTestConstants.Configuration.SHARED_STATE_NAME,
                MediaTestConstants.Configuration.MEDIA_CHANNEL,
                new TestCallback() {
                    @Override
                    public String call() {
                        return mediaState.getMediaChannel();
                    }
                },
                "unknown");
    }

    @Test
    public void test_getMediaOVP() {
        testStringState(
                MediaTestConstants.Configuration.SHARED_STATE_NAME,
                MediaTestConstants.Configuration.MEDIA_OVP,
                new TestCallback() {
                    @Override
                    public String call() {
                        return mediaState.getMediaOVP();
                    }
                },
                null);
    }

    @Test
    public void test_getMediaPlayerName() {
        testStringState(
                MediaTestConstants.Configuration.SHARED_STATE_NAME,
                MediaTestConstants.Configuration.MEDIA_PLAYER_NAME,
                new TestCallback() {
                    @Override
                    public String call() {
                        return mediaState.getMediaPlayerName();
                    }
                },
                "unknown");
    }

    @Test
    public void test_getMediaAPPVersion() {
        testStringState(
                MediaTestConstants.Configuration.SHARED_STATE_NAME,
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
    public void test_getMediaDebugLogging() {
        String module = MediaTestConstants.Configuration.SHARED_STATE_NAME;
        Map<String, Object> states = new HashMap<>();
        assertFalse(mediaState.isMediaDebugLoggingEnabled());

        states.put(MediaTestConstants.Configuration.MEDIA_DEBUG_LOGGING, "");
        mediaState.notifyMobileStateChanges(module, states);
        assertFalse(mediaState.isMediaDebugLoggingEnabled());

        states.put(MediaTestConstants.Configuration.MEDIA_DEBUG_LOGGING, true);
        mediaState.notifyMobileStateChanges(module, states);
        assertTrue(mediaState.isMediaDebugLoggingEnabled());
    }

    @Test
    public void test_getMCID() {
        testStringState(
                MediaTestConstants.Identity.SHARED_STATE_NAME,
                MediaTestConstants.Identity.MARKETING_VISITOR_ID,
                new TestCallback() {
                    @Override
                    public String call() {
                        return mediaState.getMcid();
                    }
                },
                null);
    }

    @Test
    public void test_getBlob() {
        testStringState(
                MediaTestConstants.Identity.SHARED_STATE_NAME,
                MediaTestConstants.Identity.BLOB,
                new TestCallback() {
                    @Override
                    public String call() {
                        return mediaState.getBlob();
                    }
                },
                null);
    }

    @Test
    public void test_getLocHint() {
        testIntegerState(
                MediaTestConstants.Identity.SHARED_STATE_NAME,
                MediaTestConstants.Identity.LOC_HINT,
                new TestIntCallback() {
                    @Override
                    public Integer call() {
                        return mediaState.getLocHint();
                    }
                },
                null);
    }

    @Test
    public void test_getAID() {
        testStringState(
                MediaTestConstants.Analytics.SHARED_STATE_NAME,
                MediaTestConstants.Analytics.ANALYTICS_VISITOR_ID,
                new TestCallback() {
                    @Override
                    public String call() {
                        return mediaState.getAid();
                    }
                },
                null);
    }

    @Test
    public void test_getVID() {
        testStringState(
                MediaTestConstants.Analytics.SHARED_STATE_NAME,
                MediaTestConstants.Analytics.VISITOR_ID,
                new TestCallback() {
                    @Override
                    public String call() {
                        return mediaState.getVid();
                    }
                },
                null);
    }

    @Test
    public void test_getAssuranceIntegrationId() {
        testStringState(
                MediaTestConstants.Assurance.SHARED_STATE_NAME,
                MediaTestConstants.Assurance.INTEGRATION_ID,
                new TestCallback() {
                    @Override
                    public String call() {
                        return mediaState.getAssuranceIntegrationId();
                    }
                },
                null);
    }
}

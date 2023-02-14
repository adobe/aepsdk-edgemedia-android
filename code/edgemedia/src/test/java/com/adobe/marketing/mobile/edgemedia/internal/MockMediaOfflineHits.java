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

import com.adobe.marketing.mobile.Media;
import com.adobe.marketing.mobile.MobilePrivacyStatus;
import com.adobe.marketing.mobile.VisitorID;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class MockMediaOfflineHits {
    String mediaLibraryVersion = "android-media-" + Media.extensionVersion();

    Map<String, Object> analyticsSharedState;
    Map<String, Object> identitySharedState;
    Map<String, Object> configSharedState;
    Map<String, Object> configSharedStateOptOut;
    Map<String, Object> configSharedStateUnknown;
    Map<String, Object> configSharedStateOptIn;
    Map<String, Object> assuranceSharedState;

    MediaState mediaState;
    MediaState mediaStateEmpty;
    MediaState mediaStateLocHintException;

    MediaHit sessionStart;
    String sessionStartJson;
    String sessionStartJsonWithState;
    String sessionStartJsonWithConfigurationIdentityState;

    MediaHit sessionStartChannel;
    String sessionStartChannelJson;

    MediaHit adBreakStart;
    String adBreakStartJson;

    MediaHit adBreakComplete;
    String adBreakCompleteJson;

    MediaHit adStart;
    String adStartJson;
    String adStartJsonWithState;

    MediaHit adComplete;
    String adCompleteJson;

    MediaHit play;
    String playJson;

    MediaHit pause;
    String pauseJson;

    MediaHit ping;
    String pingJson;

    MediaHit complete;
    String completeJson;

    String forceSessionEndJson;
    String forceSessionEndAfterRelaunchJson;

    MockMediaOfflineHits() {
        mediaStateEmpty = new MediaState();
        {
            // config
            configSharedStateOptOut = new HashMap<>();
            configSharedStateOptOut.put(
                    MediaTestConstants.Configuration.GLOBAL_PRIVACY,
                    MobilePrivacyStatus.OPT_OUT.getValue());

            configSharedStateOptIn = new HashMap<>();
            configSharedStateOptIn.put(
                    MediaTestConstants.Configuration.GLOBAL_PRIVACY,
                    MobilePrivacyStatus.OPT_IN.getValue());

            configSharedStateUnknown = new HashMap<>();
            configSharedStateUnknown.put(
                    MediaTestConstants.Configuration.GLOBAL_PRIVACY,
                    MobilePrivacyStatus.UNKNOWN.getValue());

            configSharedState = new HashMap<>();
            configSharedState.put(
                    MediaTestConstants.Configuration.GLOBAL_PRIVACY,
                    MobilePrivacyStatus.OPT_IN.getValue());
            configSharedState.put(
                    MediaTestConstants.Configuration.EXPERIENCE_CLOUD_ORGID, "org_id");
            configSharedState.put(MediaTestConstants.Configuration.ANALYTICS_RSID, "rsid");
            configSharedState.put(
                    MediaTestConstants.Configuration.ANALYTICS_TRACKING_SERVER, "analytics_server");
            configSharedState.put(
                    MediaTestConstants.Configuration.MEDIA_TRACKING_SERVER, "media_server");
            configSharedState.put(
                    MediaTestConstants.Configuration.MEDIA_COLLECTION_SERVER,
                    "Media_collection_server");
            configSharedState.put(MediaTestConstants.Configuration.MEDIA_CHANNEL, "channel");
            configSharedState.put(MediaTestConstants.Configuration.MEDIA_OVP, "ovp");
            configSharedState.put(
                    MediaTestConstants.Configuration.MEDIA_PLAYER_NAME, "player_name");
            configSharedState.put(
                    MediaTestConstants.Configuration.MEDIA_APP_VERSION, "app_version");
            configSharedState.put(MediaTestConstants.Configuration.MEDIA_DEBUG_LOGGING, false);

            // identity
            identitySharedState = new HashMap<>();
            identitySharedState.put(MediaTestConstants.Identity.LOC_HINT, "9");
            identitySharedState.put(MediaTestConstants.Identity.BLOB, "blob");
            identitySharedState.put(MediaTestConstants.Identity.MARKETING_VISITOR_ID, "mid");

            List<VisitorID> visitorIDList = new ArrayList<VisitorID>();
            visitorIDList.add(
                    new VisitorID(
                            "d_cid_ic",
                            "id_type1",
                            "u111111111",
                            VisitorID.AuthenticationState.UNKNOWN));
            visitorIDList.add(
                    new VisitorID(
                            "d_cid_ic",
                            "id_type2",
                            "1234567890",
                            VisitorID.AuthenticationState.AUTHENTICATED));
            visitorIDList.add(
                    new VisitorID(
                            "d_cid_ic",
                            "id_type3",
                            "testPushId",
                            VisitorID.AuthenticationState.LOGGED_OUT));
            identitySharedState.put(
                    MediaTestConstants.Identity.VISITOR_IDS_LIST, convertVisitorIds(visitorIDList));

            // Assurance
            assuranceSharedState = new HashMap<>();
            assuranceSharedState.put(
                    MediaTestConstants.Assurance.INTEGRATION_ID, "integrationId12345");

            // analytics
            analyticsSharedState = new HashMap<>();
            analyticsSharedState.put(MediaTestConstants.Analytics.VISITOR_ID, "vid");
            analyticsSharedState.put(MediaTestConstants.Analytics.ANALYTICS_VISITOR_ID, "aid");

            {
                mediaState = new MediaState();
                mediaState.notifyMobileStateChanges(
                        MediaTestConstants.Configuration.SHARED_STATE_NAME, configSharedState);
                mediaState.notifyMobileStateChanges(
                        MediaTestConstants.Identity.SHARED_STATE_NAME, identitySharedState);
                mediaState.notifyMobileStateChanges(
                        MediaTestConstants.Analytics.SHARED_STATE_NAME, analyticsSharedState);
                mediaState.notifyMobileStateChanges(
                        MediaTestConstants.Assurance.SHARED_STATE_NAME, assuranceSharedState);
            }

            {
                mediaStateLocHintException = new MediaState();
                Map<String, Object> identityEventData = new HashMap<>();
                identityEventData.put(MediaInternalConstants.Identity.LOC_HINT, "exception");
                mediaStateLocHintException.notifyMobileStateChanges(
                        MediaInternalConstants.Identity.SHARED_STATE_NAME, identityEventData);
            }

            Map<String, String> emptyStringMap = new HashMap<String, String>();
            Map<String, Object> emptyObjectMap = new HashMap<String, Object>();

            // Session Start
            {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("media.name", "media_name");
                params.put("media.id", "media_id");
                params.put("media.streamType", "video");
                params.put("media.contentType", "vod");
                params.put("media.length", 1800d);
                params.put("media.resume", false);
                params.put("media.downloaded", true);

                Map<String, String> metadata = new HashMap<String, String>();
                metadata.put("key1", "value1");

                Map<String, Object> qoe = new HashMap<String, Object>();
                qoe.put("media.qoe.bitrate", 100000d);
                qoe.put("media.qoe.droppedFrames", 2d);
                qoe.put("media.qoe.framesPerSecond", 23.5d);
                qoe.put("media.qoe.timeToStart", 20d);

                sessionStart =
                        new MediaHit(
                                MediaCollectionTestConstants.EventType.SESSION_START,
                                params,
                                metadata,
                                qoe,
                                0,
                                0);

                sessionStartJson =
                        "  {"
                                + "    \"playerTime\" : {"
                                + "      \"playhead\" : 0,"
                                + "      \"ts\" : 0"
                                + "    },"
                                + "    \"customMetadata\" : {"
                                + "      \"key1\" : \"value1\""
                                + "    },"
                                + "    \"eventType\" : \"sessionStart\","
                                + "    \"params\" : {"
                                + "      \"media.name\" : \"media_name\","
                                + "      \"media.id\" : \"media_id\","
                                + "      \"media.streamType\" : \"video\","
                                + "      \"media.contentType\" : \"vod\","
                                + "      \"media.length\" : 1800,"
                                + "      \"media.downloaded\" : true,"
                                + "      \"media.resume\" : false,"
                                + "      \"media.channel\" : \"unknown\","
                                + "      \"media.playerName\" : \"unknown\","
                                + "      \"analytics.enableSSL\":true,"
                                + "      \"media.libraryVersion\":\""
                                + mediaLibraryVersion
                                + "\""
                                + "    },"
                                + "    \"qoeData\" : {"
                                + "      \"media.qoe.bitrate\" : 100000,"
                                + "      \"media.qoe.droppedFrames\" : 2,"
                                + "      \"media.qoe.framesPerSecond\" : 23.5,"
                                + "      \"media.qoe.timeToStart\" : 20"
                                + "    }"
                                + "  }";

                // channel already present in media hit.
                params.put("media.channel", "media_channel");
                sessionStartChannel =
                        new MediaHit(
                                MediaCollectionTestConstants.EventType.SESSION_START,
                                params,
                                metadata,
                                qoe,
                                0,
                                0);

                sessionStartChannelJson =
                        "  {"
                                + "    \"playerTime\" : {"
                                + "      \"playhead\" : 0,"
                                + "      \"ts\" : 0"
                                + "    },"
                                + "    \"customMetadata\" : {"
                                + "      \"key1\" : \"value1\""
                                + "    },"
                                + "    \"eventType\" : \"sessionStart\","
                                + "    \"params\" : {"
                                + "      \"media.name\" : \"media_name\","
                                + "      \"media.id\" : \"media_id\","
                                + "      \"media.streamType\" : \"video\","
                                + "      \"media.contentType\" : \"vod\","
                                + "      \"media.length\" : 1800,"
                                + "      \"media.downloaded\" : true,"
                                + "      \"media.resume\" : false,"
                                + "      \"media.channel\" : \"media_channel\","
                                + "      \"media.playerName\" : \"unknown\","
                                + "      \"analytics.enableSSL\":true,"
                                + "      \"media.libraryVersion\":\""
                                + mediaLibraryVersion
                                + "\""
                                + "    },"
                                + "    \"qoeData\" : {"
                                + "      \"media.qoe.bitrate\" : 100000,"
                                + "      \"media.qoe.droppedFrames\" : 2,"
                                + "      \"media.qoe.framesPerSecond\" : 23.5,"
                                + "      \"media.qoe.timeToStart\" : 20"
                                + "    }"
                                + "  }";

                sessionStartJsonWithState =
                        "  {"
                                + "    \"playerTime\" : {"
                                + "      \"playhead\" : 0,"
                                + "      \"ts\" : 0"
                                + "    },"
                                + "    \"customMetadata\" : {"
                                + "      \"key1\" : \"value1\""
                                + "    },"
                                + "    \"eventType\" : \"sessionStart\","
                                + "    \"params\" : {"
                                + "      \"media.name\" : \"media_name\","
                                + "      \"media.id\" : \"media_id\","
                                + "      \"media.streamType\" : \"video\","
                                + "      \"media.contentType\" : \"vod\","
                                + "      \"media.length\" : 1800,"
                                + "      \"media.downloaded\" : true,"
                                + "      \"media.resume\" : false,"
                                + "      \"media.channel\" : \"channel\","
                                + "      \"media.playerName\" : \"player_name\","
                                + "      \"analytics.enableSSL\" : true,"
                                + "      \"analytics.trackingServer\" : \"analytics_server\","
                                + "      \"analytics.reportSuite\" : \"rsid\","
                                + "      \"analytics.visitorId\" : \"vid\","
                                + "      \"analytics.aid\" : \"aid\","
                                + "      \"visitor.marketingCloudOrgId\" : \"org_id\","
                                + "      \"visitor.marketingCloudUserId\" : \"mid\","
                                + "      \"visitor.aamLocationHint\" : 9,"
                                + "      \"media.sdkVersion\" : \"app_version\","
                                + "      \"media.libraryVersion\":\""
                                + mediaLibraryVersion
                                + "\","
                                + "      \"visitor.customerIDs\": {"
                                + "           \"id_type1\": {"
                                + "               \"id\": \"u111111111\","
                                + "               \"authState\": 0"
                                + "           },"
                                + "           \"id_type2\": {"
                                + "               \"id\": \"1234567890\","
                                + "               \"authState\": 1"
                                + "           },"
                                + "           \"id_type3\": {"
                                + "               \"id\": \"testPushId\","
                                + "               \"authState\": 2"
                                + "           }"
                                + "       }"
                                + "    },"
                                + "    \"qoeData\" : {"
                                + "      \"media.qoe.bitrate\" : 100000,"
                                + "      \"media.qoe.droppedFrames\" : 2,"
                                + "      \"media.qoe.framesPerSecond\" : 23.5,"
                                + "      \"media.qoe.timeToStart\" : 20"
                                + "    }"
                                + "  }";

                sessionStartJsonWithConfigurationIdentityState =
                        " {"
                                + "    \"playerTime\" : {"
                                + "      \"playhead\" : 0,"
                                + "      \"ts\" : 0"
                                + "    },"
                                + "    \"customMetadata\" : {"
                                + "      \"key1\" : \"value1\""
                                + "    },"
                                + "    \"eventType\" : \"sessionStart\","
                                + "    \"params\" : {"
                                + "      \"media.name\" : \"media_name\","
                                + "      \"media.id\" : \"media_id\","
                                + "      \"media.streamType\" : \"video\","
                                + "      \"media.contentType\" : \"vod\","
                                + "      \"media.length\" : 1800,"
                                + "      \"media.downloaded\" : true,"
                                + "      \"media.resume\" : false,"
                                + "      \"media.channel\" : \"channel\","
                                + "      \"media.playerName\" : \"player_name\","
                                + "      \"analytics.enableSSL\" : true,"
                                + "      \"analytics.trackingServer\" : \"analytics_server\","
                                + "      \"analytics.reportSuite\" : \"rsid\","
                                + "      \"visitor.marketingCloudOrgId\" : \"org_id\","
                                + "      \"visitor.marketingCloudUserId\" : \"mid\","
                                + "      \"visitor.aamLocationHint\" : 9,"
                                + "      \"media.sdkVersion\" : \"app_version\","
                                + "      \"media.libraryVersion\":\""
                                + mediaLibraryVersion
                                + "\","
                                + "      \"visitor.customerIDs\": {"
                                + "           \"id_type1\": {"
                                + "               \"id\": \"u111111111\","
                                + "               \"authState\": 0"
                                + "           },"
                                + "           \"id_type2\": {"
                                + "               \"id\": \"1234567890\","
                                + "               \"authState\": 1"
                                + "           },"
                                + "           \"id_type3\": {"
                                + "               \"id\": \"testPushId\","
                                + "               \"authState\": 2"
                                + "           }"
                                + "       }"
                                + "    },"
                                + "    \"qoeData\" : {"
                                + "      \"media.qoe.bitrate\" : 100000,"
                                + "      \"media.qoe.droppedFrames\" : 2,"
                                + "      \"media.qoe.framesPerSecond\" : 23.5,"
                                + "      \"media.qoe.timeToStart\" : 20"
                                + "    }"
                                + "  }";
            }

            // AdBreak Start
            {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("media.ad.podFriendlyName", "adbreak_name");
                params.put("media.ad.podIndex", 1);
                params.put("media.ad.podSecond", 10.0d);

                adBreakStart =
                        new MediaHit(
                                MediaCollectionTestConstants.EventType.ADBREAK_START,
                                params,
                                emptyStringMap,
                                emptyObjectMap,
                                10,
                                10000);

                adBreakStartJson =
                        "  {"
                                + "    \"playerTime\" : {"
                                + "      \"playhead\" : 10,"
                                + "      \"ts\" : 10000"
                                + "    },"
                                + "    \"eventType\" : \"adBreakStart\","
                                + "    \"params\" : {"
                                + "    \"media.ad.podFriendlyName\" : \"adbreak_name\","
                                + "    \"media.ad.podIndex\" : 1,"
                                + "    \"media.ad.podSecond\" : 10"
                                + "    }"
                                + "  }";
            }

            // AdBreak Complete
            {
                adBreakComplete =
                        new MediaHit(
                                MediaCollectionTestConstants.EventType.ADBREAK_COMPLETE,
                                emptyObjectMap,
                                emptyStringMap,
                                emptyObjectMap,
                                10,
                                30000);

                adBreakCompleteJson =
                        "  {"
                                + "    \"playerTime\" : {"
                                + "      \"playhead\" : 10,"
                                + "      \"ts\" : 30000"
                                + "    },"
                                + "    \"eventType\" : \"adBreakComplete\""
                                + "  }";
            }

            // Ad Start
            {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("media.ad.id", "ad_id");
                params.put("media.ad.name", "ad_name");
                params.put("media.ad.podPosition", 1);
                params.put("media.ad.length", 20d);

                adStart =
                        new MediaHit(
                                MediaCollectionTestConstants.EventType.AD_START,
                                params,
                                emptyStringMap,
                                emptyObjectMap,
                                10,
                                10000);

                adStartJson =
                        "  {"
                                + "    \"playerTime\" : {"
                                + "      \"playhead\" : 10,"
                                + "      \"ts\" : 10000"
                                + "    },"
                                + "    \"eventType\" : \"adStart\","
                                + "    \"params\" : {"
                                + "    \"media.ad.id\" : \"ad_id\","
                                + "    \"media.ad.name\" : \"ad_name\","
                                + "    \"media.ad.length\" : 20,"
                                + "    \"media.ad.podPosition\" : 1,"
                                + "    \"media.ad.playerName\" : \"unknown\""
                                + "    }"
                                + "  }";

                adStartJsonWithState =
                        "  {"
                                + "    \"playerTime\" : {"
                                + "      \"playhead\" : 10,"
                                + "      \"ts\" : 10000"
                                + "    },"
                                + "    \"eventType\" : \"adStart\","
                                + "    \"params\" : {"
                                + "    \"media.ad.id\" : \"ad_id\","
                                + "    \"media.ad.name\" : \"ad_name\","
                                + "    \"media.ad.length\" : 20,"
                                + "    \"media.ad.podPosition\" : 1,"
                                + "    \"media.ad.playerName\" : \"player_name\""
                                + "    }"
                                + "  }";
            }

            // Ad Complete
            {
                adComplete =
                        new MediaHit(
                                MediaCollectionTestConstants.EventType.AD_COMPLETE,
                                emptyObjectMap,
                                emptyStringMap,
                                emptyObjectMap,
                                10,
                                30000);

                adCompleteJson =
                        "  {"
                                + "    \"playerTime\" : {"
                                + "      \"playhead\" : 10,"
                                + "      \"ts\" : 30000"
                                + "    },"
                                + "    \"eventType\" : \"adComplete\""
                                + "  }";
            }

            // Play
            {
                play =
                        new MediaHit(
                                MediaCollectionTestConstants.EventType.PLAY,
                                emptyObjectMap,
                                emptyStringMap,
                                emptyObjectMap,
                                0,
                                100);

                playJson =
                        "  {"
                                + "    \"playerTime\" : {"
                                + "      \"playhead\" : 0,"
                                + "      \"ts\" : 100"
                                + "    },"
                                + "    \"eventType\" : \"play\""
                                + "  }";
            }

            // Pause
            {
                pause =
                        new MediaHit(
                                MediaCollectionTestConstants.EventType.PAUSE_START,
                                emptyObjectMap,
                                emptyStringMap,
                                emptyObjectMap,
                                0,
                                100);

                pauseJson =
                        "  {"
                                + "    \"playerTime\" : {"
                                + "      \"playhead\" : 0,"
                                + "      \"ts\" : 100"
                                + "    },"
                                + "    \"eventType\" : \"pauseStart\""
                                + "  }";
            }

            // Ping
            {
                ping =
                        new MediaHit(
                                MediaCollectionTestConstants.EventType.PING,
                                emptyObjectMap,
                                emptyStringMap,
                                emptyObjectMap,
                                45,
                                65000);

                pingJson =
                        "  {"
                                + "    \"playerTime\" : {"
                                + "      \"playhead\" : 45,"
                                + "      \"ts\" : 65000"
                                + "    },"
                                + "    \"eventType\" : \"ping\""
                                + "  }";
            }

            // Session End
            {
                forceSessionEndJson =
                        "  {"
                                + " \"playerTime\" : {"
                                + "      \"playhead\" : 45,"
                                + "      \"ts\" : 65000"
                                + "    },"
                                + "    \"eventType\" : \"sessionEnd\""
                                + "  }";
            }

            // Session End After relaunch
            {
                forceSessionEndAfterRelaunchJson =
                        "  {"
                                + " \"playerTime\" : {"
                                + "      \"playhead\" : 0,"
                                + "      \"ts\" : 100"
                                + "    },"
                                + "    \"eventType\" : \"sessionEnd\""
                                + "  }";
            }

            // Complete
            {
                complete =
                        new MediaHit(
                                MediaCollectionTestConstants.EventType.SESSION_COMPLETE,
                                emptyObjectMap,
                                emptyStringMap,
                                emptyObjectMap,
                                60,
                                80000);

                completeJson =
                        "  {"
                                + " \"playerTime\" : {"
                                + "      \"playhead\" : 60,"
                                + "      \"ts\" : 80000"
                                + "    },"
                                + "    \"eventType\" : \"sessionComplete\""
                                + "  }";
            }
        }
    }

    public boolean compareReport(final String expectedHit, final String result) {
        try {
            JSONObject expectedJson = new JSONObject(expectedHit);
            JSONObject actualJson = new JSONObject(result);

            if (expectedJson == null || actualJson == null) {
                return false;
            }

            return equals(expectedJson, actualJson);
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean compareReport(final List<String> expectedJsonHits, final String result) {
        try {
            JSONArray resultJson = new JSONArray(result);

            if (resultJson == null) {
                return false;
            }

            if (resultJson.length() != expectedJsonHits.size()) {
                return false;
            }

            for (int i = 0; i < expectedJsonHits.size(); i++) {
                JSONObject expectedObj = new JSONObject(expectedJsonHits.get(i));
                JSONObject actualObj = resultJson.getJSONObject(i);
                if (!equals(expectedObj, actualObj)) {
                    return false;
                }
            }

            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    boolean equals(JSONObject obj1, JSONObject obj2) {
        if (obj1 == null && obj2 == null) return true;
        try {
            Map<String, Object> obj1Map = toMap(obj1);
            Map<String, Object> obj2Map = toMap(obj2);
            return obj1Map.equals(obj2Map);
        } catch (Exception ex) {
            return false;
        }
    }

    Map<String, Object> toMap(JSONObject obj) throws JSONException {
        if (obj == null) return null;

        Map<String, Object> ret = new HashMap<>();
        for (Iterator<String> it = obj.keys(); it.hasNext(); ) {
            String key = it.next();
            Object value = obj.get(key);
            if (value == JSONObject.NULL) {
                ret.put(key, null);
            } else if (value instanceof JSONObject) {
                ret.put(key, toMap((JSONObject) value));
            } else if (value instanceof JSONArray) {
                ret.put(key, toList((JSONArray) value));
            } else {
                ret.put(key, value);
            }
        }

        return ret;
    }

    List<Object> toList(JSONArray array) throws JSONException {
        if (array == null) return null;
        List<Object> ret = new ArrayList<>();
        for (int i = 0; i < array.length(); ++i) {
            Object value = array.get(i);
            if (value == JSONObject.NULL) {
                ret.add(null);
            } else if (value instanceof JSONObject) {
                ret.add(toMap((JSONObject) value));
            } else if (value instanceof JSONArray) {
                ret.add(toList((JSONArray) value));
            } else {
                ret.add(value);
            }
        }
        return ret;
    }

    private Map<String, Object> convertVisitorId(final VisitorID visitorID) {
        Map<String, Object> data = new HashMap<>();
        data.put(MediaInternalConstants.Identity.VISITOR_ID_KEYS_ID, visitorID.getId());
        data.put(
                MediaInternalConstants.Identity.VISITOR_ID_KEYS_ID_ORIGIN, visitorID.getIdOrigin());
        data.put(MediaInternalConstants.Identity.VISITOR_ID_KEYS_ID_TYPE, visitorID.getIdType());
        data.put(
                MediaInternalConstants.Identity.VISITOR_ID_KEYS_STATE,
                visitorID.getAuthenticationState().getValue());
        return data;
    }

    private List<Map<String, Object>> convertVisitorIds(final List<VisitorID> visitorIDList) {
        List<Map<String, Object>> data = new ArrayList<>();
        for (VisitorID vId : visitorIDList) {
            if (vId != null) {
                data.add(convertVisitorId(vId));
            }
        }
        return data;
    }
}

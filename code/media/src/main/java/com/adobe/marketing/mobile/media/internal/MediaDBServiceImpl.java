/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.media.internal;

import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.util.DataReader;
import com.adobe.marketing.mobile.util.FileUtils;
import com.adobe.marketing.mobile.util.JSONUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;

class MediaDBServiceImpl implements MediaDBService {
    private static final String LOG_TAG = "MediaDBService";
    private static final String MEDIA_DB_FILE_NAME = MediaInternalConstants.Media.SHARED_STATE_NAME;
    private static final String DEPRECATED_2X_DB_FILE_NAME = "ADBMobileMedia.sqlite";

    private MediaDatabase database;

    private static final String KEY_EVENT_TYPE = "eventtype";
    private static final String KEY_PARAMS = "params";
    private static final String KEY_METADATA = "metadata";
    private static final String KEY_QOE_DATA = "qoedata";
    private static final String KEY_PLAYHEAD = "playhead";
    private static final String KEY_TIMESTAMP = "timestamp";

    MediaDBServiceImpl() {
        try {
            // Delete deprecated 2X database from cache directory.
            if (FileUtils.deleteFileFromCacheDir(DEPRECATED_2X_DB_FILE_NAME)) {
                Log.debug(
                        MediaInternalConstants.EXTENSION_LOG_TAG,
                        LOG_TAG,
                        "Media 2.x database file (%s) deleted.",
                        DEPRECATED_2X_DB_FILE_NAME);
            }

            database = new MediaDatabase(MEDIA_DB_FILE_NAME);
        } catch (Exception ex) {
            Log.trace(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "Error opening media database (%s)",
                    ex.getLocalizedMessage());
            database = null;
        }
    }

    private String serializeHit(final MediaHit mediaHit) {
        if (mediaHit == null) {
            return null;
        }

        Map<String, Object> mediaHitMap = new HashMap<>();
        mediaHitMap.put(KEY_EVENT_TYPE, mediaHit.getEventType());
        mediaHitMap.put(KEY_PARAMS, mediaHit.getParams());
        mediaHitMap.put(KEY_METADATA, mediaHit.getCustomMetadata());
        mediaHitMap.put(KEY_QOE_DATA, mediaHit.getQoEData());
        mediaHitMap.put(KEY_PLAYHEAD, mediaHit.getPlayhead());
        mediaHitMap.put(KEY_TIMESTAMP, mediaHit.getTimeStamp());
        return new JSONObject(mediaHitMap).toString();
    }

    private MediaHit deserializeHit(final String mediaHitStr) {
        try {
            Map<String, Object> mediaHitMap = JSONUtils.toMap(new JSONObject(mediaHitStr));

            String eventType = DataReader.optString(mediaHitMap, KEY_EVENT_TYPE, null);
            if (eventType == null) {
                return null;
            }

            Map<String, Object> params =
                    DataReader.optTypedMap(Object.class, mediaHitMap, KEY_PARAMS, null);
            Map<String, String> metadata = DataReader.optStringMap(mediaHitMap, KEY_METADATA, null);
            Map<String, Object> qoeData =
                    DataReader.optTypedMap(Object.class, mediaHitMap, KEY_QOE_DATA, null);
            double playhead = DataReader.optDouble(mediaHitMap, KEY_PLAYHEAD, 0);
            long timestamp = DataReader.optLong(mediaHitMap, KEY_TIMESTAMP, 0);

            return new MediaHit(eventType, params, metadata, qoeData, playhead, timestamp);
        } catch (JSONException ex) {
            return null;
        }
    }

    public Set<String> getSessionIDs() {
        if (database == null) {
            return new HashSet<>();
        }

        return database.getSessionIDs();
    }

    public List<MediaHit> getHits(final String sessionID) {
        List<MediaHit> ret = new ArrayList<>();
        if (database == null) {
            return ret;
        }

        List<String> dbHits = database.getHits(sessionID);
        if (dbHits == null || dbHits.size() == 0) {
            return ret;
        }

        for (String dbHit : dbHits) {
            MediaHit mediaHit = deserializeHit(dbHit);
            if (mediaHit != null) {
                ret.add(mediaHit);
            }
        }

        return ret;
    }

    public boolean persistHit(final String sessionID, final MediaHit hit) {
        if (database == null) {
            return false;
        }
        String hitStr = serializeHit(hit);
        if (hitStr == null) {
            return false;
        }

        return database.persistHit(sessionID, hitStr);
    }

    public boolean deleteHits(final String sessionID) {
        if (database == null) {
            return false;
        }
        return database.deleteHits(sessionID);
    }

    public void deleteAllHits() {
        if (database != null) {
            database.deleteAllHits();
        }
    }
}

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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.ServiceProvider;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class MediaDatabase {
    private static final String LOG_TAG = "MediaDatabase";
    private static final String MEDIA_TABLE_NAME = "MEDIAHITS";

    private static final String TB_KEY_ID = "id";
    private static final String TB_KEY_SESSION_ID = "sessionId";
    private static final String TB_KEY_DATA = "data";

    private final String dbPath;
    private final Object dbMutex;

    MediaDatabase(final String dbName) {
        Context context =
                ServiceProvider.getInstance().getAppContextService().getApplicationContext();
        if (context == null) {
            throw new IllegalStateException("Context is null");
        }
        File database = context.getDatabasePath(dbName);
        dbPath = database.getPath();
        dbMutex = new Object();
        createTableIfNotExist();
    }

    Set<String> getSessionIDs() {
        Set<String> ret = new HashSet<>();

        SQLiteDatabase database = null;
        synchronized (dbMutex) {
            try {
                database = openDatabase();
                Cursor cursor =
                        database.query(
                                true,
                                MEDIA_TABLE_NAME,
                                new String[] {TB_KEY_SESSION_ID},
                                null,
                                null,
                                null,
                                null,
                                null,
                                null);
                if (cursor.moveToFirst()) {
                    do {
                        ContentValues contentValues = new ContentValues();
                        DatabaseUtils.cursorRowToContentValues(cursor, contentValues);
                        ret.add(contentValues.getAsString(TB_KEY_SESSION_ID));
                    } while (cursor.moveToNext());
                }
            } catch (final SQLiteException e) {
                Log.warning(
                        MediaInternalConstants.EXTENSION_LOG_TAG,
                        LOG_TAG,
                        "Error getting session ids from table (%s). Error: (%s)",
                        MEDIA_TABLE_NAME,
                        e.getLocalizedMessage());
            } finally {
                closeDatabase(database);
            }
        }

        return ret;
    }

    List<String> getHits(final String sessionID) {
        ArrayList<String> ret = new ArrayList<>();

        SQLiteDatabase database = null;
        synchronized (dbMutex) {
            try {
                database = openDatabase();
                Cursor cursor =
                        database.query(
                                MEDIA_TABLE_NAME,
                                new String[] {TB_KEY_DATA},
                                TB_KEY_SESSION_ID + "= ?",
                                new String[] {sessionID},
                                null,
                                null,
                                "id ASC");
                if (cursor.moveToFirst()) {
                    do {
                        ContentValues contentValues = new ContentValues();
                        DatabaseUtils.cursorRowToContentValues(cursor, contentValues);
                        ret.add(contentValues.getAsString(TB_KEY_DATA));
                    } while (cursor.moveToNext());
                }
            } catch (final SQLiteException e) {
                Log.warning(
                        MediaInternalConstants.EXTENSION_LOG_TAG,
                        LOG_TAG,
                        "Error getting session ids from table (%s). Error: (%s)",
                        MEDIA_TABLE_NAME,
                        e.getLocalizedMessage());
            } finally {
                closeDatabase(database);
            }
        }

        return ret;
    }

    boolean persistHit(final String sessionId, final String hit) {
        SQLiteDatabase database = null;
        synchronized (dbMutex) {
            try {
                database = openDatabase();
                final int INDEX_SESSION_ID = 1;
                final int INDEX_DATA = 2;
                SQLiteStatement insertStatement =
                        database.compileStatement(
                                "INSERT INTO "
                                        + MEDIA_TABLE_NAME
                                        + " ("
                                        + TB_KEY_SESSION_ID
                                        + ","
                                        + TB_KEY_DATA
                                        + ") VALUES (?, ?)");

                insertStatement.bindString(INDEX_SESSION_ID, sessionId);
                insertStatement.bindString(INDEX_DATA, hit);
                long rowId = insertStatement.executeInsert();
                return rowId >= 0;
            } catch (final SQLiteException e) {
                Log.warning(
                        MediaInternalConstants.EXTENSION_LOG_TAG,
                        LOG_TAG,
                        "Error getting session ids from table (%s). Error: (%s)",
                        MEDIA_TABLE_NAME,
                        e.getLocalizedMessage());
                return false;
            } finally {
                closeDatabase(database);
            }
        }
    }

    boolean deleteHits(final String sessionID) {
        boolean ret = false;

        SQLiteDatabase database = null;
        synchronized (dbMutex) {
            try {
                database = openDatabase();
                int count =
                        database.delete(
                                MEDIA_TABLE_NAME,
                                TB_KEY_SESSION_ID + " = ?",
                                new String[] {sessionID});
                ret = count > 0;
            } catch (final SQLiteException e) {
                Log.warning(
                        MediaInternalConstants.EXTENSION_LOG_TAG,
                        LOG_TAG,
                        "Error clearing table (%s). Error: (%s)",
                        MEDIA_TABLE_NAME,
                        e.getLocalizedMessage());
            } finally {
                closeDatabase(database);
            }
        }

        return ret;
    }

    boolean deleteAllHits() {
        boolean ret = false;

        SQLiteDatabase database = null;
        synchronized (dbMutex) {
            try {
                database = openDatabase();
                database.delete(MEDIA_TABLE_NAME, null, null);
                ret = true;
            } catch (final SQLiteException e) {
                Log.warning(
                        MediaInternalConstants.EXTENSION_LOG_TAG,
                        LOG_TAG,
                        "Error clearing table (%s). Error: (%s)",
                        MEDIA_TABLE_NAME,
                        e.getLocalizedMessage());
            } finally {
                closeDatabase(database);
            }
        }

        return ret;
    }

    private boolean createTableIfNotExist() {
        SQLiteDatabase database = null;

        String query =
                "CREATE TABLE "
                        + MEDIA_TABLE_NAME
                        + "("
                        + TB_KEY_ID
                        + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,"
                        + TB_KEY_SESSION_ID
                        + " TEXT NOT NULL,"
                        + TB_KEY_DATA
                        + " TEXT"
                        + ")";

        try {
            database = openDatabase();
            database.execSQL(query);
            return true;
        } catch (final SQLiteException e) {
            Log.warning(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "createTableIfNotExists - Error in creating/accessing table. Error: (%s)",
                    e.getMessage());
            return false;
        } finally {
            closeDatabase(database);
        }
    }

    private SQLiteDatabase openDatabase() throws SQLiteException {
        SQLiteDatabase database =
                SQLiteDatabase.openDatabase(
                        dbPath,
                        null,
                        SQLiteDatabase.NO_LOCALIZED_COLLATORS
                                | SQLiteDatabase.CREATE_IF_NECESSARY
                                | SQLiteDatabase.OPEN_READWRITE);
        Log.trace(
                MediaInternalConstants.EXTENSION_LOG_TAG,
                LOG_TAG,
                "openDatabase - Successfully opened the database at path (%s)",
                dbPath);
        return database;
    }

    private void closeDatabase(final SQLiteDatabase database) {
        if (database == null) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "closeDatabase - Unable to close database, database passed is null.");
            return;
        }

        database.close();
        Log.trace(
                MediaInternalConstants.EXTENSION_LOG_TAG,
                LOG_TAG,
                "closeDatabase - Successfully closed the database.");
    }
}

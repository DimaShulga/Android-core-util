/*
 * Copyright 2014 Dima Shulga
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dalaran;


import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

public class CacheContentProvider extends ContentProvider {

    static final String DB_NAME = "cache";
    static final int DB_VERSION = 5;

    public static final String CACHE_TABLE = "cache";
    public static final String ID = "key";
    public static final String VALUE = "value";

    public static final String SCHEDULING_TABLE = "schedule";
    public static final String TASK_ID = "_id";
    public static final String CLASS_NAME = "class_name";
    public static final String UPDATE_INTERVAL = "update_interval";
    public static final String LAST_UPDATE_TIME = "last_update_time";
    public static final String PARENT_ID = "parent_id";
    public static final String CASCADE = "cascade";
    public static final String STATUS = "status";
    public static final String NEED_UPDATE = "need_update";


    static final String DB_CREATE = "CREATE TABLE `" + CACHE_TABLE + "` (`" + VALUE + "` BLOB , `" + ID + "` VARCHAR NOT NULL , `id` INTEGER PRIMARY KEY AUTOINCREMENT ,  UNIQUE (`" + ID + "`));";

    static final String CREATE_SCHEDULE_TABLE = "CREATE TABLE " + SCHEDULING_TABLE + "(" +
            TASK_ID + " integer primary key," +
            CLASS_NAME + " text unique," +
            UPDATE_INTERVAL + " bigint," +
            LAST_UPDATE_TIME + " bigint," +
            PARENT_ID + " integer," +
            CASCADE + " integer," +
            STATUS + " integer," +
            NEED_UPDATE + " integer" + ");";


    public final String AUTHORITY = this.getClass().getName();
    public static final String CACHE_PATH = "cache";
    public static final String SCHEDULE_PATH = "schedule";

    public  final Uri CACHE_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + CACHE_PATH);
    public static final Uri SCHEDULE_CONTENT_URI = Uri.parse("content://" + Cache.AUTHORITY + "/" + SCHEDULE_PATH);

     final String CACHE_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "" + CACHE_PATH;
     final String CACHE_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "" + CACHE_PATH;

     final String SCHEDULE_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "" + SCHEDULE_PATH;
     final String SCHEDULE_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "" + SCHEDULE_PATH;

    static final int URI_CACHE = 1;
    static final int URI_CACHE_ID = 2;

    static final int URI_SCHEDULE = 3;
    static final int URI_SCHEDULE_ID = 4;

    private  final UriMatcher uriMatcher;

    public CacheContentProvider() {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, CACHE_TABLE, URI_CACHE);
        uriMatcher.addURI(AUTHORITY, CACHE_TABLE + "/#", URI_CACHE_ID);
        uriMatcher.addURI(AUTHORITY, SCHEDULING_TABLE, URI_SCHEDULE);
        uriMatcher.addURI(AUTHORITY, SCHEDULING_TABLE + "/#", URI_SCHEDULE_ID);
    }

    DBHelper dbHelper;
    SQLiteDatabase db;

    @Override
    public boolean onCreate() {
        dbHelper = new DBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        String table;
        Uri _uri;
        String id;
        switch (uriMatcher.match(uri)) {
            case URI_CACHE:
                table = CACHE_TABLE;
                _uri = CACHE_CONTENT_URI;
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = ID + " ASC";
                }
                break;
            case URI_CACHE_ID:
                table = CACHE_TABLE;
                _uri = CACHE_CONTENT_URI;
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = ID + "=" + id;
                } else {
                    selection = selection + " AND " + ID + "=" + id;
                }
                break;
            case URI_SCHEDULE:
                table = SCHEDULING_TABLE;
                _uri = SCHEDULE_CONTENT_URI;
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = TASK_ID + " ASC";
                }
                break;
            case URI_SCHEDULE_ID:
                table = SCHEDULING_TABLE;
                _uri = SCHEDULE_CONTENT_URI;
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = TASK_ID + "=" + id;
                } else {
                    selection = selection + " AND " + TASK_ID + "=" + id;
                }
                break;
            default:
                id = uri.getLastPathSegment();
                table = CACHE_TABLE;
                _uri = CACHE_CONTENT_URI;
                if (uri.getPathSegments().size() != 1) {
                    selection = ID + " = ?";
                    selectionArgs = new String[]{id};
                }
        }

        db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(table, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), _uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case URI_CACHE:
                return CACHE_CONTENT_TYPE;
            case URI_CACHE_ID:
                return CACHE_CONTENT_ITEM_TYPE;
            case URI_SCHEDULE:
                return SCHEDULE_CONTENT_TYPE;
            case URI_SCHEDULE_ID:
                return SCHEDULE_CONTENT_ITEM_TYPE;
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        String idColumn;
        String table;
        Uri _uri;
        switch (uriMatcher.match(uri)) {
            case URI_CACHE:
                table = CACHE_TABLE;
                idColumn = ID;
                _uri = CACHE_CONTENT_URI;
                break;
            case URI_SCHEDULE:
                table = SCHEDULING_TABLE;
                idColumn = TASK_ID;
                _uri = SCHEDULE_CONTENT_URI;
                break;
            default:
                throw new IllegalArgumentException("Wrong URI!");
        }
        db = dbHelper.getWritableDatabase();
        long rowID;
        try {
            assert db != null;
            rowID = db.insert(table, idColumn, values);
        } catch (Throwable e) {
            String asString = values.getAsString(idColumn);
            values.remove(idColumn);
            rowID = update(uri, values, idColumn + " = ?", new String[]{asString});
        }
        if (rowID == -1) {
            String asString = values.getAsString(idColumn);
            values.remove(idColumn);
            rowID = update(uri, values, idColumn + " = ?", new String[]{asString});
        }
        Uri resultUri = ContentUris.withAppendedId(_uri, rowID);
        // уведомляем ContentResolver, что данные по адресу resultUri изменились
        getContext().getContentResolver().notifyChange(resultUri, null);
        return resultUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String table;
        Uri _uri;
        switch (uriMatcher.match(uri)) {
            case URI_CACHE:
                table = CACHE_TABLE;
                _uri = CACHE_CONTENT_URI;
                break;
            case URI_SCHEDULE:
                table = SCHEDULING_TABLE;
                _uri = SCHEDULE_CONTENT_URI;
                break;
            default:
                table = CACHE_TABLE;
                _uri = CACHE_CONTENT_URI;
                break;
        }
        db = dbHelper.getWritableDatabase();
        assert db != null;
        int cnt = db.delete(table, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(_uri, null);
        return cnt;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        String table;
        Uri _uri;
        String id;
        switch (uriMatcher.match(uri)) {
            case URI_CACHE:
                table = CACHE_TABLE;
                _uri = CACHE_CONTENT_URI;
                break;
            case URI_CACHE_ID:
                table = CACHE_TABLE;
                _uri = CACHE_CONTENT_URI;
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = ID + "=" + id;
                } else {
                    selection = selection + " AND " + ID + "=" + id;
                }
                break;
            case URI_SCHEDULE:
                table = SCHEDULING_TABLE;
                _uri = SCHEDULE_CONTENT_URI;
                break;
            case URI_SCHEDULE_ID:
                table = SCHEDULING_TABLE;
                _uri = SCHEDULE_CONTENT_URI;
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = TASK_ID + "=" + id;
                } else {
                    selection = selection + " AND " + TASK_ID + "=" + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI");
        }

        db = dbHelper.getWritableDatabase();
        assert db != null;
        int cnt = db.update(table, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(_uri, null);
        return cnt;
    }


    @TargetApi(Build.VERSION_CODES.BASE)
    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {


            db.execSQL(DB_CREATE);
            db.execSQL(CREATE_SCHEDULE_TABLE);


        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table " + CACHE_TABLE);
            db.execSQL("drop table " + SCHEDULING_TABLE);
            onCreate(db);
        }
    }
}

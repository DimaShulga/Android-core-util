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
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Build;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public class Cache {
    Context context;
    final String A = "content://" + AUTHORITY + "/" + CacheContentProvider.CACHE_PATH;
    final Uri CONTACT_URI = Uri.parse(A);

    public static String AUTHORITY = CacheContentProvider.class.getName();

    public static void setAUTHORITY(String authority) {
        AUTHORITY = authority;
    }

    public Cache(Context context) {
        this.context = context;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void save(String key, Object value) {
        Object t = value;
        if (value instanceof ContentValues) {
            HashMap<String, Object> temp = new HashMap<>();

            for (String tempKey : ((ContentValues) value).keySet()) {
                temp.put(tempKey, ((ContentValues) value).get(tempKey));
            }
            t = temp;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(t);
            byte[] bytes = bos.toByteArray();
            ContentValues cv = new ContentValues();
            cv.put("key", key);
            cv.put("value", bytes);
            Cursor query = context.getContentResolver().query(CONTACT_URI, null, "key = ?", new String[]{key}, null);
            assert query != null;
            int count = query.getCount();
            if (count == 0) {
                context.getContentResolver().insert(CONTACT_URI, cv);
            } else {
                context.getContentResolver().update(CONTACT_URI, cv, "key = ?", new String[]{key});
            }
            query.close();
        } catch (IOException ignored) {

        } finally {
            try {
                assert out != null;
                out.close();
                bos.close();
            } catch (IOException ignored) {

            }
        }
    }

    public int update(String key, Object value) {
        ContentValues cv = new ContentValues();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(value);
            byte[] bytes = bos.toByteArray();
            cv.put("value", bytes);
            Uri parse = (key != null) ? Uri.parse(A + "/" + key) : CONTACT_URI;
            return context.getContentResolver().update(parse, cv, CacheContentProvider.ID + " = ?", new String[]{key});
        } catch (IOException ignored) {

        } finally {
            try {
                assert out != null;
                out.close();
                bos.close();
            } catch (IOException ignored) {
            }
        }
        return -1;
    }

    public int delete(String key) {
        Uri parse = (key != null) ? Uri.parse(A + "/" + key) : CONTACT_URI;
        int result;
        if (key == null) {
            result = context.getContentResolver().delete(parse, null, null);
        } else {
            result = context.getContentResolver().delete(parse, CacheContentProvider.ID + " = ?", new String[]{key});
        }
        return result;
    }

    public Object get(String key) {
        Uri parse = (key != null) ? Uri.parse(A + "/" + key) : Uri.parse(A + "/");

        Cursor query = context.getContentResolver().query(parse, null, null, null, null);
        assert query != null;
        Object o = null;
        if (query.moveToFirst()) {
            byte[] blob = query.getBlob(0);
            ByteArrayInputStream bis = new ByteArrayInputStream(blob);
            ObjectInput in = null;
            try {

                in = new ObjectInputStream(bis);
                o = in.readObject();
            } catch (IOException ignored) {
            } catch (ClassNotFoundException ignored) {
            } finally {
                try {
                    bis.close();
                    assert in != null;
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                query.close();
            }
        }

        return o;
    }

    public Object get(String key, DataSetChange change) {
        Uri parse = (key != null) ? Uri.parse(A + "/" + key) : Uri.parse(A + "/");

        Cursor query = context.getContentResolver().query(parse, null, null, null, null);

        assert query != null;
        Object o = null;
        if (query.moveToFirst()) {
            byte[] blob = query.getBlob(0);
            ByteArrayInputStream bis = new ByteArrayInputStream(blob);
            ObjectInput in = null;
            try {

                in = new ObjectInputStream(bis);
                o = in.readObject();
            } catch (IOException ignored) {
            } catch (ClassNotFoundException ignored) {
            } finally {
                try {
                    bis.close();
                    assert in != null;
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        query.registerDataSetObserver(new Observer(change, query, o));
        return o;
    }


    class Observer extends DataSetObserver {
        DataSetChange change;
        Cursor query;
        Object o;

        public Observer(DataSetChange change, Cursor query, Object o) {
            this.change = change;
            this.query = query;
            this.o = o;
        }

        public void onChanged() {
            if (change != null) {
                change.onChanged(getObject());
            }
        }

        private Object getObject() {
            assert query != null;
            Object o = null;
            if (query.moveToFirst()) {
                byte[] blob = query.getBlob(0);
                ByteArrayInputStream bis = new ByteArrayInputStream(blob);
                ObjectInput in = null;
                try {

                    in = new ObjectInputStream(bis);
                    o = in.readObject();
                } catch (IOException ignored) {
                } catch (ClassNotFoundException ignored) {
                } finally {
                    try {
                        bis.close();
                        assert in != null;
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return o;
        }

        public void onInvalidated() {
            if (change != null)
                change.onInvalidated(getObject());
        }
    }
}

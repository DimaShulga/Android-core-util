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


import android.app.Application;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class Deamon extends Application {
    protected static String AUTHORITY;
    private static Deamon CONTEXT;
    protected static String CONSUMER_SECRET;
    protected static String CONSUMER_KEY;
    protected static String SHEME;
    protected static String HOST;
    protected static String LIFERAY_URL;
    protected static int LIFERAY_PORT;
    private static Set<String> dbUri = new HashSet<>();

    static {

    }

    public static String getAUTHORITY() {
        return AUTHORITY;
    }

    public static String getCONSUMER_SECRET() {
        return CONSUMER_SECRET;
    }

    public static String getCONSUMER_KEY() {
        return CONSUMER_KEY;
    }

    public static String getSHEME() {
        return SHEME;
    }

    public static String getHOST() {
        return HOST;
    }

    public static String getLIFERAY_URL() {
        return LIFERAY_URL;
    }

    public static int getLIFERAY_PORT() {
        return LIFERAY_PORT;
    }

    public static String getKEY() {
        String key;
        try {
            key = ((TelephonyManager)getCONTEXT().getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
            if(key == null) {
                key = getWifiMacAddress();
            }
        } catch (Exception e) {
            key = getWifiMacAddress();
        }
        return key;
    }

    public static String getWifiMacAddress() {
        WifiManager wfManager;
        WifiInfo wifiinfo;
        wfManager = (WifiManager) getCONTEXT().getSystemService(Context.WIFI_SERVICE);
        wifiinfo = wfManager.getConnectionInfo();
        String macAddress = wifiinfo.getMacAddress();
        return macAddress == null ? "null" : macAddress;
    }

    public static String getIpAddress() {
        WifiManager systemService = (WifiManager) getCONTEXT().getSystemService(Context.WIFI_SERVICE);
        int ipAddress = systemService.getConnectionInfo().getIpAddress();
        return String.format("%d.%d.%d.%d", (ipAddress & 0xff),(ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        CONTEXT = this;
//        startService(new Intent(this, SyncService.class));
    }

    public static <T> T get(String key, Class<T> t) {
        try {
            return t.cast(new Cache(CONTEXT).get(key));
        } catch (Throwable e) {
            Log.w("CacheManager", "cannot cast class, with key - " + key, e);
            return null;
        }
    }

    public static <T> T get(String key, Class<T> t, DataSetChange<T> change) {
        try {
            return t.cast(new Cache(CONTEXT).get(key, change));
        } catch (Throwable e) {
            Log.w("CacheManager", "cannot cast class, with key - " + key, e);
            return null;
        }
    }

    public static void set(String key, Object val) {
        new Cache(CONTEXT).save(key, val);
    }

    public static Map<String, Object> getAllCacheValue() {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = Map.class.cast(new Cache(CONTEXT).get(null));
        if (map == null) {
            map = new HashMap<String, Object>();
        }
        return map;
    }

    public static void remove(String key) {
        new Cache(CONTEXT).delete(key);
    }

    public static void removeAll() {
        new Cache(CONTEXT).delete(null);
    }

    public static <T> T getAndRemove(String key, Class<T> t) {
        try {
            Cache cache = new Cache(CONTEXT);
            T value = t.cast(cache.get(key));
            cache.delete(key);
            return value;
        } catch (Throwable e) {
            Log.w("CacheManager", "cannot cast class", e);
            return null;
        }
    }

    public static void addURI_DB(String uri) {
        dbUri.add(uri);
    }

    public static Set<String> getURI_DB() {
        return dbUri;
    }


    public static Deamon getCONTEXT() {

        if(CONTEXT == null) {

        }
        return CONTEXT;
    }

    public abstract void installCredentialsFromDeamon();
}

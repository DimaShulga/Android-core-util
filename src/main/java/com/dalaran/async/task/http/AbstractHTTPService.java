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

package com.dalaran.async.task.http;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentValues;
import android.os.Build;
import android.util.JsonReader;
import android.util.Log;
import android.widget.ProgressBar;

import com.dalaran.async.task.AbstractTask;
import com.dalaran.async.task.listener.OnTaskCancelledListener;
import com.dalaran.async.task.listener.OnTaskCompletedListener;
import com.dalaran.async.util.MySSLSocketFactory;

import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import static org.apache.http.conn.scheme.PlainSocketFactory.getSocketFactory;

public abstract class AbstractHTTPService<Params, Result> extends AbstractTask<Params, Result> {

    public enum REQUEST {
        GET,
        POST
    }

    protected AbstractHTTPService() {
        super();
    }

    protected AbstractHTTPService(OnTaskCancelledListener onTaskCancelledListener, OnTaskCompletedListener onTaskCompletedListener, ProgressBar progressBar) {
        super(onTaskCancelledListener, onTaskCompletedListener, progressBar);
    }

    protected AbstractHTTPService(OnTaskCompletedListener onTaskCompletedListener, ProgressBar progressBar) {
        super(onTaskCompletedListener, progressBar);
    }


    protected String callGetRemoteService(String url) throws IOException {
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet();
        httpGet.setURI(java.net.URI.create(url));
        HttpResponse response = client.execute(httpGet);
        HttpEntity entity = response.getEntity();

        return responseToBuffer(client, entity);
    }

    public static HttpClient getNewHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }

    protected String callPostRemoteService(String url, String json) throws IOException {
        SchemeRegistry schReg = new SchemeRegistry();
        schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 2008));

        HttpPost httpPost = new HttpPost(url);

        StringEntity se = new StringEntity(json);
        se.setContentEncoding(HTTP.UTF_8);
//        httpPost.setHeader(HTTP.USER_AGENT, URLs.USER_AGENT);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader(HTTP.CONTENT_TYPE, "application/json");
        SingleClientConnManager cm = new SingleClientConnManager(httpPost.getParams(), schReg);

        httpPost.setEntity(se);
        HttpClient client = new DefaultHttpClient();

        HttpResponse response = null;
        try {
            response = client.execute(httpPost);
            HttpEntity entity = response.getEntity();
            return responseToBuffer(client, entity);
        } catch (Exception e) {
            return null;
        }
    }

    protected String callPostRemoteService(String url) throws IOException {
        SchemeRegistry schReg = new SchemeRegistry();
        schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 2008));


        HttpPost httpPost = new HttpPost(url);

//        httpPost.setHeader(HTTP.USER_AGENT, URLs.USER_AGENT);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader(HTTP.CONTENT_TYPE, "application/json");
        SingleClientConnManager cm = new SingleClientConnManager(httpPost.getParams(), schReg);

        HttpClient client = new DefaultHttpClient();

        HttpResponse response = null;
        try {
            response = client.execute(httpPost);
            HttpEntity entity = response.getEntity();
            return responseToBuffer(client, entity);
        } catch (Exception e) {

            return null;
        }
    }

    private String responseToBuffer(HttpClient client, HttpEntity entity) throws IOException {
        StringBuilder response = new StringBuilder();
        if (entity != null) {
            InputStream inputStream = entity.getContent();
            try {
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(inputStream));
                String temp;
                while ((temp = bufferedReader.readLine()) != null) {
                    response.append(temp);
                }
            } finally {
                try {
                    inputStream.close();
                    client.getConnectionManager().shutdown();
                } catch (IOException e) {
                    Log.e(this.getClass().getSimpleName(), e.getMessage());
                    //do nothing
                }
            }
        } else {
            throw new IOException("Got empty response!");
        }
        return response.toString();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected List<ContentValues> parseJson(JsonReader reader) throws IOException {

        List<ContentValues> contentValueses = new ArrayList<ContentValues>();
        ContentValues values = new ContentValues();
        Long threadId = 0L;
        boolean notEnd = true;
        String name = "";
        if (reader.hasNext()) { //todo android.util.MalformedJsonException: Use JsonReader.setLenient(true)
            do {
                switch (reader.peek()) {
                    case BEGIN_OBJECT:
                        values = new ContentValues();
                        if(threadId != 0) {
                            values.put("threadId", threadId);
                        }
                        reader.beginObject();
                        break;
                    case BEGIN_ARRAY:
                        if(values != null && values.getAsLong("threadId") != null) {
                            threadId = values.getAsLong("threadId");
                        }
                        reader.beginArray();
                        break;
                    case BOOLEAN:
                        values.put(name, reader.nextBoolean());
                        break;
                    case END_ARRAY:
                        reader.endArray();
                        break;
                    case END_DOCUMENT:
                        notEnd = false;
                        break;
                    case END_OBJECT:
                        contentValueses.add(values);
                        reader.endObject();
                        break;
                    case NAME:
                        name = reader.nextName();
                        break;
                    case NULL:
                        reader.nextNull();
                        break;
                    case NUMBER:
                        values.put(name, reader.nextDouble());
                        break;
                    case STRING:
                        values.put(name, reader.nextString());
                        break;
                    default:
                        reader.skipValue();
                }
            } while (notEnd);
        }
        return contentValueses;
    }

    @SuppressLint("NewApi")
    public JsonReader callHttpToGetJsonReader(REQUEST request, String url, String host, int port) throws IOException {
     /*   HttpHost targetHost = new HttpHost(host, port);
        HttpClient httpClient = new DefaultHttpClient();
        HttpContext httpContext = new BasicHttpContext();
        HttpGet httpGet = new HttpGet(url);
        setAuthentificationHeader(httpGet);

        HttpResponse response = httpClient.execute(targetHost, httpGet, httpContext);
        HttpEntity entity = response.getEntity();*/
        HttpEntity entity = makeRequest(request, url, host, port);
        return new JsonReader(new InputStreamReader(entity.getContent()));
    }

    private HttpEntity makeRequest(REQUEST request, String url, String host, int port) throws IOException {
        HttpHost targetHost = new HttpHost(host, port);
        HttpClient httpClient = new DefaultHttpClient();
        HttpContext httpContext = new BasicHttpContext();
        HttpRequestBase base = getRequestByType(request);
        base.setURI(URI.create(url));
        setAuthentificationHeader(base);
        HttpResponse response = httpClient.execute(targetHost, base, httpContext); //todo  (Network is unreachable)
        return response.getEntity();
    }


    protected void setAuthentificationHeader(HttpRequestBase request) {
    }

    public String callHttpGetToGetString(String url, String host, int port) throws IOException {
        HttpHost targetHost = new HttpHost(host, port);
        HttpClient httpClient = new DefaultHttpClient();
        HttpContext httpContext = new BasicHttpContext();
        HttpGet httpGet = new HttpGet(url);
        setAuthentificationHeader(httpGet);

        HttpResponse response = httpClient.execute(targetHost, httpGet, httpContext);
        HttpEntity entity = response.getEntity();
        return responseToBuffer(httpClient, entity);
    }

    public String callHttpPostToPostValues(String url, String host, int port, List<NameValuePair> nameValuePairs) throws IOException {
        HttpHost targetHost = new HttpHost(host, port);
        HttpClient httpClient = new DefaultHttpClient();
        HttpContext httpContext = new BasicHttpContext();
        HttpPost httpPost = new HttpPost(url);
        if (nameValuePairs != null && nameValuePairs.size() != 0)
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        setAuthentificationHeader(httpPost);
        HttpResponse response = httpClient.execute(targetHost, httpPost, httpContext);
        HttpEntity entity = response.getEntity();
        return responseToBuffer(httpClient, entity);
    }

    public String callHttpPostToPostValues(String url, String host, int port) throws IOException {
        return callHttpPostToPostValues(url, host, port, null);
    }

    public InputStream callHttpToGetInputStream(REQUEST request,String url, String host, int port) throws IOException {

        HttpEntity entity = makeRequest(request, url, host, port);
        return entity.getContent();
    }

    private HttpRequestBase getRequestByType(REQUEST request){
        if(request==REQUEST.GET)
            return new HttpGet();
        else if(request==REQUEST.POST)
            return new HttpPost();
        return null;
    }
}

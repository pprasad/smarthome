package com.smart.csoft.services;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;

/**
 * Created by umprasad on 12/25/2017.
 */

public class RestClient {

    private final static String TAG = "RestClient";

    private static RestClient _INSTANCE;

    private final AsyncHttpClient httpClient;

    private final HttpClient http_Client;

    private String ipAddress;

    private final String BASE_URL = "http://";

    private final String CONTENT_TYPE = "application/json";

    private RestClient() {
        httpClient = new AsyncHttpClient();
        this.http_Client = new DefaultHttpClient();
    }

    public static RestClient getInstance() {
        if (_INSTANCE == null) {
            _INSTANCE = new RestClient();
        }
        return _INSTANCE;
    }

    public void getCall(String url, AsyncHttpResponseHandler handler) {
        Log.i("RestClient", "Request URL::" + getAbsoluteUrl(url));
        httpClient.get(getAbsoluteUrl(url), handler);
    }

    public void getCall(String url, ResponseHandlerInterface handler) {
        Log.i("RestClient", "Request URL::" + getAbsoluteUrl(url));
        httpClient.get(getAbsoluteUrl(url), handler);
    }

    public void getJsonCall(String url, JsonHttpResponseHandler handler) {
        httpClient.get(getAbsoluteUrl(url), handler);
    }

    public void getCall(String url, RequestParams params, AsyncHttpResponseHandler handler) {
        httpClient.get(getAbsoluteUrl(url), params, handler);
    }

    public void getJsonCall(String url, RequestParams params, JsonHttpResponseHandler handler) {
        httpClient.get(getAbsoluteUrl(url), params, handler);
    }

    public void postJsonCall(Context context, String url, String body, AsyncHttpResponseHandler handler) {
        try {
            //StringEntity request=new StringEntity(body);
            Log.i(TAG, "Request payload{}" + body);
            ByteArrayEntity request = new ByteArrayEntity(body.toString().getBytes());
            httpClient.post(context, getAbsoluteUrl(url), request, CONTENT_TYPE, handler);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage(), ex);
        }
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    private String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL.concat(ipAddress).concat(relativeUrl);
    }
}

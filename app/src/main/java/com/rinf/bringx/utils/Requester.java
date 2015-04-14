package com.rinf.bringx.utils;

import android.util.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class Requester {
    public enum RequestType {
        GET,
        POST
    }

    private AbstractHttpEntity getRequestContent(String params) {
        AbstractHttpEntity entity = null;

        try {
            entity = new ByteArrayEntity(params.getBytes("UTF8"));
            entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return entity;
    }

    public String POST(String url, JSONObject jsonParams) {
        Log.d("Making POST" + "request to " + url + " with " + jsonParams.toString());
        return "{}";
        /*
        String response = null;

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 30000);

        HttpEntity httpEntity = null;
        HttpResponse httpResponse = null;

        String requestParams = jsonParams != null ? jsonParams.toString() : "";
        AbstractHttpEntity entity = getRequestContent(requestParams);

        try {
            if (method == RequestType.POST) {
                HttpPost httpPost = new HttpPost(url);
                httpPost.setEntity(entity);

                httpResponse = httpClient.execute(httpPost);
            } else {
                HttpGet httpGet = new HttpGet(url);
                httpResponse = httpClient.execute(httpGet);
                httpEntity = httpResponse.getEntity();
                response = EntityUtils.toString(httpEntity);
            }

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            Log.e("Response code: " + statusCode);

            if (statusCode == 200) {
                httpEntity = httpResponse.getEntity();
                response = EntityUtils.toString(httpEntity);
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;*/
    }
}

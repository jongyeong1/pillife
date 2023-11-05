package com.crontiers.pillife.Http;

import android.util.Log;

import com.crontiers.pillife.Model.MvConfig;
import com.crontiers.pillife.Utils.Logging;

import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by Jaewoo on 2016-08-19.
 */
public class TransportConnector implements MvConfig {

    private OkHttpClient client;
    private Callback callBackMessage;

    private String stringUrl;
    private String callTag;
    private RequestBody requestBody;
    private Headers headers;

    private MediaType MEDIA_TYPE = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    private MvConfig.REQUEST_TYPE requestType;
    private CONTENT_TYPE contentType;


    public TransportConnector(String stringUrl, REQUEST_TYPE requestType, Callback callBackMessage, String callTag, CONTENT_TYPE contentType){
        this.stringUrl = stringUrl;
        this.requestType = requestType;
        this.callBackMessage = callBackMessage;
        this.callTag = callTag;
        this.contentType = contentType;
        setContentType(contentType);
    }

    public void setContentType(CONTENT_TYPE type){
        switch (type){
            case JSON:
                MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
                break;
            case URLENCODED:
                MEDIA_TYPE = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
                break;
        }
    }

    public void addParams(Map<String, Object> params){
        FormBody.Builder encoded = new FormBody.Builder();
        JSONObject json = new JSONObject();
        for(Map.Entry<String, Object> entry : params.entrySet()) {
            try {
                Logging.e("Length: " + ((String)entry.getValue()).length() + ", Key:"+entry.getKey()+", Value:"+entry.getValue());
                json.put(entry.getKey(), entry.getValue());
                encoded.add(entry.getKey(), entry.getValue().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Logging.d(encoded.toString());
        switch (contentType){
            case URLENCODED:
                this.requestBody = encoded.build();
                break;
            case JSON:
                this.requestBody = RequestBody.create(MEDIA_TYPE, json.toString());
                break;
        }

    }

    public void addQueryParam(Map<String, Object> params){
        StringBuffer sb = new StringBuffer(this.stringUrl).append("?");
        for(Map.Entry<String, Object> entry : params.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }

        this.stringUrl = sb.toString();
    }

    public void addParam(String json){
        this.requestBody = RequestBody.create(MEDIA_TYPE, json);
    }


    public RequestBody getParam(){
        return this.requestBody;
    }


    public void addHeaders(Map<String, String> headers){
        headers.put("Content-Type", MEDIA_TYPE.toString());
        this.headers = Headers.of(headers);
    }

    /**
     Request
     http request - GET, POST
     media type - JSON, TEXT, Form-data
     */
    public boolean Request(){
        client = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .build();

        Request request = null;
        try {
            switch(this.requestType){
                case GET:
                    request = new Request.Builder()
                            .headers(headers)
                            .url(this.stringUrl)
                            .tag(this.callTag)
                            .build();
                    break;
                case POST:
                    request = new Request.Builder()
                            .headers(headers)
                            .url(this.stringUrl)
                            .post(getParam())
                            .tag(this.callTag)
                            .build();
                    break;
                case PUT:
                    request = new Request.Builder()
                            .headers(headers)
                            .url(this.stringUrl)
                            .put(getParam())
                            .tag(this.callTag)
                            .build();
                    break;
                case DELETE:
                    request = new Request.Builder()
                            .headers(headers)
                            .url(this.stringUrl)
                            .delete(getParam())
                            .tag(this.callTag)
                            .build();
                    break;
                default:
            }

            client.newCall(request).enqueue(this.callBackMessage);

            return true;
            // Do something with the response.
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }

        return false;
    }


}

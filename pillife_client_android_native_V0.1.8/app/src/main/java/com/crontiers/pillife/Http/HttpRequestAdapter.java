package com.crontiers.pillife.Http;

import android.app.Activity;
import android.widget.Toast;

import com.crontiers.pillife.Listener.HttpRequestFireEventListener;
import com.crontiers.pillife.Model.Account;
import com.crontiers.pillife.Model.DataRank;
import com.crontiers.pillife.Model.HttpKeyValue;
import com.crontiers.pillife.Model.MvConfig;
import com.crontiers.pillife.Model.RequestData;
import com.crontiers.pillife.R;
import com.crontiers.pillife.Utils.JsonUtil;
import com.crontiers.pillife.Utils.Logging;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Jaewoo on 2018-11-23.
 */
public class HttpRequestAdapter implements MvConfig {
    private HttpRequestFireEventListener mListener;
    private RequestData requestData = new RequestData();
    private Activity activity;
    private Map<String, Object> reqData = new HashMap<>();
    private Map<String, String> reqHeader = new HashMap<>();
    protected int position = 0;
    private boolean running = false;

    public HttpRequestAdapter(Activity activity){
        this.activity = activity;
    }

    public void setOnCallBackRequest(HttpRequestFireEventListener listener){
        this.mListener = listener;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Request functions
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void requestLogin(REQUEST_CALLBACK request_callback){
        requestData.setReqCall(REQUEST_CALLBACK.valueOfStr(request_callback));
        requestData.setReqTag(request_callback.name());
        requestData.setReqType(REQUEST_TYPE.POST);
        requestData.setReqParam(reqData);
        start();
    }

    public void requestData(REQUEST_CALLBACK request_callback, REQUEST_TYPE request_type, String id){
        requestData.setReqCall(REQUEST_CALLBACK.valueOfStr(request_callback).replace("/##id", id));
        requestData.setReqTag(request_callback.name());
        requestData.setReqType(request_type);
        if(request_type != REQUEST_TYPE.DELETE) {
            requestData.setReqParam(reqData);
            requestData.setReqHeader(reqHeader);
        }
        start();
    }

    public void requestData(REQUEST_CALLBACK request_callback, REQUEST_TYPE request_type, CONTENT_TYPE content_type){
        requestData.setReqCall(REQUEST_CALLBACK.valueOfStr(request_callback));
        requestData.setReqTag(request_callback.name());
        requestData.setReqType(request_type);
        requestData.setContentType(content_type);
        if(request_type != REQUEST_TYPE.DELETE) {
            requestData.setReqParam(reqData);
            requestData.setReqHeader(reqHeader);
        }
        start();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Common data set
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void putParam(String key, Object value){
        reqData.put(key, value);
    }

    public void putHeader(String key, String value){
        reqHeader.put(key, value);
    }

    public boolean isRunning(){
        return running;
    }

    public void setRunning(boolean run){
        this.running = run;
    }

    public void init(){
        requestData = new RequestData();
        reqData.clear();
        reqHeader.clear();
        running = true;
    }

    public void start(){
        activity.runOnUiThread(() -> {
            running = true;
            HttpWebRequest http = new HttpWebRequest();
            requestData.setCbm(callBackMessage);
            http.httpRequest(requestData);
        });
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Callback response
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public Callback callBackMessage = new Callback() {

        public String msg;

        @Override
        public void onFailure(Call call, IOException e) {
            msg = e.getLocalizedMessage();
            Logging.e("########### onFailure:" + msg);
            activity.runOnUiThread(() -> {
                Toast.makeText(activity, activity.getString(R.string.string_common_error_handler_network).replace("##code", "APP01"), Toast.LENGTH_LONG).show();
                mListener.onCallBackRequest(activity.RESULT_CANCELED, null, null);
                //onTest();
            });
        }

        public void runFailure(final int code, final String msg){
            runFailure(code, new Exception(msg));
        }

        public void runFailure(final int code, final Exception e) {
            activity.runOnUiThread(() -> {
                e.printStackTrace();
                Logging.e(String.format("#runFailure [%d]-[%s]", code, e.getMessage()));
                Toast.makeText(activity, activity.getString(R.string.string_common_error_handler_network).replace("##code", String.valueOf(code)), Toast.LENGTH_LONG).show();
                mListener.onCallBackRequest(activity.RESULT_CANCELED, null, null);
            });
        }

        public void runCheckSum(final String method, final JSONObject jsonObject){
            activity.runOnUiThread(() -> {
                try{
                    if(jsonObject.getString(STATUS).equals(OK)) {
                        if(!jsonObject.isNull(ID)){
                            String[] str = new String[2];
                            str[0] = method;
                            str[1] = jsonObject.getString(ID);
                            mListener.onCallBackRequest(Activity.RESULT_OK, str, null);
                        }else{
                            mListener.onCallBackRequest(Activity.RESULT_OK, true, null);
                        }
                    }else{
                        runFailure(3001, "Fail");
                    }
                }catch(Exception e){
                    runFailure(3000, e);
                }
            });
        }

        public void runContents(final REQUEST_CALLBACK request_callback, final JSONObject jsonObject){
            activity.runOnUiThread(() -> {
                try{
                    JSONArray list = null;
                    JSONObject data = null;
                    Logging.d(jsonObject.toString());
                    switch(request_callback){
                        case RECOGNITION:
                            if(jsonObject.optBoolean(CHECK)) {
                                List<DataRank> dataRankArrayList = new ArrayList<>();
                                list = jsonObject.getJSONArray(RESPONSE);
                                int id = jsonObject.getInt(ID);
                                for(int i=0; i<list.length(); i++){
                                    data = list.getJSONObject(i);

                                    DataRank dr = new DataRank();
                                    dr.setRank(data.getInt(RANK));
                                    dr.setAccuracy(data.getDouble(ACCURACY));
                                    dr.setImage_link(data.isNull(IMAGE_LINK) ? "" : data.getString(IMAGE_LINK));
                                    dr.setPill_name(data.isNull(NAME) ? "" : data.getString(NAME));
                                    dr.setCls_name(data.isNull(CLS_NAME) ? "" : data.getString(CLS_NAME));
                                    dr.setHref(data.isNull(HREF) ? "" : data.getString(HREF));
                                    dr.setId(id);

                                    if(!data.isNull(HREF)) {
                                        String temp = data.getString(HREF);
                                        Pattern pattern = Pattern.compile("drug_code=\\w+");
                                        Matcher mat = pattern.matcher(temp);
                                        if(mat.find()) {
                                            dr.setCode(mat.group().split("=")[1]);
                                        }
                                    }

                                    dataRankArrayList.add(dr);
                                }

                                mListener.onCallBackRequest(Activity.RESULT_OK, dataRankArrayList, request_callback);
                            }else{
                                mListener.onCallBackRequest(Activity.RESULT_CANCELED, jsonObject.optInt(CODE), request_callback);
                            }

                            break;
                        case RECONNECT:
                            break;
                        case LOGIN:
                            if(jsonObject.getInt(CODE) != 0) {
                                mListener.onCallBackRequest(Activity.RESULT_CANCELED, null, null);
                                break;
                            }
                            JSONObject obj = jsonObject.getJSONObject(DATA);
                            Account account = new Account();
                            account.setUserId(BigInteger.valueOf(obj.getLong(ID)));
                            account.setUsername(obj.getString(USERNAME));
                            account.setName(obj.getString(NAME));
                            mListener.onCallBackRequest(Activity.RESULT_OK, account, request_callback);
                            break;
                        case ANSWER:
                            mListener.onCallBackRequest(Activity.RESULT_OK, null, request_callback);
                            break;
                    }

                }catch(Exception e){
                    runFailure(1000, e);
                }
            });
        }

        public void onResponse(Call call, Response response) {
            String tag = call.request().tag().toString();
            try {
                Logging.d(String.format("tag:%s, url:%s, code:%d, msg:%s", tag, response.request().url().toString(), response.code(), response.message()));
                if(response.code() == 200) {
                    String method = response.request().method();
                    String jsonStr = response.body().string();
                    JSONObject json = JsonUtil.getJSONObjectFrom(jsonStr);
                    runContents(REQUEST_CALLBACK.valueOf(tag), json);
                }else{
                    runFailure(response.code(), response.message());
                    if(response.code() == 401 || response.code() == 412){
                        //GlobalApplication.setAccess_token("");
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                runFailure(response.code(), response.message());
            }

        }

        public void onTest(){
            try {
                List<DataRank> dataRankArrayList = new ArrayList<>();
                DataRank dr = new DataRank();
                dr.setRank(1);
                dr.setAccuracy(98f);
                dr.setImage_link("https://www.pharm.or.kr:442/images/sb_photo/big3/201406130000501.jpg");
                dr.setPill_name("TEST1");
                dr.setCls_name("지혈제1");
                dataRankArrayList.add(dr);

                dr = new DataRank();
                dr.setRank(2);
                dr.setAccuracy(93f);
                dr.setImage_link("https://www.pharm.or.kr:442/images/sb_photo/big3/200907150515802.jpg");
                dr.setPill_name("TEST2");
                dr.setCls_name("지혈제2");
                dataRankArrayList.add(dr);

                dr = new DataRank();
                dr.setRank(3);
                dr.setAccuracy(90f);
                dr.setImage_link("https://www.pharm.or.kr:442/images/sb_photo/big3/201411240001602.jpg");
                dr.setPill_name("TEST3");
                dr.setCls_name("지혈제3");
                dataRankArrayList.add(dr);

                dr = new DataRank();
                dr.setRank(4);
                dr.setAccuracy(88f);
                dr.setImage_link("https://www.pharm.or.kr:442/images/sb_photo/big3/201406130000501.jpg");
                dr.setPill_name("TEST4");
                dr.setCls_name("지혈제4");
                dataRankArrayList.add(dr);

                dr = new DataRank();
                dr.setRank(5);
                dr.setAccuracy(82f);
                dr.setImage_link("https://www.pharm.or.kr:442/images/sb_photo/big3/201406130000501.jpg");
                dr.setPill_name("TEST5");
                dr.setCls_name("지혈제5");
                dataRankArrayList.add(dr);

                mListener.onCallBackRequest(Activity.RESULT_OK, dataRankArrayList, null);

            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    };

}

package com.crontiers.pillife.Http;

import com.crontiers.pillife.Model.MvConfig;
import com.crontiers.pillife.Model.RequestData;
import com.crontiers.pillife.Utils.Logging;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Jaewoo on 2016-09-02.
 */
public class HttpWebRequest implements MvConfig {

    public void httpRequest(RequestData item){
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        // 서버 접속
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        TransportConnector http = new TransportConnector(RELEASE_HOST + item.getReqCall(), item.getReqType(), item.getCbm(), item.getReqTag(), item.getContentType());
        try {
            if(item.isParam()) {
                if(item.getReqParam().size() > 0){
                    if(item.getReqType() == REQUEST_TYPE.GET)
                        http.addQueryParam(item.getReqParam());
                    else
                        http.addParams(item.getReqParam());
                }else {
                    JSONObject object = new JSONObject();
                    http.addParam(new JSONObject().put(item.getReqCall(), object).toString());
                }
            }
            http.addHeaders(item.getReqHeader());

            http.Request();
        } catch (JSONException e) {
            Logging.d("#2:" + e.getMessage());
            e.printStackTrace();
        }
    }

}

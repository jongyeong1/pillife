package com.crontiers.pillife.Listener;


import com.crontiers.pillife.Model.MvConfig;

/**
 * Created by Jaewoo on 2017-08-29.
 */
public interface HttpRequestFireEventListener {
    void onCallBackRequest(int result, Object item, MvConfig.REQUEST_CALLBACK type);

}

package com.crontiers.pillife.Utils;

import android.webkit.WebView;

/**
 * Created by Jaewoo on 2017-11-13.
 */
public class CustomWebViewClient extends android.webkit.WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }

}

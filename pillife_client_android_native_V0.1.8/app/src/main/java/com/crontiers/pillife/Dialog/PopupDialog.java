package com.crontiers.pillife.Dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.crontiers.pillife.GlobalApplication;
import com.crontiers.pillife.Listener.FinishClickEventListener;
import com.crontiers.pillife.Model.MvConfig;
import com.crontiers.pillife.R;
import com.crontiers.pillife.Utils.CustomWebViewClient;

/**
 * Created by Jaewoo on 2018-11-22.
 */
public class PopupDialog extends View implements MvConfig, View.OnClickListener {

    private Dialog dialog;
    private POPUP_TYPE popup_type;
    private FinishClickEventListener mListener;
    public String drug_url;

    public PopupDialog(Context context){
        super(context);
    }

    public void setOnFinishClickEvent(FinishClickEventListener listener){
        mListener = listener;
    }

    public void showDialog(POPUP_TYPE pt, boolean isCancelable) {
        popup_type = pt;

        if (dialog != null) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }

        dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(isCancelable);

        WindowManager.LayoutParams param = new WindowManager.LayoutParams();
        //팝업 외부 뿌연 효과
        param.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        //뿌연 효과 정도
        param.dimAmount = 0.8f;
        //background transparent
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        switch (pt){
            case PERMISSION:
                dialog.setContentView(R.layout.popup_permission_menu);
                param.width = (int) (GlobalApplication.getPoint().x * 0.93);
                param.height = (int) (GlobalApplication.getPoint().y * 0.6);
                permissionDialog();
                break;
            case NETWORK:
                dialog.setContentView(R.layout.popup_dlg_menu);
                param.width = (int) (GlobalApplication.getPoint().x * 0.75);
                param.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                networkCheckDialog();
                break;
            case DELETE:
                dialog.setContentView(R.layout.popup_dlg_menu);
                param.width = (int) (GlobalApplication.getPoint().x * 0.75);
                param.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                transparentDialog();
                break;
            case VIDEO:
                dialog.setContentView(R.layout.popup_dlg_menu);
                param.width = (int) (GlobalApplication.getPoint().x * 0.75);
                param.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                videoCheckDialog();
                break;
            case VERSION:
                dialog.setContentView(R.layout.popup_version_menu);
                param.width = (int) (GlobalApplication.getPoint().x * 0.93);
                param.height = (int) (GlobalApplication.getPoint().y * 0.6);
                dialog.setCancelable(false);
                dialog.findViewById(R.id.button1).setOnClickListener(this);
                break;
            case DRUG:
                dialog.setContentView(R.layout.popup_webview_menu);
                param.width = (int) (GlobalApplication.getPoint().x * 0.93);
                param.height = (int) (GlobalApplication.getPoint().y * 0.93);
                drugDetailsDialog();
                break;
        }

        // set LayoutParams
        dialog.getWindow().setAttributes(param);

        // it show the dialog box
        dialog.show();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void drugDetailsDialog(){
        WebView webView1 = dialog.findViewById(R.id.webView1);
        webView1.getSettings().setJavaScriptEnabled(true);
        webView1.loadUrl(drug_url);
        webView1.setWebViewClient(new CustomWebViewClient());

        TextView b1 = dialog.findViewById(R.id.button1);
        b1.setOnClickListener(this);
    }
    private void videoCheckDialog(){
        TextView tv = dialog.findViewById(R.id.textView1);
        tv.setText(R.string.string_common_video_check);

        TextView b1 = dialog.findViewById(R.id.button1);
        b1.setVisibility(GONE);

        TextView b2 = dialog.findViewById(R.id.button2);
        b2.setOnClickListener(this);
        b2.setText(R.string.string_common_ok);
    }

    private void networkCheckDialog(){
        TextView tv = dialog.findViewById(R.id.textView1);
        tv.setText(R.string.string_common_error_network);

        TextView b1 = dialog.findViewById(R.id.button1);
        b1.setOnClickListener(this);
        b1.setText(R.string.string_common_exit);

        TextView b2 = dialog.findViewById(R.id.button2);
        b2.setTextColor(ContextCompat.getColor(getContext(), R.color.vr1));
        b2.setOnClickListener(this);
        b2.setText(R.string.string_common_retry);
    }

    private void transparentDialog(){
        TextView tv = dialog.findViewById(R.id.textView1);
        tv.setText(R.string.string_common_content_remove);

        TextView b1 = dialog.findViewById(R.id.button1);
        b1.setOnClickListener(this);
        b1.setText(R.string.string_common_cancel);

        TextView b2 = dialog.findViewById(R.id.button2);
        b2.setTextColor(ContextCompat.getColor(getContext(), R.color.vr1));
        b2.setOnClickListener(this);
        b2.setText(R.string.string_common_remove);
    }

    private void permissionDialog(){
        dialog.findViewById(R.id.button1).setOnClickListener(this);
        dialog.findViewById(R.id.button2).setOnClickListener(this);
    }

    public void stopDialog(){
        if(dialog != null)
            dialog.dismiss();
    }

    public POPUP_TYPE getPopup_type() {
        return popup_type;
    }

    public void setPopup_type(POPUP_TYPE popup_type) {
        this.popup_type = popup_type;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button1:
                mListener.onFinishEvent(false);
                break;
            case R.id.button2:
                mListener.onFinishEvent(true);
                break;
        }

        stopDialog();
    }

}

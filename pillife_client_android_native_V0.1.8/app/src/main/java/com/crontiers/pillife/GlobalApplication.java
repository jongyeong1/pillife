package com.crontiers.pillife;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.crontiers.pillife.Model.Account;
import com.crontiers.pillife.Model.MvConfig;

/**
 * Created by Jaewoo on 2018-11-23.
 * Renew on 2019-09-07.
 */
public class GlobalApplication extends MultiDexApplication implements MvConfig {
    /// @ 주요 앱 저장 변수
    protected static SharedPreferences pref;
    protected static SharedPreferences.Editor editor;

    /// @ Global 변수
    public static volatile GlobalApplication instance = null;
    private static volatile InputMethodManager mImm;
    private static int default_Color = 0;
    public static Point point;
    private static boolean init = false;
    private static boolean refresh = false;

    private static String faqTag = "";
    private static String noticeTag = "";

    private static boolean guide = true;

    private static Toast toast;

    public static int width = 0;
    public static int height = 0;

    public static boolean img_process_finish = false;

    // ~krime
    public static Account account = null;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static synchronized GlobalApplication getInstance(){
        if(instance == null){
            instance = new GlobalApplication();
        }
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        mImm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref = getSharedPreferences("pref", MODE_PRIVATE);
        editor = pref.edit();

        point = new Point();
        point.x = pref.getInt("point.x", 0);
        point.y = pref.getInt("point.y", 0);

        refresh = pref.getBoolean("opt.refresh", false);

        faqTag = pref.getString("http.faq", "");
        noticeTag = pref.getString("http.notice", "");

        guide = pref.getBoolean("opt.guide", true);

    }

    /**
     * singleton 애플리케이션 객체를 얻는다.
     * @return singleton 애플리케이션 객체
     */
    public static GlobalApplication getGlobalApplicationContext() {
        if(instance == null)
            throw new IllegalStateException("this application does not inherit com.kakao.GlobalApplication");
        return instance;
    }

    /**
     * 애플리케이션 종료시 singleton 어플리케이션 객체 초기화한다.
     */
    @Override
    public void onTerminate() {
        super.onTerminate();
        instance = null;
        init = false;
    }

    /**
     * tab 할때마다 activity의 컬러를 저장.
     * @param window
     * @param color
     */
    public static void setStatusColor(Window window, int color){
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(color);
        }
        default_Color = color;
        if(color != Color.WHITE)
            window.getDecorView().setSystemUiVisibility(0);
    }

    public static void setNavigationColor(Window window, int color){
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setNavigationBarColor(color);
        }
        default_Color = color;
//        if(color != Color.WHITE)
//            window.getDecorView().setSystemUiVisibility(0);
    }


    /**
     * 저장된 current color를 반환
     * @return
     */
    public static int getStatusColor(){
        if(default_Color != 0){
            return default_Color;
        }else{
            return Color.WHITE;
        }
    }

    public static Point getPoint() {
        return point;
    }

    public static void setPoint(Point point) {
        GlobalApplication.point = point;
        editor.putInt("point.x", point.x);
        editor.putInt("point.y", point.y);
        editor.commit();
    }

    public static boolean isRefresh() {
        return refresh;
    }

    public static void setRefresh(boolean refresh) {
        GlobalApplication.refresh = refresh;
        editor.putBoolean("opt.refresh", refresh);
        editor.commit();

    }

    public static String getFaqTag() {
        return faqTag;
    }

    public static void setFaqTag(String faqTag) {
        GlobalApplication.faqTag = faqTag;
        editor.putString("http.faq", faqTag);
        editor.commit();
    }

    public static String getNoticeTag() {
        return noticeTag;
    }

    public static void setNoticeTag(String noticeTag) {
        GlobalApplication.noticeTag = noticeTag;
        editor.putString("http.notice", noticeTag);
        editor.commit();
    }

    public static boolean isGuide() {
        return guide;
    }

    public static void setGuide(boolean guide) {
        GlobalApplication.guide = guide;
        editor.putBoolean("opt.guide", guide);
        editor.commit();
    }

    public static boolean isInit() {
        return init;
    }

    public static void setInit(boolean init) {
        GlobalApplication.init = init;
    }

    public static Toast getToast() {
        return toast;
    }

    public static void setToast(Toast toast) {
        GlobalApplication.toast = toast;
    }

    public static void setAccount(Account account) { GlobalApplication.account = account; }

    public static Account getAccount() { return GlobalApplication.account; }
}

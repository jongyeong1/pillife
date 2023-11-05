package com.crontiers.pillife.Utils;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.os.storage.StorageManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import com.crontiers.pillife.GlobalApplication;
import com.crontiers.pillife.R;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Jaewoo on 2018-11-29.
 */
public final class Utils {
    private static final String[] units = {
            "bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"
    };

    private Utils() {
    }

    /**
     * change dp to px
     * @param context
     * @param dpValue
     * @return
     */
    public static int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * change sp to px
     * @param spValue
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
    
    public static float dp2px(Resources resources, float dp) {
        final float scale = resources.getDisplayMetrics().density;
        return  dp * scale + 0.5f;
    }

    public static float sp2px(Resources resources, float sp){
        final float scale = resources.getDisplayMetrics().scaledDensity;
        return sp * scale;
    }

    public static float px2dp(Resources resources, float px){
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(Context context, float dp){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(Context context, float px){
        return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    @SuppressLint("DefaultLocale")
    public static String switchSToM(Integer s){
        return String.format("%02d:%02d:%02d",(s/3600), ((s % 3600)/60), (s % 60));
    }

    @SuppressLint("DefaultLocale")
    public static String switchBToUnit(Long bytes){
        double unit = Math.floor(Math.log(bytes)/ Math.log(1024));
        double calcBytes = (bytes/ Math.pow(1024, unit));
        if(Double.isNaN(calcBytes)) {
            unit = 0;
            return String.format("0 %s", units[(int) unit]);
        }else {
            return String.format("%02d %s", (int) calcBytes, units[(int) unit]);
        }

    }

    public static String switchTypeFromString(String s){
        try {
            if(!s.equals("")) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.KOREA);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date newDate = sdf.parse(s);
                sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
                s = sdf.format(newDate);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return s;
    }

    public static void setAnimation(View view) {
        boolean scrollEvent = true;
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", scrollEvent == true ? 300 : -300, 0);
        animator.setDuration(500);
        animator.start();
    }

    public static String getCurrentDateString(){
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.KOREA);

        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

    private static PowerManager.WakeLock wakeLock;

    public static void startWakeLock(Context context){
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, context.getString(R.string.title_text));
        wakeLock.acquire(30*60*1000L /*30 minutes*/);

//        PowerManager pm = (PowerManager) GlobalApplication.getInstance().getSystemService(Context.POWER_SERVICE);
//        wakeLock = pm.newWakeLock(
//                PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
//                             PowerManager.ACQUIRE_CAUSES_WAKEUP |
//                             PowerManager.ON_AFTER_RELEASE,
//                GlobalApplication.getInstance().getString(R.string.title_text));
//
//        wakeLock.acquire(30*60*1000L /*30 minutes*/);
    }

    public static void releaseWakeLock(){
        if(wakeLock != null){
            if(wakeLock.isHeld()) {
                wakeLock.release();
                wakeLock = null;
            }
        }
    }

    /**
     * Get external sd card path using reflection
     * @param mContext
     * @param is_removable is external storage removable
     * @return
     */
    public static String getExternalStoragePath(Context mContext, boolean is_removable) {

        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);

                if (is_removable == removable) {
                    return path;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void getVersion(Context context, TextView v){
        PackageInfo pInfo;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

            v.setText(new StringBuffer("v ").append(pInfo.versionName).toString());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void deleteDir(int dirName) {
        File dir = new File(GlobalApplication.getInstance().getExternalFilesDir(null).getPath() + "/" +
                GlobalApplication.getInstance().getString(R.string.app_name) + "/" + "Contents_" +
                dirName);

        if (dir.exists()) {
            String deleteCmd = "rm -r " + dir;
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec(deleteCmd);
            } catch (Exception e) {
                Logging.e("Exception deleteDir");
            }
        }
    }

    public static long getFreeInternalBytes(){

//        long freeBytesInternal = new File(ctx.getFilesDir().getAbsoluteFile().toString()).getFreeSpace();
        return new File(GlobalApplication.getInstance().getExternalFilesDir(null).getPath()).getFreeSpace();
    }

    public static boolean isNetworkConnected() {
        ConnectivityManager cm =
                (ConnectivityManager)GlobalApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

}

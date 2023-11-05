package com.crontiers.pillife

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.core.content.ContextCompat
import com.crontiers.pillife.Dialog.PopupDialog
import com.crontiers.pillife.Listener.FinishClickEventListener
import com.crontiers.pillife.Model.MvConfig
import com.crontiers.pillife.Utils.CustomAppCompatActivity
import com.crontiers.pillife.Utils.JsonUtil
import com.crontiers.pillife.Utils.Logging
import com.google.firebase.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

import org.json.JSONException


class ActivityEmpty : CustomAppCompatActivity(),
    FinishClickEventListener {
    private var context: Context? = null
    private var mFirebaseRemoteConfig: FirebaseRemoteConfig? = null
    private var popupDialog: PopupDialog? = null
    private var appVersion: String? = null
    private val LOADING_VERSION_CONFIG_KEY = "pillife_app_version"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (GlobalApplication.isInit()) {
            finish()
        } else {
            setContentView(R.layout.activity_empty)
            context = this

            GlobalApplication.setNavigationColor(
                window,
                ContextCompat.getColor(applicationContext, R.color.black)
            )

            // 버전확인
            runFirebaseConfig()

            // 권한요청
            //checkMyPermission()

            val tv = TypedValue()
            if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                val metrics = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(metrics)
                GlobalApplication.setPoint(Point(metrics.widthPixels, metrics.heightPixels))
            }
        }
    }

    private fun checkMyPermission() {
        when (grantPermission(MvConfig.PERMISSION)) {
            true -> {
                goIntent(false)
            }
            false -> {
                var thread = Thread(Runnable {
                    runOnUiThread {
                        popupDialog = PopupDialog(context)
                        popupDialog?.setOnFinishClickEvent(this)
                        popupDialog?.showDialog(MvConfig.POPUP_TYPE.PERMISSION, false)
                    }
                })
                thread.start()
            }
        }
    }

    override fun onFinishEvent(b: Boolean) {
        when(popupDialog?.popup_type){
            MvConfig.POPUP_TYPE.PERMISSION -> {
                when (b) {
                    true -> {
                        goIntent(true)
                    }
                    false -> finish()
                }
            }
            MvConfig.POPUP_TYPE.VERSION -> {
                val i = Intent(Intent.ACTION_VIEW)
                val u = Uri.parse("https://play.google.com/store/apps/details?id=com.alphacircle.m3vr&hl=ko")
                i.data = u
                startActivity(i)
                finish()
            }
            else -> {}
        }
    }

    private fun goIntent(b: Boolean) {
//        var cls: Class<*>
//        if(GlobalApplication.main != null){
//            cls = ActivityMain::class.java
//        }else{
//            cls = ActivityIntro::class.java
//        }
        val intent = Intent(applicationContext, ActivityIntro::class.java)
        intent.putExtra(MvConfig.EXTRA_PERMISSION, b)
        startActivity(intent)
        finish()
    }

    @Suppress("DEPRECATION")
    private fun runFirebaseConfig() {
        FirebaseApp.initializeApp(this)

        // Get Remote Config instance.
        // [START get_remote_config_instance]
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        // [END get_remote_config_instance]

        // Create a Remote Config Setting to enable developer mode, which you can use to increase
        // the number of fetches available per hour during development. See Best Practices in the
        // README for more information.
        // [START enable_dev_mode]
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setDeveloperModeEnabled(BuildConfig.DEBUG)
            .build()
        mFirebaseRemoteConfig?.setConfigSettings(configSettings)
        // [END enable_dev_mode]

        // Set default Remote Config parameter values. An app uses the in-app default values, and
        // when you need to adjust those defaults, you set an updated value for only the values you
        // want to change in the Firebase console. See Best Practices in the README for more
        // information.
        // [START set_default_values]
        mFirebaseRemoteConfig?.setDefaults(R.xml.remote_config_defaults)
        // [END set_default_values]

        var cacheExpiration: Long = 3600 // 1 hour in seconds.
        // If your app is using developer mode, cacheExpiration is set to 0, so each fetch will
        // retrieve values from the service.
        if (mFirebaseRemoteConfig?.info?.configSettings?.isDeveloperModeEnabled!!) {
            cacheExpiration = 0
        }

        // ~krime 파이어베이스 버전 확인 코드 임시 주석 및 수정
        // 20.07.07
        checkMyPermission()
//        // [START fetch_config_with_callback]
//        // cacheExpirationSeconds is set to cacheExpiration here, indicating the next fetch request
//        // will use fetch data from the Remote Config service, rather than cached parameter values,
//        // if cached parameter values are more than cacheExpiration seconds old.
//        // See Best Practices in the README for more information.
//        mFirebaseRemoteConfig?.fetch(cacheExpiration)
//            ?.addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//                    //Toast.makeText(this, "Fetch Succeeded",Toast.LENGTH_SHORT).show()
//
//                    // After config data is successfully fetched, it must be activated before newly fetched
//                    // values are returned.
//                    mFirebaseRemoteConfig?.activateFetched()
//
//                    appVersion = mFirebaseRemoteConfig?.getString(LOADING_VERSION_CONFIG_KEY)
//                    val json = JsonUtil.getJSONObjectFrom(appVersion)
//                    var goMarket = false
//                    val appVer: String
//                    val updateVer: String
//                    try {
//                        val pInfo = packageManager.getPackageInfo(packageName, 0)
//                        appVer = pInfo.versionName
//                        updateVer = json.getString("latest_client")
//
//                        if (appVer == updateVer) {
//                            goMarket = false
//                        } else {
//                            val arrayAppVer =
//                                appVer.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
//                            val arrayUpdateVer =
//                                updateVer.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
//
//                            if (arrayAppVer.size == 3 && arrayUpdateVer.size == 3) {
//                                if (arrayAppVer[0] != arrayUpdateVer[0] || arrayAppVer[1] != arrayUpdateVer[1]) {
//                                    // If app, major version different must be go to google market.
//                                    // App, Major version
//                                    goMarket = true
//                                }
//                            }
//                        }
//                    } catch (e: PackageManager.NameNotFoundException) {
//                        e.printStackTrace()
//                        // if have exception play the application.
//                        goMarket = false
//                    } catch (e: JSONException) {
//                        e.printStackTrace()
//                        goMarket = false
//                    }
//
//                    if (goMarket) {
//                        popupDialog = PopupDialog(context)
//                        popupDialog?.setOnFinishClickEvent(this)
//                        popupDialog?.showDialog(MvConfig.POPUP_TYPE.VERSION, false)
//                    } else {
//                        // 권한요청
//                        checkMyPermission()
//                    }
//
//                } else {
//                    //Toast.makeText(this, "Fetch Failed",Toast.LENGTH_SHORT).show()
//                    Logging.e("Fetch failed.")
//
//                    // 권한요청
//                    checkMyPermission()
//                }
//            }
//        // [END fetch_config_with_callback]
    }

}

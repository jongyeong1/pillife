package com.crontiers.pillife

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.crontiers.pillife.Http.HttpRequestAdapter
import com.crontiers.pillife.Listener.HttpRequestFireEventListener
import com.crontiers.pillife.Model.MvConfig
import com.crontiers.pillife.Utils.CustomAppCompatActivity
import com.crontiers.pillife.Utils.Logging
import java.util.*
import kotlin.concurrent.schedule

class ActivityIntro : CustomAppCompatActivity(),
    HttpRequestFireEventListener {

    private var hra: HttpRequestAdapter? = null

    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        hra = HttpRequestAdapter(this)
        hra!!.setOnCallBackRequest(this)

        val intent = intent
        if (intent.getBooleanExtra(MvConfig.EXTRA_PERMISSION, false)) {
            requestPermissions()
        } else {
            checkPanelType()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MvConfig.MY_PERMISSION_REQUEST -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    Logging.d("permission was granted")
                    // create a daemon thread
                    checkPanelType()
                } else {
                    // permission denied
                    Logging.d("permission denied")
                    goIntent(ActivityEmpty::class.java)
                }
            }
        }
    }

    private fun getRequestData(rc: MvConfig.REQUEST_CALLBACK, id: String) {
        hra?.init()
        hra?.requestData(rc, MvConfig.REQUEST_TYPE.GET, id)
    }

    private fun checkPanelType(){
        GlobalApplication.setStatusColor(
            window,
            ContextCompat.getColor(applicationContext, R.color.vr5)
        )
        GlobalApplication.setNavigationColor(
            window,
            ContextCompat.getColor(applicationContext, R.color.black)
        )

        Timer().schedule(10) {
            // 2. go to main
            goIntent(ActivityMain::class.java)
        }
    }

    private fun goIntent(cls: Class<*>){
        val intent = Intent(applicationContext, cls)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private var itemCount: Int = 0

    @Suppress("UNCHECKED_CAST")
    override fun onCallBackRequest(result: Int, item: Any?, type: MvConfig.REQUEST_CALLBACK?) {
        if (result == Activity.RESULT_OK) {
            if (item != null) {

            }
        }
    }

}

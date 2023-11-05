package com.crontiers.pillife

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_guide.*
import androidx.core.content.ContextCompat
import com.crontiers.pillife.Model.MvConfig.EXTRA_INPUT_TYPE
import com.crontiers.pillife.Utils.Logging


class ActivityGuide : AppCompatActivity() {

    private var bInput:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_guide)

        bInput = intent.getBooleanExtra(EXTRA_INPUT_TYPE, false)

        imageView3.setOnClickListener {
//            goIntent(MainActivity::class.java, true)
            goIntent(ActivityDrugShape::class.java, true)
        }

        relativeLayout1.setOnClickListener {
            textView1.setTextColor(Color.WHITE)
            viewLine1.setBackgroundColor(Color.WHITE)
//            goIntent(MainActivity::class.java, false)
            goIntent(ActivityDrugShape::class.java, false)
        }
    }

    private fun goIntent(cls: Class<*>, guide: Boolean?){

        if(guide == false){
            GlobalApplication.setGuide(false)
        }

        if(bInput) {
            val intent = Intent(applicationContext, cls)
            intent.putExtra(EXTRA_INPUT_TYPE, bInput)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        finish()
    }

}

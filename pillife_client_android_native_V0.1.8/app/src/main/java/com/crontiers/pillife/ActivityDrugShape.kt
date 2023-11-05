package com.crontiers.pillife

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_guide.*
import androidx.core.content.ContextCompat
import com.crontiers.pillife.Model.MvConfig
import com.crontiers.pillife.Model.MvConfig.EXTRA_INPUT_TYPE
import com.crontiers.pillife.Model.MvConfig.EXTRA_SHAPE_TYPE
import com.crontiers.pillife.Utils.Logging
import kotlinx.android.synthetic.main.activity_drug_shape.*
import kotlinx.android.synthetic.main.activity_guide.imageView3


class ActivityDrugShape : AppCompatActivity() {

    private var bInput:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_drug_shape)

        bInput = intent.getBooleanExtra(EXTRA_INPUT_TYPE, false)

        imageView3.setOnClickListener {
            goIntent(MainActivity::class.java, MvConfig.SHAPE_TYPE.CIRCLE.rc)
        }

        val shapeArray = arrayOf(item1, item2, item3, item4, item5, item6, item7, item8, item9)
        shapeArray.forEach { shape ->
            shape.setOnClickListener {
                onSelectShape(it)
            }
        }
    }

    private fun goIntent(cls: Class<*>, selectedShape: String ){
        if(bInput) {
            val intent = Intent(applicationContext, cls)
            intent.putExtra(EXTRA_SHAPE_TYPE, selectedShape)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        finish()
    }

    private fun onSelectShape(v: View) {
        goIntent(MainActivity::class.java, v.tag.toString())
    }

}

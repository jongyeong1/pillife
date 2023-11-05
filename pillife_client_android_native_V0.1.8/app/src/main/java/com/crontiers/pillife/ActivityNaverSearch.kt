package com.crontiers.pillife

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_guide.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.crontiers.pillife.Adapter.MainRvAdapter
import com.crontiers.pillife.Dialog.Category
import com.crontiers.pillife.Model.MvConfig
import com.crontiers.pillife.Model.MvConfig.EXTRA_INPUT_TYPE
import com.crontiers.pillife.Model.MvConfig.EXTRA_SHAPE_TYPE
import com.crontiers.pillife.Utils.KeyboardVisibilityUtils
import com.crontiers.pillife.Utils.Logging
import kotlinx.android.synthetic.main.activity_drug_shape.*
import kotlinx.android.synthetic.main.activity_guide.imageView3
import kotlinx.android.synthetic.main.activity_input_drug_identity.btnFinish
import kotlinx.android.synthetic.main.activity_input_drug_identity.btnSkip
import kotlinx.android.synthetic.main.activity_input_drug_identity.identityB
import kotlinx.android.synthetic.main.activity_input_drug_identity.identityF
import kotlinx.android.synthetic.main.activity_input_drug_identity.imageView1
import kotlinx.android.synthetic.main.activity_input_drug_identity.radioButton1
import kotlinx.android.synthetic.main.activity_input_drug_identity.radioButton2
import kotlinx.android.synthetic.main.activity_input_drug_identity.textView1
import kotlinx.android.synthetic.main.activity_naver_detail.mRecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class ActivityNaverSearch : AppCompatActivity(){


    var bookJsonObject : JSONArray = JSONArray()

    var bookJsonObject3 : JSONObject = JSONObject()

    var bookJsonObjects : String = String()

    var Category2 = arrayListOf<Category>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_naver_detail)

        imageView1.setOnClickListener {
            finish() // close this activity as oppose to navigating up
        }

        bookJsonObjects = intent.getStringExtra("key1").toString()

        bookJsonObject3 = JSONObject(bookJsonObjects)
        bookJsonObject = bookJsonObject3.getJSONArray("items")

        for (i in 0 until bookJsonObject.length()) {
            val jsonObject = bookJsonObject.getJSONObject(i)

            val title1 = jsonObject.getString("title")
            val link1 = jsonObject.getString("link")
            val image1 = jsonObject.getString("image")
            val price1 = jsonObject.getString("lprice")

            var Catego = Category(title1,link1,image1,price1)

            Category2.add(Catego)



        }
        val mAdapter = MainRvAdapter(this, Category2)
        mRecyclerView.adapter = mAdapter

        val lm = LinearLayoutManager(this)
        mRecyclerView.layoutManager = lm
        mRecyclerView.setHasFixedSize(true)



    }

}
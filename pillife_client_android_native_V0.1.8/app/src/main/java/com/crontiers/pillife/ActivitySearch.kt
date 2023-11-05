package com.crontiers.pillife

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.PowerManager
import android.util.Base64
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.widget.Toast
import com.crontiers.pillife.Adapter.RecyclerAdapterDrug
import com.crontiers.pillife.GlobalApplication.img_process_finish
import com.crontiers.pillife.Http.HttpRequestAdapter
import com.crontiers.pillife.Listener.DataClickEventListener
import com.crontiers.pillife.Listener.HttpRequestFireEventListener
import com.crontiers.pillife.Model.Account
import com.crontiers.pillife.Model.DataRank
import com.crontiers.pillife.Model.HttpKeyValue
import com.crontiers.pillife.Model.MvConfig.*
import com.crontiers.pillife.Utils.CustomLinearLayoutManager
import com.crontiers.pillife.Utils.CustomWebViewClient
import com.crontiers.pillife.Utils.KeyboardVisibilityUtils
import com.crontiers.pillife.Utils.Logging
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.coroutines.*
import java.io.*
import java.math.BigInteger
import java.security.MessageDigest


class ActivitySearch : AppCompatActivity(),
    HttpRequestFireEventListener, DataClickEventListener {

    private var hra: HttpRequestAdapter? = null
    private var recyclerAdapterDrug: RecyclerAdapterDrug? = null
    private var webView1: WebView? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private var fileName:String = ""
    private var drugUrl:String = ""
    private lateinit var shapeType: SHAPE_TYPE
    private lateinit var searchType: SEARCH_TYPE
    private lateinit var outputDirectoryFront: File
    private lateinit var outputDirectoryBack: File
    private lateinit var identityF: String
    private lateinit var identityB: String
    private lateinit var identityType: String

    private lateinit var keyboardVisibilityUtils: KeyboardVisibilityUtils

    private var curSelectIdx: Int = -1

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_search)

        hra = HttpRequestAdapter(this)
        hra!!.setOnCallBackRequest(this)

        // Get intent file name
        if(intent != null) {
            searchType = intent.getSerializableExtra(EXTRA_SEARCH_TYPE) as SEARCH_TYPE
            fileName = intent.getStringExtra(EXTRA_FILE_NAME) as String
            shapeType = intent.getSerializableExtra(EXTRA_SHAPE_TYPE) as SHAPE_TYPE
            drugUrl = intent.getStringExtra(EXTRA_SEARCH_URL) as String
            if(!SEARCH_TYPE.WEBVIEW.equals(searchType)) {
                identityF = intent.getStringExtra(EXTRA_IDENTITY_F) as String
                identityB = intent.getStringExtra(EXTRA_IDENTITY_B) as String
                identityType = intent.getStringExtra(EXTRA_IDENTITY_TYPE) as String
            }
        }

        changeSearchType(searchType)
        initView()
        initToolbar()

        val msg = handler.obtainMessage()
        handler.sendMessage(msg)

        keyboardVisibilityUtils = KeyboardVisibilityUtils(window,
            onShowKeyboard = { keyboardHeight ->
                sv_root.run {
                    smoothScrollTo(scrollX, scrollY + keyboardHeight)
                }
            })
    }

    private fun initToolbar() {
        imageView1.setOnClickListener {
            finish() // close this activity as oppose to navigating up
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initView(){
        cardView1.setOnClickListener {
            // Open the camera search
            Toast.makeText(applicationContext, "촬영을 시작합니다.", Toast.LENGTH_SHORT).show()
            if(GlobalApplication.isGuide())
                goIntent(ActivityGuide::class.java)
            else
                goIntent(ActivityDrugShape::class.java)
            finish() // close this activity as oppose to navigating up
        }
        cardView2.setOnClickListener {
            changeSearchType(SEARCH_TYPE.WEBVIEW)
            webView1 = findViewById(R.id.webView1)
            webView1!!.settings.javaScriptEnabled = true
            webView1!!.loadUrl(drugUrl)
            webView1!!.webViewClient = CustomWebViewClient()
        }
    }

    private fun goIntent(cls: Class<*>){
        val intent = Intent(applicationContext, cls)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra(EXTRA_INPUT_TYPE, true)
        startActivity(intent)
    }

    private fun setupRecyclerView(){
        val linearLayoutManager = CustomLinearLayoutManager(
            this,
            CustomLinearLayoutManager.VERTICAL,
            false
        )
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)

        recyclerAdapterDrug = RecyclerAdapterDrug(this)
        recyclerView.adapter = recyclerAdapterDrug

        Picasso.get()
            .load("file://" + outputDirectoryFront.absolutePath)
            .centerCrop()
            .resize(BOARD_HEIGHT, BOARD_HEIGHT)
            .into(imageView2)

        Picasso.get()
            .load("file://" + outputDirectoryBack.absolutePath)
            .centerCrop()
            .resize(BOARD_HEIGHT, BOARD_HEIGHT)
            .into(imageView3)

        radioButton.setOnClickListener {
            recyclerAdapterDrug!!.clearSelectResult()
            radioButton.isChecked = true
            editDrugName.requestFocus()
            val imm = applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editDrugName, InputMethodManager.SHOW_IMPLICIT)
            curSelectIdx = CUSTOM_ANSWER_INPUT
        }
        cardViewCustom.setOnClickListener { v -> radioButton.callOnClick() }
        editDrugName.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus == true) {
                recyclerAdapterDrug!!.clearSelectResult()
                radioButton.isChecked = true
                curSelectIdx = CUSTOM_ANSWER_INPUT
            }
        }

        submitResult.setOnClickListener {
            val drugName = editDrugName.text
            if(curSelectIdx == -1 || (curSelectIdx == 999 && drugName.isEmpty())) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.string_search_title3)
                when(curSelectIdx) {
                    -1 -> {
                        builder.setMessage(R.string.string_search_description7)
                    }
                    999 -> {
                        builder.setMessage(R.string.string_search_description8)
                    }
                }
                builder.setPositiveButton(R.string.string_login_error_button) { dialog, which -> }
                val dialog = builder.show()
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
                return@setOnClickListener
            }
            val imm = applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(editDrugName.windowToken, 0)
            editDrugName.clearFocus()
            submitResult.isEnabled = false
            editDrugName.isEnabled = false
            radioButton.isEnabled = false
            cardViewCustom.isEnabled = false
            submitResult.setTextColor(Color.LTGRAY)
            editDrugName.setTextColor(Color.GRAY)
            recyclerAdapterDrug!!.disableSelect()
            recyclerAdapterDrug!!.setLoding(true)

            loadingLayout.visibility = View.VISIBLE

            var customAnswer: String = ""
            var name: String = ""
            var code: String = ""
            var imageLink: String = ""
            val id: Int = recyclerAdapterDrug!!.mItemList.get(0).id
            if(this.curSelectIdx == CUSTOM_ANSWER_INPUT) {
                Logging.d("Custom Answer input " + editDrugName.text)
                customAnswer = editDrugName.text.toString()
            } else {
                Logging.d("Answer Index => " + this.curSelectIdx)
                val obj = recyclerAdapterDrug!!.mItemList.get(this.curSelectIdx)
                name = obj.pill_name
                code = obj.code
                imageLink = obj.image_link
            }

            hra?.init()
            val account : Account = GlobalApplication.getAccount()
            hra?.putHeader("X-Auth", account.username)
            hra?.putHeader("X-Token", account.userId.toString())
            hra?.putParam(HttpKeyValue.ID, String.format("%d", id))
            hra?.putParam(HttpKeyValue.NAME, name)
            hra?.putParam(HttpKeyValue.CODE, code)
            hra?.putParam(HttpKeyValue.IMAGE_LINK, imageLink)
            hra?.putParam(HttpKeyValue.CUSTOM_ANSWER, customAnswer)
            hra?.requestData(REQUEST_CALLBACK.ANSWER, REQUEST_TYPE.POST, CONTENT_TYPE.URLENCODED)
        }
    }

    private fun getRequestData(rc: REQUEST_CALLBACK) {
        hra?.init()
        hra?.putParam(HttpKeyValue.SHAPE, shapeType.name.toLowerCase())
        val account : Account = GlobalApplication.getAccount()
        hra?.putHeader("X-Auth", account.username)
        hra?.putHeader("X-Token", account.userId.toString())
        hra?.putParam(HttpKeyValue.IMAGE, getBase64Encoder(outputDirectoryFront))
        hra?.putParam(HttpKeyValue.IMAGE_HASH, getMd5HashCode(outputDirectoryFront))
        hra?.putParam(HttpKeyValue.IMAGE_BACK, getBase64Encoder(outputDirectoryBack))
        hra?.putParam(HttpKeyValue.IMAGE_HASH_BACK, getMd5HashCode(outputDirectoryBack))
        hra?.putParam(HttpKeyValue.IDENTITY_F, identityF)
        hra?.putParam(HttpKeyValue.IDENTITY_B, identityB)
        hra?.putParam(HttpKeyValue.IDENTITY_TYPE, identityType)
        hra?.requestData(rc, REQUEST_TYPE.POST, CONTENT_TYPE.URLENCODED)
    }

    private fun getBase64Encoder(file: File): String{
        val bytes = file.readBytes()
        return Base64.encodeToString(bytes, 0)
    }

    private fun getMd5HashCode(file: File): String{
        return file.bufferedReader().readLines().toString().md5()
    }

    private fun String.md5(): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
    }

    private fun changeSearchType(type: SEARCH_TYPE){
        relativeLayout1.visibility = View.GONE
        relativeLayout2.visibility = View.GONE
        relativeLayout3.visibility = View.GONE
        relativeLayout4.visibility = View.GONE
        toolbarTitle.text = getString(R.string.string_search_title)
        when(type){
            SEARCH_TYPE.LOADING -> {
                relativeLayout3.visibility = View.VISIBLE
            }
            SEARCH_TYPE.SUCCESS -> {
                relativeLayout1.visibility = View.VISIBLE
            }
            SEARCH_TYPE.FAIL -> {
                //relativeLayout2.visibility = View.VISIBLE
                relativeLayout1.visibility = View.VISIBLE
            }
            SEARCH_TYPE.WEBVIEW -> {
                toolbarTitle.text = getString(R.string.string_search_title2)
                relativeLayout4.visibility = View.VISIBLE
                img_process_finish = true
            }
        }
    }

    @SuppressLint("HandlerLeak")
    val handler: Handler = object : Handler() {
        @SuppressLint("SetJavaScriptEnabled")
        override fun handleMessage(msg: Message) {
            wakeLock =
                (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                    newWakeLock(
                        PowerManager.PARTIAL_WAKE_LOCK,
                        "ActivitySearch::lock"
                    ).apply {
                        acquire()
                    }
                }

            CoroutineScope(Dispatchers.Main).launch {
                var timer = 0
                while (timer++ <= 600) {
                    if (img_process_finish) {
                        if (searchType == SEARCH_TYPE.WEBVIEW) {
                            webView1 = findViewById(R.id.webView1)
                            webView1!!.settings.javaScriptEnabled = true
                            webView1!!.loadUrl(drugUrl)
                            webView1!!.webViewClient = CustomWebViewClient()
                        } else {
                            // Determine the output directory
                            outputDirectoryFront = MainActivity.getOutputDirectory(applicationContext)
                            outputDirectoryFront = File(outputDirectoryFront.absolutePath, fileName + "_origin_crop.jpg")
//                            outputDirectoryFront = File(outputDirectoryFront.absolutePath, "20200306_130130.jpg")

                            outputDirectoryBack = MainActivity.getOutputDirectory(applicationContext)
                            outputDirectoryBack = File(outputDirectoryBack.absolutePath,fileName + "_origin_back_crop.jpg")

                            getRequestData(REQUEST_CALLBACK.RECOGNITION)
                            setupRecyclerView()
                        }
                        break
                    }

                    delay(100)
                }

                if(!img_process_finish){
                    changeSearchType(SEARCH_TYPE.FAIL)
                }

                img_process_finish = false
            }

            super.handleMessage(msg)
        }
    }

    override fun onClickEvent(position: Int, select: Boolean) {
        Logging.d("==> select position " + position)
        curSelectIdx = position
        radioButton.isChecked = false
        val imm = applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editDrugName.windowToken, 0)
        editDrugName.clearFocus()
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCallBackRequest(result: Int, item: Any?, type: REQUEST_CALLBACK?) {
        if (result == Activity.RESULT_OK) {
            when(type) {
                REQUEST_CALLBACK.RECOGNITION -> {
                    if (item != null) {
                        val listDr = item as List<DataRank>
                        recyclerAdapterDrug!!.mItemList.clear()
                        recyclerView.visibility = View.VISIBLE
                        for(dr in listDr){
                            recyclerAdapterDrug!!.mItemList.add(dr)
                        }

                        recyclerAdapterDrug!!.notifyDataSetChanged()

                        changeSearchType(SEARCH_TYPE.SUCCESS)
                    }
                }
                REQUEST_CALLBACK.ANSWER -> {
                    recyclerAdapterDrug!!.setLoding(false)
                    loadingLayout.visibility = View.INVISIBLE

                    val builder = AlertDialog.Builder(this)
                    builder.setTitle(R.string.string_search_title4)
                    builder.setMessage(R.string.string_search_description12)
                    builder.setPositiveButton(R.string.string_login_error_button) { dialog, which ->
                        finish()
                    }
                    val dialog = builder.show()
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
                }
            }
        }else{
            changeSearchType(SEARCH_TYPE.FAIL)
            if(item is Int) {
                when(item) {
                    -21 -> {
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle(R.string.string_search_title3)
                        builder.setMessage(R.string.string_search_description9)
                        builder.setPositiveButton(R.string.string_login_error_button) { dialog, which -> }
                        val dialog = builder.show()
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
                    }
                    -12 -> {
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle(R.string.string_search_title3)
                        builder.setMessage(R.string.string_search_description13)
                        builder.setPositiveButton(R.string.string_login_error_button) { dialog, which -> }
                        val dialog = builder.show()
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
                    }
                }
            }
        }

        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
    }
}

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


class ActivityNaver : AppCompatActivity() {

    val clientId = "I2MZuSFFkiNQzs_OewC1"
    val clientSecret = "7yDqQjLZNT"

    var bookJsonObject : JSONObject = JSONObject()

    var Category2 = arrayListOf<Category>()


    private var fileName:String = ""
    private var drugUrl:String = ""
    private lateinit var shapeType: MvConfig.SHAPE_TYPE
    private lateinit var searchType: MvConfig.SEARCH_TYPE

    private lateinit var keyboardVisibilityUtils: KeyboardVisibilityUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_naver_search)

//        keyboardVisibilityUtils = KeyboardVisibilityUtils(window,
//            onShowKeyboard = { keyboardHeight ->
//                sv_root.run {
//                    smoothScrollTo(scrollX, scrollY + keyboardHeight)
//                }
//            }
//        )

        imageView1.setOnClickListener {
            finish() // close this activity as oppose to navigating up
        }

        val content = SpannableString(resources.getString(R.string.string_main_description10))
        content.setSpan(UnderlineSpan(), 0, content.length,0);
        textView1.text = content



        btnSkip.setOnClickListener {
            if(identityF.text.isEmpty()) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.string_identity_title3)
                builder.setMessage(R.string.string_identity_error_description2)
                builder.setPositiveButton(R.string.string_identity_error_button) { dialog, which -> }
                val dialog = builder.show()
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
                return@setOnClickListener
            }
            bookSearch(identityF.text.toString())
        }

    }

    private fun goIntent(cls: Class<*>){
        val intent = Intent(applicationContext, cls)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra(EXTRA_INPUT_TYPE, true)
        intent.putExtra("key1",bookJsonObject.toString())
        startActivity(intent)
    }
    fun bookSearch(searchWord : String) : JSONObject{
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val text: String = URLEncoder.encode(searchWord, "UTF-8")
                val apiURL =
                    "https://openapi.naver.com/v1/search/shop.json" + "?query=" + text + "&display=20" // json 결과
                //String apiURL = "https://openapi.naver.com/v1/search/blog.xml?query="+ text; // xml 결과
                val url = URL(apiURL)
                val con: HttpURLConnection = url.openConnection() as HttpURLConnection
                con.setRequestMethod("GET")
                con.setRequestProperty("X-Naver-Client-Id", clientId)
                con.setRequestProperty("X-Naver-Client-Secret", clientSecret)
                Log.d("네이버 url", apiURL)
                Log.d("con.responseCode", con.responseCode.toString())
                val responseCode: Int = con.getResponseCode()
                val br: BufferedReader
                if (responseCode == 200) { // 정상 호출
                    br = BufferedReader(InputStreamReader(con.getInputStream(), "UTF-8"))
                } else {  // 에러 발생
                    br = BufferedReader(InputStreamReader(con.getErrorStream()))
                }
                var inputLine: String?
                val response = StringBuffer()
                while (br.readLine().also { inputLine = it } != null) {
                    response.append(inputLine)
                    response.append("\n")
                }
                br.close()
                val naverHtml: String = response.toString()
                val bun = Bundle()
                bun.putString("NAVER_HTML", naverHtml)


                bookJsonObject = JSONObject(response.toString())

                Log.d("응답", bookJsonObject.toString())

                goIntent(ActivityNaverSearch::class.java)



            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bookJsonObject
    }






}
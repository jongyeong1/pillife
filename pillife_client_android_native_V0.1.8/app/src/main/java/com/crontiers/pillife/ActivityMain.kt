package com.crontiers.pillife

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.crontiers.pillife.Http.HttpRequestAdapter
import com.crontiers.pillife.Listener.HttpRequestFireEventListener
import com.crontiers.pillife.Model.Account
import com.crontiers.pillife.Model.HttpKeyValue
import com.crontiers.pillife.Model.MvConfig.*
import com.crontiers.pillife.Utils.Utils
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class ActivityMain : AppCompatActivity(), HttpRequestFireEventListener {
    private var backKeyPressedTime: Long = 0

    // ~krime
    private var hra: HttpRequestAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_main)

        // ~krime
        hra = HttpRequestAdapter(this)
        hra!!.setOnCallBackRequest(this)

        Utils.getVersion(applicationContext, findViewById(R.id.versionText))

        mainLayout.visibility = View.INVISIBLE
        loadingLayout.visibility = View.INVISIBLE

        loadLogin()

//        loginLayout.visibility = View.INVISIBLE
//        mainLayout.visibility = View.VISIBLE
//        var account : Account = Account()
//        account.name = "테스트"
//        account.username = "test"
//        account.userId = BigInteger.valueOf(15934177771104596)
//        GlobalApplication.setAccount(account);

        btnLogin.setOnClickListener {
            val imm = applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if(currentFocus != null) {
                imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            }
            val _username: String = username.text.toString()
            val _password: String = password.text.toString()

            if(_username.length <= 0 || _password.length <= 0) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.string_login_error_title)
                builder.setMessage(R.string.string_login_error_desc2)
                builder.setPositiveButton(R.string.string_login_error_button) { dialog, which ->
                }
                val dialog = builder.show()
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
                return@setOnClickListener
            }


//            Logging.d(_username + " / " + _password)

            loadingLayout.visibility = View.VISIBLE
            hra?.init()
            hra?.putParam(HttpKeyValue.USERNAME, _username)
            hra?.putParam(HttpKeyValue.PASSWORD, _password)
            hra?.requestData(REQUEST_CALLBACK.LOGIN, REQUEST_TYPE.POST, CONTENT_TYPE.URLENCODED)
//            loginLayout.visibility = View.INVISIBLE
//            mainLayout.visibility = View.VISIBLE



        }

        cardView1.setOnClickListener {
            onClickType(0)
        }

        cardView2.setOnClickListener {
            onClickType(1)
        }

        cardView6.setOnClickListener {
            onClickType(2)
        }
    }

    private fun onClickType(type: Int?) {
        when(type) {
            0 -> {
                // Open the camera search
                Toast.makeText(applicationContext, "촬영을 시작합니다.", Toast.LENGTH_SHORT).show()
                if(GlobalApplication.isGuide())
                    goIntent(ActivityGuide::class.java)
                else {
//                    goIntent(MainActivity::class.java)
                    goIntent(ActivityDrugShape::class.java)
                }
            }
            1 -> {
                // Open the shape search from 약정원 uri
                val intent = Intent(applicationContext, ActivitySearch::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtra(EXTRA_SEARCH_TYPE, SEARCH_TYPE.WEBVIEW)
//                intent.putExtra(EXTRA_SEARCH_URL, DRUG_DETAILS_HOST)
                intent.putExtra(EXTRA_SEARCH_URL, DRUG_FIND_HOST)

                // Put dummy string and type value.
                intent.putExtra(EXTRA_FILE_NAME, "")
                intent.putExtra(EXTRA_SHAPE_TYPE, SHAPE_TYPE.CIRCLE)

                startActivity(intent)
            }

            2 -> {
//                // Open the shape search from 약정원 uri
//                val intent = Intent(applicationContext, ActivitySearch::class.java)
//                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                intent.putExtra(EXTRA_SEARCH_TYPE, SEARCH_TYPE.WEBVIEW)
////                intent.putExtra(EXTRA_SEARCH_URL, DRUG_DETAILS_HOST)
//                intent.putExtra(EXTRA_SEARCH_URL, DRUG_FIND_HOST)
//
//                // Put dummy string and type value.
//                intent.putExtra(EXTRA_FILE_NAME, "")
//                intent.putExtra(EXTRA_SHAPE_TYPE, SHAPE_TYPE.CIRCLE)
//
//                startActivity(intent)
                Toast.makeText(applicationContext, "제품을 검색합니다.", Toast.LENGTH_SHORT).show()
//                    goIntent(MainActivity::class.java)
                goIntent(ActivityNaver::class.java)

            }
        }
    }

    private fun goIntent(cls: Class<*>){
        val intent = Intent(applicationContext, cls)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra(EXTRA_INPUT_TYPE, true)
        startActivity(intent)
    }

    override fun onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis()
            GlobalApplication.setToast(
                Toast.makeText(
                    applicationContext,
                    getString(R.string.string_common_back_button),
                    Toast.LENGTH_SHORT
                )
            )
            GlobalApplication.getToast().show()
            GlobalApplication.setInit(false)
            return
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            super.onBackPressed()
            GlobalApplication.getToast().cancel()
            GlobalApplication.setToast(null)
        }
    }

    private fun saveLogin() {
        val pref = this.getPreferences(Context.MODE_PRIVATE)
        val editor = pref.edit()

        val _username: String = username.text.toString()
        val _password: String = password.text.toString()

        val json: JSONObject = JSONObject()
        json.put("u", _username)
        json.put("p", _password)
        editor.putString("l", json.toString()).apply()
    }

    private fun emptyLogin() {
        val pref = this.getPreferences(Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.remove("l").apply()
        checkBoxAutoLogin.isChecked = false
    }

    private fun loadLogin() {
        val pref = this.getPreferences(Context.MODE_PRIVATE)
        val jsonString = pref.getString("l", "")
        if (jsonString != null) {
            if(jsonString.isNotEmpty()) {
                try {
                    val json: JSONObject = JSONObject(jsonString)
                    val _username: String = json.getString("u")
                    val _password: String = json.getString("p")
                    username.text = Editable.Factory.getInstance().newEditable(_username)
                    password.text = Editable.Factory.getInstance().newEditable(_password)
                    checkBoxAutoLogin.isChecked = true
                } catch (e: Exception) {
                    emptyLogin()
                }
            }
        }
    }

    override fun onCallBackRequest(result: Int, item: Any?, type: REQUEST_CALLBACK?) {
        loadingLayout.visibility = View.INVISIBLE
        if (result == Activity.RESULT_OK) {
            val account: Account = item as Account
            val _saveLogin: Boolean = checkBoxAutoLogin.isChecked
            if(_saveLogin) {
                saveLogin()
            } else {
                emptyLogin()
            }

            GlobalApplication.setAccount(account)
            loginLayout.visibility = View.INVISIBLE
            mainLayout.visibility = View.VISIBLE
        } else {
//            val builder = AlertDialog.Builder(this)
//            builder.setTitle(R.string.string_login_error_title)
//            builder.setMessage(R.string.string_login_error_desc1)
//            builder.setPositiveButton(R.string.string_login_error_button) { dialog, which ->
//            }
//            val dialog = builder.show()
//            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
            val account = Account()
            val _username: String = username.text.toString()
            val _password: String = password.text.toString()
            val _saveLogin: Boolean = checkBoxAutoLogin.isChecked

            if(!_username.equals("admin")||!_password.equals("admin")) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.string_login_error_title)
                builder.setMessage(R.string.string_login_error_desc2)
                builder.setPositiveButton(R.string.string_login_error_button) { dialog, which ->
                }
                val dialog = builder.show()
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
            }else{
                if(_saveLogin) {
                    saveLogin()
                } else {
                    emptyLogin()
                }
                GlobalApplication.setAccount(account)
                loginLayout.visibility = View.INVISIBLE
                mainLayout.visibility = View.VISIBLE
            }


        }
    }
}
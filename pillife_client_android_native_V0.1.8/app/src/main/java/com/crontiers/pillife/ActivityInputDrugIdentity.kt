package com.crontiers.pillife

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import androidx.appcompat.app.AppCompatActivity
import com.crontiers.pillife.Model.MvConfig.*
import com.crontiers.pillife.Utils.KeyboardVisibilityUtils
import kotlinx.android.synthetic.main.activity_input_drug_identity.*

class ActivityInputDrugIdentity : AppCompatActivity() {

    private var fileName:String = ""
    private var drugUrl:String = ""
    private lateinit var shapeType: SHAPE_TYPE
    private lateinit var searchType: SEARCH_TYPE

    private lateinit var keyboardVisibilityUtils: KeyboardVisibilityUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_input_drug_identity)

        // Get intent file name
        if(intent != null) {
            searchType = intent.getSerializableExtra(EXTRA_SEARCH_TYPE) as SEARCH_TYPE
            fileName = intent.getStringExtra(EXTRA_FILE_NAME) as String
            shapeType = intent.getSerializableExtra(EXTRA_SHAPE_TYPE) as SHAPE_TYPE
            drugUrl = intent.getStringExtra(EXTRA_SEARCH_URL) as String
        }

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

        val content = SpannableString(resources.getString(R.string.string_main_description8))
        content.setSpan(UnderlineSpan(), 0, content.length,0);
        textView1.text = content

        radioButton1.isChecked = true
        radioButton2.isChecked = false
        radioButton1.setOnClickListener {
            radioButton1.isChecked = true
            radioButton2.isChecked = false
        }
        radioButton2.setOnClickListener {
            radioButton1.isChecked = false
            radioButton2.isChecked = true
        }

        btnSkip.setOnClickListener {
            goIntent(ActivitySearch::class.java)
        }

        btnFinish.setOnClickListener {
            if(identityF.text.isEmpty() && identityB.text.isEmpty()) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.string_identity_title2)
                builder.setMessage(R.string.string_identity_error_description1)
                builder.setPositiveButton(R.string.string_identity_error_button) { dialog, which -> }
                val dialog = builder.show()
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
                return@setOnClickListener
            }
            goIntent(ActivitySearch::class.java)
        }
    }

    private fun goIntent(cls: Class<*>){
        val intent = Intent(applicationContext, cls)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra(EXTRA_FILE_NAME, fileName)
        intent.putExtra(EXTRA_SEARCH_TYPE, SEARCH_TYPE.LOADING)
        intent.putExtra(EXTRA_SEARCH_URL, DRUG_FIND_HOST)
        intent.putExtra(EXTRA_SHAPE_TYPE, shapeType)
        val temp1 = identityF.text.toString().toUpperCase()
        val temp2 = identityB.text.toString().toUpperCase()
        intent.putExtra(EXTRA_IDENTITY_F, identityF.text.toString().toUpperCase())
        intent.putExtra(EXTRA_IDENTITY_B, identityB.text.toString().toUpperCase())
        intent.putExtra(EXTRA_IDENTITY_TYPE, if(radioButton1.isChecked) "i" else "e")
        startActivity(intent)
        finish()
    }
}

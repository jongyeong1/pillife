package com.crontiers.pillife

import android.os.Bundle
import com.crontiers.pillife.Camera.Camera2BasicFragment
import com.crontiers.pillife.Utils.CustomAppCompatActivity

class ActivityCamera : CustomAppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        if (null == savedInstanceState) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, Camera2BasicFragment.newInstance())
                .commit()
        }
    }
}
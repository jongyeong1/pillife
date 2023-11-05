package com.crontiers.pillife.Utils;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.crontiers.pillife.Model.MvConfig;

/**
 * Created by Jaewoo on 2017-11-09.
 */
public class CustomAppCompatActivity extends AppCompatActivity implements MvConfig {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public boolean grantPermission(String[] permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int check = checkSelfPermission(permission[0]);
            if (check == PackageManager.PERMISSION_GRANTED) {
                Logging.d("Permission is granted");
                return true;
            } else {
                Logging.d("Permission is revoked");
                //ActivityCompat.requestPermissions(this, permission, 1);
                return false;
            }
        }else{
            Logging.d("Permission is Grant ");
            return true;
        }
    }

    public void requestPermissions(){
        ActivityCompat.requestPermissions(this, PERMISSION, MY_PERMISSION_REQUEST);
    }

}

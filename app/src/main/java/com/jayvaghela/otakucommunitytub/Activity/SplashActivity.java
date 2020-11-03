package com.jayvaghela.otakucommunitytub.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.ImageView;

import com.jayvaghela.otakucommunitytub.R;

public class SplashActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private static final String shP1 = "UserLog";
    private boolean log = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getSupportActionBar().hide();
        checkForLogin();
        gotoHome();
    }

    private void checkForLogin() {
        sharedPreferences = getSharedPreferences(shP1, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.apply();
        sharedPreferences.getBoolean(shP1, false);
        if (sharedPreferences.contains(shP1)) log = true;
    }

    private void gotoHome() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (log) startActivity(new Intent(getApplicationContext(),HomeActivity.class));
                else splash();
            }
            private void splash() {
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
            }
        },1800);
    }
}
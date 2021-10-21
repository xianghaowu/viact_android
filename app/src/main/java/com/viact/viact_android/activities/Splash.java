package com.viact.viact_android.activities;

import static com.viact.viact_android.utils.Const.EXT_STORAGE_VIDEO_PATH;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.viact.viact_android.R;

import java.io.File;

public class Splash extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        nextStep();
    }

    private void nextStep(){

        Thread timerThread = new Thread() {
            public void run() {
                try {
                    sleep(1500);
                    moveMainScreen();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {

                }
            }
        };
        timerThread.start();
    }

    void moveMainScreen(){
        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(i);
        finish();
    }

}
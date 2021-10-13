package com.arashivision.sdk.demo.myactivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.arashivision.sdk.demo.R;

public class Splash extends AppCompatActivity {

    public final static int REQUEST_CUSTOM_PERMISSION = 6789;

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
                    sleep(1000);
                    checkPermission();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {

                }
            }
        };
        timerThread.start();
    }

    void checkPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestPermission();
        } else{
            moveMainScreen();
        }
    }

    void moveMainScreen(){
        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(i);
        finish();
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, REQUEST_CUSTOM_PERMISSION);
        } else {
            moveMainScreen();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CUSTOM_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            moveMainScreen();
        } else {
            Toast.makeText(this, "Please set the Permissions on setting", Toast.LENGTH_SHORT).show();
        }
    }
}
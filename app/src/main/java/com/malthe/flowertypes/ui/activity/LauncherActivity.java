package com.malthe.flowertypes.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import com.malthe.flowertypes.R;

public class LauncherActivity extends AppCompatActivity {
    CircularProgressIndicator progressIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        progressIndicator = findViewById(R.id.progress_circular);
        progressIndicator.setVisibility(View.VISIBLE);

        navigateToSnapPlantsActivity();
    }

    private void navigateToSnapPlantsActivity() {
        Thread timerThread = new Thread() {
            public void run() {
                try {
                    sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {

                    Intent intent = new Intent(LauncherActivity.this, AllFlowersActivity.class);
                    startActivity(intent);

                    finish();
                }
            }
        };
        timerThread.start();
    }
}
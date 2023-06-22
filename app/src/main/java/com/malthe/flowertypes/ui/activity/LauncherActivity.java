package com.malthe.flowertypes.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.malthe.flowertypes.R;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        // Add any additional setup or logic for your launcher start screen

        // Start the main activity after a delay or completion of necessary tasks
        navigateToSnapPlantsActivity();
    }

    private void navigateToSnapPlantsActivity() {
        // Example: Start the main activity after a delay of 2 seconds
        Thread timerThread = new Thread() {
            public void run() {
                try {
                    sleep(2000); // 2 seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    // Start the main activity
                    Intent intent = new Intent(LauncherActivity.this, AllFlowersActivity.class);
                    startActivity(intent);

                    // Finish the launcher activity
                    finish();
                }
            }
        };
        timerThread.start();
    }
}
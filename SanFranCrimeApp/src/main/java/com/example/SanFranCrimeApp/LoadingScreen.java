package com.example.SanFranCrimeApp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * Created by Danny on 2/23/14.
 */
public class LoadingScreen extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            //Remove title bar
            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
            //Remove notification bar
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.loading);

            final ProgressBar progress = (ProgressBar) findViewById(R.id.progressBar);
            progress.setVisibility(ProgressBar.VISIBLE);
            new Thread(new Runnable() {
                public void run() {
                    try {
                        DataBackend.getInstance(LoadingScreen.this);
                        progress.setVisibility(ProgressBar.INVISIBLE);
                    } catch (Exception e) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(LoadingScreen.this, "Could not load data.", Toast.LENGTH_LONG);
                            }
                        });
                    }
                    Intent main = new Intent(LoadingScreen.this, MainActivity.class);
                    startActivity(main);
                }
            }).start();
        } catch (Exception e) {

        }
    }
}

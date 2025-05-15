package com.example.myfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class FirstActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvLoading;
    private int progressStatus = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_first);

        progressBar = findViewById(R.id.progressBar);
        tvLoading = findViewById(R.id.tvLoading);
        ImageView imageView = findViewById(R.id.imgLogo);
        animateImageView(imageView);

        Thread t = new Thread() {
            public void run() {
                try {
                    while (progressStatus < 100) {
                        progressStatus++;
                        Thread.sleep(35);
                        new Handler(getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setProgress(progressStatus);
                                tvLoading.setText(progressStatus + "%");
                            }
                        });
                    }
                    sleep(500);
                    Intent i = new Intent(FirstActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        t.start();
    }

    private void animateImageView(ImageView imageView) {
        Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1500);
        fadeIn.setRepeatCount(Animation.INFINITE);
        fadeIn.setRepeatMode(Animation.REVERSE);
        imageView.startAnimation(fadeIn);
    }
}

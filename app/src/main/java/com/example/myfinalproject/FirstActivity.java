package com.example.myfinalproject;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class FirstActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_first);


        Thread t=new Thread() {
            public void run() {

                try {

                    sleep(3500);


                    Intent i=new Intent(FirstActivity.this,MainActivity.class);
                    startActivity(i);


                    finish();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        t.start();
    }
}

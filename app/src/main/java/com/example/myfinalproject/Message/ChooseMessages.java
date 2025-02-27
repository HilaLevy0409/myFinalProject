package com.example.myfinalproject.Message;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myfinalproject.R;

public class ChooseMessages extends AppCompatActivity implements View.OnClickListener {

   Button btnReviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_choose_messages);

   //     btnReviews = findViewById(R.id.btnReviews);
     //   btnReviews.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == btnReviews){
            createCustomDialog();
        }
    }

    private void createCustomDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setTitle("ביקורות");
  //      dialog.setContentView(R.layout.reviews_about_sum);
        dialog.show();
    }
}
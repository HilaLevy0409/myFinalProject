package com.example.myfinalproject;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myfinalproject.ContactUsProduct.ContactUsProductFragment;
import com.example.myfinalproject.LoginProduct.LoginProductFragment;
import com.example.myfinalproject.QuestionsProduct.QuestionsProductFragment;
import com.example.myfinalproject.RegistrationProduct.RegistrationProductFragment;
import com.example.myfinalproject.VisitorProduct.VisitorProductFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.myfinalproject.AdminLoginFragment.AdminLoginFragment;

import ChooseClassFragment.ChooseClassFragment;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);


        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.login);
        }

        bottomNavigationView
                .setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.login);
        bottomNavigationView.setSelectedItemId(R.id.registration);
        bottomNavigationView.setSelectedItemId(R.id.visitor);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.admin) {
            // Replace with AdminFragment
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, new ChooseClassFragment()) // Create AdminFragment
                    .addToBackStack(null) // Add to back stack for navigation
                    .commit();
            return true;
        } else if (item.getItemId() == R.id.contact) {
            // Replace with ContactFragment
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, new ContactUsProductFragment()) // Create ContactFragment
                    .addToBackStack(null)
                    .commit();
            return true;
        } else if (item.getItemId() == R.id.questions) {
            // Replace with QuestionsFragment
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, new QuestionsProductFragment()) // Create QuestionsFragment
                    .addToBackStack(null)
                    .commit();
            return true;
        }

        // Handle other options or default behavior
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.login) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, new LoginProductFragment())
                    .commit();
            return true;
        } else if (item.getItemId() == R.id.registration) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, new RegistrationProductFragment())
                    .commit();
        } else if (item.getItemId() == R.id.visitor) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, new VisitorProductFragment())
                    .commit();
            return true;
        }
        return false;
    }
}
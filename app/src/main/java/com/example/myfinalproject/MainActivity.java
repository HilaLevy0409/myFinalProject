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

import com.example.myfinalproject.AdminFragment.AdminFragment;
import com.example.myfinalproject.AdminLoginFragment.AdminLoginFragment;
import com.example.myfinalproject.ContactUsFragment.ContactUsFragment;
import com.example.myfinalproject.LoginFragment.LoginFragment;
import com.example.myfinalproject.QuestionsFragment.QuestionsFragment;
import com.example.myfinalproject.RegistrationFragment.RegistrationFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import ChooseClassFragment.ChooseClassFragment;
import UserProfileFragment.UserProfileFragment;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);





        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.login);
        }

        bottomNavigationView
                .setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.login);



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
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, new AdminLoginFragment())
                    .addToBackStack(null)
                    .commit();
            return true;
        } else if (item.getItemId() == R.id.contact) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, new ContactUsFragment())
                    .addToBackStack(null)
                    .commit();
            return true;
        } else if (item.getItemId() == R.id.questions) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, new QuestionsFragment())
                    .addToBackStack(null)
                    .commit();
            return true;
        } else if (item.getItemId() == R.id.user) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, new UserProfileFragment())
                    .addToBackStack(null)
                    .commit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.login) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, new LoginFragment())
                    .commit();
            return true;
        } else if (item.getItemId() == R.id.registration) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, new RegistrationFragment())
                    .commit();
            return true;
        } else if (item.getItemId() == R.id.visitor) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, new ChooseClassFragment())
                    .commit();
            return true;
        }
        return false;
    }
}
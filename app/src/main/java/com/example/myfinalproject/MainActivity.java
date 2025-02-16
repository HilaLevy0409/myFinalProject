package com.example.myfinalproject;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myfinalproject.AdminLoginFragment.AdminLoginFragment;
import com.example.myfinalproject.ChooseSumFragment.ChooseSumFragment;
import com.example.myfinalproject.ChooseUserFragment.ChooseUserFragment;
import com.example.myfinalproject.ContactUsFragment.ContactUsFragment;
import com.example.myfinalproject.LoginFragment.LoginFragment;
import com.example.myfinalproject.QuestionsFragment.QuestionsFragment;
import com.example.myfinalproject.RegistrationFragment.RegistrationFragment;
import com.example.myfinalproject.UserProfileFragment.UserProfileFragment;
import com.example.myfinalproject.WritingSumFragment.WritingSumFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;

import ChooseClassFragment.ChooseClassFragment;


public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);


        FirebaseApp.initializeApp(this);


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
        } else if (item.getItemId() == R.id.chooseSum) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, new ChooseSumFragment())
                    .addToBackStack(null)
                    .commit();
            return true;
        } else if (item.getItemId() == R.id.chooseUser) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, new ChooseUserFragment())
                .addToBackStack(null)
                .commit();
        return true;
    } else if(item.getItemId() == R.id.addSum) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, new WritingSumFragment())
                    .addToBackStack(null)
                    .commit();
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
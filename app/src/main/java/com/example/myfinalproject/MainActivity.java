package com.example.myfinalproject;

import android.app.Dialog;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.myfinalproject.AdminLoginFragment.AdminLoginFragment;
import com.example.myfinalproject.ChooseClassFragment.ChooseClassFragment;
import com.example.myfinalproject.ChooseUserFragment.ChooseUserFragment;
import com.example.myfinalproject.ContactUsFragment.ContactUsFragment;
import com.example.myfinalproject.Event.AlarmManagerFragment;
import com.example.myfinalproject.LoginFragment.LoginFragment;
import com.example.myfinalproject.QuestionsFragment.QuestionsFragment;
import com.example.myfinalproject.RegistrationFragment.RegistrationFragment;
import com.example.myfinalproject.Timer.TimerFragment;
import com.example.myfinalproject.UserProfileFragment.UserProfileFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    public void updateNavigationHeader() {
        NavigationView navigationView = findViewById(R.id.navView);
        View headerView = navigationView.getHeaderView(0);

        TextView tvUserName = headerView.findViewById(R.id.tvUserName);
        ImageView imageViewProfile = headerView.findViewById(R.id.imageViewProfile);

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String username = sharedPreferences.getString("username", "שם משתמש");
        String profileImage = sharedPreferences.getString("imageProfile", "");

        if (isLoggedIn && username != null && !username.isEmpty()) {
            tvUserName.setText(username);

            if (profileImage != null && !profileImage.isEmpty()) {
                try {
                    if (profileImage.startsWith("/9j/") || profileImage.startsWith("iVBOR")) {
                        byte[] decodedString = android.util.Base64.decode(profileImage, android.util.Base64.DEFAULT);
                        android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        if (bitmap != null) {
                            imageViewProfile.setImageBitmap(bitmap);
                        } else {
                            imageViewProfile.setImageResource(R.drawable.newlogo);
                        }
                    } else {
                        android.net.Uri imageUri = android.net.Uri.parse(profileImage);
                        android.graphics.Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        imageViewProfile.setImageBitmap(bitmap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    imageViewProfile.setImageResource(R.drawable.newlogo);
                }
            } else {
                imageViewProfile.setImageResource(R.drawable.newlogo);
            }
        } else {
            tvUserName.setText("");
            imageViewProfile.setImageResource(R.drawable.newlogo);
        }

        tvUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                boolean userLoggedIn = prefs.getBoolean("isLoggedIn", false);

                if (userLoggedIn) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.flFragment, new UserProfileFragment())
                            .commit();
                }

                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(this);

        updateNavigationHeader();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

            if (isLoggedIn) {
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, new UserProfileFragment()).commit();
            } else {
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, new LoginFragment()).commit();
                navigationView.setCheckedItem(R.id.login);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            if (id == R.id.registration || id == R.id.visitor || id == R.id.login) {
                new android.app.AlertDialog.Builder(MainActivity.this)
                        .setTitle("לא ניתן לבצע פעולה")
                        .setMessage("כדי לבחור אפשרות כניסה אחרת, יש להתנתק קודם.")
                        .setPositiveButton("סגירה", null)
                        .show();
                return false;
            }
        }
        if (id == R.id.login) {
            getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, new LoginFragment()).commit();
        } else if (id == R.id.about) {
            Dialog dialog = new Dialog(this);
            dialog.setTitle("אודות על האפליקציה");
            dialog.setContentView(R.layout.about);
            dialog.setCancelable(true);
            dialog.show();
        } else if (id == R.id.registration) {
            getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, new RegistrationFragment()).commit();
        } else if (id == R.id.visitor) {
            getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, new ChooseClassFragment()).commit();
        } else if (id == R.id.chooseUser) {
            getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, new ChooseUserFragment()).commit();
        } else if (id == R.id.reminder) {
            getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, new AlarmManagerFragment()).commit();
        } else if (id == R.id.questions) {
            getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, new QuestionsFragment()).commit();
        } else if (id == R.id.contact) {
            getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, new ContactUsFragment()).commit();
        } else if (id == R.id.admin) {
            getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, new AdminLoginFragment()).commit();
        } else if (id == R.id.timer) {
            getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, new TimerFragment()).commit();
        } else if (id == R.id.logout) {
            new android.app.AlertDialog.Builder(MainActivity.this)
                    .setTitle("התנתקות")
                    .setMessage("האם ברצונך להתנתק?")
                    .setPositiveButton("כן", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.apply();

                        updateNavigationHeader();

                        getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.flFragment, new LoginFragment())
                                .commit();
                    })
                    .setNegativeButton("לא", null)
                    .setCancelable(false)
                    .show();
        } else if (id == R.id.chooseClass) {
            getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, new ChooseClassFragment()).commit();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
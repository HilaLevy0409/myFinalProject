package com.example.myfinalproject.RegistrationFragment;

import com.example.myfinalproject.R;
import com.example.myfinalproject.Models.User;
import com.example.myfinalproject.Database.UserDatabase;
import com.example.myfinalproject.Utils.Validator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.provider.MediaStore;

import java.util.Calendar;

public class RegistrationFragment extends Fragment implements View.OnClickListener {


    private StorageReference mStorage;

    private Button btnUploadPhoto;
    private Button btnN;
    private Button btnDialogBirthday;
    private EditText etEmail, etUser, etPassword, etPassword2;
    private ImageView profileImageView;
    private Uri imageUri;
    private RegisterUserPresenter presenter;

    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_GALLERY_PICK = 102;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_registration, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mStorage = FirebaseStorage.getInstance().getReference("ProfileImages");
        presenter = new RegisterUserPresenter(this);
        btnN = view.findViewById(R.id.btnN);
        btnUploadPhoto = view.findViewById(R.id.btnUploadPhoto);
        btnDialogBirthday = view.findViewById(R.id.btnDialogBirthday);

        profileImageView = view.findViewById(R.id.imageView3);

        btnUploadPhoto.setOnClickListener(this);
        btnDialogBirthday.setOnClickListener(this);
        btnN.setOnClickListener(this);

        etEmail = view.findViewById(R.id.etEmail);
        etUser = view.findViewById(R.id.etUser);
        etPassword = view.findViewById(R.id.etPassword);
        etPassword2 = view.findViewById(R.id.etPassword2);







    }



    @Override
    public void onClick(View v) {
        if (v == btnN) {
            String username = etUser.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String email = etEmail.getText().toString().trim();

            String validUsername = Validator.isValidUsername(username);
            String validPassword = Validator.isValidPassword(password);
            String validEmail = Validator.isValidEmail(email);

/*
            if (!validUsername.isEmpty()) {
                Toast.makeText(getContext(), validUsername, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!validPassword.isEmpty()) {
                Toast.makeText(getContext(), validPassword, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!(etPassword.getText().toString().equals(etPassword2.getText().toString()))) {
                Toast.makeText(getContext(), "הסיסמאות לא תואמות", Toast.LENGTH_SHORT).show();
                return;
            }
            if (email.isEmpty()) {
                Toast.makeText(getContext(), "שדה האימייל ריק", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!validEmail.isEmpty()) {
                Toast.makeText(getContext(), validEmail, Toast.LENGTH_SHORT).show();
                return;
            }

 */



            saveUserData(imageUri != null ? imageUri.toString() : "");

            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, new ChooseClassFragment.ChooseClassFragment())
                    .commit();
        }
        if (v == btnDialogBirthday) {
            openDialog();
        }
        if (v == btnUploadPhoto) {
            showImageSourceDialog();
        }



    }


    private void showImageSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("בחירת מקור תמונה")
                .setItems(new CharSequence[]{"מצלמה", "גלריה"}, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else {
                        openGallery();
                    }
                })
                .show();
    }


    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY_PICK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    imageUri = (Uri) extras.get("data");  // Get URI from camera
                    profileImageView.setImageURI(imageUri);
                }
            } else if (requestCode == REQUEST_GALLERY_PICK) {
                imageUri = data.getData();  // Get URI from gallery
                profileImageView.setImageURI(imageUri);
            }
        }
    }

    private void uploadImage() {
        if (imageUri != null) {
            StorageReference fileRef = mStorage.child(System.currentTimeMillis() + ".jpg");
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();
                                saveUserData(imageUrl);  // Save image URL and other user data
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to get image URL", Toast.LENGTH_SHORT).show()))
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Upload failed", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserData(String imageUrl) {
        String email = etEmail.getText().toString();
        String username = etUser.getText().toString();
        String password = etPassword.getText().toString();

        // Validation
//        String validPassword = Validator.isValidPassword(password);
//        String validUsername = Validator.isValidUsername(username);
//        if (!validPassword.isEmpty()) {
//            Toast.makeText(getContext(), validPassword, Toast.LENGTH_SHORT).show();
//
//        }if (!validUsername.isEmpty())
//        {
//            Toast.makeText(getContext(), validUsername, Toast.LENGTH_SHORT).show();
//
//        }

        User user = new User(username, password, email);
        submitClicked(user);
//        mDatabase.child(mAuth.getCurrentUser().getUid()).setValue(user)
//                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "User registered successfully!", Toast.LENGTH_SHORT).show())
//                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to save user data", Toast.LENGTH_SHORT).show());
    }
    public void submitClicked(User user) {
        presenter.submitClicked(user);
    }

    public void openDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year1, month1, dayOfMonth) -> {
                    String date = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                },
                year, month, day
        );

        btnDialogBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });


    }
}

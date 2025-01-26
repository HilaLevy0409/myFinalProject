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
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class RegistrationFragment extends Fragment implements View.OnClickListener {

    private static final String IMAGE_DIRECTORY = "/demonuts";
    private int GALLERY = 1, CAMERA = 2;
    final int REQUEST_CODE_GALLERY = 999;


    private StorageReference mStorage;

    private Button btnUploadPhoto;
    private Button btnN;
    private Button btnDialogBirthday;
    private EditText etEmail, etUser, etPassword, etPassword2, etPhone;
    private Uri imageUri;
    private RegisterUserPresenter presenter;

    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_GALLERY_PICK = 102;
    private ImageView imageViewProfile;

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

        imageViewProfile = view.findViewById(R.id.imageViewProfile);

        btnUploadPhoto.setOnClickListener(this);
        btnDialogBirthday.setOnClickListener(this);
        btnN.setOnClickListener(this);

        etEmail = view.findViewById(R.id.etEmail);
        etUser = view.findViewById(R.id.etUser);
        etPassword = view.findViewById(R.id.etPassword);
        etPassword2 = view.findViewById(R.id.etPassword2);
        etPhone = view.findViewById(R.id.etPhone);
    }



    @Override
    public void onClick(View v) {
        if (v == btnN) {
            String username = etUser.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String password2 = etPassword2.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();



            String validPassword = Validator.isValidPassword(password);
            String validEmail = Validator.isValidEmail(email);
            String validUsername = Validator.isValidUsername(username);
            if(!validEmail.isEmpty()){
                Toast.makeText(getContext(), validEmail, Toast.LENGTH_SHORT).show();
                return;
            }
            if(!validUsername.isEmpty()){
                Toast.makeText(getContext(), validUsername, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!validPassword.isEmpty()) {
                Toast.makeText(getContext(), validPassword, Toast.LENGTH_SHORT).show();
                return;
            }


            if(!(password.equals(password2))){
                Toast.makeText(getContext(), "הסיסמאות לא תואמות", Toast.LENGTH_SHORT).show();
                return;
            }


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
            showPictureDialog();
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
        String phone = etPhone.getText().toString();
        User user = new User(username, password, email, phone);
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

    private void showPictureDialog(){
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(getContext());
        pictureDialog.setTitle("בחירת מקור תמונת פרופיל:");
        String[] pictureDialogItems = {
                "מהגלריה",
                "מהמצלמה" };
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                choosePhotoFromGallary();
                                break;
                            case 1:
                                takePhotoFromCamera();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    public void choosePhotoFromGallary() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);


        startActivityForResult(galleryIntent, GALLERY);
    }

    private void takePhotoFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                Uri contentURI = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), contentURI);
                    String path = saveImage(bitmap);
                    Toast.makeText(getContext(), "התמונה נשמרה!", Toast.LENGTH_SHORT).show();
                    imageViewProfile.setImageBitmap(bitmap);


                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "נכשל!", Toast.LENGTH_SHORT).show();
                }
            }


        }
        if (requestCode == CAMERA) {
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            imageViewProfile.setImageBitmap(thumbnail);
            saveImage(thumbnail);
            Toast.makeText(getContext(), "התמונה נשמרה!", Toast.LENGTH_SHORT).show();


        }
    }


    public String saveImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File wallpaperDirectory = new File(Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs();
        }


        try {
            File f = new File(wallpaperDirectory, Calendar.getInstance()
                    .getTimeInMillis() + ".jpg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(getContext(),
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d("TAG", "File Saved::--->" + f.getAbsolutePath());


            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }




    private byte[] imageViewToByte(ImageView image) {
        Bitmap bitmap=((BitmapDrawable)image.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 0, stream);
        byte[]byteArray=stream.toByteArray();
        return byteArray;
    }



}
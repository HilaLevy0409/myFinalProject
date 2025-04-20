package com.example.myfinalproject.RegistrationFragment;

import com.example.myfinalproject.ChooseClassFragment.ChooseClassFragment;
import com.example.myfinalproject.R;
import com.example.myfinalproject.Models.User;
import com.example.myfinalproject.Database.UserDatabase;
import com.example.myfinalproject.Utils.Validator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
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

    private Button btnUploadPhoto, btnN;
    private EditText etEmail, etUser, etPassword, etPassword2, etPhone, etDialogBirthday;
    private Uri imageUri;
    private RegisterUserPresenter presenter;

    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_GALLERY_PICK = 102;

    //new
//    private static final int REQUEST_CAMERA_PERMISSION = 100;
//    private static final int REQUEST_STORAGE_PERMISSION = 101;
//    private static final int REQUEST_IMAGE_CAPTURE = 1;
//    private static final int REQUEST_IMAGE_GALLERY = 2;
//    private static final String AUTHORITY = "com.example.firestorepicapplication.fileprovider";
//    private final int PICK_IMAGE_REQUEST = 71;
//    FirebaseUser user;
//   UserReference userReference;
//
//
//    private ListView listViewUsers;
//    private UserAdapter userAdapter;
//    private List<User> userList;


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
        etDialogBirthday = view.findViewById(R.id.etDialogBirthday);

        imageViewProfile = view.findViewById(R.id.imageViewProfile);

        btnUploadPhoto.setOnClickListener(this);
        btnN.setOnClickListener(this);

        etEmail = view.findViewById(R.id.etEmail);
        etUser = view.findViewById(R.id.etUser);
        etPassword = view.findViewById(R.id.etPassword);
        etPassword2 = view.findViewById(R.id.etPassword2);
        etPhone = view.findViewById(R.id.etPhone);

//        user = FirebaseStorage.getInstance();
//        userReference = user.getReference();
//        requestCameraPermission();
//        requestStoragePermission();

        etDialogBirthday.setOnClickListener(v -> openDialog());


    }




    @Override
    public void onClick(View v) {
        if (v == btnN) {
            String username = etUser.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String password2 = etPassword2.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String birthDate =  etDialogBirthday.getText().toString();

//        Uri imageUri = (Uri) imageViewProfile.getTag();
//        String imageProfile = imageUri.toString();
//
//        BitmapDrawable drawable = (BitmapDrawable) imageViewProfile.getDrawable();
//        Bitmap bitmap = drawable.getBitmap();
//
//        String imageViewProfile = imageUri != null ? imageUri.toString() : "";



            String validPassword = Validator.isValidPassword(password);
            String validEmail = Validator.isValidEmail(email);
            String validUsername = Validator.isValidUsername(username);
            String validPhone = Validator.isValidPhone(phone);
            String validBirthDate = Validator.isValidBirthDate(birthDate);


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

            if(!validPhone.isEmpty()){
                Toast.makeText(getContext(), validPhone, Toast.LENGTH_SHORT).show();
                return;
            }

            if(!validBirthDate.isEmpty()){
                Toast.makeText(getContext(), validBirthDate, Toast.LENGTH_SHORT).show();
                return;
            }


            saveUserData(imageUri != null ? imageUri.toString() : "");

            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, new ChooseClassFragment())
                    .commit();
        }
        if (v == etDialogBirthday) {
            openDialog();
        }

        if (v == btnUploadPhoto) {
            showPictureDialog();
        }

//        listViewUsers = findViewById(R.id.listViewUsers);
//        userList = new ArrayList<>();
//        userAdapter = new UserAdapter(this, userList);
//        listViewUsers.setAdapter(userAdapter);
//        loadUsers();
//
//
//
//
//        listViewUsers.setOnItemClickListener((parent, view, position, id) -> {
//            User selectedUser = userList.get(position);
//            showManagementDialog(selectedUser);
//        });




    }

//    private void showManagementDialog(User user) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        builder.setTitle("ניהול משתמש");
//        String[] options = {"עריכה", "מחיקה", "ביטול"};
//
//
//        builder.setItems(options, (dialog, which) -> {
//            switch (which) {
//                case 0: // Edit
//                    showEditDialog(user);
//                    break;
//                case 1: // Delete
//                    showDeleteConfirmationDialog(user);
//                    break;
//                case 2: // Cancel
//                    dialog.dismiss();
//                    break;
//            }
//        });
//
//
//        builder.show();
//    }
//
//    private void showEditDialog(User user) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_user, null);
//
//
//        EditText etName = dialogView.findViewById(R.id.etEditName);
//        ImageView imgUser = dialogView.findViewById(R.id.imgEditUser);
//        Button btnChangeImage = dialogView.findViewById(R.id.btnChangeImage);
//
//
//        etName.setText(user.getUserName());
//        loadBase64Image(user.getUserPic(), imgUser);
//
//
//        btnChangeImage.setOnClickListener(v -> {
//            // Store the current ImageView reference for later use
//            imageView = imgUser;
//            showPictureDialog();
//        });
//
//
//        builder.setView(dialogView)
//                .setPositiveButton("שמור", (dialog, which) -> {
//                    user.setUserName(etName.getText().toString());
//                    if (imgUser.getDrawable() != null) {
//                        user.setProdPic(imageViewToBase64(imgUser));
//                    }
//                    RegisterUserPresenter.updateUser(user);
//                })
//                .setNegativeButton("ביטול", (dialog, which) -> dialog.cancel());
//
//
//        builder.show();
//    }
//private void showDeleteConfirmationDialog(User user) {
//    new AlertDialog.Builder(getContext())
//            .setTitle("מחיקת ")
//            .setMessage("האם ברצונך למחוק?")
//            .setPositiveButton("כן", (dialog, which) ->
//                   RegisterUserPresenter.deleteUser(user.getId()))
//            .setNegativeButton("לא", null)
//            .show();
//}
//
//
//    public void onUserUpdated(User user) {
//        Toast.makeText(getContext(), "המוצר עודכן בהצלחה", Toast.LENGTH_SHORT).show();
//        loadUsers();
//    }
//
//
//    public void onUserDeleted() {
//        Toast.makeText(getContext(), "נמחק בהצלחה", Toast.LENGTH_SHORT).show();
//        loadUsers();
//    }
//
//
//    public void onError(String message) {
//        Toast.makeText(getContext(), "שגיאה: " + message, Toast.LENGTH_SHORT).show();
//    }

//
//    private void loadUsers() {
//        RegisterUserPresenter.loadUsers(new UserDatabase().UsersCallback() {
//            @Override
//            public void onSuccess(List<Users> users) {
//                userList.clear();
//                userList.addAll(users);
//                userAdapter.notifyDataSetChanged();
//            }
//
//
//            @Override
//            public void onError(String message) {
//                Toast.makeText(RegistrationFragment.this, "Error loading products: " + message,
//                        Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//
//    private void requestStoragePermission() {
//    }
//    private void launchCamera() {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        // Ensure that there's a camera activity to handle the intent
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            // Create the File where the photo should go
//            File photoFile = null;
//            try {
//                photoFile = createImageFile();
//            } catch (IOException ex) {
//                // Error occurred while creating the File
//                ex.printStackTrace();
//            }
//            // Continue only if the File was successfully created
//            Uri photoURI = FileProvider.getUriForFile(getContext(), "com.example.firestorepicapplication.fileprovider", photoFile);
//            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
//            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//        }
//    }
//
//
//    private File createImageFile() throws IOException {
//        // Create an image file name
//        String imageFileName = "JPEG_" + System.currentTimeMillis() + ".jpg";
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
//        return image;
//    }


//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//
//        if (requestCode == REQUEST_CAMERA_PERMISSION) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permission granted, proceed with camera launch
//                launchCamera();
//            } else {
//                // Permission denied, handle the error
//                Toast.makeText(getContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

//
//
//
//    public void onSuccess(User user) {
//        Toast.makeText(getContext(), user.getUserName() + " added", Toast.LENGTH_SHORT).show();
//    }




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
        String birthDate =  etDialogBirthday.getText().toString();

//        Uri imageUri = (Uri) imageViewProfile.getTag();
//        String imageProfile = imageUri.toString();
//
//        BitmapDrawable drawable = (BitmapDrawable) imageViewProfile.getDrawable();
//        Bitmap bitmap = drawable.getBitmap();
//
//        String imageViewProfile = imageUri != null ? imageUri.toString() : "";


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

                    etDialogBirthday.setText(String.format("%d-%d-%d", dayOfMonth, month + 1, year));

                },
                year, month, day
        );

        etDialogBirthday.setOnClickListener(new View.OnClickListener() {
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




//    private void requestCameraPermission() {
//        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions((Activity) getContext(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
//        }
//    }

    private byte[] imageViewToByte(ImageView image) {
        Bitmap bitmap=((BitmapDrawable)image.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 0, stream);
        byte[]byteArray=stream.toByteArray();
        return byteArray;
    }

    private void chooseImage() {


    }


//    @Override
//    public void onClick(View v) {
//        if (v == btnN) {
//            EditText etUser = findViewById(R.id.etUser);
//
//            // Convert image to Base64 string instead of byte array
//            String base64Image = imageViewToBase64(imageView);
//
//
//            User user = new User(
//                    "",
//                    etUser.getText().toString(),
//                    Integer.parseInt(etAmountProd.getText().toString()),
//                    base64Image
//            );
//            RegisterUserPresenter.submitClicked(user);
//        }
//    }
//
//
//    private String imageViewToBase64(ImageView image) {
//        try {
//            Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream); // Use 70% quality for better storage
//            byte[] byteArray = stream.toByteArray();
//            return Base64.encodeToString(byteArray, Base64.DEFAULT);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//
//
//    private void loadBase64Image(String base64Image, ImageView imageView) {
//        try {
//            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
//            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//            imageView.setImageBitmap(decodedByte);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


}
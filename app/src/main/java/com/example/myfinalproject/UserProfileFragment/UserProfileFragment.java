package com.example.myfinalproject.UserProfileFragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myfinalproject.LoginFragment.LoginFragment;
import com.example.myfinalproject.Message.ChooseMessages;
import com.example.myfinalproject.Models.User;
import com.example.myfinalproject.R;
import com.example.myfinalproject.SaveSummaryFragment;
import com.example.myfinalproject.SumByUserFragment.SumByUserFragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class UserProfileFragment extends Fragment implements View.OnClickListener {
    private TextView tvEmail, tvPhoneNumber, tvBirthDate, tvUsername, tvBadPoints, tvSumNum;
    private Button btnShowSums, btnDeleteUser, btnLogOut, btnEdit, btnFinish, btnUploadPhoto, btnSaveSummary, btnSendMessage;
    private ImageView imageView, imageViewProfile;
    private UserProfilePresenter presenter;
    private EditText etEmail, etPhoneNumber, etUsername, etBirthDate;

    private static final String IMAGE_DIRECTORY = "/demonuts";
    private int GALLERY = 1, CAMERA = 2;
    final int REQUEST_CODE_GALLERY = 999;
    private User currentUser;

//    private StorageReference mStorage;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_GALLERY_PICK = 102;
//    private Uri imageUri;
private static final String AUTHORITY = "com.example.firestorepicapplication.fileprovider";

//    private Uri filePath;
//    private final int PICK_IMAGE_REQUEST = 71;
//    FirebaseStorage storage;
//
//    private ListView listViewUsers;
//    private UserAdapter userAdapter;
//    private List<User> userList;
//



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initializeViews(view);
        // Initialize presenter
        presenter = new UserProfilePresenter(this);
        // Load user data from Firestore using the stored userId
        presenter.loadUserData();

//        mStorage = FirebaseStorage.getInstance().getReference("ProfileImages");
        presenter = new UserProfilePresenter(this);
    }

    private void initializeViews(View view) {
        tvEmail = view.findViewById(R.id.tvEmail);
        tvPhoneNumber = view.findViewById(R.id.tvPhoneNumber);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvBirthDate = view.findViewById(R.id.tvBirthDate);
        tvBadPoints = view.findViewById(R.id.tvBadPoints);
        tvSumNum = view.findViewById(R.id.tvSumNum);

        btnShowSums = view.findViewById(R.id.btnShowSums);
        btnDeleteUser = view.findViewById(R.id.btnDeleteUser);
        btnLogOut = view.findViewById(R.id.btnLogOut);
        btnEdit = view.findViewById(R.id.btnEdit);
        imageView = view.findViewById(R.id.imageView);

        etEmail = view.findViewById(R.id.etEmail);
        etPhoneNumber = view.findViewById(R.id.etPhoneNumber);
        etUsername = view.findViewById(R.id.etUsername);
        imageViewProfile = view.findViewById(R.id.imageViewProfile);
        btnFinish = view.findViewById(R.id.btnFinish);

        btnSendMessage = view.findViewById(R.id.btnSendMessage);
        btnSaveSummary = view.findViewById(R.id.btnSaveSummary);
//        btnUploadPhoto = view.findViewById(R.id.btnUploadPhoto);

        btnShowSums.setOnClickListener(this);
        btnDeleteUser.setOnClickListener(this);
        btnLogOut.setOnClickListener(this);
        btnEdit.setOnClickListener(this);

//        btnFinish.setOnClickListener(v -> saveUserData());

//        btnUploadPhoto.setOnClickListener(this);
        btnSaveSummary.setOnClickListener(this);
        btnSendMessage.setOnClickListener(this);

    }

    private void createCustomDialog() {
        final Dialog dialog = new Dialog(getContext());
        dialog.setTitle("עריכת פרטים");
        dialog.setContentView(R.layout.edit_user_profile);

        // Get references from the custom dialog layout.
        EditText etEditEmail = dialog.findViewById(R.id.etEmail);
        EditText etEditPhone = dialog.findViewById(R.id.etPhoneNumber);
        EditText etEditUsername = dialog.findViewById(R.id.etUsername);
        Button btnFinishDialog = dialog.findViewById(R.id.btnFinish);
        etBirthDate = dialog.findViewById(R.id.etBirthDate);

        // Pre-populate fields with current user data if available.
        if (currentUser != null) {
            etEditEmail.setText(currentUser.getUserEmail());
            etEditPhone.setText(currentUser.getPhone());
            etEditUsername.setText(currentUser.getUserName());
            // Optionally pre-populate a birth date field if you have one.
            // etEditBirthDate.setText(currentUser.getBirthDate());
        }

        // Set a click listener on the birth date button (if needed).
        etBirthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openDialog(); // This opens the DatePickerDialog.
            }



        });

        // When the finish button is clicked, update the user.
        btnFinishDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newEmail = etEditEmail.getText().toString().trim();
                String newPhone = etEditPhone.getText().toString().trim();
                String newUsername = etEditUsername.getText().toString().trim();
                String newBirthDate = etBirthDate.getText().toString();
//                String imageViewProfile = imageViewProfile.

                if(newEmail.isEmpty() || newPhone.isEmpty() || newUsername.isEmpty() || newBirthDate.isEmpty()) {
                    showError("יש למלא את כל השדות!");
                    return;
                }

                // Create a new User object with the updated fields.
                User updatedUser = new User();
                // Preserve the current user ID.
                updatedUser.setId(currentUser != null ? currentUser.getId() : "");
                updatedUser.setUserEmail(newEmail);
                updatedUser.setPhone(newPhone);
                updatedUser.setUserName(newUsername);

                // Optionally preserve other fields from the current user.
                if (currentUser != null) {
                    updatedUser.setBadPoints(currentUser.getBadPoints());
                    updatedUser.setSumCount(currentUser.getSumCount());
                    // If you have a birth date field, update it here as well.
                }

                // Call the presenter to submit the update.
                presenter.submitClicked(updatedUser);
                dialog.dismiss();
            }
        });

        dialog.show();
    }


//    private void saveUserData() {
//        String newEmail = etEmail.getText().toString().trim();
//        String newPhone = etPhoneNumber.getText().toString().trim();
//        String newUsername = etUsername.getText().toString().trim();
//
//        if (newEmail.isEmpty() || newPhone.isEmpty() || newUsername.isEmpty()) {
//            showError("יש למלא את כל השדות!");
//            return;
//        }
//
//        User updatedUser = new User();
//        updatedUser.setUserEmail(newEmail);
//        updatedUser.setPhone(newPhone);
//        updatedUser.setUserName(newUsername);
//
//        presenter.updateUserData(updatedUser);
//    }
//
//    public void onUpdateSuccess() {
//        Toast.makeText(getContext(), "פרטים עודכנו בהצלחה!", Toast.LENGTH_SHORT).show();
//        presenter.loadUserData(); // טען מחדש את הנתונים
//    }
//
//    public void onUpdateError(String errorMessage) {
//        showError(errorMessage);
//    }


    public void displayUserData(User user) {
        // Cache the current user so we can pre-populate the edit dialog.
        this.currentUser = user;
        tvEmail.setText("אימייל: " + user.getUserEmail());
        tvPhoneNumber.setText("מספר טלפון: " + user.getPhone());
        tvBirthDate.setText("תאריך לידה: " + user.getUserBirthDate());
        tvUsername.setText("שם משתמש: " + user.getUserName());
        tvBadPoints.setText("נקודות לרעה: " + user.getBadPoints());
        tvSumNum.setText("מספר סיכומים שנכתבו: " + user.getSumCount());
    }
    @Override
    public void onClick(View view) {
        if (view == btnShowSums) {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, new SumByUserFragment())
                    .commit();
        } else if (view == btnDeleteUser) {
            presenter.deleteUser();
        } else if (view == btnLogOut) {
            presenter.logOut();
        } else if (view == btnEdit) {
            createCustomDialog();
        } else if (view == etBirthDate) {
            openDialog();
        } else if (view == btnUploadPhoto) {
            showPictureDialog();
        } else if (view == btnSaveSummary) {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, new SaveSummaryFragment())
                    .commit();
        } else if (view == btnSendMessage) {
            Intent intent = new Intent(getContext(), ChooseMessages.class);
            startActivity(intent);
        }
    }
    public void submitClicked(User userChange) {
        presenter.submitClicked(userChange);
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




    public void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void onLogOutSuccess() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPrefs", getContext().MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();

        // TODO: Replace with your login fragment
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, new LoginFragment())
                .commit();
    }

    public void onDeleteSuccess() {
        Toast.makeText(getContext(), "המשתמש נמחק בהצלחה", Toast.LENGTH_SHORT).show();
        onLogOutSuccess();
    }



    public void openDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year1, month1, dayOfMonth) -> {
                    String date = dayOfMonth + "/" + (month1 + 1) + "/" + year1;

                    etBirthDate.setText(String.format("%d-%d-%d", dayOfMonth, month + 1, year));

                },
                year, month, day
        );

        datePickerDialog.show();



    }

//    public void showSuccessMessage(String message) {
//        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
//    }



}
package com.example.myfinalproject.UserProfileFragment;

import static com.google.firebase.appcheck.internal.util.Logger.TAG;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.example.myfinalproject.Message.ChooseMessageFragment;
import com.example.myfinalproject.DataModels.User;
import com.example.myfinalproject.R;
import com.example.myfinalproject.RegistrationFragment.RegistrationFragment;
import com.example.myfinalproject.SaveSummaryFragment.SaveSummaryFragment;
import com.example.myfinalproject.SumByUserFragment.SumByUserFragment;

import com.example.myfinalproject.MainActivity;
import com.example.myfinalproject.Utils.Validator;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class UserProfileFragment extends Fragment implements View.OnClickListener {
    private TextView tvEmail, tvPhoneNumber, tvBirthDate, tvUsername, tvBadPoints, tvSumNum;
    private Button btnShowSummaries, btnDeleteUser, btnLogOut, btnEdit, btnFinish, btnUploadPhoto, btnSaveSummary, btnSendMessage;
    private ImageView imageView, imageViewProfileEdit;
    private UserProfilePresenter presenter;
    private EditText etEmail, etPhoneNumber, etUsername, etBirthDate;

    private static final String IMAGE_DIRECTORY = "/demonuts";
    private int GALLERY = 1, CAMERA = 2;
    private User currentUser;


    private ImageView currentDialogImageView;

    private EditText currentDialogBirthDateEditText;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }




    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        presenter = new UserProfilePresenter(this);
        presenter.loadUserData();


        tvEmail = view.findViewById(R.id.tvEmail);
        tvPhoneNumber = view.findViewById(R.id.tvPhoneNumber);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvBirthDate = view.findViewById(R.id.tvBirthDate);
        tvBadPoints = view.findViewById(R.id.tvBadPoints);
        tvSumNum = view.findViewById(R.id.tvSumNum);


        btnShowSummaries = view.findViewById(R.id.btnShowSummaries);
        btnDeleteUser = view.findViewById(R.id.btnDeleteUser);
        btnLogOut = view.findViewById(R.id.btnLogOut);
        btnEdit = view.findViewById(R.id.btnEdit);
        imageView = view.findViewById(R.id.imageView);

        etEmail = view.findViewById(R.id.etEmail);
        etPhoneNumber = view.findViewById(R.id.etPhoneNumber);
        etUsername = view.findViewById(R.id.etUsername);
        imageViewProfileEdit = view.findViewById(R.id.imageViewProfileEdit);
        btnFinish = view.findViewById(R.id.btnFinish);

        btnSendMessage = view.findViewById(R.id.btnSendMessage);
        btnSaveSummary = view.findViewById(R.id.btnSaveSummary);


        btnShowSummaries.setOnClickListener(this);
        btnDeleteUser.setOnClickListener(this);
        btnLogOut.setOnClickListener(this);
        btnEdit.setOnClickListener(this);


        btnSaveSummary.setOnClickListener(this);
        btnSendMessage.setOnClickListener(this);



    }









    private void createCustomDialog() {
        final Dialog dialog = new Dialog(getContext());
        dialog.setTitle("עריכת פרטים");
        dialog.setContentView(R.layout.edit_user_profile);

        EditText etEditEmail = dialog.findViewById(R.id.etEmail);
        EditText etEditPhone = dialog.findViewById(R.id.etPhoneNumber);
        EditText etEditUsername = dialog.findViewById(R.id.etUsername);
        Button btnFinishDialog = dialog.findViewById(R.id.btnFinish);
        EditText etBirthDate = dialog.findViewById(R.id.etBirthDate);
        ImageView dialogImageView = dialog.findViewById(R.id.imageViewProfileEdit);
        Button btnUploadPhoto = dialog.findViewById(R.id.btnUploadPhoto);

        if (currentUser != null) {
            etEditEmail.setText(currentUser.getUserEmail());
            etEditPhone.setText(currentUser.getPhone());
            etEditUsername.setText(currentUser.getUserName());
            etBirthDate.setText(currentUser.getUserBirthDate());

            String imageProfileData = currentUser.getImageProfile();
            if (imageProfileData != null && !imageProfileData.isEmpty()) {
                try {
                    if (imageProfileData.startsWith("/9j/") || imageProfileData.startsWith("iVBOR")) {
                        byte[] decodedString = android.util.Base64.decode(imageProfileData, android.util.Base64.DEFAULT);
                        Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        if (bitmap != null) {
                            dialogImageView.setImageBitmap(bitmap);
                        }
                    }
                } catch (Exception e) {
                    dialogImageView.setImageResource(R.drawable.newlogo);
                }
            } else {
                dialogImageView.setImageResource(R.drawable.newlogo);
            }
        }

        if (btnUploadPhoto != null) {
            btnUploadPhoto.setOnClickListener(v -> {
                currentDialogImageView = dialogImageView;
                showPictureDialog();
            });
        }

        etBirthDate.setOnClickListener(v -> {
            currentDialogBirthDateEditText = etBirthDate;
            openDialog();
        });

        btnFinishDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newEmail = etEditEmail.getText().toString().trim();
                String newPhone = etEditPhone.getText().toString().trim();
                String newUsername = etEditUsername.getText().toString().trim();
                String newBirthDate = etBirthDate.getText().toString();


                if (newEmail.isEmpty() || newPhone.isEmpty() || newUsername.isEmpty() || newBirthDate.isEmpty()) {
                    showError("יש למלא את כל השדות!");
                    return;
                }

                String validEmail = Validator.isValidEmail(newEmail);
                String validUsername = Validator.isValidUsername(newUsername);
                String validPhone = Validator.isValidPhone(newPhone);

                if (!validEmail.isEmpty()) {
                    Toast.makeText(getContext(), validEmail, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!validUsername.isEmpty()) {
                    Toast.makeText(getContext(), validUsername, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!validPhone.isEmpty()) {
                    Toast.makeText(getContext(), validPhone, Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                db.collection("users")
                        .whereEqualTo("userEmail", newEmail)
                        .get()
                        .addOnCompleteListener(emailTask -> {
                            if (emailTask.isSuccessful()) {
                                boolean emailExists = !emailTask.getResult().isEmpty() &&
                                        (currentUser == null || !newEmail.equals(currentUser.getUserEmail()));
                                if (emailExists) {
                                    etEditEmail.setError("האימייל הזה כבר בשימוש");
                                    return;
                                }

                                db.collection("users")
                                        .whereEqualTo("userName", newUsername)
                                        .get()
                                        .addOnCompleteListener(usernameTask -> {
                                            if (usernameTask.isSuccessful()) {
                                                boolean usernameExists = !usernameTask.getResult().isEmpty() &&
                                                        (currentUser == null || !newUsername.equals(currentUser.getUserName()));
                                                if (usernameExists) {
                                                    etEditUsername.setError("השם הזה כבר בשימוש");
                                                    return;
                                                }


                                                User updatedUser = new User();
                                                updatedUser.setId(currentUser != null ? currentUser.getId() : "");
                                                updatedUser.setUserEmail(newEmail);
                                                updatedUser.setPhone(newPhone);
                                                updatedUser.setUserName(newUsername);
                                                updatedUser.setUserBirthDate(newBirthDate);

                                                if (currentUser != null) {
                                                    updatedUser.setUserPass(currentUser.getUserPass());
                                                    updatedUser.setBadPoints(currentUser.getBadPoints());
                                                    updatedUser.setSumCount(currentUser.getSumCount());
                                                }

                                                if (currentDialogImageView != null && currentDialogImageView.getDrawable() != null) {
                                                    String base64Image = imageViewToBase64(currentDialogImageView);
                                                    if (base64Image != null) {
                                                        updatedUser.setImageProfile(base64Image);
                                                    }
                                                } else if (currentUser != null) {
                                                    updatedUser.setImageProfile(currentUser.getImageProfile());
                                                }

                                                if (currentUser != null) {
                                                    updatedUser.setBadPoints(currentUser.getBadPoints());
                                                    updatedUser.setSumCount(currentUser.getSumCount());
                                                }

                                                presenter.submitClicked(updatedUser);
                                                dialog.dismiss();



                                            } else {
                                                Log.d("Firestore", "שגיאה בבדיקת שם משתמש: ", usernameTask.getException());
                                            }
                                        });

                            } else {
                                Log.d("Firestore", "שגיאה בבדיקת אימייל: ", emailTask.getException());
                            }
                        });
            }
        });

        dialog.show();
    }




    private String imageViewToBase64(ImageView imageView) {
        Drawable drawable = imageView.getDrawable();
        if (drawable == null || !(drawable instanceof BitmapDrawable)) {
            return null;
        }

        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT);
    }



    public void displayUserData(User user) {
        this.currentUser = user;
        tvEmail.setText("אימייל: " + user.getUserEmail());
        tvPhoneNumber.setText("מספר טלפון: " + user.getPhone());
        tvBirthDate.setText("תאריך לידה: " + user.getUserBirthDate());
        tvUsername.setText("שם משתמש: " + user.getUserName());
        tvBadPoints.setText("נקודות לרעה: " + user.getBadPoints());
        tvSumNum.setText("מספר סיכומים שנכתבו: " + user.getSumCount());

        String imageProfileData = user.getImageProfile();
        if (imageProfileData != null && !imageProfileData.isEmpty()) {
            try {
                if (imageProfileData.startsWith("/9j/") || imageProfileData.startsWith("iVBOR")) {
                    Log.d("USER_PROFILE_TAG", "Decoding Base64 image");
                    byte[] decodedString = android.util.Base64.decode(imageProfileData, android.util.Base64.DEFAULT);
                    Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                        Log.d("USER_PROFILE_TAG", "Base64 image set successfully");
                    } else {
                        Log.d("USER_PROFILE_TAG", "Failed to decode bitmap from Base64");
                    }
                } else {
                    Uri imageUri = Uri.parse(imageProfileData);
                    try {
                        Log.d("USER_PROFILE_TAG", "Loading image from URI");
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
                        imageView.setImageBitmap(bitmap);
                        Log.d("USER_PROFILE_TAG", "URI image set successfully");
                    } catch (IOException e) {
                        Log.e("USER_PROFILE_TAG", "Error loading image from URI", e);
                    }
                }
            } catch (Exception e) {
            }
        } else {
            Log.d("USER_PROFILE_TAG", "No image profile data available");
        }
    }
    @Override
    public void onClick(View view) {
        if (view == btnShowSummaries) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, new SumByUserFragment())
                    .addToBackStack(null)
                    .commit();
        }else if (view == btnDeleteUser) {
            new AlertDialog.Builder(getContext())
                    .setTitle("מחיקת משתמש")
                    .setMessage("האם ברצונך למחוק את המשתמש? פעולה זו אינה הפיכה.")
                    .setPositiveButton("כן", (dialog, which) -> {
                        presenter.deleteUser();

                        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.apply();

                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).updateNavigationHeader();
                        }

                    })
                    .setNegativeButton("לא", null)
                    .setCancelable(false)
                    .show();
        }
        else if (view == btnLogOut) {
            new AlertDialog.Builder(getContext())
                    .setTitle("התנתקות")
                    .setMessage("האם ברצונך להתנתק?")
                    .setPositiveButton("כן", (dialog, which) -> {
                        presenter.logOut();

                        getActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.flFragment, new LoginFragment())
                                .commit();
                    })
                    .setNegativeButton("לא", null)
                    .setCancelable(false)
                    .show();
        } else if (view == btnEdit) {
            currentDialogBirthDateEditText = null;
            currentDialogImageView = null;
            createCustomDialog();
        } else if (view == etBirthDate) {
            currentDialogBirthDateEditText = null;
            openDialog();
        } else if (view == btnUploadPhoto) {
            showPictureDialog();
        } else if (view == btnSaveSummary) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, new SaveSummaryFragment())
                    .addToBackStack(null)
                    .commit();
        } else if (view == btnSendMessage) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, new ChooseMessageFragment())
                    .addToBackStack(null)
                    .commit();
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

        ImageView targetImageView = currentDialogImageView != null ? currentDialogImageView : imageView;

        if (requestCode == GALLERY) {
            if (data != null) {
                Uri contentURI = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), contentURI);
                    saveImage(bitmap);
                    if (targetImageView != null) {
                        targetImageView.setImageBitmap(bitmap);
                    }
                    Toast.makeText(getContext(), "התמונה נשמרה!", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "נכשל!", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == CAMERA) {
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            if (targetImageView != null) {
                targetImageView.setImageBitmap(thumbnail);
            }
            saveImage(thumbnail);
            Toast.makeText(getContext(), "התמונה נשמרה!", Toast.LENGTH_SHORT).show();
        }

        currentDialogImageView = null;

        imageView = null;

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

    public void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void onLogOutSuccess() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPrefs", getContext().MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateNavigationHeader();
        }

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, new LoginFragment())
                .commit();
    }

    public void onDeleteSuccess() {
        Toast.makeText(getContext(), "המשתמש נמחק בהצלחה", Toast.LENGTH_SHORT).show();

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, new RegistrationFragment())
                .commit();
    }

    public void openDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year1, month1, dayOfMonth) -> {
                    String date = String.format("%02d/%02d/%04d", dayOfMonth, (month1 + 1), year1);

                    if (currentDialogBirthDateEditText != null) {
                        currentDialogBirthDateEditText.setText(date);
                    } else if (etBirthDate != null) {
                        etBirthDate.setText(date);
                    }
                },
                year, month, day
        );

        datePickerDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (presenter != null) {
            presenter.loadUserData();
        }
    }
}
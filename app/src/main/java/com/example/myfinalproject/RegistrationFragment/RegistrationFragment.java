package com.example.myfinalproject.RegistrationFragment;

import com.example.myfinalproject.CallBacks.AddUserCallback;
import com.example.myfinalproject.ChooseClassFragment.ChooseClassFragment;
import com.example.myfinalproject.MainActivity;
import com.example.myfinalproject.R;
import com.example.myfinalproject.DataModels.User;
import com.example.myfinalproject.Utils.Validator;
import com.google.firebase.firestore.FirebaseFirestore;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
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
    private static final String TAG = "RegistrationFragment";
    private int GALLERY = 1, CAMERA = 2;

    private Button btnUploadPhoto, btnN;
    private EditText etEmail, etUser, etPassword, etPassword2, etPhone, etDialogBirthday;
    private Uri imageUri;
    private RegisterUserPresenter presenter;

    private ImageView imageViewProfile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_registration, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
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

        etDialogBirthday.setOnClickListener(v -> openDialog());

        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String emailInput = s.toString().trim();

                String emailError = Validator.isValidEmail(emailInput);
                if (!emailError.isEmpty()) {
                    etEmail.setError(emailError);
                } else {
                    etEmail.setError(null);
                    if (!emailInput.isEmpty()) {
                        checkIfEmailExists(emailInput);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        etUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String usernameInput = s.toString().trim();


                String usernameError = Validator.isValidUsername(usernameInput);
                if (!usernameError.isEmpty()) {
                    etUser.setError(usernameError);
                } else {
                    etUser.setError(null);
                    if (!usernameInput.isEmpty()) {
                        checkIfUsernameExists(usernameInput);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });



        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = s.toString().trim();
                String validationResult = Validator.isValidPassword(password);
                if (!validationResult.isEmpty()) {
                    etPassword.setError(validationResult);
                } else {
                    etPassword.setError(null);
                }


                String confirmPassword = etPassword2.getText().toString().trim();
                if (!confirmPassword.isEmpty() && !confirmPassword.equals(password)) {
                    etPassword2.setError("הסיסמאות לא תואמות");
                } else {
                    etPassword2.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        etPassword2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = etPassword.getText().toString().trim();
                String confirmPassword = s.toString().trim();

                if (!confirmPassword.equals(password)) {
                    etPassword2.setError("הסיסמאות לא תואמות");
                } else {
                    etPassword2.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        etPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String phone = s.toString().trim();
                String validationResult = Validator.isValidPhone(phone);
                if (!validationResult.isEmpty()) {
                    etPhone.setError(validationResult);
                } else {
                    etPhone.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


    }




    private void checkIfEmailExists(String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .whereEqualTo("userEmail", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            etEmail.setError("האימייל הזה כבר בשימוש");
                        } else {
                            etEmail.setError(null);
                        }
                    }
                });
    }

    private void checkIfUsernameExists(String username) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .whereEqualTo("userName", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            etUser.setError("השם הזה כבר בשימוש");
                        } else {
                            etUser.setError(null);
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (v == btnN) {
            String username = etUser.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String password2 = etPassword2.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String birthDate = etDialogBirthday.getText().toString();
            String base64Image = imageViewToBase64(imageViewProfile);


            if (!validateInputs(username, password, password2, email, phone, birthDate, base64Image)) {
                return;
            }


            createAndRegisterUser(username, password, email, phone, birthDate, base64Image);
        } else if (v == etDialogBirthday) {
            openDialog();
        } else if (v == btnUploadPhoto) {
            showPictureDialog();
        }
    }

    private boolean validateInputs(String username, String password, String password2,
                                   String email, String phone, String birthDate, String base64Image) {
        String validImage = Validator.isValidImageProfile(base64Image);
        if(!validImage.isEmpty()){
            Toast.makeText(getContext(), validImage, Toast.LENGTH_SHORT).show();
            return false;
        }

        String validUsername = Validator.isValidUsername(username);
        if(!validUsername.isEmpty()){
            Toast.makeText(getContext(), validUsername, Toast.LENGTH_SHORT).show();
            return false;
        }

        String validEmail = Validator.isValidEmail(email);
        if(!validEmail.isEmpty()){
            Toast.makeText(getContext(), validEmail, Toast.LENGTH_SHORT).show();
            return false;
        }

        String validPhone = Validator.isValidPhone(phone);
        if(!validPhone.isEmpty()){
            Toast.makeText(getContext(), validPhone, Toast.LENGTH_SHORT).show();
            return false;
        }

        String validBirthDate = Validator.isValidBirthDate(birthDate);
        if(!validBirthDate.isEmpty()){
            Toast.makeText(getContext(), validBirthDate, Toast.LENGTH_SHORT).show();
            return false;
        }

        String validPassword = Validator.isValidPassword(password);
        if (!validPassword.isEmpty()) {
            Toast.makeText(getContext(), validPassword, Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!(password.equals(password2))){
            Toast.makeText(getContext(), "הסיסמאות לא תואמות", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void createAndRegisterUser(String username, String password, String email,
                                       String phone, String birthDate, String base64Image) {
        User user = new User(username, password, email, phone);
        user.setImageProfile(base64Image);
        user.setUserBirthDate(birthDate);

        presenter.submitClicked(user, new AddUserCallback() {
            @Override
            public void onUserAdd(User addedUser) {
                Log.d(TAG, "User added successfully with ID: " + addedUser.getId());

                saveUserDataToPreferences(addedUser);

                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.flFragment, new ChooseClassFragment())
                            .commit();
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error registering user: " + error);
                Toast.makeText(getContext(), "אירעה שגיאה בהרשמה: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserDataToPreferences(User user) {
        if (getActivity() == null) return;

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", getActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("userId", user.getId());
        editor.putBoolean("isLoggedIn", true);
        editor.putString("userName", user.getUserName());
        editor.putString("userEmail", user.getUserEmail());
        editor.putString("userPassword", user.getUserPass());
        editor.putString("phone", user.getPhone());
        editor.putString("userBirthDate", user.getUserBirthDate());
        editor.putString("imageProfile", user.getImageProfile());
        editor.putInt("badPoints", user.getBadPoints());
        editor.putInt("sumCount", user.getSumCount());
        editor.apply();

        Log.d(TAG, "Saved user to preferences with ID: " + user.getId());

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateNavigationHeader();
        }
    }

    public void submitClicked(User user, AddUserCallback callback) {
        presenter.submitClicked(user, callback);
    }


    public void openDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year1, month1, dayOfMonth) -> {
                    String date = String.format("%02d/%02d/%04d", dayOfMonth, (month1 + 1), year1);
                    etDialogBirthday.setText(date);
                },
                year, month, day
        );

        datePickerDialog.show();
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

            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }

    private String imageViewToBase64(ImageView imageView) {
        try {
            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
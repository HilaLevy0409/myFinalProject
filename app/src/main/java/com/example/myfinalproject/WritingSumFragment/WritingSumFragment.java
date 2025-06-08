package com.example.myfinalproject.WritingSumFragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.myfinalproject.DataModels.Summary;
import com.example.myfinalproject.R;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class WritingSumFragment extends Fragment implements View.OnClickListener {

    private static final String IMAGE_DIRECTORY = "/demonuts";
    private int GALLERY = 1, CAMERA = 2;

    private Button btnUploadPhoto, btnSubmit, btnTips, btnWriteSummary, btnUploadSummary;
    private SummaryPresenter summaryPresenter;
    private EditText etSummaryTitle, etSummaryContent;
    private ImageView imageViewSummary;
    private MaterialCardView writeSummaryCard, uploadSummaryCard;
    private String selectedClass, selectedProfession;

    private Summary summary;
    private boolean isWriteMode = true;

    private boolean isEditMode = false;
    private String summaryId;
    private String existingImageData;

    public WritingSumFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            selectedClass = getArguments().getString("selected_class", "");
            selectedProfession = getArguments().getString("selected_profession", "");

            // בודק האם הפרגמנט פועל במצב עריכה או יצירה חדשה.
            isEditMode = getArguments().getBoolean("isEditMode", false);

            if (isEditMode) {
                // אם מדובר במצב עריכה, מקבלים את המזהה של הסיכום הקיים והתמונה
                summaryId = getArguments().getString("summaryId");
                existingImageData = getArguments().getString("image");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_writing_sum, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        summary = new Summary();

        btnSubmit = view.findViewById(R.id.btnSubmit);
        btnTips = view.findViewById(R.id.btnTips);
        btnUploadPhoto = view.findViewById(R.id.btnUploadPhoto);
        etSummaryTitle = view.findViewById(R.id.etSummaryTitle);
        etSummaryContent = view.findViewById(R.id.etSummaryContent);

        btnWriteSummary = view.findViewById(R.id.btnWriteSummary);
        btnUploadSummary = view.findViewById(R.id.btnUploadSummary);
        EditText etClass = view.findViewById(R.id.etClass);
        EditText etProfession = view.findViewById(R.id.etProfession);

        // קביעת כיתה ומקצוע אם נבחרו
        if (selectedClass != null && !selectedClass.isEmpty()) {
            etClass.setText(selectedClass);
        }
        if (selectedProfession != null && !selectedProfession.isEmpty()) {
            etProfession.setText(selectedProfession);
        }

        summaryPresenter = new SummaryPresenter(this);
        imageViewSummary = view.findViewById(R.id.imageViewSummary);
        writeSummaryCard = view.findViewById(R.id.writeSummaryCard);
        uploadSummaryCard = view.findViewById(R.id.uploadSummaryCard);

        btnSubmit.setOnClickListener(this);
        btnUploadPhoto.setOnClickListener(this);
        btnTips.setOnClickListener(this);
        btnWriteSummary.setOnClickListener(this);
        btnUploadSummary.setOnClickListener(this);

        if (isEditMode) {
            populateExistingData();
            btnSubmit.setText("עדכון הסיכום");
        } else {
            showWriteMode();
        }
    }

    //  טוענת נתונים קיימים לתוך ממשק המשתמש, אם הם הועברו כארגומנטים לפרגמנט
    private void populateExistingData() {
        if (getArguments() != null) {
            // מקבל את כותרת הסיכום שהועברה
            String title = getArguments().getString("summaryTitle", "");
            // מקבל את תוכן הסיכום (אם קיים)
            String content = getArguments().getString("summaryContent", "");
            // מקבל את נתוני התמונה, אם קיימים
            String imageData = getArguments().getString("image", "");

            // מציב את הכותרת בשדה המתאים
            etSummaryTitle.setText(title);

            // אם קיימת תמונה – מציג את המסך במצב של העלאת תמונה
            if (imageData != null && !imageData.isEmpty()) {
                showUploadMode(); // מצב תצוגה של תמונה
                displayExistingImage(imageData); // מציג את התמונה שכבר קיימת
            }
            // אחרת, אם יש טקסט – מציג את המסך במצב כתיבת סיכום
            else if (content != null && !content.isEmpty()) {
                showWriteMode(); // מצב תצוגה של כתיבת תוכן
                etSummaryContent.setText(content); // מציב את הטקסט בשדה התוכן
            } else {
                showWriteMode();
            }
        }
    }
    private void displayExistingImage(String imageData) {
        try {
            byte[] decodedString = Base64.decode(imageData, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            if (bitmap != null) {
                imageViewSummary.setImageBitmap(bitmap);
                existingImageData = imageData;
            }
        } catch (Exception e) {
            Log.e("WritingSumFragment", "Error decoding existing image", e);
        }
    }

    // הצגת מצב כתיבה
    private void showWriteMode() {
        isWriteMode = true;
        writeSummaryCard.setVisibility(View.VISIBLE);
        uploadSummaryCard.setVisibility(View.GONE);
        btnWriteSummary.setAlpha(1.0f);
        btnUploadSummary.setAlpha(0.6f);

        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) btnSubmit.getLayoutParams();
        layoutParams.topToBottom = R.id.writeSummaryCard;
        btnSubmit.setLayoutParams(layoutParams);
        btnSubmit.setVisibility(View.VISIBLE);
    }

    // הצגת מצב העלאת תמונה
    private void showUploadMode() {
        isWriteMode = false;
        writeSummaryCard.setVisibility(View.GONE);
        uploadSummaryCard.setVisibility(View.VISIBLE);
        btnWriteSummary.setAlpha(0.6f);
        btnUploadSummary.setAlpha(1.0f);

        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) btnSubmit.getLayoutParams();
        layoutParams.topToBottom = R.id.uploadSummaryCard;
        btnSubmit.setLayoutParams(layoutParams);
        btnSubmit.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnSubmit) {
            if (validateInputs()) {
                if (isEditMode) {
                    updateSummaryData();
                } else {
                    saveSummaryData();
                }
            }
        } else if (v.getId() == R.id.btnUploadPhoto) {
            showPictureDialog();
        } else if (v.getId() == R.id.btnTips) {
            createCustomDialog();
        } else if (v.getId() == R.id.btnWriteSummary) {
            showWriteMode();
        } else if (v.getId() == R.id.btnUploadSummary) {
            showUploadMode();
        }
    }

    private boolean validateInputs() {
        if (etSummaryTitle.getText().toString().trim().isEmpty()) {
            showToast("נא להזין נושא לסיכום");
            return false;
        }

        if (isWriteMode) {
            if (etSummaryContent.getText().toString().trim().isEmpty()) {
                showToast("נא להזין תוכן סיכום");
                return false;
            }
        } else {
            if (imageViewSummary.getDrawable() == null) {
                showToast("נא להעלות תמונה");
                return false;
            }
        }
        return true;
    }

    // שמירת נתוני הסיכום ושליחתם ל־Firebase דרך הפרזנטר
    private void saveSummaryData() {
        String summaryTitle = etSummaryTitle.getText().toString().trim();

        summary.setClassOption(selectedClass);
        summary.setProfession(selectedProfession);
        summary.setSummaryTitle(summaryTitle);

        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", requireActivity().MODE_PRIVATE);
        String currentUserId = prefs.getString("userId", null);
        String currentUsername = prefs.getString("username", "אנונימי");

        summary.setUserId(currentUserId);
        summary.setUserName(currentUsername);

        if (isWriteMode) {
            String summaryContent = etSummaryContent.getText().toString().trim();
            summary.setSummaryContent(summaryContent);
            summary.setImage(null);
        } else {
            if (imageViewSummary.getDrawable() != null) {
                String base64Image = bitmapToBase64(((BitmapDrawable) imageViewSummary.getDrawable()).getBitmap());
                summary.setImage(base64Image);
            }
            summary.setSummaryContent("");
        }

        summaryPresenter.submitSummaryClicked(summary);
    }

    private void updateSummaryData() {
        if (summaryId == null || summaryId.isEmpty()) {
            showToast("שגיאה: מזהה סיכום לא תקין");
            return;
        }

        String summaryTitle = etSummaryTitle.getText().toString().trim();

        // יצירת מפת נתונים לעדכון במסד הנתונים
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("summaryTitle", summaryTitle);
        updateData.put("classOption", selectedClass);
        updateData.put("profession", selectedProfession);
        updateData.put("isEdited", true);
        updateData.put("lastEditTime", FieldValue.serverTimestamp());

        if (isWriteMode) {
            String summaryContent = etSummaryContent.getText().toString().trim();
            updateData.put("summaryContent", summaryContent); // תוכן הסיכום
            updateData.put("image", null); // מחיקת התמונה בבחירת מצב כתיבה
        } else {
            // אם יש תמונה מוצגת בתצוגה – ממירים אותה ל־Base64 ושומרים
            if (imageViewSummary.getDrawable() != null) {
                String base64Image = bitmapToBase64(((BitmapDrawable) imageViewSummary.getDrawable()).getBitmap());
                updateData.put("image", base64Image);

                // אם לא נבחרה תמונה חדשה אבל קיימת תמונה ישנה – משמרים אותה
            } else if (existingImageData != null) {
                updateData.put("image", existingImageData);
            }
            updateData.put("summaryContent", ""); //מחיקת תוכן בבחירת מצב העלאה
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("summaries").document(summaryId)
                .update(updateData)
                .addOnSuccessListener(aVoid -> {
                    showToast("הסיכום עודכן בהצלחה!");

                    if (getActivity() != null) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("WritingSumFragment", "Error updating summary", e);
                    showToast("שגיאה בעדכון הסיכום");
                });
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showPictureDialog(){
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(getContext());
        pictureDialog.setTitle("בחירת מקור תמונה:");
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
                    imageViewSummary.setImageBitmap(bitmap);
                    existingImageData = null; // בבחירת תמונה חדשה, מחיקת הקיימת
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "נכשל!", Toast.LENGTH_SHORT).show();
                }
            }
        }
        if (requestCode == CAMERA) {
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            imageViewSummary.setImageBitmap(thumbnail);
            saveImage(thumbnail);
            existingImageData = null; // נקה נתוני תמונה קיימים בעת צילום תמונה חדשה
            Toast.makeText(getContext(), "התמונה נשמרה!", Toast.LENGTH_SHORT).show();
        }

        if (requestCode == GALLERY || requestCode == CAMERA) {
            showUploadMode();
        }
    }

    // שמירת תמונה בזיכרון המכשיר
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

    private void createCustomDialog() {
        Dialog dialog = new Dialog(getContext());
        dialog.setTitle("טיפים לכתיבת סיכום");
        dialog.setContentView(R.layout.tips);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }
}
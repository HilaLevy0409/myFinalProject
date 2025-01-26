package com.example.myfinalproject.WritingSumFragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.myfinalproject.Models.Summary;
import com.example.myfinalproject.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class WritingSumFragment extends Fragment implements View.OnClickListener {

    private static final String IMAGE_DIRECTORY = "/demonuts";
    private int GALLERY = 1, CAMERA = 2;
    final int REQUEST_CODE_GALLERY = 999;


    private Button btnUploadPhoto, btnSubmit;
    private SummaryPresenter summaryPresenter;
    private EditText etClass, etProfession, etSummaryTitle, etSummaryContent;
    private ImageView imageViewSummary;

    private Summary summary;

    public WritingSumFragment() {
        // Required empty public constructor
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
        btnUploadPhoto = view.findViewById(R.id.btnUploadPhoto);
        etClass = view.findViewById(R.id.etClass);
        etProfession = view.findViewById(R.id.etProfession);
        etSummaryTitle = view.findViewById(R.id.etSummaryTitle);
        etSummaryContent = view.findViewById(R.id.etSummaryContent);

        summaryPresenter = new SummaryPresenter(this);

        imageViewSummary = view.findViewById(R.id.imageViewSummary);

        btnSubmit.setOnClickListener(this);
        btnUploadPhoto.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnSubmit) {
            if (validateInputs()) {
                saveSummaryData();
            }
        } else if (v.getId() == R.id.btnUploadPhoto) {
            // Handle photo upload
            // TODO: Implement photo upload functionality
        }
        if (v == btnUploadPhoto) {
            showPictureDialog();
        }
    }

    private boolean validateInputs() {
        if (etClass.getText().toString().trim().isEmpty()) {
            showToast("נא להזין כיתה");
            return false;
        }
        if (etProfession.getText().toString().trim().isEmpty()) {
            showToast("נא להזין מקצוע");
            return false;
        }
        if (etSummaryTitle.getText().toString().trim().isEmpty()) {
            showToast("נא להזין כותרת לסיכום");
            return false;
        }
        if (etSummaryContent.getText().toString().trim().isEmpty()) {
            showToast("נא להזין תוכן סיכום");
            return false;
        }
        return true;
    }

    private void saveSummaryData() {
        String classOption = etClass.getText().toString().trim();
        String profession = etProfession.getText().toString().trim();
        String summaryTitle = etSummaryTitle.getText().toString().trim();
        String summaryContent = etSummaryContent.getText().toString().trim();


        summary.setClassOption(classOption);
        summary.setProfession(profession);
        summary.setSummaryTitle(summaryTitle);
        summary.setSummaryContent(summaryContent);
        summaryPresenter.submitSummaryClicked(summary);
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

        Bitmap bitmap = null;
        if (requestCode == GALLERY && data != null) {
            try {
                Uri contentURI = data.getData();
                bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), contentURI);
            } catch (IOException e) {
                e.printStackTrace();
                showToast("Failed to load image");
                return;
            }
        } else if (requestCode == CAMERA) {
            bitmap = (Bitmap) data.getExtras().get("data");
        }

        if (bitmap != null) {
            imageViewSummary.setImageBitmap(bitmap);
            String base64Image = bitmapToBase64(bitmap);
            summary.setImage(base64Image);
        }
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
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
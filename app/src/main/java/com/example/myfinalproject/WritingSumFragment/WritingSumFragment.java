package com.example.myfinalproject.WritingSumFragment;

import android.app.Activity;
import android.app.Dialog;
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

import com.example.myfinalproject.Database.SummaryDatabase;
import com.example.myfinalproject.Models.Summary;
import com.example.myfinalproject.R;
import com.example.myfinalproject.Utils.Validator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class WritingSumFragment extends Fragment implements View.OnClickListener {

    private static final String IMAGE_DIRECTORY = "/demonuts";
    private int GALLERY = 1, CAMERA = 2;
    final int REQUEST_CODE_GALLERY = 999;


    private Button btnUploadPhoto, btnSubmit, btnTips;
    private SummaryPresenter summaryPresenter;
    private EditText etClass, etProfession, etSummaryTitle, etSummaryContent;
    private ImageView imageViewSummary;

    private Summary summary;


//    private static final int REQUEST_CAMERA_PERMISSION = 100;
//    private static final int REQUEST_STORAGE_PERMISSION = 101;
//    private static final int REQUEST_IMAGE_CAPTURE = 1;
//    private static final int REQUEST_IMAGE_GALLERY = 2;
//    private static final String AUTHORITY = "com.example.firestorepicapplication.fileprovider";
//
//
//    Summary summary;
//
//    private Uri filePath;
//    private final int PICK_IMAGE_REQUEST = 71;
//    SummaryDatabase summaryDatabase;
//    SummaryReference SummaryReference;
//
//
//    private ListView listViewSummaries;
//    private SummaryAdapter summaryAdapter;
//    private List<Summary> summaryList;


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
        btnTips = view.findViewById(R.id.btnTips);
        btnUploadPhoto = view.findViewById(R.id.btnUploadPhoto);
        etClass = view.findViewById(R.id.etClass);
        etProfession = view.findViewById(R.id.etProfession);
        etSummaryTitle = view.findViewById(R.id.etSummaryTitle);
        etSummaryContent = view.findViewById(R.id.etSummaryContent);


        summaryPresenter = new SummaryPresenter(this);

        imageViewSummary = view.findViewById(R.id.imageViewSummary);

        btnSubmit.setOnClickListener(this);
        btnUploadPhoto.setOnClickListener(this);
        btnTips.setOnClickListener(this);
    }
    public void onSummaryDeleted() {

    }
    public void onError(String message) {

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnSubmit) {
            if (validateInputs()) {
                saveSummaryData();
            }
        } else if (v.getId() == R.id.btnUploadPhoto) {
            // Handle photo upload (show image source dialog)
            showPictureDialog();
        } else if (v.getId() == R.id.btnTips) {
            // Show tips dialog
            createCustomDialog();
        }




//        listViewSummaries = findViewById(R.id.listViewSummaries);
//        summaryList = new ArrayList<>();
//        summaryAdapter = new SummaryAdapter(this, summaryList);
//        listViewSummaries.setAdapter(summaryAdapter);
//        loadSummaries();
//
//
//
//
//        listViewProducts.setOnItemClickListener((parent, view, position, id) -> {
//            Product selectedSummary = summaryList.get(position);
//            showManagementDialog(selectedSummary);
//        });

    }
//    private void showManagementDialog(Summary summary) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        builder.setTitle("ניהול סיכום");
//        String[] options = {"עריכה", "מחיקה", "ביטול"};
//
//
//        builder.setItems(options, (dialog, which) -> {
//            switch (which) {
//                case 0: // Edit
//                    showEditDialog(summary);
//                    break;
//                case 1: // Delete
//                    showDeleteConfirmationDialog(summary);
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

//    private void showEditDialog(Summary summary) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_summary, null);
//
//
//        EditText etName = dialogView.findViewById(R.id.etEditName);
//        ImageView imgSummary = dialogView.findViewById(R.id.imgEditSummary);
//        Button btnChangeImage = dialogView.findViewById(R.id.btnChangeImage);
//
//
//        etName.setText(summary.getSummaryName());
//        loadBase64Image(summary.getSummaryPic(), imgSummary);
//
//
//        btnChangeImage.setOnClickListener(v -> {
//            // Store the current ImageView reference for later use
//            imageView = imgSummary;
//            showPictureDialog();
//        });
//
//
//        builder.setView(dialogView)
//                .setPositiveButton("שמור", (dialog, which) -> {
//                    summary.setSummaryName(etName.getText().toString());
//                    if (imgProduct.getDrawable() != null) {
//                        product.setProdPic(imageViewToBase64(imgSummary));
//                    }
//                        summaryPresenter.updateSummary(summary);
//                })
//                .setNegativeButton("ביטול", (dialog, which) -> dialog.cancel());
//
//
//        builder.show();
//    }

//    private void showDeleteConfirmationDialog(Summary summary) {
//        new AlertDialog.Builder(getContext())
//                .setTitle("מחיקת ")
//                .setMessage("האם ברצונך למחוק ?")
//                .setPositiveButton("כן", (dialog, which) ->
//                       summaryPresenter.deleteSummary(summary.getId()))
//                .setNegativeButton("לא", null)
//                .show();
//    }
//
//
//    public void onProductUpdated(Summary summary) {
//        Toast.makeText(getContext(), "עודכן בהצלחה", Toast.LENGTH_SHORT).show();
//        loadSummaries();
//    }
//
//
//    public void onSummaryDeleted() {
//        Toast.makeText(getContext(), "נמחק בהצלחה", Toast.LENGTH_SHORT).show();
//        loadSummaries();
//    }
//
//
//    public void onError(String message) {
//        Toast.makeText(getContext(), "שגיאה: " + message, Toast.LENGTH_SHORT).show();
//    }
//
//
//
//    private void loadProducts() {
//        summaryPresenter.loadSummaries(new SummaryDatabase().SummariesCallback() {
//            @Override
//            public void onSuccess(List<Summary> summaries) {
//                summaryList.clear();
//               summaryList.addAll(summaries);
//                summaryAdapter.notifyDataSetChanged();
//            }
//
//
//            @Override
//            public void onError(String message) {
//                Toast.makeText(WritingSumFragment.this, "Error loading products: " + message,
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
//            Uri photoURI = FileProvider.getUriForFile(this, "com.example.firestorepicapplication.fileprovider", photoFile);
//            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
//            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//        }
//    }
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
//    private void requestCameraPermission() {
//        if (WritingSumFragment.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {WritingSumFragment.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
//        }
//    }
//
//
//    public void onSuccess(Summary summary) {
//        Toast.makeText(this, summary.getProdName() + " added", Toast.LENGTH_SHORT).show();
//    }





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



//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == Activity.RESULT_CANCELED) {
//            return;
//        }
//        if (requestCode == GALLERY) {
//            if (data != null) {
//                Uri contentURI = data.getData();
//                try {
//                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
//                    String path = saveImage(bitmap);
//                    Toast.makeText(getContext(), "Image Saved!", Toast.LENGTH_SHORT).show();
//                    imageView.setImageBitmap(bitmap);
//
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    Toast.makeText(getContext(), "Failed!", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//
//        }
//        if (requestCode == CAMERA) {
//            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
//            imageView.setImageBitmap(thumbnail);
//            saveImage(thumbnail);
//            Toast.makeText(getContext(), "Image Saved!", Toast.LENGTH_SHORT).show();
//
//
//        }
//    }



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

    private void chooseImage() {


    }

//    @Override
//    public void onClick(View v) {
//        if (v == btnSubmit) {
//            EditText etSummary = findViewById(R.id.etSummary);
//
//
//            // Convert image to Base64 string instead of byte array
//            String base64Image = imageViewToBase64(imageView);
//
//
//            Summary prod = new Summary(
//                    "",
//                    etSummary.getText().toString(),
//                    Integer.parseInt(etAmountProd.getText().toString()),
//                    base64Image
//            );
//            SummaryPresenter.submitClicked(summary);
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
//    // Add method to load Base64 image into ImageView
//    private void loadBase64Image(String base64Image, ImageView imageView) {
//        try {
//            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
//            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//            imageView.setImageBitmap(decodedByte);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//

    private void createCustomDialog() {
        Dialog dialog  = new Dialog(getContext());
        dialog.setTitle("טיפים לכתיבת סיכום");
        dialog.setContentView(R.layout.fragment_tips_sum);
        dialog.show();


    }


}
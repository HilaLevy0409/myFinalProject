package com.example.myfinalproject.ChooseSumFragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.myfinalproject.Adapters.SummaryAdapter;
import com.example.myfinalproject.CallBacks.SummariesCallback;
import com.example.myfinalproject.Models.Summary;
import com.example.myfinalproject.R;
import com.example.myfinalproject.WritingSumFragment.WritingSumFragment;

import com.example.myfinalproject.SumFragment.SumFragment;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ChooseSumFragment extends Fragment implements View.OnClickListener {
    private static final String IMAGE_DIRECTORY = "/demonuts";
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 101;
    private static final int GALLERY = 1;
    private static final int CAMERA = 2;

    private Button btnAdd;
    private ImageView imageSum;
    private ListView listViewSummaries;
    private SummaryAdapter summaryAdapter;
    private ArrayList<Summary> summaryList;
    private ChooseSumPresenter chooseSumPresenter;
    private SearchView searchView;
    private String selectedClass, selectedProfession;
    private ArrayList<Summary> fullSummaryList = new ArrayList<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        summaryList = new ArrayList<>();
        chooseSumPresenter = new ChooseSumPresenter(this);
        requestPermissions();
        if (getArguments() != null) {
            selectedClass = getArguments().getString("selected_class", "");
            selectedProfession = getArguments().getString("selected_profession", "");
            Log.d("ChooseSumFragment", "Received: class=" + selectedClass + ", profession=" + selectedProfession);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_sum, container, false);
        loadSummaries();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btnAdd = view.findViewById(R.id.btnAdd);
        listViewSummaries = view.findViewById(R.id.listViewSummaries);
        searchView = view.findViewById(R.id.searchView);
        imageSum = new ImageView(getContext());

        summaryAdapter = new SummaryAdapter(getContext(), summaryList);
        listViewSummaries.setAdapter(summaryAdapter);

        btnAdd.setOnClickListener(this);

        listViewSummaries.setOnItemClickListener((parent, view1, position, id) -> {
            Summary selectedSummary = summaryList.get(position);
            showManagementDialog(selectedSummary);
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterSummariesByTitle(newText);
                return true;
            }
        });

    }

    private void requestPermissions() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        }
    }



    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnAdd) {
            navigateToWritingSumFragment();
        }
    }

    private void navigateToWritingSumFragment() {
        Bundle args = new Bundle();
        args.putString("selected_class", selectedClass);
        args.putString("selected_profession", selectedProfession);

        WritingSumFragment writingSumFragment = new WritingSumFragment();
        writingSumFragment.setArguments(args);

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, writingSumFragment)
                .addToBackStack(null)
                .commit();
    }


    private void loadSummaries() {
        chooseSumPresenter.loadSummaries(new SummariesCallback() {
            @Override
            public void onSuccess(List<Summary> summaries) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    summaryList.clear();
                    fullSummaryList.clear();
                    fullSummaryList.addAll(summaries); // ← שמירת המקור
                    summaryList.addAll(summaries);
                    summaryAdapter.notifyDataSetChanged();
                });
            }


            @Override
            public void onError(String message) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error loading summaries: " + message,
                                Toast.LENGTH_SHORT).show()
                );
            }
        }, selectedClass, selectedProfession);
    }

    private void showManagementDialog(Summary summary) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("ניהול סיכום");
        String[] options = {"עריכה", "מעבר לסיכום", "מחיקה", "ביטול"};

        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    showEditDialog(summary);
                    break;
                case 1:
                    showSum(summary);
                    break;
                case 2:
                    showDeleteConfirmationDialog(summary);
                    break;
                case 3:
                    dialog.dismiss();
                    break;
            }
        });

        builder.show();
    }


    private void showSum(Summary summary) {
        if (getContext() == null) return;

        // Create a new instance of SumFragment
        SumFragment sumFragment = new SumFragment();

        // Create a Bundle to store the summary ID
        Bundle args = new Bundle();
        args.putString("summaryId", summary.getSummaryId());

        // Set the arguments to the fragment
        sumFragment.setArguments(args);

        // Replace the current fragment with the new SumFragment
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, sumFragment)
                .commit();
    }

    private void showEditDialog(Summary summary) {
        if (getContext() == null) return;
    /*
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_summary, null);

        EditText etClass = dialogView.findViewById(R.id.etClass);
        EditText etProfession = dialogView.findViewById(R.id.etProfession);
        EditText etSummaryTitle = dialogView.findViewById(R.id.etSummaryTitle);
        ImageView imgSum = dialogView.findViewById(R.id.imageSum);
        Button btnChangeImage = dialogView.findViewById(R.id.btnChangeImage);

        etClass.setText(summary.getClassOption());
        etProfession.setText(summary.getProfession());
        etSummaryTitle.setText(summary.getSummaryTitle());
        loadBase64Image(summary.getImage(), imgSum);

        btnChangeImage.setOnClickListener(v -> {
            imageSum = imgSum;
            showPictureDialog();
        });

        builder.setView(dialogView)
                .setPositiveButton("שמור", (dialog, which) -> {
                    summary.setClassOption(etClass.getText().toString());
                    summary.setProfession(etProfession.getText().toString());
                    summary.setSummaryTitle(etSummaryTitle.getText().toString());
                    if (imgSum.getDrawable() != null) {
                        summary.setImage(imageViewToBase64(imgSum));
                    }
                    chooseSumPresenter.updateSummary(summary);
                })
                .setNegativeButton("ביטול", (dialog, which) -> dialog.cancel());

        builder.show();

     */
    }

    private void showDeleteConfirmationDialog(Summary summary) {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("מחיקת סיכום")
                .setMessage("האם שברצונך למחוק סיכום זה?")
                .setPositiveButton("כן", (dialog, which) ->
                        chooseSumPresenter.deleteSummary(summary.getSummaryId()))
                .setNegativeButton("לא", null)
                .show();
    }



    private void showPictureDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(getContext());
        pictureDialog.setTitle("בחר מקור תמונה");
        String[] pictureDialogItems = {"גלריה", "מצלמה"};
        pictureDialog.setItems(pictureDialogItems,
                (dialog, which) -> {
                    switch (which) {
                        case 0:
                            choosePhotoFromGallary();
                            break;
                        case 1:
                            takePhotoFromCamera();
                            break;
                    }
                });
        pictureDialog.show();
    }

    private void choosePhotoFromGallary() {
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

        if (data == null || getContext() == null) return;

        try {
            if (requestCode == GALLERY) {
                handleGalleryResult(data);
            } else if (requestCode == CAMERA) {
                handleCameraResult(data);
            }
        } catch (Exception e) {
            Log.e("ChooseSumFragment", "Error processing image: " + e.getMessage());
            Toast.makeText(getContext(), "Error processing image", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleGalleryResult(Intent data) throws IOException {
        Uri contentURI = data.getData();
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), contentURI);
        String path = saveImage(bitmap);
        imageSum.setImageBitmap(bitmap);
        Toast.makeText(getContext(), "התמונה נשמרה!", Toast.LENGTH_SHORT).show();
    }

    private void handleCameraResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        imageSum.setImageBitmap(thumbnail);
        saveImage(thumbnail);
        Toast.makeText(getContext(), "התמונה נשמרה!", Toast.LENGTH_SHORT).show();
    }

    private String saveImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        File wallpaperDirectory = new File(
                Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs();
        }

        try {
            File f = new File(wallpaperDirectory,
                    MessageFormat.format("{0}.jpg", Calendar.getInstance().getTimeInMillis()));
            f.createNewFile();

            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            fo.close();

            MediaScannerConnection.scanFile(getContext(),
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);

            return f.getAbsolutePath();
        } catch (IOException e) {
            Log.e("ChooseSumFragment", "Error saving image: " + e.getMessage());
            return "";
        }
    }

    private String imageViewToBase64(ImageView image) {
        try {
            Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
            byte[] byteArray = stream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e("ChooseSumFragment", "Error converting image to base64: " + e.getMessage());
            return null;
        }
    }

    private void loadBase64Image(String base64Image, ImageView imageView) {
        try {
            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            imageView.setImageBitmap(decodedByte);
        } catch (Exception e) {
            Log.e("ChooseSumFragment", "Error loading base64 image: " + e.getMessage());
        }
    }

    public void onSummaryUpdated(Summary summary) {
        if (getContext() == null) return;
        Toast.makeText(getContext(), "הסיכום עודכן בהצלחה", Toast.LENGTH_SHORT).show();
        loadSummaries();
    }

    public void onSummaryDeleted() {
        if (getContext() == null) return;
        Toast.makeText(getContext(), "הסיכום נמחק בהצלחה", Toast.LENGTH_SHORT).show();
        loadSummaries();
    }

    public void onError(String message) {
        if (getContext() == null) return;
        Toast.makeText(getContext(), "שגיאה: " + message, Toast.LENGTH_SHORT).show();
    }

    private void filterSummariesByTitle(String query) {
        summaryList.clear();
        if (query.isEmpty()) {
            summaryList.addAll(fullSummaryList);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (Summary summary : fullSummaryList) {
                if (summary.getSummaryTitle().toLowerCase().contains(lowerCaseQuery)) {
                    summaryList.add(summary);
                }
            }
        }
        summaryAdapter.notifyDataSetChanged();
    }

}
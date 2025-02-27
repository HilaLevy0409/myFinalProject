package com.example.myfinalproject.ChooseUserFragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.myfinalproject.AdminLoginFragment.AdminLoginFragment;
import com.example.myfinalproject.CallBacks.OnUserClickListener;
import com.example.myfinalproject.CallBacks.UsersCallback;

import com.example.myfinalproject.Models.User;
import com.example.myfinalproject.R;
import com.example.myfinalproject.ReportFragment.ReportFragment;
import com.example.myfinalproject.UserProfileFragment.UserProfilePresenter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;


public class ChooseUserFragment extends Fragment {

    private static final String IMAGE_DIRECTORY = "/demonuts";
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 101;
    private static final int GALLERY = 1;
    private static final int CAMERA = 2;

    private ImageView imgUserProfile;
    private ListView listViewUsers;
    private UserAdapter userAdapter;
    private ArrayList<User> userList;
    private ChooseUserPresenter presenter;
    private SearchView searchView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userList = new ArrayList<>();
        presenter = new ChooseUserPresenter(this);
        requestPermissions();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_user, container, false);
        initializeViews(view);
        setupListeners();
        loadUsers();
        return view;
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

    private void initializeViews(View view) {

        listViewUsers = view.findViewById(R.id.listViewUsers);
        searchView = view.findViewById(R.id.searchView);
        imgUserProfile = new ImageView(getContext());

        userAdapter = new UserAdapter(getContext(), userList, new OnUserClickListener() {
            @Override
            public void onUserClick(int position) {
                Bundle bundle = new Bundle();
                bundle.putString("userName", userList.get(position).getUserName());

                ReportFragment reportFragment = new ReportFragment();
                reportFragment.setArguments(bundle);
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flFragment, reportFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
        listViewUsers.setAdapter(userAdapter);
    }

    private void setupListeners() {

        listViewUsers.setOnItemClickListener((parent, view, position, id) -> {
            User selectedUser = userList.get(position);
            showManagementDialog(selectedUser);
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//                userProfilePresenter.filterUsers(newText, userList, userAdapter);
                return true;
            }
        });
    }




    private void loadUsers() {
        presenter.loadUsers(new UsersCallback() {
            @Override
            public void onSuccess(List<User> users) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                   userList.clear();
                    userList.addAll(users);
                    userAdapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onError(String message) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error loading users: " + message,
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void showManagementDialog(User user) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("ניהול משתמש");
        String[] options = {"עריכה", "מחיקה", "ביטול"};

        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    showEditDialog(user);
                    break;
                case 1:
                    showDeleteConfirmationDialog(user);
                    break;
                case 2:
                    dialog.dismiss();
                    break;
            }
        });
        builder.show();
    }

    private void showEditDialog(User user) {
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

    private void showDeleteConfirmationDialog(User user) {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("מחיקת משתמש")
                .setMessage("האם ברצונך למחוק משתמש זה?")
//                .setPositiveButton("כן", (dialog, which) ->
//                        userProfilePresenter.deleteUser(user.getId()))
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
            Log.e("ChooseUserFragment", "Error processing image: " + e.getMessage());
            Toast.makeText(getContext(), "Error processing image", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleGalleryResult(Intent data) throws IOException {
        Uri contentURI = data.getData();
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), contentURI);
        String path = saveImage(bitmap);
        imgUserProfile.setImageBitmap(bitmap);
        Toast.makeText(getContext(), "התמונה נשמרה!", Toast.LENGTH_SHORT).show();
    }

    private void handleCameraResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        imgUserProfile.setImageBitmap(thumbnail);
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
            Log.e("ChooseUserFragment", "Error saving image: " + e.getMessage());
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
            Log.e("ChooseUserFragment", "Error converting image to base64: " + e.getMessage());
            return null;
        }
    }

    private void loadBase64Image(String base64Image, ImageView imageView) {
        try {
            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            imageView.setImageBitmap(decodedByte);
        } catch (Exception e) {
            Log.e("ChooseUserFragment", "Error loading base64 image: " + e.getMessage());
        }
    }

    // Callback methods for the presenter
    public void onUserUpdated(User user) {
        if (getContext() == null) return;
        Toast.makeText(getContext(), "המשתמש עודכן בהצלחה", Toast.LENGTH_SHORT).show();
        loadUsers();
    }

    public void onUsersDeleted() {
        if (getContext() == null) return;
        Toast.makeText(getContext(), "המשתמש נמחק בהצלחה", Toast.LENGTH_SHORT).show();
        loadUsers();
    }

    public void onError(String message) {
        if (getContext() == null) return;
        Toast.makeText(getContext(), "שגיאה: " + message, Toast.LENGTH_SHORT).show();
    }




    public ChooseUserFragment() {
        // Required empty public constructor
    }


    public static ChooseUserFragment newInstance(String param1, String param2) {
        ChooseUserFragment fragment = new ChooseUserFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }


}



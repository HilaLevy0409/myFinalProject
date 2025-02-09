package com.example.myfinalproject.ChooseSumFragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.myfinalproject.Models.Summary;
import com.example.myfinalproject.R;
import com.example.myfinalproject.WritingSumFragment.WritingSumFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChooseSumFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChooseSumFragment extends Fragment implements View.OnClickListener{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Button btnAdd;

    private static final String IMAGE_DIRECTORY = "/demonuts";
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 101;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;
    private static final String AUTHORITY = "com.example.firestorepicapplication.fileprovider";
    private int GALLERY = 1, CAMERA = 2;


    ProductPresenter productPresenter;
    Button btnPress;
    private Button btnChoose,btnUpload;
    private ImageView imageView;
    private ImageView imgFirebase;
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 71;
    FirebaseStorage storage;
    StorageReference storageReference;


    private ListView listViewSummaries;
    private SumAdapter SumAdapter;
    private List<Summary> sumList;

    public ChooseSumFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChooseSumFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChooseSumFragment newInstance(String param1, String param2) {
        ChooseSumFragment fragment = new ChooseSumFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


        setContentView(R.layout.activity_main);
        productPresenter=new ProductPresenter(this);
        btnPress=findViewById(R.id.btnPress);
        btnPress.setOnClickListener(this);
        btnChoose = (Button) findViewById(R.id.btnChoose);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        imageView = (ImageView) findViewById(R.id.imgView);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        requestCameraPermission();
        requestStoragePermission();
        imgFirebase = (ImageView) findViewById(R.id.imgFirebase);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPictureDialog();
            }
        });


        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //uploadImage();
            }
        });


        listViewProducts = findViewById(R.id.listViewProducts);
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(this, productList);
        listViewProducts.setAdapter(productAdapter);
        loadProducts();




        listViewProducts.setOnItemClickListener((parent, view, position, id) -> {
            Product selectedProduct = productList.get(position);
            showManagementDialog(selectedProduct);
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choose_sum, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btnAdd = view.findViewById(R.id. btnAdd);
        btnAdd.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view == btnAdd) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, new WritingSumFragment())
                    .commit();
        }
        if (v == btnPress) {
            EditText etProd = findViewById(R.id.etProd);
            EditText etAmountProd = findViewById(R.id.etAmountProd);


            // Convert image to Base64 string instead of byte array
            String base64Image = imageViewToBase64(imageView);


            Product prod = new Product(
                    "",
                    etProd.getText().toString(),
                    Integer.parseInt(etAmountProd.getText().toString()),
                    base64Image
            );
            productPresenter.submitClicked(prod);
        }

    }



    private void showEditDialog(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_product, null);


        EditText etName = dialogView.findViewById(R.id.etEditName);
        EditText etAmount = dialogView.findViewById(R.id.etEditAmount);
        ImageView imgProduct = dialogView.findViewById(R.id.imgEditProduct);
        Button btnChangeImage = dialogView.findViewById(R.id.btnChangeImage);


        etName.setText(product.getProdName());
        etAmount.setText(String.valueOf(product.getProdAmount()));
        loadBase64Image(product.getProdPic(), imgProduct);


        btnChangeImage.setOnClickListener(v -> {
            // Store the current ImageView reference for later use
            imageView = imgProduct;
            showPictureDialog();
        });


        builder.setView(dialogView)
                .setPositiveButton("שמור", (dialog, which) -> {
                    product.setProdName(etName.getText().toString());
                    product.setProdAmount(Integer.parseInt(etAmount.getText().toString()));
                    if (imgProduct.getDrawable() != null) {
                        product.setProdPic(imageViewToBase64(imgProduct));
                    }
                    productPresenter.updateProduct(product);
                })
                .setNegativeButton("ביטול", (dialog, which) -> dialog.cancel());


        builder.show();
    }


    private void showDeleteConfirmationDialog(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("מחיקת מוצר")
                .setMessage("האם אתה בטוח שברצונך למחוק מוצר זה?")
                .setPositiveButton("כן", (dialog, which) ->
                        productPresenter.deleteProduct(product.getId()))
                .setNegativeButton("לא", null)
                .show();
    }


    public void onProductUpdated(Product product) {
        Toast.makeText(this, "המוצר עודכן בהצלחה", Toast.LENGTH_SHORT).show();
        loadProducts();
    }


    public void onProductDeleted() {
        Toast.makeText(this, "המוצר נמחק בהצלחה", Toast.LENGTH_SHORT).show();
        loadProducts();
    }


    public void onError(String message) {
        Toast.makeText(this, "שגיאה: " + message, Toast.LENGTH_SHORT).show();
    }




    private void showManagementDialog(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ניהול מוצר");
        String[] options = {"עריכה", "מחיקה", "ביטול"};


        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Edit
                    showEditDialog(product);
                    break;
                case 1: // Delete
                    showDeleteConfirmationDialog(product);
                    break;
                case 2: // Cancel
                    dialog.dismiss();
                    break;
            }
        });


        builder.show();
    }




    private void loadProducts() {
        productPresenter.loadProducts(new ProductRepository.ProductsCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                productList.clear();
                productList.addAll(products);
                productAdapter.notifyDataSetChanged();
            }


            @Override
            public void onError(String message) {
                Toast.makeText(ProductActivity.this, "Error loading products: " + message,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void requestStoragePermission() {
    }
    private void launchCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            Uri photoURI = FileProvider.getUriForFile(this, "com.example.firestorepicapplication.fileprovider", photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = "JPEG_" + System.currentTimeMillis() + ".jpg";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        return image;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with camera launch
                launchCamera();
            } else {
                // Permission denied, handle the error
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestCameraPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }


    public void onSuccess(Product product) {
        Toast.makeText(this, product.getProdName() + " added", Toast.LENGTH_SHORT).show();
    }


    private void showPictureDialog(){
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("נא לבחור מאיפה להוסיף תמונה:");
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
        if (resultCode == this.RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                Uri contentURI = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                    String path = saveImage(bitmap);
                    Toast.makeText(this, "Image Saved!", Toast.LENGTH_SHORT).show();
                    imageView.setImageBitmap(bitmap);


                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }


        }
        if (requestCode == CAMERA) {
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(thumbnail);
            saveImage(thumbnail);
            Toast.makeText(this, "Image Saved!", Toast.LENGTH_SHORT).show();


        }
    }


    public String saveImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File wallpaperDirectory = new File(
                Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        // have the object build the directory structure, if needed.
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs();
        }


        try {
            File f = new File(wallpaperDirectory, MessageFormat.format("{0}.jpg", Calendar.getInstance().getTimeInMillis()));
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this,
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


    //Convert imageView to byte[]
    private byte[] imageViewToByte(ImageView image) {
        Bitmap bitmap=((BitmapDrawable)image.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 0, stream);
        byte[]byteArray=stream.toByteArray();
        return byteArray;
    }
    private void chooseImage() {


    }



    private String imageViewToBase64(ImageView image) {
        try {
            Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream); // Use 70% quality for better storage
            byte[] byteArray = stream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    // Add method to load Base64 image into ImageView
    private void loadBase64Image(String base64Image, ImageView imageView) {
        try {
            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            imageView.setImageBitmap(decodedByte);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
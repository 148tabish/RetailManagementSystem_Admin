package com.retailmanagement.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.retailmanagement.admin.adapters.CustomSpinnerAdapter;
import com.retailmanagement.admin.helper.ImageHelper;
import com.retailmanagement.admin.webservices.JSONParse;
import com.retailmanagement.admin.webservices.RestAPI;
import com.retailmanagement.admin.webservices.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AddProducts extends AppCompatActivity {
    public static final int PERMISSION_REQUEST = 101;
    public static final int GALLERY_IMAGE_REQUEST = 102;
    private static final String TAG = "AddProducts";
    private static final int REQUEST_IMAGE = 103;

    private EditText mProductName, mProductPrice, mProductQty, mProductDesc;
    private ImageView mProductImage;
    private Spinner mCategory;
    private MaterialButton mProductAdd;
    private Bitmap mImageBitmap;
    private String mImageString;
    private File destination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_products);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add Products");

        mProductAdd = findViewById(R.id.productAdd);

        mProductName = findViewById(R.id.productName);
        mProductPrice = findViewById(R.id.productPrice);
        mProductQty = findViewById(R.id.productQty);
        mProductDesc = findViewById(R.id.productDesc);
        mProductImage = findViewById(R.id.productImage);

        mCategory = findViewById(R.id.product_category);

        final List<Pair<String, String>> mData = getItemData();
        final CustomSpinnerAdapter customSpinnerAdapter = new CustomSpinnerAdapter(AddProducts.this
                , R.layout.spinner_drop_down, mData);
        customSpinnerAdapter.setDropDownViewResource(R.layout.spinner_drop_down);
        mCategory.setAdapter(customSpinnerAdapter);

        mProductAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ifValid()) {
                    String uuid = UUID.randomUUID().toString();
                    //String barcode_id,String name,String image
                    // ,String price,String qty,String desc,String type
                    Pair<String, String> selectedItem = mData.get(mCategory.getSelectedItemPosition());
                    Log.d(TAG, String.format("Name : %s\nPrice : %s, \nQuantity : %s\nType : %s"
                            ,mProductName.getText().toString()
                            ,mProductPrice.getText().toString()
                            ,mProductQty.getText().toString()
                            ,selectedItem.first));

                    new AddProductsTask(AddProducts.this)
                            .execute(uuid, mProductName.getText().toString()
                                    ,mImageString,mProductPrice.getText().toString()
                                    ,mProductQty.getText().toString()
                                    , mProductDesc.getText().toString(), selectedItem.first);
                }
            }
        });

        mProductImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(weHavePermission()){
                    takePicture();
                }else {
                    requestContactsPermission();
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    private List<Pair<String, String>> getItemData() {
        ArrayList<Pair<String, String>> mItems = new ArrayList<Pair<String, String>>();
        String[] itemCategories = getResources().getStringArray(R.array.item_categories);
        String[] itemDescription = getResources().getStringArray(R.array.item_hint);
        for (int i = 0; i < itemCategories.length; i++) {
            mItems.add(new Pair<String, String>(itemCategories[i], itemDescription[i]));
        }
        return mItems;
    }

    private boolean ifValid() {
        boolean isValid = true;
        if (mProductName.getText().length() == 0) {
            isValid = false;
            Snackbar.make(mProductAdd, "Enter product name", Snackbar.LENGTH_SHORT)
                    .show();
            mProductName.requestFocus();
        } else if (mProductPrice.getText().length() == 0) {
            isValid = false;
            Snackbar.make(mProductAdd, "Enter product price", Snackbar.LENGTH_SHORT)
                    .show();
            mProductPrice.requestFocus();
        } else if (Integer.parseInt(mProductPrice.getText().toString()) == 0) {
            isValid = false;
            Snackbar.make(mProductAdd, "Enter valid product price", Snackbar.LENGTH_SHORT)
                    .show();
            mProductPrice.requestFocus();
        } else if (mProductQty.getText().length() == 0) {
            isValid = false;
            Snackbar.make(mProductAdd, "Enter product quantity", Snackbar.LENGTH_SHORT)
                    .show();
            mProductQty.requestFocus();
        } else if (Integer.parseInt(mProductQty.getText().toString()) == 0) {
            isValid = false;
            Snackbar.make(mProductAdd, "Enter valid product quantity", Snackbar.LENGTH_SHORT)
                    .show();
            mProductQty.requestFocus();
        } else if (mProductDesc.getText().length() == 0) {
            isValid = false;
            Snackbar.make(mProductAdd, "Enter product description", Snackbar.LENGTH_SHORT)
                    .show();
            mProductDesc.requestFocus();
        } else if (mImageString == null || mImageString.length() == 0) {
            isValid = false;
            Snackbar.make(mProductAdd, "Please add image of product", Snackbar.LENGTH_SHORT)
                    .show();
        }
        return isValid;
    }

    private boolean weHavePermission() {
        return (ActivityCompat.checkSelfPermission(AddProducts.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(AddProducts.this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        );
    }

    private void requestContactsPermission() {
        ActivityCompat.requestPermissions(AddProducts.this,
                new String[]{Manifest.permission.CAMERA
                        , Manifest.permission.WRITE_EXTERNAL_STORAGE
                        }
                , PERMISSION_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePicture();
            } else {
                requestContactsPermission();
            }
        }
    }

    private void takePicture() {
        File folder = new File(Environment.getExternalStorageDirectory() + "/RMS/");

        if (!folder.exists()) {
            if (folder.mkdir()) {
                Log.d(TAG, "takePicture: Directory Created");
            }
        }

        File nomediaFile = new File(folder.getAbsolutePath() + "/.nomedia");
        if (!nomediaFile.exists()) {
            try {
                if (nomediaFile.createNewFile())
                    Log.d(TAG, "takePicture: No Media File Created");
            } catch (IOException e) {
                Log.d(TAG, "takePicture: Error Creating File " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "takePicture: Media File Exists" + nomediaFile.getAbsolutePath());
        }

        // Generate the path for the next photo
        String name = dateToString(new Date(), "yyyy_MM_dd_hh_mm_ss");
        destination = new File(folder, name + ".png");

        Log.d(TAG, "takePicture: " + destination.getAbsolutePath());

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(AddProducts.this);
        builder.setCancelable(false)
                .setMessage(R.string.dialog_select_prompt)
                .setNeutralButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setPositiveButton("Camera", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startCamera();
                    }
                })
                .setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startGalleryChooser();
                    }
                });
        builder.create().show();
    }
    public File getCameraFile() {
        File dir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(dir, "temp.jpg");
    }

    private void startCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri photoUri = FileProvider.getUriForFile(AddProducts.this,
                AddProducts.this.getApplicationContext().getPackageName() + ".provider", getCameraFile());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    public void startGalleryChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select a photo"),
                GALLERY_IMAGE_REQUEST);
    }

    private void launchMediaScanIntent(@NonNull Uri imageUri) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(imageUri);
        this.sendBroadcast(mediaScanIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, String.format("Result Code : %d, Request Code : %d", resultCode, requestCode));
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {

                Uri photoUri = FileProvider.getUriForFile(AddProducts.this, AddProducts.this.
                        getApplicationContext().getPackageName() + ".provider", getCameraFile());
                if (photoUri != null) {
                    setImg(photoUri);
                }

               /* launchMediaScanIntent(Uri.fromFile(destination));
                setImg(Uri.fromFile(destination));*/
            } else {
                finish();
            }
        } else if (requestCode == GALLERY_IMAGE_REQUEST) {
            //Received Data and Update Current Data
            if (resultCode == RESULT_OK && data != null) {
                setImg(data.getData());
            } else {
                finish();
                Log.d(TAG, "onActivityResult: Do Noting Result Cancelled");
            }

        }
    }
    public void setImg(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(AddProducts.this.getContentResolver(), uri),
                                300);
                mProductImage.setImageBitmap(bitmap);
                mImageString = Utility.getStringFromBitmap(bitmap);
            } catch (IOException e) {
                Log.d("TAG", "Image picking failed because " + e.getMessage());
                Toast.makeText(AddProducts.this, "Selecting image failed", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d("TAG", "Image picker gave us a null image.");
            Toast.makeText(AddProducts.this, "Error selecting an image  ", Toast.LENGTH_LONG).show();
        }
    }

    /*private void setImg(Uri fromFile) {
        mImageBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(fromFile, getContentResolver());
        mProductImage.setImageBitmap(mImageBitmap);
        mImageString = Utility.getStringFromBitmap(mImageBitmap);
    }*/
    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }


    public String dateToString(Date date, String format) {
        SimpleDateFormat df = new SimpleDateFormat(format, Locale.getDefault());
        return df.format(date);
    }

    private class AddProductsTask extends AsyncTask<String, JSONObject, String> {
        private WeakReference<Context> mContext;
        private String TAG = "GetCounters";
        private ProgressDialog mProgressDialog;

        public AddProductsTask(Context mContext) {
            this.mContext = new WeakReference<Context>(mContext);
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!mProgressDialog.isShowing()) {
                mProgressDialog.setMessage("Please wait");
                mProgressDialog.show();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            String result = "";
            RestAPI restAPI = new RestAPI();
            try {
                result = new JSONParse()
                        .Parse(restAPI.AddProduct(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5], strings[6]));
            } catch (Exception e) {
                result = e.getMessage();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            if (Utility.checkConnection(s)) {
                Pair<String, String> mErrors = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(mContext.get(), mErrors.first, mErrors.second, false);
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    if (jsonObject.getString("status").equalsIgnoreCase("true")) {
                        Toast.makeText(mContext.get(), "Product Added", Toast.LENGTH_SHORT).show();
                        finish();
                    } else if (jsonObject.getString("status").equalsIgnoreCase("already")) {
                        Utility.ShowAlertDialog(mContext.get(), "Product Exists"
                                , "A product with same name already exists, enter another name.", false);
                    } else {
                        Utility.ShowAlertDialog(mContext.get(), "Add Product"
                                , "Could not add product, Please try again", false);
                        String error = jsonObject.getString("Data");
                        Log.d(TAG, "Error : " + error);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    @Override
    protected void onDestroy() {
        if (destination != null && destination.exists())
            if (destination.delete()) {
                Log.d(TAG, "onDestroy: File Deleted");
            }
        super.onDestroy();

    }
}

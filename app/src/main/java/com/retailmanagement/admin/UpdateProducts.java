package com.retailmanagement.admin;

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
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.retailmanagement.admin.adapters.CustomSpinnerAdapter;
import com.retailmanagement.admin.helper.ImageHelper;
import com.retailmanagement.admin.models.CounterModel;
import com.retailmanagement.admin.models.Product;
import com.retailmanagement.admin.webservices.JSONParse;
import com.retailmanagement.admin.webservices.RestAPI;
import com.retailmanagement.admin.webservices.Utility;

import org.json.JSONArray;
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

public class UpdateProducts extends AppCompatActivity {
    public static final String PRODUCT = "Product";
    public static final int PERMISSION_REQUEST = 101;
    public static final int GALLERY_IMAGE_REQUEST = 102;
    private static final String TAG = "UpdateProducts";
    private static final int REQUEST_IMAGE = 103;

    private EditText mProductName, mProductPrice, mProductQty, mProductDesc;
    private ImageView mProductImage;
    private Spinner mCategory;
    private MaterialButton mProductAdd;
    private Bitmap mImageBitmap;
    private String mImageString;
    private File destination;
    private Product mProductItem;
    private String mDataString;

    private List<Pair<String, String>> mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_products);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Update Products");

        mDataString = getIntent().getStringExtra(PRODUCT);


        mProductAdd = findViewById(R.id.productAdd);
        mProductAdd.setText("Update");
        mProductName = findViewById(R.id.productName);
        mProductPrice = findViewById(R.id.productPrice);
        mProductQty = findViewById(R.id.productQty);
        mProductDesc = findViewById(R.id.productDesc);
        mProductImage = findViewById(R.id.productImage);

        mCategory = findViewById(R.id.product_category);

        mData = getItemData();
        final CustomSpinnerAdapter customSpinnerAdapter = new CustomSpinnerAdapter(UpdateProducts.this
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
                            , mProductName.getText().toString()
                            , mProductPrice.getText().toString()
                            , mProductQty.getText().toString()
                            , selectedItem.first));

                    //String pid,String name,String description
                    //      ,String type,String price,String qty,String image,String barcode

                    new UpdateProduct(UpdateProducts.this)
                            .execute(mProductItem.getProductId(), mProductName.getText().toString()
                                    , mProductDesc.getText().toString()
                                    , selectedItem.first
                                    , mProductPrice.getText().toString()
                                    , mProductQty.getText().toString()
                                    , mImageString, mProductItem.getProductBarcode());
                }
            }
        });

        mProductImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (weHavePermission()) {
                    takePicture();
                } else {
                    requestContactsPermission();
                }
            }
        });

        if (mDataString == null) {
            Toast.makeText(this, "Could find details about product, try again", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            new GetProducts(UpdateProducts.this).execute(mDataString);
        }

    }

    private class GetProducts extends AsyncTask<String, JSONObject, String> {
        private WeakReference<Context> mContext;
        private String TAG = "GetProducts";
        private ProgressDialog mProgressDialog;

        public GetProducts(Context mContext) {
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
                result = new JSONParse().Parse(restAPI.Product_byId(strings[0]));
            } catch (Exception e) {
                result = e.getMessage();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute: " + s);
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            if (Utility.checkConnection(s)) {
                Pair<String, String> mErrors = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(mContext.get(), mErrors.first, mErrors.second, false);
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    if (jsonObject.getString("status").equalsIgnoreCase("ok")) {

                        JSONArray mJsonArray = jsonObject.getJSONArray("Data");
                        for (int i = 0; i < mJsonArray.length(); i++) {
                            JSONObject data = mJsonArray.getJSONObject(i);
//                            String productId, String productName
//                                    , String productBarcode, String productPrice, String productType, String productDescription
//                                    , String productImage, String productQuantity
                            mProductItem = new Product(data.getString("data0")
                                    , data.getString("data1"), data.getString("data2")
                                    , data.getString("data3"), data.getString("data4")
                                    , data.getString("data5"), data.getString("data6")
                                    , data.getString("data7"));
                        }

                        setValues();

                    } else if (jsonObject.getString("status").equalsIgnoreCase("no")) {
                        Utility.ShowAlertDialog(mContext.get(), "No Products"
                                , "Could not find the product, please try again", true);
                    } else {
                        String error = jsonObject.getString("Data");
                        Log.d(TAG, "Error : " + error);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private void setValues() {
        mProductName.setText(mProductItem.getProductName());
        mProductDesc.setText(mProductItem.getProductDescription());
        mProductQty.setText(mProductItem.getProductQuantity());
        mProductPrice.setText(mProductItem.getProductPrice());
        mImageString = mProductItem.getProductImage();
        mProductImage.setImageBitmap(Utility.getBitmapFromString(mProductItem.getProductImage()));

        for (int i = 0; i < mData.size(); i++) {
            Pair<String, String> mTypes = mData.get(i);
            if (mTypes.first.equalsIgnoreCase(mProductItem.getProductType())) {
                mCategory.setSelection(i);
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_products, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        else if (item.getItemId() == R.id.delete_product) {
            showDeleteAlertDialog(mProductItem);
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteAlertDialog(@NonNull final Product counterModel) {
        new MaterialAlertDialogBuilder(UpdateProducts.this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete this product ?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new DeleteProduct(UpdateProducts.this).execute(counterModel.getProductId());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
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
        return (ActivityCompat.checkSelfPermission(UpdateProducts.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(UpdateProducts.this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(UpdateProducts.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestContactsPermission() {
        ActivityCompat.requestPermissions(UpdateProducts.this,
                new String[]{Manifest.permission.CAMERA
                        , Manifest.permission.WRITE_EXTERNAL_STORAGE
                        , Manifest.permission.READ_EXTERNAL_STORAGE}
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

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(UpdateProducts.this);
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

    private void startCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri photoUri = FileProvider.getUriForFile(UpdateProducts.this,
                UpdateProducts.this.getApplicationContext().getPackageName() + ".provider", getCameraFile());
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
                Uri photoUri = FileProvider.getUriForFile(UpdateProducts.this, UpdateProducts.this.
                        getApplicationContext().getPackageName() + ".provider", getCameraFile());
                if (photoUri != null) {
                    setImg(photoUri);
                }

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
    public File getCameraFile() {
        File dir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(dir, "temp.jpg");
    }

    public void setImg(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(UpdateProducts.this.getContentResolver(), uri),
                                300);
                mProductImage.setImageBitmap(bitmap);
                mImageString = Utility.getStringFromBitmap(bitmap);
            } catch (IOException e) {
                Log.d("TAG", "Image picking failed because " + e.getMessage());
                Toast.makeText(UpdateProducts.this, "Selecting image failed", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d("TAG", "Image picker gave us a null image.");
            Toast.makeText(UpdateProducts.this, "Error selecting an image  ", Toast.LENGTH_LONG).show();
        }
    }
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

    private class UpdateProduct extends AsyncTask<String, JSONObject, String> {
        private WeakReference<Context> mContext;
        private String TAG = "GetCounters";
        private ProgressDialog mProgressDialog;

        public UpdateProduct(Context mContext) {
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
                        .Parse(restAPI.UpdateProduct(strings[0], strings[1], strings[2], strings[3]
                                , strings[4], strings[5], strings[6], strings[7]));
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
                        Toast.makeText(mContext.get(), "Product Updated", Toast.LENGTH_SHORT).show();
                        finish();
                    } else if (jsonObject.getString("status").equalsIgnoreCase("already")) {
                        Utility.ShowAlertDialog(mContext.get(), "Product Exists"
                                , "A product with same name already exists, enter another name.", false);
                    } else {
                        Utility.ShowAlertDialog(mContext.get(), "Add Product"
                                , "Could not chnage product details, Please try again", false);
                        String error = jsonObject.getString("Data");
                        Log.d(TAG, "Error : " + error);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private class DeleteProduct extends AsyncTask<String, JSONObject, String> {
        private WeakReference<Context> mContext;
        private String TAG = "GetCounters";
        private ProgressDialog mProgressDialog;

        public DeleteProduct(Context mContext) {
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
                result = new JSONParse().Parse(restAPI.DeleteProduct(strings[0]));
            } catch (Exception e) {
                result = e.getMessage();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute: " + s);
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
                        Toast.makeText(UpdateProducts.this, "Product Deleted", Toast.LENGTH_SHORT).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 200);
                    } else {
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

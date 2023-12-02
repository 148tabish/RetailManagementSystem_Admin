package com.retailmanagement.admin;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.retailmanagement.admin.models.Product;
import com.retailmanagement.admin.webservices.JSONParse;
import com.retailmanagement.admin.webservices.RestAPI;
import com.retailmanagement.admin.webservices.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ProductsActivity extends AppCompatActivity {
    private ListView mProductsList;

    private ArrayList<Product> mProductData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counters);

        getSupportActionBar().setTitle("Products");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mProductsList = findViewById(R.id.list_counters);
        FloatingActionButton mAddCounters = findViewById(R.id.add_counters);

        mAddCounters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProductsActivity.this, AddProducts.class));
            }
        });



        mProductsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ProductsActivity.this, UpdateProducts.class);
                intent.putExtra(UpdateProducts.PRODUCT, mProductData.get(position).getProductId());
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mProductsList.setAdapter(null);
        new GetProducts(ProductsActivity.this).execute();
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
        protected String doInBackground(String... strings)
        {
            String result = "";
            RestAPI restAPI = new RestAPI();
            try {
                result = new JSONParse().Parse(restAPI.GetAllProducts());
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
                        mProductData = new ArrayList<Product>();
                        JSONArray mJsonArray = jsonObject.getJSONArray("Data");
                        for (int i = 0; i < mJsonArray.length(); i++) {
                            JSONObject data = mJsonArray.getJSONObject(i);
//                            String productId, String productName
//                                    , String productBarcode, String productPrice, String productType, String productDescription
//                                    , String productImage, String productQuantity
                            mProductData.add(new Product(data.getString("data0")
                                    , data.getString("data1"), data.getString("data2")
                                    , data.getString("data3"), data.getString("data4")
                                    , data.getString("data5"), data.getString("data6")
                                    , data.getString("data7")));
                        }

                        setValues();

                    } else if (jsonObject.getString("status").equalsIgnoreCase("no")) {
                        Utility.ShowAlertDialog(mContext.get(), "No Products"
                                , "No products found,Please add products", false);
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
        ListAdapter mListAdapter = new ListAdapter(mProductData);
        mProductsList.setAdapter(mListAdapter);
    }

    private class ListAdapter extends BaseAdapter {
        private ArrayList<Product> mData;

        public ListAdapter(ArrayList<Product> mData) {
            this.mData = mData;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder mViewHolder;
            if (convertView == null) {
                mViewHolder = new ViewHolder();
                convertView = getLayoutInflater().inflate(R.layout.item_products
                        , parent, false);

                mViewHolder.mProductImage = convertView.findViewById(R.id.item_product_image);

                mViewHolder.mProductName = convertView.findViewById(R.id.item_product_name);
                mViewHolder.mProductDesc = convertView.findViewById(R.id.item_product_desc);
                mViewHolder.mProductQty = convertView.findViewById(R.id.item_product_qty);
                mViewHolder.mProductPrice = convertView.findViewById(R.id.item_product_price);

                convertView.setTag(mViewHolder);
            } else {
                mViewHolder = (ViewHolder) convertView.getTag();
            }
            final Product mProduct = (Product) getItem(position);

            mViewHolder.mProductName.setText("Product Name : " + mProduct.getProductName());
            mViewHolder.mProductDesc.setText("Product Description : \n" + mProduct.getProductDescription());
            mViewHolder.mProductQty.setText("Qty : " + mProduct.getProductQuantity());
            mViewHolder.mProductPrice.setText(getResources().getString(R.string.currency) + " : " + mProduct.getProductPrice());
            mViewHolder.mProductImage.setImageBitmap(Utility.getBitmapFromString(mProduct.getProductImage()));


            return convertView;
        }

        private class ViewHolder {
            private TextView mProductName, mProductDesc, mProductQty, mProductPrice;
            private ImageView mProductImage;
        }
    }
}

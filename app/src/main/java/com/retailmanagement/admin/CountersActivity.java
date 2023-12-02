package com.retailmanagement.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.retailmanagement.admin.models.CounterModel;
import com.retailmanagement.admin.webservices.JSONParse;
import com.retailmanagement.admin.webservices.RestAPI;
import com.retailmanagement.admin.webservices.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class CountersActivity extends AppCompatActivity {
    private ListView mCounters;

    private ArrayList<CounterModel> mCounterData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counters);

        getSupportActionBar().setTitle("Counter");

        mCounters = findViewById(R.id.list_counters);
        FloatingActionButton mAddCounters = findViewById(R.id.add_counters);

        mAddCounters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CounterDialog addCountersDialog = new CounterDialog(CountersActivity.this
                        , R.style.AppTheme, false, new CounterDialog.CounterInterface() {
                    @Override
                    public void onCounterSuccess() {
                        new GetCountersTask(CountersActivity.this).execute();
                    }
                }, "");
                addCountersDialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorTransparent)));
                addCountersDialog.show();
            }
        });

        mCounters.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CounterDialog addCountersDialog = new CounterDialog(CountersActivity.this
                        , R.style.AppTheme, true, new CounterDialog.CounterInterface() {
                    @Override
                    public void onCounterSuccess() {
                        new GetCountersTask(CountersActivity.this).execute();
                    }
                }, mCounterData.get(position).getCounterId(),mCounterData.get(position).getCounterName()
                        ,mCounterData.get(position).getCounterPass());
                addCountersDialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorTransparent)));
                addCountersDialog.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        new GetCountersTask(CountersActivity.this).execute();
    }

    private class GetCountersTask extends AsyncTask<String, JSONObject, String> {
        private WeakReference<Context> mContext;
        private String TAG = "GetCounters";
        private ProgressDialog mProgressDialog;

        public GetCountersTask(Context mContext) {
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
                result = new JSONParse().Parse(restAPI.GetAllCounters());
            } catch (Exception e) {
                result = e.getMessage();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute: "+s);
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
                        mCounterData = new ArrayList<CounterModel>();
                        JSONArray mJsonArray = jsonObject.getJSONArray("Data");
                        for (int i = 0; i < mJsonArray.length(); i++) {
                            JSONObject data = mJsonArray.getJSONObject(i);
                            mCounterData.add(new CounterModel(data.getString("data0")
                                    , data.getString("data1"), data.getString("data2")));
                        }

                        setValues();

                    } else if (jsonObject.getString("status").equalsIgnoreCase("no")) {
                        Utility.ShowAlertDialog(mContext.get(), "No Counter"
                                , "No counters found,Please add counters", false);
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
        ListAdapter mListAdapter = new ListAdapter(mCounterData);
        mCounters.setAdapter(mListAdapter);
    }

    private class ListAdapter extends BaseAdapter {
        private ArrayList<CounterModel> mData;

        public ListAdapter(ArrayList<CounterModel> mData) {
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
                convertView = getLayoutInflater().inflate(R.layout.item_counters
                        , parent, false);

                mViewHolder.mCountersView = convertView.findViewById(R.id.counter_item_name);
                mViewHolder.mItemDelete = convertView.findViewById(R.id.counter_item_delete);
                convertView.setTag(mViewHolder);
            } else {
                mViewHolder = (ViewHolder) convertView.getTag();
            }
            final CounterModel mCounters = (CounterModel) getItem(position);
            mViewHolder.mCountersView.setText("Counter Name : "+mCounters.getCounterName());
            mViewHolder.mItemDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteAlertDialog(mCounters);
                }
            });
            return convertView;
        }

        private void showDeleteAlertDialog(@NonNull final CounterModel counterModel) {
            new MaterialAlertDialogBuilder(CountersActivity.this)
                    .setTitle("Delete Counter")
                    .setMessage("Are you sure you want to delete this counter ?")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new DeleteCounter(CountersActivity.this).execute(counterModel.getCounterId());
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
        }

        private class ViewHolder {
            private TextView mCountersView;
            private ImageView mItemDelete;
        }
    }

    private class DeleteCounter extends AsyncTask<String, JSONObject, String> {
        private WeakReference<Context> mContext;
        private String TAG = "GetCounters";
        private ProgressDialog mProgressDialog;

        public DeleteCounter(Context mContext) {
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
                result = new JSONParse().Parse(restAPI.DeleteCounter(strings[0]));
            } catch (Exception e) {
                result = e.getMessage();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute: "+s);
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
                        Toast.makeText(CountersActivity.this, "Counter Deleted", Toast.LENGTH_SHORT).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                new GetCountersTask(CountersActivity.this).execute();
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
}

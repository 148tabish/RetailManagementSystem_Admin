package com.retailmanagement.admin;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import com.retailmanagement.admin.webservices.JSONParse;
import com.retailmanagement.admin.webservices.RestAPI;
import com.retailmanagement.admin.webservices.Utility;


import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.UUID;


public class CounterDialog extends Dialog {
    private Context mContext;
    private EditText mEditCounterName, mEditCounterPass;
    private MaterialButton mCancelButton, mAddButton;
    private CounterInterface mCounterInterface;
    private boolean isUpdateCounter;
    private String[] mCounterValues;

    public CounterDialog(@NonNull Context context, boolean isUpdate, CounterInterface mCounterInterface
            , String...values) {
        super(context);
        this.mContext = context;
        this.mCounterInterface = mCounterInterface;
        this.isUpdateCounter = isUpdate;
        this.mCounterValues = values;
    }

    public CounterDialog(@NonNull Context context, int themeResId
            , boolean isUpdate, CounterInterface mCounterInterface
            , String...values) {
        super(context, themeResId);
        this.mContext = context;
        this.mCounterInterface = mCounterInterface;
        this.isUpdateCounter = isUpdate;
        this.mCounterValues = values;
    }

    protected CounterDialog(@NonNull Context context, boolean cancelable
            , @Nullable OnCancelListener cancelListener
            , boolean isUpdate, CounterInterface mCounterInterface
            , String...values) {
        super(context, cancelable, cancelListener);
        this.mContext = context;
        this.mCounterInterface = mCounterInterface;
        this.isUpdateCounter = isUpdate;
        this.mCounterValues = values;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_counter);

        mEditCounterName = findViewById(R.id.add_counter_name);
        mEditCounterPass = findViewById(R.id.add_counter_password);
        mCancelButton = findViewById(R.id.add_counter_can);
        mAddButton = findViewById(R.id.add_counter_add);

        final TextInputLayout mCounterName = (TextInputLayout) findViewById(R.id.add_counter_name_main);
        final TextInputLayout mCounterPass = (TextInputLayout) findViewById(R.id.add_counter_pass_main);
//        mCounterPass.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);


        mEditCounterName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCounterName.setErrorEnabled(false);
            }
        });

        mEditCounterPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCounterPass.setErrorEnabled(false);
            }
        });


        if(isUpdateCounter){
            TextView textView = findViewById(R.id.add_counter_title);
            textView.setText("Update Counter");
            mEditCounterName.setText(mCounterValues[1]);
            mEditCounterPass.setText(mCounterValues[2]);
            mAddButton.setText("Update");
        }


        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mEditCounterName.getText().toString().length() == 0){
                    mCounterName.setErrorEnabled(true);
                    mCounterName.setError("Enter counter name");
                    mEditCounterName.requestFocus();
                }else if(mEditCounterPass.getText().toString().length() == 0){
                    mCounterPass.setErrorEnabled(true);
                    mCounterPass.setError("Enter counter password");
                    mEditCounterPass.requestFocus();
                }else {
                    if(isUpdateCounter){
                        new UpdateCounter(mContext).execute(mCounterValues[0], mEditCounterName.getText().toString()
                                ,mEditCounterPass.getText().toString());
                    }else {

                        new AddCounterTask(mContext).execute(mEditCounterName.getText().toString()
                                ,mEditCounterPass.getText().toString());
                    }

                }
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isShowing())
                    cancel();
            }
        });
    }

    private class AddCounterTask extends AsyncTask<String, JSONObject, String> {
        private WeakReference<Context> mContext;
        private String TAG = "GetCounters";
        private ProgressDialog mProgressDialog;

        public AddCounterTask(Context mContext) {
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
                result = new JSONParse().Parse(restAPI.AddCounter(strings[0],strings[1]));
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
                        Toast.makeText(mContext.get(), "Counter Added", Toast.LENGTH_SHORT).show();
                        if(CounterDialog.this.isShowing()){
                            CounterDialog.this.cancel();
                        }
                        mCounterInterface.onCounterSuccess();
                    } else if (jsonObject.getString("status").equalsIgnoreCase("already")) {
                        Utility.ShowAlertDialog(mContext.get(), "Counter Exists"
                                , "A counter with same name already exists, enter another name.", false);
                    } else {
                        Utility.ShowAlertDialog(mContext.get(), "Add Counter"
                                , "Could not add counters, Please try again", false);
                        String error = jsonObject.getString("Data");
                        Log.d(TAG, "Error : " + error);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private class UpdateCounter extends AsyncTask<String, JSONObject, String> {
        private WeakReference<Context> mContext;
        private String TAG = "GetCounters";
        private ProgressDialog mProgressDialog;

        public UpdateCounter(Context mContext) {
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
                result = new JSONParse().Parse(restAPI.UpdateCounter(strings[0],strings[1],strings[2]));
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
                        Toast.makeText(mContext.get(), "Counter Updated", Toast.LENGTH_SHORT).show();
                        if(CounterDialog.this.isShowing()){
                            CounterDialog.this.cancel();
                        }
                        mCounterInterface.onCounterSuccess();
                    } else if (jsonObject.getString("status").equalsIgnoreCase("already")) {
                        Utility.ShowAlertDialog(mContext.get(), "Counter Exists"
                                , "A counter with same name already exists,please enter another name.", false);
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

    public interface CounterInterface{
        void onCounterSuccess();
    }

}

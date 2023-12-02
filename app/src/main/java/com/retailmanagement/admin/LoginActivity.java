package com.retailmanagement.admin;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.retailmanagement.admin.helper.PreferenceManager;
import com.retailmanagement.admin.webservices.JSONParse;
import com.retailmanagement.admin.webservices.RestAPI;
import com.retailmanagement.admin.webservices.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText mUserName, mPassword;
    private static ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(PreferenceManager.getAdminLogin(LoginActivity.this)){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }else {
            setContentView(R.layout.activity_login);

            mProgressDialog = new ProgressDialog(LoginActivity.this);
            mProgressDialog.setCancelable(false);

            mUserName = (TextInputEditText) findViewById(R.id.login_username);

            final TextInputLayout mUser = (TextInputLayout) findViewById(R.id.login_user);

            mPassword = (TextInputEditText) findViewById(R.id.login_password);
            final TextInputLayout mPass = (TextInputLayout) findViewById(R.id.login_pass);

            MaterialButton mSignInButton = findViewById(R.id.login_sign_in);

            mUserName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mUser.setErrorEnabled(false);
                }
            });

            mPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPass.setErrorEnabled(false);
                }
            });

            mSignInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mUserName.getText() != null && mUserName.getText().toString().length() == 0) {
                        mUser.setErrorEnabled(true);
                        mUser.setError("Enter your username");
                        mUserName.requestFocus();
                    } else if (mPassword.getText() != null && mPassword.getText().toString().length() == 0) {
                        mPass.setErrorEnabled(true);
                        mPass.setError("Enter your password");
                        mPassword.requestFocus();
                    } else {
                        //Call LoginAPI
                        new LoginTask(LoginActivity.this).execute(mUserName.getText().toString()
                                , mPassword.getText().toString());
                    }
                }
            });
        }

    }

    public static class LoginTask extends AsyncTask<String, JSONObject, String> {
        private WeakReference<Context> mContext;
        private String TAG = "Login";

        public LoginTask(Context mContext) {
            this.mContext = new WeakReference<Context>(mContext);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!mProgressDialog.isShowing()) {
                mProgressDialog.setMessage("Please wait");
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            String result = "";
            RestAPI restAPI = new RestAPI();
            try {
                result = new JSONParse().Parse(restAPI.ALogin(strings[0], strings[1]));
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
                        PreferenceManager.SetAdminLogin(mContext.get(), true);
                        Intent intent = new Intent(mContext.get(), MainActivity.class);
                        mContext.get().startActivity(intent);
                        ((Activity) mContext.get()).finish();
                    } else if (jsonObject.getString("status").equalsIgnoreCase("false")) {
                        Utility.ShowAlertDialog(mContext.get(), "Invalid Credentials"
                                , "You have entered an invalid username or password", false);
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

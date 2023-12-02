package com.retailmanagement.admin.helper;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class PreferenceManager {
    private static final String SHARED_PREF = "RMS_System";

    private static final String ADMIN = "AdminLoggedIn";


    public static  void SetAdminLogin(@NonNull Context mContext, boolean isLoggedIn){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(ADMIN, isLoggedIn);
        editor.apply();
        editor.commit();
    }

    public static boolean getAdminLogin(@NonNull Context mContext) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(ADMIN, false);
    }
}

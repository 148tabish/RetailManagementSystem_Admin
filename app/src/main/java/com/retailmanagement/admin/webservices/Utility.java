package com.retailmanagement.admin.webservices;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.retailmanagement.admin.CountersActivity;
import com.retailmanagement.admin.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class Utility {


    public static final String STRING_SEPARATOR = " , ";

    public static boolean checkConnection(String errorString) {
        boolean error = false;
        if (errorString.contains("unable to resolve host") || errorString.contains("failed to connect") || errorString.contains("network is unreachable")
                || errorString.contains("software caused connection abort") || errorString.contains("connection timed out") || errorString.contains("No address associated with hostname")) {
            error = true;
        }
        return error;
    }

    public static Pair<String, String> GetErrorMessage(String errorString) {
        Pair<String, String> pair = new Pair<>("Something went Wrong","Can't find anything for you");

        if (errorString.contains("Unable to resolve host")) {

            pair = new Pair<>("Unable to Connect!", "Check your Internet Connection,Unable to connect the Server");

        } else if (errorString.contains("Failed to connect")) {

            pair = new Pair<>("Connection timed out", "Check your Internet Connection");

        } else if (errorString.contains("Network is unreachable")) {

            pair = new Pair<>("Network unreachable", "Could not connect to Internet, Check your mobile/wifi Connection");

        } else if (errorString.contains("Software caused connection abort")) {

            pair = new Pair<>("Connection Aborted", "Connection was aborted by server, without any response");

        } else if (errorString.contains("Connection timed out")) {

            pair = new Pair<>("Connection timed out", "Could not connect server, check internet connection");

        } else if (errorString.contains("No address associated with hostname")) {

            pair = new Pair<>("Unable to Connect!", "Check your Internet Connection,Unable to connect the Server");

        }
        return pair;
    }

    public static void ShowAlertDialog(final Context context, String title, String message, final boolean isFinish) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (isFinish)
                            ((Activity) context).finish();
                    }
                })
                .show();
    }

    public static String getStringFromBitmap(@NonNull Bitmap bitmap){
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    public static Bitmap getBitmapFromString(@NonNull String image){
        byte [] encodeByte = Base64.decode(image,Base64.DEFAULT);
        InputStream inputStream  = new ByteArrayInputStream(encodeByte);
        return BitmapFactory.decodeStream(inputStream);
    }

}

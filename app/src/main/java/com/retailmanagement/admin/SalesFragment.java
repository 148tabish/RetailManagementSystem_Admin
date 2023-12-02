package com.retailmanagement.admin;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.retailmanagement.admin.adapters.OrderDetailListAdap;
import com.retailmanagement.admin.adapters.OrrderAdap;
import com.retailmanagement.admin.adapters.OrrderPojo;
import com.retailmanagement.admin.models.Product;
import com.retailmanagement.admin.webservices.JSONParse;
import com.retailmanagement.admin.webservices.RestAPI;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SalesFragment extends Fragment implements View.OnClickListener {
    private ProgressDialog progLoader;
    private LinearLayout llTab;
    private String sDate;
    private ListView listView;
    private ArrayList<OrrderPojo> orderPojos;
    Calendar cal;
    private TextView dateTVStart;
    Calendar sCal;
    private SimpleDateFormat sdFormat;
    private RelativeLayout noHistImageView;
    private static final String TAG = "TAg";
    private Dialog dialogDetailList;
    private Button payTotalBtn, dismmissBtn;


    public SalesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.activity_order, container, false);
        llTab = rootView.findViewById(R.id.ll_tab);
        noHistImageView = rootView.findViewById(R.id.rl_image);
        listView = rootView.findViewById(R.id.lv_tab);
        setHasOptionsMenu(true);
        initDataObj();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getSharedHist();
    }

    private void initDataObj() {
        cal = Calendar.getInstance();
        sCal = Calendar.getInstance();
        Date date = cal.getTime();
        sdFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
        sDate = simpleFormat.format(date);
        //dateTVStart.setText(getResources().getString(R.string.date_) + " " + sDate);
    }

    private void getSharedHist() {
        if (sDate != null) {
            OrderTask task = new OrderTask();
            task.execute(sDate);
            Log.d(TAG, sDate + ": sDate, ");
        }
    }

    private void sDatePicker() {
        DatePickerDialog pickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                sCal.set(Calendar.YEAR, year);
                sCal.set(Calendar.MONTH, month);
                sCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                sDate = sdFormat.format(sCal.getTime());
                //dateStart = sDate;
                ((AppCompatActivity) getActivity())
                        .getSupportActionBar().setSubtitle(getResources().getString(R.string.date_) + " " + sDate);
                getSharedHist();
            }
        }, sCal.get(Calendar.YEAR), sCal.get(Calendar.MONTH), sCal.get(Calendar.DAY_OF_MONTH));
        pickerDialog.getDatePicker().setMaxDate(new Date().getTime());
        pickerDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.date_picker_actions) {
            sDatePicker();
        }
        return true;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_subs, menu);
    }

    public void openOrderDetailActivity(OrrderPojo orderPojo) {
        //dialog
        if (dialogDetailList == null) {
            dialogDetailList = new Dialog(getActivity());
            //dialogDetailList.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#8D000000")));
            final View view = getLayoutInflater().inflate(R.layout.dialog_detail_order, null);
            listView = view.findViewById(R.id.lv_orderdetail);
            payTotalBtn = view.findViewById(R.id.btn_total_pay);
            //payTotalBtn.setOnClickListener(this);
            String totalEach = null ;
            float totatl = 0;
            for (int i = 0; i < orderPojo.getProductArrayList().size(); i++) {
                if (!(totalEach = orderPojo.getProductArrayList().get(i).getTotal()).isEmpty()){
                    totatl =+ Float.parseFloat(totalEach);
                }
            }
            payTotalBtn.setText("Total \u20b9 "+ totatl);
            payTotalBtn.setEnabled(false);
            dismmissBtn = view.findViewById(R.id.btn_dismiss);
            dismmissBtn.setOnClickListener(this);
            listView = view.findViewById(R.id.lv_orderdetail);
            listView.setAdapter(new OrderDetailListAdap(getContext(), orderPojo.getProductArrayList()));
            dialogDetailList.setContentView(view);
            dialogDetailList.setCancelable(false);
        }
        if (dialogDetailList != null && !dialogDetailList.isShowing())
            dialogDetailList.show();

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_total_pay) {
            dismissDialog();


            //show pay option for user-credit card detals and counter -just user paid now
        } else if (view.getId() == R.id.btn_dismiss) {
            dismissDialog();
        }
    }

    private void dismissDialog() {
        if (dialogDetailList != null && dialogDetailList.isShowing())
            dialogDetailList.dismiss();
    }

    private class OrderTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            startAnimation();
        }

        @Override
        protected String doInBackground(String... strings) {
            String result;
            RestAPI restAPI = new RestAPI();
            JSONObject jsonObject = null;
            try {
                Log.i(TAG, "doInBackgroundO: " + strings[0]);
                result = new JSONParse().Parse(restAPI.AdminAllsales(strings[0]));
            } catch (Exception e) {
                result = e.getMessage();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            stopAnimation();
            Log.d("replyorder det", s);
            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder(getContext());
                ad.setTitle("Unable to Connect!");
                ad.setMessage("Check your Internet Connection,Unable to connect the Server");
                ad.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                ad.show();
            } else {
                try {
                    JSONObject json = new JSONObject(s);
                    orderPojos = new ArrayList<>();
                    OrrderPojo orderPojo;
                    String ans = json.getString("status");
                    if (ans.compareTo("ok") == 0) {
                        JSONArray jsonDataArray = json.getJSONArray("Data");
                        for (int j = 0; j < jsonDataArray.length(); j++) {
                            JSONObject jsonO = jsonDataArray.getJSONObject(j);
                            //Oid, Uid,counterid, date, status, Uname
                            //Data[] - pid,pname,pimg,quantity,price,totprice
                            orderPojo = new OrrderPojo(jsonO.getString("O_Id"), jsonO.getString("Cust_Id")
                                    , jsonO.getString("Counter_Id"),
                                    jsonO.getString("O_Date"), jsonO.getString("Status")
                                    , jsonO.getString("C_Name"));
                            JSONArray jArray = jsonO.getJSONArray("Data");
                            ArrayList<Product> orderDetailsPojos = null;
                            orderDetailsPojos = new ArrayList<>();
                            for (int i = 0; i < jArray.length(); i++) {
                                JSONObject object = jArray.getJSONObject(i);
                                Log.i(TAG, "onPostExecute: DetailOrder list" + i);
                                //DetailOrder list

                                orderDetailsPojos.add(new Product(object.getString("data0"),
                                        object.getString("data1"),
                                        object.getString("data2"),
                                        object.getString("data3"),
                                        object.getString("data4"),
                                        object.getString("data5")));
                            }
                            orderPojo.setProductArrayList(orderDetailsPojos);
                            orderPojos.add(orderPojo);
                        }
                        setLV();
                    } else if (ans.compareTo("no") == 0) {
                        noHistImageView.setVisibility(View.VISIBLE);  //visible, gone
                        listView.setAdapter(null);
                        listView.setVisibility(View.GONE);
                    } else if (ans.compareTo("error") == 0) {
                        String error = json.getString("Data");
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    stopAnimation();
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void setLV() {
        noHistImageView.setVisibility(View.GONE);
        OrrderAdap usersAdap = new OrrderAdap(getContext(), orderPojos
                , SalesFragment.this);
        listView.setAdapter(usersAdap);
        listView.setVisibility(View.VISIBLE);
    }

    private void stopAnimation() {
        if (progLoader != null && progLoader.isShowing())
            progLoader.cancel();
    }

    private void startAnimation() {
        if (progLoader == null) {
            progLoader = new ProgressDialog(getContext());
            progLoader.setTitle("Loading...");
            progLoader.setCancelable(false);
        }
        if (progLoader != null && !progLoader.isShowing()) progLoader.show();
    }
}

package com.retailmanagement.admin;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.retailmanagement.admin.adapters.SpnAdap;
import com.retailmanagement.admin.adapters.ProdSalesAdap;
import com.retailmanagement.admin.models.Product;
import com.retailmanagement.admin.webservices.JSONParse;
import com.retailmanagement.admin.webservices.RestAPI;
import com.retailmanagement.admin.webservices.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class BarGraphActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "TAG";
    BarChart chart;
    ArrayList<BarEntry> BARENTRY;
    BarDataSet Bardataset;
    BarData BARDATA;
    //-------------

    private Spinner spnFilter;
    private ArrayList<String> list;
    private ArrayList<String> prodCount, prodName;
    private ArrayList<String> typesOfProds;

    private String eDate, sDate;
    private String currentDate;
    private SimpleDateFormat sdFormat;

    Calendar sCal, eCal;
    private SimpleDateFormat simpleFormat;
    private Calendar cal;
    private Dialog dialogCustomDates;
    private TextView dateTVStart, dateTVEnd;
    private ImageView dateFromBtn, dateToBtn;
    private Button okBtn, cancelBtn;
    private LinearLayout llTab;

    private DatePickerDialog pickerDialog;
    private DatePickerDialog pickerDialogE;
    private String[] itemCategories;
    private Spinner spnProdType;
    private ListView listView;
    private RelativeLayout rLayoutImage;
    private ProdSalesAdap adapter;
    private ArrayList<Product> prodPojos;
    private boolean firstCall;
    private SpnAdap typesOfProdsAdap, listAdap;
    private boolean firstCallDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_graph);
        chart = (BarChart) findViewById(R.id.chart1);
        itemCategories = getResources().getStringArray(R.array.item_categories);
        listView = findViewById(R.id.lv_tab);
        rLayoutImage = findViewById(R.id.rl_image);
        spnFilter = findViewById(R.id.spn_filterlist);
        typesOfProds = new ArrayList<>();    //dynamically from list
        typesOfProds.add("All");
        spnProdType = findViewById(R.id.spn_prodTypeList);
        list = new ArrayList<>();
        list.addAll(Arrays.asList("Todays", "Weekly", "Monthly", "Yearly", "Custom Dates"));
        listAdap = new SpnAdap(BarGraphActivity.this, list);
        spnFilter.setAdapter(listAdap);
        spnFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                Log.i(TAG, "onItemSelected: filter");
                if (firstCall) {
                    switch (position) {
                        case 0:
                            getDates(Calendar.DAY_OF_MONTH);
                            break;
                        case 1:
                            getDates(Calendar.WEEK_OF_MONTH);
                            break;
                        case 2:
                            getDates(Calendar.MONTH);
                            break;
                        case 3:
                            getDates(Calendar.YEAR);
                            break;
                        case 4:
                            //show calendar dialog for two custom dates
                            showCalendarCustomDates();
                            break;

                    }
                }
                firstCall = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //will be called in itemselectListener
        initDataObj();
        Log.i(TAG, "onCreate: getdata");
        getData();
    }


    private void getDates(int i) {
        Date date;
        SimpleDateFormat simpleDateFormat;
        switch (i) {
            case Calendar.DAY_OF_MONTH:
                eDate = getCurrentDate();
                sDate = getCurrentDate();
                break;
            case Calendar.WEEK_OF_MONTH:
                cal = Calendar.getInstance();
                date = cal.getTime();
                simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
                eDate = simpleDateFormat.format(date);
                cal.add(Calendar.WEEK_OF_MONTH, -1);
                sDate = simpleDateFormat.format(cal.getTime());
                break;
            case Calendar.MONTH:
                cal = Calendar.getInstance();
                date = cal.getTime();
                simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
                eDate = simpleDateFormat.format(date);
                cal.add(Calendar.MONTH, -1);
                sDate = simpleDateFormat.format(cal.getTime());
                break;
            case Calendar.YEAR:
                cal = Calendar.getInstance();
                date = cal.getTime();
                simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
                eDate = simpleDateFormat.format(date);
                cal.add(Calendar.YEAR, -1);
                sDate = simpleDateFormat.format(cal.getTime());
                break;

        }
        Log.i(TAG, "getDates: getdata");
        getData();
    }

    private void showCalendarCustomDates() {
        //dialog view
        if (dialogCustomDates == null) {
            dialogCustomDates = new Dialog(this, R.style.AppTheme_AlertDialog);
            dialogCustomDates.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#8D000000")));
            final View view = getLayoutInflater().inflate(R.layout.dialog_custom_dates, null);
            llTab = view.findViewById(R.id.ll_tab);
            dateFromBtn = view.findViewById(R.id.btn_datepick_from);
            dateToBtn = view.findViewById(R.id.btn_datepick_to);
            cancelBtn = view.findViewById(R.id.btn_cancel);
            okBtn = view.findViewById(R.id.btn_ok);
            initDataObj();

            dateToBtn.setOnClickListener(this);
            dateFromBtn.setOnClickListener(this);
            okBtn.setOnClickListener(this);
            cancelBtn.setOnClickListener(this);
            dateTVStart = view.findViewById(R.id.tv_date_from);
            dateTVEnd = view.findViewById(R.id.tv_date_to);

            dialogCustomDates.setContentView(view);
            dialogCustomDates.setCancelable(false);
        }
        if (!dialogCustomDates.isShowing())
            dialogCustomDates.show();
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_datepick_to) {
            eDatePicker();
        } else if (view.getId() == R.id.btn_datepick_from) {
            sDatePicker();
        } else if (view.getId() == R.id.btn_cancel) {
            dismissDialoCustomDates();
            spnFilter.setSelection(0);
        } else if (view.getId() == R.id.btn_ok) {
            dismissDialoCustomDates();
            Log.i(TAG, "onClick: getdata");
            getData();
        }
    }

    private void dismissDialoCustomDates() {
        if (dialogCustomDates != null && dialogCustomDates.isShowing())
            dialogCustomDates.dismiss();
    }

    private class GetGraphTask extends AsyncTask<String, JSONObject, String> {
        private WeakReference<Context> mContext;
        private String TAG = "GetProducts";
        private ProgressDialog mProgressDialog;

        public GetGraphTask(Context mContext) {
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
                result = new JSONParse().Parse(restAPI.getsalesreport(strings[0], strings[1], strings[2]));
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
                //api getsalesreport TASK
                prodCount = new ArrayList<>();
                prodName = new ArrayList<>();
                typesOfProds = new ArrayList<>();    //dynamically from list
                typesOfProds.add("All");
                try {

                    JSONObject jsonObject = new JSONObject(s);
                    if (jsonObject.getString("status").equalsIgnoreCase("ok")) {
                        JSONArray mJsonArray = jsonObject.getJSONArray("Data");
                        prodPojos = new ArrayList<>();
                        for (int i = 0; i < mJsonArray.length(); i++) {
                            JSONObject data = mJsonArray.getJSONObject(i);
                            //pid,pname,type,count
                            for (int j = 0; j < itemCategories.length; j++) {
                                if (data.getString("data2").equalsIgnoreCase(itemCategories[j])) {
                                    //add and break
                                    prodName.add(data.getString("data1"));
                                    prodCount.add(data.getString("data3"));
                                    prodPojos.add(new Product(data.getString("data0"),
                                            data.getString("data1"),
                                            data.getString("data2"),
                                            data.getString("data3")));
                                    if (!typesOfProds.contains(data.getString("data2")))
                                        typesOfProds.add(data.getString("data2"));
                                    break;
                                }
                            }
                        }
                        setValues();

                    } else if (jsonObject.getString("status").equalsIgnoreCase("no")) {
                        Utility.ShowAlertDialog(mContext.get(), "No Products"
                                , "Could not find the product, please try again", true);
                        listView.setAdapter(null);
                        listView.setVisibility(View.GONE);
                        rLayoutImage.setVisibility(View.VISIBLE);
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

        //set spinner
        if (typesOfProdsAdap == null) {
            typesOfProdsAdap = new SpnAdap(BarGraphActivity.this, typesOfProds);
            spnProdType.setAdapter(typesOfProdsAdap);
            spnProdType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Log.i(TAG, "onItemSelected: ");
                    if (firstCallDone)
                        if (typesOfProds != null && typesOfProds.size() > 0) {
                            Log.i(TAG, "onItemSelected: getdata");
                            getData();
                        }
                    firstCallDone = true;
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        } else {
            typesOfProdsAdap.notifyDataSetChanged();
        }
        //second for option spinner
        float fcounter = 0f;
        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        for (int i = 0; i < prodCount.size(); i++) {
            //for i products:
            BARENTRY = new ArrayList<>();
            fcounter++;
            BARENTRY.add(new BarEntry(fcounter, Float.parseFloat(prodCount.get(i))));
            Bardataset = new BarDataSet(BARENTRY, prodName.get(i));
            dataSets.add(Bardataset);
        }
        BARDATA = new BarData(dataSets);

        Bardataset.setColors(ColorTemplate.COLORFUL_COLORS);

        chart.setData(BARDATA);

        chart.animateY(2000);

        //lisetview
        setListView();
    }

    private void setListView() {
        if (prodName.size() > 0) {
            adapter = new ProdSalesAdap(BarGraphActivity.this, prodPojos);
            listView.setAdapter(adapter);
            listView.setVisibility(View.VISIBLE);
            rLayoutImage.setVisibility(View.GONE);
        } else {
            listView.setAdapter(null);
            listView.setVisibility(View.GONE);
            rLayoutImage.setVisibility(View.VISIBLE);
        }

    }

    private void initDataObj() {
        cal = Calendar.getInstance();
        sCal = Calendar.getInstance();
        eCal = Calendar.getInstance();
        Date date = cal.getTime();
        simpleFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
        sdFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
        sDate = simpleFormat.format(date);
        eDate = simpleFormat.format(date);
    }

    private String getCurrentDate() {
        cal = Calendar.getInstance();
        Date date = cal.getTime();
        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
        return simpleFormat.format(date);
    }


    private void eDatePicker() {
        pickerDialogE = new DatePickerDialog(BarGraphActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                eCal.set(Calendar.YEAR, year);
                eCal.set(Calendar.MONTH, month);
                eCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                eDate = sdFormat.format(eCal.getTime());
                //dateEnd = eDate;
                dateTVEnd.setText("To: " + eDate);
            }
        }, eCal.get(Calendar.YEAR), eCal.get(Calendar.MONTH), eCal.get(Calendar.DAY_OF_MONTH));
        pickerDialogE.getDatePicker().setMaxDate(new Date().getTime());
        pickerDialogE.getDatePicker().setMinDate(sCal.getTimeInMillis());
        //if (!pickerDialogE.isShowing()) {
        pickerDialogE.show();
        //}
    }

    private void getData() {
        Log.i(TAG, "getData: " + sDate + Utility.STRING_SEPARATOR + eDate);
        if (typesOfProds != null) {
            if (spnProdType.getSelectedItemPosition() == -1) {
                GetGraphTask graphTask = new GetGraphTask(BarGraphActivity.this);
                graphTask.execute(sDate, eDate, typesOfProds.get(0));
            } else {
                GetGraphTask graphTask = new GetGraphTask(BarGraphActivity.this);
                graphTask.execute(sDate, eDate, typesOfProds.get(spnProdType.getSelectedItemPosition()));
            }
        }
    }

    private void sDatePicker() {
        pickerDialog = new DatePickerDialog(BarGraphActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                sCal.set(Calendar.YEAR, year);
                sCal.set(Calendar.MONTH, month);
                sCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                sDate = sdFormat.format(sCal.getTime());
                //dateStart = sDate;
                dateTVStart.setText("From: " + sDate);
            }
        }, sCal.get(Calendar.YEAR), sCal.get(Calendar.MONTH), sCal.get(Calendar.DAY_OF_MONTH));
        pickerDialog.getDatePicker().setMaxDate(new Date().getTime());
        //if (!pickerDialog.isShowing()) {
        pickerDialog.show();
        //}
    }

}

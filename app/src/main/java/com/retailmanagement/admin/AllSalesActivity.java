package com.retailmanagement.admin;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class AllSalesActivity extends AppCompatActivity {

    private SalesFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_sales);
        fragment = new SalesFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_nav, fragment).commit();
    }
}

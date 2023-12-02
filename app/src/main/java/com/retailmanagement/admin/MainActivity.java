package com.retailmanagement.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.retailmanagement.admin.helper.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("Home");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_menu_logout) {
            if (PreferenceManager.getAdminLogin(MainActivity.this)) {
                PreferenceManager.SetAdminLogin(MainActivity.this, false);
            }
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.home_counters:
                startActivity(new Intent(MainActivity.this, CountersActivity.class));
                break;
            case R.id.home_product:
                startActivity(new Intent(MainActivity.this, ProductsActivity.class));
                break;
            case R.id.home_reports:
                startActivity(new Intent(MainActivity.this, BarGraphActivity.class));
                break;
                case R.id.home_allsales:
                startActivity(new Intent(MainActivity.this, AllSalesActivity.class));
                break;
        }
    }
}

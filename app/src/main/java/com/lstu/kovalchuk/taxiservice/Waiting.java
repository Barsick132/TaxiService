package com.lstu.kovalchuk.taxiservice;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

public class Waiting extends AppCompatActivity {

    private DriverGPS driverGPS;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        initToolbar();
    }

    private void initToolbar() {
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.WaitingToolbar);
        toolbar.setOnMenuItemClickListener(menuItem -> false);

        toolbar.inflateMenu(R.menu.menu);
    }

    public void openMenu(MenuItem item) {
        Intent intent = new Intent(this, Menu.class);
        startActivity(intent);
    }

    public void openDetailOrder(View view) {
        Intent intent = new Intent(this, DetailOrder.class);
        startActivity(intent);
    }

    public void closeOrder(View view) {
        Intent intent = new Intent(this, Global.class);
        startActivity(intent);
    }

    private void updateDriverLocation() {
        Intent intent = new Intent(this, Assessment.class);
        startActivity(intent);

    }

    public void openPhoneKeyboard(View view) {
    }
}

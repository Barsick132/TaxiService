package com.lstu.kovalchuk.taxiservice;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class Where extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_where);

        EditText etWhereAddress = (EditText) findViewById(R.id.whereAddress);
    }


    public void checkWhereToMap(View view) {
        Intent intent = new Intent(this, Global.class);
        startActivity(intent);
    }

    public void checkAddress(View view) {
        Intent intent = new Intent(this, Ordering.class);
        startActivity(intent);
    }
}

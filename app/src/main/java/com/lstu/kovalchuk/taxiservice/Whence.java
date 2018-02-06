package com.lstu.kovalchuk.taxiservice;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class Whence extends AppCompatActivity {

    private EditText etWhenceAddress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whence);

        etWhenceAddress = (EditText)findViewById(R.id.whenceAddress);
    }


    public void checkWhenceToMap(View view) {
        Intent intent = new Intent(this, Global.class);
        startActivity(intent);
    }

    private  void checkAddress(View view){
        Intent intent = new Intent(this, Ordering.class);
        startActivity(intent);
    }
}

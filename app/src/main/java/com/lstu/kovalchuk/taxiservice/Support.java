package com.lstu.kovalchuk.taxiservice;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class Support extends AppCompatActivity {

    EditText etEmail;
    EditText etMessage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        etEmail = (EditText)findViewById(R.id.supportEmail);
        etMessage = (EditText)findViewById(R.id.supportContent);
    }

    public void sendMessage(View view) {

    }
}

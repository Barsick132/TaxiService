package com.lstu.kovalchuk.taxiservice;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class Profile extends AppCompatActivity {

    private DataProfile dataProfile = new DataProfile();
    private EditText etFirstName = (EditText)findViewById(R.id.editText15);
    private EditText etLastName = (EditText)findViewById(R.id.editText14);
    private EditText etPatronymic = (EditText)findViewById(R.id.editText16);
    private EditText etPhone = (EditText)findViewById(R.id.editText17);
    private EditText etPassword = (EditText)findViewById(R.id.editText18);
    private EditText etConfirmPassword = (EditText)findViewById(R.id.editText19);


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
    }

    public void saveDataProfile(View view) {
    }
}

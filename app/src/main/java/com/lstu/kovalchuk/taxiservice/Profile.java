package com.lstu.kovalchuk.taxiservice;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class Profile extends AppCompatActivity {

    private DataProfile dataProfile;
    private EditText etFirstName ;
    private EditText etLastName ;
    private EditText etPatronymic;
    private EditText etPhone;
    private EditText etPassword;
    private EditText etConfirmPassword;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dataProfile = new DataProfile();
        etFirstName = (EditText)findViewById(R.id.profileName);
        etLastName = (EditText)findViewById(R.id.profileFamily);
        etPatronymic = (EditText)findViewById(R.id.profilePatronymic);
        etPhone = (EditText)findViewById(R.id.profilePhone);
        etPassword = (EditText)findViewById(R.id.profilePass1);
        etConfirmPassword = (EditText)findViewById(R.id.profilePass2);
    }

    public void saveDataProfile(View view) {
    }
}

package com.lstu.kovalchuk.taxiservice;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class Profile extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Client dataProfile = new Client();
        EditText etFirstName = (EditText)findViewById(R.id.profileName);
        EditText etLastName = (EditText)findViewById(R.id.profileFamily);
        EditText etPatronymic = (EditText)findViewById(R.id.profilePatronymic);
        EditText etPhone = (EditText)findViewById(R.id.profilePhone);
        EditText etPassword = (EditText)findViewById(R.id.profilePass1);
        EditText etConfirmPassword = (EditText)findViewById(R.id.profilePass2);
    }

    public void saveDataProfile(View view) {
    }
}

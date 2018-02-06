package com.lstu.kovalchuk.taxiservice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class Registration extends Activity{

    private EditText etPhone;
    private EditText etLastName;
    private EditText etFirstName;
    private EditText etPass;
    private EditText etConfirmPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        etPhone = (EditText)findViewById(R.id.regPhone);
        etLastName = (EditText)findViewById(R.id.regFamily);
        etFirstName = (EditText)findViewById(R.id.regName);
        etPass = (EditText)findViewById(R.id.regPass);
        etConfirmPass = (EditText)findViewById(R.id.regPass2);
    }

    public void toRegister(View view) {
        /*------
        Зарегистрировать клиента через сервер
        ------*/
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}

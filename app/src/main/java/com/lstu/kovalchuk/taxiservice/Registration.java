package com.lstu.kovalchuk.taxiservice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class Registration extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        EditText etPhone = (EditText)findViewById(R.id.regPhone);
        EditText etLastName = (EditText)findViewById(R.id.regFamily);
        EditText etFirstName = (EditText)findViewById(R.id.regName);
        EditText etPass = (EditText)findViewById(R.id.regPass);
        EditText etConfirmPass = (EditText)findViewById(R.id.regPass2);
    }

    public void toRegister(View view) {
        /*------
        Зарегистрировать клиента через сервер
        ------*/
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}

package com.lstu.kovalchuk.taxiservice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class Registration extends Activity{

    private EditText etPhone = (EditText)findViewById(R.id.editText);
    private EditText etLastName = (EditText)findViewById(R.id.editText);
    private EditText etFirstName = (EditText)findViewById(R.id.editText);
    private EditText etPass = (EditText)findViewById(R.id.editText);
    private EditText etConfirmPass = (EditText)findViewById(R.id.editText);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
    }

    public void toRegister(View view) {
        /*------
        Зарегистрировать клиента через сервер
        ------*/
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}

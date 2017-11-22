package com.lstu.kovalchuk.taxiservice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Registration extends Activity{

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

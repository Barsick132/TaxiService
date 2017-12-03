package com.lstu.kovalchuk.taxiservice;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class Assessment extends AppCompatActivity {

    private int estimate;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment);
    }

    public void saveAssessment(View view) {
        Intent intent = new Intent(this, Global.class);
        startActivity(intent);
    }

    public void star5(View view) {
    }

    public void star4(View view) {
    }

    public void star3(View view) {
    }

    public void star2(View view) {
    }

    public void star1(View view) {
    }
}

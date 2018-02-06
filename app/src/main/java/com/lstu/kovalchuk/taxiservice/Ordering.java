package com.lstu.kovalchuk.taxiservice;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class Ordering extends AppCompatActivity {

    private EditText etWhence;
    private EditText etEntranceWhence;
    private EditText etWhere;
    private EditText etEntranceWhere;
    private TextView tvApproxCost;
    private EditText etComment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordering);

        etWhence = (EditText)findViewById(R.id.orderingWhence1);
        etEntranceWhence = (EditText)findViewById(R.id.orderingWhence2);
        etWhere = (EditText)findViewById(R.id.orderingWhere1);
        etEntranceWhere = (EditText)findViewById(R.id.orderingWhere2);
        tvApproxCost = (TextView)findViewById(R.id.orderingCost);
        etComment = (EditText)findViewById(R.id.orderingComment);

        Spinner spin = (Spinner) findViewById(R.id.orderingSpinnerList);

        ArrayAdapter<String> namesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.spinner_array));

        namesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(namesAdapter);
    }

    public void callTaxi(View view) {
        Intent intent = new Intent(this, Waiting.class);
        startActivity(intent);
    }

    public void openWhence(View view) {
        Intent intent = new Intent(this, Whence.class);
        startActivity(intent);
    }

    public void openWhere(View view) {
        Intent intent = new Intent(this, Where.class);
        startActivity(intent);
    }
}

package com.lstu.kovalchuk.taxiservice;

import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;

public class Ordering extends AppCompatActivity {

    private boolean waitWhere = false;
    private boolean waitWhence = false;

    private MaterialEditText metWhence;
    private MaterialEditText metEntranceWhence;
    private MaterialEditText metWhere;
    private MaterialEditText metEntranceWhere;
    private EditText etComment;

    private Location currentLocation;
    private Address whenceAddress;
    private Address whereAddress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordering);

        metWhence = findViewById(R.id.orderingWhence1);
        metWhence.setKeyListener(null);
        metWhence.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus){
                Intent intent = new Intent(Ordering.this, Whence.class);
                startActivity(intent);
                waitWhence = true;
            }
        });
        metWhence.setOnClickListener(v -> {
            if(!waitWhence) {
                Intent intent = new Intent(Ordering.this, Whence.class);
                startActivity(intent);
            }
        });
        metEntranceWhence = findViewById(R.id.orderingWhence2);
        metWhere = findViewById(R.id.orderingWhere1);
        metWhere.setKeyListener(null);
        metWhere.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus){
                Intent intent = new Intent(Ordering.this, Where.class);
                startActivity(intent);
                waitWhere = true;
            }
        });
        metWhere.setOnClickListener(v -> {
            if(!waitWhere) {
                Intent intent = new Intent(Ordering.this, Where.class);
                startActivity(intent);
            }
        });
        metEntranceWhere = findViewById(R.id.orderingWhere2);
        etComment = findViewById(R.id.orderingComment);

        Spinner spin = findViewById(R.id.orderingSpinnerList);

        ArrayAdapter<String> namesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.spinner_array));

        namesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(namesAdapter);

        Bundle arguments = getIntent().getExtras();
        if (arguments != null) {
            currentLocation = (Location) arguments.get("CurrentLocation");
            whenceAddress = (Address) arguments.get("WhenceAddress");
            whereAddress = (Address) arguments.get("WhereAddress");

            if (getStringAddress(whenceAddress) != null) {
                metWhence.setTextColor(getResources().getColor(R.color.colorBlack));
                metWhence.setText(getStringAddress(whenceAddress));
            }
            if (getStringAddress(whereAddress) != null) {
                metWhere.setTextColor(getResources().getColor(R.color.colorBlack));
                metWhere.setText(getStringAddress(whereAddress));
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        waitWhere = false;
        waitWhence = false;
    }

    private String getStringAddress(Address address) {
        if (address != null && address.getThoroughfare() != null) {
            String strWhereAddress = address.getThoroughfare();
            if (address.getSubThoroughfare() != null) {
                strWhereAddress += ", " + address.getSubThoroughfare();
            }
            return strWhereAddress;
        } else {
            return null;
        }
    }

    public void callTaxi(View view) {
        Intent intent = new Intent(this, Waiting.class);
        startActivity(intent);
    }
}

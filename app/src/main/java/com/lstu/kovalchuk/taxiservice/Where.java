package com.lstu.kovalchuk.taxiservice;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Where extends AppCompatActivity {

    private static final String TAG = "WhereActivity";
    private EditText etWhereAddress;
    private LinearLayout llVariant;
    private CheckBox myCity;
    private Timer timer = new Timer();
    private final long DELAY = 1000; // in ms
    private Location currentLocation;
    private Address whenceAddress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_where);

        llVariant = findViewById(R.id.whereVariants);
        myCity = findViewById(R.id.whereMyCity);
        myCity.setOnCheckedChangeListener((buttonView, isChecked) -> geoLocate());
        etWhereAddress = findViewById(R.id.whereAddress);
        etWhereAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }
            @Override
            public void onTextChanged(final CharSequence s, int start, int before,
                                      int count) {
                if(timer != null)
                    timer.cancel();
            }
            @Override
            public void afterTextChanged(final Editable s) {
                //avoid triggering event when text is too short
                if (s.length() >= 3) {

                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(Where.this::geoLocate);
                        }

                    }, DELAY);
                }
            }
        });

        Bundle arguments = getIntent().getExtras();
        if(arguments!=null){
            currentLocation = (Location) arguments.get("CurrentLocation");
            whenceAddress = (Address) arguments.get("WhenceAddress");
        }
        if(currentLocation!=null)
        {
            myCity.setVisibility(View.VISIBLE);
            myCity.setChecked(true);
        }
        else{
            myCity.setVisibility(View.GONE);
            myCity.setChecked(false);
        }
    }

    private void geoLocate() {
        Log.d(TAG, "geoLocate: search geolocating");

        llVariant.removeAllViews();
        String searchString;

        if(myCity.isChecked()){
            Geocoder geocoder = new Geocoder(Where.this);
            try {
                Address currentAddress = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1).get(0);
                searchString = currentAddress.getLocality() + ", " + etWhereAddress.getText().toString();
            }catch (IOException e){
                searchString = etWhereAddress.getText().toString();
            }
        }
        else{
            searchString = etWhereAddress.getText().toString();
        }

        Geocoder geocoder = new Geocoder(Where.this);
        List<Address> addressList = new ArrayList<>();
        try {
            addressList = geocoder.getFromLocationName(searchString, 15);
        } catch (IOException e) {
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage());
        }

        if (addressList.size() > 0) {
            for (Address address : addressList) {
                createVariantAddress(address);
            }
        }
    }

    private void createVariantAddress(Address address) {
        String street = address.getThoroughfare();

        if (street == null) return;
        LinearLayout linearLayout = new LinearLayout(this);
        TextView tvStreet = new TextView(this);
        TextView tvCityAndCoutry = new TextView(this);
        View viewLine = new View(this);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tvStreet.setLayoutParams(layoutParams);
        tvStreet.setPadding(0, 10, 0, 0);
        tvStreet.setTextSize(18);
        tvStreet.setTextColor(getResources().getColor(R.color.colorBlack));

        if (address.getSubThoroughfare() != null) {
            street += ", " + address.getSubThoroughfare();
        }
        tvStreet.setText(street);

        String globalAddress = address.getLocality() + ", " + address.getCountryName();
        tvCityAndCoutry.setLayoutParams(layoutParams);
        tvCityAndCoutry.setPadding(0,0,0,10);
        tvCityAndCoutry.setText(globalAddress);

        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Where.this, Ordering.class);
                intent.putExtra("CurrentLocation", currentLocation);
                intent.putExtra("WhenceAddress", whenceAddress);
                intent.putExtra("WhereAddress", address);
                startActivity(intent);
            }
        });
        linearLayout.addView(tvStreet);
        linearLayout.addView(tvCityAndCoutry);

        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
        viewLine.setLayoutParams(layoutParams);
        viewLine.setBackground(getDrawable(R.color.colorBlack));

        llVariant.addView(linearLayout);
        llVariant.addView(viewLine);
    }

    public void checkWhereToMap(View view) {
        Intent intent = new Intent(this, Global.class);
        startActivity(intent);
    }

    public void checkAddress(View view) {
        Intent intent = new Intent(this, Ordering.class);
        startActivity(intent);
    }
}

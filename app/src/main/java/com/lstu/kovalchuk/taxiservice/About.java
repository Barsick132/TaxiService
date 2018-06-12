package com.lstu.kovalchuk.taxiservice;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class About extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView tvAuthorInfo = findViewById(R.id.aboutText4);
        TextView versionString = findViewById(R.id.aboutText5);
        versionString.setText(BuildConfig.VERSION_NAME);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
        String authorInfo = getString(R.string.authorInfo) + dateFormat.format(new Date());
        tvAuthorInfo.setText(authorInfo);
    }
}

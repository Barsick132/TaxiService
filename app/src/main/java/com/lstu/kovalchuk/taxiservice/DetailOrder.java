package com.lstu.kovalchuk.taxiservice;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.MessageFormat;

public class DetailOrder extends AppCompatActivity {

    private TextView tvWhence;
    private TextView tvWhere;
    private TextView tvCost;
    private TextView tvTime;
    private TextView tvDistance;
    private TextView tvTimeWait;
    private TextView tvDriverName;
    private TextView tvBrandCar;
    private TextView tvColorCar;
    private TextView tvNumberCar;
    private LinearLayout llDriver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_order);

        setTheme(R.style.Widget_AppCompat_ActionBar_TabBar);
        setTitle(getResources().getString(R.string.detailOrder));

        tvWhence = findViewById(R.id.detailOrderWhence);
        tvWhere = findViewById(R.id.detailOrderWhere);
        tvCost = findViewById(R.id.detailOrderCost);
        tvTime = findViewById(R.id.detailOrderTime);
        tvDistance = findViewById(R.id.detailOrderDistance);
        tvTimeWait = findViewById(R.id.detailOrderTimeWait);
        tvDriverName = findViewById(R.id.detailOrderDriverName);
        tvBrandCar = findViewById(R.id.detailOrderBrandCar);
        tvColorCar = findViewById(R.id.detailOrderColorCar);
        tvNumberCar = findViewById(R.id.detailOrderNumberCar);
        llDriver = findViewById(R.id.detailOrderDriverLayout);

        getDetailOrder();
    }

    private void getDetailOrder() {
        String whenceStrAddress = null, whereStrAddress = null, driverName = null,
                brandCar = null, colorCar = null, numberCar = null;
        Integer approxCost = null, approxTime = null,
                approxDistance = null, approxTimeWait = null;

        Bundle arguments = getIntent().getExtras();
        // Если есть переданные аргументы
        if (arguments != null) {
            // Получаем эти аргументы как текущие координаты
            whenceStrAddress = arguments.getString("whenceStrAddress");
            whereStrAddress = arguments.getString("whereStrAddress");
            approxCost = arguments.getInt("approxCost");
            approxTime = arguments.getInt("approxTime");
            approxDistance = arguments.getInt("approxDistance");
            approxTimeWait = arguments.getInt("approxTimeWait");
            driverName = arguments.getString("driverName");
            brandCar = arguments.getString("brandCar");
            colorCar = arguments.getString("colorCar");
            numberCar = arguments.getString("numberCar");
        }
        if (whenceStrAddress != null) {
            tvWhence.setText(whenceStrAddress);
        }
        if (whereStrAddress != null) {
            tvWhere.setText(whereStrAddress);
        }
        if (approxCost != null) {
            tvCost.setText(MessageFormat.format("{0} руб.", approxCost));
        }
        if (approxDistance != null) {
            tvDistance.setText(MessageFormat.format("{0} км.",
                    Math.round(approxDistance / 100) / (double) 10));
        }
        if (approxTime != null) {
            Double hours = Math.floor(approxTime / (double) 3600);
            Double minutes = Math.floor(approxTime / (double) 60) - hours.intValue() * 60;
            String strApproxTime;
            if (hours == 0) {
                strApproxTime = MessageFormat.format("{0} мин.", minutes.intValue());
            } else {
                strApproxTime = MessageFormat.format("{0} ч. {1} мин.", hours.intValue(), minutes.intValue());
            }
            tvTime.setText(strApproxTime);
        }

        if (approxTimeWait != null) {
            Double hours = Math.floor(approxTimeWait / (double) 3600);
            Double minutes = Math.floor(approxTimeWait / (double) 60) - hours.intValue() * 60;
            String strApproxTimeWait;
            if (hours == 0) {
                strApproxTimeWait = MessageFormat.format("{0} мин.", minutes.intValue());
            } else {
                strApproxTimeWait = MessageFormat.format("{0} ч. {1} мин.", hours.intValue(), minutes.intValue());
            }
            tvTimeWait.setText(strApproxTimeWait);
        }
        if (driverName != null) {
            tvDriverName.setText(driverName);
        }
        if (brandCar != null) {
            tvBrandCar.setText(brandCar);
        }
        if (colorCar != null) {
            tvColorCar.setText(colorCar);
        }
        if (numberCar != null) {
            tvNumberCar.setText(numberCar);
        }

        if (driverName == null && brandCar == null && colorCar == null && numberCar == null) {
            llDriver.setVisibility(View.GONE);
        } else {
            llDriver.setVisibility(View.VISIBLE);
        }
    }
}

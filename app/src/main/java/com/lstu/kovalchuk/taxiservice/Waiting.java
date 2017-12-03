package com.lstu.kovalchuk.taxiservice;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import ru.yandex.yandexmapkit.MapController;
import ru.yandex.yandexmapkit.MapView;
import ru.yandex.yandexmapkit.utils.GeoPoint;

public class Waiting extends AppCompatActivity {

    private android.support.v7.widget.Toolbar toolbar;
    private DriverGPS driverGPS;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        final MapView mMapView = (MapView) findViewById(R.id.map);

// Получаем MapController
        MapController mMapController = mMapView.getMapController();

        mMapController.setZoomCurrent(17);
// Перемещаем карту на заданные координаты
        mMapController.setPositionAnimationTo(new GeoPoint(52.583556, 39.476184));

        mMapView.showZoomButtons(true);

        initToolbar();
    }

    private void initToolbar() {
        toolbar = (android.support.v7.widget.Toolbar)findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(new android.support.v7.widget.Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                return false;
            }
        });

        toolbar.inflateMenu(R.menu.menu);
    }

    public void openMenu(MenuItem item) {
        Intent intent = new Intent(this, Menu.class);
        startActivity(intent);
    }

    public void openDetailOrder(View view) {
        Intent intent = new Intent(this, DetailOrder.class);
        startActivity(intent);
    }

    public void closeOrder(View view) {
        Intent intent = new Intent(this, Global.class);
        startActivity(intent);
    }

    private void updateDriverLocation() {
        Intent intent = new Intent(this, Assessment.class);
        startActivity(intent);

    }

    public void openPhoneKeyboard(View view) {
    }
}

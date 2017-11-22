package com.lstu.kovalchuk.taxiservice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toolbar;
import ru.yandex.yandexmapkit.MapController;
import ru.yandex.yandexmapkit.MapView;
import ru.yandex.yandexmapkit.utils.GeoPoint;

public class Global extends Activity {

    private android.support.v7.widget.Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global);

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

    public void checkout(View view) {

    }

    public void openMenu(MenuItem item) {
        Intent intent = new Intent(this, Menu.class);
        startActivity(intent);
    }
}



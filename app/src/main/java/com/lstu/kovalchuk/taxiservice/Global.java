package com.lstu.kovalchuk.taxiservice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.mapview.MapView;

/*
import ru.yandex.yandexmapkit.MapController;
import ru.yandex.yandexmapkit.MapView;
import ru.yandex.yandexmapkit.utils.GeoPoint;
*/

public class Global extends Activity {

    private MapView mapview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MapKitFactory.setApiKey("ce22acd0-3f1a-4e4b-bb04-293ac4af654a");
        MapKitFactory.initialize(this);

        setContentView(R.layout.activity_global);
        mapview = (MapView)findViewById(R.id.globalMap);
        mapview.getMap().move(
                new CameraPosition(new Point(55.751574, 37.573856), 11.0f, 0.0f, 0.0f),
                new Animation(Animation.Type.SMOOTH, 0),
                null);

        /*
        final MapView mMapView = (MapView) findViewById(R.id.globalMap1);

// Получаем MapController
        MapController mMapController = mMapView.getMapController();

        mMapController.setZoomCurrent(17);
// Перемещаем карту на заданные координаты
        mMapController.setPositionAnimationTo(new GeoPoint(52.583556, 39.476184));

        mMapView.showZoomButtons(true);
        */
        initToolbar();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapview.onStop();
        MapKitFactory.getInstance().onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapview.onStart();
        MapKitFactory.getInstance().onStart();
    }

    private void initToolbar() {
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.globalToolbar1);
        toolbar.setOnMenuItemClickListener(menuItem -> false);

        toolbar.inflateMenu(R.menu.menu);
    }

    public void checkout(View view) {
        Intent intent = new Intent(this, Where.class);
        startActivity(intent);
    }

    public void openMenu(MenuItem item) {
        Intent intent = new Intent(this, Menu.class);
        startActivity(intent);
    }

    public void checkWhence(View view) {
        Intent intent = new Intent(this, Whence.class);
        startActivity(intent);
    }
}



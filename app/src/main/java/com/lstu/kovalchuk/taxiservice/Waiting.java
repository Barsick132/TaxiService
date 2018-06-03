package com.lstu.kovalchuk.taxiservice;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Waiting extends AppCompatActivity implements OnMapReadyCallback {

    // Обработчик завершения инициализации карты
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        // Если есть права доступа к даным местоположения
        if (mLocationPermissionGranted) {
            // Получаем и отображаем текущее местоположение на карте
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }

            mMap.setMyLocationEnabled(true); // Делаем доступной кнопку определения местоположения на карте
        }
    }

    public static final String TAG = "Waiting";
    public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;

    private boolean searchDriver;
    private String orderID;
    private Order order = null;
    private DriverGPS driverGPS;
    private DocumentReference docRef;

    //vars
    private Boolean mLocationPermissionGranted = false;
    private TextView tvSearchDriver;
    private Button btnCall;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location currentLocation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        searchDriver = true;
        tvSearchDriver = findViewById(R.id.waitingSearchDriver);
        btnCall = findViewById(R.id.waitingBtnCall);
        btnCall.setVisibility(View.GONE);

        // Получение прав доступа о местоположении, отрисовка карты
        // установка текущего местоположения на карте
        getLocationPermission();

        initToolbar();

        Bundle arguments = getIntent().getExtras();
        // Если есть переданные аргументы
        if (arguments != null) {
            // Получаем эти аргументы как текущие координаты
            orderID = arguments.getString("OrderID");
        }
        if (orderID != null) {
            getOrder();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        docRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Toast.makeText(Waiting.this, "Что-то пошло не так :( Проверьте соединение с сетью", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onEvent: Ошибка при прослушивании БД");
                return;
            }

            try {
                if (documentSnapshot.exists()) {
                    order = documentSnapshot.toObject(Order.class);

                    if (order.getDriverUID() != null && searchDriver) {
                        tvSearchDriver.setVisibility(View.GONE);
                        Toast.makeText(Waiting.this, "Водитель найден :)", Toast.LENGTH_SHORT).show();
                        searchDriver = false;
                        btnCall.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(Waiting.this, "Заказ не найден", Toast.LENGTH_SHORT).show();
                }
            }
            catch (Exception ex){
                Log.d(TAG, "onStart: " + ex.getMessage());
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences sharedPref = getSharedPreferences("com.lstu.kovalchuk.taxiservice", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("OrderID", orderID);
        editor.apply();
    }

    // Метод получения данных местоположения для отображения конкретного места на карте при первом запуске
    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: получение координат текущего местоположения");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            // Если есть право на получение данных местоположения
            if (mLocationPermissionGranted) {
                // Пытаемся их получить
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Если удалось определить местоположение
                        Log.d(TAG, "onComplete: местоположение определено!");
                        currentLocation = (Location) task.getResult();

                        // Меняем местоположение камеры
                        moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                    } else {
                        // Если НЕ удалось определить местоположение
                        Log.d(TAG, "onComplete: местоположение НЕ определено");
                        Toast.makeText(Waiting.this, "Не удалось определить текущее местоположение", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    // Перемещаем камеру по заданным координатам с заданным зумом
    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCamera: перемещаем камеру на: широту: " + latLng.latitude + ", долготу: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    // Инициализация карты
    private void initMap() {
        Log.d(TAG, "initMap: инициализация карты");
        // Отображение фрагмента с картой
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.waitingMap);
        mapFragment.getMapAsync(Waiting.this);
    }

    // Получение прав доступа к данным местоположения
    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: получение прав доступа к данным местоположения");
        String[] permission = {FINE_LOCATION, COARSE_LOCATION};

        // Проверка предоставления прав доступа к данным местоположения
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Если права предоставлены инициализируем карту
                mLocationPermissionGranted = true;
                initMap();
            } else {
                // Если права не были предоставлены
                // Вызов диалогового окна для предоставления прав доступа к данным местоположения
                ActivityCompat.requestPermissions(this,
                        permission,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            // Если права не были предоставлены
            // Вызов диалогового окна для предоставления прав доступа к данным местоположения
            ActivityCompat.requestPermissions(this,
                    permission,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void getOrder() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        docRef = db.collection("orders").document(orderID);
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> order = documentSnapshot.toObject(Order.class))
                .addOnFailureListener(aVoid -> {
                    Toast.makeText(Waiting.this,
                            "Не удалось получить заказ. Проверьте соединение с сетью",
                            Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "getOrder: не удалось проверить статус заказа в БД");
                });
    }

    private void initToolbar() {
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.WaitingToolbar);
        toolbar.setOnMenuItemClickListener(menuItem -> false);

        toolbar.inflateMenu(R.menu.menu);
    }

    public void openMenu(MenuItem item) {
        Intent intent = new Intent(this, Menu.class);
        startActivity(intent);
    }

    public void openDetailOrder(View view) {
        Intent intent = new Intent(this, DetailOrder.class);
        intent.putExtra("whenceStrAddress", order.getWhenceAddress());
        intent.putExtra("whereStrAddress", order.getWhereAddress());
        intent.putExtra("approxCost", order.getApproxCost());
        intent.putExtra("approxTime", order.getApproxTimeToDest());
        intent.putExtra("approxDistance", order.getApproxDistanceToDest());
        startActivity(intent);
    }

    public void closeOrder(View view) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("orders").document(orderID)
                .update("cancel", true);

        orderID = null;
        SharedPreferences sharedPref = getSharedPreferences("com.lstu.kovalchuk.taxiservice", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("OrderID", orderID);
        editor.apply();

        Intent intent = new Intent(Waiting.this, Global.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    public void openPhoneKeyboard(View view) {
    }
}

package com.lstu.kovalchuk.taxiservice;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Global extends AppCompatActivity implements OnMapReadyCallback {

    // Обработчик завершения инициализации карты
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        // Если есть права доступа к даным местоположения
        if (mLocationPermissionGranted) {
            // Получаем и отображаем текущее местоположение на карте
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }

            mMap.setMyLocationEnabled(true); // Делаем доступной кнопку определения местоположения на карте
            mMap.setOnCameraIdleListener(this::showAddress); // Запускаем слушателя изменения положения камеры на карте
        }
    }

    private static final String TAG = "GlobalActivity";
    public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;

    //vars
    private Boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private TextView tvAddress;
    private Toolbar toolbar;
    private Button btnGlobal;
    private Location currentLocation;
    private Address globalAddress;
    private String checkPlace = null;

    // Обработчик создания текущей активити
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global);

        toolbar = findViewById(R.id.globalToolbar1);
        btnGlobal = findViewById(R.id.globalButton);
        tvAddress = findViewById(R.id.globalTVAddress);

        // Получение прав доступа о местоположении, отрисовка карты
        // установка текущего местоположения на карте
        getLocationPermission();

        // Инициализация тулбара
        initToolbar();

        Bundle arguments = getIntent().getExtras();
        if (arguments != null) {
            // Если передан CheckPlace, значит указывается место на карте
            if (arguments.getString("CheckPlace") != null) {
                checkPlace = arguments.getString("CheckPlace");
                updateUI();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences sharedPref = getSharedPreferences("com.lstu.kovalchuk.taxiservice", Context.MODE_PRIVATE);
        String orderID = sharedPref.getString("OrderID", null);
        String isAssessment = sharedPref.getString("assessment", null);
        if(orderID!=null){
            if(isAssessment==null) {
                Intent intent = new Intent(Global.this, Waiting.class);
                intent.putExtra("OrderID", orderID);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }else {
                Intent intent = new Intent(Global.this, Assessment.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        }

    }

    // Метод изменения интерфейса
    private void updateUI() {
        toolbar.setVisibility(View.GONE);
        btnGlobal.setText("Подтвердить");
    }

    // Обработчик изменений местоположения камеры на карте
    // Отображает адрес центра карты
    private void showAddress() {
        Log.d(TAG, "showAddress: отобразить адрес центра карты");

        List<Address> addressList = new ArrayList<>();
        Geocoder geocoder = new Geocoder(Global.this);
        try {
            //Получаем информацию по координатам центра карты
            addressList = geocoder.getFromLocation(mMap.getCameraPosition().target.latitude, mMap.getCameraPosition().target.longitude, 1);
        } catch (IOException e) {
            Log.e(TAG, "showAddress: IOException: " + e.getMessage());
            Toast.makeText(Global.this, "Не удалось определить адрес. Проверьте соединение с сетью", Toast.LENGTH_LONG).show();
        }

        // Если есть хотя бы улица
        if (addressList.size() > 0 && addressList.get(0).getThoroughfare() != null) {
            // Выводи название улицы
            Address address = addressList.get(0);
            String street = address.getThoroughfare();

            // Также сокращаем слово улица до ул.
            Pattern pattern = Pattern.compile("^улица\\b.+");
            if (pattern.matcher(street).matches()) {
                street = street.replace("улица ", "ул. ");
            }
            pattern = Pattern.compile(".+\\bулица$");
            if (pattern.matcher(street).matches()) {
                street = street.replace(" улица", " ул.");
            }

            // Если есть информация о номере дома, то выводим ее после названия улицы
            if (address.getSubThoroughfare() != null) {
                street += ", " + address.getSubThoroughfare();
            }
            globalAddress = address;
            tvAddress.setText(street);
        }
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
                        showAddress();
                    } else {
                        // Если НЕ удалось определить местоположение
                        Log.d(TAG, "onComplete: местоположение НЕ определено");
                        Toast.makeText(Global.this, "Не удалось определить текущее местоположение", Toast.LENGTH_SHORT).show();
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
                .findFragmentById(R.id.globalMap);
        mapFragment.getMapAsync(Global.this);
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

    // Обработчик окончания вызова диалогового окна предоставления прав доступа
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            // Если хоть какое-то право отсутствует
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    // Если все права были предоставлены
                    mLocationPermissionGranted = true;
                    initMap();
                }

            }
        }
    }

    // Инициализация тулбара
    private void initToolbar() {
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.globalToolbar1);
        toolbar.setOnMenuItemClickListener(menuItem -> false);

        toolbar.inflateMenu(R.menu.menu);
    }


    // Обработчик нажатия кнопки
    public void checkout(View view) {
        // Если НЕ указываем место на карте
        if (checkPlace == null) {
            // Если есть права на получение местоположения, можно начать
            // оформление заказа
            if (mLocationPermissionGranted) {
                // Вызываем активити указания адреса отправления
                // Передаем данные текущего местоположения и адрес отправления
                Intent intent = new Intent(this, Where.class);
                intent.putExtra("CurrentLocation", currentLocation);
                intent.putExtra("WhenceAddress", globalAddress);
                intent.putExtra("changeAddress", false);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Включите геолокацию", Toast.LENGTH_LONG).show();
            }
        } else {
            // Если указываем место на карте для активити Whence
            if (checkPlace.equals("Whence")) {
                // При наличии прав доступа к геолокации передаем адрес
                // отправления и возвращаемся к Whence
                Intent intent = new Intent(this, Whence.class);
                if (mLocationPermissionGranted) {
                    intent.putExtra("WhenceAddress", globalAddress);
                } else {
                    Toast.makeText(this, "Включите геолокацию", Toast.LENGTH_LONG).show();
                }
                // Если прав нет, то возвращаемся без информации
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            }
            // Если указываем место на карте для активити Where
            if (checkPlace.equals("Where")) {
                // При наличии прав доступа к геолокации передаем адрес
                // отправления и возвращаемся к Where
                Intent intent = new Intent(this, Where.class);
                if (mLocationPermissionGranted) {
                    intent.putExtra("WhereAddress", globalAddress);
                } else {
                    Toast.makeText(this, "Включите геолокацию", Toast.LENGTH_LONG).show();
                }
                // Если прав нет, то возвращаемся без информации
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            }
        }
    }

    // Обработчик открытия меню
    public void openMenu(MenuItem item) {
        Intent intent = new Intent(this, Menu.class);
        startActivity(intent);
    }
}



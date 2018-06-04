package com.lstu.kovalchuk.taxiservice;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
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
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.gson.Gson;
import com.google.maps.android.PolyUtil;
import com.lstu.kovalchuk.taxiservice.mapapi.Route;
import com.lstu.kovalchuk.taxiservice.mapapi.RouteResponse;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
    public static final String CALL_PHONE = Manifest.permission.CALL_PHONE;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;

    private boolean searchDriver;
    private boolean markerCreated;
    private boolean routePrinted;
    private MarkerOptions markerOptions;
    private Marker markerDriver;
    private String orderID;
    private Order order = null;
    private DriverGPS driverGPS;
    private Driver driver;
    private DocumentReference docRef;
    private FirebaseFirestore db;
    BitmapDescriptor icon, iconWhence, iconWhere;

    //vars
    private Boolean mLocationPermissionGranted = false;
    private TextView tvSearchDriver;
    private Button btnCall;
    private Button btnGoingOut;
    private Button btnCancel;
    private Button btnArrived;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location currentLocation;


    // Событие создания активити
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        db = FirebaseFirestore.getInstance();
        icon = bitmapDescriptorFromVector(this, R.drawable.ic_cab);
        iconWhence = bitmapDescriptorFromVector(this, R.drawable.ic_place_accent_24dp);
        iconWhere = bitmapDescriptorFromVector(this, R.drawable.ic_place_blue_24dp);

        searchDriver = true;
        markerCreated = false;
        routePrinted = false;
        tvSearchDriver = findViewById(R.id.waitingSearchDriver);
        btnCall = findViewById(R.id.waitingBtnCall);
        btnCall.setVisibility(View.GONE);
        btnCancel = findViewById(R.id.waitinfBtnCancel);
        btnGoingOut = findViewById(R.id.waitingBtnGoingOut);
        btnGoingOut.setVisibility(View.GONE);
        btnGoingOut.setOnClickListener(view -> {
            java.util.Map<String, Object> mapCameOut = new java.util.HashMap<>();
            mapCameOut.put("clientCameOut", true);

            db.collection("orders").document(orderID)
                    .update(mapCameOut)
                    .addOnSuccessListener(vVoid -> {
                        Log.d(TAG, "onCreate: водитель оповещен о выходе клиента");
                        btnGoingOut.setVisibility(View.GONE);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "onCreate: " + e.getMessage());
                        Toast.makeText(Waiting.this, "Не удалось оповестить водителя о выходе", Toast.LENGTH_SHORT).show();
                    });
        });
        btnArrived = findViewById(R.id.waitingBtnArrived);
        btnArrived.setVisibility(View.GONE);

        getCallingPermissionGranted();
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

    // Функция получения данных о заказе
    private void getOrder() {
        docRef = db.collection("orders").document(orderID);
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    order = documentSnapshot.toObject(Order.class);
                    /*try {
                        Timestamp ts = new Timestamp(new Date());
                        DriverGPS driverGPS = new DriverGPS(ts, new GeoPoint(52.585503, 39.474788));
                        db.collection("driverGPS").document(order.getClientUID()).set(driverGPS)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "getOrder: радуемся");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "getOrder: " + e.getMessage());
                                });
                    }
                    catch (Exception ex){
                        Log.e(TAG, "getOrder: " + ex.getMessage());
                    }*/
                })
                .addOnFailureListener(aVoid -> {
                    Toast.makeText(Waiting.this,
                            "Не удалось получить заказ. Проверьте соединение с сетью",
                            Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "getOrder: не удалось проверить статус заказа в БД");
                });
    }

    // Функция получения BitmapDescriptor по id векторного изображения в drawable
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable icon = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(icon.getIntrinsicWidth(), icon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        icon.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    // Метод запуска активности, в котором создается и выполняется слушатель заказа
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
                        btnCall.setVisibility(View.VISIBLE);
                        searchDriver = false;

                        getDriver();
                        waitingDriver();
                    }
                    if (order.getDriverUID() == null && !searchDriver) {
                        tvSearchDriver.setVisibility(View.VISIBLE);
                        Toast.makeText(Waiting.this, "Водитель отказался от заказа", Toast.LENGTH_SHORT).show();
                        btnCall.setVisibility(View.GONE);
                        searchDriver = true;
                        driver = null;
                    }

                    if (order.isDriverArrived() && !order.isClientCameOut()) {
                        btnGoingOut.setVisibility(View.VISIBLE);
                        btnCancel.setVisibility(View.GONE);

                        android.app.Notification notification = new android.app.Notification.Builder(Waiting.this)
                                .setContentTitle("Водитель приехал")
                                .setContentText("Такси ожидает вас на улице.")
                                .setSmallIcon(R.drawable.ic_cab)
                                .build();
                        android.support.v4.app.NotificationManagerCompat notificationManager = android.support.v4.app.NotificationManagerCompat.from(this);
                        notificationManager.notify(0, notification);
                    }

                    if (order.isDriverArrived() && order.isClientCameOut()) {
                        btnGoingOut.setVisibility(View.GONE);
                        btnCancel.setVisibility(View.GONE);
                    }

                    if (order.getDTbegin() != null) {
                        if (!routePrinted) {
                            // Водитель начал движение по маршруту
                            String position = order.getWhenceGeoPoint().getLatitude() + "," + order.getWhenceGeoPoint().getLongitude();
                            String destination = order.getWhereGeoPoint().getLatitude() + "," + order.getWhereGeoPoint().getLongitude();
                            routePrint(position, destination);
                            btnCall.setVisibility(View.GONE);
                            btnArrived.setVisibility(View.VISIBLE);
                            btnGoingOut.performClick();
                        }
                    }
                } else {
                    Toast.makeText(Waiting.this, "Заказ не найден", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception ex) {
                Log.e(TAG, "onStart: " + ex.getMessage());
            }
        });
    }

    // Функция для совершения Http запросов
    private static class GetQuery {
        OkHttpClient client = new OkHttpClient();

        void run(String url, Callback callback) {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            client.newCall(request).enqueue(callback);
        }
    }

    // Функция отображения маршрута на карте
    private void routePrint(String origin, String destination) {
        GetQuery query = new GetQuery();
        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + origin +
                "&destination=" + destination +
                "&sensor=true" +
                "&language=ru" +
                "&key=" + getResources().getString(R.string.google_maps_webkey);

        query.run(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(Waiting.this, "Не удалось отобразить маршрут", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: не удалось получить маршруты в формате json");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String jsonString = response.body().string();
                    Gson g = new Gson();
                    RouteResponse routeResponse = g.fromJson(jsonString, RouteResponse.class);

                    if (routeResponse.getStatus().equals("OK")) {
                        Waiting.this.runOnUiThread(() -> {
                            try {
                                Route minTimeRoute = routeResponse.getRoutes().get(0);
                                for (Route route : routeResponse.getRoutes()) {
                                    if (route.getLegs().get(0).getDuration().getValue() < minTimeRoute.getLegs().get(0).getDuration().getValue()) {
                                        minTimeRoute = route;
                                    }
                                }

                                List<LatLng> mPoints = PolyUtil.decode(minTimeRoute.getOverviewPolyline().getPoints());

                                PolylineOptions line = new PolylineOptions();
                                line.width(6f).color(R.color.colorAccent);
                                LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
                                for (int i = 0; i < mPoints.size(); i++) {
                                    if (i == 0) {
                                        MarkerOptions startMarkerOptions = new MarkerOptions()
                                                .position(mPoints.get(i))
                                                .icon(iconWhence)
                                                .title("A");
                                        mMap.addMarker(startMarkerOptions);
                                    } else if (i == mPoints.size() - 1) {
                                        MarkerOptions endMarkerOptions = new MarkerOptions()
                                                .position(mPoints.get(i))
                                                .icon(iconWhere)
                                                .title("B");
                                        mMap.addMarker(endMarkerOptions);
                                    }
                                    line.add(mPoints.get(i));
                                    latLngBuilder.include(mPoints.get(i));
                                }
                                mMap.addPolyline(line);
                                int size = getResources().getDisplayMetrics().widthPixels;
                                LatLngBounds latLngBounds = latLngBuilder.build();
                                CameraUpdate track = CameraUpdateFactory.newLatLngBounds(latLngBounds, size, size, 25);
                                mMap.moveCamera(track);
                                routePrinted = true;
                            } catch (Exception ex) {
                                Log.e(TAG, "onResponse: " + ex.getMessage());
                            }
                        });
                    }
                } catch (Exception ex) {
                    Log.d(TAG, "onResponse: ошибка при получении маршрута в формате json");
                    Toast.makeText(Waiting.this, "Не удалось отобразить маршрут", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Функция получения информации о видителе
    private void getDriver() {
        db.collection("drivers").document(order.getDriverUID()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    driver = documentSnapshot.toObject(Driver.class);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "getDriver: " + e.getMessage());
                    Toast.makeText(Waiting.this, "Не удалось получить данные о водителе", Toast.LENGTH_SHORT).show();
                });
    }

    // Функция создания слушателя gps координат водителя
    private void waitingDriver() {
        DocumentReference drWaitDriver = db.collection("driverGPS").document(order.getDriverUID());
        drWaitDriver.addSnapshotListener(((documentSnapshot, e) -> {
            if (e != null) {
                Log.e(TAG, "waitingDriver: ошибка при получении данных GPS водителя");
                return;
            }

            if (driver == null) {
                if (markerDriver != null) {
                    markerDriver.remove();
                    markerCreated = false;
                }
                return;
            }

            // Получаем данные GPS водителя и отображаем на карте
            try {
                if (documentSnapshot.exists()) {
                    driverGPS = documentSnapshot.toObject(DriverGPS.class);

                    LatLng llDriverPosition;
                    if (driverGPS != null && !markerCreated) {
                        llDriverPosition = new LatLng(driverGPS.getGeoPoint().getLatitude(), driverGPS.getGeoPoint().getLongitude());
                        markerOptions = new MarkerOptions()
                                .position(llDriverPosition)
                                .icon(icon);
                        markerDriver = mMap.addMarker(markerOptions);
                        moveCamera(llDriverPosition, DEFAULT_ZOOM);
                        markerCreated = true;
                    }
                    if (driverGPS != null && markerCreated) {
                        llDriverPosition = new LatLng(driverGPS.getGeoPoint().getLatitude(), driverGPS.getGeoPoint().getLongitude());
                        markerDriver.setPosition(llDriverPosition);
                    }
                } else {
                    Log.d(TAG, "waitingDriver: GPS координаты не были получены");
                }
            } catch (Exception ex) {
                Log.e(TAG, "waitingDriver: " + ex.getMessage());
            }

        }));
    }

    // Событие остановки активити
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

    // Получение прав для совершения звонков
    private void getCallingPermissionGranted() {
        Log.d(TAG, "getCallingPermissionGranted: получение прав доступа для совершения звонков");
        String[] permission = {CALL_PHONE};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // Если права не были предоставлены
            // Вызов диалогового окна для предоставления прав доступа для совершения звонков
            ActivityCompat.requestPermissions(this,
                    permission,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    // Инициализация Toolbar
    private void initToolbar() {
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.WaitingToolbar);
        toolbar.setOnMenuItemClickListener(menuItem -> false);

        toolbar.inflateMenu(R.menu.menu);
    }

    // Событие открытия меню
    public void openMenu(MenuItem item) {
        Intent intent = new Intent(this, Menu.class);
        startActivity(intent);
    }

    // Событие открытия подробностей о заказе
    public void openDetailOrder(View view) {
        Intent intent = new Intent(this, DetailOrder.class);
        intent.putExtra("whenceStrAddress", order.getWhenceAddress());
        intent.putExtra("whereStrAddress", order.getWhereAddress());
        intent.putExtra("approxCost", order.getApproxCost());
        intent.putExtra("approxTime", order.getApproxTimeToDest());
        intent.putExtra("approxDistance", order.getApproxDistanceToDest());

        if (driver != null) {
            intent.putExtra("approxTimeWait", order.getTimeWaiting());
            intent.putExtra("driverName", driver.getFullName());
            intent.putExtra("brandCar", driver.getBrandCar());
            intent.putExtra("colorCar", driver.getColorCar());
            intent.putExtra("numberCar", driver.getNumberCar());
        }
        startActivity(intent);
    }

    // Событие отмены заказа
    public void closeOrder(View view) {
        db = FirebaseFirestore.getInstance();
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

    // Событие звнока водителю
    public void openPhoneKeyboard(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(Waiting.this, "Нет права для совершения вызова", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + driver.getPhone()));
        startActivity(intent);
    }

    public void completeOrder(View view) {
        Timestamp dtend = new Timestamp(new Date());
        Double totalCost = 50 + ((dtend.getSeconds()-order.getDTbegin().getSeconds()) / (double) 60) * 7 +
                (order.getApproxDistanceToDest() / (double) 1000) * 7;
        totalCost = Math.ceil(totalCost);

        Map<String, Object> updateCompleteOrder = new HashMap<>();
        updateCompleteOrder.put("dtend", dtend);
        updateCompleteOrder.put("totalCost", totalCost.intValue());

        db.collection("orders").document(order.getID())
                .update(updateCompleteOrder)
                .addOnSuccessListener(vVoid -> {
                    Intent intent = new Intent(Waiting.this, Assessment.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "completeOrder: " + e.getMessage());
                    Toast.makeText(Waiting.this, "Не удалось завершить заказа. Проверьте соединение с сетью и попробуйте снова", Toast.LENGTH_SHORT).show();
                });
    }
}

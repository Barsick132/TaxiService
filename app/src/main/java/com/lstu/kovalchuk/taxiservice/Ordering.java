package com.lstu.kovalchuk.taxiservice;

import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.io.Resources;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.gson.Gson;
import com.lstu.kovalchuk.taxiservice.mapapi.Leg;
import com.lstu.kovalchuk.taxiservice.mapapi.Route;
import com.lstu.kovalchuk.taxiservice.mapapi.RouteResponse;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Ordering extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = "Ordering";

    private boolean waitWhere = false;
    private boolean waitWhence = false;

    private MaterialEditText metWhence;
    private MaterialEditText metEntranceWhence;
    private MaterialEditText metWhere;
    private MaterialEditText metEntranceWhere;
    private Spinner spin;
    private EditText etComment;
    private TextView tvOrderingCost;
    private SwipeRefreshLayout srlRefresh;
    private Order order = null;

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
            if (hasFocus) {
                Intent intent = new Intent(Ordering.this, Whence.class);
                intent.putExtra("CurrentLocation", currentLocation);
                startActivity(intent);
                waitWhence = true;
            }
        });
        metWhence.setOnClickListener(v -> {
            if (!waitWhence) {
                Intent intent = new Intent(Ordering.this, Whence.class);
                intent.putExtra("CurrentLocation", currentLocation);
                startActivity(intent);
            }
        });
        metEntranceWhence = findViewById(R.id.orderingWhence2);
        metWhere = findViewById(R.id.orderingWhere1);
        metWhere.setKeyListener(null);
        metWhere.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                Intent intent = new Intent(Ordering.this, Where.class);
                intent.putExtra("CurrentLocation", currentLocation);
                intent.putExtra("changeAddress", true);
                startActivity(intent);
                waitWhere = true;
            }
        });
        metWhere.setOnClickListener(v -> {
            if (!waitWhere) {
                Intent intent = new Intent(Ordering.this, Where.class);
                intent.putExtra("CurrentLocation", currentLocation);
                intent.putExtra("changeAddress", true);
                startActivity(intent);
            }
        });
        metEntranceWhere = findViewById(R.id.orderingWhere2);
        tvOrderingCost = findViewById(R.id.orderingCost);
        etComment = findViewById(R.id.orderingComment);

        srlRefresh = findViewById(R.id.orderingRefresh);
        srlRefresh.setOnRefreshListener(this);
        srlRefresh.setColorSchemeColors(getResources().getColor(R.color.colorAccent));

        spin = findViewById(R.id.orderingSpinnerList);

        ArrayAdapter<String> namesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.spinner_array));

        namesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(namesAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        waitWhere = false;
        waitWhence = false;

        Address tmpWhere, tmpWhence;
        Location tmpCurrentLocation;

        Bundle arguments = getIntent().getExtras();
        if (arguments != null) {
            tmpCurrentLocation = (Location) arguments.get("CurrentLocation");
            tmpWhence = (Address) arguments.get("WhenceAddress");
            tmpWhere = (Address) arguments.get("WhereAddress");

            if (tmpCurrentLocation != null) currentLocation = tmpCurrentLocation;
            if (tmpWhence != null) whenceAddress = tmpWhence;
            if (tmpWhere != null) whereAddress = tmpWhere;

            if (getStringAddress(whenceAddress) != null) {
                metWhence.setTextColor(getResources().getColor(R.color.colorBlack));
                metWhence.setText(getStringAddress(whenceAddress));
            }
            if (getStringAddress(whereAddress) != null) {
                metWhere.setTextColor(getResources().getColor(R.color.colorBlack));
                metWhere.setText(getStringAddress(whereAddress));
            }
        }

        String position = whenceAddress.getLatitude() + "," + whenceAddress.getLongitude();
        String destination = whereAddress.getLatitude() + "," + whereAddress.getLongitude();
        GetRoute(position, destination, "true", "ru");
    }

    @Override
    public void onRefresh() {
        srlRefresh.setRefreshing(true);
        srlRefresh.postDelayed(() -> {
            srlRefresh.setRefreshing(false);

            String position = whenceAddress.getLatitude() + "," + whenceAddress.getLongitude();
            String destination = whereAddress.getLatitude() + "," + whereAddress.getLongitude();
            GetRoute(position, destination, "true", "ru");
        }, 3000);
    }

    private static class GetQuery {
        OkHttpClient client = new OkHttpClient();

        void run(String url, Callback callback) {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            client.newCall(request).enqueue(callback);
        }
    }

    public void GetRoute(String origin, String destination, String sensor, String language) {
        GetQuery query = new GetQuery();
        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + origin +
                "&destination=" + destination +
                "&sensor=" + sensor +
                "&language=" + language +
                "&key=" + getResources().getString(R.string.google_maps_webkey);

        query.run(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(Ordering.this, "Проверьте соединение с сетью", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: не удалось получить маршруты в формате json");
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String jsonString = response.body().string();
                    Gson g = new Gson();
                    RouteResponse routeResponse = g.fromJson(jsonString, RouteResponse.class);

                    if (routeResponse.getStatus().equals("OK")) {
                        Ordering.this.runOnUiThread(() -> {
                            Double approxCost = (double) 50;

                            Route minTimeRoute = routeResponse.getRoutes().get(0);
                            for (Route route : routeResponse.getRoutes()) {
                                if (route.getLegs().get(0).getDuration().getValue() < minTimeRoute.getLegs().get(0).getDuration().getValue()) {
                                    minTimeRoute = route;
                                }
                            }

                            approxCost += ((minTimeRoute.getLegs().get(0).getDuration().getValue() / (double) 60) * 7) +
                                    ((minTimeRoute.getLegs().get(0).getDistance().getValue() / (double) 1000) * 7);
                            approxCost = Math.ceil(approxCost);


                            tvOrderingCost.setText(MessageFormat.format("{0} руб.", approxCost.intValue()));
                            order = new Order();
                            order.setApproxCost(approxCost.intValue()); // Записали приблизительную стоимость в заказ
                            order.setApproxTimeToDest(minTimeRoute.getLegs().get(0).getDuration().getValue());
                            order.setApproxDistanceToDest(minTimeRoute.getLegs().get(0).getDistance().getValue());
                        });
                    }
                } catch (Exception ex) {
                    Log.d(TAG, "onResponse: ошибка при получении маршрута в формате json");
                    Toast.makeText(Ordering.this, "Проверьте соединение с сетью", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // При создании второго экземпляра данного активити,
    // необходимо обновить intent для передаачи параметров
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
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
        if (order == null) {
            Toast.makeText(this,
                    "Сумма не определена приблизительная сумма. Проверьте соединение и обновите страницу",
                    Toast.LENGTH_SHORT).show();
            Log.d(TAG, "callTaxi: order==null");
            return;
        }
        if (getStringAddress(whenceAddress) == null || getStringAddress(whereAddress) == null) {
            Toast.makeText(this,
                    "Адрес не определен. Проверьте соединение и обновите страницу",
                    Toast.LENGTH_SHORT).show();
            Log.d(TAG, "callTaxi: адрес не определен");
            return;
        }
        String sWhenceAddress = whenceAddress.getLocality() + ", " + getStringAddress(whenceAddress);
        if (!metEntranceWhence.getText().toString().equals(""))
            sWhenceAddress += ", п. " + metEntranceWhence.getText().toString();

        String sWhereAddress = whereAddress.getLocality() + ", " + getStringAddress(whereAddress);
        if (!metEntranceWhere.getText().toString().equals(""))
            sWhereAddress += ", п. " + metEntranceWhere.getText().toString();

        boolean cashlessPay = false;
        if (spin.getSelectedItemPosition() == 1) cashlessPay = true;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String orderID = db.collection("orders").document().getId();

        order.assembleOrder(orderID, FirebaseAuth.getInstance().getUid(),
                new GeoPoint(whenceAddress.getLatitude(), whenceAddress.getLongitude()),
                sWhenceAddress,
                new GeoPoint(whereAddress.getLatitude(), whereAddress.getLongitude()),
                sWhereAddress,
                cashlessPay, etComment.getText().toString());

        db.collection("orders").document(orderID).set(order)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "callTaxi: заказ записан в БД");
                    Intent intent = new Intent(Ordering.this, Waiting.class);
                    intent.putExtra("OrderID", orderID);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(aVoid -> {
                    Toast.makeText(Ordering.this,
                            "Не удалось отправить заказ. Проверьте соединение с сетью",
                            Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "callTaxi: не удалось записать заказ в БД");
                });
    }
}

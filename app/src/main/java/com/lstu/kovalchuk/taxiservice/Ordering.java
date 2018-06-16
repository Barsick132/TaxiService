package com.lstu.kovalchuk.taxiservice;

import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Ordering extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = "Ordering";

    private boolean waitWhere = false;
    private boolean waitWhence = false;

    private ScrollView svScrollView;
    private LinearLayout llProgressBar;
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

        svScrollView = findViewById(R.id.orderingScrollView);
        llProgressBar = findViewById(R.id.orderingProgressBar);

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
        GetRoute(position, destination);
    }

    @Override
    public void onRefresh() {
        srlRefresh.setRefreshing(true);
        srlRefresh.postDelayed(() -> {
            srlRefresh.setRefreshing(false);

            String position = whenceAddress.getLatitude() + "," + whenceAddress.getLongitude();
            String destination = whereAddress.getLatitude() + "," + whereAddress.getLongitude();
            GetRoute(position, destination);
        }, 3000);
    }

    private static class GetQuery {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();

        void run(String url, Callback callback) {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            client.newCall(request).enqueue(callback);
        }
    }

    public void GetRoute(String origin, String destination) {
        GetQuery query = new GetQuery();
        String url = "https://taxiserviceproject-92fe6.appspot.com?" +
                "position=" + origin +
                "&destination=" + destination;

        query.run(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                try {
                    runOnUiThread(() -> Toast.makeText(Ordering.this,
                            "Проверьте соединение с сетью", Toast.LENGTH_SHORT).show());
                    Log.d(TAG, "onFailure: не удалось получить маршруты в формате json");
                } catch (Exception ex) {
                    Log.e(TAG, "onFailure: " + ex.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String jsonString = response.body().string();
                    Gson g = new Gson();
                    RespCounterCost routeResponse = g.fromJson(jsonString, RespCounterCost.class);

                    if (routeResponse.getStatus().equals("OK")) {
                        Ordering.this.runOnUiThread(() -> {
                            tvOrderingCost.setText(MessageFormat.format("{0} руб.", routeResponse.getCost()));
                            order = new Order();
                            order.setApproxCost(routeResponse.getCost()); // Записали приблизительную стоимость в заказ
                            order.setApproxTimeToDest(routeResponse.getTime());
                            order.setApproxDistanceToDest(routeResponse.getDistance());
                        });
                    }
                    if (routeResponse.getStatus().equals("FAIL")) {
                        Log.d(TAG, "onResponse: ошибка на сервере при определении параметров");
                        runOnUiThread(() -> Toast.makeText(Ordering.this, "Ошибка расчета стоимости на сервере", Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception ex) {
                    Log.d(TAG, "onResponse: ошибка при получении маршрута в формате json");
                    runOnUiThread(() -> Toast.makeText(Ordering.this, "Проверьте соединение с сетью", Toast.LENGTH_SHORT).show());
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

    private void showProgressBar(boolean isShow) {
        if (isShow) {
            svScrollView.setVisibility(View.GONE);
            llProgressBar.setVisibility(View.VISIBLE);
        } else {
            svScrollView.setVisibility(View.VISIBLE);
            llProgressBar.setVisibility(View.GONE);
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

        String position = whenceAddress.getLatitude() + "," + whenceAddress.getLongitude();
        String destination = whereAddress.getLatitude() + "," + whereAddress.getLongitude();
        createOrder(FirebaseAuth.getInstance().getUid(), sWhenceAddress, position,
                sWhereAddress, destination, cashlessPay, etComment.getText().toString());

        /*
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
        */
    }

    private void createOrder(String clientUID, String whenceAddress, String whenceGeoPoint,
                             String whereAddress, String whereGeoPoint,
                             boolean cashlessPay, String comment) {
        showProgressBar(true);

        GetQuery query = new GetQuery();
        String url = "https://taxiserviceproject-92fe6.appspot.com/createOrder?" +
                "clientUID=" + clientUID +
                "&whenceAddress=" + whenceAddress +
                "&whenceGeoPoint=" + whenceGeoPoint +
                "&whereAddress=" + whereAddress +
                "&whereGeoPoint=" + whereGeoPoint +
                "&cashlessPay=" + cashlessPay +
                "&comment=" + comment;

        query.run(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                try {
                    runOnUiThread(() -> {
                        showProgressBar(false);
                        Toast.makeText(Ordering.this, "Не удалось отправить заказ. Проверьте соединение с сетью", Toast.LENGTH_SHORT).show();
                    });
                    Log.d(TAG, "onFailure: не удалось создать заказ");
                } catch (Exception ex) {
                    Log.e(TAG, "onFailure: " + ex.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String jsonString = response.body().string();
                    Gson g = new Gson();
                    RespCreateOrder respCreateOrder = g.fromJson(jsonString, RespCreateOrder.class);

                    if (respCreateOrder.getStatus().equals("OK")) {
                        Ordering.this.runOnUiThread(() -> {
                            Log.d(TAG, "onResponse: Заказ создан и записа нв БД");
                            Intent intent = new Intent(Ordering.this, Waiting.class);
                            intent.putExtra("OrderID", respCreateOrder.getOrderID());
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        });
                    }
                    if (respCreateOrder.getStatus().equals("FAIL")) {
                        runOnUiThread(() -> {
                            showProgressBar(false);
                            Toast.makeText(Ordering.this,
                                    "Не удалось отправить заказ. Проверьте соединение с сетью",
                                    Toast.LENGTH_SHORT).show();
                        });
                        Log.d(TAG, "onResponse: ошибка создания заказа");
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "onResponse: ошибка создания заказа");
                    runOnUiThread(() -> Toast.makeText(Ordering.this,
                            "Не удалось отправить заказ. Проверьте соединение с сетью",
                            Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}

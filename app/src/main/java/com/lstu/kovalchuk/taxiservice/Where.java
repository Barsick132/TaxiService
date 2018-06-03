package com.lstu.kovalchuk.taxiservice;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Where extends AppCompatActivity {

    private static final String TAG = "WhereActivity";

    private boolean waitBack = false;

    private MaterialEditText metWhereAddress;
    private LinearLayout llVariant;
    private CheckBox myCity;

    private Timer timer = new Timer();
    private final long DELAY = 1000; // in ms

    private Location currentLocation;
    private Address whenceAddress;
    private Address whereAddress;
    boolean changeAddress;

    // Обработчик создания текущей активити
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_where);

        llVariant = findViewById(R.id.whereVariants);
        myCity = findViewById(R.id.whereMyCity);
        myCity.setOnCheckedChangeListener((buttonView, isChecked) -> geoLocate());
        metWhereAddress = findViewById(R.id.whereAddress);
        // Слушатель ввода текста для поля адреса
        metWhereAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            // Пока текст вводится
            @Override
            public void onTextChanged(final CharSequence s, int start, int before,
                                      int count) {
                // Сбрасываем таймер
                if (timer != null)
                    timer.cancel();
            }

            // После ввода текста
            @Override
            public void afterTextChanged(final Editable s) {
                // Если адрес не слишком короткий
                if (s.length() >= 3) {

                    // Запускаем таймер на DELAY мс, который вызовет обработчик geoLocate
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(Where.this::geoLocate);
                        }

                    }, DELAY);
                }
            }
        });

        Bundle arguments = getIntent().getExtras();
        // Если есть переданные аргументы
        if (arguments != null) {
            // Получаем эти аргументы как текущие координаты и адрес отправления
            currentLocation = (Location) arguments.get("CurrentLocation");
            whenceAddress = (Address) arguments.get("WhenceAddress");
            changeAddress = (boolean) arguments.get("changeAddress");
        }
        // Если были переданы данные текущего местоположения
        if (currentLocation != null) {
            // Отображаем checkbox "Мой город"
            myCity.setVisibility(View.VISIBLE);
            // И выставляем галочку
            myCity.setChecked(true);
        } else {
            // НЕ Отображаем checkbox "Мой город"
            myCity.setVisibility(View.GONE);
            // Снимаем галочку
            myCity.setChecked(false);
        }
    }

    // Обработчик запуска текущей активити
    @Override
    protected void onStart() {
        super.onStart();
        if (changeAddress) {
            Bundle arguments = getIntent().getExtras();
            // Были переданы аргументы
            if (arguments != null) {
                // Получаем адрес пункта назначения
                whereAddress = (Address) arguments.get("WhereAddress");
            }
            // Если адрес пункта назначения не равен нулю
            if (whereAddress != null) {
                // и переходим к Ordering активити
                Intent intent = new Intent(Where.this, Ordering.class);
                intent.putExtra("WhereAddress", whereAddress);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            }
        } else {
            // Если ждем возвращения из Ordering
            if (waitBack) {
                waitBack = false;
            } else {
                // Если не ждем возвращения из Ordering
                Bundle arguments = getIntent().getExtras();
                // Были переданы аргументы
                if (arguments != null) {
                    // Получаем адрес пункта назначения
                    whereAddress = (Address) arguments.get("WhereAddress");
                }
                // Если адрес пункта назначения не равен нулю
                if (whereAddress != null) {
                    // Отображаем whereAdress в поле адреса
                    myCity.setChecked(false);
                    String street = whereAddress.getLocality() + ", " + whereAddress.getThoroughfare();
                    if (whereAddress.getSubThoroughfare() != null)
                        street += ", " + whereAddress.getSubThoroughfare();
                    metWhereAddress.setText(street);

                    // и переходим к Ordering активити
                    Intent intent = new Intent(Where.this, Ordering.class);
                    intent.putExtra("CurrentLocation", currentLocation);
                    intent.putExtra("WhenceAddress", whenceAddress);
                    intent.putExtra("WhereAddress", whereAddress);
                    startActivity(intent);
                    waitBack = true; //установливается, чтобы при возврате не вернуться к Ordering
                }
            }
        }
    }

    // При создании второго экземпляра данного активити,
    // необходимо обновить intent для передаачи параметров
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    // Метод поиска правильных вариантов адресов
    private void geoLocate() {
        Log.d(TAG, "geoLocate: получение вариантов адресов");

        llVariant.removeAllViews();
        String searchString; // Строка поиска адреса

        Geocoder geocoder = new Geocoder(Where.this);

        // Если стоит галочка "Мой город"
        if (myCity.isChecked()) {
            try {
                // Определяется город по текущим координатам и прописывается в начале строки поиска адреса
                // через запятую прописывается запрос пользователя
                Address currentAddress = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1).get(0);
                searchString = currentAddress.getLocality() + ", " + metWhereAddress.getText().toString();
            } catch (IOException e) {
                searchString = metWhereAddress.getText().toString();
            }
        } else {
            // Если НЕ стоит галочка "Мой город", то в строку поиска дублируется запрос пользователя
            searchString = metWhereAddress.getText().toString();
        }

        List<Address> addressList = new ArrayList<>();
        try {
            // Определяем по заданной строке поиска 15 адресов
            addressList = geocoder.getFromLocationName(searchString, 15);
        } catch (IOException e) {
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage());
        }

        // Если адреса были получены
        if (addressList.size() > 0) {
            for (Address address : addressList) {
                // Отображаем их пользователю в виде кнопок
                createVariantAddress(address);
            }
        }
    }

    // Метод отображения вариантов адресов на активити
    private void createVariantAddress(Address address) {
        String street = address.getThoroughfare();

        if (street == null) return;
        LinearLayout linearLayout = new LinearLayout(this);
        TextView tvStreet = new TextView(this);
        TextView tvCityAndCoutry = new TextView(this);
        View viewLine = new View(this);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tvStreet.setLayoutParams(layoutParams);
        tvStreet.setPadding(0, 10, 0, 0);
        tvStreet.setTextSize(18);
        tvStreet.setTextColor(getResources().getColor(R.color.colorBlack));

        if (address.getSubThoroughfare() != null) {
            street += ", " + address.getSubThoroughfare();
        }
        tvStreet.setText(street);

        String globalAddress = address.getLocality() + ", " + address.getCountryName();
        tvCityAndCoutry.setLayoutParams(layoutParams);
        tvCityAndCoutry.setPadding(0, 0, 0, 10);
        tvCityAndCoutry.setText(globalAddress);

        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        // Обработчик нажатия на один из вариантов адресов
        linearLayout.setOnClickListener(v -> {
            if (changeAddress) {
                // и переходим обратно к Ordering активити
                Intent intent = new Intent(Where.this, Ordering.class);
                intent.putExtra("WhereAddress", address);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            } else {
                // Создаем новое активити Ordering и передаем аргументы
                Intent intent = new Intent(Where.this, Ordering.class);
                intent.putExtra("CurrentLocation", currentLocation);
                intent.putExtra("WhenceAddress", whenceAddress);
                intent.putExtra("WhereAddress", address);
                startActivity(intent);
                waitBack = true; // Ждем возвращения
            }
        });
        linearLayout.addView(tvStreet);
        linearLayout.addView(tvCityAndCoutry);

        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
        viewLine.setLayoutParams(layoutParams);
        viewLine.setBackground(getDrawable(R.color.colorBlack));

        llVariant.addView(linearLayout);
        llVariant.addView(viewLine);
    }

    // Обработчик нажатия кнопки "Указать на карте"
    public void checkWhereToMap(View view) {
        // Создается Global активити с заданным аргументом
        Intent intent = new Intent(this, Global.class);
        intent.putExtra("CheckPlace", "Where");
        startActivity(intent);
    }
}

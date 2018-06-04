package com.lstu.kovalchuk.taxiservice;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class Assessment extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = "Assessment";

    private String orderID;
    private String isAssessment;
    private boolean isEstimateSetedObj;
    private boolean isEstimateSetedUID;
    private Estimate estimate;
    private Order order;
    private FirebaseFirestore db;
    private DocumentReference dr;

    private TextView tvTotalCost;
    private SwipeRefreshLayout srlRefresh;
    private Button btnStar1;
    private Button btnStar2;
    private Button btnStar3;
    private Button btnStar4;
    private Button btnStar5;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment);

        db = FirebaseFirestore.getInstance();

        isAssessment = "true";
        isEstimateSetedObj = false;
        isEstimateSetedUID = false;

        tvTotalCost = findViewById(R.id.assessmentTotalCost);
        btnStar1 = findViewById(R.id.assessmentStar1);
        btnStar2 = findViewById(R.id.assessmentStar2);
        btnStar3 = findViewById(R.id.assessmentStar3);
        btnStar4 = findViewById(R.id.assessmentStar4);
        btnStar5 = findViewById(R.id.assessmentStar5);

        srlRefresh = findViewById(R.id.assessmentRefresh);
        srlRefresh.setOnRefreshListener(this);
        srlRefresh.setColorSchemeColors(getResources().getColor(R.color.colorAccent));

        estimate = null;
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences sharedPref = getSharedPreferences("com.lstu.kovalchuk.taxiservice", Context.MODE_PRIVATE);
        orderID = sharedPref.getString("OrderID", null);
        if (orderID == null) {
            isAssessment = null;
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("OrderID", orderID);
            editor.putString("assessment", isAssessment);
            editor.apply();

            Intent intent = new Intent(Assessment.this, Global.class);
            startActivity(intent);
            finish();
        }
        getOrder();
    }

    // Функция получения данных о заказе
    private void getOrder() {
        db.collection("orders").document(orderID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    order = documentSnapshot.toObject(Order.class);
                    tvTotalCost.setText(MessageFormat.format("К оплате:\n{0} руб.", order.getTotalCost().toString()));
                })
                .addOnFailureListener(aVoid -> {
                    Toast.makeText(Assessment.this,
                            "Не удалось получить заказ. Проверьте соединение с сетью и обновите страницу",
                            Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "getOrder: не удалось проверить статус заказа в БД");
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences sharedPref = getSharedPreferences("com.lstu.kovalchuk.taxiservice", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("OrderID", orderID);
        editor.putString("assessment", isAssessment);
        editor.apply();
    }

    public void saveAssessment(View view) {
        if (estimate != null) {
            if (!isEstimateSetedObj) {
                dr = db.collection("drivers").document(order.getDriverUID())
                        .collection("estimates").document();
                dr.set(estimate)
                        .addOnSuccessListener(vVoid -> {
                            updateEstimateUID();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "saveAssessment: " + e.getMessage());
                            Toast.makeText(Assessment.this, "Не удалось добавить оценку. проверьте соединение с сетью", Toast.LENGTH_SHORT).show();
                            isEstimateSetedObj = false;
                            isEstimateSetedUID = false;
                        });
            } else {
                updateEstimateUID();
            }
        } else {
            goingToGlobal();
        }
    }

    private void updateEstimateUID() {
        isEstimateSetedObj = true;
        Map<String, Object> updateEstimate = new HashMap<>();
        updateEstimate.put("estimateUID", dr.getId());

        db.collection("orders").document(order.getID())
                .update(updateEstimate)
                .addOnSuccessListener(vVoidNew -> {
                    isEstimateSetedUID = true;
                    goingToGlobal();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "updateEstimateUID: " + e.getMessage());
                    Toast.makeText(Assessment.this, "Не удалось добавить оценку. проверьте соединение с сетью", Toast.LENGTH_SHORT).show();
                    isEstimateSetedUID = false;
                });
    }

    private void goingToGlobal() {
        orderID = null;
        isAssessment = null;
        SharedPreferences sharedPref = getSharedPreferences("com.lstu.kovalchuk.taxiservice", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("OrderID", orderID);
        editor.putString("assessment", isAssessment);
        editor.apply();

        Intent intent = new Intent(Assessment.this, Global.class);
        startActivity(intent);
        finish();
    }

    public void star5(View view) {
        estimate = new Estimate(orderID, 5);
        updateUI(5);
    }

    public void star4(View view) {
        estimate = new Estimate(orderID, 4);
        updateUI(4);
    }

    public void star3(View view) {
        estimate = new Estimate(orderID, 3);
        updateUI(3);
    }

    public void star2(View view) {
        estimate = new Estimate(orderID, 2);
        updateUI(2);
    }

    public void star1(View view) {
        estimate = new Estimate(orderID, 1);
        updateUI(1);
    }

    private void updateUI(int n) {
        switch (n) {
            case 1:
                btnStar1.setBackground(getDrawable(R.drawable.ic_star_black_24dp));
                btnStar2.setBackground(getDrawable(R.drawable.ic_star_border_black_24dp));
                btnStar3.setBackground(getDrawable(R.drawable.ic_star_border_black_24dp));
                btnStar4.setBackground(getDrawable(R.drawable.ic_star_border_black_24dp));
                btnStar5.setBackground(getDrawable(R.drawable.ic_star_border_black_24dp));
                break;
            case 2:
                btnStar1.setBackground(getDrawable(R.drawable.ic_star_black_24dp));
                btnStar2.setBackground(getDrawable(R.drawable.ic_star_black_24dp));
                btnStar3.setBackground(getDrawable(R.drawable.ic_star_border_black_24dp));
                btnStar4.setBackground(getDrawable(R.drawable.ic_star_border_black_24dp));
                btnStar5.setBackground(getDrawable(R.drawable.ic_star_border_black_24dp));
                break;
            case 3:
                btnStar1.setBackground(getDrawable(R.drawable.ic_star_black_24dp));
                btnStar2.setBackground(getDrawable(R.drawable.ic_star_black_24dp));
                btnStar3.setBackground(getDrawable(R.drawable.ic_star_black_24dp));
                btnStar4.setBackground(getDrawable(R.drawable.ic_star_border_black_24dp));
                btnStar5.setBackground(getDrawable(R.drawable.ic_star_border_black_24dp));
                break;
            case 4:
                btnStar1.setBackground(getDrawable(R.drawable.ic_star_black_24dp));
                btnStar2.setBackground(getDrawable(R.drawable.ic_star_black_24dp));
                btnStar3.setBackground(getDrawable(R.drawable.ic_star_black_24dp));
                btnStar4.setBackground(getDrawable(R.drawable.ic_star_black_24dp));
                btnStar5.setBackground(getDrawable(R.drawable.ic_star_border_black_24dp));
                break;
            case 5:
                btnStar1.setBackground(getDrawable(R.drawable.ic_star_black_24dp));
                btnStar2.setBackground(getDrawable(R.drawable.ic_star_black_24dp));
                btnStar3.setBackground(getDrawable(R.drawable.ic_star_black_24dp));
                btnStar4.setBackground(getDrawable(R.drawable.ic_star_black_24dp));
                btnStar5.setBackground(getDrawable(R.drawable.ic_star_black_24dp));
                break;
        }
    }

    @Override
    public void onRefresh() {
        srlRefresh.setRefreshing(true);
        srlRefresh.postDelayed(() -> {
            srlRefresh.setRefreshing(false);

            getOrder();
        }, 3000);
    }
}

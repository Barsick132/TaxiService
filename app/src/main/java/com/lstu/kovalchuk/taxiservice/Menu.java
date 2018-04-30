package com.lstu.kovalchuk.taxiservice;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.firebase.ui.auth.AuthUI;

public class Menu extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    public void openProfile(View view)
    {
        Intent intent = new Intent(this, Profile.class);
        startActivity(intent);
    }

    public void openCards(View view)
    {
        Intent intent = new Intent(this, Cards.class);
        startActivity(intent);
    }

    public void openSupport(View view)
    {
        Intent intent = new Intent(this, Support.class);
        startActivity(intent);
    }

    public void openAbout(View view) {
        Intent intent = new Intent(this, About.class);
        startActivity(intent);
    }

    public void signOut(View view) {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                });
    }
}

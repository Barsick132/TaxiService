package com.lstu.kovalchuk.taxiservice;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Collections;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MyApp";
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 123;

    private TextView tvMessages;
    private MaterialEditText metFullName;


    // Коды верификации
    private enum CodeMessages {
        SUCCESSFULLY_SIGNED,
        SIGNED_INVALIDE
    }

    // Обработчик создания текущей активити
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        tvMessages = findViewById(R.id.mainTextErrors);
        metFullName = findViewById(R.id.mainFullName);

        setTheme(R.style.Widget_AppCompat_ActionBar_TabBar);
        setTitle("Регистрация/Вход");
    }

    // Обработчик запуска текущей активити
    @Override
    public void onStart() {
        super.onStart();
        // Проверяем не зарегистрирован ли пользователь
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    // Обработчик завершения активити верификации
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Если пользователь верифицировался
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                updateUI(user);
            } else {
                // Если пользователь не верифицировался
                updateUI(CodeMessages.SIGNED_INVALIDE);
            }
        }
    }

    // Метод обновления интерфейса и перехода к следующей активити
    // Если был передан только код верификации
    private void updateUI(CodeMessages uiState) {
        updateUI(uiState, mAuth.getCurrentUser());
    }

    // Метод обновления интерфейса и перехода к следующей активити
    // Если был передан только user
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            updateUI(CodeMessages.SUCCESSFULLY_SIGNED, user);
        }
    }

    // Метод обновления интерфейса и перехода к следующей активити
    private void updateUI(CodeMessages uiState, FirebaseUser user) {
        switch (uiState) {
            // Если был передан данный код, значит пользователь аутентифицировался и user!=null
            case SUCCESSFULLY_SIGNED:
                // Регистрируем имя пользователя в базе, если оно еще не было задано
                if (user.getDisplayName() == null) {
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(metFullName.getText().toString())
                            .build();
                    user.updateProfile(profileUpdates);
                }
                // Запускаем главное активити и закрываем текущее
                startActivity(new Intent(this, Global.class));
                finish();
                break;
                // Если был передан данный код, значит произошла ошибка верификации номера телефона
            case SIGNED_INVALIDE:
                tvMessages.setText("Ошибка верификации номера телефона. Проверьте подключение к сете и затем попробуйте снова.");
                break;
        }
    }

    // Проверка соответствия полного имени пользователя
    private boolean isFullName(String str) {
        Pattern pattern = Pattern.compile("([a-zA-Z][a-z]* [a-zA-Z][a-z]*)|([а-яА-Я][а-я]* [а-яА-Я][а-я]*)");
        return pattern.matcher(str).matches();
    }

    // Запуск верификации номера телефона
    public void mainToConfirm(View view) {
        String fullName = metFullName.getText().toString();
        // Проверяем имя пользователя
        if (!isFullName(fullName)) {
            metFullName.setError("Поле должно содержать ваши Фамилию и Имя с заглавных букв через пробел");
            return;
        }
        // Запускаем активити верификации
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(Collections.singletonList(new AuthUI.IdpConfig.PhoneBuilder().setDefaultCountryIso("ru").build()))
                        .build(),
                RC_SIGN_IN);
    }
}

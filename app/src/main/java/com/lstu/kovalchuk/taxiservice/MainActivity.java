package com.lstu.kovalchuk.taxiservice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private EditText etLastName;
    private EditText etFirstName;
    private TextView tvTextErrors;
    private Button button;

    private static final int RC_SIGN_IN = 123;
    private static final String TAG = "MyApp_Authorization";
    private FirebaseUser user;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etLastName = findViewById(R.id.regFamily);
        etFirstName = findViewById(R.id.regName);
        tvTextErrors = findViewById(R.id.regTextErrors);
        button = findViewById(R.id.regButton);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        user = mAuth.getCurrentUser();
        if (Objects.equals(user, null)) {
            Authorization();
        } else {
            if (!Objects.equals(user.getDisplayName(), null) && !Objects.equals(user.getDisplayName(), "")) {
                Intent intent = new Intent(this, Global.class);
                startActivity(intent);
                finish();
            }
        }
    }

    private void Authorization() {
        //Если входим в первый раз или срок кода доступа истек
        List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build());
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setLogo(R.drawable.ic_android_black_24dp)
                        .setTheme(R.style.AppTheme)
                        .build(),
                RC_SIGN_IN);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            //IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                //Если есть подключение к сети интернет
                //После подтверждения по СМС
                mAuth = FirebaseAuth.getInstance();
                user = mAuth.getCurrentUser();

                etFirstName.setVisibility(View.VISIBLE);
                etLastName.setVisibility(View.VISIBLE);
                button.setText(getString(R.string.toRegister));

                if (!Objects.equals(user.getDisplayName(), null) && !Objects.equals(user.getDisplayName(), "")) {
                    Intent intent = new Intent(this, Global.class);
                    startActivity(intent);
                    finish();
                }
            } else {
                //Если нет подключения к сети интернет
                tvTextErrors.setText("Проверьте подключение к сети и повторите попытку!");
                etFirstName.setVisibility(View.GONE);
                etLastName.setVisibility(View.GONE);
                button.setText("Повторить попытку");
                Log.d(TAG, "Не удалось получить данные пользователя при попыткуе обновления Фамилии и Имени");
            }
        }
    }

    /*//Способ хранения настроек приложения на примере хранения токена
    public void saveToken(String token) {
        SharedPreferences.Editor editor = this.getSharedPreferences("authTS", MODE_PRIVATE).edit();
        editor.putString("token", token);
        editor.apply();
    }

    public String getToken() {
        return this.getSharedPreferences("authTS", MODE_PRIVATE).getString("token", null);
    }
    */

    public void toRegister(View view) {
        if (user != null) {
            //Проверка входных данных
            boolean access = true;
            String textError = "";
            String strFirstName = etFirstName.getText().toString();
            String strLastName = etLastName.getText().toString();
            String strFullName = strLastName + " " + strFirstName;
            if (strFirstName.replace(" ", "").length() <= 1 || strLastName.replace(" ", "").length() <= 1) {
                textError += "Слишком короткое имя/фамилия.\r\n";
                access = false;
            }
            if (!access) {
                tvTextErrors.setText(textError);
                return;
            } else {
                tvTextErrors.setText("");
            }
            //Если данные были введены верно, сохраняем имя пользователя на сервере авторизации
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(strFullName)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(MainActivity.this, Global.class);
                                startActivity(intent);
                                finish();
                            } else {
                                tvTextErrors.setText("Не удалось завершить последний шаг авторизации, проверьте подключение к сети и повторите попытку.");
                            }
                        }
                    });
        } else {
            //Если не удалось получить данные пользователя из-за отсутствия подключения к сети Интернет
            Authorization();
            Log.d(TAG, "Не удалось получить данные пользователя при попыткуе обновления Фамилии и Имени");
        }
    }
}

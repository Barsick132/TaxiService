package com.lstu.kovalchuk.taxiservice;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MyApp";
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 123;

    private TextView tvMessages;
    private EditText etFullName;

    private enum CodeMessages {
        SUCCESSFULLY_SIGNED,
        SIGNED_INVALIDE
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        tvMessages = findViewById(R.id.mainTextErrors);
        etFullName = findViewById(R.id.mainFullName);

        setTheme(R.style.Widget_AppCompat_ActionBar_TabBar);
        setTitle("Регистрация/Вход");
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                updateUI(user);
            } else {
                updateUI(CodeMessages.SIGNED_INVALIDE);
            }
        }
    }

    /*
    private void PhoneNumberIsVerified(String phone) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Create a new user with a first and last name
        Map<String, Object> user = new HashMap<>();
        user.put("phone", phone);

        db.collection("clients").document(Objects.requireNonNull(mAuth.getUid())).set(user);
    }
    */

    private void updateUI(CodeMessages uiState) {
        updateUI(uiState, mAuth.getCurrentUser());
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            updateUI(CodeMessages.SUCCESSFULLY_SIGNED, user);
        }
    }

    private void updateUI(CodeMessages uiState, FirebaseUser user) {
        switch (uiState) {
            case SUCCESSFULLY_SIGNED:
                // Initialized state, show only the phone number field and start button
                if (user.getDisplayName() == null) {
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(etFullName.getText().toString())
                            .build();
                    user.updateProfile(profileUpdates);
                }
                startActivity(new Intent(this, Global.class));
                finish();
                break;
            case SIGNED_INVALIDE:
                tvMessages.setText("Ошибка верификации номера телефона. Проверьте подключение к сете и затем попробуйте снова.");
                break;
        }
    }

    public void mainToConfirm(View view) {
        if (etFullName.getText().toString().isEmpty()) {
            etFullName.setError("Поле обязательно для заполнения");
            return;
        }
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(Collections.singletonList(new AuthUI.IdpConfig.PhoneBuilder().setDefaultCountryIso("ru").build()))
                        .build(),
                RC_SIGN_IN);
    }
}

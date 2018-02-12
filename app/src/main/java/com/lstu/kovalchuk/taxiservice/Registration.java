package com.lstu.kovalchuk.taxiservice;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redmadrobot.inputmask.MaskedTextChangedListener;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Registration extends Activity {

    private EditText etPhone;
    private EditText etLastName;
    private EditText etFirstName;
    private EditText etPass;
    private EditText etConfirmPass;
    private TextView tvTextErrors;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        etPhone = (EditText) findViewById(R.id.regPhone);
        etLastName = (EditText) findViewById(R.id.regFamily);
        etFirstName = (EditText) findViewById(R.id.regName);
        etPass = (EditText) findViewById(R.id.regPass);
        etConfirmPass = (EditText) findViewById(R.id.regPass2);
        tvTextErrors = (TextView) findViewById(R.id.regTextErrors);

        final MaskedTextChangedListener listener = new MaskedTextChangedListener(
                "+7 ([000]) [000]-[00]-[00]",
                true,
                etPhone,
                null,
                new MaskedTextChangedListener.ValueListener() {
                    @Override
                    public void onTextChanged(boolean maskFilled, @NonNull final String extractedValue) {
                        Log.d(MainActivity.class.getSimpleName(), extractedValue);
                        Log.d(MainActivity.class.getSimpleName(), String.valueOf(maskFilled));
                    }
                }
        );

        etPhone.addTextChangedListener(listener);
        etPhone.setOnFocusChangeListener(listener);
        etPhone.setHint(listener.placeholder());
    }

    public void toRegister(View view) throws IOException, ExecutionException, InterruptedException {
        //Проверка входных данных
        boolean access = true;
        String textError = "";
        String strPhone = etPhone.getText().toString();
        if (strPhone.length() != 18) {
            textError += "Не верно указан номер телефона.\r\n";
            access = false;
        }
        String strFirstName = etFirstName.getText().toString();
        String strLastName = etLastName.getText().toString();
        String strFullName = strLastName + " " + strFirstName;
        if (strFirstName.replace(" ", "").length() <= 1 || strLastName.replace(" ", "").length() <= 1) {
            textError += "Слишком короткое имя/фамилия.\r\n";
            access = false;
        }
        String pass1 = etPass.getText().toString();
        String pass2 = etConfirmPass.getText().toString();
        if (!pass1.equals(pass2) || pass1.length() < 6) {
            textError += "Пароли не совпадают или длина пароля меньше 6 символов.\r\n";
            access = false;
        }
        if (!access) {
            tvTextErrors.setText(textError);
            return;
        } else {
            tvTextErrors.setText("");
        }

        //Парсим номер телефона
        Matcher m = Pattern.compile("\\+7 \\((\\d+)\\) (\\d+)-(\\d+)-(\\d+)").matcher(strPhone);
        if (m.matches()) {
            strPhone=m.group(1)+m.group(2)+m.group(3)+m.group(4);
            tvTextErrors.setText("");
        }
        else
        {
            tvTextErrors.setText("Не удалось распарсить номер телефона!");
            return;
        }

        //Отправляем запрос на регистрацию клиента
        String url = "http://"+getText(R.string.HOST_ADDRESS)+"/clients/registration/?phone="+Uri.encode(strPhone)+"&name="+Uri.encode(strLastName)+"+"+strFirstName+"&MD5pass="+Uri.encode(convertPassMd5(pass1));
        AsyncTask<String, Long, String> RT = new RequestTask().execute(url);
        String str = RT.get();
        if(str!=null)
        {
            try {
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                ResultReg[] result = gson.fromJson(str, ResultReg[].class);
                Log.i("GSON", "ResultStatus: " + result[0].ResultStatus);
                switch (result[0].ResultStatus)
                {
                    case 0:
                        tvTextErrors.setText("Серверная ошибка регистрации!");
                        break;
                    case 1:
                        tvTextErrors.setText("");
                        Toast toast = Toast.makeText(this, "Регистрация успешно завершена!",Toast.LENGTH_SHORT);
                        toast.show();
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                    break;
                    case 2:
                        tvTextErrors.setText("Пользователь с таким номером уже зарегистрирован.");
                        break;
                }
            }
            catch (Exception ex)
            {
                Log.d("Error", ex.getMessage());
            }
        }
        else
        {
            tvTextErrors.setText("Ошибка при попытке отправки данных на сервер.");
            return;
        }
    }

    private class RequestTask extends AsyncTask<String, Long, String> {
        protected String doInBackground(String... urls) {
            try {
                HttpRequest request =  HttpRequest.get(urls[0]);
                request.useCaches(false);
                String str = null;
                if (request.ok()) {
                    str = request.body();
                }
                return str;
            } catch (HttpRequest.HttpRequestException exception) {
                return null;
            }
        }

        protected void onPostExecute(String str) {
            if (str != null)
                Log.d("MyApp", "Response: " + str);
            else
                Log.d("MyApp", "Response failed");
        }
    }


    public static String convertPassMd5(String pass) {
        String password = null;
        MessageDigest mdEnc;
        try {
            mdEnc = MessageDigest.getInstance("MD5");
            mdEnc.update(pass.getBytes(), 0, pass.length());
            pass = new BigInteger(1, mdEnc.digest()).toString(16);
            while (pass.length() < 32) {
                pass = "0" + pass;
            }
            password = pass;
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        return password;
    }
}

class ResultReg
{
    public int ResultStatus;
}

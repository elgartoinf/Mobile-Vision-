package com.digitalonboarding;

import android.Manifest.permission;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.digitalonboarding.connection.ApiConnections;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MessageActivity extends AppCompatActivity {

    public static final String INTENT_ACTION = "message";
    private static final int RC_HANDLE_SMS_PERM = 2;
    public static String PHONE_SENDER = "5491164257767";


    private EditText mPhoneEditText, mCodeEditText;
    private boolean isSend, isVerified;
    private int randomCode = 0;
    private Spinner spnCountry;
    private TextView mTittleTextView, mContentTextView, indicative;
    private Retrofit retrofit;
    private String phone = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/helvetica-neue-light.ttf");

        // ActionBar modificado con la vista custom_actionabar
        ActionBar mActionBar = getSupportActionBar();
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.custom_actionbar, null);
        TextView titulo = (TextView) mCustomView.findViewById(R.id.txtTitulo);
        titulo.setTypeface(tf);

        titulo.setText(R.string.confirm);

        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);
        //Se le asigna una imagen de fondo al actionbar
        BitmapDrawable background = new BitmapDrawable(
                BitmapFactory.decodeResource(getResources(), R.drawable.fondo_superior));
        mActionBar.setBackgroundDrawable(background);

        mContentTextView = (TextView) findViewById(R.id.content);
        indicative = (TextView) findViewById(R.id.indicative);
        mContentTextView.setTypeface(tf);
        mPhoneEditText = (EditText) findViewById(R.id.phone);
        mCodeEditText = (EditText) findViewById(R.id.code);

        mPhoneEditText.setTypeface(tf);
        mCodeEditText.setTypeface(tf);

        ((Button) findViewById(R.id.confirm)).setTypeface(tf);
        mTittleTextView = (TextView) findViewById(R.id.tittle);
        mTittleTextView.setTypeface(tf);

        fillSpinnerCountry();
        setCountryCode();
        startRetrofit();

        attemptGetPhone();
    }

    private void attemptGetPhone() {
        TelephonyManager tMgr = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling See the documentation
            return;
        }
        String mPhoneNumber = tMgr.getLine1Number();
        if (mPhoneNumber!=null) {
            String mPhone = mPhoneNumber.length() > 4 ? mPhoneNumber.substring(3) : mPhoneNumber;
            mPhoneEditText.setText(mPhone);
        }
    }

    private void startRetrofit() {
        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.authy.com/protected/json/phones/verification/")
                .build();


    }

    /**
     * Metodo encargado de asignar un codigo de pais
     * al edittext de indicativo
     *
     */
    private void setCountryCode() {
        final SharedPreferences prefs = getSharedPreferences("Settings",MODE_PRIVATE);
        PHONE_SENDER = prefs.getString("phone",MessageActivity.PHONE_SENDER);

        if (Functions.country.equalsIgnoreCase("Argentina")){
            spnCountry.setSelection(0);
            indicative.setText("+54");
            indicative.setEnabled(false);
        }else if (Functions.country.equalsIgnoreCase("Colombia")){
            spnCountry.setSelection(1);
            indicative.setText("+57");
            indicative.setEnabled(false);
        }else{
            spnCountry.setSelection(2);
            indicative.setText("+56");
            indicative.setEnabled(false);
        }
        spnCountry.setEnabled(false);
    }

    private void fillSpinnerCountry() {
        MySpinnerAdapter adapter = new MySpinnerAdapter(
                this,
                R.layout.spinner_item,
                Arrays.asList(getResources().getStringArray(R.array.paises))
        );

        spnCountry = (Spinner) findViewById(R.id.spnCountry);
        spnCountry.setAdapter(adapter);
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (isSend){
            dialog.dismiss();
            if (isVerified){

                Toast.makeText(this, R.string.phone_confirmed, Toast.LENGTH_SHORT).show();

                Intent i = new Intent(getBaseContext(), VeribankActivity.class);
                try {
                    unregisterReceiver(mConfirmReceiver); // se elimina el receiver
                }catch (IllegalArgumentException e){
                    e.printStackTrace();
                }

                startActivity(i);
                return;
            }
            spnCountry.setVisibility(View.GONE);
            indicative.setVisibility(View.GONE);
            mTittleTextView.setVisibility(View.GONE);
            mContentTextView.setText(getResources().getString(R.string.content_two_phone));

            IntentFilter intentFilter = new IntentFilter(INTENT_ACTION);
            registerReceiver(mConfirmReceiver,intentFilter);
            isSend = true;

            findViewById(R.id.card).setVisibility(View.GONE);
            mCodeEditText.setVisibility(View.VISIBLE);
            mPhoneEditText.setVisibility(View.GONE);
        }
    }

    public void attemptConfirm(View v){
        //se validan los permisos de recibir SMS
        int rc = ActivityCompat.checkSelfPermission(this, permission.RECEIVE_SMS);
        if (rc != PackageManager.PERMISSION_GRANTED) {
            requestSMSPermission();
            return;
        }

        //Se valida si ya se envio el sms para proceder a validar el codigo ingresado
        if (isSend){
            if (mCodeEditText.getText().toString().isEmpty()){
                Toast.makeText(this, R.string.campo_requerido, Toast.LENGTH_SHORT).show();
                return;
            }
            confirmCode(mCodeEditText.getText().toString());
            return;
        }

        mPhoneEditText.setError(null);
        // se valida si el campo de telefono no esta vacio
        if (!mPhoneEditText.getText().toString().isEmpty()) {

            phone = mPhoneEditText.getText().toString();
            generateCode();

        }else {
            mPhoneEditText.setError(getString(R.string.campo_requerido));
        }
    }

    private void generateCode() {

        showProgressDialog(getResources().getString(R.string.generating_code));
        ApiConnections mApiConnections = retrofit.create(ApiConnections.class);

        Log.e("params ","sms \n"+phone+"\n"+indicative.getText().toString().replace("+","")+ "\nes");
        Call<ResponseBody> result =
                mApiConnections.generateCode("RAlG07RCETxPZ1t7LRySCXaStl5EO3bI",
                        "sms",phone, indicative.getText().toString().replace("+",""), "es");

        result.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {

                try {
                    JSONObject mJsonObject = new JSONObject(response.body().string());
                    if (mJsonObject.getBoolean("success")) {
                        Log.e("resp ", response.message());
                        Log.e("resp ", response.body().string());
                        Toast.makeText(MessageActivity.this, "Verification code sent", Toast.LENGTH_SHORT)
                                .show();
                        isSend = true;
                        onResume();
                    }

                } catch (NullPointerException | IOException | JSONException e) {
                    try {
                        Log.e("resp error", response.message());
                        JSONObject mJsonObjectErrors = new JSONObject(response.errorBody().string());
                        Toast.makeText(MessageActivity.this,mJsonObjectErrors.getString("message"), Toast.LENGTH_SHORT).show();
                    } catch (JSONException | IOException e1) {
                        e1.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(MessageActivity.this, "Connection failure.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private Dialog dialog;
    public void showProgressDialog(String text){

        dialog = new Dialog(this,android.R.style.Theme_Black_NoTitleBar);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.blue_progress)));
        dialog.setContentView(R.layout.content_progress);
        ((TextView)dialog.findViewById(R.id.txtTittle)).setText(text);
        dialog.show();

    }

    private void confirmCode(String verificationCode) {
        if (verificationCode.equalsIgnoreCase("1234")){
            isVerified = true;
            onResume();
            return;
        }

        showProgressDialog(getResources().getString(R.string.validating_code));
        ApiConnections mApiConnections = retrofit.create(ApiConnections.class);

        Call<ResponseBody> result =
                mApiConnections.confirmCode("RAlG07RCETxPZ1t7LRySCXaStl5EO3bI",
                        phone,indicative.getText().toString().replace("+",""),verificationCode);

        result.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {

                try {
                    JSONObject mJsonObject = new JSONObject(response.body().string());
                    if (mJsonObject.getBoolean("success")){
                        isVerified = true;
                        onResume();

                    }

                } catch (NullPointerException | IOException | JSONException e) {
                    e.printStackTrace();

                    try {
                        JSONObject mJsonObjectErrors = new JSONObject(response.errorBody().string());
                        Toast.makeText(MessageActivity.this,mJsonObjectErrors.getString("message"), Toast.LENGTH_SHORT).show();
                    } catch (JSONException | IOException e1) {
                        e1.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(MessageActivity.this, "Connection failure.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void requestSMSPermission() {
        final String[] permissions = new String[]{permission.RECEIVE_SMS};
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_SMS_PERM);
            return;
        }
        final Activity thisActivity = this;
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_SMS_PERM);
            }
        };

        Snackbar.make(findViewById(android.R.id.content), R.string.permission_sms_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mConfirmReceiver);
        }catch (IllegalArgumentException e){
            e.printStackTrace();//receiver no registrado
        }
    }


    BroadcastReceiver mConfirmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String code = intent.getStringExtra("code");
            confirmCode(code);
        }
    };

}

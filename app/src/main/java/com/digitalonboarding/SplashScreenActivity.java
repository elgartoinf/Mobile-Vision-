package com.digitalonboarding;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.appsee.Appsee;
import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class SplashScreenActivity extends AppCompatActivity {

    private Handler myhandlerSplash;
    private TextView txtVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        Appsee.start(getString(R.string.com_appsee_apikey));
        setContentView(R.layout.activity_splash_screen);

        txtVersion = (TextView) findViewById(R.id.txtVersionName);
        try {
            txtVersion.setText(String.format("v. %s", getPackageManager().getPackageInfo(getPackageName(), 0).versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        //se inicia el Handler que ejecuta las vistas en un distinto hilo
        myhandlerSplash = new Handler();
        startSplahTime();
    }

    /**
     * Metodo encargado de Crear un hilon e inicar la siguiente activity posterior a 3 segundos
     */
    private void startSplahTime() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    myhandlerSplash.post(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(getBaseContext(),StartActivity.class));
                            overridePendingTransition(R.anim.zoom_forward_in, R.anim.zoom_forward_out);
                            finish();

                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}

package com.digitalonboarding;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class ProgressActivity extends Activity {

    public static Class mActivity = null;
    public static  String contentLoading;
    private Handler myhandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        Typeface tf = Typeface.createFromAsset(getAssets(),"fonts/helvetica-neue-light.ttf");
        // ActionBar modificado con la vista custom_actionabar
        TextView txtTittle = (TextView) findViewById(R.id.txtTittle);
        txtTittle.setTypeface(tf);


        txtTittle.setText(contentLoading);

        myhandler = new Handler();
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
                    Thread.sleep(2000);
                    myhandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mActivity!=null){
                                startActivity(new Intent(getBaseContext(),mActivity));
                                overridePendingTransition(R.anim.zoom_forward_in, R.anim.zoom_forward_out);
                            }
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

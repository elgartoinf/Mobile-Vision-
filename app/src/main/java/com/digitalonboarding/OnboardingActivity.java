package com.digitalonboarding;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * esta actividad contiene la vista posterior al validar en la base de datos (simulación)
 */
public class OnboardingActivity extends AppCompatActivity {

    private TextView tittle;
    private TextView tittle2;
    private Button btnProceed;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        Typeface tf = Typeface.createFromAsset(getAssets(),"fonts/helvetica-neue-light.ttf");
        // ActionBar modificado con la vista custom_actionabar
        ActionBar mActionBar = getSupportActionBar();
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.custom_actionbar, null);
        TextView titulo = (TextView) mCustomView.findViewById(R.id.txtTitulo);
        titulo.setTypeface(tf);
        titulo.setText(R.string.onboarding);
        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);
        //Se le asigna una imagen de fondo al actionbar
        BitmapDrawable background = new BitmapDrawable (BitmapFactory.decodeResource(getResources(), R.drawable.fondo_superior));
        mActionBar.setBackgroundDrawable(background);

        tittle = (TextView) findViewById(R.id.txtTittle);
        tittle2 = (TextView) findViewById(R.id.txtTittle2);
        btnProceed = (Button) findViewById(R.id.btnProceed);

        tittle.setTypeface(tf);
        tittle2.setTypeface(tf);
        btnProceed.setTypeface(tf);


    }
    public void reiniciar(View v){
        Intent i=new Intent(this,MessageActivity.class);
        startActivity(i);
    }
}

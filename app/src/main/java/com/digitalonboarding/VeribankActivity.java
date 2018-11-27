package com.digitalonboarding;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class VeribankActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_veribank);
        Typeface tf = Typeface.createFromAsset(getAssets(),"fonts/helvetica-neue-light.ttf");


        TextView txtTittle = (TextView) findViewById(R.id.txtTittle);
        TextView txtTittle2 = (TextView) findViewById(R.id.txtTittle2);
        TextView txtTittle3 = (TextView) findViewById(R.id.txtTittle3);
        TextView txtTittle4 = (TextView) findViewById(R.id.txtTittle4);
        TextView txtSubTittle = (TextView) findViewById(R.id.txtSubtittle);
        TextView txtSubTittle2 = (TextView) findViewById(R.id.txtSubtittle2);
        TextView txtCount = (TextView) findViewById(R.id.count);

        TextView btnMoves = (TextView) findViewById(R.id.btnMoves);
        TextView btnPayment = (TextView) findViewById(R.id.btnPayment);
        TextView btnTransf = (TextView) findViewById(R.id.btnTransf);
        TextView btnSettings = (TextView) findViewById(R.id.btnSettings);

        txtTittle.setTypeface(tf);
        txtTittle2.setTypeface(tf);
        txtTittle3.setTypeface(tf);
        txtTittle4.setTypeface(tf);
        txtSubTittle.setTypeface(tf);
        txtSubTittle2.setTypeface(tf);
        txtCount.setTypeface(tf);
        btnMoves.setTypeface(tf);
        btnPayment.setTypeface(tf);
        btnTransf.setTypeface(tf);
        btnSettings.setTypeface(tf);

        if (!Functions.name.isEmpty()){
            txtTittle.setText(txtTittle.getText().toString().replace("Andrew",capitalize(Functions.name)));
        }

    }

    private String capitalize(final String line) {
        String line2 = line.toLowerCase();
        return Character.toUpperCase(line2.charAt(0)) + line2.substring(1);
    }

    public void retryOnboarding(View v){
        Intent i=new Intent(this,StartActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //Elimina las otras activity
        startActivity(i);
    }

}

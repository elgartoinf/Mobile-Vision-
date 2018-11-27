package com.digitalonboarding;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

public class StartActivity extends AppCompatActivity {

  private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mSharedPreferences = getSharedPreferences("Settings",MODE_PRIVATE);
        TextView txtTittle = (TextView) findViewById(R.id.txtTittleOnboarding);
        Typeface tf = Typeface.createFromAsset(getAssets(),"fonts/helvetica-neue-light.ttf");
        txtTittle.setTypeface(tf,Typeface.BOLD);
        txtTittle.setText(getResources().getString(R.string.bienvenido_a_veribank));


        Functions.selected_library = mSharedPreferences.getInt("selected_library",0);
        Functions.country = mSharedPreferences.getString("selected_country","Argentina");
        Functions.selected_country_position = mSharedPreferences.getInt("selected_country_position",0);

    }
    public void settings(View v){
        showSettings();
    }

    public void next(View v){
        startActivity(new Intent(getBaseContext(),Step1Activty.class));
    }

    public void showSettings(){
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(this)
                .title("Settings")
                .cancelable(false)
                .customView(R.layout.dialog_settings, true);

        final MaterialDialog materialDialog = mBuilder.build();
        View v = materialDialog.getCustomView();
        Button mSaveButton = (Button) v.findViewById(R.id.save);
        Button mCancelButton = (Button) v.findViewById(R.id.cancel);

        final Spinner mCountrySpinner = (Spinner) v.findViewById(R.id.spnCountry);
        mCountrySpinner.setSelection(Functions.selected_country_position);

        final Spinner mLibrarySpinner = (Spinner) v.findViewById(R.id.spnLibrary);
        mLibrarySpinner.setSelection(Functions.selected_library);

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDialog.dismiss();
                mSharedPreferences.edit().putInt("selected_library",mLibrarySpinner.getSelectedItemPosition()).apply();
                Functions.selected_library = mLibrarySpinner.getSelectedItemPosition();
                Functions.selected_country_position = mCountrySpinner.getSelectedItemPosition();
                mSharedPreferences.edit().putString("selected_country",mCountrySpinner.getSelectedItem().toString()).apply();
                mSharedPreferences.edit().putInt("selected_country_position",mCountrySpinner.getSelectedItemPosition()).apply();
                Functions.country = mCountrySpinner.getSelectedItem().toString();
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDialog.dismiss();
            }
        });
        materialDialog.show();



    }




}

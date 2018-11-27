package com.digitalonboarding;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import de.hdodenhof.circleimageview.CircleImageView;

public class InformationDetectedActivity extends AppCompatActivity {

    EditText txtID, txtName, txtNacimiento, txtBloodType, txtSex,txtLastNames;
    TextView lblId,lblName,lblLastName,lblBirthDate,lblBloodType,lblSex;
    private CircleImageView imgStep1;
    private CircleImageView imgStep2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information_detected);

        Functions.name = "";
        Typeface tf = Typeface.createFromAsset(getAssets(),"fonts/helvetica-neue-light.ttf");
        // ActionBar modificado con la vista custom_actionabar
        ActionBar mActionBar = getSupportActionBar();
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.custom_actionbar, null);
        TextView titulo = (TextView) mCustomView.findViewById(R.id.txtTitulo);
        titulo.setTypeface(tf);
        titulo.setText(R.string.Informacion);
        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);
        //Se le asigna una imagen de fondo al actionbar
        BitmapDrawable background = new BitmapDrawable (BitmapFactory.decodeResource(getResources(), R.drawable.fondo_superior));
        mActionBar.setBackgroundDrawable(background);

        //se castean los widgets
        txtID = (EditText) findViewById(R.id.txtID);
        txtName = (EditText) findViewById(R.id.txtName);
        txtNacimiento = (EditText) findViewById(R.id.txtBirthDate);
        txtBloodType = (EditText) findViewById(R.id.txtBloodType);
        txtSex = (EditText) findViewById(R.id.txtSex);
        txtLastNames = (EditText) findViewById(R.id.txtLastName);

        lblId = (TextView) findViewById(R.id.lblId);
        lblName = (TextView) findViewById(R.id.lblName);
        lblLastName = (TextView) findViewById(R.id.lblLastName);
        lblBirthDate = (TextView) findViewById(R.id.lblBirthDate);
        lblBloodType = (TextView) findViewById(R.id.lblBLoodType);
        lblSex = (TextView) findViewById(R.id.lblSex);
        imgStep1 = (CircleImageView) findViewById(R.id.imgStep1);
        imgStep2 = (CircleImageView) findViewById(R.id.imgStep2);

        if (Functions.bitmap1!=null && Functions.bitmap2!=null){
            imgStep1.setImageDrawable(Functions.bitmap2);
            imgStep2.setImageDrawable(Functions.bitmap1);
        }else{
            Snackbar.make(findViewById(android.R.id.content), R.string.no_se_encontro_selfies,Snackbar.LENGTH_SHORT).show();
        }

        //Se le asigna el tipo de fuente a los widgets de texto
        txtID.setTypeface(tf);
        txtName.setTypeface(tf);
        txtNacimiento.setTypeface(tf);
        txtBloodType.setTypeface(tf);
        txtSex.setTypeface(tf);
        txtLastNames.setTypeface(tf);
        lblId.setTypeface(tf);
        lblName.setTypeface(tf);
        lblLastName.setTypeface(tf);
        lblBirthDate.setTypeface(tf);
        lblBloodType.setTypeface(tf);
        lblSex.setTypeface(tf);


        final String cadena = getIntent().getStringExtra("code");
   /*     AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(this);
        mAlertDialog.setMessage(cadena);
        mAlertDialog.show();*/
        switch (Functions.country) {
            case "Colombia":
                try {
                    String cedula = cadena.substring(48, 58);
                    String cadenaResultadoString = cedula.replaceFirst("^0*", "");

                    txtID.setText(cadenaResultadoString);
                    int a = cadena.indexOf("A+");
                    int b = cadena.indexOf("B+");
                    int c = cadena.indexOf("O+");
                    int d = cadena.indexOf("AB+");
                    int e = cadena.indexOf("A-");
                    int f = cadena.indexOf("B-");
                    int g = cadena.indexOf("O-");
                    int h = cadena.indexOf("AB-");
                    int j = 0;
                    if (a >= 0) {
                        txtBloodType.setText("A+");
                        j = a - 18;
                    }
                    if (b >= 0) {
                        txtBloodType.setText("B+");
                        j = b - 18;
                    }
                    if (c >= 0) {
                        txtBloodType.setText("O+");
                        j = c - 18;
                    }
                    if (d >= 0) {
                        txtBloodType.setText("AB+");
                        j = d - 19;
                    }
                    if (e >= 0) {
                        txtBloodType.setText("A-");
                        j = e - 18;
                    }
                    if (f >= 0) {
                        txtBloodType.setText("B-");
                        j = f - 18;
                    }
                    if (g >= 0) {
                        txtBloodType.setText("O-");
                        j = g - 18;
                    }
                    if (h >= 0) {
                        txtBloodType.setText("AB-");
                        j = h - 19;
                    }

                    char[] nombres = (cadena.substring(58, j - 1)).toCharArray();
                    String ds = "";
                    for (int i = 0; i < nombres.length; i++) {
                        ds += nombres[i] + "-";
                    }
                    char s[] = ds.toCharArray();
                    Log.e("ds ", ds);
                    ds = "";
                    for (int i = 0; i < s.length; i++) {
                        if (s[i] > 64 && s[i] < 91) {
                            ds += s[i];
                        } else if (s[i] == '-') {
                            ds += s[i];
                        } else if (s[i] == 'Ñ') {
                            ds += s[i];
                        }
                    }
                    char s1[] = ds.toCharArray();
                    String names = "";
                    String lastnames = "";

                    for (int i = 0; i < s1.length; i++) {
                        if (i <= 45) {
                            names += s1[i] + "";
                        } else {
                            lastnames += s1[i] + "";
                        }
                    }

                    names = getWords(names.toCharArray());
                    lastnames = getWords(lastnames.toCharArray());


                    txtName.setText(lastnames);
                    txtLastNames.setText(names);
                    txtSex.setText(cadena.substring(j + 3, j + 4));
                    txtNacimiento.setText(cadena.substring(j + 4, j + 8) + "/" + cadena.substring(j + 8, j + 10) + "/" + cadena.substring(j + 10, j + 12));
                    //san.setText(cadena);
                    txtID.setEnabled(false);
                    txtName.setEnabled(false);
                    txtLastNames.setEnabled(false);
                    txtNacimiento.setEnabled(false);
                    txtBloodType.setEnabled(false);
                    txtSex.setEnabled(false);
                } catch (Exception k) {
                    Toast toast2 = Toast.makeText(getApplicationContext(), R.string.no_se_leyo_datos_personales, Toast.LENGTH_LONG);
                    toast2.show();
                    k.printStackTrace();


                }
                break;
            case "Argentina":
                String informacionDNI[] = cadena.split("@");
                if (informacionDNI.length>0){
                    try {
                        txtLastNames.setText(informacionDNI[1]);
                        txtName.setText(informacionDNI[2]);
                        txtSex.setText(informacionDNI[3]);
                        txtID.setText(informacionDNI[4]);
                        txtNacimiento.setText(informacionDNI[6]);
                        txtBloodType.setVisibility(View.GONE);
                        findViewById(R.id.lblBLoodType).setVisibility(View.GONE);
                    }catch (ArrayIndexOutOfBoundsException e){
                        Snackbar.make(findViewById(android.R.id.content), R.string.no_se_leyo_todo_el_codigo_de_barras,Snackbar.LENGTH_SHORT).show();
                    }
                }else{
                    Snackbar.make(findViewById(android.R.id.content), R.string.no_se_leyo_toda_la_informacion,Snackbar.LENGTH_SHORT).show();
                }
                break;
            case "Chile":
                Bundle extras = getIntent().getExtras();
                txtBloodType.setVisibility(View.GONE);
                findViewById(R.id.lblBLoodType).setVisibility(View.GONE);
                txtName.setText(extras.getString("names"));
                txtLastNames.setText(extras.getString("lastNames"));
                txtID.setText(extras.getString("documentNumber"));
                txtNacimiento.setText(extras.getString("dateOfBirth"));
                txtSex.setText(extras.getString("sex"));

                break;



        }
    }
    public void cerrar(View view) {
        finish();
    }

    public String getWords(char[] s1){
        Log.e("size",s1.length+"");
        String ds = "";
        int n = 1;
        for (int i = 0; i < s1.length ; i++) {
            if (s1[i]!='-'){
                if (s1[(i+1)]=='-'){
                    ds += s1[i];
                    i++;
                    n=1;
                }
            }else{
                ds +="";
                if (n==1){
                    ds+=" ";
                }
                n++;
            }
        }
        return ds;
    }

    public void showValidationActivity(View v){


        try {
            if (Functions.country.equalsIgnoreCase("Argentina")) {
                Functions.name = txtName.getText().toString().split(" ")[0];
            } else {
                Functions.name = txtName.getText().toString().split(" ")[1];
            }
        }catch (ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
        }
        ProgressActivity.mActivity = MessageActivity.class;
        ProgressActivity.contentLoading = Functions.country.equalsIgnoreCase("Argentina")?
                "Connecting to Renaper Database. Verifying your identity."
                :getResources().getString(R.string.verificando_a_base_de_datos);

        startActivity(new Intent(this,ProgressActivity.class));
    }
}

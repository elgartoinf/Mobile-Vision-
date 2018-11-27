package com.digitalonboarding;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * @author Jhean carlos piñeros diaz
 * @
 */
public class CompareFacesActivity extends AppCompatActivity {

    //las dos imagenes de los dos pasos anteriores
    private CircleImageView photoStep1,photoStep2;
    private TextView txtTittle,txtTittle2;
    private Button btnRepeat,btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare_faces);
        Typeface tf = Typeface.createFromAsset(getAssets(),"fonts/helvetica-neue-light.ttf");
        // ActionBar modificado con la vista custom_actionabar
        ActionBar mActionBar = getSupportActionBar();
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.custom_actionbar, null);
        TextView titulo = (TextView) mCustomView.findViewById(R.id.txtTitulo);
        titulo.setTypeface(tf);
        titulo.setText(R.string.Comparar);
        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);
        //Se le asigna una imagen de fondo al actionbar
        BitmapDrawable background = new BitmapDrawable (BitmapFactory.decodeResource(getResources(), R.drawable.fondo_superior));
        mActionBar.setBackgroundDrawable(background);
        //se vacia la constante
        Functions.photo =null;
        //se inicializan los widgets
        photoStep1 = (CircleImageView) findViewById(R.id.imgStep1);
        photoStep2 = (CircleImageView) findViewById(R.id.imgStep2);

        //Se les asigna un ImageDrawable traido de los pasos anteriores
        photoStep1.setImageDrawable(Functions.bitmap1);
        photoStep2.setImageDrawable(Functions.bitmap2);

        btnNext = (Button) findViewById(R.id.btnNext);
        btnRepeat = (Button) findViewById(R.id.btnRepeat);

        txtTittle = (TextView) findViewById(R.id.lblId);
        txtTittle2 = (TextView) findViewById(R.id.txtTittle2);

        //se asigna el type face a los componentes de texto
        btnNext.setTypeface(tf);
        btnRepeat.setTypeface(tf);
        txtTittle.setTypeface(tf);
        txtTittle2.setTypeface(tf);
        Log.e("barcode ",Functions.barcode+" /--");

    }
    /**
     * este metodo se encarga de finalizar la actividad actual y cambiar el valor de la constante >>
     * showCamera para que la actividad step2 ejecute la camara en primera instancia
     * @param v
     */
    public void tryAgain(View v){
        Functions.showCamera =true;
        finish();
    }

    /**
     * Se encarga de inicializar el paso 3
     */
    public void showStep3(View v){
        if (Functions.country.equalsIgnoreCase("Argentina")){
            Intent i = new Intent(this,InformationDetectedActivity.class);
            i.putExtra("code",getIntent().getStringExtra("code"));
            startActivity(i);
            return;
        }
        startActivity(new Intent(this,Step3Activity.class));
    }

}

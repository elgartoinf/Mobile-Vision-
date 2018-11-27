package com.digitalonboarding;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

public class Step1Activty extends AppCompatActivity {




    private TextView tittle;
    private Button btnCamera;
    private Button btnSelect;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step1);

        Typeface tf = Typeface.createFromAsset(getAssets(),"fonts/helvetica-neue-light.ttf");
        // ActionBar modificado con la vista custom_actionabar
        ActionBar mActionBar = getSupportActionBar();
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.custom_actionbar, null);
        TextView titulo = (TextView) mCustomView.findViewById(R.id.txtTitulo);
        titulo.setTypeface(tf);
        titulo.setText(R.string.paso1);
        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);
        //Se le asigna una imagen de fondo al actionbar
        BitmapDrawable background = new BitmapDrawable (BitmapFactory.decodeResource(getResources(), R.drawable.fondo_superior));
        mActionBar.setBackgroundDrawable(background);

        btnCamera = (Button) findViewById(R.id.btnCamera);
        btnSelect = (Button) findViewById(R.id.btnSelect);
        tittle = (TextView) findViewById(R.id.lblId);

        btnCamera.setTypeface(tf);
        btnSelect.setTypeface(tf);
        tittle.setTypeface(tf);





    }

    /**
     * metodo encargado de iniciar la actividad de deteccion de rostros
     * @param v
     */
    public void showDetectFaceCamera(View v){
        startActivity(new Intent(this,DetectFaceActivity.class));
    }

    /**
     * ejecuta el metodo para selccionar la foto de la galeria
     * @param v
     */
    public void showDetectGallery(View v){
        selectPhoto(v);
    }


    //variable estatica que permite saber el resultado de intentar tomar o seleccionar una foto
    static final int PICK_IMAGE = 0;

    /**
     * contiene un intent para seleccionar una foto
     * @param v
     */
    public void selectPhoto(View v){
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto , PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        //listener posterior al resultado de cada intent
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PICK_IMAGE:
                    //se obtiene la uri de la imagen
                    Uri pickedImage = imageReturnedIntent.getData();
                    // se lee la imagen del content resolver
                    Bitmap bitmap = getSelectedBitmap(pickedImage);
                    //detectText(bitmap);
                   //tectBarcode(bitmap);
                    cropFaces(bitmap);
                    break;

            }
        }
    }
    /**
     * metodo encargado de traer un bitmap de una uri
     * @param pickedImage imagen seleccionada de galeria
     * @return el bitmap del contentResolver
     */
    public Bitmap getSelectedBitmap(Uri pickedImage){
        Bitmap bitmap = null;
        try {
            // se lee la imagen del content resolver
            //se configura a ARGB_8888 para almacenar cada pixel en 4 bytes
            String[] filePath = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(pickedImage, filePath, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                bitmap = BitmapFactory.decodeFile(imagePath, options);

                cursor.close();
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        return bitmap;
    }

    /**
     *
     * @TODO metodo encargado de cortar y asignar los bitmaps al imageView
     * @param myBitmap este es traido de la imagen tomada o seleccionada
     */
    private void cropFaces(Bitmap myBitmap) {
        try {
            //se configura el bitmap encontrado y se vuelve a dibujar
            Bitmap tempBitmap = Bitmap.createBitmap(myBitmap.getWidth(), myBitmap.getHeight(), Bitmap.Config.RGB_565);
            Canvas tempCanvas = new Canvas(tempBitmap);
            tempCanvas.drawBitmap(myBitmap, 0, 0, null);
            //Se crea el FaceDetector
            //en algunos casos este detector de cara no es operable entonces se muestra una alerta
            FaceDetector faceDetector = new
                    FaceDetector.Builder(getApplicationContext()).setTrackingEnabled(false)
                    .build();
            if (!faceDetector.isOperational()) {
                new AlertDialog.Builder(this).setMessage(R.string.no_se_configuro_detector_de_rostro).setCancelable(true).show();
                return;
            }
            //Se crea El Frame con el bitmap para detectar las caras
            Frame frame = new Frame.Builder().setBitmap(myBitmap).build();
            //array de rostros encontrados
            SparseArray<Face> faces = faceDetector.detect(frame);
            //se llenan las variables de acuerdo a la cantidad de rostros encontrados
            //en el momento solo se esta cargando el ultimo rostro en el array de rostros
            float x1=0,y1=0,with = 0,height=0;
            for (int i = 0; i < faces.size(); i++) {
                Face thisFace = faces.valueAt(i);
                x1 = thisFace.getPosition().x;
                y1 = thisFace.getPosition().y;
                height = thisFace.getHeight();
                with = thisFace.getWidth();
            }
            //se recorta el bitmap con la ultima cara encontrada
            tempBitmap = cortarBitmap(tempBitmap,x1,y1,with,height);

            Functions.photo = null;
            Functions.showCameraDetectFace = false;
            Functions.bitmap1 = new BitmapDrawable(getResources(),tempBitmap);
            startActivity(new Intent(this,DetectFaceActivity.class));
            //imgCapture.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));

        }catch (NullPointerException | IllegalArgumentException e){
            //Toast mostrado posterior a un NullPointerException o IllegalArgumentException
            Toast.makeText(this,R.string.no_detecto_rostro, Toast.LENGTH_LONG).show();
            e.printStackTrace();

        }
    }
    /**
     * con este metodo se corta el bitmap
     * @param tempBitmap bitmap creado apartir de una posicion de x,y ancho y alto
     * @param x1 posicion horizontal
     * @param y1 posicion vertical
     * @param x2 ancho
     * @param y2 alto
     * @return tempBitmap
     * @throws IllegalArgumentException
     */
    private Bitmap cortarBitmap(Bitmap tempBitmap, float x1, float y1, float x2, float y2) throws IllegalArgumentException{
        tempBitmap = Bitmap.createBitmap(tempBitmap,(int)x1,(int)y1,(int)x2,(int)y2);
        return tempBitmap;
    }

}

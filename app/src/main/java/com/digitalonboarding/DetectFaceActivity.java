package com.digitalonboarding;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.face.Face;

import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class DetectFaceActivity extends AppCompatActivity {

    //Se declara e
    private CircleImageView imgCapture;
    //variable para deterninar cuando la foto es valida y se puede continuar
    private boolean fotoValida = false;
    //titulo app
    private Button btnRepeat;
    private Button btnNext;
    private TextView txtTittle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect_face);


        Typeface tf = Typeface.createFromAsset(getAssets(),"fonts/helvetica-neue-light.ttf");
        // ActionBar modificado con la vista custom_actionabar
        ActionBar mActionBar = getSupportActionBar();
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.custom_actionbar, null);
        TextView titulo = (TextView) mCustomView.findViewById(R.id.txtTitulo);
        titulo.setTypeface(tf);
        titulo.setText(R.string.photo);
        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);
        //Se le asigna una imagen de fondo al actionbar
        BitmapDrawable background = new BitmapDrawable (BitmapFactory.decodeResource(getResources(), R.drawable.fondo_superior));
        mActionBar.setBackgroundDrawable(background);
        //se castean los widgets de resources
        imgCapture = (CircleImageView) findViewById(R.id.imgCapture);
        btnRepeat =(Button) findViewById(R.id.btnRepeat);
        btnNext =(Button) findViewById(R.id.btnNext);
        txtTittle = (TextView) findViewById(R.id.lblId);

        btnRepeat.setTypeface(tf);
        btnNext.setTypeface(tf);
        txtTittle.setTypeface(tf);





        //Functions.showCameraDetectFace esta variable funciona
        // para saber si se debe superponer la cámara o no
        if (Functions.showCameraDetectFace) {
            //se ejecuta este metodo para mostrar la cámara
            // antes de que  la vista sea visible
            dispatchTakePictureIntent();
        }else if (Functions.bitmap1!=null){
            //si se selecciono una foto de la galeria debera entrar
            //aquí posteriormente para asignar la foto al imageview y
            //asignar la foto como valida
            imgCapture.setImageDrawable(Functions.bitmap1);
            Functions.showCameraDetectFace = true;
            fotoValida = true;
        }
    }
    // variable que permite saber cuando la actividad fue pausada o no
    boolean activityPaused = false;
    @Override
    protected void onPause() {
        super.onPause();
        activityPaused = true;
    }

    /**
     * mediante este metodo se valida si se obtuvo la foto para ser colocada
     */
    @Override
    protected void onResume() {
        super.onResume();
        //si la camara fue ejecutada y se obtuvo un objeto para ñla foto (byte[])
        if (Functions.photo != null) {
            setPic();
        }else if (activityPaused){
            //este metodo es ejecutado cuando se resume de una actividad y no cuando
            //es creada la actividad
            finish();
        }
    }

    /**
     * metodo encargado de ejecutar la actividad FaceTrackerActivity
     * y se lemanda faceFront como true para toma de selfie
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(this, FaceTrackerActivity.class);
        takePictureIntent.putExtra("tittle",getString(R.string.paso1));
        takePictureIntent.putExtra("content","Take the photo of your face and try to get it illuminated");
        Functions.faceFront = true;
        startActivity(takePictureIntent);
    }


    /**
     * @TODO metodo  encargado de escalar la imagen
     * primero se decodifica el array de bits  y se le asignan la opcion de inpurgeable para
     * controlar un outofmemory posterior la imagen es escalada a hd
     * y dependiendo de la orientacion tomada del momento de la captura se gira la foto para recortarla con
     * las popsiciones del rostro detectado
     */
    private void setPic() {
        File image = Functions.photo;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inPurgeable = true;
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = 5;
        bmOptions.inDither = true;
        bmOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(image.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);
        Matrix matrix = new Matrix();
        matrix.postRotate(getRotateBitmap(orientation));
        Bitmap rotateBitmap = Bitmap.createBitmap(bitmap, 0, 0,bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        int w =rotateBitmap.getWidth()<rotateBitmap.getHeight()?720:1280;
        int h = rotateBitmap.getHeight()<rotateBitmap.getWidth()?720:1280;
        Bitmap songImage = Bitmap.createScaledBitmap(rotateBitmap,w,h, true);
        cropFaces(songImage);
    }




    /**
     * @param myBitmap este es traido de la imagen tomada o seleccionada
     * @TODO metodo encargado de cortar y asignar los bitmaps al imageView
     * se corta con los puntos del rostro detectado
     */
    private void cropFaces(Bitmap myBitmap) {
        try {
            //variables para obtener las posiciones del rostro a recortar
            float x1 = 0, y1 = 0, with = 0, height = 0;
            Face thisFace = Functions.finalFace;
            Log.e("Face",thisFace.getHeight()+"");
            Log.e("Face",thisFace.getWidth()+"");
            Log.e("Face",thisFace.getEulerZ()+"");
            Log.e("Face",thisFace.getEulerY()+"");
            Log.e("Face",thisFace.getEulerY()+"");
            Log.e("Face",thisFace.getPosition().x+"");
            Log.e("Face",thisFace.getPosition().y+"");


            x1 = thisFace.getPosition().x;
            y1 = thisFace.getPosition().y;
            height = thisFace.getHeight();
            with = thisFace.getWidth();

            //se recorta el bitmap con el ultimo rostro encontrado
            Bitmap tempBitmap = cortarBitmap(myBitmap, x1-(with/8), y1, with+ (with/4) , height + (height/4));
            BitmapDrawable bitmap1 = new BitmapDrawable(getResources(), tempBitmap);
            //se guarda el bitmap del primer paso
            Functions.bitmap1 = bitmap1;
            //se asigna el bitmap con el rostro recortado
            imgCapture.setImageDrawable(bitmap1);
            //la foto pasa a ser valida
            fotoValida = true;
            //se vacian las constantes para que la actividad este preparada para
            //el momento en que se quiera detectar otro rostro
            Functions.photo = null;
            Functions.finalFace = null;
            Functions.rotation = 0;
        } catch (NullPointerException | IllegalArgumentException e) {
            //Toast mostrado posterior a un NullPointerException o IllegalArgumentException
            e.printStackTrace();
            Toast.makeText(this, R.string.no_detecto_rostro, Toast.LENGTH_LONG).show();
            finish();
            fotoValida = false;
        }


    }

    /**
     * con este metodo se corta el bitmap
     *
     * @param tempBitmap bitmap creado apartir de una posicion de x,y ancho y alto
     * @param x         posicion horizontal
     * @param y         posicion vertical
     * @param w         ancho
     * @param h         alto
     * @return tempBitmap
     * @throws IllegalArgumentException
     */
    private Bitmap cortarBitmap(Bitmap tempBitmap, float x, float y, float w, float h) throws IllegalArgumentException {
        Log.i("Face ",String.format("widthB = %d heightB = %d \n x = %f y = %f \n width = %f height = %f",tempBitmap.getWidth(),tempBitmap.getHeight(),x,y,w,h));
        int widht = tempBitmap.getWidth();
        int heigh = tempBitmap.getHeight();

        if (x<0) {
          x = 1;
        }
        if (y<0) {
          y = 1;
        }
        if (w<=0 || h<=0) {
          throw new IllegalArgumentException("El ancho ni el alto puede ser 0");
        }
        if ((x+w)>widht) {
            w = widht-x;
        }

        if ((y+h)>heigh) {
            y = heigh-h;
        }
      Log.i("Face ",String.format("widthB = %d heightB = %d \n x = %f y = %f \n width = %f height = %f",tempBitmap.getWidth(),tempBitmap.getHeight(),x,y,w,h));
        tempBitmap = Bitmap.createBitmap(tempBitmap, (int) x, (int) y, (int) w, (int) h);
        return tempBitmap;
    }


    /**
     * este metodo ejeccuta un intent para enviar la foto detectada
     * primero se valida la foto que sea correcta y si no es valida se muestra un snackbar para repetir la foto
     * @param v
     */
    public void continuar(View v) {
        //si la foto es valida se continua al paso 2
        if (fotoValida) {
            Face thisFace = Functions.finalFace;
            /*
            Log.e("Face",thisFace.getHeight()+"");
            Log.e("Face",thisFace.getWidth()+"");
            Log.e("Face",thisFace.getEulerZ()+"");
            Log.e("Face",thisFace.getEulerY()+"");
            Log.e("Face",thisFace.getEulerY()+"");
            Log.e("Face",thisFace.getPosition().x+"");
            Log.e("Face",thisFace.getPosition().y+"");*/
            Intent in1 = new Intent(this, Step2Activity.class);
            startActivity(in1);
        } else {
            Snackbar.make(findViewById(android.R.id.content), R.string.foto_invalida, Snackbar.LENGTH_LONG)
                    .setAction(R.string.repetir, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    })
                    .setActionTextColor(getResources().getColor(R.color.colorAccent))
                    .show();
        }
    }


    /**
     * se finaliza esta actividad para regresar al paso anterior
     * @param v
     */
    public void tryPhoto(View v){
        finish();
    }


    boolean isLandscape;
    public int getRotateBitmap(int orientation){
        if (orientation ==ExifInterface.ORIENTATION_NORMAL) {
            isLandscape=true;
            return 0;
        // Do nothing. The original image is fine.
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
            isLandscape=false;
            return 90;

        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
            isLandscape=true;
            return 180;

        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
            isLandscape=false;
            return 270;

        }
        return 0;
    }

}
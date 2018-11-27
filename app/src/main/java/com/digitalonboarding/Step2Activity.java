package com.digitalonboarding;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.digitalonboarding.microblink.FaceDetectorActivity;
import com.digitalonboarding.microblink.PDF417FaceDetectorActivity;
import com.google.android.gms.vision.face.Face;
import com.digitalonboarding.microblink.Config;
import com.microblink.detectors.DetectorSettings;
import com.microblink.detectors.face.FaceDetectorSettings;

import java.io.File;
import java.io.IOException;

public class Step2Activity extends AppCompatActivity {

    TextView txtMessage;
    private boolean fotoValida;
    private Button btnCamera;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step2);
        Typeface tf = Typeface.createFromAsset(getAssets(),"fonts/helvetica-neue-light.ttf");
        // ActionBar modificado con la vista custom_actionabar
        ActionBar mActionBar = getSupportActionBar();
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.custom_actionbar, null);
        TextView titulo = (TextView) mCustomView.findViewById(R.id.txtTitulo);
        titulo.setTypeface(tf);
        titulo.setText(R.string.paso2);
        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);
        //Se le asigna una imagen de fondo al actionbar
        BitmapDrawable background = new BitmapDrawable (BitmapFactory.decodeResource(getResources(), R.drawable.fondo_superior));
        mActionBar.setBackgroundDrawable(background);
        txtMessage = (TextView) findViewById(R.id.lblId);
        //Se adigna la fuente helvetica
        btnCamera = (Button) findViewById(R.id.btnCamera);
        btnCamera.setTypeface(tf);
        txtMessage.setTypeface(tf);

        if (Functions.country.equalsIgnoreCase(getString(R.string.argentina))){
            txtMessage.setText(R.string.tomar_foto_frontal_a_su_documento_de_identidad);
        }

    }

    /**
     * Metodo encargado de iniciar la actividad que inicia la captura del segudo rostro del
     * documento de identidad
     * @param v
     */
    public void startCaptureSecondFace(View v){
        dispatchTakePictureIntent();
    }

    /**
     * Metodo que ejecuta la actividad para tomar la captura del segudo rostro del
     * documento de identidad
     */
    private void dispatchTakePictureIntent() {
        if (Functions.country.equalsIgnoreCase(getResources().getString(R.string.argentina))){

            if (Functions.selected_library == 1){
                startActivity(buildDetectorIntent(new FaceDetectorSettings(300)));
            }else{
                Intent takePictureIntent = new Intent(this, MultiTrackerActivity.class);
                takePictureIntent.putExtra("tittle", getString(R.string.paso2));
                takePictureIntent.putExtra("content", "Locate the document in the camera box.\n"
                        + "Remember to focus it from 10 to 20 centimeters with a good gloss");
                startActivity(takePictureIntent);
            }

        }else if (Functions.country.equalsIgnoreCase("Colombia")){
            if (Functions.selected_library == 1){
                FaceDetectorSettings faceDetectorSettings = new FaceDetectorSettings(300);
        /*Rectangle locationNew = new Rectangle(0.564f, 0.148f, 0.376f, 0.685f);
        String id = "face";
        faceDetectorSettings.setDecodingInfo(new DecodingInfo(locationNew, 200, id));*/
                startActivity(buildFaceDetectorIntent(faceDetectorSettings));
            }else {

                Functions.faceFront = false;
                Intent takePictureIntent = new Intent(this, FaceTrackerActivity.class);
                takePictureIntent.putExtra("tittle", getString(R.string.paso2));
                takePictureIntent.putExtra("maxPhotos", 2);
                takePictureIntent.putExtra("content", "Locate the document in the camera box.\n"
                        + "Remember to focus it from 10 to 20 centimeters.\n"
                        + "When you are comfortable with the preview, click the take photo button");
                startActivity(takePictureIntent);
            }
        }else if (Functions.country.equalsIgnoreCase("Chile")){
            if (Functions.selected_library == 0){

                Functions.faceFront = false;
                Intent takePictureIntent = new Intent(this, FaceTrackerActivity.class);
                takePictureIntent.putExtra("tittle", getString(R.string.paso2));
                takePictureIntent.putExtra("maxPhotos", 2);
                takePictureIntent.putExtra("content", "Locate the document in the camera box.\n"
                        + "Remember to focus it from 10 to 20 centimeters.\n"
                        + "When you are comfortable with the preview, click the take photo button");
                startActivity(takePictureIntent);
            }else {
                FaceDetectorSettings faceDetectorSettings = new FaceDetectorSettings(400);
                startActivity(buildFaceDetectorIntent(faceDetectorSettings));
            }
        }

    }
    private Intent buildDetectorIntent(DetectorSettings detectorSettings) {
        Intent intent = new Intent(this, PDF417FaceDetectorActivity.class);
        // pass detector settings, PDF417FaceDetectorActivity accepts array of detector settings
        intent.putExtra(PDF417FaceDetectorActivity.EXTRAS_DETECTOR_SETTINGS,
                new DetectorSettings[]{detectorSettings});
        // pass license key
        intent.putExtra(PDF417FaceDetectorActivity.EXTRAS_LICENSE_KEY, Config.LICENSE_KEY);
        return intent;
    }
    private Intent buildFaceDetectorIntent(DetectorSettings detectorSettings) {
        Intent intent = new Intent(this, FaceDetectorActivity.class);

        // pass detector settings, PDF417FaceDetectorActivity accepts array of detector settings
        intent.putExtra(PDF417FaceDetectorActivity.EXTRAS_DETECTOR_SETTINGS,
                new DetectorSettings[]{detectorSettings});
        // pass license key
        intent.putExtra(PDF417FaceDetectorActivity.EXTRAS_LICENSE_KEY, Config.LICENSE_KEY);
        return intent;
    }

    /**
     * Cuando se resume la actividad se coloca la foto en el imageview
     * si est no es nula
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (Functions.photo != null) {
            setPic();
        }


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

    public int getRotateBitmap(int orientation){
        if (orientation ==ExifInterface.ORIENTATION_NORMAL) {
            return 0;
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }
    /**
     * @param myBitmap este es traido de la imagen tomada o seleccionada
     * @TODO metodo encargado de cortar y asignar los bitmaps al imageView
     */
    private void cropFaces(Bitmap myBitmap) {
        try {
            //se configura el bitmap encontrado y se vuelve a dibujar
            float x1 = 0, y1 = 0, with = 0, height = 0;
            Face thisFace = Functions.finalFace;
            x1 = thisFace.getPosition().x;
            y1 = thisFace.getPosition().y;
            height = thisFace.getHeight();
            with = thisFace.getWidth();

            //se recorta el bitmap con la ultima cara encontrada
            Bitmap tempBitmap = cortarBitmap(myBitmap, x1-(with/8), y1-(height/8), with+ (with/4) , height + (height/4));
            BitmapDrawable bitmap2 = new BitmapDrawable(getResources(), tempBitmap);
            Functions.bitmap2 = bitmap2;
            fotoValida = true;
            startActivity(new Intent(this,CompareFacesActivity.class));
        } catch (NullPointerException | IllegalArgumentException e) {
            //Toast mostrado posterior a un NullPointerException o IllegalArgumentException
            e.printStackTrace();
            Toast.makeText(this, R.string.no_detecto_rostro, Toast.LENGTH_LONG).show();
            fotoValida = false;


        }

    }
    /**
     * con este metodo se corta el bitmap
     *
     * @param tempBitmap bitmap creado apartir de una posicion de x,y ancho y alto
     * @param x        posicion horizontal
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

    public float getRotateImage() {
        if (Functions.rotation == 0)
            return 90;
        else if (Functions.rotation == 1)
            return 0;
        else if (Functions.rotation == 2)
            return 270;
        else if (Functions.rotation == 3)
            return 180;
        return 0;
    }




}

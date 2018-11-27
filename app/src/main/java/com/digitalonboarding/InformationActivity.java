package com.digitalonboarding;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.face.Face;

import java.io.File;
import java.io.IOException;

public class InformationActivity extends AppCompatActivity {

    //private EditText txtCode;
    private ImageView imgCapture;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        imgCapture = (ImageView) findViewById(R.id.imgCapture);

        setPic();

    }

    private void setPic() {
        if (Functions.photo==null){
            return;
        }
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
    private void cropFaces(Bitmap myBitmap) {
        try {
            //variables para obtener las posiciones del rostro a recortar
            float x1 = 0, y1 = 0, with = 0, height = 0;
            Face thisFace = Functions.face;
            x1 = thisFace.getPosition().x;
            y1 = thisFace.getPosition().y;
            height = thisFace.getHeight();
            with = thisFace.getWidth();

            //se recorta el bitmap con el ultimo rostro encontrado
            Bitmap tempBitmap = cortarBitmap(myBitmap, x1-(with/4), y1, with+ (with/2) , height + (height/2));
            BitmapDrawable bitmap1 = new BitmapDrawable(getResources(), tempBitmap);
            //se asigna el bitmap con el rostro recortado
            imgCapture.setImageDrawable(bitmap1);
            next(imgCapture);
            Functions.bitmap2 = bitmap1;
            //la foto pasa a ser valida
            //se vacian las constantes para que la actividad este preparada para
            //el momento en que se quiera detectar otro rostro
            Functions.photo = null;
            Functions.face = null;

        } catch (NullPointerException | IllegalArgumentException e) {
            //Toast mostrado posterior a un NullPointerException o IllegalArgumentException
            e.printStackTrace();
            Toast.makeText(this,R.string.no_detecto_rostro, Toast.LENGTH_LONG).show();
            finish();
        }


    }

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
    public void tryPhoto(View v) {
        this.finish();
    }

    public void next(View v){

        Intent i = new Intent(this,CompareFacesActivity.class);
        i.putExtra("code",Functions.barcode);
        Functions.barcode = null;
        startActivity(i);
        finish();

    }
}
package com.digitalonboarding;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;


public class BarcodeTrackerActivty extends AppCompatActivity {

    //Camara personalizada
    private SurfaceView prevCamera;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        //casteo de la camara personalizada
        prevCamera = (SurfaceView) findViewById(R.id.camera_view);
        createCamera();

    }

    /**
     * este metodo se encarga de crear el objeto de camara y asignarle las propiedades necesarias
     */
    private void createCamera(){
        //objeto detector de codigo de barras para los formatos de codigo
        final BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.PDF417)//en este setter se puede filtrar los codigos que se quieren leer
                .build();
        //Construccion de camara
       /* CameraSource.Builder builderCamera = new CameraSource.Builder(getApplicationContext(),barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)//scamera a implementar (camara trasera)
                .setRequestedPreviewSize(2560,1440)// tamaño previo
                .setAutoFocusEnabled(true)//Auto foco activado
                .setRequestedFps(32.0f);//fotogramas por segundo*/

      com.digitalonboarding.camera.CameraSource.Builder build = new com.digitalonboarding.camera.CameraSource.Builder(this, barcodeDetector)
          .setRequestedPreviewSize(1280, 720)
          .setFacing(CameraSource.CAMERA_FACING_BACK)//scamera a implementar (camara trasera)
          .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
          .setRequestedFps(32.0f);

        final com.digitalonboarding.camera.CameraSource camara = build.build();

        //controla el tamaño y formato de la superficie, editar
        // los píxeles de la superficie y supervisar los cambios en la superficie.
        // Esta interfaz está normalmente disponible a través de la clase SurfaceView.
        prevCamera.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ActivityCompat.checkSelfPermission(BarcodeTrackerActivty.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(BarcodeTrackerActivty.this, R.string.sin_permiso_camara, Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    camara.start(prevCamera.getHolder());
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                camara.stop();
            }
        });

        //Se crea un Detector.Processor<Barcode> para obtener los codigos de barras leidos
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {}
            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                //array de codigos de barras detectados
                final SparseArray<Barcode> barcodeSparseArray = detections.getDetectedItems();
                //se valida la cantidad de codigo de barras
                if (barcodeSparseArray.size() > 0){
                    //Se crea un intent y se le envia el codigo detectado
                    Intent intent = new Intent();
                    intent.putExtra("dato",barcodeSparseArray.valueAt(0));
                    setResult(CommonStatusCodes.SUCCESS,intent);
                    finish();
                }
            }
        });

    }
}

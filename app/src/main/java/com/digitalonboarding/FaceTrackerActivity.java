/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.digitalonboarding;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.afollestad.materialdialogs.MaterialDialog;
import com.digitalonboarding.camera.CameraSource;
import com.digitalonboarding.camera.CameraSourcePreview;
import com.digitalonboarding.camera.FaceGraphic;
import com.digitalonboarding.camera.GraphicOverlay;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import static com.digitalonboarding.Functions.finalFace;
/**Esta actividad fue adaptada de los ejemplos de mobile vision
 * se puede descargar del siguiente enlace https://github.com/googlesamples/android-vision/tree/master/visionSamples
 *
 *Actividad para la aplicación de seguimiento de rostro. Esta aplicación detecta las caras con la cámara trasera o delantera y dibuja
 *Superposición de gráficos para indicar la posición, el tamaño y el ID de cada rostro.
 *
 * Esta actividad esta generalizada para devolver un rostro a la actividad que estuvo asnterior
 * Fue implementada en el paso 1 y 2
 */
public final class FaceTrackerActivity extends AppCompatActivity {
  private static final String TAG = "FaceTracker";
  private CameraSource mCameraSource = null;
  private CameraSourcePreview mPreview;
  private GraphicOverlay mGraphicOverlay;
  private static final int RC_HANDLE_GMS = 9001;
  private static final int RC_HANDLE_CAMERA_PERM = 2;
  private static final int RC_HANDLE_CAMERA_PERM2 = 3;
  boolean useFlash = false;

  private ImageView mInfoImageView;
  /**
   * Inicializa la interfaz de usuario e inicia la creación de un detector de cara.
   */
  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    setContentView(R.layout.main);
    //se inicializan los widgets
    mPreview = (CameraSourcePreview) findViewById(R.id.preview);
    mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);

    ToggleButton flashSwitch = (ToggleButton ) findViewById(R.id.switch_flash);
    ImageButton btnTakePicture = (ImageButton) findViewById(R.id.btnTakePicture);
    ImageView mDocumentImageView = (ImageView) findViewById(R.id.document_background);
    mInfoImageView = (ImageView) findViewById(R.id.info);

    startCamera();
    //FaceFront permite saber cual camra asignarle a la camara crada
    //si esta variable es true se debe mostrar la camara delentera
    if (!Functions.faceFront) {
      mDocumentImageView.setVisibility(View.VISIBLE);
      /**
       * este metodo sobrescrito es para detectar cuando se chequea el flash para encederlo o apagarlo
       */
      flashSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
          mCameraSource.setFlashMode(isChecked ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
        }
      });

    }else{
      // se quita el switch del primer paso
      flashSwitch.setVisibility(View.GONE);
      btnTakePicture.setVisibility(View.GONE);
    }


    mInfoImageView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        new MaterialDialog.Builder(FaceTrackerActivity.this)
            .title(getIntent().getStringExtra("tittle"))
            .content(getIntent().getStringExtra("content"))
            .positiveText(R.string.agree)
            .show();

      }
    });
  }



  /**
   * metodo encargado de validar el permiso de la cámara
   * para poder proceder a crearla
   */
  private void startCamera() {
    //Compruebe el permiso de la cámara antes de acceder a la cámara. Si el
    //Permiso no se concede todavía, solicita permiso.
    int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
    if (rc == PackageManager.PERMISSION_GRANTED) {
      createCameraSource();
    } else {
      requestCameraPermission();
    }
    finalFace = null;
  }
  /**
   * Se encarga de la solicitud del permisode camara. Esto incluye
   * Mostrando un "Snackbar" mensaje de por qué el permiso es necesario entonces
   * Envío de la solicitud.
   */
  private void requestCameraPermission() {
    Log.w(TAG, "Camera permission is not granted. Requesting permission");
    final String[] permissions = new String[]{Manifest.permission.CAMERA};
    if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
        Manifest.permission.CAMERA)) {
      ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
      return;
    }
    final Activity thisActivity = this;
    View.OnClickListener listener = new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        ActivityCompat.requestPermissions(thisActivity, permissions,
            RC_HANDLE_CAMERA_PERM);
      }
    };
    Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
        Snackbar.LENGTH_INDEFINITE)
        .setAction(R.string.ok, listener)
        .show();
  }
  /** Crea e inicia la cámara. Tenga en cuenta que esto utiliza una resolución más alta en comparación
   * A otros ejemplos de detección para permitir que el detector de rostros  detecte rostros pequeños
   * A largas distancias.
   */
  private void createCameraSource() {
    Context context = getApplicationContext();
    FaceDetector detector = new FaceDetector.Builder(context)
        .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
        .build();
    detector.setProcessor(
        new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
            .build());
    if (!detector.isOperational()) {// Nota: La primera vez que se instala una aplicación que utiliza la API cara en un dispositivo, GMS
      // descarga una biblioteca nativa en el dispositivo para realizar la detección. Generalmente esto
      // se completa antes de ejecutar la aplicación por primera vez. Pero si esa descarga aún no ha
      // completado, entonces la llamada anterior no detectará ningun rostro.
      // isOperational () se puede usar para comprobar si la biblioteca nativa requerida está actualmente
      // disponible. El detector se pondrá automáticamente en funcionamiento una vez que la biblioteca
      // descarga completa en el dispositivo.
      Log.w(TAG, "Face detector dependencies are not yet available.");
    }
    com.digitalonboarding.camera.CameraSource.Builder build = new com.digitalonboarding.camera.CameraSource.Builder(context, detector)
        .setRequestedPreviewSize(1280, 720)
        .setFacing(Functions.faceFront?CameraSource.CAMERA_FACING_FRONT:CameraSource.CAMERA_FACING_BACK)
        .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
        .setRequestedFps(32.0f);


    mCameraSource = build.setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
        .build();
  }
  /**
   * reiniciar la  camara.
   */
  @Override
  protected void onResume() {
    super.onResume();
    startCameraSource();
  }
  /**
   * DEtener la camara
   */
  @Override
  protected void onPause() {
    super.onPause();
    mPreview.stop();
  }
  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mCameraSource != null) {
      mCameraSource.release();
    }
  }
  /** Callback para el resultado de solicitar permisos. Este método
   * Se invoca para cada llamada en {@link #requestPermissions (String [], int)}.
   * <p>
   * <Strong> Nota: </ strong> Es posible que la interacción de solicitudes de permisos
   * Con el usuario interrumpido. En este caso, recibirá permisos vacíos
   * Y los arrays de resultados que deben tratarse como una cancelación.
   * </ P>
   * @param requestCode El código de solicitud pasó en {@link #requestPermissions (String [], int)}.
   * @param permissions Los permisos solicitados. Nunca nulo.
   * @param grantResults Los resultados de la concesión para los permisos correspondientes
   * Que es {@link PackageManager # PERMISSION_GRANTED}
   * O {@link PackageManager # PERMISSION_DENIED}. Nunca nulo.
   * @see #requestPermissions (String [], int)
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    if (requestCode != RC_HANDLE_CAMERA_PERM && requestCode != RC_HANDLE_CAMERA_PERM2) {
      Log.d(TAG, "Got unexpected permission result: " + requestCode);
      super.onRequestPermissionsResult(requestCode, permissions, grantResults);
      return;
    }
    if ( requestCode == RC_HANDLE_CAMERA_PERM2) {
      if(grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        startActivity(new Intent(this,FaceTrackerActivity.class));
        finish();
        return;
      }else{
        showDialogPermissionDenied(getString(R.string.permiso_almacenar));
        return;
      }
    }
    if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      createCameraSource();
    }else{
      showDialogPermissionDenied(getString(R.string.permiso_tomar_foto));
    }
  }
  public void showDialogPermissionDenied(String msg){
    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {
        finish();
      }
    };
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.permiso_denegado)
        .setMessage(msg)
        .setPositiveButton(R.string.ok, listener)
        .show();
  }
  //==============================================================================================
  // Camera Source Preview
  //==============================================================================================
  /**
   * Inicia o reinicia camara source, si existe. Si la fuente de la cámara no existe todavía
   * (Por ejemplo, porque onResume se llamó antes de que se creara la fuente de la cámara), se llamará
   * De nuevo cuando se crea la camara sorce.
   */
  private void startCameraSource() {
    // Compruebe que el dispositivo tenga servicios de google play disponibles.
    int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
        getApplicationContext());
    if (code != ConnectionResult.SUCCESS) {
      Dialog dlg =
          GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
      dlg.show();
    }
    if (mCameraSource != null) {
      try {
        mPreview.start(mCameraSource, mGraphicOverlay);

      } catch (IOException e) {
        Log.e(TAG, "Unable to start camera source.", e);
        mCameraSource.release();
        mCameraSource = null;
      }
    }
  }
  //==============================================================================================
  // Graphic Face Tracker
  //==============================================================================================
  /**
   * Graphic Face Tracker  para crear un rastreador de rostro para asociarse con una nueva cara. El multiprocesador
   * Utiliza este Graphic Face Tracker para crear rastreadores de rostro según sea necesario - uno para cada individuo.
   */
  private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
    @Override
    public Tracker<Face> create(Face face) {
      return new GraphicFaceTracker(mGraphicOverlay);
    }
  }
  /**
   * Rastreador de rostro para cada individuo detectado. Esto mantiene una cara gráfica dentro de la aplicación
   * asocia a face overlay.
   */
  private boolean takePicture;
  private boolean iniciarTiempo=true;
  private class GraphicFaceTracker extends Tracker<Face> {
    private GraphicOverlay mOverlay;
    private FaceGraphic mFaceGraphic;
    GraphicFaceTracker(GraphicOverlay overlay) {
      mOverlay = overlay;
      mFaceGraphic = new FaceGraphic(overlay);
    }
    /**
     * Comienza a rastrear la instancia de cara detectada dentro de la superposición de rostros.
     */
    @Override
    public void onNewItem(int faceId, Face item) {
      mFaceGraphic.setId(faceId);
    }
    /**
     * actualiza las posciciones del rostro con overlay
     */
    @Override
    public void onUpdate(final FaceDetector.Detections<Face> detectionResults, final Face face) {

      mOverlay.add(mFaceGraphic);
      mFaceGraphic.updateFace(face);
      finalFace = face;
      if (iniciarTiempo){
        iniciarTiempo=false;
        startThreadTakePicture();
      }
      Log.i(TAG, "onUpdate: "+0);
    }

    /**
     * Oculta el gráfico cuando no se detectó la cara correspondiente. Esto puede suceder
     * en Tramas intermedias temporalmente (por ejemplo, si la cara fue momentáneamente bloqueada de vista)
     */
    @Override
    public void onMissing(FaceDetector.Detections<Face> detectionResults) {
      mOverlay.remove(mFaceGraphic);
    }
    /** Se llama cuando la cara se supone que se ha estado permanente. Quite la anotación gráfica de
     * La superposición.
     */
    @Override
    public void onDone() {
      mOverlay.remove(mFaceGraphic);
    }
  }
  Handler miHandler = new Handler();
  private void startThreadTakePicture() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          Thread.sleep(1500);
          miHandler.post(new Runnable() {
            @Override
            public void run() {
              takePiucture(mPreview);
            }
          });
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }).start();
  }
  public void takePiucture(View v){
    final File pictureFile = getOutputMediaFile();
    checkPermissionStorage();
    if (finalFace!=null) {
      try {
        mCameraSource.getCamera().getParameters().setRotation(getRotateImage(getResources().getConfiguration().orientation));
        mCameraSource.takePicture(new CameraSource.ShutterCallback() {
          @Override
          public void onShutter() {}
        }, new CameraSource.PictureCallback() {
          @Override
          public void onPictureTaken(byte[] bytes,Camera camera) {
            mCameraSource.stop();
                    /*
                    Functions.rotation = mPreview.getDisplay().getRotation();
                    finish();*/

            if (pictureFile == null) {
              return;
            }
            try {
              FileOutputStream fos = new FileOutputStream(pictureFile);
              fos.write(bytes);
              fos.close();
              Functions.photo = pictureFile;
              finish();
            } catch (FileNotFoundException e) {
              Toast.makeText(FaceTrackerActivity.this, R.string.no_se_encontro_imagen, Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
              Toast.makeText(FaceTrackerActivity.this, R.string.error_al_guardar_foto, Toast.LENGTH_SHORT).show();
            }
          }
        });
      }catch (NullPointerException e){
        e.printStackTrace();//se finalizó la actividad y no hay contexto
      }

    }else{
      Snackbar.make(findViewById(android.R.id.content),
          R.string.no_se_detecto_rostro,Snackbar.LENGTH_SHORT).show();
    }
  }
  private boolean checkPermissionStorage() {
    int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    if (rc == PackageManager.PERMISSION_GRANTED) {
      return true;
    } else {
      requestStoragePermission();
    }
    return false;
  }
  private File getOutputMediaFile() {
    File mediaStorageDir = new File(
        Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        getString(R.string.directorio));
    if (!mediaStorageDir.exists()) {
      if (!mediaStorageDir.mkdirs()) {
        Log.d("DigitalOnboarding", "failed to create directory");
        return null;
      }
    }
    // Create a media file name
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
        .format(new Date());
    File mediaFile;
    mediaFile = new File(mediaStorageDir.getPath() + File.separator
        + "IMG_" + timeStamp + ".jpg");
    return mediaFile;
  }
  public int getRotateImage(int rotation) {
    if (rotation == 0)
      return 270;
    else if (rotation == 1)
      return 0;
    else if (rotation == 2)
      return 90;
    else if (rotation == 3)
      return 180;
    return 0;
  }
  /**
   * Se encarga de la solicitud del permisode camara. Esto incluye
   * Mostrando un "Snackbar" mensaje de por qué el permiso es necesario entonces
   * Envío de la solicitud.
   */
  private void requestStoragePermission() {
    Log.w(TAG, "storage permission is not granted. Requesting permission");
    final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
      ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM2);
      return;
    }
    final Activity thisActivity = this;
    View.OnClickListener listener = new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        ActivityCompat.requestPermissions(thisActivity, permissions,
            RC_HANDLE_CAMERA_PERM2);
      }
    };
    Snackbar.make(mGraphicOverlay, R.string.es_necesario_guardar_en_almacenamiento,
        Snackbar.LENGTH_INDEFINITE)
        .setAction(R.string.ok, listener)
        .show();
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {

    if (event.getAction() == MotionEvent.ACTION_DOWN){

    }
    return super.onTouchEvent(event);
  }
}

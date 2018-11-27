package com.digitalonboarding;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.afollestad.materialdialogs.MaterialDialog;
import com.digitalonboarding.camera.CameraSource;
import com.digitalonboarding.camera.CameraSourcePreview;
import com.digitalonboarding.camera.GraphicOverlay;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.MultiDetector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.gms.vision.face.FaceDetector;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * Activity for the multi-tracker app.  This app detects faces and barcodes with the rear facing
 * camera, and draws overlay graphics to indicate the position, size, and ID of each face and
 * barcode.
 */
public final class MultiTrackerActivity extends AppCompatActivity {
  private static final String TAG = "MultiTracker";
  private static final int RC_HANDLE_GMS = 9001;
  // permission request codes need to be < 256
  private static final int RC_HANDLE_CAMERA_PERM = 2;
  private CameraSource mCameraSource = null;
  private CameraSourcePreview mPreview;
  private GraphicOverlay mGraphicOverlay;
  private TextView verticalTextView;
  private ImageView mInfoImageView;
  /**
   * Initializes the UI and creates the detector pipeline.
   */
  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    setContentView(R.layout.multitracker_layout);


    mPreview = (CameraSourcePreview) findViewById(R.id.preview);
    mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);
    verticalTextView = (TextView) findViewById(R.id.chronometer);
    mInfoImageView = (ImageView) findViewById(R.id.info);

    // Check for the camera permission before accessing the camera.  If the
    // permission is not granted yet, request permission.
    int rc = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
    if (rc == PackageManager.PERMISSION_GRANTED) {
      createCameraSource();
    } else {
      requestCameraPermission();
    }
    ToggleButton flashSwitch = (ToggleButton) findViewById(R.id.switch_flash);

    /**
     * este metodo sobrescrito es para detectar cuando se chequea el flash para encederlo o apagarlo
     */
    flashSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mCameraSource.setFlashMode(isChecked ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
      }
    });

    mInfoImageView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        new MaterialDialog.Builder(MultiTrackerActivity.this)
            .title(getIntent().getStringExtra("tittle"))
            .content(getIntent().getStringExtra("content"))
            .positiveText(R.string.agree)
            .show();

      }
    });

    findViewById(R.id.btnTakePicture).setVisibility(View.GONE);

    startCameraSource();
  }
  /**
   * Handles the requesting of the camera permission.  This includes
   * showing a "Snackbar" message of why the permission is needed then
   * sending the request.
   */
  private void requestCameraPermission() {
    Log.w(TAG, "Camera permission is not granted. Requesting permission");
    final String[] permissions = new String[]{android.Manifest.permission.CAMERA};
    if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
        android.Manifest.permission.CAMERA)) {
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
  /**
   * Creates and starts the camera.  Note that this uses a higher resolution in comparison
   * to other detection examples to enable the barcode detector to detect small barcodes
   * at long distances.
   */
  private void createCameraSource() {
    Context context = getApplicationContext();
    // A face detector is created to track faces.  An associated multi-processor instance
    // is set to receive the face detection results, track the faces, and maintain graphics for
    // each face on screen.  The factory is used by the multi-processor to create a separate
    // tracker instance for each face.
    FaceDetector faceDetector = new FaceDetector.Builder(context).build();
    FaceTrackerFactory faceFactory = new FaceTrackerFactory(mGraphicOverlay,this);
    faceDetector.setProcessor(
        new MultiProcessor.Builder<>(faceFactory).build());
    // A barcode detector is created to track barcodes.  An associated multi-processor instance
    // is set to receive the barcode detection results, track the barcodes, and maintain
    // graphics for each barcode on screen.  The factory is used by the multi-processor to
    // create a separate tracker instance for each barcode.
    BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context).build();
    BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(mGraphicOverlay,this);
    barcodeDetector.setProcessor(
        new MultiProcessor.Builder<>(barcodeFactory).build());
    // A multi-detector groups the two detectors together as one detector.  All images received
    // by this detector from the camera will be sent to each of the underlying detectors, which
    // will each do face and barcode detection, respectively.  The detection results from each
    // are then sent to associated tracker instances which maintain per-item graphics on the
    // screen.
    MultiDetector multiDetector = new MultiDetector.Builder()
        .add(faceDetector)
        .add(barcodeDetector)
        .build();
    if (!multiDetector.isOperational()) {
      // Note: The first time that an app using the barcode or face API is installed on a
      // device, GMS will download a native libraries to the device in order to do detection.
      // Usually this completes before the app is run for the first time.  But if that
      // download has not yet completed, then the above call will not detect any barcodes
      // and/or faces.
      //
      // isOperational() can be used to check if the required native libraries are currently
      // available.  The detectors will automatically become operational once the library
      // downloads complete on device.
      Log.w(TAG, "Detector dependencies are not yet available.");
      // Check for low storage.  If there is low storage, the native library will not be
      // downloaded, so detection will not become operational.
      IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
      boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;
      if (hasLowStorage) {
        Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
        Log.w(TAG, getString(R.string.low_storage_error));
      }
    }

    // Creates and starts the camera.  Note that this uses a higher resolution in comparison
    // to other detection examples to enable the barcode detector to detect small barcodes
    // at long distances.
    mCameraSource = new CameraSource.Builder(getApplicationContext(), multiDetector)
        .setFacing(CameraSource.CAMERA_FACING_BACK)
        .setRequestedPreviewSize(1280, 720)
        .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
        .setRequestedFps(15.0f)
        .build();

  }
  /**
   * Restarts the camera.
   */
  @Override
  protected void onResume() {
    super.onResume();
    startCameraSource();
  }
  /**
   * Stops the camera.
   */
  @Override
  protected void onPause() {
    super.onPause();
    mPreview.stop();
  }
  /**
   * Releases the resources associated with the camera source, the associated detectors, and the
   * rest of the processing pipeline.
   */
  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mCameraSource != null) {
      mCameraSource.release();
    }
  }
  /**
   * Callback for the result from requesting permissions. This method
   * is invoked for every call on {@link #requestPermissions(String[], int)}.
   * <p>
   * <strong>Note:</strong> It is possible that the permissions request interaction
   * with the user is interrupted. In this case you will receive empty permissions
   * and results arrays which should be treated as a cancellation.
   * </p>
   *
   * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
   * @param permissions  The requested permissions. Never null.
   * @param grantResults The grant results for the corresponding permissions
   *                     which is either {@link PackageManager#PERMISSION_GRANTED}
   *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
   * @see #requestPermissions(String[], int)
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    if (requestCode != RC_HANDLE_CAMERA_PERM) {
      Log.d(TAG, "Got unexpected permission result: " + requestCode);
      super.onRequestPermissionsResult(requestCode, permissions, grantResults);
      return;
    }
    if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      Log.d(TAG, "Camera permission granted - initialize the camera source");
      // we have permission, so create the camerasource
      createCameraSource();
      return;
    }
    Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
        " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {
        finish();
      }
    };
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.error_sin_permiso)
        .setMessage(R.string.no_camera_permission)
        .setPositiveButton(R.string.ok, listener)
        .show();
  }
  /**
   * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
   * (e.g., because onResume was called before the camera source was created), this will be called
   * again when the camera source is created.
   */
  private void startCameraSource() {
    // check that the device has play services available.
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
  @Override
  protected void onResumeFragments() {
    super.onResumeFragments();
    Functions.barcode = null;
  }

  boolean takedPicture = false;
  public void takePicture() {
    if (!takedPicture) {
      takedPicture = true;
      new Handler(Looper.getMainLooper()).post(new Runnable() {
        @Override
        public void run() {
          verticalTextView.setVisibility(View.VISIBLE);
          new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
              verticalTextView.setText("" + millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
              verticalTextView.setVisibility(View.GONE);
                completeStep(3);
              attemptPicture();
            }
          }.start();
        }
      });
    }

  }

  private void attemptPicture() {
    final File pictureFile = getOutputMediaFile();
    mCameraSource.getCamera().getParameters().setRotation(getRotateImage(getResources().getConfiguration().orientation));
    FileOutputStream fos = null;
    try {
      if (pictureFile == null) {
        return;
      }
      fos = new FileOutputStream(pictureFile);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    final FileOutputStream finalFos = fos;
    mCameraSource.takePicture(new CameraSource.ShutterCallback() {
      @Override
      public void onShutter() {}
    }, new CameraSource.PictureCallback() {
      @Override
      public void onPictureTaken(byte[] bytes,Camera camera) {
        try {
          mCameraSource.stop();

          finalFos.write(bytes);
          finalFos.close();
          Functions.photo = pictureFile;
          startActivity(new Intent(getBaseContext(),InformationActivity.class));
          finish();
        } catch (FileNotFoundException e) {
          Toast.makeText(getBaseContext(), R.string.no_encontro_imagen, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
          Toast.makeText(getBaseContext(), R.string.error_al_guardar_foto, Toast.LENGTH_SHORT).show();
        }catch (Exception e){
          e.printStackTrace();
        }
      }
    });
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
  private File getOutputMediaFile() {
    File mediaStorageDir = new File(
        Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        "DigitalOnboarding");
    if (!mediaStorageDir.exists()) {
      if (!mediaStorageDir.mkdirs()) {
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

  public void completeStep(int i){
    switch (i){
      case 1:
        findViewById(R.id.progress1).setVisibility(View.GONE);
        findViewById(R.id.correct1).setVisibility(View.VISIBLE);
        break;
      case 2:
        findViewById(R.id.progress2).setVisibility(View.GONE);
        findViewById(R.id.correct2).setVisibility(View.VISIBLE);
        break;
      case 3:
        findViewById(R.id.progress3).setVisibility(View.GONE);
        findViewById(R.id.correct3).setVisibility(View.VISIBLE);
        break;
    }
  }

}

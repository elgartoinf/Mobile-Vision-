package com.digitalonboarding;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.digitalonboarding.id.templating.ChileIDBackSides;
import com.digitalonboarding.id.templating.MyScanActivity;
import com.digitalonboarding.microblink.PDF417DetectorActivity;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.digitalonboarding.microblink.Config;
import com.microblink.activity.Pdf417ScanActivity;
import com.microblink.activity.ScanActivity;
import com.microblink.activity.ScanCard;
import com.microblink.activity.SegmentScanActivity;
import com.microblink.activity.ShowOcrResultMode;
import com.microblink.recognizers.BaseRecognitionResult;
import com.microblink.recognizers.RecognitionResults;
import com.microblink.recognizers.blinkbarcode.pdf417.Pdf417RecognizerSettings;
import com.microblink.recognizers.blinkbarcode.pdf417.Pdf417ScanResult;
import com.microblink.recognizers.settings.RecognitionSettings;
import com.microblink.recognizers.settings.RecognizerSettings;
import com.microblink.results.barcode.BarcodeDetailedData;
import com.microblink.results.date.DateResult;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

public class Step3Activity extends AppCompatActivity {

    private TextView txtTittle;
    private Button btnCamera;
    private RecognitionSettings recognitionSettings;
    private static final int MY_REQUEST_CODE = 1337;
    public static final int MY_BLINKID_REQUEST_CODE = 0x101;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step3);

        Typeface tf = Typeface.createFromAsset(getAssets(),"fonts/helvetica-neue-light.ttf");
        // ActionBar modificado con la vista custom_actionabar
        ActionBar mActionBar = getSupportActionBar();
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.custom_actionbar, null);
        TextView titulo = (TextView) mCustomView.findViewById(R.id.txtTitulo);
        titulo.setTypeface(tf);
        titulo.setText(R.string.paso3);
        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);
        //Se le asigna una imagen de fondo al actionbar
        BitmapDrawable background = new BitmapDrawable (BitmapFactory.decodeResource(getResources(), R.drawable.fondo_superior));
        mActionBar.setBackgroundDrawable(background);

        txtTittle = (TextView) findViewById(R.id.lblId);
        btnCamera = (Button) findViewById(R.id.btnCamera);

        //se asigna el typeface a los componentes de texto
        txtTittle.setTypeface(tf);
        btnCamera.setTypeface(tf);

        recognitionSettings = new RecognitionSettings();
        // add settings objects to recognizer settings array
        // Pdf417Recognizer and ZXingRecognizer will be used in the recognition process
        recognitionSettings.setRecognizerSettingsArray(
                new RecognizerSettings[]{new Pdf417RecognizerSettings()});
    }

    /**
     * mustra la actividad con la informacion detectada
     * @param v
     */
    public void showInformationDetectedActivity(View v) {
        if (Functions.country.equalsIgnoreCase("Chile")){
            // startActivityForResult(buildTemplatingAPICroIDBackElement().getScanIntent(), MY_BLINKID_REQUEST_CODE);
            // Intent for MyScanActivity
            if (Functions.selected_library == 1) {
                final Intent intent = new Intent(this, MyScanActivity.class);
                startActivityForResult(intent, MY_BLINKID_REQUEST_CODE);
            }else{
                final Intent intent = new Intent(this, OACIChileDetectorActivity.class);
                startActivity(intent);
            }


            }else if (Functions.selected_library == 0) {
                Intent intent = new Intent(this, BarcodeTrackerActivty.class);
                intent.putExtra("tittle", getString(R.string.paso3));
                intent.putExtra("content", "Locate the document in the camera box.\n"
                        + "Remember to focus on it from 10 to 20 centimeters and make sure you are in a bright place.");
                startActivityForResult(intent, 0);
            }else if (Functions.country.equalsIgnoreCase("Colombia")){
                // create intent for custom scan activity
                Intent intent = new Intent(this, PDF417DetectorActivity.class);
                intent.putExtra(Pdf417ScanActivity.EXTRAS_RECOGNITION_SETTINGS, recognitionSettings);

                startActivityForResult(intent, MY_REQUEST_CODE);
            }

        }

        /**
         * This method will build scan intent for BlinkID. Method needs array of recognizer settings
         * to know which recognizers to enable, activity to which intent will be sent and optionally
         * an intent for HelpActivity that will be used if user taps the Help button on scan activity.
         */
    private Intent buildIntent(RecognizerSettings[] settArray, Class<?> target, Intent helpIntent) {
        // first create intent for given activity
        final Intent intent = new Intent(this, target);

        // optionally, if you want the beep sound to be played after a scan
        // add a sound resource id as EXTRAS_BEEP_RESOURCE extra
        intent.putExtra(ScanActivity.EXTRAS_BEEP_RESOURCE, R.raw.beep);

        // if we have help intent, we can pass it to scan activity so it can invoke
        // it if user taps the help button. If we do not set the help intent,
        // scan activity will hide the help button.
        if (helpIntent != null) {
            intent.putExtra(ScanActivity.EXTRAS_HELP_INTENT, helpIntent);
        }

        // prepare the recognition settings
        RecognitionSettings settings = new RecognitionSettings();

        // with setNumMsBeforeTimeout you can define number of miliseconds that must pass
        // after first partial scan result has arrived before scan activity triggers a timeout.
        // Timeout is good for preventing infinitely long scanning experience when user attempts
        // to scan damaged or unsupported slip. After timeout, scan activity will return only
        // data that was read successfully. This might be incomplete data.
        settings.setNumMsBeforeTimeout(2000);

        // If you add more recognizers to recognizer settings array, you can choose whether you
        // want to have the ability to obtain multiple scan results from same video frame. For example,
        // if both payment slip and payment barcode are visible on a single frame, by setting
        // setAllowMultipleScanResultsOnSingleImage to true you can obtain both scan results
        // from barcode and slip. If this is false (default), you will get the first valid result
        // (i.e. first result that contains all required data). Having this option turned off
        // creates better and faster user experience.
//        settings.setAllowMultipleScanResultsOnSingleImage(true);

        // now add array with recognizer settings so that scan activity will know
        // what do you want to scan. Setting recognizer settings array is mandatory.
        settings.setRecognizerSettingsArray(settArray);
        intent.putExtra(ScanActivity.EXTRAS_RECOGNITION_SETTINGS, settings);

        // In order for scanning to work, you must enter a valid licence key. Without licence key,
        // scanning will not work. Licence key is bound the the package name of your app, so when
        // obtaining your licence key from Microblink make sure you give us the correct package name
        // of your app. You can obtain your licence key at http://microblink.com/login or contact us
        // at http://help.microblink.com.
        // Licence key also defines which recognizers are enabled and which are not. Since the licence
        // key validation is performed on image processing thread in native code, all enabled recognizers
        // that are disallowed by licence key will be turned off without any error and information
        // about turning them off will be logged to ADB logcat.
        intent.putExtra(ScanActivity.EXTRAS_LICENSE_KEY, Config.LICENSE_KEY);

        // If you want, you can disable drawing of OCR results on scan activity. Drawing OCR results can be visually
        // appealing and might entertain the user while waiting for scan to complete, but might introduce a small
        // performance penalty.
        // intent.putExtra(ScanActivity.EXTRAS_SHOW_OCR_RESULT, false);

        /// If you want you can have scan activity display the focus rectangle whenever camera
        // attempts to focus, similarly to various camera app's touch to focus effect.
        // By default this is off, and you can turn this on by setting EXTRAS_SHOW_FOCUS_RECTANGLE
        // extra to true.
        intent.putExtra(ScanActivity.EXTRAS_SHOW_FOCUS_RECTANGLE, true);

        // If you want, you can enable the pinch to zoom feature of scan activity.
        // By enabling this you allow the user to use the pinch gesture to zoom the camera.
        // By default this is off and can be enabled by setting EXTRAS_ALLOW_PINCH_TO_ZOOM extra to true.
        intent.putExtra(ScanActivity.EXTRAS_ALLOW_PINCH_TO_ZOOM, true);

        // Enable showing of OCR results as animated dots. This does not have effect if non-OCR recognizer like
        // barcode recognizer is active.
        intent.putExtra(SegmentScanActivity.EXTRAS_SHOW_OCR_RESULT_MODE, (Parcelable) ShowOcrResultMode.ANIMATED_DOTS);

        return intent;
    }

    private ListElement buildTemplatingAPICroIDBackElement() {
        return new ListElement("TemplatingAPI Cro ID Back", buildIntent(new RecognizerSettings[]{ChileIDBackSides.buildCroatianIDBackSideRecognizerSettings()}, ScanCard.class, null));
    }

    /**
     * Element of {@link ArrayAdapter} for {@link ListView} that holds information about title
     * which should be displayed in list and {@link Intent} that should be started on click.
     */
    private class ListElement {
        private String mTitle;
        private Intent mScanIntent;

        public String getTitle() {
            return mTitle;
        }

        public Intent getScanIntent() {
            return mScanIntent;
        }

        public ListElement(String title, Intent scanIntent) {
            mTitle = title;
            mScanIntent = scanIntent;
        }

        /**
         * Used by array adapter to determine list element text
         */
        @Override
        public String toString() {
            return getTitle();
        }
    }

    /**
     * this method is same as in Pdf417MobiDemo project
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode resultado = data.getParcelableExtra("dato");
                    Log.e("barcode Colombia ",resultado.displayValue);
                    String barcode = resultado.displayValue;
                    Intent i = new Intent(this, InformationDetectedActivity.class);
                    i.putExtra("code", barcode);
                    startActivity(i);
                } else {
                    Toast.makeText(this, R.string.no_detecto_codigo, Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == MY_REQUEST_CODE && resultCode == Pdf417ScanActivity.RESULT_OK) {
            // First, obtain recognition result
            RecognitionResults results = data
                    .getParcelableExtra(Pdf417ScanActivity.EXTRAS_RECOGNITION_RESULTS);
            // Get scan results array. If scan was successful, array will contain at least one element.
            // Multiple element may be in array if multiple scan results from single image were allowed in settings.
            BaseRecognitionResult[] resultArray = results.getRecognitionResults();

            StringBuilder sb = new StringBuilder();

            for (BaseRecognitionResult res : resultArray) {
                if (res instanceof Pdf417ScanResult) { // check if scan result is result of Pdf417 recognizer
                    Pdf417ScanResult result = (Pdf417ScanResult) res;
                    // getStringData getter will return the string version of barcode contents
                    String barcodeData = result.getStringData();
                    BarcodeDetailedData rawData = result.getRawData();
                    // BarcodeDetailedData contains information about barcode's binary layout, if you
                    // are only interested in raw bytes, you can obtain them with getAllData getter
                    byte[] rawDataBuffer = rawData.getAllData();
                    barcodeData = decodeUTF8(rawDataBuffer);

                    // if data is URL, open the browser and stop processing result
                    if (checkIfDataIsUrlAndCreateIntent(barcodeData)) {
                        return;
                    } else {

                        sb.append(barcodeData);

                    }
                }
            }


            String barcode = sb.toString();
            Log.e("barcode Colombia ",barcode);
            Intent i = new Intent(this, InformationDetectedActivity.class);
            i.putExtra("code", barcode);
            startActivity(i);
        }if (requestCode == MY_BLINKID_REQUEST_CODE) {

            if (resultCode == Activity.RESULT_OK && data != null) {
                Bundle extras = data.getExtras();
                RecognitionResults mResults = extras.getParcelable(
                        ScanActivity.EXTRAS_RECOGNITION_RESULTS);

                BaseRecognitionResult s = mResults.getRecognitionResults()[0];
//                Iterator var2 = s.getResultHolder().keySet().iterator();

                String names = s.getResultHolder().getObject("SecondaryId").toString();
                String lastNames = s.getResultHolder().getObject("PrimaryId").toString();
                String documentNumber = s.getResultHolder().getObject("DocumentNumber").toString();
                com.microblink.results.date.DateResult dateOfBirth = (DateResult) s.getResultHolder().getObject("DateOfBirth");
                String sex = s.getResultHolder().getObject("Sex").toString();

                Intent i = new Intent(this, InformationDetectedActivity.class);
                i.putExtra("names",names);
                i.putExtra("lastNames",lastNames);
                i.putExtra("documentNumber",documentNumber);
                i.putExtra("dateOfBirth",dateOfBirth.getDate().getDay()+"/"+dateOfBirth.getDate().getMonth()+"/"+dateOfBirth.getDate().getYear());
                i.putExtra("sex",sex);
                startActivity(i);
                Log.e("res result",s.toString());

                /*while(var2.hasNext()) {
                    String var3 = (String)var2.next();
                    Object var4;
                    if((var4 = s.getResultHolder().getObject(var3)) != null) {
                        Log.e("result",var3+": "+var4.toString());

                    }
                }*/
            }else{
                Toast.makeText(this, "cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private final Charset UTF8_CHARSET = Charset.forName("ISO-8859-1");

    String decodeUTF8(byte[] bytes) {
        return new String(bytes, UTF8_CHARSET);
    }


    private boolean checkIfDataIsUrlAndCreateIntent(String data) {
        // if barcode contains URL, create intent for browser
        boolean barcodeDataIsUrl;

        try {
            @SuppressWarnings("unused")
            URL url = new URL(data);
            barcodeDataIsUrl = true;
        } catch (MalformedURLException exc) {
            barcodeDataIsUrl = false;
        }

        if (barcodeDataIsUrl) {
            // create intent for browser
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(data));
            startActivity(intent);
        }

        return barcodeDataIsUrl;
    }
}


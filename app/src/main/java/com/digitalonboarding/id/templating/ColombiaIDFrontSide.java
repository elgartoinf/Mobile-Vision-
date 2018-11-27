package com.digitalonboarding.id.templating;

import android.os.Parcel;
import com.digitalonboarding.id.TemplatingUtils;
import com.microblink.detectors.DecodingInfo;
import com.microblink.detectors.DetectorSettings;
import com.microblink.detectors.document.DocumentDetectorSettings;
import com.microblink.detectors.document.DocumentSpecification;
import com.microblink.detectors.document.DocumentSpecificationPreset;
import com.microblink.detectors.face.FaceDetectorSettings;
import com.microblink.detectors.multi.MultiDetectorSettings;
import com.microblink.geometry.Rectangle;
import com.microblink.recognizers.blinkocr.BlinkOCRRecognitionResult;
import com.microblink.recognizers.blinkocr.BlinkOCRRecognizerSettings;
import com.microblink.recognizers.blinkocr.DocumentClassifier;
import com.microblink.recognizers.blinkocr.engine.BlinkOCREngineOptions;
import com.microblink.recognizers.blinkocr.parser.regex.RegexParserSettings;
import com.microblink.recognizers.templating.TemplatingRecognizerSettings;
import com.microblink.results.ocr.OcrFont;
import java.util.ArrayList;
import java.util.List;

/**
 * This class will show how a Templating API extension of {@link BlinkOCRRecognizerSettings} can be used
 * to scan elements from parts of detection.
 * <p/>
 * This example will show how to setup {@link BlinkOCRRecognizerSettings} to perform detection and
 * data extraction from front side of Croatian ID cards.
 * <p/>
 * The description of how ID cards in Croatia look like can be found on Wikipedia:
 * https://en.wikipedia.org/wiki/Croatian_identity_card
 * <p/>
 * Croatian IDs are credit card sized documents. This example will show you how to setup {@link DocumentDetectorSettings}
 * to perform detection of credit card sized documents and how to perform OCR and data extraction from
 * locations defined by {@link DecodingInfo} objects inherent to detector. Even more, this data will
 * then be used to perform classification of type of Croatian ID document using {@link DocumentClassifier}
 * which will determine whether scanned document is Croatian ID version from year 2003 or from year 2013.
 * After this is determined, specific {@link DecodingInfo} objects will be used to extract other
 * data from locations specific to each version of Croatian ID.
 * <p/>
 * The process of setting up {@link BlinkOCRRecognizerSettings} for scanning front side of Croatian ID
 * is shown in method {@link #buildCroatianIDFrontSideRecognizerSettings()}.
 */
public class ColombiaIDFrontSide {
  private static final String ID_LAST_NAME = "LastName";
  private static final String ID_FIRST_NAME = "FirstName";
  private static final String ID_DOCUMENT_NUMBER = "DocumentNumber";
  private static final String ID_DOCUMENT_NUMBER_NEW = "DocumentNumberNew";
  private static final String CLASS_NEW_ID = "newCroId";


  /**
   * This function will setup first and last name parser and locations of first and last
   * names on front side of Croatian ID cards.
   * <p/>
   * The Croatian ID card has width of 85mm and height of 54mm. If we take a ruler and measure
   * the locations of address field, we get the following measurements:
   * <p/>
   * on old croatian ID card, last name is located in following rectangle:
   * left = 23 mm
   * right = 50 mm
   * top = 11 mm
   * bottom = 17 mm
   * <p/>
   * {@link DecodingInfo} requires converting this into relative coordinates, so we
   * get the following:
   * <p/>
   * x = 23mm / 85mm = 0.271
   * y = 11mm / 54mm = 0.204
   * width = (50mm - 23mm) / 85mm = 0.318
   * height = (17mm - 11mm) / 54mm = 0.111
   * <p/>
   * on new croatian ID card, last name is located in following rectangle:
   * left = 23 mm
   * right = 54 mm
   * top = 11 mm
   * bottom = 20 mm
   * <p/>
   * {@link DecodingInfo} requires converting this into relative coordinates, so we
   * get the following:
   * <p/>
   * x = 23mm / 85mm = 0.271
   * y = 11mm / 54mm = 0.204
   * w = (54mm - 23mm) / 85mm = 0.365
   * h = (20mm - 11mm) / 54mm = 0.167
   * <p/>
   * In the same manner we can measure the locations of first name on both old and new ID cards.
   * <p/>
   * Both first and last name can hold a single line of text, but both on new and old ID card
   * first name is printed with smaller font than last name. Therefore, we will require that
   * dewarped image for last names will be of height 100 pixels and for first names of height 150
   * pixels.
   * The width of the image will be automatically determined to keep the original aspect ratio.
   *
   * @param settings  Settings object to which parser will be set.
   * @param newId     list of {@link DecodingInfo} objects specific for new Croatian ID card
   * @param firstName indicator whether first or last name should be configured (only dewarp height
   *                  and location differs - regular expression for data extraction is the same)
   */
  private static void setupName(TemplatingRecognizerSettings settings, List<DecodingInfo> newId, boolean firstName) {

    String id = firstName ? ID_FIRST_NAME : ID_LAST_NAME;

    int dewarpHeight = firstName ? 150 : 100;

    RegexParserSettings nameParser = new RegexParserSettings("([A-Z]+)\\s([A-Z]+)?");


    settings.addParserToParserGroup(id, id, nameParser);

    //x y w h
    Rectangle locationNew = firstName ? new Rectangle(0.035f, 0.5f, 0.376f, 0.092f) : new Rectangle(0.035f, 0.351f, 0.376f, 0.092f);

    newId.add(new DecodingInfo(locationNew, dewarpHeight, id));
  }


  /**
   * This function will show how to setup scanning of document number from {@link DecodingInfo} objects
   * inherent to detector set with {@link BlinkOCRRecognizerSettings#setDetectorSettings(DetectorSettings)}.
   * <p/>
   * First, we will add two possible locations of document number - one location on old Croatian IDs
   * and one location on new Croatian IDs. After that when card-like document will be detected,
   * both locations will be dewarped and OCR and data extraction will be performed. After that,
   * only one parser will succeed in parsing document number - either one on location for old
   * Croatian ID or one on location for new Croatian ID. This information will then be used
   * in {@link CroFrontIdClassifier} to determine whether the scanned document is old or new
   * Croatian ID and appropriate array of {@link DecodingInfo} objects can then be used
   * to extract other data as set up in functions setupName(TemplatingRecognizerSettings, List, List, boolean)}
   * and setupSexCitizenshipAndDateOfBirth( TemplatingRecognizerSettings, List, List)}.
   *
   * @param settings      Settings object to which parser will be set.
   * @param decodingInfos List of {@link DecodingInfo} objects inherent to the detector.
   */
  private static void setupDocumentNumber(TemplatingRecognizerSettings settings, List<DecodingInfo> decodingInfos) {
    /**
     * First define locations of document number on both old and new Croatian ID cards. Make sure you use different
     * names to later be able to distinguish which location produced result and which did not.
     */
    decodingInfos.add(new DecodingInfo(new Rectangle(0.152f, 0.277f, 0.352f, 0.092f), 150, ID_DOCUMENT_NUMBER_NEW));

    /**
     * Document number on Croatian ID is 9-digit number. We will extract that with simple
     * regex parser which only allows digits in OCR engine settings.
     */
    RegexParserSettings documentNumberParser = new RegexParserSettings("([\\d]{1,3}\\.?)+");
    ((BlinkOCREngineOptions)documentNumberParser.getOcrEngineOptions()).addAllDigitsToWhitelist(OcrFont.OCR_FONT_ANY);
    documentNumberParser.getOcrEngineOptions().setMinimumCharHeight(35);

    /**
     * It is important to add that parser to both parser groups associated with both decoding infos set above.
     */
    settings.addParserToParserGroup(ID_DOCUMENT_NUMBER_NEW, ID_DOCUMENT_NUMBER, documentNumberParser);
  }

  /**
   * This method shows how {@link BlinkOCRRecognizerSettings} can be set up to scan front side of
   * Croatian ID cards. At first, parser groups for first and last name and other data (sex, citizenship,
   * date of birth) are created, together with associated parsers (see {@link com.microblink.recognizers.blinkocr.parser.OcrParserSettings})
   * and {@link DecodingInfo} objects specific for old and new Croatian ID cards.
   * Next, a list of {@link DecodingInfo} objects inherent to detector will be prepared and detector
   * which can detect credit card sized documents will be set to be used. Finally, a {@link DocumentClassifier}
   * will be implemented which will determine which version of Croatian ID document is being scanned.
   *
   * @return set-up {@link BlinkOCRRecognizerSettings} object which can scan front side of Croatian ID cards
   */
  public static BlinkOCRRecognizerSettings buildCroatianIDFrontSideRecognizerSettings() {
    // settings object which will be set up
    BlinkOCRRecognizerSettings settings = new BlinkOCRRecognizerSettings();

    // list of decoding info objects for old Croatian ID
    List<DecodingInfo> oldIdDecodingInfos = new ArrayList<>();
    // list of decoding info objects for new Croatian ID
    List<DecodingInfo> newIdDecodingInfos = new ArrayList<>();
    // list of decoding info objects inherent to detector and used for
    // parsing document number and ID type classification
    /**
     * NOTE: if you do not need document classification in your use case,
     *       you only need to set decoding info objects inherent to
     *       detector you will be using. Locations from these objects
     *       are always analysed, while locations set with
     *       #setParserDecodingInfos(DecodingInfo[], String) are only used
     *       after DocumentClassifier returns the same string.
     */
    List<DecodingInfo> classificationDecodingInfos = new ArrayList<>();

    // call methods for setting up locations and parsers for all fields
    setupName(settings,  newIdDecodingInfos, false);
    setupName(settings,  newIdDecodingInfos, true);
    setupDocumentNumber(settings, classificationDecodingInfos);

    // setup card detector
    DocumentSpecification idSpec = DocumentSpecification.createFromPreset(DocumentSpecificationPreset.DOCUMENT_SPECIFICATION_PRESET_ID1_CARD);
    // set decoding info objects inherent to this document specification
    idSpec.setDecodingInfos(TemplatingUtils.listToArray(classificationDecodingInfos));
    // create card detector with single document specification
    DocumentDetectorSettings dds = new DocumentDetectorSettings(new DocumentSpecification[]{idSpec});

    // ensure this detector will be used when performing object detection
    MultiDetectorSettings mds =  new MultiDetectorSettings( new DetectorSettings [] {dds, setupDetector()});

    settings.setDetectorSettings(mds);

    // set classifier which will analyse recognition result obtained from decoding locations
    // inherent to detector (set above) and return either string CLASS_OLD_ID or string CLASS_NEW_ID,
    // depending on whether it has identified old or new Croatian ID.
    settings.setDocumentClassifier(new CroFrontIdClassifier());
    // set decoding info classes
    // After document classification is performed, only one array of decoding info objects will
    // be used to extract information like first and last name etc.
    settings.setParserDecodingInfos(TemplatingUtils.listToArray(newIdDecodingInfos), CLASS_NEW_ID);

    /**
     * If detector which cannot determine orientation is used, like in this case*, allow
     * flipped recognition. This will ensure that after detection has been performed and nothing
     * was extracted from any of the decoding locations inherent to the detector, the detection
     * will be flipped and process will be repeated. This is slower, but enables scanning of
     * Croatian IDs which are held upside down.
     *
     * * DocumentDetector performs detection based on document edges. Since documents are symmetric,
     *   it cannot know the correct orientation of the text. Some other detectors, like
     *   MRTDDetector, have the ability to know the correct orientation of the text on document.
     */
    settings.setAllowFlippedRecognition(true);

    return settings;
  }

  private static DetectorSettings setupDetector() {
    // following constructor initializes FaceDetector settings
    // and requests height of dewarped image to be 300 pixels
    FaceDetectorSettings settings = new FaceDetectorSettings(300);
    return settings;
  }
  /**
   * This class implements {@link DocumentClassifier} interface. In its {@link #classifyDocument(BlinkOCRRecognitionResult)}
   * method it must decide from {@link BlinkOCRRecognitionResult} containing data extracted from
   * locations defined with {@link DecodingInfo} objects inherent to detector used whether document
   * being scanned is old or new Croatian ID. If document cannot be classified, empty string or
   * null can be returned.
   */
  private static class CroFrontIdClassifier implements DocumentClassifier {

    @Override
    public String classifyDocument(BlinkOCRRecognitionResult extractionResult) {

        return CLASS_NEW_ID;

    }

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public CroFrontIdClassifier() {
    }

    protected CroFrontIdClassifier(Parcel in) {
    }

    /**
     * {@link DocumentClassifier} interface extends {@link android.os.Parcelable} so it can
     * be sent via Intent inside {@link BlinkOCRRecognizerSettings}. In order to be able to
     * extract the classifier from {@link Parcel}, {@link #CREATOR} field must be defined.
     */
    public static final Creator<CroFrontIdClassifier> CREATOR = new Creator<ColombiaIDFrontSide.CroFrontIdClassifier>() {
      @Override
      public ColombiaIDFrontSide.CroFrontIdClassifier createFromParcel(Parcel source) {
        return new ColombiaIDFrontSide.CroFrontIdClassifier(source);
      }

      @Override
      public ColombiaIDFrontSide.CroFrontIdClassifier[] newArray(int size) {
        return new ColombiaIDFrontSide.CroFrontIdClassifier[size];
      }
    };
  }
}

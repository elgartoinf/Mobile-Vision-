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
package com.digitalonboarding.ocr;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.SparseArray;

import com.digitalonboarding.InformationDetectedActivity;
import com.digitalonboarding.OACIChileDetectorActivity;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A very simple Processor which receives detected TextBlocks and adds them to the overlay
 * as OcrGraphics.
 */
public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {

    private GraphicOverlay<OcrGraphic> mGraphicOverlay;
    private Context ctx;

    public OcrDetectorProcessor(GraphicOverlay<OcrGraphic> ocrGraphicOverlay, Context ctx) {
        mGraphicOverlay = ocrGraphicOverlay;
        this.ctx = ctx;
    }

    /**
     * Called by the detector to deliver detection results.
     * If your application called for it, this could be a place to check for
     * equivalent detections by tracking TextBlocks that are similar in location and content from
     * previous frames, or reduce noise by eliminating TextBlocks that have not persisted through
     * multiple detections.
     */
    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        mGraphicOverlay.clear();
        SparseArray<TextBlock> items = detections.getDetectedItems();
        for (int i = 0; i < items.size(); ++i) {
            TextBlock item = items.valueAt(i);
            OcrGraphic graphic = new OcrGraphic(mGraphicOverlay, item);
            mGraphicOverlay.add(graphic);
            String item2 = item.getValue().replace("\n","");
            item2 = item2.replace(" ","");
            if (item2.length()>60)
                validateLine(item2);
        }

    }

    public void validateLine(String line){
        Log.e("line",line);
        // String to be scanned to find the pattern.
            String pattern = "^I[A-Z]CHL([0-9]{8,10})[0-9][Ss][0-9]{2}[<Kcs*x]{6,12}([0-9]{6})[0-9]([MF])[0-9]{7}(CHL|MEX)[A-Z0-9][0-9]{7,9}([<Kcsx*][0-9]){1,2}((([A-Z]{2,10}[<Kcsx*]?){1,4})[<Kcsx*]{2}(([A-Z]{3,}[<Kcsx*]?){1,3}))$";

        // Create a Pattern object
        Pattern r = Pattern.compile(pattern);



        // Now create matcher object.
        Matcher m = r.matcher(line);
        int i = 0;
        Intent mIntent = new Intent(ctx, InformationDetectedActivity.class);
        while (m.find()) {
            Log.e("--",m.group());

            mIntent.putExtra("names",split(m.group(10)));
            mIntent.putExtra("lastNames",split(m.group(7)));
            mIntent.putExtra("documentNumber",m.group(1));
            //731022
            char[] d = m.group(2).toCharArray();
            String year = new String(d, 0, 2);
            String month = new String(d, 2, 2);
            String day = new String(d, 4, 2);
            mIntent.putExtra("dateOfBirth",String.format("19%s/%s/%s",year,month,day));
            mIntent.putExtra("sex",m.group(3));
            ctx.startActivity(mIntent);
            ((OACIChileDetectorActivity)ctx).finish();
        }

    }

    /**
     * este metodo se encarga de separar los nombres para extraer los
     * nombres
     */
    public String split(String n){
        Log.e("names ",n);
        String[] s = n.split("");
        String outString = s[0];
        boolean findK = false;

        for(int i = 1; i<s.length;i++){

            if(s[i].equals("<")){
                outString+=" ";
                findK=true;
            }else if(s[i].equals("K") && !findK){
                outString+=" ";
                findK=true;
            }else if(s[i].equals("K") && findK){
                outString+="K";
                findK = false;
            }else{
                outString+=s[i];
                findK = false;
            }
        }

        return outString;
    }


    /**
     * Frees the resources associated with this detection processor.
     */
    @Override
    public void release() {
        mGraphicOverlay.clear();
    }
}

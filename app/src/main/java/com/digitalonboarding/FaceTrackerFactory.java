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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import android.util.Log;
import com.digitalonboarding.camera.GraphicOverlay;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;

/**
 * Factory for creating a tracker and associated graphic to be associated with a new face.  The
 * multi-processor uses this factory to create face trackers as needed -- one for each individual.
 */
class FaceTrackerFactory implements MultiProcessor.Factory<Face> {
  private GraphicOverlay mGraphicOverlay;
  private MultiTrackerActivity context;
  FaceTrackerFactory(GraphicOverlay graphicOverlay,MultiTrackerActivity context) {
    mGraphicOverlay = graphicOverlay;
    this.context = context;
  }

  @Override
  public Tracker<Face> create(Face face) {
    FaceGraphic graphic = new FaceGraphic(mGraphicOverlay,context);
    return new GraphicTracker<>(mGraphicOverlay, graphic);
  }
}

/**
 * Graphic instance for rendering face position, size, and ID within an associated graphic overlay
 * view.
 */
class FaceGraphic extends TrackedGraphic<Face> {
  private static final float FACE_POSITION_RADIUS = 10.0f;
  private static final float ID_TEXT_SIZE = 40.0f;
  private static final float ID_Y_OFFSET = 50.0f;
  private static final float ID_X_OFFSET = -90.0f;
  private static final float BOX_STROKE_WIDTH = 5.0f;

  private static final int COLOR_CHOICES[] = {
      Color.CYAN,
      Color.RED
  };
  private static int mCurrentColorIndex = 0;

  private Paint mFacePositionPaint;
  private Paint mIdPaint;
  private Paint mBoxPaint;

  private volatile Face mFace;
  private MultiTrackerActivity context;
  FaceGraphic(GraphicOverlay overlay, MultiTrackerActivity context) {
    super(overlay);
    this.context = context;
    mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
    final int selectedColor = COLOR_CHOICES[0];

    mFacePositionPaint = new Paint();
    mFacePositionPaint.setColor(selectedColor);

    mIdPaint = new Paint();
    mIdPaint.setColor(selectedColor);
    mIdPaint.setTextSize(ID_TEXT_SIZE);

    mBoxPaint = new Paint();
    mBoxPaint.setColor(selectedColor);
    mBoxPaint.setStyle(Paint.Style.STROKE);
    mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);


  }

  /**
   * Updates the face instance from the detection of the most recent frame.  Invalidates the
   * relevant portions of the overlay to trigger a redraw.
   */

  void updateItem(Face face) {
    mFace = face;
    if (Functions.barcode!=null){
      Functions.face = mFace;

      context.takePicture();
    }
    postInvalidate();
  }

  /**
   * Draws the face annotations for position, size, and ID on the supplied canvas.
   */
  @Override
  public void draw(Canvas canvas) {


    Face face = mFace;
    if (face == null) {
      return;
    }
    context.completeStep(1);


    // Draws a circle at the position of the detected face, with the face's track id below.
    float cx = translateX(face.getPosition().x + face.getWidth() / 2);
    float cy = translateY(face.getPosition().y + face.getHeight() / 2);

    // Draws an oval around the face.
    float xOffset = scaleX(face.getWidth() / 2.0f);
    float yOffset = scaleY(face.getHeight() / 2.0f);
    float left = cx - xOffset;
    float top = cy - yOffset;
    float right = cx + xOffset;
    float bottom = cy + yOffset;



    String result = "Correct";
    if (left < 0 || top < 0 || bottom > canvas.getHeight() || right > canvas.getWidth()){
      final int selectedColor = COLOR_CHOICES[1];
      mBoxPaint.setColor(selectedColor);
      mFacePositionPaint.setColor(selectedColor);
      mIdPaint.setColor(selectedColor);
      result = "Incorrect";
    }else{
      final int selectedColor = COLOR_CHOICES[0];
      mBoxPaint.setColor(selectedColor);
      mFacePositionPaint.setColor(selectedColor);
      mIdPaint.setColor(selectedColor);
    }

    canvas.drawCircle(cx, cy, FACE_POSITION_RADIUS, mFacePositionPaint);
    //canvas.drawText("id: " + getId(), cx + ID_X_OFFSET, cy + ID_Y_OFFSET, mIdPaint);
    canvas.drawText(result, cx + ID_X_OFFSET, cy + ID_Y_OFFSET, mIdPaint);


    //Log.e("Face ", String.format("left = %s top= %s right=%s bottom =%s",left,top,right,bottom));


    //canvas.drawOval(left, top, right, bottom, mBoxPaint);
    canvas.drawRect(left, top, right, bottom, mBoxPaint);
  }





}

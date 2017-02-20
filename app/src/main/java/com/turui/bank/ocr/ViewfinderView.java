/*
 * Copyright (C) 2008 ZXing authors
 * Download by http://www.codefans.net
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

package com.turui.bank.ocr;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;


/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 */
public final class ViewfinderView extends View {

  private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
  private static final long ANIMATION_DELAY = 100L;
  private static final int OPAQUE = 0xFF;

  private Bitmap resultBitmap;
  private int scannerAlpha;
  int linewidth = 12;
  public int isleft = 0, isright =0, istop =0, isbottom =0;
  // This constructor is used when the class is built from an XML resource.
  public ViewfinderView(Context context, AttributeSet attrs) {
    super(context, attrs);

    // Initialize these once for performance rather than calling them every time in onDraw().
    Resources resources = getResources();
    scannerAlpha = 0;
  }
  public ViewfinderView(Context context) {
	    super(context);

	    // Initialize these once for performance rather than calling them every time in onDraw().
	    Resources resources = getResources();
	    scannerAlpha = 0;
	  }

  Paint paint = new Paint();
  {
	  paint.setAntiAlias(true);
	  paint.setColor(0xF8F504);
	  paint.setStyle(Style.STROKE);
	  paint.setStrokeWidth(linewidth);
	  paint.setAlpha(255);
  };
  Paint paint2 = new Paint();
  {
	  paint2.setAntiAlias(true);
	  paint2.setColor(0xE31700);
	  paint2.setStyle(Style.STROKE);
	  paint2.setStrokeWidth(linewidth );
	  paint2.setAlpha(255);
  };
  Paint paint1 = new Paint();
  @Override
  public void onDraw(Canvas canvas) {
    Rect rect = CameraManager.get().getFramingRect();
    if (rect == null) {
      return;
    }
    int width = canvas.getWidth();
    int height = canvas.getHeight();
    int nHeihgt = 10;
    // Draw the exterior (i.e. outside the framing rect) darkened
	if (this.istop == 1) {
		paint2.setAlpha(255);
		canvas.drawLine(rect.left + height / nHeihgt, rect.top, rect.right - height / nHeihgt, rect.top, paint2);
	}
	else {
		paint2.setAlpha(0);
		canvas.drawLine(rect.left + height / nHeihgt, rect.top, rect.right - height / nHeihgt, rect.top, paint2);
	}
	
	if (this.isleft == 2) {
		paint2.setAlpha(255);
		canvas.drawLine(rect.left, rect.top + height / nHeihgt, rect.left, rect.bottom - height / nHeihgt, paint2);
	}
	else {
		paint2.setAlpha(0);
		canvas.drawLine(rect.left, rect.top + height / nHeihgt, rect.left, rect.bottom - height / nHeihgt, paint2);
	}
	if (this.isbottom == 4) {
		paint2.setAlpha(255);
		canvas.drawLine(rect.left + height / nHeihgt, rect.bottom, rect.right - height / nHeihgt, rect.bottom, paint2);
	}
	else {
		paint2.setAlpha(0);
		canvas.drawLine(rect.left + height / nHeihgt, rect.bottom, rect.right - height / nHeihgt, rect.bottom, paint2);
	}
	if (this.isright == 8) {
		paint2.setAlpha(255);
		canvas.drawLine(rect.right, rect.top + height /nHeihgt, rect.right, rect.bottom - height / nHeihgt, paint2);
	}
	else {
		paint2.setAlpha(0);
		canvas.drawLine(rect.right, rect.top + height /nHeihgt, rect.right, rect.bottom - height / nHeihgt, paint2);
	}
	paint.setAlpha(255);
	paint1.setColor(resultBitmap != null ? Color.parseColor("#b0000000") : Color.parseColor("#60000000"));
    canvas.drawRect(0, 0, width, rect.top-linewidth/2, paint1);
    canvas.drawRect(0, rect.top - linewidth/2 , rect.left - linewidth/2, rect.bottom  + linewidth/2 , paint1);
    canvas.drawRect(rect.right + linewidth/2, rect.top - linewidth/2 , width, rect.bottom + + linewidth/2, paint1);
    canvas.drawRect(0, rect.bottom + linewidth/2, width, height, paint1);
    
    
	canvas.drawLine(rect.left, rect.top, rect.left, rect.top + height / nHeihgt , paint);
	canvas.drawLine(rect.left, rect.bottom - height / nHeihgt, rect.left, rect.bottom , paint);
	canvas.drawLine(rect.left - linewidth/2, rect.top, rect.left + height / nHeihgt, rect.top, paint);
	canvas.drawLine(rect.left- linewidth/2, rect.bottom, rect.left + height / nHeihgt, rect.bottom, paint);
	
	canvas.drawLine(rect.right, rect.top, rect.right, rect.top + height / nHeihgt , paint);
	canvas.drawLine(rect.right, rect.bottom - height / nHeihgt, rect.right, rect.bottom , paint);
	canvas.drawLine(rect.right - height / nHeihgt, rect.top, rect.right + linewidth/2, rect.top, paint);
	canvas.drawLine(rect.right - height / nHeihgt, rect.bottom, rect.right + linewidth/2, rect.bottom, paint);
	
  }
  public void SetEdgeVal(int val)
  {
		this.istop = val & 1;
		this.isleft = val & 2;
		this.isbottom = val & 4;
		this.isright = val & 8;
		
  }
  public void drawViewfinder() {
    resultBitmap = null;
    invalidate();
  }

  /**
   * Draw a bitmap with the result points highlighted instead of the live scanning display.
   *
   * @param barcode An image of the decoded barcode.
   */
  public void drawResultBitmap(Bitmap barcode) {
    resultBitmap = barcode;
    invalidate();
  }

 
}

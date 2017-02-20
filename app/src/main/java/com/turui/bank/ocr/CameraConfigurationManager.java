/*
 * Copyright (C) 2010 ZXing authors
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
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.idcard.TengineID;

import java.util.List;
import java.util.regex.Pattern;

final class CameraConfigurationManager {

  private static final String TAG = CameraConfigurationManager.class.getSimpleName();

  private static final int TEN_DESIRED_ZOOM = 27;
  private static final int DESIRED_SHARPNESS = 30;

  private static final Pattern COMMA_PATTERN = Pattern.compile(",");

  private final Context context;
  private Point screenResolution;
  private Point cameraResolution;
  private int previewFormat;
//  private int valMaxSize = 1500;
  private String previewFormatString;

  CameraConfigurationManager(Context context) {
    this.context = context;
  }

  /**
   * Reads, one time, values from the camera that are needed by the app.
   */
	private Size getSizeForPreviewSize(Camera.Parameters parameters){
		List<Size> ViewSize = parameters.getSupportedPreviewSizes();
//		List<Size> picSize = parameters.getSupportedPictureSizes();
//		List<Size> MaxSizes = new ArrayList<Camera.Size>();
//		List<Size> MinSize = new ArrayList<Camera.Size>();
		Size tempSize;
		
		for (int j = ViewSize.size() - 1; j >= 0; j--) {
			for (int i = 0; i < j; i++) {
				if(ViewSize.get(i).width > ViewSize.get(i + 1).width){
					tempSize = ViewSize.get(i + 1);	
					ViewSize.set(i + 1, ViewSize.get(i));
					ViewSize.set(i,tempSize);
				}
			}
		}
		
		for (int i = ViewSize.size() - 1; i >= 0 ; i -- )
		{
			if (CaptureActivity.tengineID == TengineID.TIDCARD2) {
				if(ViewSize.get(i).width > 1280)
				{
					continue;
				}
				else
				{
					return ViewSize.get(i);
				}
			}
			else {
				if(ViewSize.get(i).width > 640)
				{
					continue;
				}
				else
				{
					return ViewSize.get(i);
				}
			}
			
		}
		
		return null;
		
//		for (int j = picSize.size() - 1; j >= 0; j--) {
//			for (int i = 0; i < j; i++) {
//				if(picSize.get(i).width > picSize.get(i + 1).width){
//					tempSize = picSize.get(i + 1);
//					picSize.set(i + 1, picSize.get(i));
//					picSize.set(i,tempSize);
//				}
//			}
//		}
//		
//		for (int i = 0; i < picSize.size(); i++) {
//			if(picSize.get(i).width >= valMaxSize ){
//				MaxSizes.add(picSize.get(i));
//			}else{
//				MinSize.add(picSize.get(i));
//			}
//		}
//		
//		
//		for (int i = ViewSize.size() - 1; i >= 0 ; i -- ) {
//			if(ViewSize.get(i).width <= valMaxSize){
//				for (int j = 0; j < MaxSizes.size(); j++) {
//					if((double)ViewSize.get(i).width / ViewSize.get(i).height 
//							== (double)MaxSizes.get(j).width / MaxSizes.get(j).height){
//
//						parameters.setPictureSize(MaxSizes.get(j).width,MaxSizes.get(j).height);
//						return ViewSize.get(i);
//					}
//				}
//			}
//		}
//		for (int i = ViewSize.size() - 1; i >= 0 ; i -- ) {
//			if(ViewSize.get(i).width <= valMaxSize){
//				for (int j = MinSize.size() - 1; j >= 0; j--) {
//					if(ViewSize.get(i).width / ViewSize.get(i).height 
//							== MinSize.get(j).width / MinSize.get(j).height){
//						parameters.setPictureSize(MinSize.get(j).width,MinSize.get(j).height);
//						return ViewSize.get(i);
//					}
//				}
//			}
//		}
//
//		return null;
	}
  void initFromCameraParameters(Camera camera) {
    Camera.Parameters parameters = camera.getParameters();
    previewFormat = parameters.getPreviewFormat();
    previewFormatString = parameters.get("preview-format");
    Log.d(TAG, "Default preview format: " + previewFormat + '/' + previewFormatString);
    WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    Display display = manager.getDefaultDisplay();
    screenResolution = new Point(display.getWidth(), display.getHeight());
    Log.d(TAG, "Screen resolution: " + screenResolution);
    Size size = getSizeForPreviewSize(camera.getParameters());
    if (size != null) {
    	cameraResolution = new Point();
    	cameraResolution.x = size.width;
    	cameraResolution.y = size.height;
	}
    else {
    	cameraResolution = getCameraResolution(parameters, screenResolution);
	}
    Log.d(TAG, "Camera resolution: " + screenResolution);
  }

  /**
   * Sets the camera up to take preview images which are used for both preview and decoding.
   * We detect the preview format here so that buildLuminanceSource() can build an appropriate
   * LuminanceSource subclass. In the future we may want to force YUV420SP as it's the smallest,
   * and the planar Y can be used for barcode scanning without a copy in some cases.
   */
  void setDesiredCameraParameters(Camera camera) {
    Camera.Parameters parameters = camera.getParameters();
    Log.d(TAG, "Setting preview size: " + cameraResolution);
    parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);
    setFlash(parameters);
    setZoom(parameters);
    //setSharpness(parameters);
    //modify here
    parameters.set("rotation", 0);  	
    camera.setDisplayOrientation(0);  
//    camera.setDisplayOrientation(90);
    camera.setParameters(parameters);
  }

  Point getCameraResolution() {
    return cameraResolution;
  }

  Point getScreenResolution() {
    return screenResolution;
  }

  int getPreviewFormat() {
    return previewFormat;
  }

  String getPreviewFormatString() {
    return previewFormatString;
  }

  private static Point getCameraResolution(Camera.Parameters parameters, Point screenResolution) {

    String previewSizeValueString = parameters.get("preview-size-values");
    // saw this on Xperia
    if (previewSizeValueString == null) {
      previewSizeValueString = parameters.get("preview-size-value");
    }

    Point cameraResolution = null;

    if (previewSizeValueString != null) {
      Log.d(TAG, "preview-size-values parameter: " + previewSizeValueString);
      cameraResolution = findBestPreviewSizeValue(previewSizeValueString, screenResolution);
    }

    if (cameraResolution == null) {
      // Ensure that the camera resolution is a multiple of 8, as the screen may not be.
      cameraResolution = new Point(
          (screenResolution.x >> 3) << 3,
          (screenResolution.y >> 3) << 3);
    }

    return cameraResolution;
  }

  private static Point findBestPreviewSizeValue(CharSequence previewSizeValueString, Point screenResolution) {
    int bestX = 0;
    int bestY = 0;
    int diff = Integer.MAX_VALUE;
    for (String previewSize : COMMA_PATTERN.split(previewSizeValueString)) {

      previewSize = previewSize.trim();
      int dimPosition = previewSize.indexOf('x');
      if (dimPosition < 0) {
        Log.w(TAG, "Bad preview-size: " + previewSize);
        continue;
      }

      int newX;
      int newY;
      try {
        newX = Integer.parseInt(previewSize.substring(0, dimPosition));
        newY = Integer.parseInt(previewSize.substring(dimPosition + 1));
      } catch (NumberFormatException nfe) {
        Log.w(TAG, "Bad preview-size: " + previewSize);
        continue;
      }

      int newDiff = Math.abs(newX - screenResolution.x) + Math.abs(newY - screenResolution.y);
      if (newDiff == 0) {
        bestX = newX;
        bestY = newY;
        break;
      } else if (newDiff < diff) {
        bestX = newX;
        bestY = newY;
        diff = newDiff;
      }

    }

    if (bestX > 0 && bestY > 0) {
      return new Point(bestX, bestY);
    }
    return null;
  }

  private static int findBestMotZoomValue(CharSequence stringValues, int tenDesiredZoom) {
    int tenBestValue = 0;
    for (String stringValue : COMMA_PATTERN.split(stringValues)) {
      stringValue = stringValue.trim();
      double value;
      try {
        value = Double.parseDouble(stringValue);
      } catch (NumberFormatException nfe) {
        return tenDesiredZoom;
      }
      int tenValue = (int) (10.0 * value);
      if (Math.abs(tenDesiredZoom - value) < Math.abs(tenDesiredZoom - tenBestValue)) {
        tenBestValue = tenValue;
      }
    }
    return tenBestValue;
  }

  private void setFlash(Camera.Parameters parameters) {
    // FIXME: This is a hack to turn the flash off on the Samsung Galaxy.
    // And this is a hack-hack to work around a different value on the Behold II
    // Restrict Behold II check to Cupcake, per Samsung's advice
    //if (Build.MODEL.contains("Behold II") &&
    //    CameraManager.SDK_INT == Build.VERSION_CODES.CUPCAKE) {
    if (Build.MODEL.contains("Behold II") && CameraManager.SDK_INT == 3) { // 3 = Cupcake
      parameters.set("flash-value", 1);
    } else {
      parameters.set("flash-value", 2);
    }
    // This is the standard setting to turn the flash off that all devices should honor.
    parameters.set("flash-mode", "off");
  }

  private void setZoom(Camera.Parameters parameters) {

    String zoomSupportedString = parameters.get("zoom-supported");
    if (zoomSupportedString != null && !Boolean.parseBoolean(zoomSupportedString)) {
      return;
    }

    int tenDesiredZoom = TEN_DESIRED_ZOOM;

    String maxZoomString = parameters.get("max-zoom");
    if (maxZoomString != null) {
      try {
        int tenMaxZoom = (int) (10.0 * Double.parseDouble(maxZoomString));
        if (tenDesiredZoom > tenMaxZoom) {
          tenDesiredZoom = tenMaxZoom;
        }
      } catch (NumberFormatException nfe) {
        Log.w(TAG, "Bad max-zoom: " + maxZoomString);
      }
    }

    String takingPictureZoomMaxString = parameters.get("taking-picture-zoom-max");
    if (takingPictureZoomMaxString != null) {
      try {
        int tenMaxZoom = Integer.parseInt(takingPictureZoomMaxString);
        if (tenDesiredZoom > tenMaxZoom) {
          tenDesiredZoom = tenMaxZoom;
        }
      } catch (NumberFormatException nfe) {
        Log.w(TAG, "Bad taking-picture-zoom-max: " + takingPictureZoomMaxString);
      }
    }

    String motZoomValuesString = parameters.get("mot-zoom-values");
    if (motZoomValuesString != null) {
      tenDesiredZoom = findBestMotZoomValue(motZoomValuesString, tenDesiredZoom);
    }

    String motZoomStepString = parameters.get("mot-zoom-step");
    if (motZoomStepString != null) {
      try {
        double motZoomStep = Double.parseDouble(motZoomStepString.trim());
        int tenZoomStep = (int) (10.0 * motZoomStep);
        if (tenZoomStep > 1) {
          tenDesiredZoom -= tenDesiredZoom % tenZoomStep;
        }
      } catch (NumberFormatException nfe) {
        // continue
      }
    }

    // Set zoom. This helps encourage the user to pull back.
    // Some devices like the Behold have a zoom parameter
    if (maxZoomString != null || motZoomValuesString != null) {
      parameters.set("zoom", String.valueOf(tenDesiredZoom / 10.0));
    }

    // Most devices, like the Hero, appear to expose this zoom parameter.
    // It takes on values like "27" which appears to mean 2.7x zoom
    if (takingPictureZoomMaxString != null) {
      parameters.set("taking-picture-zoom", tenDesiredZoom);
    }
  }

	public static int getDesiredSharpness() {
		return DESIRED_SHARPNESS;
	}

}

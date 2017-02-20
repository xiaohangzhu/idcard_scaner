/*
 * Copyright (C) 2008 ZXing authors
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

package com.turui.bank.ocr;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Build;
import android.os.Handler;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import java.io.IOException;

/**
 * This object wraps the Camera service object and expects to be the only one
 * talking to it. The implementation encapsulates the steps needed to take
 * preview-sized images, which are used for both preview and decoding.
 * 
 */

/**
 * This object wraps the Camera service object and expects to be the only one
 * talking to it. The implementation encapsulates the steps needed to take
 * preview-sized images, which are used for both preview and decoding.
 *
 */
public final class CameraManager {

	private static final String TAG = CameraManager.class.getSimpleName();


	private static CameraManager cameraManager;

	private Parameters mParameters;

	static final int SDK_INT; // Later we can use Build.VERSION.SDK_INT
	static {
		int sdkInt;
		try {
			sdkInt = Integer.parseInt(Build.VERSION.SDK);
		} catch (NumberFormatException nfe) {
			// Just to be safe
			sdkInt = 10000;
		}
		SDK_INT = sdkInt;
	}

	private final Context context;
	private final CameraConfigurationManager configManager;
	private Camera camera;
	private Rect framingRect;
	private Rect framingRectInPreview;
	private boolean initialized;
	private boolean previewing;
	private final boolean useOneShotPreviewCallback;
	/**
	 * Preview frames are delivered here, which we pass on to the registered
	 * handler. Make sure to clear the handler so it will only receive one
	 * message.
	 */
	private final PreviewCallback previewCallback;
	/**
	 * Autofocus callbacks arrive here, and are dispatched to the Handler which
	 * requested them.
	 */
	private final AutoFocusCallback autoFocusCallback;

	/**
	 * Initializes this static object with the Context of the calling Activity.
	 *
	 * @param context
	 *            The Activity which wants to use the camera.
	 */
	public static void init(Context context) {
		if (cameraManager == null) {
			cameraManager = new CameraManager(context);
		}
	}

	/**
	 * Gets the CameraManager singleton instance.
	 *
	 * @return A reference to the CameraManager singleton.
	 */
	public static CameraManager get() {
		return cameraManager;
	}

	private CameraManager(Context context) {

		this.context = context;
		this.configManager = new CameraConfigurationManager(context);

		// Camera.setOneShotPreviewCallback() has a race condition in Cupcake,
		// so we use the older
		// Camera.setPreviewCallback() on 1.5 and earlier. For Donut and later,
		// we need to use
		// the more efficient one shot callback, as the older one can swamp the
		// system and cause it
		// to run out of memory. We can't use SDK_INT because it was introduced
		// in the Donut SDK.
		// useOneShotPreviewCallback = Integer.parseInt(Build.VERSION.SDK) >
		// Build.VERSION_CODES.CUPCAKE;
		useOneShotPreviewCallback = Integer.parseInt(Build.VERSION.SDK) > 3; // 3
		// =
		// Cupcake

		previewCallback = new PreviewCallback(configManager,
				useOneShotPreviewCallback);
		autoFocusCallback = new AutoFocusCallback();
	}

	/**
	 * Opens the camera driver and initializes the hardware parameters.
	 *
	 * @param holder
	 *            The surface object which the camera will draw preview frames
	 *            into.
	 * @throws IOException
	 *             Indicates the camera driver failed to open.
	 */
	public void openDriver(SurfaceHolder holder) throws IOException {
		if (camera == null) {
			camera = Camera.open();
			if (camera == null) {
				throw new IOException();
			}
			camera.setPreviewDisplay(holder);

			if (!initialized) {
				initialized = true;
				configManager.initFromCameraParameters(camera);
			}
			configManager.setDesiredCameraParameters(camera);

			// 自己添加的闪光灯

			/*mParameters = camera.getParameters();
			String flashMode = Camera.Parameters.FLASH_MODE_TORCH;
			if (flashMode != null) {
				mParameters.setFlashMode(flashMode);
				camera.setParameters(mParameters);

			}*/

			// FIXME
			// SharedPreferences prefs =
			// PreferenceManager.getDefaultSharedPreferences(context);
			// 是否使用前灯
			// if (prefs.getBoolean(PreferencesActivity.KEY_FRONT_LIGHT, false))
			// {
			// FlashlightManager.enableFlashlight();
			// }

			FlashlightManager.enableFlashlight();
		}
	}

	/**
	 * Closes the camera driver if still in use.
	 */
	public void closeDriver() {
		if (camera != null) {

			/*mParameters.setFlashMode(Parameters.FLASH_MODE_OFF);
			camera.setParameters(mParameters);*/

			FlashlightManager.disableFlashlight();
			camera.release();
			camera = null;
		}
	}

	/**
	 * Asks the camera hardware to begin drawing preview frames to the screen.
	 */
	public void startPreview() {
		if (camera != null && !previewing) {
			camera.startPreview();
			previewing = true;
		}
	}

	/**
	 * Tells the camera to stop drawing preview frames.
	 */
	public void stopPreview() {
		if (camera != null && previewing) {
			if (!useOneShotPreviewCallback) {
				camera.setPreviewCallback(null);
			}
			camera.stopPreview();
			previewCallback.setHandler(null, 0);
			autoFocusCallback.setHandler(null, 0);
			previewing = false;
		}
	}

	/**
	 * A single preview frame will be returned to the handler supplied. The data
	 * will arrive as byte[] in the message.obj field, with width and height
	 * encoded as message.arg1 and message.arg2, respectively.
	 *
	 * @param handler
	 *            The handler to send the message to.
	 * @param message
	 *            The what field of the message to be sent.
	 */
	public void requestPreviewFrame(Handler handler, int message) {
		if (camera != null && previewing) {
			previewCallback.setHandler(handler, message);
			if (useOneShotPreviewCallback) {
				camera.setOneShotPreviewCallback(previewCallback);
			} else {
				camera.setPreviewCallback(previewCallback);
			}
		}
	}

	/**
	 * Asks the camera hardware to perform an autofocus.
	 *
	 * @param handler
	 *            The Handler to notify when the autofocus completes.
	 * @param message
	 *            The message to deliver.
	 */
	public void requestAutoFocus(Handler handler, int message) {
		if (camera != null && previewing) {
			autoFocusCallback.setHandler(handler, message);
			// Log.d(TAG, "Requesting auto-focus callback");
			camera.autoFocus(autoFocusCallback);
		}
	}

	/**
	 * Calculates the framing rect which the UI should draw to show the user
	 * where to place the barcode. This target helps with alignment as well as
	 * forces the user to hold the device far enough away to ensure the image
	 * will be in focus.
	 *
	 * @return The rectangle to draw on screen in window coordinates.
	 */
	public Rect getSize(int width, int height, int rate_w, int rate_h)
	{
		int realW = 0;
		int realH = 0;
		Rect rect = new Rect();
		if (width != 0 && height != 0)
		{
			realW = ((width*rate_w)>>10);
			realH = ((height*rate_h)>>10);

			if(realW%2 != 0) {
				realW++;
			}
			if(realH%2 != 0) {
				realH++;
			}

			if (realH*203 <= realW<<7) {
				int thresExt = 16;
				int DistZoomY = height / thresExt;
				int RectHeight = height - (DistZoomY<<1);
				int RectWidth = ((RectHeight*203*rate_h)>>7)/rate_w;//(RectHeight*203)>>7;
				int DistZoomX = (width - RectWidth)>>1;

				rect.left = DistZoomX;//
				rect.top = DistZoomY;//
				rect.right = rect.left + RectWidth - 1;
				rect.bottom = rect.top + RectHeight - 1;//
			}
			else {
				int thresExt = 16;
				int DistZoomX = width / thresExt;
				int RectWidth = width - (DistZoomX<<1);
				int RectHeight = ((RectWidth*81)>>7)*rate_w/rate_h;//(RectWidth*81)>>7;//
				int DistZoomY = (height - RectHeight)>>1;

				rect.left = DistZoomX;//
				rect.top = DistZoomY;//
				rect.right = rect.left + RectWidth - 1;
				rect.bottom = rect.top + RectHeight - 1;//
			}
		}
		return rect;
	}
	public Rect getFramingRect() {
		Point screenResolution = configManager.getCameraResolution();
		if (screenResolution == null)//避免摄像头被禁用时出现程序崩溃的BUG
		{
			return null;
		}
		WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		Point point = new Point(display.getWidth(), display.getHeight());
		int width = 0,height = 0;
		width = point.x;
		height = point.y;
		int rate_x = 0;
		int rate_y = 0;

		rate_x = (screenResolution.x<<10)/width;
		rate_y = (screenResolution.y<<10)/height;

		Rect rect = getSize(width, height, rate_x, rate_y);
		framingRect = new Rect();
		framingRect.left = rect.left;
		framingRect.right = rect.right;
		framingRect.top = rect.top;
		framingRect.bottom = rect.bottom;
		return framingRect;
	}

	/**
	 * Like {@link #getFramingRect} but coordinates are in terms of the preview
	 * frame, not UI / screen.
	 */
	public  Point getScreenPoint () {
		Point screenResolution = configManager.getScreenResolution();
		return screenResolution;

	}
	public Rect getFramingRectInPreview() {
//		if (framingRectInPreview == null) {
//			Rect rect = new Rect(getFramingRect());
//			Point cameraResolution = configManager.getCameraResolution();
//			Point screenResolution = configManager.getScreenResolution();
//			// modify here
//			// rect.left = rect.left * cameraResolution.x / screenResolution.x;
//			// rect.right = rect.right * cameraResolution.x /
//			// screenResolution.x;
//			// rect.top = rect.top * cameraResolution.y / screenResolution.y;
//			// rect.bottom = rect.bottom * cameraResolution.y /
//			// screenResolution.y;
//			rect.left = rect.left * cameraResolution.y / screenResolution.x;
//			rect.right = rect.right * cameraResolution.y / screenResolution.x;
//			rect.top = rect.top * cameraResolution.x / screenResolution.y;
//			rect.bottom = rect.bottom * cameraResolution.x / screenResolution.y;
//			framingRectInPreview = rect;
//		}
		Point screenResolution = configManager.getCameraResolution();
		int h = screenResolution.y;
		int w = screenResolution.x;
		int x1 = 0, x2 = 0, y1 = 0, y2 = 0;
		if ((h*203)>>7 < w/*宽高比小于1.58*/) {
			int thresExt = 16;
			int DistZoomY = h / thresExt; //预览框的内缩值
			int RectHeight = h - (DistZoomY<<1);
			int RectWidth = (RectHeight*203)>>7;//(RectHeight*203*rateExpress)>>14;
			int DistZoomX = (w - RectWidth)>>1;
			//得到预览方框的宽与高
			x1 = DistZoomX;//
			y1 = DistZoomY;//
			x2 = x1 + RectWidth - 1;
			y2 = y1 + RectHeight - 1;//
		}
		else {
			int thresExt = 16;
			int DistZoomX = w / thresExt; //预览框的内缩值
			int RectWidth = w - (DistZoomX<<1);
			int RectHeight = (RectWidth*81)>>7;
			int DistZoomY = (h - RectHeight)>>1;

			//得到预览方框的宽与高
			x1 = DistZoomX;//
			y1 = DistZoomY;//
			x2 = x1 + RectWidth - 1;
			y2 = y1 + RectHeight - 1;//
		}
		Rect rect = new Rect();
		rect.left = x1;
		rect.right = x2;
		rect.top = y1;
		rect.bottom = y2;
		framingRectInPreview = rect;
		return framingRectInPreview;
	}

	/**
	 * Converts the result points from still resolution coordinates to screen
	 * coordinates.
	 *
	 * @param points
	 *            The points returned by the Reader subclass through
	 *            Result.getResultPoints().
	 * @return An array of Points scaled to the size of the framing rect and
	 *         offset appropriately so they can be drawn in screen coordinates.
	 */
	/*
	 * public Point[] convertResultPoints(ResultPoint[] points) { Rect frame =
	 * getFramingRectInPreview(); int count = points.length; Point[] output =
	 * new Point[count]; for (int x = 0; x < count; x++) { output[x] = new
	 * Point(); output[x].x = frame.left + (int) (points[x].getX() + 0.5f);
	 * output[x].y = frame.top + (int) (points[x].getY() + 0.5f); } return
	 * output; }
	 */

	/**
	 * A factory method to build the appropriate LuminanceSource object based on
	 * the format of the preview buffers, as described by Camera.Parameters.
	 *
	 * @param data
	 *            A preview frame.
	 * @param width
	 *            The width of the image.
	 * @param height
	 *            The height of the image.
	 * @return A PlanarYUVLuminanceSource instance.
	 */
	public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data,
														 int width, int height) {
		Rect rect = getFramingRectInPreview();
		int previewFormat = configManager.getPreviewFormat();
		String previewFormatString = configManager.getPreviewFormatString();
		switch (previewFormat) {
			// This is the standard Android format which all devices are REQUIRED to
			// support.
			// In theory, it's the only one we should ever care about.
			case PixelFormat.YCbCr_420_SP:
				// This format has never been seen in the wild, but is compatible as
				// we only care
				// about the Y channel, so allow it.
			case PixelFormat.YCbCr_422_SP:
				return new PlanarYUVLuminanceSource(data, width, height, rect.left,
						rect.top, rect.width(), rect.height());
			default:
				// The Samsung Moment incorrectly uses this variant instead of the
				// 'sp' version.
				// Fortunately, it too has all the Y data up front, so we can read
				// it.
				if ("yuv420p".equals(previewFormatString)) {
					return new PlanarYUVLuminanceSource(data, width, height,
							rect.left, rect.top, rect.width(), rect.height());
				}
		}
		throw new IllegalArgumentException("Unsupported picture format: "
				+ previewFormat + '/' + previewFormatString);
	}

	public Context getContext() {
		return context;
	}
	//返回本类中的Camera
	public Camera getCamera(){
		if(this.camera!=null){
			return this.camera;
		}
		return null;

	}

}
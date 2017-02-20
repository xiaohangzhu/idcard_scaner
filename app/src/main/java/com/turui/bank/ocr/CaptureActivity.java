package com.turui.bank.ocr;

import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.idcard.CardInfo;
import com.idcard.TParam;
import com.idcard.TRECAPIImpl;
import com.idcard.TStatus;
import com.idcard.TengineID;

import java.io.IOException;

/**
 * Initial the camera
 * @author Ryan.Tang
 */
public class CaptureActivity extends Activity implements Callback {


	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.10f;
	public TRECAPIImpl engineDemo = null;
	private boolean vibrate;
	private boolean isShow=false;

	private ProgressBar pg;
	private ImageView iv_pg_bg_grey;
	private SurfaceView surfaceView;
	private static boolean isLight = false;
	private static boolean isShowBack = false;
	public static TengineID  tengineID = TengineID.TIDBANK;
	private static String  ShowBANKText = "请将银行卡置于此区域尝试对齐边缘";
	private static String  ShowIDCText = "请将身份证置于此区域尝试对齐边缘";
	private TextView logtxt;
	public static Bitmap TakeBitmap = null;
	public static Bitmap SmallBitmap = null;
	public static boolean isOpenLog = false;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		engineDemo = (TRECAPIImpl) getIntent().getSerializableExtra("engine");
//		TStatus tStatus = engineDemo.TR_StartUP();
//		if (tStatus == TStatus.TR_TIME_OUT ) {
//			Toast.makeText(getBaseContext(), "引擎过期", Toast.LENGTH_SHORT).show();
//			setResult(0xff);
////		    finish();
//			Capfinish();
//		}
//		else  if (tStatus == TStatus.TR_FAIL) {
//			Toast.makeText(getBaseContext(), "引擎初始化失败", Toast.LENGTH_SHORT).show();
//			setResult(0xff);
////		    finish();
//			Capfinish();
//		}
		if (tengineID == TengineID.TIDCARD2 ) {
			if (engineDemo.TR_SetSupportEngine(TengineID.TIDCARD2 ) == TStatus.TR_FAIL) {
				Toast.makeText(getBaseContext(), "引擎不支持", Toast.LENGTH_SHORT).show();
//			    finish();
				Intent zhuceIntent = new Intent();
				Bundle myBundle = new Bundle();
				myBundle.putString("result", "");
				myBundle.putParcelable(DecodeThread.BARCODE_BITMAP, null);
				//zhuceIntent.putExtra("username", zhuceEdit.getText().toString());
				CardInfo cardInfo = new CardInfo();
				zhuceIntent.putExtra("cardinfo", cardInfo);
				zhuceIntent.putExtras(myBundle);
				setResult(1,zhuceIntent);
				Capfinish();
			}
			engineDemo.TR_SetParam(TParam.T_SET_RECMODE,1);
		}
		else if (tengineID == TengineID.TIDSSCCARD ) {
			if (engineDemo.TR_SetSupportEngine(TengineID.TIDSSCCARD ) == TStatus.TR_FAIL) {
				Toast.makeText(getBaseContext(), "引擎不支持", Toast.LENGTH_SHORT).show();
//			    finish();
				Intent zhuceIntent = new Intent();
				Bundle myBundle = new Bundle();
				myBundle.putString("result", "");
				myBundle.putParcelable(DecodeThread.BARCODE_BITMAP, null);
				//zhuceIntent.putExtra("username", zhuceEdit.getText().toString());
				CardInfo cardInfo = new CardInfo();
				zhuceIntent.putExtra("cardinfo", cardInfo);
				zhuceIntent.putExtras(myBundle);
				setResult(1,zhuceIntent);
				Capfinish();
			}
			engineDemo.TR_SetParam(TParam.T_SET_RECMODE,1);
		}
		else
		{
			if (engineDemo.TR_SetSupportEngine(TengineID.TIDBANK ) == TStatus.TR_FAIL) {
				Toast.makeText(getBaseContext(), "引擎不支持", Toast.LENGTH_SHORT).show();
//			    finish();
				Intent zhuceIntent = new Intent();
				Bundle myBundle = new Bundle();
				myBundle.putString("result", "");
				myBundle.putParcelable(DecodeThread.BARCODE_BITMAP, null);
				//zhuceIntent.putExtra("username", zhuceEdit.getText().toString());
				CardInfo cardInfo = new CardInfo();
				zhuceIntent.putExtra("cardinfo", cardInfo);
				zhuceIntent.putExtras(myBundle);
				setResult(1,zhuceIntent);
				Capfinish();
			}

			TStatus tStatus = engineDemo.TR_SetParam(TParam.T_SET_RECMODE,0);// 0为扫描模式， 1为其他模式
		}

		initLayout();
//		setContentView(R.layout.camera_diy);
		//ViewUtil.addTopView(getApplicationContext(), this, R.string.scan_card);
		CameraManager.init(getApplication());
		getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
	}
	@SuppressLint("NewApi")
	public void initLayout() {
		FrameLayout main = (FrameLayout) new FrameLayout(this);
		main.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		surfaceView = (SurfaceView) new SurfaceView(this);
		LinearLayout.LayoutParams layoutParamsRoot = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParamsRoot.gravity = Gravity.CENTER;
		surfaceView.setLayoutParams(layoutParamsRoot);
		main.addView(surfaceView);

		viewfinderView = (ViewfinderView) new ViewfinderView(this);
		LinearLayout.LayoutParams viewfindlayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		main.addView(viewfinderView);

		RelativeLayout relativeLayout = (RelativeLayout) new RelativeLayout(this);
		RelativeLayout.LayoutParams relayoutParams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.FILL_PARENT);
		relayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		relativeLayout.setLayoutParams(relayoutParams);

		WindowManager manager = (WindowManager) getBaseContext().getSystemService(
				Context.WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		int textWidth = display.getWidth() / 2;
		logtxt = new TextView(getBaseContext());
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(textWidth,RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);

		if (tengineID == TengineID.TIDCARD2) {
			logtxt.setText(ShowIDCText);
		}
		else
		{
			logtxt.setText(ShowBANKText);
		}
		logtxt.setGravity(Gravity.CENTER);
		logtxt.setTextSize(16);
		logtxt.setTextColor(Color.WHITE);
		logtxt.setLayoutParams(params);
		relativeLayout.addView(logtxt);

		if (isShowBack == true) {
			Button button = new Button(getBaseContext());
			RelativeLayout.LayoutParams bParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
			bParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			button.setText("返回");
			button.setTag("back");
			button.setBackgroundColor(Color.parseColor("#00000000"));
			button.setOnClickListener(initClickListener());
			button.setLayoutParams(bParams);
			relativeLayout.addView(button);
		}

		main.addView(relativeLayout);
		setContentView(main);

	}
	@Override
	protected void onResume() {
		super.onResume();
		//SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		characterSet = null;

		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
//		initBeepSound();
		vibrate = true;

	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			//此处写处理的事件
			Intent zhuceIntent = new Intent();
			Bundle myBundle = new Bundle();
			myBundle.putString("result", "");
			myBundle.putParcelable(DecodeThread.BARCODE_BITMAP, null);
			//zhuceIntent.putExtra("username", zhuceEdit.getText().toString());
			CardInfo cardInfo = new CardInfo();
			zhuceIntent.putExtra("cardinfo", cardInfo);
			zhuceIntent.putExtras(myBundle);
			setResult(1,zhuceIntent);
			Capfinish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}
	protected void Capfinish() {
		finish();
	}
	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}
	private View.OnClickListener initClickListener() {
		return new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v.getTag().equals("back")) {
//				    finish();
					Intent zhuceIntent = new Intent();
					Bundle myBundle = new Bundle();
					myBundle.putString("result", "");
					myBundle.putParcelable(DecodeThread.BARCODE_BITMAP, null);
					//zhuceIntent.putExtra("username", zhuceEdit.getText().toString());
					CardInfo cardInfo = new CardInfo();
					zhuceIntent.putExtra("cardinfo", cardInfo);
					zhuceIntent.putExtras(myBundle);
					setResult(1,zhuceIntent);
					Capfinish();
				}
			}
		};
	}
	/**
	 * Handler scan result
	 * @param result
	 * @param barcode
	 */
	public void handleDecode(String result, Bitmap barcode,CardInfo cardInfo) {
		inactivityTimer.onActivity();
		playBeepSoundAndVibrate();
		String resultString = result;
		//FIXME
		if (resultString.equals("")) {
			Toast.makeText(CaptureActivity.this, "Scan failed!", Toast.LENGTH_SHORT).show();
		}else {
//			System.out.println("Result:"+resultString);
			if(pg!=null&&pg.isShown()){
				pg.setVisibility(View.GONE);
				iv_pg_bg_grey.setVisibility(View.VISIBLE);
			}

			Intent zhuceIntent = new Intent();
			Bundle myBundle = new Bundle();
			myBundle.putString("result", resultString);
			myBundle.putParcelable(DecodeThread.BARCODE_BITMAP, barcode);
			zhuceIntent.putExtra("cardinfo", cardInfo);
			zhuceIntent.putExtras(myBundle);
			setResult(1,zhuceIntent);
//            finish();
			Capfinish();
			//this.setResult(RESULT_OK, resultIntent);
			//startActivityForResult(resultIntent,3);
		}
//		CaptureActivity.this.finish();
	}
	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			//对摄像头调用异常做出处理并提示用户开放摄像头权限
//			TextView cameraErrorTextView = (TextView)findViewById(R.id.textViewResult);
			String scanResult = "您已禁止应用程序调用摄像头！请在安全助手中授权！";
			logtxt.setText(scanResult);

			return;
		}
		if (handler == null) {
			LightControl mLightControl = new LightControl();
			if (isLight ==  true) {
				mLightControl.turnOn();
			}
			else {
				mLightControl.turnOff();
			}
			handler = new CaptureActivityHandler(this,
					characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
							   int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();

	}

//	private void initBeepSound() {
//		if (playBeep && mediaPlayer == null) {
//			// The volume on STREAM_SYSTEM is not adjustable, and users found it
//			// too loud,
//			// so we now play on the music stream.
//			setVolumeControlStream(AudioManager.STREAM_MUSIC);
//			mediaPlayer = new MediaPlayer();
//			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//			mediaPlayer.setOnCompletionListener(beepListener);
//
//			AssetFileDescriptor file = getResources().openRawResourceFd(
//					R.raw.beep);
//			try {
//				mediaPlayer.setDataSource(file.getFileDescriptor(),
//						file.getStartOffset(), file.getLength());
//				file.close();
//				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
//				mediaPlayer.prepare();
//			} catch (IOException e) {
//				mediaPlayer = null;
//			}
//		}
//	}

	private static final long VIBRATE_DURATION = 200L;

	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};
	public static void SetDisplayLight(boolean init) {
		isLight = init;
	}
	public static void SetShowBackBotton(boolean init) {
		isShowBack =init;
	}
	public static void isOpenLog(boolean init) {
		isOpenLog =init;
	}
}
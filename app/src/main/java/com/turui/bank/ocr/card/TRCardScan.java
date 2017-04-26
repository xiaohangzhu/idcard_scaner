package com.turui.bank.ocr.card;


import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.idcard.CBInterface;
import com.idcard.CardInfo;
import com.idcard.GlobalData;
import com.idcard.TFieldID;
import com.idcard.TParam;
import com.idcard.TRECAPIImpl;
import com.idcard.TStatus;
import com.idcard.TengineID;
import com.newhope.idcardscan.R;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/////////http://download.csdn.net/download/ssxxqqaaa/5262932
public class TRCardScan extends Activity implements SurfaceHolder.Callback,CBInterface{

	public static final String tag="trscan";
	public static boolean isPreview = false;
	//private SurfaceView mPreviewSV = null;//预览SurfaceView
    public static final  int  ONFONT=2;
	public static  final int  ONBACK=3;

	private SurfaceView surfaceView;
	private SurfaceHolder mySurfaceHolder = null;
	private ProgressBar mProgressBar = null;
	private TextView mTextView = null;
	private Camera myCamera = null;
	private View focusIndex = null;
	private Handler handler = new Handler();
	//	public static Bitmap rectBitmap = null;
//	public Bitmap HeadImgBitmap = null;
	public static Bitmap TakeBitmap = null;
	public static Bitmap HeadImgBitmap = null;
	private AutoFocusCallback myAutoFocusCallback = null;//自动对焦并拍照回调
	private AutoFocusCallback myAutoFocusCallback1 = null;//自动对焦回调
	public static final int only_auto_focus = 110;
	private static final int auto_focus = 111;
	private static final int take_pic_ok = 222;
	private static final int get_data_ok = 333;
	private static final int flashtxt = 444;// 刷新进度条
	private static final int closeview = 555;//关闭界面
	private TextView logtxt; // 提示语文本控件
	private WindowManager manager;
	private Display display;
	private RelativeLayout.LayoutParams Params;
	private int process = 0;
	List<Size> mSupportedPreviewSizes;
	int issuccessfocus=0;      // 对焦次数，防止一次对焦，一次不对焦情况
	int nfocusNum=0;      // 对焦次数，防止一次对焦，一次不对焦情况
	private byte[] data = null;// 获取拍照流数据
	public  int isGetdataok = -1;//数据处理 流程  刚开始是-1  拍照后设置为1  识别成功后为0
	private final Handler mHandler = new MyHandler(this);// 拍照进程处理
	private final Thread MyThread = new MyThread(this);  // 识别线程
	private final Thread MyOpenThread = new MyOpenThread(this); // 摄像头打开线程
	private boolean isGetWidth = false ; //	界面加载是获取高宽的变量控制
	private boolean cameraErr = false;
	private boolean isFlash = false;
	private int drawHeight = 0;
	private int drawWidth =0;
	private int drawPerConff = 4;
	private TRECAPIImpl engineDemo = null;
	private static TengineID tengineID = TengineID.TUNCERTAIN;
	public static boolean isOpenLog = false;
	public static boolean isOpenProgress = false;
	CardInfo cardInfo = new CardInfo();
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isOpenLog == true) {
			SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			String date = sDateFormat.format(new java.util.Date());
			WriteFile("\n" + date + "\n" +"onCreate 0\n");
		}
		engineDemo = (TRECAPIImpl) getIntent().getSerializableExtra("engine");
		TStatus ret = engineDemo.TR_SetSupportEngine(tengineID);

		if (ret != TStatus.TR_OK) {
			Toast.makeText(getBaseContext(), "引擎不支持", Toast.LENGTH_SHORT).show();
			CloseView();
		}
		engineDemo.TR_SetParam(TParam.T_SET_HEADIMG, 1);// 打开人头像功能
		engineDemo.TR_SetParam(TParam.T_SET_RECMODE,0);//拍照模式，默认是拍照模式
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (isOpenLog == true) {
			String saveLogPath = GlobalData.saveLogPath;
			File folderLog = new File(saveLogPath);
			if(!folderLog.exists())
			{
				folderLog.mkdirs();
			}
			String saveImgPath = GlobalData.saveImgPath;
			File folderImg = new File(saveImgPath);
			if(!folderImg.exists())
			{
				folderImg.mkdirs();
			}
			engineDemo.TR_SetLOGPath(saveLogPath);
		}
		int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
		Window myWindow = this.getWindow();
		myWindow.setFlags(flag, flag);
		/*初始化变量并清空图像内存*/
		isGetdataok = -1;
		isPreview = false;
		issuccessfocus = 0;
		nfocusNum = 0;
		MyOpenThread.start();
		try {
			MyOpenThread.join();
		} catch (Exception e) {
			cameraErr = true;
		}
		if(cameraErr){
			Toast.makeText(getBaseContext(), "请允许拍照权限,或者相机打开异常", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		if (TakeBitmap != null) {
			TakeBitmap.recycle();
			TakeBitmap= null;
		}
		if (HeadImgBitmap != null) {
			HeadImgBitmap.recycle();
			HeadImgBitmap = null;
		}

		/*设置提示语 start*/
		manager = (WindowManager) getBaseContext().getSystemService(
				Context.WINDOW_SERVICE);
		display = manager.getDefaultDisplay();
		//setContentView(R.layout.activity_rect_photo);
		initLayout();
//		logtxt = new TextView(getBaseContext());
//		int textWidth = display.getWidth() / 2;
//		Params = new RelativeLayout.LayoutParams(textWidth,RelativeLayout.LayoutParams.WRAP_CONTENT);
//		Params.addRule(RelativeLayout.CENTER_IN_PARENT);
//		logtxt.setText("请将身份证置于预览区中,尽量使身份证区域足够大!");
//		logtxt.setGravity(Gravity.CENTER);
//		logtxt.setTextSize(16);
//		logtxt.setTextColor(Color.WHITE);
//		logtxt.setLayoutParams(Params);
//		RelativeLayout layout = (RelativeLayout) findViewById(R.id.TxtLayout);
//		layout.addView(logtxt);
		/*设置提示语 end*/

		//初始化SurfaceView
		//mPreviewSV = (SurfaceView)findViewById(R.id.previewSV);
		surfaceView.setZOrderOnTop(false);
		mySurfaceHolder = surfaceView.getHolder();
		mySurfaceHolder.setFormat(PixelFormat.TRANSPARENT);//

		mySurfaceHolder.addCallback(this);
		mySurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		//自动聚焦并拍照回调
		myAutoFocusCallback = new AutoFocusCallback() {
			public void onAutoFocus(boolean success, Camera camera) {
				if(success)//success表示对焦成功
				{
					Log.i(tag, "myAutoFocusCallback: success...");
					mHandler.sendEmptyMessageDelayed(take_pic_ok,600);
				}
				else
				{
					Toast.makeText(getApplicationContext(), "对焦失败", Toast.LENGTH_SHORT).show();
					mHandler.sendEmptyMessageDelayed(take_pic_ok,300);
					Log.i(tag, "myAutoFocusCallback: 失败");
				}
			}
		};
		myAutoFocusCallback1 = new AutoFocusCallback() {

			public void onAutoFocus(boolean success, Camera camera) {
				if(success)//success表示对焦成功
				{
					issuccessfocus++;
					if(issuccessfocus <= 1)
						mHandler.sendEmptyMessage(only_auto_focus);
					Log.i(tag, "myAutoFocusCallback1: success..."+issuccessfocus);
				}
				else
				{
					nfocusNum++;
					if (nfocusNum <= 2) {
						mHandler.sendEmptyMessage(only_auto_focus);
					}
					Log.i(tag, "myAutoFocusCallback1: 失败...");
				}
			}
		};

		mHandler.sendEmptyMessageDelayed(only_auto_focus, 100);
		if (isOpenLog == true) {
			WriteFile("onCreate 1\n");
		}
		if(isOpenProgress == true){
			engineDemo.TR_SetSendMsgCB(this);
		}

	}
	public static void SetEngineType(TengineID tID) {
		tengineID =tID;
	}
	@SuppressLint("NewApi")
	public void initLayout() {
		if (isOpenLog == true) {
			WriteFile("initLayout 0\n");
		}
		FrameLayout main = (FrameLayout) new FrameLayout(this);
		main.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		surfaceView = (SurfaceView) new SurfaceView(this);
		LinearLayout.LayoutParams layoutParamsRoot = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParamsRoot.gravity = Gravity.CENTER;
		surfaceView.setLayoutParams(layoutParamsRoot);
		main.addView(surfaceView);

//        viewfinderView = (ViewfinderView) new ViewfinderView(this);
//		LinearLayout.LayoutParams viewfindlayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
//		main.addView(viewfinderView);

		RelativeLayout relativeLayout = (RelativeLayout) new RelativeLayout(this);
		RelativeLayout.LayoutParams relayoutParams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.FILL_PARENT);
		relayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		relativeLayout.setLayoutParams(relayoutParams);



		WindowManager manager = (WindowManager) getBaseContext().getSystemService(
				Context.WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		int textWidth = display.getWidth() ;
		logtxt = new TextView(getBaseContext());
		RelativeLayout.LayoutParams Params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT);
		Params.addRule(RelativeLayout.CENTER_IN_PARENT);
		if (tengineID == TengineID.TIDCARD2) {
			logtxt.setText("请将身份证置于预览区中,尽量使身份证区域足够大!");
		}
		else if (tengineID == TengineID.TIDLPR) {
			logtxt.setText("请将车牌置于预览区中,尽量使识别区域足够大!");
		}
		else if (tengineID == TengineID.TIDJSZCARD) {
			logtxt.setText("请将驾驶证置于预览区中,尽量使识别区域足够大!");
		}
		else if (tengineID == TengineID.TIDXSZCARD) {
			logtxt.setText("请将行驶证置于预览区中,尽量使识别区域足够大!");
		}
		else if (tengineID == TengineID.TIDTICKET) {
			logtxt.setText("请将火车票置于预览区中,尽量使识别区域足够大!");
		}
		else if (tengineID == TengineID.TIDSSCCARD) {
			logtxt.setText("请将社保卡置于预览区中,尽量使识别区域足够大!");
		}
		else if (tengineID == TengineID.TIDPASSPORT) {
			logtxt.setText("请将护照置于预览区中,尽量使识别区域足够大!");
		}
		else {
			logtxt.setText("请将证件图片置于预览区中,尽量使身份证区域足够大!");
		}
		logtxt.setGravity(Gravity.CENTER);
		logtxt.setTextSize(16);
		logtxt.setTextColor(Color.WHITE);
		logtxt.setLayoutParams(Params);
		relativeLayout.addView(logtxt);

		focusIndex = new View(this);
		RelativeLayout.LayoutParams focusLayoutParams = new RelativeLayout.LayoutParams( 48,48);//displayUtil.dip2px(48), displayUtil.dip2px(48));
		focusIndex.setLayoutParams(focusLayoutParams);
		int strokeWidth1 = 2; // 3dp 边框宽度
		int strokeColor1 = Color.parseColor("#7FFF00");//边框颜色
		int fillColor1 = Color.parseColor("#00000000");//内部填充颜色
		GradientDrawable focus_gd = new GradientDrawable();//创建drawable
		focus_gd.setColor(fillColor1);
		focus_gd.setStroke(strokeWidth1, strokeColor1);
		focusIndex.setBackgroundDrawable(focus_gd);
		relativeLayout.addView(focusIndex);
		focusIndex.setVisibility(View.GONE);
		ImageView imageView=new ImageView(this);
		imageView.setPadding(0,0,50,0);
		int intExtra = getIntent().getIntExtra(tag, 0);
			if (ONFONT==intExtra){
				logtxt.setVisibility(View.GONE);
				imageView.setImageResource(R.drawable.ic_font_scan);
			}else if (ONBACK==intExtra){
				logtxt.setVisibility(View.GONE);
				imageView.setImageResource(R.drawable.ic_back_scan);
			}
		RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);//displayUtil.dip2px(48), displayUtil.dip2px(48));
		imageParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		imageParams.rightMargin=30;

		relativeLayout.addView(imageView,imageParams);

		main.addView(relativeLayout);
		int strokeWidth = 3; // 3dp 边框宽度
		int roundRadius = 20; // 8dp 圆角半径
		int strokeColor = Color.parseColor("#fb6d59");//边框颜色
		int fillColor = Color.parseColor("#f18a47");//内部填充颜色
		GradientDrawable takegd = new GradientDrawable();//创建drawable
		takegd.setColor(fillColor);
		takegd.setCornerRadius(roundRadius);
		takegd.setStroke(strokeWidth, strokeColor);
		Button tack_button  = new Button(this);
		tack_button.setText("   拍照   ");
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		//此处相当于布局文件中的Android:layout_gravity属性
		lp.gravity = Gravity.RIGHT;
		lp.setMargins(0, 24, 20, 24);
		tack_button.setLayoutParams(lp);
		//此处相当于布局文件中的Android：gravity属性
		tack_button.setGravity(Gravity.CENTER);
		tack_button.setTag("takepic");
		tack_button.setBackgroundDrawable(takegd);
		tack_button.setOnClickListener(initClickListener());

		Button back_button  = new Button(this);
		back_button.setText("   返回   ");
		back_button.setLayoutParams(lp);
		//此处相当于布局文件中的Android：gravity属性
		back_button.setGravity(Gravity.CENTER);
		back_button.setTag("back");
		back_button.setBackgroundDrawable(takegd);
		back_button.setOnClickListener(initClickListener());

		LinearLayout linear = new LinearLayout(this);
		//注意，对于LinearLayout布局来说，设置横向还是纵向是必须的！否则就看不到效果了。
		linear.setOrientation(LinearLayout.VERTICAL);
		linear.setGravity(Gravity.CENTER);
		linear.addView(tack_button);
		linear.addView(back_button);
		main.addView(linear);

		if (isOpenProgress == true) {

			LinearLayout linearLayout1= new LinearLayout(this);
			linearLayout1.setGravity(Gravity.BOTTOM);
			linearLayout1.setOrientation(LinearLayout.VERTICAL);
			mProgressBar=new ProgressBar(this);
//		mProgressBar.setBackgroundColor(android.graphics.Color.parseColor("#FFFF00"));
			BeanUtils.setFieldValue(mProgressBar, "mOnlyIndeterminate", new Boolean(false));
			mProgressBar.setIndeterminate(false);
			mProgressBar.setProgressDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));
			mProgressBar.setIndeterminateDrawable(getResources().getDrawable(android.R.drawable.progress_indeterminate_horizontal));
			mProgressBar.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, 36, Gravity.CENTER_VERTICAL) );
			mProgressBar.setMax(100);
			mProgressBar.setProgress(0);
			linearLayout1.addView(mProgressBar);
			mProgressBar.setVisibility(View.GONE);
			main.addView(linearLayout1);

			LinearLayout linearLayout2= new LinearLayout(this);
			linearLayout2.setGravity(Gravity.BOTTOM);
			linearLayout2.setOrientation(LinearLayout.VERTICAL);
			mTextView = new TextView(this);
			mTextView.setGravity(Gravity.CENTER);
			linearLayout2.addView(mTextView);
			mTextView.setVisibility(View.GONE);
			main.addView(linearLayout2);
		}




//		LinearLayout linearLayout = (LinearLayout) new LinearLayout(this);
//		LinearLayout.LayoutParams LineLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
//		linearLayout.setLayoutParams(LineLayoutParams);
//		linearLayout.setOrientation(LinearLayout.VERTICAL);
//		Button back_button = new Button(getBaseContext());
//		LinearLayout.LayoutParams bParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
//		bParams.gravity = Gravity.RIGHT;
//		back_button.setText("返回");
//		back_button.setTag("back");
//		back_button.setOnClickListener(initClickListener());
//		back_button.setLayoutParams(bParams);
//		back_button.setGravity(Gravity.CENTER);
//		linearLayout.addView(back_button);
//
//		Button tack_button = new Button(getBaseContext());
//		tack_button.setText("拍照");
//		tack_button.setTag("takepic");
//		tack_button.setOnClickListener(initClickListener());
//		tack_button.setLayoutParams(bParams);
//		tack_button.setGravity(Gravity.CENTER);
//		linearLayout.addView(tack_button);
//
//		main.addView(linearLayout);
		setContentView(main);
		if (isOpenLog == true) {
			WriteFile("initLayout 1\n");
		}
	}
	private View.OnClickListener initClickListener() {
		return new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v.getTag().equals("takepic")) {
					if(isPreview && myCamera!=null){
						isPreview = false;
						mHandler.sendEmptyMessage(auto_focus);

					}
				}
				else if (v.getTag().equals("back")) {
					CloseView();
				}
			}
		};
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if (myCamera != null ) {
			myCamera.autoFocus(myAutoFocusCallback1);
		}
		RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(
				focusIndex.getLayoutParams());
		layout.setMargins((int) event.getX() - 70, (int) event.getY() -70, 0,0);

		focusIndex.setLayoutParams(layout);
		focusIndex.setVisibility(View.VISIBLE);

		ScaleAnimation sa = new ScaleAnimation(3f, 1f, 3f, 1f,
				ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
				ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
		sa.setDuration(800);
		focusIndex.startAnimation(sa);
		handler.postAtTime(new Runnable() {
			@Override
			public void run() {
				focusIndex.setVisibility(View.INVISIBLE);
			}
		}, 800);
		return true;
	}
	private static class MyThread extends Thread {
		WeakReference<TRCardScan> mThreadActivityRef;
		public MyThread(TRCardScan activity) {
			mThreadActivityRef = new WeakReference<TRCardScan>(activity);
		}
		@Override
		public void run() {
			super.run();
			if (isOpenLog == true) {
				mThreadActivityRef.get().WriteFile("MyThread 0\n");
			}
			if (mThreadActivityRef == null)
				return;
			if (mThreadActivityRef.get() == null)
				return;
			if (mThreadActivityRef.get().isGetdataok == 1 && mThreadActivityRef.get().data != null) {
				if (mThreadActivityRef.get().data == null) {
					Toast.makeText(mThreadActivityRef.get().getApplicationContext(), "拍照数据获取失败，请手动对焦", Toast.LENGTH_SHORT).show();
					mThreadActivityRef.get().myCamera.startPreview();
					mThreadActivityRef.get().myCamera.autoFocus(mThreadActivityRef.get().myAutoFocusCallback1);
					return;
				}
				TStatus isRecSucess = TStatus.TR_FAIL;
				long beforeTime=0;
				long afterTime=0;
				long timeDistance;
				Bitmap mBitmap = null;
				String smallHeadPath ;
				beforeTime =System.currentTimeMillis();
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inPreferredConfig = Config.RGB_565;
				mBitmap=BitmapFactory.decodeByteArray(mThreadActivityRef.get().data, 0, mThreadActivityRef.get().data.length,options);
				TakeBitmap = mBitmap;
				if (isOpenLog == true) {
					mThreadActivityRef.get().WriteFile("MyThread TR_LoadMemBitMap\n");
				}
				mThreadActivityRef.get().engineDemo.TR_LoadMemBitMap(TakeBitmap);
				if (isOpenLog == true) {
					mThreadActivityRef.get().WriteFile("MyThread TR_RECOCR\n");
				}
				if (isOpenLog == true) {
					String jpgNameString = System.currentTimeMillis() + ".jpg";
					mThreadActivityRef.get().WriteFile("MyThread TR_SaveImage path = " + jpgNameString +"\n");
					mThreadActivityRef.get().engineDemo.TR_SaveImage(GlobalData.saveImgPath + jpgNameString);
				}
				isRecSucess = mThreadActivityRef.get().engineDemo.TR_RECOCR();
				if (isOpenLog == true) {
					mThreadActivityRef.get().WriteFile("MyThread TR_FreeImage\n");
				}
				mThreadActivityRef.get().engineDemo.TR_FreeImage();
				//isRecSucess = IDCardIdentifyActivity.engineDemo.RunOCR(rectBitmap, null);
				if (isRecSucess == TStatus.TR_OK) {
					if (tengineID == TengineID.TIDCARD2) {
						String name = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.NAME);
						String sex = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf( TFieldID.SEX);
						String folk = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf( TFieldID.FOLK);
						String BirthDay = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf( TFieldID.BIRTHDAY);
						String Address = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf( TFieldID.ADDRESS);
						String CardNum = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf( TFieldID.NUM);
						String issue = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf( TFieldID.ISSUE);
						String period = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.PERIOD);
						String allinfo = mThreadActivityRef.get().engineDemo.TR_GetOCRStringBuf();
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.NAME,name);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.SEX,sex);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.FOLK,folk);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.BIRTHDAY,BirthDay);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.ADDRESS,Address);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.NUM,CardNum);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.ISSUE,issue);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.PERIOD,period);
						mThreadActivityRef.get().cardInfo.setAllinfo(allinfo);
						byte []hdata = mThreadActivityRef.get().engineDemo.TR_GetHeadImgBuf();
						int size =  mThreadActivityRef.get().engineDemo.TR_GetHeadImgBufSize();
						if (size > 0 && hdata != null && hdata.length > 0) {
							mThreadActivityRef.get().HeadImgBitmap = BitmapFactory.decodeByteArray(hdata,0,size);
						}
					}
					else if (tengineID == TengineID.TIDLPR) {
						String CardNum = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf( TFieldID.NUM);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.LPRNUM,CardNum);
						String allinfo = mThreadActivityRef.get().engineDemo.TR_GetOCRStringBuf();
						mThreadActivityRef.get().cardInfo.setAllinfo(allinfo);


					}
					else if (tengineID == TengineID.TIDXSZCARD) {
						String DP_PLATENO = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.DP_PLATENO);
						String DP_TYPE = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.DP_TYPE);
						String DP_OWNER = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.DP_OWNER);
						String DP_ADDRESS = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.DP_ADDRESS);
						String DP_USECHARACTER = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.DP_USECHARACTER);
						String DP_MODEL = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.DP_MODEL);
						String DP_VIN = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.DP_VIN);
						String DP_ENGINENO = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.DP_ENGINENO);
						String DP_REGISTER_DATE = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.DP_REGISTER_DATE);
						String DP_ISSUE_DATE = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.DP_ISSUE_DATE);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.DP_PLATENO,DP_PLATENO);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.DP_TYPE,DP_TYPE);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.DP_OWNER,DP_OWNER);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.DP_ADDRESS,DP_ADDRESS);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.DP_USECHARACTER,DP_USECHARACTER);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.DP_MODEL,DP_MODEL);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.DP_VIN,DP_VIN);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.DP_ENGINENO,DP_ENGINENO);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.DP_REGISTER_DATE,DP_REGISTER_DATE);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.DP_ISSUE_DATE,DP_ISSUE_DATE);
						String allinfo = mThreadActivityRef.get().engineDemo.TR_GetOCRStringBuf();
						mThreadActivityRef.get().cardInfo.setAllinfo(allinfo);

					}
					else if (tengineID ==  TengineID.TIDJSZCARD) {

						String DL_NUM = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.DL_NUM);
						String DL_NAME = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.DL_NAME);
						String DL_SEX = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.DL_SEX);
						String DL_COUNTRY = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.DL_COUNTRY);
						String DL_ADDRESS = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.DL_ADDRESS);
						String DL_BIRTHDAY = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.DL_BIRTHDAY);
						String DL_ISSUE_DATE = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.DL_ISSUE_DATE);
						String DL_CLASS = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.DL_CLASS);
						String DL_VALIDFROM = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.DL_VALIDFROM);
						String DL_VALIDFOR = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.DL_VALIDFOR);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.DL_NUM,DL_NUM);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.DL_NAME,DL_NAME);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.DL_SEX,DL_SEX);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.DL_COUNTRY,DL_COUNTRY);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.DL_ADDRESS,DL_ADDRESS);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.DL_BIRTHDAY,DL_BIRTHDAY);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.DL_ISSUE_DATE,DL_ISSUE_DATE);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.DL_CLASS,DL_CLASS);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.DL_VALIDFROM,DL_VALIDFROM);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.DL_VALIDFOR,DL_VALIDFOR);
						String allinfo = mThreadActivityRef.get().engineDemo.TR_GetOCRStringBuf();
						mThreadActivityRef.get().cardInfo.setAllinfo(allinfo);

					}
					else if (tengineID ==  TengineID.TIDTICKET) {
						String TIC_START = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.TIC_START);
						String TIC_NUM = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.TIC_NUM);
						String TIC_END = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.TIC_END);
						String TIC_TIME = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.TIC_TIME);
						String TIC_SEAT = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.TIC_SEAT);
						String TIC_NAME = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.TIC_NAME);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.TIC_START,TIC_START);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.TIC_NUM,TIC_NUM);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.TIC_END,TIC_END);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.TIC_TIME,TIC_TIME);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.TIC_SEAT,TIC_SEAT);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.TIC_NAME,TIC_NAME);
						String allinfo = mThreadActivityRef.get().engineDemo.TR_GetOCRStringBuf();
						mThreadActivityRef.get().cardInfo.setAllinfo(allinfo);

					}
					else if (tengineID ==  TengineID.TIDSSCCARD) {
						String SSC_NAME = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.SSC_NAME);
						String SSC_NUM = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.SSC_NUM);
						String SSC_SHORTNUM = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.SSC_SHORTNUM);
						String SSC_PERIOD = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.SSC_PERIOD);
						String SSC_BANKNUM = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.SSC_BANKNUM);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.SSC_NAME,SSC_NAME);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.SSC_NUM,SSC_NUM);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.SSC_SHORTNUM,SSC_SHORTNUM);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.SSC_PERIOD,SSC_PERIOD);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.SSC_BANKNUM,SSC_BANKNUM);
						String allinfo = mThreadActivityRef.get().engineDemo.TR_GetOCRStringBuf();
						mThreadActivityRef.get().cardInfo.setAllinfo(allinfo);

					}
					else if (tengineID ==  TengineID.TIDPASSPORT) {
						String PAS_PASNO = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.PAS_PASNO);
						String PAS_NAME = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.PAS_NAME);
						String PAS_SEX = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.PAS_SEX);
						String PAS_IDCARDNUM = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.PAS_IDCARDNUM);
						String PAS_BIRTH = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.PAS_BIRTH);
						String PAS_PLACE_BIRTH = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.PAS_PLACE_BIRTH);
						String PAS_DATE_ISSUE = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.PAS_DATE_ISSUE);
						String PAS_DATE_EXPIRY = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.PAS_DATE_EXPIRY);
						String PAS_PLACE_ISSUE = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.PAS_PLACE_ISSUE);
						String PAS_NATION_NAME = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.PAS_NATION_NAME);
						String PAS_MACHINE_RCODE = mThreadActivityRef.get().engineDemo.TR_GetOCRFieldStringBuf(TFieldID.PAS_MACHINE_RCODE);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.PAS_PASNO,PAS_PASNO);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.PAS_NAME,PAS_NAME);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.PAS_SEX,PAS_SEX);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.PAS_IDCARDNUM,PAS_IDCARDNUM);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.PAS_BIRTH,PAS_BIRTH);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.PAS_PLACE_BIRTH,PAS_PLACE_BIRTH);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.PAS_DATE_ISSUE,PAS_DATE_ISSUE);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.PAS_DATE_EXPIRY,PAS_DATE_EXPIRY);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.PAS_PLACE_ISSUE,PAS_PLACE_ISSUE);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.PAS_NATION_NAME,PAS_NATION_NAME);
						mThreadActivityRef.get().cardInfo.setFieldString(TFieldID.PAS_MACHINE_RCODE,PAS_MACHINE_RCODE);
						String allinfo = mThreadActivityRef.get().engineDemo.TR_GetOCRStringBuf();
						mThreadActivityRef.get().cardInfo.setAllinfo(allinfo);

					}

				}
				else {
					mThreadActivityRef.get().cardInfo.setNull();
				}
				if (isOpenLog == true) {
					mThreadActivityRef.get().WriteFile("MyThread 1\n");
				}
				afterTime =System.currentTimeMillis();
				timeDistance = afterTime-beforeTime;
				mThreadActivityRef.get().data = null;
				if(null != mThreadActivityRef.get().myCamera)
				{
					mThreadActivityRef.get().myCamera.setPreviewCallback(null);
					mThreadActivityRef.get().myCamera.stopPreview();
					mThreadActivityRef.get().isPreview = false;
					mThreadActivityRef.get().myCamera.release();
					mThreadActivityRef.get().myCamera = null;
				}
				mThreadActivityRef.get().isGetdataok = 0;
				mThreadActivityRef.get().mHandler.sendEmptyMessage(closeview);
			}
		}
	}
	private static class MyOpenThread extends Thread {
		WeakReference<TRCardScan> mThreadActivityRef;

		public MyOpenThread(TRCardScan activity) {
			mThreadActivityRef = new WeakReference<TRCardScan>(activity);
		}
		@Override
		public void run() {
			super.run();
			if (mThreadActivityRef == null)
				return;
			if (mThreadActivityRef.get() == null)
				return;
			try {
				mThreadActivityRef.get().myCamera = Camera.open();
			} catch (Exception e) {
				Log.i(tag, "Camera open fail!");
				mThreadActivityRef.get().cameraErr = true;
			}
			if (mThreadActivityRef.get().myCamera != null) {
				if (mThreadActivityRef.get().isFlash == true) {
					mThreadActivityRef.get().openFlashlight();
				}
			}
		}
	}
	private static class MyHandler extends Handler {
		private final WeakReference<TRCardScan> mActivity;
		public MyHandler(TRCardScan activity) {
			mActivity = new WeakReference<TRCardScan>(activity);
		}
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
				case only_auto_focus:
					if(null != mActivity.get().myCamera && mActivity.get().isGetdataok == -1 )
					{

						try {
							mActivity.get().myCamera.autoFocus(mActivity.get().myAutoFocusCallback1);

						}catch (Exception e){
							e.printStackTrace();
						}


					}
					break;
				case auto_focus:
					if(null != mActivity.get().myCamera)
					{
						try {
							mActivity.get().myCamera.autoFocus(mActivity.get().myAutoFocusCallback);
						}catch (Exception e){
							e.printStackTrace();
						}
					}
					break;
				case take_pic_ok:
					mActivity.get().logtxt.setText("请稍后,正在识别中...");
					try {
						if(null != mActivity.get().myCamera)
						{
							mActivity.get().myCamera.takePicture(null, null, mActivity.get().myJpegCallback);
//							mActivity.get().myCamera.takePicture(mActivity.get().myShutterCallback, null, mActivity.get().myJpegCallback);
						}
					}catch (Exception e){
						e.printStackTrace();
					}
					break;
				case flashtxt:
					//logtxt.setText("请稍后,正在识别中..." + process);
					if (mActivity.get().mTextView != null) {
						mActivity.get().mTextView.setText(mActivity.get().process + "%");
					}
					if(mActivity.get().mProgressBar != null)
						mActivity.get().mProgressBar.setProgress(mActivity.get().process);
					break;
				case get_data_ok:
					mActivity.get().data = (byte[])msg.obj;
					if (mActivity.get().myCamera != null) {
						if (mActivity.get().isFlash == true) {
							mActivity.get().closeFlashlight();
						}
					}
					mActivity.get().isGetdataok = 1;
					if (isOpenProgress == true) {
						mActivity.get().mProgressBar.setVisibility(View.VISIBLE);
						mActivity.get().mTextView.setVisibility(View.VISIBLE);
					}
					mActivity.get().MyThread.start();
					break;
				case closeview:
					if (mActivity.get().isGetdataok == 0) {
						mActivity.get().CloseView();
					}
					break;
			}
		}
	};
	public boolean openFlashlight() {
		Camera.Parameters parameters = myCamera.getParameters();
		parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
		try {
			myCamera.setParameters(parameters);
		} catch (Exception e) {
		}
		return true;
	}

	public boolean closeFlashlight() {
		Camera.Parameters parameters = myCamera.getParameters();
		parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
		try {
			myCamera.setParameters(parameters);
		} catch (Exception e) {
		}
		return true;
	}
	public void CloseView()
	{
//		engineDemo.TR_ClearUP();
		Intent it = new Intent();
		it.putExtra("cardinfo", cardInfo);
		Bundle myBundle = new Bundle();
		myBundle.putParcelable("image", null);
		it.putExtras(myBundle);
		setResult(2, it);
		TRCardScan.this.finish();
	}
	int reqWidth;
	int reqHeight;


	/*下面三个是SurfaceHolder.Callback创建的回调函*/
	public void surfaceChanged(SurfaceHolder holder, int format, int width,int height)// 当SurfaceView/预览界面的格式和大小发生改变时，该方法被调用
	{

		reqWidth=width;
		reqHeight=height;

		Log.i(tag, "SurfaceHolder.Callback:surfaceChanged!");
		initCamera();
	}
	public void surfaceCreated(SurfaceHolder holder)// SurfaceView启动初次实例化，预览界面被创建时，该方法被调用
	{
		if (myCamera == null) {
			myCamera = Camera.open();
			if (myCamera == null) {
				mHandler.sendEmptyMessage(closeview);
				return;
			}
		}
		try {
			myCamera.setPreviewDisplay(mySurfaceHolder);
			Log.i(tag, "SurfaceHolder.Callback: surfaceCreated!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			if(null != myCamera){
				myCamera.release();
				myCamera = null;
			}
			e.printStackTrace();
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) //销毁时候调用
	{
		// TODO Auto-generated method stub
		Log.i(tag, "SurfaceHolder.Callback：Surface Destroyed");
		if(null != myCamera)
		{
			myCamera.setPreviewCallback(null);
			myCamera.stopPreview();
			isPreview = false;
			myCamera.release();
			myCamera = null;
		}
	}
	private Size getSizeForPreSize(Camera.Parameters parameters){
		List<Size> ViewSize = parameters.getSupportedPreviewSizes();
		List<Size> picSize = parameters.getSupportedPictureSizes();
		List<Size> MaxSizes = new ArrayList<Camera.Size>();
		List<Size> MinSize = new ArrayList<Camera.Size>();
		Size tempSize;

		/*屏幕分辨率排序*/
		for (int j = ViewSize.size(); j >= 0; j--) {
			for (int i = 0; i < j - 1; i++) {
				if(ViewSize.get(i).width > ViewSize.get(i + 1).width){
					tempSize = ViewSize.get(i + 1);
					ViewSize.set(i + 1, ViewSize.get(i));
					ViewSize.set(i,tempSize);
				}
			}
		}

		/*图片分辨率排序*/
		for (int j = picSize.size(); j >= 0; j--) {
			for (int i = 0; i < j - 1; i++) {
				if(picSize.get(i).width > picSize.get(i + 1).width){
					tempSize = picSize.get(i + 1);
					picSize.set(i + 1, picSize.get(i));
					picSize.set(i,tempSize);
				}
			}
		}

		for (int i = 0; i < picSize.size(); i++) {
			if(picSize.get(i).width >= 1500 ){
				MaxSizes.add(picSize.get(i));
			}else{
				MinSize.add(picSize.get(i));
			}
		}

		/*查找与屏幕分辨率一致的图片分辨率*/
		for (int i = ViewSize.size() - 1; i >= 0 ; i -- ) {
			if(ViewSize.get(i).width <= 1500){
				for (int j = MinSize.size() - 1; j >= 0; j--) {
					if((ViewSize.get(i).width * MinSize.get(j).height
							== MinSize.get(j).width * ViewSize.get(i).height) &&
							(MinSize.get(j).width >= 1024 && MinSize.get(j).height >= 768)){
						parameters.setPictureSize(MinSize.get(j).width,MinSize.get(j).height);
						Log.i(tag, "setPictureSize width --------  "+ MinSize.get(j).width + "     height-----------"+MinSize.get(j).height);
						if (isOpenLog == true) {
							String info = "pic size width=" + MinSize.get(j).width + "    height= "+ MinSize.get(j).height + "\n";
							WriteFile(info);
						}
						return ViewSize.get(i);
					}
				}
			}
		}
		for (int i = ViewSize.size() - 1; i >= 0 ; i -- ) {
			if(ViewSize.get(i).width <= 1500){
				for (int j = 0; j < MaxSizes.size(); j++) {
					if((double)ViewSize.get(i).width / ViewSize.get(i).height
							== (double)MaxSizes.get(j).width / MaxSizes.get(j).height){
						parameters.setPictureSize(MaxSizes.get(j).width,MaxSizes.get(j).height);
						Log.i(tag, "setPictureSize width --------  "+ MaxSizes.get(j).width + "     height-----------"+MaxSizes.get(j).height);
						if (isOpenLog == true) {
							String info = "pic size width=" +MaxSizes.get(j).width + "    height= "+ MaxSizes.get(j).height + "\n";
							WriteFile(info);
						}
						return ViewSize.get(i);
					}
				}
			}
		}

		/*未找到与屏幕分辨率相匹配的图片分辨率的情况下，采用最接近1024的图片分辨率*/
		if (picSize.size() > 0)
		{
			for (int i = 0; i < picSize.size(); i++)
			{
				if (picSize.get(i).width >= 1024)
				{
					Log.i(tag, "setPictureSize width --------  "+ picSize.get(i).width + "     height----------- "+ picSize.get(i).height);
					parameters.setPictureSize(picSize.get(i).width, picSize.get(i).height);
					if (isOpenLog == true) {
						String info = "pic size width=" +picSize.get(i).width + "    height= " + picSize.get(i).height + "\n";
						WriteFile(info);
					}
					return null;
				}
			}

			/*分辨率均较小，采用最大图片分辨率*/
			parameters.setPictureSize(picSize.get(picSize.size()-1).width, picSize.get(picSize.size()-1).height);
			if (isOpenLog == true) {
				String info = "pic size width=" +(picSize.get(picSize.size()-1).width) + "    height= " + picSize.get(picSize.size()-1).height + "\n";
				WriteFile(info);
			}
			Log.i(tag, "setPictureSize width --------  "+ picSize.get(picSize.size()-1).width + "     height----------- "+ picSize.get(picSize.size()-1).height);
			return null;
		}

		return null;/*返回NULL的时候，屏幕分辨率采用标准的屏幕分辨率*/
	}
	//初始化相机
	public void initCamera(){
		if (isOpenLog == true) {
			WriteFile("initCamera 0\n");
		}
		if(isPreview){
			myCamera.stopPreview();
		}
		if(null != myCamera){
			Camera.Parameters myParam = myCamera.getParameters();

			myParam.setPictureFormat(PixelFormat.JPEG);//设置拍照后存储的图片格式
			myParam.setJpegQuality(100);

			DisplayMetrics displaysMetrics = new DisplayMetrics();

			getWindowManager().getDefaultDisplay().getMetrics( displaysMetrics );
			int width = 0,height = 0;
			width = displaysMetrics.widthPixels;
			height = displaysMetrics.heightPixels;
//	        myParam.setPictureSize(width,height);
//			myParam.setPreviewSize(width, height);
			Size size = getSizeForPreSize(myParam);





//			if (size!= null) {
//				width = size.width;
//				height = size.height;
//			}
//			if (isOpenLog == true) {
//				WriteFile("setPreviewSize width="+ width + "    height=" + height +"\n");
//			}
//			try {
//
//				printsizeNew(myParam);
//			}catch (Exception e){
//				e.printStackTrace();
//				printsize(myParam);
//			}




			List<Size> ViewSize = myParam.getSupportedPreviewSizes();
			Size tempSize;

			/*屏幕分辨率排序*/
			for (int j = ViewSize.size(); j >= 0; j--) {
				for (int i = 0; i < j - 1; i++) {
					if(ViewSize.get(i).width > ViewSize.get(i + 1).width){
						tempSize = ViewSize.get(i + 1);
						ViewSize.set(i + 1, ViewSize.get(i));
						ViewSize.set(i,tempSize);
					}
				}
			}
			int PreSize = 0;
			for (int i = 1; i < ViewSize.size(); i++) {
				if (ViewSize.get(i-1).width <= width && width < ViewSize.get(i).width) {
					PreSize = i-1;
				}
			}
			if (PreSize > 0) {
				width = ViewSize.get(PreSize).width;
				height = ViewSize.get(PreSize).height;
			}
			else  if (size!= null) {
				width = size.width;
				height = size.height;
			}




			if (isOpenLog == true) {
				WriteFile("setPreviewSize width="+ width + "    height=" + height +"\n");
			}




			printsize(myParam);















































//			myParam.setPreviewSize(width, height);
			myParam.set("rotation", 0);
			myCamera.setDisplayOrientation(0);
			try
			{
				myCamera.setParameters(myParam);
			}
			catch(Exception e)
			{
//				ToastUtils.e(this,"设备不支持").show();
				Toast.makeText(this,"设备不支持",Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
			myCamera.startPreview();
			mHandler.sendEmptyMessageDelayed(only_auto_focus, 100);
			isPreview = true;
		}
		if (isOpenLog == true) {
			WriteFile("initCamera 1\n");
		}
	}

	/*为了实现拍照的快门声音及拍照保存照片下面三个回调变量*/
	ShutterCallback myShutterCallback = new ShutterCallback()//快门按下的回调，在这里我们可以设置类似播放“咔嚓”声之类的操作默认的就是咔嚓
	{
		public void onShutter() {
			Log.i(tag, "myShutterCallback:onShutter...");
		}
	};
	PictureCallback myRawCallback = new PictureCallback()// 拍摄的未压缩原数据的回调,可以为null
	{
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.i(tag, "myRawCallback:onPictureTaken...");
		}
	};
	PictureCallback myJpegCallback = new PictureCallback()//对jpeg图像数据的回调函数使用
	{
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.i(tag, "myJpegCallback:onPictureTaken...");
			if(null != data){
				myCamera.stopPreview();
				Message msg_data = mHandler.obtainMessage();
				msg_data.what = get_data_ok;
				msg_data.obj = data;
				mHandler.sendMessage(msg_data);
				isPreview = false;

			}

		}
	};
	public void WriteFile(String info){
		String jpegName = "";
		String savePath = GlobalData.saveRootPath;
		File folder = new File(savePath);
		if(!folder.exists())
		{
			folder.mkdirs();
		}
		long dataTake = System.currentTimeMillis();
		jpegName = savePath + "uilog.ini";
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(jpegName, true)));
			out.write(info);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public void saveJpeg(Bitmap bm){
		String jpegName = "";
		String savePath = GlobalData.saveImgPath;
		File folder = new File(savePath);
		if(!folder.exists())
		{
			folder.mkdirs();
		}
		long dataTake = System.currentTimeMillis();
		jpegName = savePath + dataTake +".jpg";
		try {
			FileOutputStream fout = new FileOutputStream(jpegName);
			BufferedOutputStream bos = new BufferedOutputStream(fout);
			bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			bos.flush();
			bos.close();
			Log.i(tag, "saveJpeg：存储完毕！");
		} catch (IOException e) {
			Log.i(tag, "saveJpeg:存储失败！");
			e.printStackTrace();
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode==3) {
			setResult(2);
			finish();
		}
	}
	@Override
	public void onBackPressed()//返回键
	{
		Intent it = new Intent();
		cardInfo.setNull();
		it.putExtra("cardinfo", cardInfo);
		setResult(1, it);
		TRCardScan.this.finish();
		super.onBackPressed();
	}
	@Override
	protected void onDestroy() {
		if(myCamera != null){
			myCamera.stopPreview();
			isPreview = false;
			myCamera.release();
			myCamera = null;
		}
		mHandler.removeCallbacksAndMessages(null);
		super.onDestroy();
	}
	@Override
	public void output(String out) {
		// TODO Auto-generated method stub

	}
	@Override
	public int UserProcess(int process) {
		// TODO Auto-generated method stub
		if(process < 0 || process > 100)
			return 0;
		Log.d("libLPOCRDLL","Recphoto process = "+ process);
		this.process = process;
		mHandler.sendEmptyMessage(flashtxt);
		return 0;
	}






	private void  printsizeNew(Camera.Parameters parameters){

		List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
		int maxSize = Math.max(display.getWidth(), display.getHeight());
		int length = sizes.size();
		if (maxSize > 0) {
			for (int i = 0; i < length; i++) {
				if (maxSize <= Math.max(sizes.get(i).width, sizes.get(i).height)) {
					parameters.setPictureSize(sizes.get(i).width, sizes.get(i).height);
					break;
				}
			}
		}
		List<Camera.Size> ShowSizes = parameters.getSupportedPreviewSizes();
		int showLength = ShowSizes.size();
		if (maxSize > 0) {
			for (int i = 0; i < showLength; i++) {
				if (maxSize <= Math.max(ShowSizes.get(i).width, ShowSizes.get(i).height)) {
					parameters.setPreviewSize(ShowSizes.get(i).width, ShowSizes.get(i).height);
					break;
				}
			}
		}

	}







	@SuppressWarnings("unchecked")
	private void  printsize(Camera.Parameters parameters){

		List<Size> pszize = parameters.getSupportedPreviewSizes();
//      List<Size> pszize = parameters.getSupportedPictureSizes();
		if (null != pszize && 0 < pszize.size()) {
			int height[] = new int[pszize.size()];// 声明一个数组
			Map<Integer, Integer> map = new HashMap<Integer, Integer>();

			for (int i = 0; i < pszize.size(); i++) {
				Size size = (Size) pszize.get(i);
				int sizeheight = size.height;
				int sizewidth = size.width;
				height[i] = sizeheight;
				map.put(sizeheight, sizewidth);
				System.out.println("size.width:"+sizewidth+"\tsize.height:"+sizeheight);
			}
//                Arrays.sort(height);
			// 设置
//                parameters.setPictureSize(map.get(height[0]),height[0]);

			parameters.setPreviewSize(map.get(height[height.length-1]),height[height.length-1]);

		}



		List<Size> SupportedPreviewSizes = parameters
				.getSupportedPreviewSizes();// 获取支持预览照片的尺寸


		ComparatorUser comparator=new ComparatorUser();
		Collections.sort(SupportedPreviewSizes, comparator);


		for (int i = 0; i < SupportedPreviewSizes.size(); i++) {
			Size size = SupportedPreviewSizes.get(i);
			System.out.println(size.width+"====SupportedPreviewSizes=="+size.height);
		}
		Size previewSize = SupportedPreviewSizes.get(0);// 从List取出Size
		parameters
				.setPreviewSize(previewSize.width, previewSize.height);// 设置预览照片的大小
		List<Size> supportedPictureSizes = parameters
				.getSupportedPictureSizes();// 获取支持保存图片的尺寸
		Collections.sort(supportedPictureSizes, comparator);
		Size pictureSize = supportedPictureSizes.get(0);// 从List取出Size

		parameters
				.setPictureSize(pictureSize.width, pictureSize.height);// 设置照片的大小

	}




	private class ComparatorUser implements Comparator {

		public int compare(Object arg0, Object arg1) {
			Size user0=(Size)arg0;
			Size user1=(Size)arg1;
			if ((user0.width+user0.height)>(user1.width+user1.height)) {
				return -1;

			}else if ((user0.width+user0.height)<(user1.width+user1.height)) {
				return 1;

			}else {
				return 0;
			}



		}
	}



	protected Camera.Size determinePreviewSize(Camera.Parameters parameters, int reqWidth, int reqHeight) {
		// Meaning of width and height is switched for preview when portrait,
		// while it is the same as user's view for surface and metrics.
		// That is, width must always be larger than height for setPreviewSize.
		int reqPreviewWidth; // requested width in terms of camera hardware
		int reqPreviewHeight; // requested height in terms of camera hardware

			reqPreviewWidth = reqWidth;
			reqPreviewHeight = reqHeight;
		// Adjust surface size with the closest aspect-ratio
		float reqRatio = ((float) reqPreviewWidth) / reqPreviewHeight;
		float curRatio, deltaRatio;
		float deltaRatioMin = Float.MAX_VALUE;
		Camera.Size retSize = null;
		for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
			curRatio = ((float) size.width) / size.height;
			deltaRatio = Math.abs(reqRatio - curRatio);
			if (deltaRatio < deltaRatioMin) {
				deltaRatioMin = deltaRatio;
				retSize = size;
			}
		}
		return retSize;
	}


	protected Camera.Size determinePictureSize(Camera.Size previewSize,Camera.Parameters parameters) {
		Camera.Size retSize = null;
		for (Camera.Size size : parameters.getSupportedPictureSizes()) {
			if (size.equals(previewSize)) {
				return size;
			}
		}

		// if the preview size is not supported as a picture size
		float reqRatio = ((float) previewSize.width) / previewSize.height;
		float curRatio, deltaRatio;
		float deltaRatioMin = Float.MAX_VALUE;
		for (Camera.Size size : parameters.getSupportedPictureSizes()) {
			curRatio = ((float) size.width) / size.height;
			deltaRatio = Math.abs(reqRatio - curRatio);
			if (deltaRatio < deltaRatioMin) {
				deltaRatioMin = deltaRatio;
				retSize = size;
			}
		}

		return retSize;
	}


}

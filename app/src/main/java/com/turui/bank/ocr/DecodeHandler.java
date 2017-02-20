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

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.idcard.CardInfo;
import com.idcard.GlobalData;
import com.idcard.TFieldID;
import com.idcard.TStatus;
import com.idcard.TengineID;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;


final class DecodeHandler extends Handler {

	private static final String TAG = DecodeHandler.class.getSimpleName();

	static int x1 = 0;
	static int y1 = 0;
	static int x2 = 0;
	static int y2 = 0;
	private final CaptureActivity activity;
	DecodeHandler(CaptureActivity activity) {
		this.activity = activity;
	}

	@Override
	public void handleMessage(Message message) {
		switch (message.what) {
			case GlobalDataDefine.decode:
				//Log.d(TAG, "Got decode message");
				decode((byte[]) message.obj, message.arg1, message.arg2);
				break;
			case GlobalDataDefine.quit:
				Looper.myLooper().quit();
				break;
		}
	}

	/**
	 * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
	 * reuse the same reader objects from one decode to the next.
	 *
	 * @param data   The YUV preview frame.
	 * @param width  The width of the preview frame.
	 * @param height The height of the preview frame.
	 */

	/**
	 * saveJpeg功能说明：
	 * 以时间戳为文件名，保存Bitmap到SD卡的AATurec/img的目录下
	 */
	public void saveJpeg(Bitmap bm){
		String jpegName = "";
		String savePath = Environment.getExternalStorageDirectory().getPath() + "/AATurec/img/";
		String savePathRoot = Environment.getExternalStorageDirectory().getPath() + "/AATurec/";
		File folder = new File(savePath);
		if(!folder.exists())
		{
			folder = new File(savePathRoot);
			folder.mkdir();

			folder = new File(savePath);
			folder.mkdir();
		}

		long dataTake = System.currentTimeMillis();
		jpegName = savePath + dataTake +".jpg";
		try {
			FileOutputStream fout = new FileOutputStream(jpegName);
			BufferedOutputStream bos = new BufferedOutputStream(fout);
			bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			bos.flush();
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * saveJpeg功能说明：
	 * 以filename为文件名，保存Bitmap到SD卡的AATurec/img的目录下
	 */
	public void saveJpegSig(Bitmap bm, String filename){
		String jpegName = "";
		String savePath = Environment.getExternalStorageDirectory().getPath() + "/AATurec/img/";
		String savePathRoot = Environment.getExternalStorageDirectory().getPath() + "/AATurec/";
		File folder = new File(savePath);
		if(!folder.exists())
		{
			folder = new File(savePathRoot);
			folder.mkdir();

			folder = new File(savePath);
			folder.mkdir();
		}

		long dataTake = System.currentTimeMillis();
		jpegName = savePath + filename;
		try {
			FileOutputStream fout = new FileOutputStream(jpegName);
			BufferedOutputStream bos = new BufferedOutputStream(fout);
			bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			bos.flush();
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void WriteFile(String info){
		String jpegName = "";
		String savePath = GlobalData.saveRootPath;
		File folder = new File(savePath);
		if(!folder.exists())
		{
			folder.mkdirs();
		}

		folder = new File(GlobalData.saveImgPath);
		if(!folder.exists())
		{
			folder.mkdirs();
		}

		folder = new File(GlobalData.saveLogPath);
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
	private void decode(byte[] data, int width, int height) {
		long start = System.currentTimeMillis();
		String rawResult = null;

		//modify here
//    byte[] rotatedData = new byte[data.length];
//    for (int y = 0; y < height; y++) {
//        for (int x = 0; x < width; x++)
//            rotatedData[x * height + height - y - 1] = data[x + y * width];
//    }
//    int tmp = width; // Here we are swapping, that's the difference to #11
//    width = height;
//    height = tmp;

//    PlanarYUVLuminanceSource source = CameraManager.get().buildLuminanceSource(data, width, height);
//    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
//    try {
//      rawResult = multiFormatReader.decodeWithState(bitmap);
//    } catch (ReaderException re) {
//      // continue
//    } finally {
//      multiFormatReader.reset();
//    }
//    rawResult = new Result("hello", null, null, null);
		long beforeTime=0;
		long afterTime=0;
		long timeDistance;
		if (activity.isOpenLog == true) {
			SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			String date = sDateFormat.format(new java.util.Date());
			WriteFile("\n" + date + "\n" +"decode 0\n");
			WriteFile("width = "+ width + "  height = " + height + "\n");
		}
		beforeTime =System.currentTimeMillis();
		if (activity.TakeBitmap != null) {
			activity.TakeBitmap.recycle();
			activity.TakeBitmap = null;
		}
		if ( activity.SmallBitmap != null) {
			activity.SmallBitmap.recycle();
			activity.SmallBitmap = null;
		}
//	Point screenResolution = CameraManager.get().getScreenPoint();
		YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, width,
				height, null);
		ByteArrayOutputStream outputSteam = new ByteArrayOutputStream();
		yuvimage.compressToJpeg(new Rect(0, 0, width, height), 100, outputSteam);
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Config.RGB_565;
		activity.TakeBitmap = BitmapFactory.decodeByteArray(outputSteam.toByteArray(), 0, outputSteam.toByteArray().length,options);
		try {
			outputSteam.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
//	saveJpeg(rectBitmap);
		Rect rect = CameraManager.get().getFramingRectInPreview();
		activity.engineDemo.TR_LoadMemBitMap(activity.TakeBitmap);
		if (activity.isOpenLog == true) {
			WriteFile("TR_LoadMemBitMap\n");
		}
		if (activity.isOpenLog == true) {
			String jpgNameString = System.currentTimeMillis() + ".jpg";
			WriteFile("MyThread TR_SaveImage path = " + jpgNameString +"\n");
			activity.engineDemo.TR_SaveImage(GlobalData.saveImgPath + jpgNameString);
		}
//	saveJpegSig(rectBitmap);
		int ret = activity.engineDemo.TR_BankJudgeExist4Margin(rect.left, rect.top ,rect.right , rect.bottom);
		if (activity.isOpenLog == true) {
			WriteFile("TR_BankJudgeExist4Margin\n");
		}
		yuvimage = null;
		afterTime =System.currentTimeMillis();
		timeDistance = (afterTime- beforeTime);
		ViewfinderView viewfinderView = activity.getViewfinderView();
		viewfinderView.SetEdgeVal(ret);
		Message message1 = Message.obtain(activity.getHandler(), GlobalDataDefine.drawfindview);
		message1.sendToTarget();
//	activity.drawViewfinder();
		if (ret == 15 || ret == 7|| ret == 11|| ret == 13|| ret == 14){//ret == 3 || ret == 5 || ret > 9){
//    		ret == 15 || ret == 7|| ret == 11|| ret == 13|| ret == 14) {
//    	saveJpeg(rectBitmap);
			if (activity.isOpenLog == true) {
				WriteFile("TR_RECOCR0\n");
			}
			TStatus isRecSucess = activity.engineDemo.TR_RECOCR();
			if (activity.isOpenLog == true) {
				WriteFile("TR_RECOCR1\n");
			}
			if (activity.tengineID == TengineID.TIDCARD2 || activity.tengineID == TengineID.TIDSSCCARD) {
				activity.engineDemo.TR_FreeImage();
				TStatus tStatus1 = activity.engineDemo.TR_GetCardNumState();
				if (tStatus1 == TStatus.TR_FAIL) {

					Message message = Message.obtain(activity.getHandler(), GlobalDataDefine.decode_failed);
					message.sendToTarget();
				}
				else if (isRecSucess == TStatus.TR_TIME_OUT) {
					Message message = Message.obtain(activity.getHandler(), GlobalDataDefine.decode_succeeded, "时间过期，请call 0592-5588468");
					Bundle bundle = new Bundle();
					bundle.putParcelable(DecodeThread.BARCODE_BITMAP, null);
					message.setData(bundle);
					//Log.d(TAG, "Sending decode succeeded message...");
					message.sendToTarget();
				}
				else  if(activity.tengineID == TengineID.TIDCARD2){
					String num = activity.engineDemo.TR_GetOCRFieldStringBuf(TFieldID.NUM);
					String name =activity.engineDemo.TR_GetOCRFieldStringBuf(TFieldID.NAME);
					String bir = activity.engineDemo.TR_GetOCRFieldStringBuf(TFieldID.BIRTHDAY);
					String address = activity.engineDemo.TR_GetOCRFieldStringBuf(TFieldID.ADDRESS);
					String folk =  activity.engineDemo.TR_GetOCRFieldStringBuf(TFieldID.FOLK);
					String sex =  activity.engineDemo.TR_GetOCRFieldStringBuf(TFieldID.SEX);
					String period =  activity.engineDemo.TR_GetOCRFieldStringBuf(TFieldID.PERIOD);
					String issue =  activity.engineDemo.TR_GetOCRFieldStringBuf(TFieldID.ISSUE);
//				String version = null;
					if (num != null) {
						long end = System.currentTimeMillis();
//					rawResult = CardNum + "\n (" + (end - start) + " ms)\n";
//					rawResult = version + "\n" + CardNum + "\n" + BankName + "\n (" + (end - start) + " ms)\n";
						rawResult =   "姓名  	   : "+ name + "\n"
								+ "性别  	   : "+ sex + "\n"
								+ "民族            : "+ folk + "\n"
								+ "出生日期  : "+ bir + "\n"
								+ "地址            : "+ address + "\n"
								+ "号码            : "+ num + "\n"
								+ "签发机关   : "+ issue + "\n"
								+ "有效期限   : "+ period + "\n";
						Log.d(TAG, "Found barcode (" + (end - start) + " ms)\n" + rawResult.toString());
						Message message = Message.obtain(activity.getHandler(), GlobalDataDefine.decode_succeeded, rawResult);
						Bundle bundle = new Bundle();
						if (activity.TakeBitmap  != null && width != 0 && height != 0) {
							//bundle.putParcelable(DecodeThread.BARCODE_BITMAP, Bitmap.createBitmap(activity.TakeBitmap, x1, y1, x2-x1, y2-y1));
							bundle.putParcelable(DecodeThread.BARCODE_BITMAP, null);
							activity.SmallBitmap = null;//Bitmap.createBitmap(activity.TakeBitmap, x1, y1, x2-x1, y2-y1);
						}
						else
							bundle.putParcelable(DecodeThread.BARCODE_BITMAP, null);
						byte []hdata = activity.engineDemo.TR_GetHeadImgBuf();
						int size =  activity.engineDemo.TR_GetHeadImgBufSize();
						if (size > 0 && hdata != null && hdata.length > 0) {
							activity.SmallBitmap = BitmapFactory.decodeByteArray(hdata,0,size);
						}
						CardInfo cardInfo = new CardInfo();
						cardInfo.setFieldString(TFieldID.NAME, name);
						cardInfo.setFieldString(TFieldID.SEX, sex);
						cardInfo.setFieldString(TFieldID.FOLK, folk);
						cardInfo.setFieldString(TFieldID.BIRTHDAY, bir);
						cardInfo.setFieldString(TFieldID.ADDRESS, address);
						cardInfo.setFieldString(TFieldID.NUM, num);
						cardInfo.setFieldString(TFieldID.PERIOD, period);
						cardInfo.setFieldString(TFieldID.ISSUE, issue);
						cardInfo.setAllinfo(rawResult);
						bundle.putSerializable("cardinfo",  cardInfo);
						message.setData(bundle);
						//Log.d(TAG, "Sending decode succeeded message...");
						message.sendToTarget();
					}
					else{
						Message message = Message.obtain(activity.getHandler(), GlobalDataDefine.decode_failed);
						message.sendToTarget();
					}

				}

				else  if(activity.tengineID == TengineID.TIDSSCCARD){

					String SSC_NUM = activity.engineDemo.TR_GetOCRFieldStringBuf(TFieldID.SSC_NUM);
					String SSC_NAME =activity.engineDemo.TR_GetOCRFieldStringBuf(TFieldID.SSC_NAME);
					String SSC_SHORTNUM = activity.engineDemo.TR_GetOCRFieldStringBuf(TFieldID.SSC_SHORTNUM);
					String SSC_PERIOD = activity.engineDemo.TR_GetOCRFieldStringBuf(TFieldID.SSC_PERIOD);
					String SSC_BANKNUM =  activity.engineDemo.TR_GetOCRFieldStringBuf(TFieldID.SSC_BANKNUM);
//				String version = null;
					//if (num != null)
					{
						long end = System.currentTimeMillis();
//					rawResult = CardNum + "\n (" + (end - start) + " ms)\n";
//					rawResult = version + "\n" + CardNum + "\n" + BankName + "\n (" + (end - start) + " ms)\n";
						rawResult =   //"姓名  	   : "+ SSC_NAME + "\n"
								"社会保障号码          : "+ SSC_NUM + "\n";
						//+ "卡号                   : "+ SSC_SHORTNUM + "\n"
						//+ "有效期限          : "+ SSC_PERIOD + "\n"
						//+ "银行卡号          : "+ SSC_BANKNUM + "\n";
						Log.d(TAG, "Found barcode (" + (end - start) + " ms)\n" + rawResult.toString());
						Message message = Message.obtain(activity.getHandler(), GlobalDataDefine.decode_succeeded, rawResult);
						Bundle bundle = new Bundle();
						if (activity.TakeBitmap  != null && width != 0 && height != 0) {
							//bundle.putParcelable(DecodeThread.BARCODE_BITMAP, Bitmap.createBitmap(activity.TakeBitmap, x1, y1, x2-x1, y2-y1));
							bundle.putParcelable(DecodeThread.BARCODE_BITMAP, null);
							activity.SmallBitmap = null;//Bitmap.createBitmap(activity.TakeBitmap, x1, y1, x2-x1, y2-y1);
						}
						else
							bundle.putParcelable(DecodeThread.BARCODE_BITMAP, null);
//					byte []hdata = activity.engineDemo.TR_GetHeadImgBuf();
//					int size =  activity.engineDemo.TR_GetHeadImgBufSize();
//					if (size > 0 && hdata != null && hdata.length > 0) {
//						activity.SmallBitmap = BitmapFactory.decodeByteArray(hdata,0,size);
//					}
						CardInfo cardInfo = new CardInfo();
//					cardInfo.setFieldString(TFieldID.SSC_NAME, SSC_NAME);
						cardInfo.setFieldString(TFieldID.SSC_NUM, SSC_NUM);
//					cardInfo.setFieldString(TFieldID.SSC_SHORTNUM, SSC_SHORTNUM);
//					cardInfo.setFieldString(TFieldID.SSC_PERIOD, SSC_PERIOD);
//					cardInfo.setFieldString(TFieldID.SSC_BANKNUM, SSC_BANKNUM);
						cardInfo.setAllinfo(rawResult);
						bundle.putSerializable("cardinfo",  cardInfo);
						message.setData(bundle);
						//Log.d(TAG, "Sending decode succeeded message...");
						message.sendToTarget();
					}
//				else{
//			    	Message message = Message.obtain(activity.getHandler(), GlobalData.decode_failed);
//			        message.sendToTarget();
//			    }

				}
			}
			else if (activity.tengineID == TengineID.TIDBANK) {
				if (  isRecSucess != TStatus.TR_FAIL)
				{
					x1 = activity.engineDemo.TR_GetLineRect(1);
					y1 = activity.engineDemo.TR_GetLineRect(2);
					x2 = activity.engineDemo.TR_GetLineRect(3);
					y2 = activity.engineDemo.TR_GetLineRect(4);

				}
				activity.engineDemo.TR_FreeImage();
				if (isRecSucess == TStatus.TR_FAIL) {

					Message message = Message.obtain(activity.getHandler(), GlobalDataDefine.decode_failed);
					message.sendToTarget();
				}
				else if (isRecSucess == TStatus.TR_TIME_OUT) {
					Message message = Message.obtain(activity.getHandler(), GlobalDataDefine.decode_succeeded, "时间过期，请call 0592-5588468");
					Bundle bundle = new Bundle();
					bundle.putParcelable(DecodeThread.BARCODE_BITMAP, null);
					message.setData(bundle);
					//Log.d(TAG, "Sending decode succeeded message...");
					message.sendToTarget();
				}
				else {
					String CardNum = activity.engineDemo.TR_GetOCRFieldStringBuf(TFieldID.TBANK_NUM);
					String BankName =activity.engineDemo.TR_GetOCRFieldStringBuf(TFieldID.TBANK_NAME);
					String BANK_OrganizeCode = activity.engineDemo.TR_GetOCRFieldStringBuf(TFieldID.TBANK_ORGCODE);
					String BANK_CardClass = activity.engineDemo.TR_GetOCRFieldStringBuf(TFieldID.TBANK_CLASS);
					String CARD_NAME =  activity.engineDemo.TR_GetOCRFieldStringBuf(TFieldID.TBANK_CARD_NAME);
//				String version = null;
					if (CardNum != null) {
						long end = System.currentTimeMillis();
//					rawResult = CardNum + "\n (" + (end - start) + " ms)\n";
//					rawResult = version + "\n" + CardNum + "\n" + BankName + "\n (" + (end - start) + " ms)\n";
						rawResult =   "银行卡号: "+ CardNum + "\n"
								+ "发卡行    : "+ BankName + "\n"
								+ "机构代码: "+ BANK_OrganizeCode + "\n"
								+ "卡种         : "+ BANK_CardClass + "\n"
								+ "卡名         : "+ CARD_NAME + "\n";
						Log.d(TAG, "Found barcode (" + (end - start) + " ms)\n" + rawResult.toString());
						Message message = Message.obtain(activity.getHandler(), GlobalDataDefine.decode_succeeded, rawResult);
						Bundle bundle = new Bundle();
						if (activity.TakeBitmap  != null && width != 0 && height != 0) {
							//bundle.putParcelable(DecodeThread.BARCODE_BITMAP, Bitmap.createBitmap(activity.TakeBitmap, x1, y1, x2-x1, y2-y1));
							bundle.putParcelable(DecodeThread.BARCODE_BITMAP, null);
							activity.SmallBitmap = Bitmap.createBitmap(activity.TakeBitmap, x1, y1, x2-x1, y2-y1);
						}
						else
							bundle.putParcelable(DecodeThread.BARCODE_BITMAP, null);
						CardInfo cardInfo = new CardInfo();
						cardInfo.setFieldString(TFieldID.TBANK_NUM, CardNum);
						cardInfo.setFieldString(TFieldID.TBANK_NAME, BankName);
						cardInfo.setFieldString(TFieldID.TBANK_ORGCODE, BANK_OrganizeCode);
						cardInfo.setFieldString(TFieldID.TBANK_CLASS, BANK_CardClass);
						cardInfo.setFieldString(TFieldID.TBANK_CARD_NAME, CARD_NAME);
						cardInfo.setAllinfo(rawResult);
						bundle.putSerializable("cardinfo",  cardInfo);
						message.setData(bundle);
						//Log.d(TAG, "Sending decode succeeded message...");
						message.sendToTarget();
					}
					else{
						Message message = Message.obtain(activity.getHandler(), GlobalDataDefine.decode_failed);
						message.sendToTarget();
					}

				}
			}

		}
		else{
			activity.engineDemo.TR_FreeImage();
			Message message = Message.obtain(activity.getHandler(), GlobalDataDefine.decode_failed);
			message.sendToTarget();
		}
	}

}

package com.sdkInterface;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.idcard.CardInfo;
import com.idcard.TFieldID;
import com.idcard.TRECAPIImpl;
import com.idcard.TStatus;
import com.idcard.TengineID;
import com.turui.bank.ocr.CaptureActivity;
import com.turui.bank.ocr.card.TRCardScan;

/**
 * 1.初始化 new CardScanner();
 * 2.设置监听事件
 * 3.获取数据
 */

public class CardScaner {
    private TRECAPIImpl engineDemo = new TRECAPIImpl();
    private static TengineID tengineID = TengineID.TUNCERTAIN;
    private byte[] data = null;// 获取拍照流数据
    public int isGetdataok = -1;//数据处理 流程  刚开始是-1  拍照后设置为1  识别成功后为0
    private Handler mHandler = new MyHandler();// 拍照进程处理
    private boolean flag;
    private int whichScan;
    public static final int IDCardFront = 2096;
    public static final int IDCardBack = 2097;
    public static final int BankCard = 2098;
    public static final int ScanIDCardFront = 2093;
    public static final int ScanIDCardBack = 2095;
    public static final int ScanBankCard = 2094;
    public static final int ScanError = 3000;


    CardInfo cardInfo = new CardInfo();
    private Context context;

    private DataListener dataListener;

    private Bitmap bitmap;

    public interface DataListener {
        void data(int i, Object object);

        void error();

        void start();

        void end();
    }

    public void GetDataListener(DataListener dataListener) {
        this.dataListener = dataListener;
    }

    public CardScaner(Context context) {
        this.context = context;
    }

    /**
     * 获取身份证前的信息
     *
     * @param bitmap
     */
    public void getIDCardFrontData(Bitmap bitmap) {
        this.bitmap = bitmap;
        initIDCardSDK(IDCardFront);
        toGetIDCardFront();
    }

    /**
     * 获取身份证后的信息
     *
     * @param bitmap
     */
    public void getIDCardBackData(Bitmap bitmap) {
        this.bitmap = bitmap;
        initIDCardSDK(IDCardBack);
        toGetIDCardFront();
    }

    /**
     * 获取银行卡信息（不推荐使用，对图片要求比较苛刻，并非清晰等条件因素决定，具体条件因素无法获知）
     *
     * @param bitmap
     */
    public void getBankCardData(Bitmap bitmap) {
        this.bitmap = bitmap;
        initIDCardSDK(BankCard);
        toGetIDCardFront();
    }

    public void scanIDCardFront(Activity activity) {
        initIDCardSDK(ScanIDCardFront);
        TRCardScan.isOpenProgress = true;
        Intent intent = new Intent(activity, TRCardScan.class);
        intent.putExtra("engine", engineDemo);
        intent.putExtra(TRCardScan.tag, TRCardScan.ONFONT);
        activity.startActivityForResult(intent, ScanIDCardFront);
    }

    public void scanIDCardBack(Activity activity) {
        initIDCardSDK(ScanIDCardBack);
        TRCardScan.isOpenProgress = true;
        Intent intent = new Intent(activity, TRCardScan.class);
        intent.putExtra("engine", engineDemo);
        intent.putExtra(TRCardScan.tag, TRCardScan.ONBACK);
        activity.startActivityForResult(intent, ScanIDCardBack);
    }

    public void scanBankCard(Activity activity) {
        initIDCardSDK(ScanBankCard);
        Intent intent = new Intent(activity, CaptureActivity.class);
        intent.putExtra("engine", engineDemo);
        activity.startActivityForResult(intent, ScanBankCard);
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case IDCardFront:
                    CardInfo cardInfoFront = (CardInfo) msg.getData().getSerializable("content");
                    assert cardInfoFront != null;
                    if (TextUtils.isEmpty(cardInfoFront.getFieldString(TFieldID.NAME)) ||
                            TextUtils.isEmpty(cardInfoFront.getFieldString(TFieldID.SEX)) ||
                            TextUtils.isEmpty(cardInfoFront.getFieldString(TFieldID.FOLK)) ||
                            TextUtils.isEmpty(cardInfoFront.getFieldString(TFieldID.BIRTHDAY)) ||
                            TextUtils.isEmpty(cardInfoFront.getFieldString(TFieldID.ADDRESS)) ||
                            TextUtils.isEmpty(cardInfoFront.getFieldString(TFieldID.NUM))) {
                        dataListener.error();
                        dataListener.end();
                        break;
                    }
                    dataListener.data(IDCardFront, cardInfoFront);
                    dataListener.end();
                    break;
                case IDCardBack:
                    CardInfo cardInfoBack = (CardInfo) msg.getData().getSerializable("content");
                    assert cardInfoBack != null;
                    if (TextUtils.isEmpty(cardInfoBack.getFieldString(TFieldID.ISSUE)) ||
                            TextUtils.isEmpty(cardInfoBack.getFieldString(TFieldID.PERIOD))) {
                        dataListener.error();
                        dataListener.end();
                        break;
                    }
                    dataListener.data(IDCardBack, cardInfoBack);
                    dataListener.end();
                    break;
                case BankCard:
                    CardInfo bankCardInfo = (CardInfo) msg.getData().getSerializable("content");
                    assert bankCardInfo != null;
                    if (TextUtils.isEmpty(bankCardInfo.getFieldString(TFieldID.TBANK_NUM))) {
                        dataListener.error();
                        dataListener.end();
                        break;
                    }
                    dataListener.data(BankCard, bankCardInfo);
                    dataListener.end();
                    break;
            }
        }
    }

    /**
     * 初始化sdk
     */
    private void initIDCardSDK(int i) {
        switch (i) {
            case IDCardFront:
                dataListener.start();
                whichScan = IDCardFront;
                setEngineType(TengineID.TIDCARD2);
                break;
            case IDCardBack:
                dataListener.start();
                whichScan = IDCardBack;
                setEngineType(TengineID.TIDCARD2);
                break;
            case BankCard:
                dataListener.start();
                whichScan = BankCard;
                setEngineType(TengineID.TIDBANK);
                break;
            case ScanIDCardFront:
                TRCardScan.SetEngineType(TengineID.TIDCARD2);
                break;
            case ScanIDCardBack:
                TRCardScan.SetEngineType(TengineID.TIDCARD2);
                break;
            case ScanBankCard:
                TRCardScan.SetEngineType(TengineID.TIDBANK);
                break;
        }
        TStatus tStatus = engineDemo.TR_StartUP();
        if (tStatus == TStatus.TR_TIME_OUT) {
            Toast.makeText(context, "引擎过期", Toast.LENGTH_SHORT).show();
        } else if (tStatus == TStatus.TR_FAIL) {
            Toast.makeText(context, "引擎初始化失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void setEngineType(TengineID tidcard2) {
        tengineID = tidcard2;
    }

    private void toGetIDCardFront() {
        TStatus ret = engineDemo.TR_SetSupportEngine(tengineID);
        if (ret != TStatus.TR_OK) {
            Toast.makeText(context, "引擎不支持", Toast.LENGTH_SHORT).show();
        }
//        data = FormatTools.getInstance().Bitmap2Bytes(bitmap);
        isGetdataok = 1;
        flag = true;
        new MyThread().start();
    }

    private class MyThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (flag) {
                flag = false;
                if (whichScan == IDCardBack || whichScan == IDCardFront) {
                    if (isGetdataok == 1 && bitmap != null) {
                        TStatus isRecSucess = TStatus.TR_FAIL;
//                    Bitmap mBitmap = null;
//                    BitmapFactory.Options options = new BitmapFactory.Options();
//                    options.inPreferredConfig = Bitmap.Config.RGB_565;
//                    mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                        engineDemo.TR_LoadMemBitMap(bitmap);
                        isRecSucess = engineDemo.TR_RECOCR();
                        engineDemo.TR_FreeImage();
                        if (isRecSucess == TStatus.TR_OK) {
                            if (tengineID == TengineID.TIDCARD2) {
                                String name = engineDemo.TR_GetOCRFieldStringBuf(TFieldID.NAME);
                                String sex = engineDemo.TR_GetOCRFieldStringBuf(TFieldID.SEX);
                                String folk = engineDemo.TR_GetOCRFieldStringBuf(TFieldID.FOLK);
                                String BirthDay = engineDemo.TR_GetOCRFieldStringBuf(TFieldID.BIRTHDAY);
                                String Address = engineDemo.TR_GetOCRFieldStringBuf(TFieldID.ADDRESS);
                                String CardNum = engineDemo.TR_GetOCRFieldStringBuf(TFieldID.NUM);
                                String issue = engineDemo.TR_GetOCRFieldStringBuf(TFieldID.ISSUE);
                                String period = engineDemo.TR_GetOCRFieldStringBuf(TFieldID.PERIOD);
                                String allinfo = engineDemo.TR_GetOCRStringBuf();
                                cardInfo.setFieldString(TFieldID.NAME, name);
                                cardInfo.setFieldString(TFieldID.SEX, sex);
                                cardInfo.setFieldString(TFieldID.FOLK, folk);
                                cardInfo.setFieldString(TFieldID.BIRTHDAY, BirthDay);
                                cardInfo.setFieldString(TFieldID.ADDRESS, Address);
                                cardInfo.setFieldString(TFieldID.NUM, CardNum);
                                cardInfo.setFieldString(TFieldID.ISSUE, issue);
                                cardInfo.setFieldString(TFieldID.PERIOD, period);
                                cardInfo.setAllinfo(allinfo);
                                Message message = new Message();
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("content", cardInfo);
                                message.setData(bundle);
                                switch (whichScan) {
                                    case IDCardBack:
                                        message.what = IDCardBack;
                                        break;
                                    case IDCardFront:
                                        message.what = IDCardFront;
                                        break;
                                }
                                mHandler.sendMessage(message);
                            } else {
                                Message message = new Message();
                                message.what = ScanError;
                                mHandler.sendMessage(message);
                            }
                        } else {
                            cardInfo.setNull();
                            Message message = new Message();
                            message.what = ScanError;
                            mHandler.sendMessage(message);
                        }
                        isGetdataok = 0;
                    }
                } else if (whichScan == BankCard) {
                    String rawResult = null;
                    TStatus isRecSucess = TStatus.TR_FAIL;
//                    Bitmap mBitmap = null;
//                    BitmapFactory.Options options = new BitmapFactory.Options();
//                    options.inPreferredConfig = Bitmap.Config.RGB_565;
//                    mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                    engineDemo.TR_LoadMemBitMap(bitmap);
                    isRecSucess = engineDemo.TR_RECOCR();
                    engineDemo.TR_FreeImage();
                    if (isRecSucess != TStatus.TR_FAIL) {
                        Message message = new Message();
                        message.what = ScanError;
                        mHandler.sendMessage(message);
                    }
                    if (isRecSucess == TStatus.TR_FAIL) {
                        Message message = new Message();
                        message.what = ScanError;
                        mHandler.sendMessage(message);
                    }
                    String CardNum = engineDemo.TR_GetOCRFieldStringBuf(TFieldID.TBANK_NUM);
                    String BankName = engineDemo.TR_GetOCRFieldStringBuf(TFieldID.TBANK_NAME);
                    String BANK_OrganizeCode = engineDemo.TR_GetOCRFieldStringBuf(TFieldID.TBANK_ORGCODE);
                    String BANK_CardClass = engineDemo.TR_GetOCRFieldStringBuf(TFieldID.TBANK_CLASS);
                    String CARD_NAME = engineDemo.TR_GetOCRFieldStringBuf(TFieldID.TBANK_CARD_NAME);
                    if (!TextUtils.isEmpty(CardNum)) {
                        rawResult = "银行卡号: " + CardNum + "\n"
                                + "发卡行    : " + BankName + "\n"
                                + "机构代码: " + BANK_OrganizeCode + "\n"
                                + "卡种         : " + BANK_CardClass + "\n"
                                + "卡名         : " + CARD_NAME + "\n";
                        CardInfo cardInfo = new CardInfo();
                        cardInfo.setFieldString(TFieldID.TBANK_NUM, CardNum);
                        cardInfo.setFieldString(TFieldID.TBANK_NAME, BankName);
                        cardInfo.setFieldString(TFieldID.TBANK_ORGCODE, BANK_OrganizeCode);
                        cardInfo.setFieldString(TFieldID.TBANK_CLASS, BANK_CardClass);
                        cardInfo.setFieldString(TFieldID.TBANK_CARD_NAME, CARD_NAME);
                        cardInfo.setAllinfo(rawResult);
                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("content", cardInfo);
                        message.setData(bundle);
                        message.what = BankCard;
                        mHandler.sendMessage(message);
                    } else {
                        Message message = new Message();
                        message.what = ScanError;
                        mHandler.sendMessage(message);
                    }
                }
            }
        }
    }
}


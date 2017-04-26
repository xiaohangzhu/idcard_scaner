package com.newhope.idcardscan;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.idcard.CardInfo;
import com.idcard.TFieldID;
import com.idcard.TRECAPIImpl;
import com.idcard.TStatus;
import com.idcard.TengineID;
import com.turui.bank.ocr.card.TRCardScan;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class IDCardIdentifyActivity extends AppCompatActivity {

    private TextView textView_scan_idCard_front;
    private TextView textView_scan_idCard_back;
    private TextView textView_scan_idCard_all;
    private ImageView imageView_scan_idCard_front;
    private ImageView imageView_scan_idCard_back;
    private ImageView imageView_scan_idCard_all;
    private RelativeLayout relativeLayout_scan_idCard_front;
    private RelativeLayout relativeLayout_scan_idCard_back;
    private RelativeLayout relativeLayout_scan_idCard_all;
    private final int SCAN_IDCARD_FRONT = 0;//扫描身份证前面
    private final int SCAN_IDCARD_BACK = 1;//扫描身份证后面
    private final int SCAN_IDCARD_ALL = 2;//拍手持照


    private TRECAPIImpl engineDemo = new TRECAPIImpl();
    private static String catchpath;//文件储存路径


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_id_card_identify);

        textView_scan_idCard_front = (TextView) findViewById(R.id.textView_scan_idCard_front);
        textView_scan_idCard_back = (TextView) findViewById(R.id.textView_scan_idCard_back);
        textView_scan_idCard_all = (TextView) findViewById(R.id.textView_scan_idCard_all);
        imageView_scan_idCard_front = (ImageView) findViewById(R.id.imageView_scan_idCard_front);
        imageView_scan_idCard_back = (ImageView) findViewById(R.id.imageView_scan_idCard_back);
        imageView_scan_idCard_all = (ImageView) findViewById(R.id.imageView_scan_idCard_all);
        relativeLayout_scan_idCard_front = (RelativeLayout) findViewById(R.id.relativeLayout_scan_idCard_front);
        relativeLayout_scan_idCard_back = (RelativeLayout) findViewById(R.id.relativeLayout_scan_idCard_back);
        relativeLayout_scan_idCard_all = (RelativeLayout) findViewById(R.id.relativeLayout_scan_idCard_all);

        initSDK();
        catchpath = getCacheDir().getAbsolutePath();

        relativeLayout_scan_idCard_front.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toScan(SCAN_IDCARD_FRONT);
            }
        });
        relativeLayout_scan_idCard_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toScan(SCAN_IDCARD_BACK);
            }
        });
        relativeLayout_scan_idCard_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toScan(SCAN_IDCARD_ALL);
            }
        });
    }

    /**
     * 扫描身份证
     * SCAN_IDCARD_FRONT  扫描前面
     * SCAN_IDCARD_BACK   扫描后面
     * SCAN_IDCARD_ALL  拍手持 照片
     */
    private void toScan(int scanWhich) {
        boolean b = sdcardExist();
        if (!b) {
            Toast.makeText(IDCardIdentifyActivity.this, "未插入内存卡", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent;
        switch (scanWhich) {
            case SCAN_IDCARD_FRONT:
                TRCardScan.isOpenProgress = true;
                intent = new Intent(IDCardIdentifyActivity.this, TRCardScan.class);
                intent.putExtra("engine", engineDemo);
                intent.putExtra(TRCardScan.tag, TRCardScan.ONFONT);
                startActivityForResult(intent, SCAN_IDCARD_FRONT);
                break;
            case SCAN_IDCARD_BACK:
                TRCardScan.isOpenProgress = true;
                intent = new Intent(IDCardIdentifyActivity.this, TRCardScan.class);
                intent.putExtra("engine", engineDemo);
                intent.putExtra(TRCardScan.tag, TRCardScan.ONBACK);
                startActivityForResult(intent, SCAN_IDCARD_BACK);
                break;
            case SCAN_IDCARD_ALL:
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, SCAN_IDCARD_ALL);
                break;
            default:
                break;
        }
    }

    /**
     * 判断SD卡是否存在
     */
    protected boolean sdcardExist() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 初始化sdk
     */
    private void initSDK() {
        TRCardScan.SetEngineType(TengineID.TIDCARD2);
        TStatus tStatus = engineDemo.TR_StartUP();
        if (tStatus == TStatus.TR_TIME_OUT) {
            Toast.makeText(getBaseContext(), "引擎过期", Toast.LENGTH_SHORT).show();
        } else if (tStatus == TStatus.TR_FAIL) {
            Toast.makeText(getBaseContext(), "引擎初始化失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 扫描拍摄完返回回掉
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SCAN_IDCARD_FRONT:
                    Bitmap takeFront = TRCardScan.TakeBitmap;  // 正面
                    if (takeFront == null) {
                        Toast.makeText(IDCardIdentifyActivity.this, "扫描失败,请重新扫描身份证", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    CardInfo cardInfo = (CardInfo) data.getSerializableExtra("cardinfo");
                    if (TextUtils.isEmpty(cardInfo.getFieldString(TFieldID.NAME)) || TextUtils.isEmpty(cardInfo.getFieldString(TFieldID.ADDRESS)) || TextUtils.isEmpty(cardInfo.getFieldString(TFieldID.NUM))) {
                        Toast.makeText(IDCardIdentifyActivity.this, "扫描失败，请保证身份证清晰", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(IDCardIdentifyActivity.this, "姓名：" + cardInfo.getFieldString(TFieldID.NAME) + "\n"
                            + "身份证号：" + cardInfo.getFieldString(TFieldID.NUM) + "\n"
                            + "家庭住址：" + cardInfo.getFieldString(TFieldID.ADDRESS), Toast.LENGTH_SHORT).show();
                //压缩 储存 读取 BitMap
                int bitmap_width = 600;
                int bitmap_height = (bitmap_width * takeFront.getHeight()) / takeFront.getWidth();
                Bitmap zoomBit = zoomBitmap(takeFront, bitmap_width, bitmap_height);
                saveBitmap("idCardFront", zoomBit);
                imageView_scan_idCard_front.setImageBitmap(getBitMap(catchpath + "/" + "idCardFront"));
                break;
            case SCAN_IDCARD_BACK:
                Bitmap takeBack = TRCardScan.TakeBitmap;  // 反面
                if (takeBack == null) {
                    Toast.makeText(IDCardIdentifyActivity.this, "扫描失败,请重新扫描身份证", Toast.LENGTH_SHORT).show();
                    return;
                }
                CardInfo cardInfo2 = (CardInfo) data.getSerializableExtra("cardinfo");
                if (TextUtils.isEmpty(cardInfo2.getFieldString(TFieldID.PERIOD)) || !verityPeriod(cardInfo2.getFieldString(TFieldID.PERIOD))) {
                    Toast.makeText(IDCardIdentifyActivity.this, "扫描失败，请保证身份证清晰", Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(IDCardIdentifyActivity.this, "签证机关：" + cardInfo2.getFieldString(TFieldID.ISSUE) + "\n"
                        + "有效期：" + cardInfo2.getFieldString(TFieldID.PERIOD), Toast.LENGTH_LONG).show();
                //压缩 储存 读取 BitMap
                int bitmap_width2 = 600;
                int bitmap_height2 = (bitmap_width2 * takeBack.getHeight()) / takeBack.getWidth();
                Bitmap zoomBit2 = zoomBitmap(takeBack, bitmap_width2, bitmap_height2);
                saveBitmap("idCardBack", zoomBit2);
                imageView_scan_idCard_back.setImageBitmap(getBitMap(catchpath + "/" + "idCardBack"));
                break;
            case SCAN_IDCARD_ALL:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    Bitmap bitmap = (Bitmap) bundle.get("data");
                    //压缩 储存 读取 BitMap
                    int bitmap_width3 = 600;
                    int bitmap_height3 = (bitmap_width3 * bitmap.getHeight()) / bitmap.getWidth();
                    Bitmap zoomBit3 = zoomBitmap(bitmap, bitmap_width3, bitmap_height3);
                    saveBitmap("idCardAll", zoomBit3);
                    imageView_scan_idCard_all.setImageBitmap(getBitMap(catchpath + "/" + "idCardAll"));
                }
                break;
            default:
                break;
        }
    }


    /**
     * 校验身份证有效期
     */
    private boolean verityPeriod(String period) {
        String[] split = period.split("[-]+");
        try {
            if (split.length == 2) {
                String start = split[0].replace(".", "-");
                String end = split[1].replace(".", "-");
                if (!TextUtils.isEmpty(start) && !TextUtils.isEmpty(end)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 保存文件
     */
    public static void saveBitmap(String bitName, Bitmap mBitmap) {
        File f = new File(catchpath, bitName);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            mBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据URL获取文件
     *
     * @return
     */
    public static File getFile(final String url, Context context) {
        File file = new File(url);
        if (file.exists()) {
            return file;
        }
        return null;
    }

    /**
     * 文件转bitmap
     *
     * @return
     */
    private Bitmap getBitMap(String path) {
        return BitmapFactory.decodeFile(path, getBitmapOption(2));
    }

    private BitmapFactory.Options getBitmapOption(int inSampleSize) {
        System.gc();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
        options.inSampleSize = inSampleSize;
        return options;
    }

    /**
     * BitMap压缩
     */
    public static Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {

        int w = bitmap.getWidth();

        int h = bitmap.getHeight();

        Matrix matrix = new Matrix();

        float scaleWidth = ((float) width / w);

        float scaleHeight = ((float) height / h);

        matrix.postScale(scaleWidth, scaleHeight);// 利用矩阵进行缩放不会造成内存溢出

        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);

        if (bitmap != newbmp) {
            bitmap.recycle();
        }
        return newbmp;

    }
}

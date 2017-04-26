package com.newhope.idcardscan;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.idcard.CardInfo;
import com.idcard.TFieldID;
import com.idcard.TRECAPIImpl;
import com.idcard.TStatus;
import com.idcard.TengineID;
import com.turui.bank.ocr.CaptureActivity;
import com.turui.bank.ocr.card.TRCardScan;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class BankCardIdentifyActivity extends AppCompatActivity {
    private TRECAPIImpl engineDemo = new TRECAPIImpl();
    private final int BANK_CARD_SCAN = 1;
    private static String catchpath;//文件储存路径
    private ImageView imageView_scan_bankCard_nb;
    private ImageView imageView_scan_bankCard_all;
    private TextView textView_scan_bankCard_content;
    private Button button_bankCardIdentify;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_card_identify);
        initSDK();
        catchpath = getCacheDir().getAbsolutePath();

        imageView_scan_bankCard_nb = (ImageView) findViewById(R.id.imageView_scan_bankCard_nb);
        imageView_scan_bankCard_all = (ImageView) findViewById(R.id.imageView_scan_bankCard_all);
        textView_scan_bankCard_content = (TextView) findViewById(R.id.textView_scan_bankCard_content);
        button_bankCardIdentify = (Button) findViewById(R.id.button_bankCardIdentify);
        button_bankCardIdentify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBankingScan();
            }
        });

    }

    /**
     * 初始化银行卡扫描
     */
    private void initSDK() {
        TRCardScan.SetEngineType(TengineID.TIDBANK);
        TStatus tStatus = engineDemo.TR_StartUP();
        if (tStatus == TStatus.TR_TIME_OUT) {
            Toast.makeText(getBaseContext(), "引擎过期", Toast.LENGTH_SHORT).show();
        } else if (tStatus == TStatus.TR_FAIL) {
            Toast.makeText(getBaseContext(), "引擎初始化失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 打开打开银行卡扫描
     */
    private void onBankingScan() {
        Intent intent = new Intent(this, com.turui.bank.ocr.CaptureActivity.class);
        intent.putExtra("engine", engineDemo);
        startActivityForResult(intent, BANK_CARD_SCAN);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (requestCode == BANK_CARD_SCAN) {
                // 处理银行卡扫描结果（在界面上显示）
                Bundle bundle = data.getExtras();
                String result = bundle.getString("result");
                Bitmap numberBitmap = CaptureActivity.SmallBitmap;//号码截图
                Bitmap allBitmap = CaptureActivity.TakeBitmap;// 全图
                //压缩 储存 读取 BitMap
                int bitmap_width = 600;
                int bitmap_height = (bitmap_width * numberBitmap.getHeight()) / numberBitmap.getWidth();
                Bitmap zoomBit = zoomBitmap(numberBitmap, bitmap_width, bitmap_height);
                saveBitmap("bankCardNB", zoomBit);
                int bitmap_height2 = (bitmap_width * allBitmap.getHeight()) / allBitmap.getWidth();
                Bitmap zoomBit2 = zoomBitmap(allBitmap, bitmap_width, bitmap_height2);
                saveBitmap("bankCardAll", zoomBit2);
                imageView_scan_bankCard_nb.setImageBitmap(getBitMap(catchpath + "/" + "bankCardNB"));
                imageView_scan_bankCard_all.setImageBitmap(getBitMap(catchpath + "/" + "bankCardAll"));
//                imageView_scan_idCard_front.setImageBitmap(getBitMap(catchpath + "/" + "idCardFront"));


                CardInfo cardInfo = (CardInfo) data.getSerializableExtra("cardinfo");
                String cardNum = cardInfo.getFieldString(TFieldID.TBANK_NUM);
                textView_scan_bankCard_content.setText(cardNum);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

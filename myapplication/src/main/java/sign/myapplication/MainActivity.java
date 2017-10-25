package sign.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.idcard.CardInfo;
import com.idcard.TFieldID;
import com.sdkInterface.CardScaner;
import com.turui.bank.ocr.CaptureActivity;
import com.turui.bank.ocr.card.FormatTools;
import com.turui.bank.ocr.card.TRCardScan;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private CardScaner scaner;
    private String picPath;//照片储存路径
    private String fileName;//照片名
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.imageView);
        scaner = new CardScaner(this);

        scaner.GetDataListener(new CardScaner.DataListener() {
            @Override
            public void data(int i, Object object) {
                switch (i) {
                    case CardScaner.IDCardFront:
                        CardInfo cardInfoFront;
                        cardInfoFront = (CardInfo) object;
                        String contentFront = cardInfoFront.getFieldString(TFieldID.NAME) +
                                cardInfoFront.getFieldString(TFieldID.SEX) +
                                cardInfoFront.getFieldString(TFieldID.FOLK) +
                                cardInfoFront.getFieldString(TFieldID.BIRTHDAY) +
                                cardInfoFront.getFieldString(TFieldID.ADDRESS) +
                                cardInfoFront.getFieldString(TFieldID.NUM);
                        Toast.makeText(MainActivity.this, contentFront, Toast.LENGTH_LONG).show();
                        Log.i("zxh", "dataFront:" + contentFront);
                        break;
                    case CardScaner.IDCardBack:
                        CardInfo cardInfoBack;
                        cardInfoBack = (CardInfo) object;
                        String contentBack =
                                cardInfoBack.getFieldString(TFieldID.ISSUE) +
                                        cardInfoBack.getFieldString(TFieldID.PERIOD);
                        Toast.makeText(MainActivity.this, contentBack, Toast.LENGTH_LONG).show();
                        Log.i("zxh", "dataBack:" + contentBack);
                        break;
                    case CardScaner.BankCard:
                        CardInfo bankCardInfo;
                        bankCardInfo = (CardInfo) object;
                        String contentBank =
                                bankCardInfo.getFieldString(TFieldID.TBANK_NUM) +
                                        bankCardInfo.getFieldString(TFieldID.TBANK_CARD_NAME) +
                                        bankCardInfo.getFieldString(TFieldID.TBANK_CLASS) +
                                        bankCardInfo.getFieldString(TFieldID.TBANK_ORGCODE) +
                                        bankCardInfo.getFieldString(TFieldID.TBANK_NAME) +
                                        bankCardInfo.getFieldString(TFieldID.TBANK_NUM_REGION);
                        Toast.makeText(MainActivity.this, contentBank, Toast.LENGTH_LONG).show();
                        Log.i("zxh", "dataBack:" + contentBank);
                        break;
                }
            }

            @Override
            public void error() {
                Toast.makeText(MainActivity.this, "识别信息不完整，请重新选择图片", Toast.LENGTH_LONG).show();
                Log.i("zxh", "error");
            }

            @Override
            public void start() {
                Log.i("zxh", "start");
            }

            @Override
            public void end() {
                Log.i("zxh", "end");
            }
        });

        scaner.getIDCardFrontData(FormatTools.getInstance().drawable2Bitmap(getResources().getDrawable(R.drawable.test3)));
//        scaner.getIDCardBackData(FormatTools.getInstance().drawable2Bitmap(getResources().getDrawable(R.drawable.test7)));
//        scaner.getBankCardData(FormatTools.getInstance().drawable2Bitmap(getResources().getDrawable(R.drawable.test)));
    }

    public void startActivity(View view) {
        startCamera();
    }

    /**
     * 开启相机
     */
    private void startCamera() {
        fileName = System.currentTimeMillis() + ".png";
        picPath = Environment.getExternalStorageDirectory().getPath() + "/WWXH" + "/" + fileName;
        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/WWXH");
        if (!file.exists() && !file.isDirectory())
            file.mkdir();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri uri = Uri.fromFile(new File(picPath));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, 5867);
    }

    public void startActivity2(View view) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 5867:
                File file = new File(picPath);
                Bitmap zoombitmapBitmap = Util.zoombitmapBitmap(
                        file.getAbsolutePath(), 800*800);
                FileImageUtils.saveCatchBitmap(StringUtils.md5(file.getName()), zoombitmapBitmap, MainActivity.this, Environment.getExternalStorageDirectory().getPath() + "/WWXH");
                File newFile = FileImageUtils.getFile(Environment.getExternalStorageDirectory().getPath() + "/WWXH" + "/" + StringUtils.md5(file.getName()), MainActivity.this);
                imageView.setImageBitmap(BitmapFactory.decodeFile(newFile.getPath()));
                scaner.getIDCardFrontData(BitmapFactory.decodeFile(newFile.getPath()));
                Log.i("zxh", "5867");

                break;
            case CardScaner.ScanIDCardFront:
                Log.i("zxh", "1");
                Bitmap takeFront = TRCardScan.TakeBitmap;  // 正面
                if (takeFront == null) {
                    Toast.makeText(MainActivity.this, "扫描失败,请重新扫描身份证", Toast.LENGTH_SHORT).show();
                    return;
                }
                CardInfo cardInfo = (CardInfo) data.getSerializableExtra("cardinfo");
                if (TextUtils.isEmpty(cardInfo.getFieldString(TFieldID.NAME)) || TextUtils.isEmpty(cardInfo.getFieldString(TFieldID.ADDRESS)) || TextUtils.isEmpty(cardInfo.getFieldString(TFieldID.NUM))) {
                    Toast.makeText(MainActivity.this, "扫描失败，请保证身份证清晰", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(MainActivity.this, "姓名：" + cardInfo.getFieldString(TFieldID.NAME) + "\n"
                        + "身份证号：" + cardInfo.getFieldString(TFieldID.NUM) + "\n"
                        + "家庭住址：" + cardInfo.getFieldString(TFieldID.ADDRESS), Toast.LENGTH_SHORT).show();
                break;
            case CardScaner.ScanBankCard:
                Log.i("zxh", "2");
                Bundle bundle = data.getExtras();
                String result = bundle.getString("result");
                Bitmap numberBitmap = CaptureActivity.SmallBitmap;//号码截图
                Bitmap allBitmap = CaptureActivity.TakeBitmap;// 全图
                //压缩 储存 读取 BitMap
                CardInfo cardInfo2 = (CardInfo) data.getSerializableExtra("cardinfo");
                String cardNum = cardInfo2.getFieldString(TFieldID.TBANK_NUM);
                Toast.makeText(MainActivity.this, "签证机关：" + cardInfo2.getFieldString(TFieldID.TBANK_NUM) + "\n"
                        + "签证机关：" + cardInfo2.getFieldString(TFieldID.TBANK_CARD_NAME) + "\n"
                        + "签证机关：" + cardInfo2.getFieldString(TFieldID.TBANK_CLASS) + "\n"
                        + "签证机关：" + cardInfo2.getFieldString(TFieldID.TBANK_NAME) + "\n"
                        + "签证机关：" + cardInfo2.getFieldString(TFieldID.TBANK_NUM_CHECKSTATUS) + "\n"
                        + "签证机关：" + cardInfo2.getFieldString(TFieldID.TBANK_NUM_REGION) + "\n"
                        + "有效期：" + cardInfo2.getFieldString(TFieldID.TBANK_ORGCODE), Toast.LENGTH_LONG).show();
                break;
            case CardScaner.ScanIDCardBack:
                Log.i("zxh", "3");
                Bitmap takeBack = TRCardScan.TakeBitmap;  // 反面
                if (takeBack == null) {
                    Toast.makeText(MainActivity.this, "扫描失败,请重新扫描身份证", Toast.LENGTH_SHORT).show();
                    return;
                }
                CardInfo cardInfo3 = (CardInfo) data.getSerializableExtra("cardinfo");
                if (TextUtils.isEmpty(cardInfo3.getFieldString(TFieldID.PERIOD))) {
                    Toast.makeText(MainActivity.this, "扫描失败，请保证身份证清晰", Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(MainActivity.this, "签证机关：" + cardInfo3.getFieldString(TFieldID.ISSUE) + "\n"
                        + "有效期：" + cardInfo3.getFieldString(TFieldID.PERIOD), Toast.LENGTH_LONG).show();
                break;
        }
    }

}

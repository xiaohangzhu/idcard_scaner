#配置

##使用cardScan.aar
###1.将文件cardScan.aar放入项目lib文件夹目录下
###2.Gradle配置 引入文件

 ` android {
repositories {
    flatDir {
        dirs 'libs'
    }
}
}
dependencies {
        compile(name: 'cardScan_1.0', ext: 'aar')
} ` 

##使用cardScan.jar
###1.将文件cardScan.jar放入项目lib文件夹目录下
###2.	配置manifest
###2.配置manifest
权限
 `<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.VIBRATE"/> `
    
 Activity
  ` <activity
            android:name="com.turui.bank.ocr.card.TRCardScan"
            android:screenOrientation="landscape">
        </activity>
        <activity
            android:name="com.turui.bank.ocr.CaptureActivity"
            android:configChanges="orientation|keyboardHidden"
            android:screenOrientation="landscape"
            android:theme="@android:style/Theme.NoTitleBar.Fullscreen"
            android:windowSoftInputMode="stateAlwaysHidden">`
        </activity>
###3.Gradle配置 引入文件
   `compile files('libs/classes.jar')` 
###4.添加资源文件
将文件中的资源copy至项目中


#使用方法
##通过BitMap对象获取数据
###1.声明CardScaner对象
 ` private CardScaner scaner; ` 
###2.初始化
 ` scaner = new CardScaner(this); ` 
###3.设置监听（仅针对于通过Bitmap获取信息的方式）（银行卡信息不推荐使用这种方式，因为银行卡识别是否成功不仅取决于图片清晰度，还有其他因素，具体不明）
 ` scaner.GetDataListener(new CardScaner.DataListener() {
    @Override
    public void data(int i, Object o) {
	//识别成功
        switch (i) {
            case CardScaner.IDCardFront:
                CardInfo cardInfoFront;
                cardInfoFront = (CardInfo) o;
                String contentFront = cardInfoFront.getFieldString(TFieldID.NAME) +
                        cardInfoFront.getFieldString(TFieldID.SEX) +
                        cardInfoFront.getFieldString(TFieldID.FOLK) +
                        cardInfoFront.getFieldString(TFieldID.BIRTHDAY) +
                        cardInfoFront.getFieldString(TFieldID.ADDRESS) +
                        cardInfoFront.getFieldString(TFieldID.NUM);
                Toast.makeText(MainActivity.this, contentFront, Toast.LENGTH_LONG).show();
                    break;
            case CardScaner.IDCardBack:
                CardInfo cardInfoBack;
                cardInfoBack = (CardInfo) o;
                String contentBack =
                        cardInfoBack.getFieldString(TFieldID.ISSUE) +
                                cardInfoBack.getFieldString(TFieldID.PERIOD);
                Toast.makeText(MainActivity.this, contentBack, Toast.LENGTH_LONG).show();
                    break;
            case CardScaner.BankCard:
                CardInfo bankCardInfo;
                bankCardInfo = (CardInfo) o;
                String contentBank=
                        bankCardInfo.getFieldString(TFieldID.TBANK_NUM) +
                                bankCardInfo.getFieldString(TFieldID.TBANK_CARD_NAME)+
                                bankCardInfo.getFieldString(TFieldID.TBANK_CLASS)+
                                bankCardInfo.getFieldString(TFieldID.TBANK_ORGCODE)+
                                bankCardInfo.getFieldString(TFieldID.TBANK_NAME)+
                                bankCardInfo.getFieldString(TFieldID.TBANK_NUM_REGION)
                        ;
                Toast.makeText(MainActivity.this, contentBank, Toast.LENGTH_LONG).show();
                  break;
        }
    }
@Override
public void error() {
       //识别错误
} ` 

 ` @Override
public void start() {
      //开始识别
} ` 

 ` @Override
public void end() {
      //识别结束
}  ` 

###4.获取数据
 ` scaner.getIDCardFrontData(bitmap);//获取身份证前的信息 ` 
 ` scaner.getIDCardBackData(bitmap);//获取身份证后的信息 ` 
 ` scaner.getBankCardData(bitmap);//获取银行卡信息（不推荐） ` 

##通过扫描方式获取数据
###1.声明CardScaner对象
 ` private CardScaner scaner; ` 
###2.初始化
 ` scaner = new CardScaner(this); ` 
###3.启动扫描
 ` scaner.scanBankCard(this); //扫描银行卡信息 ` 
 ` scaner.scanIDCardBack(this); //扫描身份证后信息 ` 
 ` scaner.scanIDCardFront(this);//扫描身份证前信息 ` 
###4.设置onActivityResult获取数据

 ` @Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
        case CardScaner.ScanIDCardFront:
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
            Bundle bundle = data.getExtras();
            String result = bundle.getString("result");
            Bitmap numberBitmap = CaptureActivity.SmallBitmap;//号码截图
            Bitmap allBitmap = CaptureActivity.TakeBitmap;// 全图
            //压缩 储存 读取 BitMap
            CardInfo cardInfo2 = (CardInfo) data.getSerializableExtra("cardinfo");
            String cardNum = cardInfo2.getFieldString(TFieldID.TBANK_NUM);
            Toast.makeText(MainActivity.this, "银行卡卡号：" + cardInfo2.getFieldString(TFieldID.TBANK_NUM) + "\n"
                    +"：" + cardInfo2.getFieldString(TFieldID.TBANK_CARD_NAME) + "\n"
                    +"：" + cardInfo2.getFieldString(TFieldID.TBANK_CLASS) + "\n"
                    +"：" + cardInfo2.getFieldString(TFieldID.TBANK_NAME) + "\n"
                    +"：" + cardInfo2.getFieldString(TFieldID.TBANK_NUM_CHECKSTATUS) + "\n"
                    +"：" + cardInfo2.getFieldString(TFieldID.TBANK_NUM_REGION) + "\n"
                    + "：" + cardInfo2.getFieldString(TFieldID.TBANK_ORGCODE), Toast.LENGTH_LONG).show();
            break;
        case CardScaner.ScanIDCardBack:
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
} ` 



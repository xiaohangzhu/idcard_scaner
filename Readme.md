һ����
1.���ļ�cardScan.aar������Ŀlib�ļ���Ŀ¼��
2.Gradle���� �����ļ�

android {
repositories {
    flatDir {
        dirs 'libs'
    }
}
dependencies {
        compile(name: 'cardScan_1.0', ext: 'aar')
}
��ʹ�÷���
ͨ��BitMap�����ȡ����
1.����CardScaner����
private CardScaner scaner;
2.��ʼ��
scaner = new CardScaner(this);
3.���ü������������ͨ��Bitmap��ȡ��Ϣ�ķ�ʽ�������п���Ϣ���Ƽ�ʹ�����ַ�ʽ����Ϊ���п�ʶ���Ƿ�ɹ�����ȡ����ͼƬ�����ȣ������������أ����岻����
scaner.GetDataListener(new CardScaner.DataListener() {
    @Override
    public void data(int i, Object o) {
	//ʶ��ɹ�
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
       //ʶ�����
}

@Override
public void start() {
      //��ʼʶ��
}

@Override
public void end() {
      //ʶ�����
}

4.��ȡ����
scaner.getIDCardFrontData(bitmap);//��ȡ���֤ǰ����Ϣ
scaner.getIDCardBackData(bitmap);//��ȡ���֤�����Ϣ
scaner.getBankCardData(bitmap);//��ȡ���п���Ϣ�����Ƽ���

ͨ��ɨ�跽ʽ��ȡ����
1.����CardScaner����
private CardScaner scaner;
2.��ʼ��
scaner = new CardScaner(this);
3.����ɨ��
scaner.scanBankCard(this); //ɨ�����п���Ϣ
scaner.scanIDCardBack(this); //ɨ�����֤����Ϣ
scaner.scanIDCardFront(this);//ɨ�����֤ǰ��Ϣ
4.����onActivityResult��ȡ����

@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
        case CardScaner.ScanIDCardFront:
          
            Bitmap takeFront = TRCardScan.TakeBitmap;  // ����
            if (takeFront == null) {
                Toast.makeText(MainActivity.this, "ɨ��ʧ��,������ɨ�����֤", Toast.LENGTH_SHORT).show();
                return;
            }
            CardInfo cardInfo = (CardInfo) data.getSerializableExtra("cardinfo");
            if (TextUtils.isEmpty(cardInfo.getFieldString(TFieldID.NAME)) || TextUtils.isEmpty(cardInfo.getFieldString(TFieldID.ADDRESS)) || TextUtils.isEmpty(cardInfo.getFieldString(TFieldID.NUM))) {
                Toast.makeText(MainActivity.this, "ɨ��ʧ�ܣ��뱣֤���֤����", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(MainActivity.this, "������" + cardInfo.getFieldString(TFieldID.NAME) + "\n"
                    + "���֤�ţ�" + cardInfo.getFieldString(TFieldID.NUM) + "\n"
                    + "��ͥסַ��" + cardInfo.getFieldString(TFieldID.ADDRESS), Toast.LENGTH_SHORT).show();
            break;
        case CardScaner.ScanBankCard:
         
            Bundle bundle = data.getExtras();
            String result = bundle.getString("result");
            Bitmap numberBitmap = CaptureActivity.SmallBitmap;//�����ͼ
            Bitmap allBitmap = CaptureActivity.TakeBitmap;// ȫͼ
            //ѹ�� ���� ��ȡ BitMap
            CardInfo cardInfo2 = (CardInfo) data.getSerializableExtra("cardinfo");
            String cardNum = cardInfo2.getFieldString(TFieldID.TBANK_NUM);
            Toast.makeText(MainActivity.this, "���п����ţ�" + cardInfo2.getFieldString(TFieldID.TBANK_NUM) + "\n"
                    +"��" + cardInfo2.getFieldString(TFieldID.TBANK_CARD_NAME) + "\n"
                    +"��" + cardInfo2.getFieldString(TFieldID.TBANK_CLASS) + "\n"
                    +"��" + cardInfo2.getFieldString(TFieldID.TBANK_NAME) + "\n"
                    +"��" + cardInfo2.getFieldString(TFieldID.TBANK_NUM_CHECKSTATUS) + "\n"
                    +"��" + cardInfo2.getFieldString(TFieldID.TBANK_NUM_REGION) + "\n"
                    + "��" + cardInfo2.getFieldString(TFieldID.TBANK_ORGCODE), Toast.LENGTH_LONG).show();
            break;
        case CardScaner.ScanIDCardBack:
        
            Bitmap takeBack = TRCardScan.TakeBitmap;  // ����
            if (takeBack == null) {
                Toast.makeText(MainActivity.this, "ɨ��ʧ��,������ɨ�����֤", Toast.LENGTH_SHORT).show();
                return;
            }
            CardInfo cardInfo3 = (CardInfo) data.getSerializableExtra("cardinfo");
            if (TextUtils.isEmpty(cardInfo3.getFieldString(TFieldID.PERIOD))) {
                Toast.makeText(MainActivity.this, "ɨ��ʧ�ܣ��뱣֤���֤����", Toast.LENGTH_LONG).show();
                return;
            }
            Toast.makeText(MainActivity.this, "ǩ֤���أ�" + cardInfo3.getFieldString(TFieldID.ISSUE) + "\n"
                    + "��Ч�ڣ�" + cardInfo3.getFieldString(TFieldID.PERIOD), Toast.LENGTH_LONG).show();
            break;
    }
}



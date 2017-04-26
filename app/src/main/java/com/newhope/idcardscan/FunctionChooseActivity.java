package com.newhope.idcardscan;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.sdkInterface.CardScaner;

public class FunctionChooseActivity extends AppCompatActivity {

    private ImageView img;
    private CardScaner scaner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_function_choose);
        img = (ImageView) findViewById(R.id.img);
        scaner = new CardScaner(this);
    }

    public void click_button_idCardIdentify(View view) {
        startActivity(new Intent(FunctionChooseActivity.this, IDCardIdentifyActivity.class));
    }

    public void click_button_bankCardIdentify(View view) {
        startActivity(new Intent(FunctionChooseActivity.this, BankCardIdentifyActivity.class));
    }
}

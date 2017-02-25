package com.fussroll.fussroll;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textView = (TextView) findViewById(R.id.privacyPolicyMessage);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        AppCompatButton appCompatButton = (AppCompatButton) findViewById(R.id.button);
        appCompatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });
        //Documented - 2
        //Bypass registration and verification process if already done
        SharedPreferences sharedPreferences = getSharedPreferences("request", Context.MODE_PRIVATE);
        if(sharedPreferences.getString("registered","").equals("true") && sharedPreferences.getString("confirmed","").equals("true")) {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra(HomeActivity.mobile, sharedPreferences.getString("mobile",""));
            String uid;
            try {
                uid = AESEncryption.decrypt(sharedPreferences.getString("uid",""));
            } catch (Exception e) {
                uid = "";
                e.printStackTrace();
            }
            intent.putExtra(HomeActivity.uid, uid);
            startActivity(intent);
            this.finish();
        }
        else if((sharedPreferences.getString("registered","").equals("true"))) {
            Intent intent = new Intent(this, VerifyActivity.class);
            intent.putExtra(VerifyActivity.mobileToVerify, sharedPreferences.getString("mobile",""));
            String uid;
            try {
                uid = AESEncryption.decrypt(sharedPreferences.getString("uid",""));
            } catch (Exception e) {
                uid = "";
                e.printStackTrace();
            }
            intent.putExtra(VerifyActivity.uidToVerify, uid);
            startActivity(intent);
            this.finish();
        }

    }

    public void register() {
        startActivity(new Intent(this, RegisterActivity.class));
    }
}

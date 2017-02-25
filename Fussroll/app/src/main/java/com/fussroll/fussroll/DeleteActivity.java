package com.fussroll.fussroll;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

public class DeleteActivity extends AppCompatActivity {

    private EditText editText;
    TextInputLayout textInputLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        editText = (EditText) findViewById(R.id.editText);
        textInputLayout = (TextInputLayout) findViewById(R.id.textInputLayout);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        AppCompatButton appCompatButton = (AppCompatButton) findViewById(R.id.button);
        appCompatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void delete() {

        String phoneNumber = editText.getText().toString();
        phoneNumber = phoneNumber.trim();
        SharedPreferences sharedPreferences = getSharedPreferences("request", MODE_PRIVATE);
        String userMobileNumber = sharedPreferences.getString("mobile", "");

        if(phoneNumber.equals("0000000000")) {
            textInputLayout.setError(getResources().getString(R.string.requireValid));
        } else if(phoneNumber.length() == 10 && phoneNumber.matches(getResources().getString(R.string.regexDigits)) && userMobileNumber.substring(userMobileNumber.length() - 10).equals(phoneNumber)) {
            textInputLayout.setErrorEnabled(false);
            startActivity(new Intent(this, FinalDeleteActivity.class));
        }else {
            textInputLayout.setError(getString(R.string.enterYourPhoneNumber));
        }

    }
}

package com.fussroll.fussroll;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class VerifyActivity extends AppCompatActivity {

    public static final String mobileToVerify = "mobileToVerify";
    public static final String uidToVerify = "uidToVerify";

    private EditText editText1;
    private EditText editText2;
    private TextView textView4;
    private TextView textView3;
    private Activity activity = this;
    private int seconds = 60;
    String mobileFinal = "";
    String uidFinal = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        textView4 = (TextView) findViewById(R.id.textView4);
        textView3 = (TextView) findViewById(R.id.textView3);
        editText1 = (EditText) findViewById(R.id.editText1);
        editText2 = (EditText) findViewById(R.id.editText2);
        mobileFinal = getIntent().getStringExtra(mobileToVerify);
        uidFinal = getIntent().getStringExtra(uidToVerify);

        AppCompatButton appCompatButton = (AppCompatButton) findViewById(R.id.button);
        appCompatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verify();
            }
        });

        //Documented - 1
        editText1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() == 3)
                    editText2.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Nothing
            }
        });

        if(savedInstanceState != null)
            seconds = savedInstanceState.getInt("seconds");

        //Documented - 2
        runTimer();

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("seconds", seconds);
    }

    //Documented - 3
    public void runTimer() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(seconds != 0) {
                    if(seconds == 9) {
                        textView4.setText(getResources().getString(R.string.nine));
                        seconds--;
                    }
                    else if(seconds == 8) {
                        textView4.setText(getResources().getString(R.string.eight));
                        seconds--;
                    }
                    else if(seconds == 7) {
                        textView4.setText(getResources().getString(R.string.seven));
                        seconds--;
                    }
                    else if(seconds == 6) {
                        textView4.setText(getResources().getString(R.string.six));
                        seconds--;
                    }
                    else if(seconds == 5) {
                        textView4.setText(getResources().getString(R.string.five));
                        seconds--;
                    }
                    else if(seconds == 4) {
                        textView4.setText(getResources().getString(R.string.four));
                        seconds--;
                    }
                    else if(seconds == 3) {
                        textView4.setText(getResources().getString(R.string.three));
                        seconds--;
                    }
                    else if(seconds == 2) {
                        textView4.setText(getResources().getString(R.string.two));
                        seconds--;
                    }
                    else if(seconds == 1) {
                        textView4.setText(getResources().getString(R.string.one));
                        seconds--;
                    }
                    else {
                        String countDown = getResources().getString(R.string.zeroColon)+seconds--;
                        textView4.setText(countDown);
                    }
                }
                if(seconds == 0) {
                    textView4.setText(getResources().getString(R.string.zero));
                    textView3.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimary));
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    public void resendOTP(View view) {
        if(seconds == 0) {
            textView3.setTextColor(ContextCompat.getColor(activity, android.R.color.darker_gray));
            seconds = 60;

            final AlertDialog.Builder feedback = new AlertDialog.Builder(this);

            //Resend the OTP
            StringRequest stringRequest = new StringRequest(Request.Method.POST,
                    getResources().getString(R.string.registerAPI),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if(jsonObject.getString("statusCode").equals("201")) {
                                    //Documented - 4
                                    //Store the uid and mobile for making further requests
                                    SharedPreferences sharedPreferences = getSharedPreferences("request", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("mobile", mobileFinal);
                                    editor.putString("uid", AESEncryption.encrypt(jsonObject.getString("uid")));
                                    editor.apply();
                                }
                                else if(jsonObject.getString("statusCode").equals("400")) {
                                    //Toast.makeText(activity, getResources().getString(R.string.badRequest), Toast.LENGTH_LONG).show();
                                    feedback.setMessage(getResources().getString(R.string.badRequest));
                                    feedback.setPositiveButton(R.string.ok, null);
                                    feedback.show();
                                }
                                else if(jsonObject.getString("statusCode").equals("500")) {
                                    //Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                                    feedback.setMessage(getResources().getString(R.string.serverError));
                                    feedback.setPositiveButton(R.string.ok, null);
                                    feedback.show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if(error instanceof TimeoutError) {
                                //Toast.makeText(activity, getResources().getString(R.string.serversDown), Toast.LENGTH_LONG).show();
                                feedback.setMessage(getResources().getString(R.string.serversDown));
                                feedback.setPositiveButton(R.string.ok, null);
                                feedback.show();
                            }
                            else {
                                //Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                                feedback.setMessage(getResources().getString(R.string.serverError));
                                feedback.setPositiveButton(R.string.ok, null);
                                feedback.show();
                            }
                        }
                    })
            {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("mobile", mobileFinal);
                    return params;
                }
            };
            MySingleton.getInstance(this).addToRequestQueue(stringRequest);
        }
    }

    public void verify() {

        final SharedPreferences sharedPreferences = getSharedPreferences("request", Context.MODE_PRIVATE);
        String a = editText1.getText().toString();
        String b = editText2.getText().toString();
        final String otp = a+"-"+b;
        //Resend feature might have changed the OTP
        try {
            uidFinal = AESEncryption.decrypt(sharedPreferences.getString("uid",""));
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        if(a.length() == 3 && a.matches(getResources().getString(R.string.regexDigits)) && b.length() == 3 && b.matches(getResources().getString(R.string.regexDigits))) {

            //Hide keyboard
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(findViewById(android.R.id.content).getWindowToken(), 0);

            final ProgressDialog progress = new ProgressDialog(this);
            progress.setMessage(getResources().getString(R.string.verifying));
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            progress.show();

            final AlertDialog.Builder feedback = new AlertDialog.Builder(this);

            //Send the OTP and mobile for verification
            StringRequest stringRequest = new StringRequest(Request.Method.POST,
                    getResources().getString(R.string.verifyOTPAPI),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progress.cancel();
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if(jsonObject.getString("statusCode").equals("201")) {
                                    //Documented - 5
                                    //Store the uid and mobile for making further requests
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("confirmed", "true");
                                    editor.putString("notification", "0");
                                    editor.apply();
                                    Intent intent = new Intent(activity, HomeActivity.class);
                                    intent.putExtra(HomeActivity.mobile, mobileFinal);
                                    intent.putExtra(HomeActivity.uid, uidFinal);
                                    //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }
                                else if(jsonObject.getString("statusCode").equals("400")) {
                                    //Toast.makeText(activity, getResources().getString(R.string.badRequest), Toast.LENGTH_LONG).show();
                                    feedback.setMessage(getResources().getString(R.string.badRequest));
                                    feedback.setPositiveButton(R.string.ok, null);
                                    feedback.show();
                                }
                                else if(jsonObject.getString("statusCode").equals("500")) {
                                    //Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                                    feedback.setMessage(getResources().getString(R.string.serverError));
                                    feedback.setPositiveButton(R.string.ok, null);
                                    feedback.show();
                                }
                                else if(jsonObject.getString("statusCode").equals("404")) {
                                    //Toast.makeText(activity, getResources().getString(R.string.tryAgain), Toast.LENGTH_LONG).show();
                                    feedback.setMessage(getResources().getString(R.string.tryAgain));
                                    feedback.setPositiveButton(R.string.ok, null);
                                    feedback.show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progress.cancel();
                            if(error instanceof TimeoutError) {
                                //Toast.makeText(activity, getResources().getString(R.string.serversDown), Toast.LENGTH_LONG).show();
                                feedback.setMessage(getResources().getString(R.string.serversDown));
                                feedback.setPositiveButton(R.string.ok, null);
                                feedback.show();
                            }
                            else {
                                //Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                                feedback.setMessage(getResources().getString(R.string.serverError));
                                feedback.setPositiveButton(R.string.ok, null);
                                feedback.show();
                            }
                        }
                    })
            {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("mobile", mobileFinal);
                    params.put("uid", uidFinal);
                    params.put("otp", otp);
                    return params;
                }
            };
            MySingleton.getInstance(this).addToRequestQueue(stringRequest);

        }
        else
            Toast.makeText(activity, getResources().getString(R.string.validOTP), Toast.LENGTH_LONG).show();
    }
}
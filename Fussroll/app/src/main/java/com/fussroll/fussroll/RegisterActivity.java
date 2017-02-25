package com.fussroll.fussroll;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText editText;
    private Spinner spinner;
    private TextInputLayout textInputLayout;
    private Activity activity = this;
    private int activityCheck = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editText = (EditText) findViewById(R.id.editText);
        spinner = (Spinner) findViewById(R.id.spinner);
        textInputLayout = (TextInputLayout) findViewById(R.id.textInputLayout);

        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this, R.array.countries, R.layout.spinner_layout);
        arrayAdapter.setDropDownViewResource(R.layout.spinner_layout);
        spinner.setAdapter(arrayAdapter);

        AppCompatButton appCompatButton = (AppCompatButton) findViewById(R.id.button);
        appCompatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });

    }

    public void register() {

                String mobile = editText.getText().toString();
                String countryCode = spinner.getSelectedItem().toString();

                //Extract country code
                countryCode = new GetCountryCode(countryCode).getCountryCode();

                if (mobile.equals(getResources().getString(R.string.allZeros)))
                    textInputLayout.setError(getResources().getString(R.string.requireValid));
                else if (mobile.length() == 10 && mobile.matches(getResources().getString(R.string.regexDigits))) {

                    //Hide keyboard
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(findViewById(android.R.id.content).getWindowToken(), 0);

                    final ProgressDialog progress = new ProgressDialog(this);
                    progress.setMessage(getResources().getString(R.string.registering));
                    progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progress.setIndeterminate(true);
                    progress.show();

                    final AlertDialog.Builder feedback = new AlertDialog.Builder(this);

                    textInputLayout.setErrorEnabled(false);
                    mobile = mobile.trim();
                    final String mobileFinal = countryCode + mobile;

                    StringRequest stringRequest = new StringRequest(Request.Method.POST,
                            getResources().getString(R.string.registerAPI),
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    progress.cancel();
                                    try {
                                        JSONObject jsonObject = new JSONObject(response);
                                        if (jsonObject.getString("statusCode").equals("201")) {
                                            //Documented - 1
                                            //Store the uid and mobile for making further requests
                                            SharedPreferences sharedPreferences = getSharedPreferences("request", Context.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putString("mobile", mobileFinal);
                                            editor.putString("uid", AESEncryption.encrypt(jsonObject.getString("uid")));
                                            editor.putString("registered", "true");
                                            editor.apply();

                                            //Start new activity, for verifying mobile number with otp
                                            Intent intent = new Intent(activity, VerifyActivity.class);
                                            intent.putExtra(VerifyActivity.mobileToVerify, mobileFinal);
                                            intent.putExtra(VerifyActivity.uidToVerify, jsonObject.getString("uid"));
                                            startActivity(intent);

                                        } else if (jsonObject.getString("statusCode").equals("400")) {
                                            //Toast.makeText(activity, getResources().getString(R.string.badRequest), Toast.LENGTH_LONG).show();
                                            feedback.setMessage(getResources().getString(R.string.badRequest));
                                            feedback.setPositiveButton(R.string.ok, null);
                                            if (activityCheck != 0)
                                                feedback.show();
                                            else
                                                Toast.makeText(activity, getResources().getString(R.string.badRequest), Toast.LENGTH_LONG).show();
                                        } else if (jsonObject.getString("statusCode").equals("500")) {
                                            //Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                                            feedback.setMessage(getResources().getString(R.string.serverError));
                                            feedback.setPositiveButton(R.string.ok, null);
                                            if (activityCheck != 0)
                                                feedback.show();
                                            else
                                                Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
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
                                    if (error instanceof TimeoutError) {
                                        //Toast.makeText(activity, getResources().getString(R.string.serversDown), Toast.LENGTH_LONG).show();
                                        feedback.setMessage(getResources().getString(R.string.serversDown));
                                        feedback.setPositiveButton(R.string.ok, null);
                                        if (activityCheck != 0)
                                            feedback.show();
                                        else
                                            Toast.makeText(activity, getResources().getString(R.string.serversDown), Toast.LENGTH_LONG).show();
                                    } else {
                                        //Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                                        feedback.setMessage(getResources().getString(R.string.serverError));
                                        feedback.setPositiveButton(R.string.ok, null);
                                        if (activityCheck != 0)
                                            feedback.show();
                                        else
                                            Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                                    }
                                }
                            }) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<>();
                            params.put("mobile", mobileFinal);
                            return params;
                        }
                    };
                    MySingleton.getInstance(this).addToRequestQueue(stringRequest);
                } else
                    textInputLayout.setError(getResources().getString(R.string.require10D));
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityCheck = 0;
    }
}

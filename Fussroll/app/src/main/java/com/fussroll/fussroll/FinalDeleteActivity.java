package com.fussroll.fussroll;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FinalDeleteActivity extends AppCompatActivity {

    private Activity activity = this;
    private int activityCheck = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_delete);

        AppCompatButton noButton = (AppCompatButton) findViewById(R.id.button);
        AppCompatButton yesButton = (AppCompatButton) findViewById(R.id.button1);

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                no();
            }
        });

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                yes();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        activityCheck = 1;
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityCheck = 0;
    }

    public void yes() {

        SharedPreferences sharedPreferences = getSharedPreferences("request", Context.MODE_PRIVATE);
        final String mobile = sharedPreferences.getString("mobile", "");
        String uid;

        try {
            uid = AESEncryption.decrypt(sharedPreferences.getString("uid", ""));
        } catch (Exception e) {
            uid = "";
            e.printStackTrace();
        }

        final AlertDialog.Builder feedback = new AlertDialog.Builder(this);
        final String finalUid = uid;

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                getResources().getString(R.string.deleteAPI),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if(jsonObject.getString("statusCode").equals("200")) {

                                //Delete all activities present in the local database
                                DatabaseHandler databaseHandler = new DatabaseHandler(activity);
                                databaseHandler.wipeAll();

                                //Delete everything from SharedPreferences i.e, from the request file
                                SharedPreferences sharedPreferences = getSharedPreferences("request", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.clear();
                                editor.apply();

                                //Make firebaseTokenRefreshed key's value to false so that if previous or current users logs back in
                                //then the same firebase token will be used because in HomeActivity we check for the key's value to be false
                                //and the fact that token is generated for each device and it is not for each user
                                SharedPreferences sharedPreferencesFB = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                SharedPreferences.Editor editorFB = sharedPreferencesFB.edit();
                                editorFB.putString("firebaseTokenRefreshed", "false");
                                editorFB.apply();

                                //Take user to the MainActivity
                                Intent intent = new Intent(activity, MainActivity.class);
                                startActivity(intent);

                            } else if(jsonObject.getString("statusCode").equals("500")) {
                                progressDialog.cancel();
                                //Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                                feedback.setMessage(R.string.serverError);
                                feedback.setPositiveButton(R.string.ok, null);
                                if(activityCheck != 0)
                                    feedback.show();
                                else
                                    Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                            } else if(jsonObject.getString("statusCode").equals("401")) {
                                progressDialog.cancel();
                                //Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                                final AlertDialog.Builder verifyAgain = new AlertDialog.Builder(activity);
                                verifyAgain.setMessage(R.string.verifyAgain);
                                verifyAgain.setPositiveButton(getResources().getString(R.string.verify), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        SharedPreferences sharedPreferences = getSharedPreferences("request", Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("registered", "false");
                                        editor.putString("confirmed", "false");
                                        editor.apply();
                                        Intent intent = new Intent(activity, RegisterActivity.class);
                                        startActivity(intent);
                                    }
                                });
                                if(activityCheck != 0)
                                    verifyAgain.show();
                                else
                                    Toast.makeText(activity, getResources().getString(R.string.verify), Toast.LENGTH_LONG).show();
                            } else if(jsonObject.getString("statusCode").equals("404")) {
                                progressDialog.cancel();
                                //Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                                feedback.setMessage(R.string.couldNotProcess);
                                feedback.setPositiveButton(R.string.ok, null);
                                if(activityCheck != 0)
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
                        if(error instanceof TimeoutError) {
                            progressDialog.cancel();
                            //Toast.makeText(activity, getResources().getString(R.string.serversDown), Toast.LENGTH_LONG).show();
                            feedback.setMessage(R.string.serversDown);
                            feedback.setPositiveButton(R.string.ok, null);
                            if(activityCheck != 0)
                                feedback.show();
                            else
                                Toast.makeText(activity, getResources().getString(R.string.serversDown), Toast.LENGTH_LONG).show();
                        }
                        else {
                            progressDialog.cancel();
                            //Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                            feedback.setMessage(R.string.serverError);
                            feedback.setPositiveButton(R.string.ok, null);
                            if(activityCheck != 0)
                                feedback.show();
                            else
                                Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                        }
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("mobile", mobile);
                params.put("uid", finalUid);
                return params;
            }
        };

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if(networkInfo != null) {
            progressDialog.show();
            MySingleton.getInstance(this).addToRequestQueue(stringRequest);
        }
        else {
            progressDialog.cancel();
            Toast.makeText(activity, getResources().getString(R.string.notConnected), Toast.LENGTH_LONG).show();
        }
    }

    public void no() {
        startActivity(new Intent(this, HomeActivity.class));
        Toast.makeText(this, R.string.thanksStaying, Toast.LENGTH_SHORT).show();
    }
}

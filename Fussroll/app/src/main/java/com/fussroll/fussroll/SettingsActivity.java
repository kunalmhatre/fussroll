package com.fussroll.fussroll;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.IntegerRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity implements CategoryAdapter.ClickListener {

    int privacyOption = 0;
    int notificationOption = 0;
    private Activity activity = this;
    int activityCheck = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        CategoryAdapter categoryAdapter = new CategoryAdapter(this, CategoryLogMeta.settings, CategoryLogMeta.settingsIcons, R.layout.settings_row);
        categoryAdapter.setClickListener(this);
        recyclerView.setAdapter(categoryAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        //For adding dividers in the list
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.line_divider));
        recyclerView.addItemDecoration(dividerItemDecoration);
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

    @Override
    public void onItemClick(View view, int position) {
        switch (position) {
            case 0:
                final AlertDialog.Builder notification = new AlertDialog.Builder(this);
                final SharedPreferences sharedPreferencesNotification = getSharedPreferences("request", Context.MODE_PRIVATE);
                final int optionNotification = Integer.parseInt(sharedPreferencesNotification.getString("notification", "0"));

                notification.setTitle(R.string.allNotificationSetting);
                notification.setSingleChoiceItems(new String[]{getString(R.string.yes), getString(R.string.no)}, optionNotification, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        notificationOption = i;
                    }
                });
                notification.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(optionNotification != notificationOption) {
                            updateNotification(Integer.toString(notificationOption));
                        }
                    }
                });
                notification.setNegativeButton(R.string.cancel, null);
                notification.show();
                break;
            case 1:
                final AlertDialog.Builder privacy = new AlertDialog.Builder(this);
                SharedPreferences sharedPreferences = getSharedPreferences("request", Context.MODE_PRIVATE);
                final int option = Integer.parseInt(sharedPreferences.getString("privacy", "0"));

                privacy.setTitle(R.string.whoCanCheckActivities);
                privacy.setSingleChoiceItems(new String[]{getResources().getString(R.string.everyone), getResources().getString(R.string.onlyContacts)}, option, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        privacyOption = i;
                    }
                });
                privacy.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(option != privacyOption)
                            updatePrivacy(privacyOption);
                    }
                });
                privacy.setNegativeButton(R.string.cancel, null);
                privacy.show();
                break;
            case 2:
                Intent intent = new Intent(this, BlockActivity.class);
                startActivity(intent);
                break;
            case 3:
                startActivity(new Intent(this, DeleteActivity.class));
                break;
        }
    }

    void updateNotification(String option) {
        SharedPreferences sharedPreferences = getSharedPreferences("request", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("notification", option);
        editor.apply();
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

    void updatePrivacy(final int option) {

        SharedPreferences sharedPreferences = getSharedPreferences("request", Context.MODE_PRIVATE);
        final String mobile = sharedPreferences.getString("mobile","");
        String uid;

        try {
            uid = AESEncryption.decrypt(sharedPreferences.getString("uid",""));
        } catch (Exception e) {
            uid = "";
            e.printStackTrace();
        }

        final AlertDialog.Builder feedback = new AlertDialog.Builder(this);
        final String finalUid = uid;

        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage(getString(R.string.updating));
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                getResources().getString(R.string.blockAPI),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if(jsonObject.getString("statusCode").equals("200")) {
                                progress.cancel();
                                SharedPreferences sharedPreferences = getSharedPreferences("request", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("privacy", Integer.toString(option));
                                editor.apply();
                                Toast.makeText(activity, R.string.privacyUpdated, Toast.LENGTH_LONG).show();
                            }
                            else if(jsonObject.getString("statusCode").equals("500")) {
                                progress.cancel();
                                //Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                                feedback.setMessage(R.string.serverError);
                                feedback.setPositiveButton(R.string.ok, null);
                                if(activityCheck != 0)
                                    feedback.show();
                                else
                                    Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                            }
                            else if(jsonObject.getString("statusCode").equals("401")) {
                                progress.cancel();
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
                                progress.cancel();
                                //Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                                feedback.setMessage(R.string.couldNotProcess);
                                feedback.setPositiveButton(R.string.ok, null);
                                if(activityCheck != 0)
                                    feedback.show();
                                else
                                    Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(error instanceof TimeoutError) {
                            progress.cancel();
                            //Toast.makeText(activity, getResources().getString(R.string.serversDown), Toast.LENGTH_LONG).show();
                            feedback.setMessage(R.string.serversDown);
                            feedback.setPositiveButton(R.string.ok, null);
                            if(activityCheck != 0)
                                feedback.show();
                            else
                                Toast.makeText(activity, getResources().getString(R.string.serversDown), Toast.LENGTH_LONG).show();
                        }
                        else {
                            progress.cancel();
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
                String blockOption = null;
                params.put("mobile", mobile);
                params.put("uid", finalUid);
                params.put("contact", mobile);
                if(option == 0)
                    blockOption = "nc";
                else if(option == 1)
                    blockOption = "oc";
                params.put("block", blockOption);
                return params;
            }
        };

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if(networkInfo != null) {
            progress.show();
            MySingleton.getInstance(this).addToRequestQueue(stringRequest);
        }
        else {
            progress.cancel();
            Toast.makeText(activity, getResources().getString(R.string.notConnected), Toast.LENGTH_LONG).show();
        }
    }
}

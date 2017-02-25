package com.fussroll.fussroll;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.NativeActivity;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.TimeUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.gordonwong.materialsheetfab.MaterialSheetFab;
import com.gordonwong.materialsheetfab.MaterialSheetFabEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    public static final String mobile = "mobile";
    public static final String uid = "uid";
    private MaterialSheetFab materialSheetFab;
    private ListView listView;
    private Activity activity = this;
    private int activityCheck = 1;
    public static Fab fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        PagerAdapter pagerAdapter = new pagerAdapter(getSupportFragmentManager());
        setSupportActionBar(toolbar);
        viewPager.setAdapter(pagerAdapter);
        //Setting current selected tab to People
        viewPager.setCurrentItem(1);
        tabLayout.setupWithViewPager(viewPager);

        //Documented - 1
        SharedPreferences sharedPreferencesDefault = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String firebaseToken = sharedPreferencesDefault.getString("firebaseToken", "N/A");
        String firebaseIsRefreshed = sharedPreferencesDefault.getString("firebaseTokenRefreshed", "N/A");

        if(firebaseIsRefreshed.equals("false")) {
            //Log.i("firebase", "Token is refreshed : "+firebaseToken);
            setFirebaseToken(firebaseToken, getApplicationContext());
        }
        //else
            //Log.i("firebase", "Token is not refreshed yet");

        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(true);

        //Documented - 2
        fab = (Fab) findViewById(R.id.fab);
        View sheetView = findViewById(R.id.fabSheet);
        View overlay = findViewById(R.id.overlay);
        int sheetColor = ContextCompat.getColor(this, android.R.color.white);
        int fabColor = ContextCompat.getColor(this, R.color.colorAccent);
        listView = (ListView) findViewById(R.id.list);
        CategoriesArrayAdapter categoriesArrayAdapter = new CategoriesArrayAdapter(activity, CategoryLogMeta.categories, CategoryLogMeta.categoriesIcons);
        listView.setAdapter(categoriesArrayAdapter);

        materialSheetFab = new MaterialSheetFab<>(fab, sheetView, overlay, sheetColor, fabColor);
        materialSheetFab.setEventListener(new MaterialSheetFabEventListener() {
            @Override
            public void onShowSheet() {
                super.onShowSheet();
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        TextView textView = (TextView) view.findViewById(R.id.textView);
                        if(textView.getText().toString().equals("Feeling")) {
                            Intent intent = new Intent(activity, CategoryActivity.class);
                            intent.putExtra(CategoryActivity.category, "feeling");
                            startActivity(intent);
                        }
                        else if(textView.getText().toString().equals("Place")) {
                            Intent intent = new Intent(activity, CategoryActivity.class);
                            intent.putExtra(CategoryActivity.category, "place");
                            startActivity(intent);
                        }
                        else if(textView.getText().toString().equals("Activity")) {
                            Intent intent = new Intent(activity, CategoryActivity.class);
                            intent.putExtra(CategoryActivity.category, "activity");
                            startActivity(intent);
                        }
                        else if(textView.getText().toString().equals("Food")) {
                            Intent intent = new Intent(activity, CategoryActivity.class);
                            intent.putExtra(CategoryActivity.category, "food");
                            startActivity(intent);
                        }
                        else if(textView.getText().toString().equals("Drink")) {
                            Intent intent = new Intent(activity, CategoryActivity.class);
                            intent.putExtra(CategoryActivity.category, "drink");
                            startActivity(intent);
                        }
                        else if(textView.getText().toString().equals("Game")) {
                            Intent intent = new Intent(activity, CategoryActivity.class);
                            intent.putExtra(CategoryActivity.category, "game");
                            startActivity(intent);
                        }
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(materialSheetFab.isSheetVisible()) {
                                    materialSheetFab.hideSheet();
                                }
                            }
                        }, 500);
                    }
                });
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        activityCheck = 1;
        //Documented - 3
        CloudMessages.notificationList.clear();
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1337);
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityCheck = 0;
        //Documented - 4
        CloudMessages.notificationList.clear();
    }

    @Override
    public void onBackPressed() {
        if(materialSheetFab.isSheetVisible()) {
            materialSheetFab.hideSheet();
        }
        else
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.about:
                Intent intentWeb = new Intent(Intent.ACTION_VIEW, Uri.parse("https://fussroll.com/"));
                startActivity(intentWeb);
        }
        return super.onOptionsItemSelected(item);
    }

    class pagerAdapter extends FragmentPagerAdapter {

        pagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new ActivitiesFragment();
                case 1:
                    return new PeopleFragment();
                case 2:
                    return new ContactsFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Yours";
                case 1:
                    return "People";
                case 2:
                    return "Contacts";
                default:
                    return null;
            }
        }


    }

    void setFirebaseToken(final String token, final Context context) {

        Log.i("firebase", "Token to be sent to the server : "+token);

        final SharedPreferences sharedPreferences = getSharedPreferences("request", Context.MODE_PRIVATE);

        final String mobile = sharedPreferences.getString("mobile", "");
        String uid;

        try {
            uid = AESEncryption.decrypt(sharedPreferences.getString("uid", ""));
        } catch (Exception e) {
            uid = "";
            e.printStackTrace();
        }

        final String finalUid = uid;
        final AlertDialog.Builder feedback = new AlertDialog.Builder(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                getResources().getString(R.string.setFirebaseTokenAPI),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if(jsonObject.getString("statusCode").equals("200")) {
                                SharedPreferences sharedPreferences1 = PreferenceManager.getDefaultSharedPreferences(context);
                                SharedPreferences.Editor editor = sharedPreferences1.edit();
                                editor.putString("firebaseTokenRefreshed", "true");
                                editor.apply();
                                Log.i("firebase", "Token has been successfully synced with the backend");
                            }
                            else if(jsonObject.getString("statusCode").equals("401")) {
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
                                if (activityCheck != 0)
                                    verifyAgain.show();
                                else
                                    Toast.makeText(activity, getResources().getString(R.string.verify), Toast.LENGTH_LONG).show();
                            }
                            else if(jsonObject.getString("statusCode").equals("404")) {
                                //Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                                feedback.setMessage(R.string.couldNotProcess);
                                feedback.setPositiveButton(R.string.ok, null);
                                if (activityCheck != 0)
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
                    public void onErrorResponse(VolleyError error) {}
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("mobile", mobile);
                params.put("uid", finalUid);
                params.put("fbtoken", token);
                return params;
            }
        };

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null) {
            MySingleton.getInstance(this).addToRequestQueue(stringRequest);
        }

    }
}

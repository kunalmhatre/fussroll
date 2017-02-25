package com.fussroll.fussroll;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ActivitiesActivity extends AppCompatActivity {

    public static final String userName = "userName";
    public static final String userPhoneNumber = "userPhoneNumber";
    private int activityCheck = 1;
    private Activity activity = this;
    String phoneNumberHere;
    private ActivitiesAdapter activitiesAdapter;
    private RecyclerView recyclerView;
    TextView textView;
    ProgressBar progressBar;
    List<Logs> listLogsPrevious;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        textView = (TextView) findViewById(R.id.textView);
        phoneNumberHere = getIntent().getStringExtra(userPhoneNumber);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(getIntent().getStringExtra(userName));
        }

        //Calling with 0 because we need to setup logs which we already have in local database
        checkForNewUpdates(0);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                    //Calling with 1 because logs from local database are already set and now we need to check for new updates
                    checkForNewUpdates(1);
                swipeRefreshLayout.setRefreshing(false);

            }
        });

    }

    public void checkForNewUpdates(int opt) {

        SimpleDateFormat entireDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String localEntireDate = entireDate.format(new Date());
        //Log.i("activities", "Local date is "+localEntireDate);

        DatabaseHandler databaseHandler = new DatabaseHandler(this);
        listLogsPrevious = databaseHandler.getActivities(getIntent().getStringExtra(userPhoneNumber));
        databaseHandler.close();

        if(listLogsPrevious != null && opt == 0) {

            String fetchDate = listLogsPrevious.get(0).getDate();

            if(!localEntireDate.equals(fetchDate))
                textView.setText(coolDateString(fetchDate));
            else
                textView.setText(R.string.today);

            activitiesAdapter = new ActivitiesAdapter(this, listLogsPrevious);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setAdapter(activitiesAdapter);

            //For adding dividers in the list
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
            dividerItemDecoration.setDrawable(ContextCompat.getDrawable(activity, R.drawable.line_divider));
            recyclerView.addItemDecoration(dividerItemDecoration);

        }

        getActivitiesOfUser(getIntent().getStringExtra(userPhoneNumber), localEntireDate, textView);

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activities_activity, menu);

        MenuItem menuItem = menu.findItem(R.id.muteUnmute);
        SharedPreferences sharedPreferences = getSharedPreferences("request", MODE_PRIVATE);

        //Getting users which are muted
        List<String> mutedList = new ArrayList<>(Arrays.asList(sharedPreferences.getString("muted", "N/A").replaceAll("[\\[\\] ]", "").split(",")));

        //We are internationalizing the number present in the list, i.e, 9867740461 to +919867740461 - Check the function InternationalizedTheNumber for more info
        if(mutedList.contains(new InternationalizedTheNumber(phoneNumberHere, getSharedPreferences("request", MODE_PRIVATE).getString("mobile","N/A")).getIt())) {
            menuItem.setTitle("UNMUTE");
            menuItem.setIcon(R.mipmap.ic_notifications_off_white_24dp);
        }
        else {
            menuItem.setTitle("MUTE");
            menuItem.setIcon(R.mipmap.ic_notifications_active_white_24dp);
        }

        //onClickListener for toggling between muting the user and un-muting it
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(menuItem.getTitle().equals("MUTE")) {
                    phoneNumberHere = new InternationalizedTheNumber(phoneNumberHere, getSharedPreferences("request", MODE_PRIVATE).getString("mobile","N/A")).getIt();
                    //Adding user to the mute list
                    addRemoveMuteList(phoneNumberHere, 0);
                    menuItem.setTitle("UNMUTE");
                    menuItem.setIcon(R.mipmap.ic_notifications_off_white_24dp);
                }
                else {
                    phoneNumberHere = new InternationalizedTheNumber(phoneNumberHere, getSharedPreferences("request", MODE_PRIVATE).getString("mobile","N/A")).getIt();
                    addRemoveMuteList(phoneNumberHere, 1);
                    //Removing user from the mute list
                    menuItem.setTitle("MUTE");
                    menuItem.setIcon(R.mipmap.ic_notifications_active_white_24dp);
                }
                return true;
            }
        });

        return true;
    }

    void addRemoveMuteList(String phoneNumber, int opt) {
        SharedPreferences sharedPreferences = getSharedPreferences("request", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        List<String> mutedList = new ArrayList<>(Arrays.asList(sharedPreferences.getString("muted", "N/A").replaceAll("[\\[\\] ]", "").split(",")));
        mutedList.remove("N/A");
        mutedList.remove("");

        //Log.i("notification", "Muted list : "+mutedList.toString());

        if(opt == 0) {

            if(mutedList.toString().equals("[]")) {
                editor.putString("muted", "["+phoneNumber+"]");
                mutedList.add(phoneNumber);
            }
            else if(!mutedList.contains(phoneNumber)){
                mutedList.add(phoneNumber);
                editor.putString("muted", mutedList.toString());
            }
            editor.apply();
            //Log.i("notification", phoneNumber+" got muted");
            //Log.i("notification", "updated list : "+mutedList.toString());

        }
        else if(opt == 1) {

            if(mutedList.contains(phoneNumber)) {
                mutedList.remove(phoneNumber);
                editor.putString("muted", mutedList.toString());
                editor.apply();

                //Log.i("notification", phoneNumber+" got un-muted");
                //Log.i("notification", "updated list : "+mutedList.toString());
            }

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.muteUnmute:
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    String coolDateString(String date) {
        DateFormat dateFormatToParse = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date dateObject;
        try {
            dateObject = dateFormatToParse.parse(date);
        } catch (ParseException e) {
            dateObject = new Date();
            e.printStackTrace();
        }
        DateFormat dateFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        String dateMonth = dateFormat.format(dateObject);
        String startDate;
        switch (date.substring(0,2)) {
            case "01":
                startDate = "1";
                break;
            case "02":
                startDate = "2";
                break;
            case "03":
                startDate = "3";
                break;
            case "04":
                startDate = "4";
                break;
            case "05":
                startDate = "5";
                break;
            case "06":
                startDate = "6";
                break;
            case "07":
                startDate = "7";
                break;
            case "08":
                startDate = "8";
                break;
            case "09":
                startDate = "9";
                break;
            default:
                startDate = date.substring(0,2);
        }

        return  dateMonth+" "+startDate+", "+date.substring(date.length() - 4);


    }

    protected void getActivitiesOfUser(final String userPhoneNumber, final String phoneDate, final TextView textView) {

        final SharedPreferences sharedPreferences = getSharedPreferences("request", Context.MODE_PRIVATE);

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

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                getResources().getString(R.string.logsAPI),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            //Log.i("activities", "jsonObject "+jsonObject.toString());
                            if(jsonObject.getString("statusCode").equals("200")) {
                                //progressDialog.cancel();

                                JSONArray jsonArray = new JSONArray(jsonObject.getString("activities"));
                                String latestDate = jsonArray.getJSONObject(0).getString("latestUpdate");
                                //Log.i("activities", "Latest date : "+latestDate);

                                jsonArray = new JSONArray(jsonArray.getJSONObject(0).getString("logs"));

                                //For logs
                                DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                                Date dateObject;
                                SimpleDateFormat formatterForTimeRequired = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                                SimpleDateFormat formatterForDateRequired = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                                DatabaseHandler databaseHandler = new DatabaseHandler(activity);
                                String[] maxUTCDateTime = databaseHandler.getMAXUTCDateTime(userPhoneNumber);

                                //Log.i("activities", "getMAXUTCDateTime "+ Arrays.toString(maxUTCDateTime));
                                Date dateObjectToCompare = null;
                                if(!maxUTCDateTime[0].equals("") && !maxUTCDateTime[1].equals("")) {
                                    dateObjectToCompare = format.parse(maxUTCDateTime[0]+" "+maxUTCDateTime[1]);
                                    //Log.i("activities", "dateObjectToCompare "+ dateObjectToCompare.toString());
                                }

                                List<Logs> listLogs = new ArrayList<>();

                                //Log.i("activities", "Following are the activities of the user");
                                for(int i = 0; i < jsonArray.length(); i++) {

                                    dateObject = format.parse(jsonArray.getJSONObject(i).getString("date") + " " + jsonArray.getJSONObject(i).getString("time"));
                                    String time = formatterForTimeRequired.format(dateObject);
                                    String date = formatterForDateRequired.format(dateObject);
                                    String category = String.valueOf(jsonArray.getJSONObject(i).getString("category"));
                                    String log = String.valueOf(jsonArray.getJSONObject(i).getString("log"));
                                    String meta = String.valueOf(jsonArray.getJSONObject(i).getString("meta"));
                                    String utcDate = jsonArray.getJSONObject(i).getString("date");
                                    String utcTime = jsonArray.getJSONObject(i).getString("time");

                                    if(category.equals("activity"))
                                        meta = "RESERVED";

                                    int imageIcon = new CategoryLogMetaHelper(category, log, meta).getImageID();

                                    if(!maxUTCDateTime[0].equals("") && !maxUTCDateTime[1].equals("") && (dateObject.compareTo(dateObjectToCompare) < 0 || dateObject.compareTo(dateObjectToCompare) == 0)) {

                                        //Log.i("activities", "We have this one and coming ones, so break the loop");
                                        break;

                                    }
                                    else {
                                        //Log.i("activities", "MobileNumber : " + userPhoneNumber + ", LocalDate : " + date + ", LocalTime : " + time + ", Category : " + category + ", Log : " + log + ", Meta : " + meta + ", ImageIcon : " + Integer.toString(imageIcon) + ", UTCDate : " + utcDate + ", UTCTime : " + utcTime);

                                        Logs logs = new Logs(userPhoneNumber, category, log, meta, date, time, utcDate, utcTime, imageIcon);
                                        listLogs.add(logs);

                                        //Log.i("activities", "added to the database");
                                    }
                                }

                                //Add all collected logs in database in reverse order

                                for(int i = listLogs.size()-1; i >= 0; i--) {
                                    databaseHandler.addLog(listLogs.get(i).getUserPhoneNumber(),
                                            listLogs.get(i).getCategory(),
                                            listLogs.get(i).getLog(),
                                            listLogs.get(i).getMeta(),
                                            listLogs.get(i).getDate(),
                                            listLogs.get(i).getTime(),
                                            listLogs.get(i).getUtcDate(),
                                            listLogs.get(i).getUtcTime(),
                                            listLogs.get(i).getLogImage());
                                }
                                databaseHandler.close();

                                if(activitiesAdapter == null && listLogs.size() != 0) {

                                    if(phoneDate.equals(listLogs.get(0).getDate())) {
                                        textView.setText(R.string.today);
                                        textView.setGravity(Gravity.START);
                                        textView.setTypeface(null, Typeface.BOLD);
                                        textView.setTextColor(ContextCompat.getColor(activity, R.color.grey));
                                    }
                                    else
                                        textView.setText(coolDateString(listLogs.get(0).getDate()));

                                    //This will only run for one time
                                    activitiesAdapter = new ActivitiesAdapter(activity, listLogs);
                                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
                                    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                                    recyclerView.setLayoutManager(linearLayoutManager);
                                    recyclerView.setAdapter(activitiesAdapter);

                                    //For adding dividers in the list
                                    DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
                                    dividerItemDecoration.setDrawable(ContextCompat.getDrawable(activity, R.drawable.line_divider));
                                    recyclerView.addItemDecoration(dividerItemDecoration);
                                    progressBar.setVisibility(View.GONE);

                                }
                                else if(listLogs.size() > 0 && activitiesAdapter != null) {
                                    if(!textView.getText().toString().equals(getResources().getString(R.string.today))) {

                                        if(phoneDate.equals(listLogs.get(0).getDate())) {
                                            textView.setText(R.string.today);
                                        }
                                        else
                                            textView.setText(coolDateString(listLogs.get(0).getDate()));

                                        activitiesAdapter.setListLogs(listLogs);
                                        activitiesAdapter.notifyDataSetChanged();
                                    }
                                    else {
                                        for(int i = listLogs.size()-1; i >= 0; i--) {
                                            activitiesAdapter.addLog(listLogs.get(i));
                                            activitiesAdapter.notifyItemInserted(0);
                                        }
                                    }
                                    recyclerView.scrollToPosition(0);
                                    progressBar.setVisibility(View.GONE);

                                }
                                else if(listLogs.size() == 0) {
                                    progressBar.setVisibility(View.GONE);
                                }

                            } else if(jsonObject.getString("statusCode").equals("204")) {
                                //No activities updated yet
                                //Log.i("activities", "No activities updated yet");
                                textView.setText(R.string.noActivities);
                                textView.setTextColor(ContextCompat.getColor(activity, R.color.background_dim_overlay));
                                textView.setGravity(Gravity.CENTER_HORIZONTAL);
                                progressBar.setVisibility(View.GONE);
                                //progressDialog.cancel();
                            } else if(jsonObject.getString("statusCode").equals("403")) {
                                //User is blocked
                                //progressDialog.cancel();
                                progressBar.setVisibility(View.GONE);
                                if(listLogsPrevious == null) {
                                    textView.setText(R.string.noActivities);
                                    textView.setTextColor(ContextCompat.getColor(activity, R.color.background_dim_overlay));
                                    textView.setGravity(Gravity.CENTER_HORIZONTAL);
                                }
                            } else if(jsonObject.getString("statusCode").equals("500")) {
                                //progressDialog.cancel();
                                progressBar.setVisibility(View.GONE);
                                //Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                                feedback.setMessage(R.string.serverError);
                                feedback.setPositiveButton(R.string.ok, null);
                                if (activityCheck != 0)
                                    feedback.show();
                                else
                                    Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                            } else if(jsonObject.getString("statusCode").equals("401")) {
                                //progressDialog.cancel();
                                progressBar.setVisibility(View.GONE);
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
                            } else if(jsonObject.getString("statusCode").equals("404")) {
                                //progressDialog.cancel();
                                progressBar.setVisibility(View.GONE);
                                //Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                                feedback.setMessage(R.string.couldNotProcess);
                                feedback.setPositiveButton(R.string.ok, null);
                                if (activityCheck != 0)
                                    feedback.show();
                                else
                                    Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                            } else if(jsonObject.getString("statusCode").equals("400")) {
                                //progressDialog.cancel();
                                progressBar.setVisibility(View.GONE);
                                //Toast.makeText(activity, getResources().getString(R.string.badRequest), Toast.LENGTH_LONG).show();
                                feedback.setMessage(R.string.badRequest);
                                feedback.setPositiveButton(R.string.ok, null);
                                if (activityCheck != 0)
                                    feedback.show();
                                else
                                    Toast.makeText(activity, getResources().getString(R.string.badRequest), Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof TimeoutError) {
                            //progressBar.setVisibility(View.GONE);
                            //Toast.makeText(activity, getResources().getString(R.string.serversDown), Toast.LENGTH_LONG).show();
                            feedback.setMessage(R.string.serversDown);
                            feedback.setPositiveButton(R.string.ok, null);
                            if (activityCheck != 0)
                                feedback.show();
                            else
                                Toast.makeText(activity, getResources().getString(R.string.serversDown), Toast.LENGTH_LONG).show();
                        } else {
                            //progressBar.setVisibility(View.GONE);
                            //Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                            feedback.setMessage(R.string.serverError);
                            feedback.setPositiveButton(R.string.ok, null);
                            if (activityCheck != 0)
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
                params.put("contact", userPhoneNumber);
                params.put("opt", "1");
                return params;
            }
        };

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null) {
            //progressDialog.show();
            MySingleton.getInstance(this).addToRequestQueue(stringRequest);
        } else {
            //progressDialog.cancel();
            //Toast.makeText(activity, getResources().getString(R.string.notConnected), Toast.LENGTH_LONG).show();
            feedback.setMessage(getResources().getString(R.string.notConnected));
            feedback.setPositiveButton(R.string.ok, null);
            if (activityCheck != 0)
                feedback.show();
            else
                Toast.makeText(activity, getResources().getString(R.string.notConnected), Toast.LENGTH_LONG).show();
        }

    }
}

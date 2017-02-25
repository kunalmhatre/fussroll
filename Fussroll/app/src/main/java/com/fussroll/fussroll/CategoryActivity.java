package com.fussroll.fussroll;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONObject;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class CategoryActivity extends AppCompatActivity implements CategoryAdapter.ClickListener {

    public static final String category = "category";

    Activity activity = this;
    private String categoryFinal;
    private int activityCheck = 1;
    private CategoryAdapter categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        categoryFinal = getIntent().getStringExtra(category);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(categoryFinal.substring(0, 1).toUpperCase() + categoryFinal.substring(1));
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        try {

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(linearLayoutManager);

            //For adding dividers in the list
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
            dividerItemDecoration.setDrawable(ContextCompat.getDrawable(activity, R.drawable.line_divider));
            recyclerView.addItemDecoration(dividerItemDecoration);

            switch (categoryFinal) {
                case "feeling":
                    categoryAdapter = new CategoryAdapter(this, CategoryLogMeta.feeling, CategoryLogMeta.feelingIcons);
                    categoryAdapter.setClickListener(this);
                    recyclerView.setAdapter(categoryAdapter);
                    break;
                case "place":
                    categoryAdapter = new CategoryAdapter(this, CategoryLogMeta.place, CategoryLogMeta.placeIcons);
                    categoryAdapter.setClickListener(this);
                    recyclerView.setAdapter(categoryAdapter);
                    break;
                case "activity":
                    //Making the first letter of all activities smaller
                    String[] activities = new String[CategoryLogMeta.activity.length];
                    for (int i = 0; i < CategoryLogMeta.activity.length; i++) {
                        activities[i] = CategoryLogMeta.activity[i].substring(0, 1).toLowerCase() + CategoryLogMeta.activity[i].substring(1, CategoryLogMeta.activity[i].length());
                    }
                    categoryAdapter = new CategoryAdapter(this, activities, CategoryLogMeta.activityIcons);
                    categoryAdapter.setClickListener(this);
                    recyclerView.setAdapter(categoryAdapter);
                    break;
                case "food":
                    categoryAdapter = new CategoryAdapter(this, CategoryLogMeta.food, CategoryLogMeta.foodIcons);
                    categoryAdapter.setClickListener(this);
                    recyclerView.setAdapter(categoryAdapter);
                    break;
                case "drink":
                    categoryAdapter = new CategoryAdapter(this, CategoryLogMeta.drink, CategoryLogMeta.drinkIcons);
                    categoryAdapter.setClickListener(this);
                    recyclerView.setAdapter(categoryAdapter);
                    break;
                case "game":
                    categoryAdapter = new CategoryAdapter(this, CategoryLogMeta.game, CategoryLogMeta.gameIcons);
                    categoryAdapter.setClickListener(this);
                    recyclerView.setAdapter(categoryAdapter);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.category_activity, menu);

        MenuItem menuItem = menu.findItem(R.id.searchView);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                newText = newText.toLowerCase();

                List<String> logs = new ArrayList<>();
                List<Integer> logsIcons = new ArrayList<>();

                switch (categoryFinal) {
                    case "feeling":
                        for (int i = 0; i < CategoryLogMeta.feeling.length; i++) {
                            if (CategoryLogMeta.feeling[i].contains(newText)) {
                                logs.add(CategoryLogMeta.feeling[i]);
                                logsIcons.add(CategoryLogMeta.feelingIcons[i]);
                            }
                        }
                        break;
                    case "place":
                        for (int i = 0; i < CategoryLogMeta.place.length; i++) {
                            if (CategoryLogMeta.place[i].contains(newText)) {
                                logs.add(CategoryLogMeta.place[i]);
                                logsIcons.add(CategoryLogMeta.placeIcons[i]);
                            }
                        }
                        break;
                    case "activity":
                        for (int i = 0; i < CategoryLogMeta.activity.length; i++) {
                            String tempLog = CategoryLogMeta.activity[i].toLowerCase();
                            if (tempLog.contains(newText)) {
                                logs.add(CategoryLogMeta.activity[i].substring(0, 1).toLowerCase() + CategoryLogMeta.activity[i].substring(1, CategoryLogMeta.activity[i].length()));
                                logsIcons.add(CategoryLogMeta.activityIcons[i]);
                            }
                        }
                        break;
                    case "food":
                        for (int i = 0; i < CategoryLogMeta.food.length; i++) {
                            if (CategoryLogMeta.food[i].contains(newText)) {
                                logs.add(CategoryLogMeta.food[i]);
                                logsIcons.add(CategoryLogMeta.foodIcons[i]);
                            }
                        }
                        break;
                    case "drink":
                        for (int i = 0; i < CategoryLogMeta.drink.length; i++) {
                            if (CategoryLogMeta.drink[i].contains(newText)) {
                                logs.add(CategoryLogMeta.drink[i]);
                                logsIcons.add(CategoryLogMeta.drinkIcons[i]);
                            }
                        }
                        break;
                    case "game":
                        for (int i = 0; i < CategoryLogMeta.game.length; i++) {
                            if (CategoryLogMeta.game[i].contains(newText)) {
                                logs.add(CategoryLogMeta.game[i]);
                                logsIcons.add(CategoryLogMeta.gameIcons[i]);
                            }
                        }
                        break;
                }

                categoryAdapter.setData(logs.toArray(new String[logs.size()]), ArrayUtils.toPrimitive(logsIcons.toArray(new Integer[logsIcons.size()])));
                categoryAdapter.notifyDataSetChanged();

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
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

    protected void update(final String log, final String meta, final int logImage) {

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

        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage(getString(R.string.updating));
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                getResources().getString(R.string.updateLogAPI),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getString("statusCode").equals("201")) {
                                progress.cancel();
                                Toast.makeText(activity, R.string.momentUpdated, Toast.LENGTH_SHORT).show();

                                DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                                Date date = format.parse(jsonObject.getString("date") + " " + jsonObject.getString("time"));

                                SimpleDateFormat formatterForTimeRequired = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                                SimpleDateFormat formatterForDateRequired = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                SimpleDateFormat formatterForLocalDateNewFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                String timeRequired = formatterForTimeRequired.format(date);
                                String dateRequired = formatterForDateRequired.format(date);
                                String localDateNewFormat = formatterForLocalDateNewFormat.format(date);


                                DatabaseHandler databaseHandler = new DatabaseHandler(activity);
                                databaseHandler.addLog(categoryFinal, log, meta, dateRequired, timeRequired, logImage);
                                databaseHandler.addLogPeople(mobile, categoryFinal, log, meta, localDateNewFormat, timeRequired, jsonObject.getString("date"), jsonObject.getString("time"), logImage);
                                databaseHandler.close();
                                Toast.makeText(activity, R.string.momentUpdated, Toast.LENGTH_SHORT).show();

                            } else if (jsonObject.getString("statusCode").equals("400")) {
                                progress.cancel();
                                //Toast.makeText(activity, getResources().getString(R.string.badRequest), Toast.LENGTH_LONG).show();
                                feedback.setMessage(R.string.badRequest);
                                feedback.setPositiveButton(R.string.ok, null);
                                if (activityCheck != 0)
                                    feedback.show();
                                else
                                    Toast.makeText(activity, getResources().getString(R.string.badRequest), Toast.LENGTH_LONG).show();
                            } else if (jsonObject.getString("statusCode").equals("500")) {
                                progress.cancel();
                                //Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                                feedback.setMessage(R.string.serverError);
                                feedback.setPositiveButton(R.string.ok, null);
                                if (activityCheck != 0)
                                    feedback.show();
                                else
                                    Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                            } else if (jsonObject.getString("statusCode").equals("401")) {
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
                                if (activityCheck != 0)
                                    verifyAgain.show();
                                else
                                    Toast.makeText(activity, getResources().getString(R.string.verify), Toast.LENGTH_LONG).show();
                            } else if (jsonObject.getString("statusCode").equals("404")) {
                                progress.cancel();
                                //Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                                feedback.setMessage(R.string.couldNotProcess);
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
                        if (error instanceof TimeoutError) {
                            progress.cancel();
                            //Toast.makeText(activity, getResources().getString(R.string.serversDown), Toast.LENGTH_LONG).show();
                            feedback.setMessage(R.string.serversDown);
                            feedback.setPositiveButton(R.string.ok, null);
                            if (activityCheck != 0)
                                feedback.show();
                            else
                                Toast.makeText(activity, getResources().getString(R.string.serversDown), Toast.LENGTH_LONG).show();
                        } else {
                            progress.cancel();
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
                params.put("category", categoryFinal);
                params.put("log", log);
                params.put("meta", meta);
                return params;
            }
        };

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null) {
            progress.show();
            MySingleton.getInstance(this).addToRequestQueue(stringRequest);
        } else {
            progress.cancel();
            //Toast.makeText(activity, getResources().getString(R.string.notConnected), Toast.LENGTH_LONG).show();
            feedback.setMessage(getResources().getString(R.string.notConnected));
            feedback.setPositiveButton(R.string.ok, null);
            if (activityCheck != 0)
                feedback.show();
            else
                Toast.makeText(activity, getResources().getString(R.string.notConnected), Toast.LENGTH_LONG).show();
        }
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

    @Override
    public void onItemClick(View view, int positionClick) {

        TextView log = (TextView) view.findViewById(R.id.textView);

        int position = 0;

        //Dialog requirements
        final AlertDialog.Builder meta = new AlertDialog.Builder(activity);

        String[] metaList = null;
        final int[] option = new int[1];

        switch (categoryFinal) {
            case "feeling":
                //Mapping position from feeling array present in CategoryLogMeta
                position = Arrays.asList(CategoryLogMeta.feeling).indexOf(log.getText().toString());
                metaList = CategoryLogMeta.getMetaForFeeling();
                Log.i("log", CategoryLogMeta.feeling[position] + " " + Arrays.toString(metaList));
                break;
            case "place":
                position = Arrays.asList(CategoryLogMeta.place).indexOf(log.getText().toString());
                metaList = CategoryLogMeta.getMetaForPlace(CategoryLogMeta.place[position]);
                Log.i("log", CategoryLogMeta.place[position] + " " + Arrays.toString(metaList));
                break;
            case "activity":
                String tempLog = log.getText().toString();
                position = Arrays.asList(CategoryLogMeta.activity).indexOf(tempLog.substring(0, 1).toUpperCase()+tempLog.substring(1, tempLog.length()));
                Log.i("log", CategoryLogMeta.activity[position]);
                break;
            case "food":
                position = Arrays.asList(CategoryLogMeta.food).indexOf(log.getText().toString());
                metaList = CategoryLogMeta.getMetaForFood(CategoryLogMeta.food[position]);
                Log.i("log", CategoryLogMeta.food[position] + " " + Arrays.toString(metaList));
                break;
            case "drink":
                position = Arrays.asList(CategoryLogMeta.drink).indexOf(log.getText().toString());
                metaList = CategoryLogMeta.getMetaForDrink(CategoryLogMeta.drink[position]);
                Log.i("log", CategoryLogMeta.drink[position] + " " + Arrays.toString(metaList));
                break;
            case "game":
                position = Arrays.asList(CategoryLogMeta.game).indexOf(log.getText().toString());
                metaList = CategoryLogMeta.getMetaForGame(CategoryLogMeta.game[position]);
                Log.i("log", CategoryLogMeta.game[position] + " " + Arrays.toString(metaList));
                break;
            default:
                //Should not be reached
                position = 1337;
                metaList = new String[]{"Default"};
                break;
        }

        final int positionFinal = position;

        if (metaList != null) {
            meta.setSingleChoiceItems(metaList, 0, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    option[0] = which;
                }
            });

            final String[] finalMetaList = metaList;
            meta.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String log = null;
                    int logImage = 0;
                    switch (categoryFinal) {
                        case "feeling":
                            log = CategoryLogMeta.feeling[positionFinal];
                            logImage = CategoryLogMeta.feelingIcons[positionFinal];
                            break;
                        case "place":
                            log = CategoryLogMeta.place[positionFinal];
                            logImage = CategoryLogMeta.placeIcons[positionFinal];
                            break;
                        case "activity":
                            //Nothing
                            break;
                        case "food":
                            log = CategoryLogMeta.food[positionFinal];
                            logImage = CategoryLogMeta.foodIcons[positionFinal];
                            break;
                        case "drink":
                            log = CategoryLogMeta.drink[positionFinal];
                            logImage = CategoryLogMeta.drinkIcons[positionFinal];
                            break;
                        case "game":
                            log = CategoryLogMeta.game[positionFinal];
                            logImage = CategoryLogMeta.gameIcons[positionFinal];
                            break;
                    }

                    update(log, finalMetaList[option[0]], logImage);

                }
            });

            meta.setNegativeButton(R.string.cancel, null);

            meta.show();
        } else {
            if (categoryFinal.equals("activity")) {
                update(CategoryLogMeta.activity[position], "", CategoryLogMeta.activityIcons[position]);
            }
        }
    }
}


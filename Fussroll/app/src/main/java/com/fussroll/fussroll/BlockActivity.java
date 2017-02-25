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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockActivity extends AppCompatActivity implements BlockAdapter.ClickListener {

    private int activityCheck = 1;
    private Activity activity = this;
    protected List<String> blockedContacts;
    protected List<String> blockedNames;
    protected BlockAdapter blockAdapter = null;
    protected RecyclerView recyclerView;
    protected HashMap<String, String> hashMap;
    protected String blockingProgressEnd;
    protected ProgressDialog progressDialogBlocking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block);

        //Log.i("block", "onCreate called");

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        CheckForContactsPermission checkForContactsPermission = new CheckForContactsPermission(this);

        if(checkForContactsPermission.checkStatus() == 1) {

            SharedPreferences sharedPreferences = getSharedPreferences("request", MODE_PRIVATE);
            List<String> blockedContactsToDisplay = new ArrayList<>(Arrays.asList(sharedPreferences.getString("blockedContacts", "").replaceAll("[\\[\\] ]", "").split(",")));
            List<String> blockedNamesToDisplay = new ArrayList<>();

            //For getting names of the blocked contacts
            RefreshContacts refreshContacts = new RefreshContacts();
            refreshContacts.refreshContacts(this);
            hashMap = refreshContacts.getHashMapContactsNames();

            if (!blockedContactsToDisplay.toString().equals("[]")) {

                //Log.i("block", "blocked contacts to display" + blockedContactsToDisplay);


                for (String contact : blockedContactsToDisplay) {
                    blockedNamesToDisplay.add(hashMap.get(contact));
                }
                //Log.i("block", "blocked names to display" + blockedNamesToDisplay);

                //Setting adapter and display the list
                blockAdapter = new BlockAdapter(this, blockedNamesToDisplay, blockedContactsToDisplay);
                blockAdapter.setClickListener(this);
                recyclerView.setAdapter(blockAdapter);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                recyclerView.setLayoutManager(linearLayoutManager);

                //For adding dividers in the list
                DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
                dividerItemDecoration.setDrawable(ContextCompat.getDrawable(activity, R.drawable.line_divider));
                recyclerView.addItemDecoration(dividerItemDecoration);
            }

        }

    }

    public void selectContacts(View view) {
        Intent intent = new Intent(this, ContactsActivity.class);
        startActivityForResult(intent, 1337);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1337 && data != null) {
            String[] contacts = data.getStringExtra("contacts").replaceAll("[\\[\\] ]", "").split(",");
            List<String> contactsToBlock = new ArrayList<>(Arrays.asList(contacts));
            contactsToBlock.remove("");



            if (contactsToBlock.size() > 0) {

                blockingProgressEnd = contactsToBlock.get(contactsToBlock.size() - 1);
                //Log.i("block", Arrays.toString(contacts) + " - " + contactsToBlock + " - " + blockingProgressEnd);

                //Start showing the spinner
                progressDialogBlocking = new ProgressDialog(this);
                progressDialogBlocking.setMessage("Blocking...");
                progressDialogBlocking.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialogBlocking.setIndeterminate(true);
                progressDialogBlocking.show();

                for (String contact : contacts) {
                    blockTheUser(contact);
                }

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityCheck = 1;
        //System.out.println("onResume"+ blockedContacts);
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityCheck = 0;
        //System.out.println("onPause"+ blockedContacts);
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
    public void onItemUnblockClicked(String contactName, String phoneNumber) {

        //Log.i("block", "Unblock " + contactName);
        unblockTheUser(phoneNumber);

    }

    void blockTheUser(final String contact) {
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

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                getResources().getString(R.string.blockAPI),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getString("statusCode").equals("200")) {

                                //Add in blockedContacts
                                SharedPreferences sharedPreferences = getSharedPreferences("request", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();

                                //Get any blockedContacts from SharedPrefs if any
                                List<String> previouslyBlockedContacts = new ArrayList<>(Arrays.asList(sharedPreferences.getString("blockedContacts", "").replaceAll("[\\[\\] ]", "").split(",")));
                                //Log.i("block", "previously blocked " + previouslyBlockedContacts.toString());
                                previouslyBlockedContacts.add(contact);
                                previouslyBlockedContacts.remove("");
                                //Log.i("block", "updated blocked list from SharedPrefs " + previouslyBlockedContacts);

                                //Add new contact in SharedPrefs
                                editor.putString("blockedContacts", previouslyBlockedContacts.toString());
                                editor.apply();
                                //Log.i("block", "Added in blocked list : " + contact);

                                if (blockAdapter != null) {
                                    blockAdapter.notifyItemInserted(blockAdapter.addItem(hashMap.get(contact), contact));
                                } else {
                                    //This will run only once, when there are no contacts to display in block activity
                                    List<String> blockedNamesToDisplay = new ArrayList<>();
                                    blockedNamesToDisplay.add(hashMap.get(contact));

                                    //Setting adapter and display the list
                                    blockAdapter = new BlockAdapter(activity, blockedNamesToDisplay, previouslyBlockedContacts);
                                    blockAdapter.setClickListener((BlockAdapter.ClickListener) activity);
                                    recyclerView.setAdapter(blockAdapter);
                                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
                                    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                                    recyclerView.setLayoutManager(linearLayoutManager);

                                    //For adding dividers in the list
                                    DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
                                    dividerItemDecoration.setDrawable(ContextCompat.getDrawable(activity, R.drawable.line_divider));
                                    recyclerView.addItemDecoration(dividerItemDecoration);
                                }

                                //If the contact is at the end of list then stop the spinner
                                if (contact.equals(blockingProgressEnd)) {
                                    progressDialogBlocking.cancel();
                                    blockingProgressEnd = "";
                                }

                            } else if (jsonObject.getString("statusCode").equals("500")) {
                                if (contact.equals(blockingProgressEnd)) {
                                    progressDialogBlocking.cancel();
                                    blockingProgressEnd = "";
                                    //Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                                    feedback.setMessage(R.string.serverError);
                                    feedback.setPositiveButton(R.string.ok, null);
                                    if (activityCheck != 0)
                                        feedback.show();
                                    else
                                        Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                                }
                            } else if (jsonObject.getString("statusCode").equals("403")) {
                                if (contact.equals(blockingProgressEnd)) {
                                    progressDialogBlocking.cancel();
                                    blockingProgressEnd = "";
                                }
                            } else if (jsonObject.getString("statusCode").equals("404")) {
                                if (contact.equals(blockingProgressEnd)) {
                                    progressDialogBlocking.cancel();
                                    blockingProgressEnd = "";
                                    //Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                                    feedback.setMessage(R.string.couldNotProcess);
                                    feedback.setPositiveButton(R.string.ok, null);
                                    if (activityCheck != 0)
                                        feedback.show();
                                    else
                                        Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                                }
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
                            if (contact.equals(blockingProgressEnd)) {
                                progressDialogBlocking.cancel();
                                blockingProgressEnd = "";
                                //Toast.makeText(activity, getResources().getString(R.string.serversDown), Toast.LENGTH_LONG).show();
                                feedback.setMessage(R.string.serversDown);
                                feedback.setPositiveButton(R.string.ok, null);
                                if (activityCheck != 0)
                                    feedback.show();
                                else
                                    Toast.makeText(activity, getResources().getString(R.string.serversDown), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            if (contact.equals(blockingProgressEnd)) {
                                progressDialogBlocking.cancel();
                                blockingProgressEnd = "";
                                //Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                                feedback.setMessage(R.string.serverError);
                                feedback.setPositiveButton(R.string.ok, null);
                                if (activityCheck != 0)
                                    feedback.show();
                                else
                                    Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                            }

                        }
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("mobile", mobile);
                params.put("uid", finalUid);
                params.put("contact", contact);
                params.put("block", "y");
                return params;
            }
        };

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null) {
            MySingleton.getInstance(this).addToRequestQueue(stringRequest);
        } else {
            progressDialogBlocking.cancel();
            Toast.makeText(activity, getResources().getString(R.string.notConnected), Toast.LENGTH_LONG).show();
        }
    }

    void unblockTheUser(final String contact) {

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
        progressDialog.setMessage("Unblocking...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                getResources().getString(R.string.blockAPI),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getString("statusCode").equals("200")) {
                                //Remove from blockedContacts
                                SharedPreferences sharedPreferences = getSharedPreferences("request", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();

                                //Get any blockedContacts from SharedPrefs if any
                                List<String> previouslyBlockedContacts = new ArrayList<>(Arrays.asList(sharedPreferences.getString("blockedContacts", "").replaceAll("[\\[\\] ]", "").split(",")));
                                //Log.i("block", "previously blocked " + previouslyBlockedContacts.toString());
                                previouslyBlockedContacts.remove(contact);
                                //Log.i("block", "updated blocked list from SharedPrefs " + previouslyBlockedContacts);

                                //Add new contact in SharedPrefs
                                editor.putString("blockedContacts", previouslyBlockedContacts.toString());
                                editor.apply();
                                //Log.i("block", "Removed from blocked list : " + contact);
                                progressDialog.cancel();

                                //Remove the contact from the displayed list
                                blockAdapter.notifyItemRemoved(blockAdapter.removeItem(contact));
                            } else if (jsonObject.getString("statusCode").equals("500")) {
                                progressDialog.cancel();
                                //Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                                feedback.setMessage(R.string.serverError);
                                feedback.setPositiveButton(R.string.ok, null);
                                if (activityCheck != 0)
                                    feedback.show();
                                else
                                    Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                            } else if (jsonObject.getString("statusCode").equals("401")) {
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
                                if (activityCheck != 0)
                                    verifyAgain.show();
                                else
                                    Toast.makeText(activity, getResources().getString(R.string.verify), Toast.LENGTH_LONG).show();
                            } else if (jsonObject.getString("statusCode").equals("403")) {
                                progressDialog.cancel();
                            } else if (jsonObject.getString("statusCode").equals("404")) {
                                progressDialog.cancel();
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
                            progressDialog.cancel();
                            //Toast.makeText(activity, getResources().getString(R.string.serversDown), Toast.LENGTH_LONG).show();
                            feedback.setMessage(R.string.serversDown);
                            feedback.setPositiveButton(R.string.ok, null);
                            if (activityCheck != 0)
                                feedback.show();
                            else
                                Toast.makeText(activity, getResources().getString(R.string.serversDown), Toast.LENGTH_LONG).show();
                        } else {
                            progressDialog.cancel();
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
                params.put("contact", contact);
                params.put("block", "n");
                return params;
            }
        };

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null) {
            MySingleton.getInstance(this).addToRequestQueue(stringRequest);
        } else
            Toast.makeText(activity, getResources().getString(R.string.notConnected), Toast.LENGTH_LONG).show();
    }
}

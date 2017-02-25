package com.fussroll.fussroll;

import android.*;
import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class ContactsFragment extends Fragment implements ContactsAdapter.ClickListener {

    protected static List<String> finalPhoneNumbers;
    protected static List<String> finalNames;
    private ContactsAdapter contactsAdapter;
    static protected int activityCheck = 1;
    private SharedPreferences sharedPreferences;
    static protected HashMap<String, String> hashMapNamesContacts = new HashMap<>();
    static protected HashMap<String, String> hashMapContactsNames = new HashMap<>();
    CheckForContactsPermission checkForContactsPermission;
    boolean onCreatePerm = false;
    boolean isFragmentVisible = false;
    View view;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        sharedPreferences = getActivity().getSharedPreferences("request", Context.MODE_PRIVATE);
        view = inflater.inflate(R.layout.fragment_contacts, container, false);

        //Checking if we have contacts permission which we need to run the following code
        checkForContactsPermission = new CheckForContactsPermission(getActivity());
        //Log.i("ContactsFragment", "OnCreateView");
        if(checkForContactsPermission.checkStatus() == 1) {
            onCreatePerm = true;
            //Log.i("ContactsFragment", "Permission is granted");
            getPhoneNumbers();
            RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
            contactsAdapter = new ContactsAdapter(getActivity(), finalNames, finalPhoneNumbers);
            contactsAdapter.setClickListener(this);
            recyclerView.setAdapter(contactsAdapter);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(linearLayoutManager);

            //For adding dividers in the list
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
            dividerItemDecoration.setDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.line_divider));
            recyclerView.addItemDecoration(dividerItemDecoration);
        }
        else {
            //Log.i("ContactsFragment", "Permission is not yet granted");
            //Using following variable to prevent dialog box to pop up for asking for permission from onResume() (which will be twice that is, one from onCreate() and other from onResume())
            onCreatePerm = false;
        }

        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onResume();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        return view;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CheckForContactsPermission.MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Log.i("ContactsFragment", "Dialog : Permission is granted");
                    if(!onCreatePerm)
                        onCreatePerm = true;
                    getPhoneNumbers();
                }
                else {
                    //Log.i("ContactsFragment", "Dialog : Permission is not yet granted");
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.contacts_fragment, menu);

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
                List<String> contacts = new ArrayList<>();

                for (int i = 0; i < finalNames.size(); i++) {
                    String tempName = finalNames.get(i).toLowerCase();
                    if (tempName.contains(newText))
                        contacts.add(finalNames.get(i));
                }

                List<String> numbers = new ArrayList<>();

                for (int i = 0; i < contacts.size(); i++) {
                    numbers.add(hashMapNamesContacts.get(contacts.get(i)));
                }

                contactsAdapter.setData(contacts, numbers);
                contactsAdapter.notifyDataSetChanged();

                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                onResume();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser) {
            isFragmentVisible = true;
            HomeActivity.fab.hide();
        }
        else
            isFragmentVisible = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        activityCheck = 1;

        if(isFragmentVisible)
            HomeActivity.fab.hide();

        //Log.i("ContactsFragment", "onResume()");

        //Documented - 1
        if(onCreatePerm) {
            if(checkForContactsPermission.checkStatus() == 1) {
                //Log.i("ContactsFragment", "Permission is granted");
                getPhoneNumbers();
                if(contactsAdapter != null) {
                    Log.i("ContactsFragment", "onResume() - contactsAdapter != null");
                    contactsAdapter.setData(finalNames, finalPhoneNumbers);
                    contactsAdapter.notifyDataSetChanged();
                }
                else {
                    Log.i("ContactsFragment", "onResume() - contactsAdapter == null");
                    getPhoneNumbers();
                    RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
                    contactsAdapter = new ContactsAdapter(getActivity(), finalNames, finalPhoneNumbers);
                    contactsAdapter.setClickListener(this);
                    recyclerView.setAdapter(contactsAdapter);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                    recyclerView.setLayoutManager(linearLayoutManager);

                    //For adding dividers in the list
                    DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
                    dividerItemDecoration.setDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.line_divider));
                    recyclerView.addItemDecoration(dividerItemDecoration);
                }
            }
            else {
                //Log.i("ContactsFragment", "Permission is not yet granted");
            }
        }
        else {
            Log.i("ContactsFragment", "onResume() - onCreatePerm false");
            if(ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                getPhoneNumbers();
                if(contactsAdapter != null) {
                    Log.i("ContactsFragment", "onResume() - onCreatePerm false - contactsAdapter != null");
                    contactsAdapter.setData(finalNames, finalPhoneNumbers);
                    contactsAdapter.notifyDataSetChanged();
                }
                else {
                    Log.i("ContactsFragment", "onResume() - onCreatePerm false - contactsAdapter == null");
                    getPhoneNumbers();
                    RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
                    contactsAdapter = new ContactsAdapter(getActivity(), finalNames, finalPhoneNumbers);
                    contactsAdapter.setClickListener(this);
                    recyclerView.setAdapter(contactsAdapter);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                    recyclerView.setLayoutManager(linearLayoutManager);

                    //For adding dividers in the list
                    DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
                    dividerItemDecoration.setDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.line_divider));
                    recyclerView.addItemDecoration(dividerItemDecoration);
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        activityCheck = 0;
    }

    protected void getPhoneNumbers() {

            finalPhoneNumbers = new ArrayList<>();
            finalNames = new ArrayList<>();

            //Getting all contacts
            ContentResolver contentResolver = getActivity().getContentResolver();
            Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                        Cursor cursor1 = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{id}, null);
                        if (cursor1 != null) {
                            while (cursor1.moveToNext()) {
                                String phoneNumber = cursor1.getString(cursor1.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                //Documented - 2
                                phoneNumber = phoneNumber.replaceAll("[\\(\\)\\- ]", "");
                                if (phoneNumber.matches(getActivity().getResources().getString(R.string.regexPhoneNumber)) && !phoneNumber.equals("0000000000")) {
                                    //System.out.println(name + " * " + phoneNumber);
                                    finalPhoneNumbers.add(phoneNumber);
                                    finalNames.add(name);
                                }
                            }
                        }
                        if (cursor1 != null) {
                            cursor1.close();
                        }

                    }
                } while (cursor.moveToNext());
            }
            if (cursor != null) {
                cursor.close();
            }

            /*//Removing numbers which are appearing twice or for more number of times - Caution : two different numbers with same name scenario is an issue - FCFS is used
            String[] finalNamesString = finalNames.toArray(new String[finalNames.size()]);
            String[] finalPhoneNumbersString = finalPhoneNumbers.toArray(new String[finalPhoneNumbers.size()]);

            for (int i = 0; i < finalNamesString.length; i++) {
                if (i != finalNamesString.length - 1) {
                    for (int j = i + 1; j < finalNamesString.length; j++) {
                        if (finalNamesString[i].equals(finalNamesString[j])) {
                            finalPhoneNumbers.remove(finalPhoneNumbersString[j]);
                        }
                    }
                }
            }

            //Removing duplicates
            finalNames = new ArrayList<>(new LinkedHashSet<>(finalNames));*/

            //Log.i("contacts", "Refreshed names list from phone #" +finalNames.toString()+"#"+finalNames.size());
            //Log.i("contacts", "Refreshed numbers list from phone #" +finalPhoneNumbers+"#"+finalPhoneNumbers.size());

            //Creating them for further mapping when required
            for (int i = 0; i < finalNames.size(); i++) {
                //System.out.println(finalNames.get(i)+" "+finalPhoneNumbers.get(i));
                hashMapNamesContacts.put(finalNames.get(i), finalPhoneNumbers.get(i));
                hashMapContactsNames.put(finalPhoneNumbers.get(i), finalNames.get(i));
            }

            //Log.i("contacts", "Refreshed list from phone #" +finalNames.toString()+"#"+finalNames.size()+" "+finalPhoneNumbers);

            List<String> contactsList = new ArrayList<>(Arrays.asList(sharedPreferences.getString("contacts", "N/A").replaceAll("[\\[\\] ]", "").split(",")));
            String userMobileNumber = sharedPreferences.getString("mobile", "");
            contactsList.remove("N/A");
            contactsList.remove("");

            //Log.i("contacts", "Contacts from SP #"+contactsList.toString()+"#"+contactsList.size());

            //Log.i("contacts", "Checking if there are numbers to add");
            for (String refreshedContact : finalPhoneNumbers) {
                if (!contactsList.contains(refreshedContact) && !userMobileNumber.substring(userMobileNumber.length() - 10).equals(refreshedContact.substring(refreshedContact.length() - 10))) {
                    //Log.i("contacts", "We need to add " + refreshedContact);
                    addRemoveContacts(refreshedContact, 1, false, contactsList);
                }
            }

            //Log.i("contacts", "Checking if there are numbers to remove");
            for (String savedContact : contactsList) {
                if (!finalPhoneNumbers.contains(savedContact)) {
                    //Log.i("contacts", "We need to remove " + savedContact);
                    addRemoveContacts(savedContact, 2, false, contactsList);
                }
            }
    }

    protected void addRemoveContacts(final String contact, int opt, boolean array, final List<String> contactsList) {

        final String mobile = sharedPreferences.getString("mobile", "");
        String uid;

        try {
            uid = AESEncryption.decrypt(sharedPreferences.getString("uid", ""));
        } catch (Exception e) {
            uid = "";
            e.printStackTrace();
        }

        final AlertDialog.Builder feedback = new AlertDialog.Builder(getActivity());
        final String finalUid = uid;

        if (!array && opt == 1) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST,
                    getResources().getString(R.string.contactsAPI),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (jsonObject.getString("statusCode").equals("200")) {
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    List<String> contactList = new ArrayList<>(Arrays.asList(sharedPreferences.getString("contacts", "N/A").replaceAll("[\\[\\] ]", "").split(",")));
                                    contactList.remove("N/A");
                                    contactList.remove("");
                                    if (contactList.toString().equals("[]")) {
                                        //Log.i("contacts", "API add, contactList is equal to []");
                                        editor.putString("contacts", "[" + contact + "]");
                                        contactList.add(contact);
                                    } else {
                                        //Log.i("contacts", "API add, contactList is equal to "+contactList);
                                        contactList.add(contact);
                                        editor.putString("contacts", contactList.toString());
                                    }
                                    editor.apply();
                                    //Log.i("contacts", "added "+contact+" to SP");
                                    //Log.i("contacts", "updated contactList "+contactList);
                                } else if (jsonObject.getString("statusCode").equals("401")) {
                                    //Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                                    final AlertDialog.Builder verifyAgain = new AlertDialog.Builder(getActivity());
                                    verifyAgain.setMessage(R.string.verifyAgain);
                                    verifyAgain.setPositiveButton(getResources().getString(R.string.verify), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putString("registered", "false");
                                            editor.putString("confirmed", "false");
                                            editor.apply();
                                            Intent intent = new Intent(getActivity(), RegisterActivity.class);
                                            startActivity(intent);
                                        }
                                    });
                                    if (activityCheck != 0)
                                        verifyAgain.show();
                                    else
                                        Toast.makeText(getActivity(), getResources().getString(R.string.verify), Toast.LENGTH_LONG).show();
                                } else if(jsonObject.getString("statusCode").equals("404")) {
                                    //Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                                    feedback.setMessage(R.string.couldNotProcess);
                                    feedback.setPositiveButton(R.string.ok, null);
                                    if (activityCheck != 0)
                                        feedback.show();
                                    else
                                        Toast.makeText(getActivity(), getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("mobile", mobile);
                    params.put("uid", finalUid);
                    params.put("contact", contact);
                    params.put("opt", "1");
                    return params;
                }
            };

            final ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            if (networkInfo != null)
                MySingleton.getInstance(getActivity()).addToRequestQueue(stringRequest);

        } else if (!array && opt == 2) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST,
                    getResources().getString(R.string.contactsAPI),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (jsonObject.getString("statusCode").equals("200")) {
                                    List<String> contactList = new ArrayList<>(Arrays.asList(sharedPreferences.getString("contacts", "N/A").replaceAll("[\\[\\] ]", "").split(",")));
                                    List<String> notOnFussrollList = new ArrayList<>(Arrays.asList(sharedPreferences.getString("notOnFussroll", "N/A").replaceAll("[\\[\\] ]", "").split(",")));
                                    notOnFussrollList.remove("");
                                    contactList.remove("N/A");
                                    contactList.remove("");
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    //Log.i("contacts", "API remove, contactList is equal to "+contactList);
                                    contactList.remove(contact);
                                    editor.putString("contacts", contactList.toString());

                                    //Log.i("contacts", "removed "+contact+" from SP");
                                    //Log.i("contacts", "updated contactList "+contactList);
                                    if (notOnFussrollList.contains(contact)) {
                                        //Log.i("contacts", contact+" was not on Fussroll so removing from notOnFussroll list too because user is no more in contacts list");
                                        notOnFussrollList.remove(contact);
                                        editor.putString("notOnFussroll", notOnFussrollList.toString());
                                        //Log.i("contacts", "updated notOnFussroll list is "+notOnFussrollList.toString());
                                    }
                                    editor.apply();
                                } else if (jsonObject.getString("statusCode").equals("401")) {
                                    //Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                                    final AlertDialog.Builder verifyAgain = new AlertDialog.Builder(getActivity());
                                    verifyAgain.setMessage(R.string.verifyAgain);
                                    verifyAgain.setPositiveButton(getResources().getString(R.string.verify), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putString("registered", "false");
                                            editor.putString("confirmed", "false");
                                            editor.apply();
                                            Intent intent = new Intent(getActivity(), RegisterActivity.class);
                                            startActivity(intent);
                                        }
                                    });
                                    if (activityCheck != 0)
                                        verifyAgain.show();
                                    else
                                        Toast.makeText(getActivity(), getResources().getString(R.string.verify), Toast.LENGTH_LONG).show();
                                } else if(jsonObject.getString("statusCode").equals("404")) {
                                    //Toast.makeText(activity, getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                                    feedback.setMessage(R.string.couldNotProcess);
                                    feedback.setPositiveButton(R.string.ok, null);
                                    if (activityCheck != 0)
                                        feedback.show();
                                    else
                                        Toast.makeText(getActivity(), getResources().getString(R.string.serverError), Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("mobile", mobile);
                    params.put("uid", finalUid);
                    params.put("contact", contact);
                    params.put("opt", "2");
                    return params;
                }
            };
            final ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            if (networkInfo != null)
                MySingleton.getInstance(getActivity()).addToRequestQueue(stringRequest);

        }
    }

    @Override
    public void onItemClick(View view, int position, String phoneNumber, String contactName) {
        AppCompatButton appCompatButton = (AppCompatButton) view.findViewById(R.id.appCompatButton);
        String mobile = sharedPreferences.getString("mobile", "N/A");

        //Country specific check in second condition
        if (appCompatButton.getVisibility() != View.VISIBLE && !mobile.substring(mobile.length() - 10).equals(phoneNumber)) {
            Intent intent = new Intent(getActivity(), ActivitiesActivity.class);
            intent.putExtra(ActivitiesActivity.userName, contactName);
            intent.putExtra(ActivitiesActivity.userPhoneNumber, phoneNumber);
            startActivity(intent);
        }
    }

    @Override
    public void onItemInviteClick(String contactName, String phoneNumber) {

        String[] name = contactName.split(" ");
        String message = "Hey " + name[0] + getString(R.string.inviteToJoin);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.subjectForInvitation));
        intent.putExtra(Intent.EXTRA_TEXT, message);
        intent.putExtra(Intent.EXTRA_PHONE_NUMBER, phoneNumber);
        Intent chooserIntent = Intent.createChooser(intent, getString(R.string.inviteBoxTitle));
        getActivity().startActivity(chooserIntent);

    }
}

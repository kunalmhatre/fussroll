package com.fussroll.fussroll;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ContactsActivity extends AppCompatActivity implements ContactsListAdapter.ClickListener{

    private List<String> blockThem = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        AppCompatButton appCompatButton = (AppCompatButton) findViewById(R.id.button);
        appCompatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                block();
            }
        });

        CheckForContactsPermission checkForContactsPermission = new CheckForContactsPermission(this);

        if(checkForContactsPermission.checkStatus() == 1) {

            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
            RefreshContacts refreshContacts = new RefreshContacts();
            refreshContacts.refreshContacts(getApplicationContext());

            SharedPreferences sharedPreferences = getSharedPreferences("request",MODE_PRIVATE);
            List<String> blockedContacts = new ArrayList<>(Arrays.asList(sharedPreferences.getString("blockedContacts", "").replaceAll("[\\[\\] ]", "").split(",")));
            //System.out.println(blockedContacts);
            //System.out.println(refreshContacts.finalPhoneNumbers);

            List<String> names = new ArrayList<>();
            List<String> phoneNumbers = new ArrayList<>();

            for(int i = 0; i < refreshContacts.finalPhoneNumbers.size(); i++) {
                if(!blockedContacts.contains(refreshContacts.finalPhoneNumbers.get(i))) {
                    names.add(refreshContacts.finalNames.get(i));
                    phoneNumbers.add(refreshContacts.finalPhoneNumbers.get(i));
                }
            }

            //System.out.println(names+" "+phoneNumbers);

            ContactsListAdapter contactsListAdapter = new ContactsListAdapter(this, names, phoneNumbers);
            contactsListAdapter.setClickListener(this);
            recyclerView.setAdapter(contactsListAdapter);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(linearLayoutManager);

            //For adding dividers in the list
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
            dividerItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.line_divider));
            recyclerView.addItemDecoration(dividerItemDecoration);

        }

    }

    @Override
    public void onItemClick(String name, String contact, int position) {
        if(blockThem.contains(contact))
            blockThem.remove(contact);
        else
            blockThem.add(contact);
    }

    public void block() {
        Intent intent = new Intent();
        intent.putExtra("contacts", blockThem.toString());
        setResult(1337, intent);
        finish();
    }

}

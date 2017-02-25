package com.fussroll.fussroll;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

class RefreshContacts {

    List<String> finalPhoneNumbers;
    List<String> finalNames;
    private HashMap<String, String> hashMapNamesContacts = new HashMap<>();
    private HashMap<String, String> hashMapContactsNames = new HashMap<>();

    void refreshContacts(Context context) {

            finalPhoneNumbers = new ArrayList<>();
            finalNames = new ArrayList<>();

            //Getting all contacts
            ContentResolver contentResolver = context.getContentResolver();
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
                                phoneNumber = phoneNumber.replaceAll("[\\(\\)\\- ]", "");
                                if (phoneNumber.matches(context.getResources().getString(R.string.regexPhoneNumber)) && !phoneNumber.equals("0000000000")) {
                                    //System.out.println(name + " " + phoneNumber);
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

            /*String[] finalNamesString = finalNames.toArray(new String[finalNames.size()]);
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

            finalNames = new ArrayList<>(new LinkedHashSet<>(finalNames));*/

            for(int i = 0; i < finalNames.size(); i++) {
                //System.out.println(finalNames.get(i)+" "+finalPhoneNumbers.get(i));
                hashMapNamesContacts.put(finalNames.get(i), finalPhoneNumbers.get(i));
                hashMapContactsNames.put(finalPhoneNumbers.get(i), finalNames.get(i));
            }
    }

    HashMap<String, String> getHashMapContactsNames() {
        return hashMapContactsNames;
    }

    HashMap<String, String> getHashMapNamesContacts() {
        return hashMapNamesContacts;
    }
}

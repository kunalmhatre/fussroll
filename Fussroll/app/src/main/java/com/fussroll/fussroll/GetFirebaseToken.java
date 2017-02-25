package com.fussroll.fussroll;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;

/**
 * Created by kunal on 28/1/17.
 */

public class GetFirebaseToken extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("Firebase", "Token is : "+refreshedToken);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("firebaseToken", refreshedToken);
        editor.putString("firebaseTokenRefreshed", "false");
        editor.apply();

    }

}

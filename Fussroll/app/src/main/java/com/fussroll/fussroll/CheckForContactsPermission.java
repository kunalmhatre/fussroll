package com.fussroll.fussroll;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import java.security.Permission;

/**
 * Created by kunal on 13/2/17.
 */

class CheckForContactsPermission {

    static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    private final Activity activity;

    CheckForContactsPermission(Activity activity) {
        this.activity = activity;
    }

    int checkStatus() {

        if(ContextCompat.checkSelfPermission(activity, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

            //Should we explain?
            if(ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_CONTACTS)) {

                AlertDialog.Builder explainDialog = new AlertDialog.Builder(activity);
                explainDialog.setMessage(R.string.explainWhyContactsPermission);
                explainDialog.setPositiveButton(R.string.givePermission, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                    }
                });
                explainDialog.show();
                /*final Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:"+activity.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                activity.startActivity(intent);*/
                return 3;
            }
            else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                return 2;
            }

        }
        else
            return 1;

    }



}

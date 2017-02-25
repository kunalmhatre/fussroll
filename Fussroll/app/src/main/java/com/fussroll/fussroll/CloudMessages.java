package com.fussroll.fussroll;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class CloudMessages extends FirebaseMessagingService {

    final static String GROUP_KEY_NOTIFICATIONS = "group_key_notifications";
    static List<SpannableString> notificationList = new ArrayList<>();

    public CloudMessages() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String, String> map = remoteMessage.getData();
        String mobile = map.get("mobile");
        String category = map.get("category");
        String meta = map.get("meta");
        String log = map.get("log");
        String date = map.get("date");
        String time = map.get("time");
        //Log.i("firebase", "Message received : "+mobile+" "+category+" "+meta+" "+log+" "+date+" "+time);

        //Checking if the data is valid
        CategoryLogMetaHelper categoryLogMetaHelper = new CategoryLogMetaHelper(category, log, meta);
        boolean validData = categoryLogMetaHelper.validate();
        //Log.i("people", "validData : "+validData);

        if(validData) {

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat getLocalDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat getLocalTime = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            String localDate, localTime;
            Date dateObject = null;

            try {
                dateObject = dateFormat.parse(date+" "+time);
            } catch (Exception e) {
                e.printStackTrace();
            }

            localDate = getLocalDate.format(dateObject);
            localTime = getLocalTime.format(dateObject);

            //Log.i("people", "utcDate and utcTime : "+date+" "+time);
            //Log.i("people", "localDate and localTime : "+localDate+" "+localTime);

            int imageIcon = categoryLogMetaHelper.getImageID();

            //Log.i("people", "ImageIcon : "+imageIcon);

            DatabaseHandler databaseHandler = new DatabaseHandler(this);
            databaseHandler.addLogPeople(mobile, category, log, meta, localDate, localTime, date, time, imageIcon);

            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {

                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("request", MODE_PRIVATE);
                List<String> mutedList = new ArrayList<>(Arrays.asList(sharedPreferences.getString("muted", "N/A").replaceAll("[\\[\\] ]", "").split(",")));
                String userMobile = sharedPreferences.getString("mobile","N/A");
                String notificationOption = sharedPreferences.getString("notification","N/A");
                RefreshContacts refreshContacts = new RefreshContacts();
                refreshContacts.refreshContacts(getApplicationContext());

                boolean flag = false;

                if(Integer.parseInt(notificationOption) == 0) {

                    if(!mutedList.contains(mobile)) {

                        String name = "";

                        HashMap<String, String> hashMap = refreshContacts.getHashMapContactsNames();

                        if(!mobile.substring(0, mobile.length()-10).equals(userMobile.substring(0, mobile.length()-10)))
                            flag = true; //Which means they are not from same country

                        if(flag) {

                            String mobile_1 = mobile.substring(1, mobile.length());

                            //Log.i("notification", mobile+" "+mobile_1);

                            for(String phoneNumber : refreshContacts.finalPhoneNumbers) {
                                if(phoneNumber.equals(mobile) || phoneNumber.equals(mobile_1)) {
                                    name = hashMap.get(phoneNumber);
                                    break;
                                }
                                else
                                    name = phoneNumber;
                            }

                        }
                        else {

                            String mobile_1 = mobile.substring(mobile.length()-10);
                            String mobile_2 = mobile.substring(1, mobile.length());
                            String mobile_3 = '0'+mobile.substring(mobile.length()-10);

                            //Log.i("notification", mobile+" "+mobile_1+" "+mobile_2+" "+mobile_3);

                            for(String phoneNumber : refreshContacts.finalPhoneNumbers) {
                                if(phoneNumber.equals(mobile) || phoneNumber.equals(mobile_1) || phoneNumber.equals(mobile_2) || phoneNumber.equals(mobile_3)) {
                                    name = hashMap.get(phoneNumber);
                                    break;
                                }
                                else {
                                    name = phoneNumber;
                                }
                            }

                        }

                        //Log.i("notification", "Notification for "+name);

                        if(category.equals("activity")) {
                            SpannableString spannableString = new SpannableString(name+"  "+log);
                            spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            notificationList.add(spannableString);
                        }
                        else {
                            SpannableString spannableString = new SpannableString(name+"  "+meta+" "+log);
                            spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            notificationList.add(spannableString);
                        }

                        android.support.v7.app.NotificationCompat.InboxStyle inboxStyle = new android.support.v7.app.NotificationCompat.InboxStyle();
                        //Log.i("notification", "size "+notificationList.size());

                        if(notificationList.size() == 1) {
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                inboxStyle.setSummaryText(Integer.toString(notificationList.size()) + " new update");
                            }
                            else {
                                inboxStyle.setBigContentTitle("Fussroll");
                                inboxStyle.setSummaryText(Integer.toString(notificationList.size()) + " new update");
                            }
                        }
                        else {
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                inboxStyle.setSummaryText(Integer.toString(notificationList.size())+" new updates");
                            }
                            else {
                                inboxStyle.setBigContentTitle("Fussroll");
                                inboxStyle.setSummaryText(Integer.toString(notificationList.size())+" new updates");
                            }
                        }

                        int counter = 7;
                        for(int i = notificationList.size()-1; i >= 0; i--) {
                            if(counter >= 1) {
                                inboxStyle.addLine(notificationList.get(i));
                            }
                            counter--;
                        }

                        Intent intent = new Intent(this, HomeActivity.class);
                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                        stackBuilder.addParentStack(HomeActivity.class);
                        stackBuilder.addNextIntent(intent);
                        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                        if(isFussrollRunning()) {
                            Notification summaryNotification;
                            assert dateObject != null;
                            summaryNotification = new NotificationCompat.Builder(this)
                                    .setSmallIcon(R.mipmap.fussroll_notification_logo)
                                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), imageIcon))
                                    .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                                    .setStyle(inboxStyle)
                                    .setGroup(GROUP_KEY_NOTIFICATIONS)
                                    .setGroupSummary(true)
                                    .setWhen(dateObject.getTime())
                                    .setContentIntent(pendingIntent)
                                    .setAutoCancel(true)
                                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                    .build();


                            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager.notify(1337, summaryNotification);
                        }

                    }
                    //else
                        //Log.i("people", "This mobile number is muted");

                }
                //else
                    //Log.i("people", "User opted for no notifications at all");

            }

        }

    }

    boolean isFussrollRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = manager.getRunningAppProcesses();
        for(ActivityManager.RunningAppProcessInfo processInfo : runningAppProcesses) {
            if(processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for(String activeProcess : processInfo.pkgList) {
                    if(activeProcess.equals(getPackageName()))
                        return false;
                }
            }
        }
        return true;
    }

}

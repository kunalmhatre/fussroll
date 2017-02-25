package com.fussroll.fussroll;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "fussroll";
    private static final String TABLE_LOGS = "logs";
    private static final String TABLE_ACTIVITIES = "activities";
    private static final String TABLE_PEOPLE = "people";

    //Table columns
    private static final String CATEGORY = "category";
    private static final String LOG = "log";
    private static final String META = "meta";
    private static final String DATE = "dateFromServer";
    private static final String TIME = "timeFromServer";
    private static final String LOG_IMAGE = "logImage";
    private static final String USER_PHONE_NUMBER = "userPhoneNumber";
    private static final String LOCAL_DATE = "localDate";
    private static final String LOCAL_TIME = "localTime";
    private static final String UTC_DATE = "UTCDate";
    private static final String UTC_TIME = "UTCTime";

    DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_LOGS_TABLE = "create table "+TABLE_LOGS+"("+
                "id integer primary key autoincrement,"+
                CATEGORY+" TEXT,"+
                LOG+" TEXT,"+
                META+" TEXT,"+
                DATE+" TEXT,"+
                TIME+" TEXT,"+
                LOG_IMAGE+" INTEGER)";

        String CREATE_ACTIVITIES_TABLE = "create table "+TABLE_ACTIVITIES+"("+
                "id integer primary key autoincrement,"+
                USER_PHONE_NUMBER+" TEXT,"+
                CATEGORY+" TEXT,"+
                LOG+" TEXT,"+
                META+" TEXT,"+
                LOCAL_DATE+" TEXT,"+
                LOCAL_TIME+" TEXT,"+
                UTC_DATE+" TEXT,"+
                UTC_TIME+" TEXT,"+
                LOG_IMAGE+" INTEGER)";

        String CREATE_PEOPLE_TABLE = "create table "+TABLE_PEOPLE+"("+
                "id integer primary key autoincrement,"+
                USER_PHONE_NUMBER+" TEXT,"+
                CATEGORY+" TEXT,"+
                LOG+" TEXT,"+
                META+" TEXT,"+
                LOCAL_DATE+" TEXT,"+
                LOCAL_TIME+" TEXT,"+
                UTC_DATE+" TEXT,"+
                UTC_TIME+" TEXT,"+
                LOG_IMAGE+" INTEGER)";

        //Log.i("database", "onCreate : "+CREATE_LOGS_TABLE);
        //Log.i("database", "onCreate : "+CREATE_ACTIVITIES_TABLE);
        //Log.i("database", "onCreate : "+CREATE_PEOPLE_TABLE);

        db.execSQL(CREATE_LOGS_TABLE);
        db.execSQL(CREATE_ACTIVITIES_TABLE);
        db.execSQL(CREATE_PEOPLE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS "+TABLE_LOGS);
        onCreate(db);

    }

    //For logs table
    void addLog(String category, String log, String meta, String date, String time, int logImage) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(CATEGORY, category);
        contentValues.put(LOG, log);
        contentValues.put(META, meta);
        contentValues.put(DATE, date);
        contentValues.put(TIME, time);
        contentValues.put(LOG_IMAGE, logImage);

        db.insert(TABLE_LOGS, null, contentValues);
        db.close();
    }

    //For activities table
    void addLog(String userPhoneNumber, String category, String log, String meta, String localDate, String localTime, String utcDate, String utcTime, int logImage) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(USER_PHONE_NUMBER, userPhoneNumber);
        contentValues.put(CATEGORY, category);
        contentValues.put(LOG, log);
        contentValues.put(META, meta);
        contentValues.put(LOCAL_DATE, localDate);
        contentValues.put(LOCAL_TIME, localTime);
        contentValues.put(UTC_DATE, utcDate);
        contentValues.put(UTC_TIME, utcTime);
        contentValues.put(LOG_IMAGE, logImage);

        db.insert(TABLE_ACTIVITIES, null, contentValues);
        db.close();
    }

    //For people table
    void addLogPeople(String userPhoneNumber, String category, String log, String meta, String localDate, String localTime, String utcDate, String utcTime, int logImage) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(USER_PHONE_NUMBER, userPhoneNumber);
        contentValues.put(CATEGORY, category);
        contentValues.put(LOG, log);
        contentValues.put(META, meta);
        contentValues.put(LOCAL_DATE, localDate);
        contentValues.put(LOCAL_TIME, localTime);
        contentValues.put(UTC_DATE, utcDate);
        contentValues.put(UTC_TIME, utcTime);
        contentValues.put(LOG_IMAGE, logImage);

        db.insert(TABLE_PEOPLE, null, contentValues);
        db.close();
    }

    List<Logs> getLogs() {
        SQLiteDatabase db = this.getWritableDatabase();
        List<Logs> listLogs = new ArrayList<>();
        String selectQuery = "SELECT * FROM "+TABLE_LOGS+" ORDER BY id desc";
        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.moveToFirst()) {
            do{
                Logs logs = new Logs(cursor.getString(1),
                        cursor.getString(2), cursor.getString(3),
                        cursor.getString(4), cursor.getString(5), cursor.getInt(6));
                listLogs.add(logs);
            }while(cursor.moveToNext());
        }
        cursor.close();
        return listLogs;
    }

    List<Logs> getPeopleLogs() {
        SQLiteDatabase db = this.getWritableDatabase();
        List<Logs> listLogs = new ArrayList<>();
        String selectQuery = "SELECT * FROM "+TABLE_PEOPLE+" ORDER BY id desc";
        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.moveToFirst() || cursor.getCount() != 0) {
            do{
                Logs logs = new Logs(cursor.getString(1),
                        cursor.getString(2), cursor.getString(3),
                        cursor.getString(4), cursor.getString(5),
                        cursor.getString(6), cursor.getString(7),
                        cursor.getString(8), cursor.getInt(9));
                listLogs.add(logs);
            }while(cursor.moveToNext());
        }
        else {
            cursor.close();
            return null;
        }
        cursor.close();
        return listLogs;
    }

    List<Logs> getActivities(String userPhoneNumber) {
        SQLiteDatabase db = this.getWritableDatabase();
        List<Logs> listLogs = new ArrayList<>();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String todaysDate = dateFormat.format(new Date());

        String selectQueryTodaysDate = "SELECT * FROM "+TABLE_ACTIVITIES+" WHERE "+USER_PHONE_NUMBER+"='"+userPhoneNumber+"' and "+LOCAL_DATE+"='"+todaysDate+"' ORDER BY id desc";
        //Log.i("database", "getActivities of today's date : "+selectQueryTodaysDate);

        String selectQueryMaxLocalDate = "SELECT * FROM "+TABLE_ACTIVITIES+" WHERE "+USER_PHONE_NUMBER+"='"+userPhoneNumber+"' and "+LOCAL_DATE+"= (SELECT MAX("+LOCAL_DATE+") from "+TABLE_ACTIVITIES+" where "+USER_PHONE_NUMBER+"='"+userPhoneNumber+"') ORDER BY id desc";
        //Log.i("database", "getActivities of max local date : "+selectQueryMaxLocalDate);

        Cursor cursor = db.rawQuery(selectQueryTodaysDate, null);
        if(!(cursor.moveToFirst()) || cursor.getCount() == 0) {
            //Log.i("database", "getActivities has nothing for this user - todays date now checking if there are any of past date");
            Cursor cursor1 = db.rawQuery(selectQueryMaxLocalDate, null);
            if(!(cursor1.moveToFirst()) || cursor1.getCount() == 0) {
                //Log.i("database", "getActivities has nothing for this user even for past date - make textview to nothing updated yet");
                return null;
            }
            else {
                if(cursor1.moveToFirst()) {
                    do{
                        Logs logs = new Logs(cursor1.getString(1),
                                cursor1.getString(2), cursor1.getString(3),
                                cursor1.getString(4), cursor1.getString(5),
                                cursor1.getString(6), cursor1.getString(7),
                                cursor1.getString(8), cursor1.getInt(9));
                        listLogs.add(logs);
                    }while(cursor1.moveToNext());
                }
                cursor1.close();
                return listLogs;
            }
        }
        else {
            if(cursor.moveToFirst()) {
                do{
                    Logs logs = new Logs(cursor.getString(1),
                            cursor.getString(2), cursor.getString(3),
                            cursor.getString(4), cursor.getString(5),
                            cursor.getString(6), cursor.getString(7),
                            cursor.getString(8), cursor.getInt(9));
                    listLogs.add(logs);
                }while(cursor.moveToNext());
            }
            cursor.close();
            return listLogs;
        }
    }

    String[] getMAXUTCDateTime(String userPhoneNumber) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] response;
        String selectQuery = "SELECT MAX("+UTC_DATE+"), MAX("+UTC_TIME+") FROM "+TABLE_ACTIVITIES+" WHERE "+USER_PHONE_NUMBER+" = '"+userPhoneNumber+"' and "+UTC_DATE+" = (select MAX("+UTC_DATE+") from "+TABLE_ACTIVITIES+" where "+USER_PHONE_NUMBER+"='"+userPhoneNumber+"')";
        //Log.i("database", "getMAXUTCDateTime : "+selectQuery);

        Cursor cursor = db.rawQuery(selectQuery, null);
        if(!(cursor.moveToFirst()) || cursor.getCount() == 0 ) {
            //Log.i("database", "getLatestUTCDateTime has nothing for this user");
            return new String[]{"",""};
        }
        else {
            cursor.moveToFirst();
            if(cursor.getString(0) == null || cursor.getString(1) == null)
                return new String[]{"",""};
            else {
                //Log.i("database", "getLatestUTCDateTime has something for this user : "+cursor.getString(0)+" "+cursor.getString(1));
                response = new String[]{cursor.getString(0), cursor.getString(1)};
                cursor.close();
                return response;
            }
        }
    }

    void wipeAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        String deleteTableLogs = "DELETE FROM "+TABLE_LOGS;
        String deleteTableActivities = "DELETE FROM "+TABLE_ACTIVITIES;
        String deleteTablePeople = "DELETE FROM "+TABLE_PEOPLE;
        db.execSQL(deleteTableLogs);
        db.execSQL(deleteTableActivities);
        db.execSQL(deleteTablePeople);
    }
}

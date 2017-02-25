package com.fussroll.fussroll;

/**
 * Created by kunal on 7/1/17.
 */

class Logs {
    private String category, log, meta, date, time, userPhoneNumber, utcDate, utcTime;
    private int logImage;

    Logs(String category, String log, String meta, String date, String time, int logImage) {
        this.category = category;
        this.log = log;
        this.meta = meta;
        this.date = date;
        this.time = time;
        this.logImage = logImage;
    }

    Logs(String userPhoneNumber, String category, String log, String meta, String localDate, String localTime, String utcDate, String utcTime, int logImage) {

        this.userPhoneNumber = userPhoneNumber;
        this.category = category;
        this.log = log;
        this.meta = meta;
        this.date = localDate;
        this.time = localTime;
        this.utcDate = utcDate;
        this.utcTime = utcTime;
        this.logImage = logImage;

    }

    String getCategory() {
        return category;
    }

    String getLog() {
        return log;
    }

    String getMeta() {
        return meta;
    }

    String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    int getLogImage() {
        return logImage;
    }

    String getUserPhoneNumber() {
        return userPhoneNumber;
    }

    String getUtcDate() {
        return utcDate;
    }

    String getUtcTime() {
        return utcTime;
    }
}

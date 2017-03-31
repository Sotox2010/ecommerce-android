package com.smartsecurity.android.piudonnacouture.util;

import com.google.gson.JsonObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {

    private static DateFormat sRfc3339Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
    private static DateFormat sMySqlDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private static DateFormat sOrderDateFormat = new SimpleDateFormat("dd'\n'MMM", Locale.US);
    private static DateFormat sOrderDetailDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
    private static DateFormat sCartItemDateFormat = new SimpleDateFormat("MMM dd", Locale.US);

    /*
     * MySQL Date JSON Object:
     *
     * "date": {
     *  "date": "2016-03-18 13:29:36",
     *  "timezone_type": 3,
     *  "timezone": "America/Chicago"
     * }
     * */
    private static Date getDateFromMySqlDate(JsonObject dateObject) throws ParseException {
        String dateString = dateObject.get("date").getAsString();
        String zoneId = dateObject.get("timezone").getAsString();

        TimeZone tz = TimeZone.getTimeZone(zoneId);
        sMySqlDateFormat.setTimeZone(tz);

        return sMySqlDateFormat.parse(dateString);
    }

    public static String getRfc3339DateFromMySqlObject(JsonObject dateObject) {
        try {
            Date date = getDateFromMySqlDate(dateObject);
            return sRfc3339Format.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getCurrentRfc3339Date() {
        return sRfc3339Format.format(new Date());
    }

    public static String getOrderDateFromRfc3339(String rfc3339Date) {
        try {
            Date date = sRfc3339Format.parse(rfc3339Date);
            return sOrderDateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getOrderDateFromMySqlDate(JsonObject dateObject) {
        try {
            Date date = getDateFromMySqlDate(dateObject);
            return sOrderDateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getOrderDetailDateFromMySqlDate(JsonObject dateObject) {
        try {
            Date date = getDateFromMySqlDate(dateObject);
            return sOrderDetailDateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getCartItemDateFromRfc3339(String rfc3339Date) {
        try {
            Date date = sRfc3339Format.parse(rfc3339Date);
            return sCartItemDateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

}

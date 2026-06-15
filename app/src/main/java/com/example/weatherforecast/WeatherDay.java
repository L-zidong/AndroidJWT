package com.example.weatherforecast;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class WeatherDay {
    public static final String COL_ID = "_id";
    public static final String COL_DATE = "date";
    public static final String COL_SUMMARY = "summary";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_TEMP_HIGH_C = "temp_high_c";
    public static final String COL_TEMP_LOW_C = "temp_low_c";
    public static final String COL_HUMIDITY = "humidity";
    public static final String COL_WIND = "wind";
    public static final String COL_CITY = "city";
    public static final String COL_LAT = "latitude";
    public static final String COL_LON = "longitude";

    private long id;
    private String date;
    private String summary;
    private String description;
    private double tempHighC;
    private double tempLowC;
    private int humidity;
    private String wind;
    private String city;
    private double latitude;
    private double longitude;

    public WeatherDay() {
    }

    public WeatherDay(String date, String summary, String description,
                      double tempHighC, double tempLowC, int humidity, String wind,
                      String city, double latitude, double longitude) {
        this.date = date;
        this.summary = summary;
        this.description = description;
        this.tempHighC = tempHighC;
        this.tempLowC = tempLowC;
        this.humidity = humidity;
        this.wind = wind;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static WeatherDay fromCursor(Cursor cursor) {
        WeatherDay day = new WeatherDay();
        day.id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID));
        day.date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE));
        day.summary = cursor.getString(cursor.getColumnIndexOrThrow(COL_SUMMARY));
        day.description = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION));
        day.tempHighC = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TEMP_HIGH_C));
        day.tempLowC = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TEMP_LOW_C));
        day.humidity = cursor.getInt(cursor.getColumnIndexOrThrow(COL_HUMIDITY));
        day.wind = cursor.getString(cursor.getColumnIndexOrThrow(COL_WIND));
        day.city = cursor.getString(cursor.getColumnIndexOrThrow(COL_CITY));
        day.latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LAT));
        day.longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LON));
        return day;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(COL_DATE, date);
        values.put(COL_SUMMARY, summary);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_TEMP_HIGH_C, tempHighC);
        values.put(COL_TEMP_LOW_C, tempLowC);
        values.put(COL_HUMIDITY, humidity);
        values.put(COL_WIND, wind);
        values.put(COL_CITY, city);
        values.put(COL_LAT, latitude);
        values.put(COL_LON, longitude);
        return values;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public String getSummary() {
        return summary;
    }

    public String getDescription() {
        return description;
    }

    public double getTempHighC() {
        return tempHighC;
    }

    public double getTempLowC() {
        return tempLowC;
    }

    public int getHumidity() {
        return humidity;
    }

    public String getWind() {
        return wind;
    }

    public String getCity() {
        return city;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String formatTempRange(Context context, boolean useFahrenheit) {
        if (useFahrenheit) {
            return context.getString(R.string.temp_range_fahrenheit, formatF(tempLowC), formatF(tempHighC));
        }
        return context.getString(R.string.temp_range_celsius, (int) tempLowC, (int) tempHighC);
    }

    public String formatDetailTemps(Context context, boolean useFahrenheit) {
        if (useFahrenheit) {
            return context.getString(R.string.detail_temps_fahrenheit, formatF(tempHighC), formatF(tempLowC));
        }
        return context.getString(R.string.detail_temps_celsius, (int) tempHighC, (int) tempLowC);
    }

    private int formatF(double celsius) {
        return (int) Math.round(celsius * 9.0 / 5.0 + 32);
    }

    public String getShareText(Context context, boolean useFahrenheit) {
        return context.getString(
                R.string.share_weather_body,
                city,
                date,
                summary,
                description,
                formatDetailTemps(context, useFahrenheit),
                humidity,
                wind);
    }
}

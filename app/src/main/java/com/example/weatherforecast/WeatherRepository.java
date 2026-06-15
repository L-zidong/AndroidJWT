package com.example.weatherforecast;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class WeatherRepository {
    private static final String TAG = "WeatherRepository";
    private final Context context;
    private final WeatherDbHelper dbHelper;

    public WeatherRepository(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = new WeatherDbHelper(this.context);
    }

    public List<WeatherDay> loadForecast(String city, boolean forceNetwork) {
        if (forceNetwork || isNetworkAvailable()) {
            saveToDatabase(fetchFromNetwork(city));
            return loadFromDatabase(city);
        }
        List<WeatherDay> cached = loadFromDatabase(city);
        if (!cached.isEmpty()) {
            return cached;
        }
        saveToDatabase(fetchFromNetwork(city));
        return loadFromDatabase(city);
    }

    public boolean usesQWeatherApi() {
        return QWeatherApi.isConfigured() && QWeatherJwt.hasPrivateKeyAsset(context);
    }

    private List<WeatherDay> fetchFromNetwork(String city) {
        if (QWeatherApi.isConfigured() && QWeatherJwt.hasPrivateKeyAsset(context)) {
            try {
                return QWeatherApi.fetchForecast(context, city);
            } catch (Exception e) {
                Log.w(TAG, "QWeather request failed, fallback to mock", e);
            }
        }
        return MockWeatherApi.fetchForecast(city);
    }

    public boolean isNetworkAvailable() {
        return checkNetwork();
    }

    public WeatherDay getById(long id) {
        Cursor cursor = context.getContentResolver().query(
                WeatherContentProvider.CONTENT_URI,
                null,
                WeatherDay.COL_ID + "=?",
                new String[]{String.valueOf(id)},
                null
        );
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    return WeatherDay.fromCursor(cursor);
                }
            } finally {
                cursor.close();
            }
        }
        return null;
    }

    private void saveToDatabase(List<WeatherDay> days) {
        if (days.isEmpty()) {
            return;
        }
        String city = days.get(0).getCity();
        dbHelper.getWritableDatabase().delete(
                WeatherDbHelper.TABLE_WEATHER,
                WeatherDay.COL_CITY + "=?",
                new String[]{city});
        ContentResolver resolver = context.getContentResolver();
        for (WeatherDay day : days) {
            resolver.insert(WeatherContentProvider.CONTENT_URI, day.toContentValues());
        }
    }

    private List<WeatherDay> loadFromDatabase(String city) {
        List<WeatherDay> list = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(
                WeatherContentProvider.CONTENT_URI,
                null,
                WeatherDay.COL_CITY + "=?",
                new String[]{city},
                WeatherDay.COL_ID + " ASC"
        );
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    list.add(WeatherDay.fromCursor(cursor));
                }
            } finally {
                cursor.close();
            }
        }
        return list;
    }

    private boolean checkNetwork() {
        ConnectivityManager cm = context.getSystemService(ConnectivityManager.class);
        if (cm == null) {
            return false;
        }
        NetworkCapabilities caps = cm.getNetworkCapabilities(cm.getActiveNetwork());
        return caps != null && (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
    }
}

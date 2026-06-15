package com.example.weatherforecast;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WeatherDbHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "weather.db";
    public static final String TABLE_WEATHER = "weather_forecast";
    private static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE =
            "CREATE TABLE " + TABLE_WEATHER + " ("
                    + WeatherDay.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + WeatherDay.COL_DATE + " TEXT NOT NULL, "
                    + WeatherDay.COL_SUMMARY + " TEXT NOT NULL, "
                    + WeatherDay.COL_DESCRIPTION + " TEXT NOT NULL, "
                    + WeatherDay.COL_TEMP_HIGH_C + " REAL NOT NULL, "
                    + WeatherDay.COL_TEMP_LOW_C + " REAL NOT NULL, "
                    + WeatherDay.COL_HUMIDITY + " INTEGER NOT NULL, "
                    + WeatherDay.COL_WIND + " TEXT NOT NULL, "
                    + WeatherDay.COL_CITY + " TEXT NOT NULL, "
                    + WeatherDay.COL_LAT + " REAL NOT NULL, "
                    + WeatherDay.COL_LON + " REAL NOT NULL"
                    + ");";

    public WeatherDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEATHER);
        onCreate(db);
    }
}

package com.example.weatherforecast;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class WeatherContentProvider extends ContentProvider {
    public static final String AUTHORITY = "com.example.weatherforecast.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/forecast");

    private static final int FORECAST = 1;
    private static final int FORECAST_ID = 2;
    private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        MATCHER.addURI(AUTHORITY, "forecast", FORECAST);
        MATCHER.addURI(AUTHORITY, "forecast/#", FORECAST_ID);
    }

    private WeatherDbHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new WeatherDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        switch (MATCHER.match(uri)) {
            case FORECAST:
                return db.query(WeatherDbHelper.TABLE_WEATHER, projection, selection,
                        selectionArgs, null, null, sortOrder);
            case FORECAST_ID:
                String id = uri.getLastPathSegment();
                return db.query(WeatherDbHelper.TABLE_WEATHER, projection,
                        WeatherDay.COL_ID + "=?", new String[]{id},
                        null, null, sortOrder);
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (MATCHER.match(uri) != FORECAST || values == null) {
            throw new IllegalArgumentException("Insert not supported: " + uri);
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = db.insert(WeatherDbHelper.TABLE_WEATHER, null, values);
        if (id > 0) {
            Uri newUri = ContentUris.withAppendedId(CONTENT_URI, id);
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(newUri, null);
            }
            return newUri;
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows;
        if (MATCHER.match(uri) == FORECAST_ID) {
            String id = uri.getLastPathSegment();
            rows = db.delete(WeatherDbHelper.TABLE_WEATHER, WeatherDay.COL_ID + "=?",
                    new String[]{id});
        } else {
            rows = db.delete(WeatherDbHelper.TABLE_WEATHER, selection, selectionArgs);
        }
        if (rows > 0 && getContext() != null) {
            getContext().getContentResolver().notifyChange(CONTENT_URI, null);
        }
        return rows;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.update(WeatherDbHelper.TABLE_WEATHER, values, selection, selectionArgs);
        if (rows > 0 && getContext() != null) {
            getContext().getContentResolver().notifyChange(CONTENT_URI, null);
        }
        return rows;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (MATCHER.match(uri)) {
            case FORECAST:
                return "vnd.android.cursor.dir/vnd.weatherforecast";
            case FORECAST_ID:
                return "vnd.android.cursor.item/vnd.weatherforecast";
            default:
                return null;
        }
    }
}

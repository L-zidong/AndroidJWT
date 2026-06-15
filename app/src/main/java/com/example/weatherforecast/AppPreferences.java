package com.example.weatherforecast;

import android.content.Context;
import android.content.SharedPreferences;

public final class AppPreferences {
    private static final String PREFS = "weather_settings";
    private static final String KEY_CITY = "city";
    private static final String KEY_FAHRENHEIT = "use_fahrenheit";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";

    private static final String DEFAULT_CITY = "长沙";

    private AppPreferences() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static String getCity(Context context) {
        return prefs(context).getString(KEY_CITY, DEFAULT_CITY);
    }

    public static void setCity(Context context, String city) {
        prefs(context).edit().putString(KEY_CITY, city).apply();
    }

    public static boolean useFahrenheit(Context context) {
        return prefs(context).getBoolean(KEY_FAHRENHEIT, false);
    }

    public static void setUseFahrenheit(Context context, boolean fahrenheit) {
        prefs(context).edit().putBoolean(KEY_FAHRENHEIT, fahrenheit).apply();
    }

    public static boolean notificationsEnabled(Context context) {
        return prefs(context).getBoolean(KEY_NOTIFICATIONS, false);
    }

    public static void setNotificationsEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply();
    }
}

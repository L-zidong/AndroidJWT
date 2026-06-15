package com.example.weatherforecast;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * 和风天气 v7 七日预报 + GeoAPI 城市查询。
 */
public final class QWeatherApi {
    private static final String TAG = "QWeatherApi";

    private static final Map<String, String> FALLBACK_LOCATION_IDS = Map.of(
            "长沙", "101250101",
            "北京", "101010100",
            "上海", "101020100"
    );

    private static String cachedToken;
    private static long tokenExpiresAtEpochSec;

    private QWeatherApi() {
    }

    public static boolean isConfigured() {
        return BuildConfig.QWEATHER_API_HOST != null
                && !BuildConfig.QWEATHER_API_HOST.isEmpty()
                && BuildConfig.QWEATHER_PROJECT_ID != null
                && !BuildConfig.QWEATHER_PROJECT_ID.isEmpty()
                && BuildConfig.QWEATHER_CREDENTIAL_ID != null
                && !BuildConfig.QWEATHER_CREDENTIAL_ID.isEmpty();
    }

    public static List<WeatherDay> fetchForecast(Context context, String city) throws Exception {
        if (!isConfigured()) {
            throw new IllegalStateException("QWeather not configured in local.properties");
        }
        if (!QWeatherJwt.hasPrivateKeyAsset(context)) {
            throw new IllegalStateException("Missing assets/qweather_private.pem");
        }
        LocationInfo location = lookupCity(context, city);
        return fetchDaily(context, city, location);
    }

    private static LocationInfo lookupCity(Context context, String city) throws Exception {
        String encoded = URLEncoder.encode(city, StandardCharsets.UTF_8.name());
        String body = httpGet(context,
                "https://" + BuildConfig.QWEATHER_API_HOST + "/geo/v2/city/lookup?location=" + encoded);
        JSONObject json = new JSONObject(body);
        if (!"200".equals(json.optString("code"))) {
            String fallbackId = FALLBACK_LOCATION_IDS.get(city);
            if (fallbackId != null) {
                return LocationInfo.fallback(city, fallbackId);
            }
            throw new IllegalStateException("GeoAPI error: " + json.optString("code"));
        }
        JSONArray locations = json.optJSONArray("location");
        if (locations == null || locations.length() == 0) {
            String fallbackId = FALLBACK_LOCATION_IDS.get(city);
            if (fallbackId != null) {
                return LocationInfo.fallback(city, fallbackId);
            }
            throw new IllegalStateException("City not found: " + city);
        }
        JSONObject first = locations.getJSONObject(0);
        return new LocationInfo(
                city,
                first.getString("id"),
                Double.parseDouble(first.getString("lat")),
                Double.parseDouble(first.getString("lon"))
        );
    }

    private static List<WeatherDay> fetchDaily(Context context, String city, LocationInfo location)
            throws Exception {
        String body = httpGet(context,
                "https://" + BuildConfig.QWEATHER_API_HOST + "/v7/weather/7d?location=" + location.id);
        JSONObject json = new JSONObject(body);
        if (!"200".equals(json.optString("code"))) {
            throw new IllegalStateException("Weather API error: " + json.optString("code"));
        }
        JSONArray daily = json.getJSONArray("daily");
        List<WeatherDay> list = new ArrayList<>();
        for (int i = 0; i < daily.length(); i++) {
            JSONObject day = daily.getJSONObject(i);
            String fxDate = day.getString("fxDate");
            String dateLabel = formatDateLabel(fxDate);
            String summary = day.optString("textDay", "");
            String description = context.getString(
                    R.string.qweather_day_night,
                    day.optString("textDay", ""),
                    day.optString("textNight", ""));
            double tempMax = Double.parseDouble(day.optString("tempMax", "0"));
            double tempMin = Double.parseDouble(day.optString("tempMin", "0"));
            int humidity = Integer.parseInt(day.optString("humidity", "0"));
            String wind = day.optString("windDirDay", "") + " "
                    + day.optString("windScaleDay", "") + "级";
            list.add(new WeatherDay(
                    dateLabel,
                    summary,
                    description,
                    tempMax,
                    tempMin,
                    humidity,
                    wind.trim(),
                    city,
                    location.lat,
                    location.lon
            ));
        }
        return list;
    }

    private static String formatDateLabel(String fxDate) {
        if (fxDate.length() >= 10) {
            return fxDate.substring(5, 7) + "-" + fxDate.substring(8, 10);
        }
        return fxDate;
    }

    private static String httpGet(Context context, String urlString) throws Exception {
        String token = getToken(context);
        HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
        conn.setConnectTimeout(8_000);
        conn.setReadTimeout(8_000);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Accept-Encoding", "gzip");
        int code = conn.getResponseCode();
        InputStream stream = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
        if (stream == null) {
            throw new IllegalStateException("HTTP " + code + " for " + urlString);
        }
        String encoding = conn.getContentEncoding();
        if ("gzip".equalsIgnoreCase(encoding)) {
            stream = new GZIPInputStream(stream);
        }
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        if (code >= 400) {
            Log.w(TAG, "HTTP " + code + ": " + response);
            throw new IllegalStateException("HTTP " + code);
        }
        return response.toString();
    }

    private static synchronized String getToken(Context context) throws Exception {
        long now = System.currentTimeMillis() / 1000L;
        if (cachedToken != null && now < tokenExpiresAtEpochSec - 60) {
            return cachedToken;
        }
        cachedToken = QWeatherJwt.createToken(context);
        tokenExpiresAtEpochSec = now + 900;
        return cachedToken;
    }

    private static final class LocationInfo {
        final String id;
        final double lat;
        final double lon;

        LocationInfo(String city, String id, double lat, double lon) {
            this.id = id;
            this.lat = lat;
            this.lon = lon;
        }

        static LocationInfo fallback(String city, String id) {
            Map<String, double[]> coords = new HashMap<>();
            coords.put("长沙", new double[]{28.2282, 112.9388});
            coords.put("北京", new double[]{39.9042, 116.4074});
            coords.put("上海", new double[]{31.2304, 121.4737});
            double[] c = coords.getOrDefault(city, new double[]{28.2282, 112.9388});
            return new LocationInfo(city, id, c[0], c[1]);
        }
    }
}

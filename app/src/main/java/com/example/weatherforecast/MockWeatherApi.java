package com.example.weatherforecast;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MockWeatherApi {

    private static final int FORECAST_DAYS = 7;

    private static final double DEFAULT_LAT = 28.2282;
    private static final double DEFAULT_LON = 112.9388;

    private static final Map<String, double[]> CITY_COORDS = Map.of(
            "北京", new double[]{39.9042, 116.4074},
            "上海", new double[]{31.2304, 121.4737}
    );

    private static final String[] SUMMARIES = {"晴", "多云", "小雨", "阴", "晴转多云", "雷阵雨", "晴"};
    private static final String[] DESCRIPTIONS = {
            "白天晴朗，紫外线较强，注意防晒。",
            "云量较多，气温舒适。",
            "有小雨，出门请带伞。",
            "阴天，体感略凉。",
            "午后云量增多。",
            "短时强降雨，注意出行安全。",
            "天气晴好，适合户外活动。"
    };
    private static final double[][] TEMPS = {
            {26, 18}, {24, 17}, {22, 16}, {23, 15}, {25, 19}, {21, 14}, {27, 20}
    };
    private static final int[] HUMIDITY = {45, 55, 78, 60, 50, 85, 42};
    private static final String[] WIND = {"东南风 2级", "东北风 3级", "南风 2级", "北风 1级",
            "西南风 2级", "东风 4级", "西北风 2级"};
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM-dd");

    private MockWeatherApi() {
    }

    public static List<WeatherDay> fetchForecast(String city) {
        double[] coords = CITY_COORDS.getOrDefault(city, new double[]{DEFAULT_LAT, DEFAULT_LON});
        double lat = coords[0];
        double lon = coords[1];

        List<WeatherDay> list = new ArrayList<>(FORECAST_DAYS);
        LocalDate date = LocalDate.now();
        for (int i = 0; i < FORECAST_DAYS; i++) {
            list.add(new WeatherDay(
                    date.format(DATE_FMT),
                    SUMMARIES[i],
                    DESCRIPTIONS[i],
                    TEMPS[i][0],
                    TEMPS[i][1],
                    HUMIDITY[i],
                    WIND[i],
                    city,
                    lat,
                    lon
            ));
            date = date.plusDays(1);
        }
        return list;
    }
}

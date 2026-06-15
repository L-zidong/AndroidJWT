package com.example.weatherforecast;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private final ExecutorService loadExecutor = Executors.newSingleThreadExecutor();

    private WeatherRepository repository;
    private WeatherAdapter adapter;
    private TextView statusView;
    private List<WeatherDay> currentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.purple_700));
        getWindow().setNavigationBarColor(Color.WHITE);
        setContentView(R.layout.activity_main);

        repository = new WeatherRepository(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        statusView = findViewById(R.id.text_status);
        RecyclerView recyclerView = findViewById(R.id.recycler_weather);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setBackgroundColor(ContextCompat.getColor(this, R.color.surface_light));
        adapter = new WeatherAdapter(this::onWeatherSelected);
        recyclerView.setAdapter(adapter);

        // 先显示 Mock 列表，避免等待和风 API 时整屏空白
        String city = AppPreferences.getCity(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(
                    R.string.forecast_title, city, getString(R.string.forecast_7day)));
        }
        currentList = MockWeatherApi.fetchForecast(city);
        adapter.setUseFahrenheit(AppPreferences.useFahrenheit(this));
        adapter.submitList(currentList);
        statusView.setText(R.string.loading);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.setUseFahrenheit(AppPreferences.useFahrenheit(this));
        refreshForecast(false);
        WeatherFetchService.scheduleIfNeeded(this);
    }

    @Override
    protected void onDestroy() {
        loadExecutor.shutdownNow();
        super.onDestroy();
    }

    private void refreshForecast(boolean forceNetwork) {
        String city = AppPreferences.getCity(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(
                    R.string.forecast_title, city, getString(R.string.forecast_7day)));
        }
        statusView.setText(R.string.loading);
        loadExecutor.execute(() -> {
            try {
                List<WeatherDay> list = repository.loadForecast(city, forceNetwork);
                boolean fromNetwork = forceNetwork || repository.isNetworkAvailable();
                runOnUiThread(() -> {
                    if (isFinishing()) {
                        return;
                    }
                    showForecastOnUi(list, fromNetwork);
                });
            } catch (Exception e) {
                Log.e(TAG, "loadForecast failed", e);
                runOnUiThread(() -> {
                    if (isFinishing()) {
                        return;
                    }
                    showForecastOnUi(MockWeatherApi.fetchForecast(city), true);
                    statusView.setText(getString(
                            R.string.data_source_hint, getString(R.string.source_network)));
                });
            }
        });
    }

    private void showForecastOnUi(List<WeatherDay> list, boolean fromNetwork) {
        if (list == null || list.isEmpty()) {
            list = MockWeatherApi.fetchForecast(AppPreferences.getCity(this));
        }
        currentList = list;
        adapter.setUseFahrenheit(AppPreferences.useFahrenheit(this));
        adapter.submitList(currentList);
        String source;
        if (!fromNetwork) {
            source = getString(R.string.source_cache);
        } else if (repository.usesQWeatherApi()) {
            source = getString(R.string.source_qweather);
        } else {
            source = getString(R.string.source_network);
        }
        statusView.setText(getString(R.string.data_source_hint, source));
    }

    private void onWeatherSelected(WeatherDay day) {
        if (findViewById(R.id.detail_container) != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.detail_container, DetailFragment.newInstance(day.getId()))
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_WEATHER_ID, day.getId());
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_map) {
            openMap();
            return true;
        }
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (id == R.id.action_refresh) {
            refreshForecast(true);
            Toast.makeText(this, R.string.refreshed, Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openMap() {
        if (currentList == null || currentList.isEmpty()) {
            Toast.makeText(this, R.string.no_location_data, Toast.LENGTH_SHORT).show();
            return;
        }
        WeatherDay day = currentList.get(0);
        Uri geo = Uri.parse("geo:" + day.getLatitude() + "," + day.getLongitude()
                + "?q=" + day.getLatitude() + "," + day.getLongitude()
                + "(" + day.getCity() + ")");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, geo);
        try {
            startActivity(mapIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.no_map_app, Toast.LENGTH_SHORT).show();
        }
    }
}

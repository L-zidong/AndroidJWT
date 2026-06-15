package com.example.weatherforecast;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SettingsActivity extends AppCompatActivity {
    private Spinner citySpinner;
    private RadioGroup unitGroup;
    private CheckBox notificationCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.settings_title);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        citySpinner = findViewById(R.id.spinner_city);
        unitGroup = findViewById(R.id.radio_unit);
        notificationCheck = findViewById(R.id.checkbox_notification);
        Button saveButton = findViewById(R.id.button_save);

        String[] cities = getResources().getStringArray(R.array.city_list);
        citySpinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, cities));

        String currentCity = AppPreferences.getCity(this);
        for (int i = 0; i < cities.length; i++) {
            if (cities[i].equals(currentCity)) {
                citySpinner.setSelection(i);
                break;
            }
        }

        if (AppPreferences.useFahrenheit(this)) {
            unitGroup.check(R.id.radio_fahrenheit);
        } else {
            unitGroup.check(R.id.radio_celsius);
        }
        notificationCheck.setChecked(AppPreferences.notificationsEnabled(this));

        saveButton.setOnClickListener(v -> saveSettings());
    }

    private void saveSettings() {
        String city = (String) citySpinner.getSelectedItem();
        boolean fahrenheit = unitGroup.getCheckedRadioButtonId() == R.id.radio_fahrenheit;
        boolean notifications = notificationCheck.isChecked();

        AppPreferences.setCity(this, city);
        AppPreferences.setUseFahrenheit(this, fahrenheit);
        AppPreferences.setNotificationsEnabled(this, notifications);

        if (notifications && Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
        WeatherFetchService.scheduleIfNeeded(this);
        Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }
}

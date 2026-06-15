package com.example.weatherforecast;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class DetailActivity extends AppCompatActivity {
    public static final String EXTRA_WEATHER_ID = "weather_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        long weatherId = getIntent().getLongExtra(EXTRA_WEATHER_ID, -1);
        if (savedInstanceState == null && weatherId > 0) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.detail_fragment_container,
                            DetailFragment.newInstance(weatherId))
                    .commit();
        }
    }
}

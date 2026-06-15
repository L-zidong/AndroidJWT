package com.example.weatherforecast;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetailFragment extends Fragment {
    private static final ExecutorService loadExecutor = Executors.newSingleThreadExecutor();
    private static final String ARG_WEATHER_ID = "weather_id";

    private TextView titleView;
    private TextView summaryView;
    private TextView descriptionView;
    private TextView tempView;
    private TextView extraView;

    private WeatherDay currentDay;

    public static DetailFragment newInstance(long weatherId) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_WEATHER_ID, weatherId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        titleView = view.findViewById(R.id.detail_title);
        summaryView = view.findViewById(R.id.detail_summary);
        descriptionView = view.findViewById(R.id.detail_description);
        tempView = view.findViewById(R.id.detail_temp);
        extraView = view.findViewById(R.id.detail_extra);

        long id = getArguments() != null ? getArguments().getLong(ARG_WEATHER_ID) : -1;
        if (id > 0) {
            loadWeather(id);
        }
    }

    public void loadWeather(long weatherId) {
        WeatherRepository repo = new WeatherRepository(requireContext());
        loadExecutor.execute(() -> {
            WeatherDay day = repo.getById(weatherId);
            if (!isAdded()) {
                return;
            }
            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) {
                    return;
                }
                currentDay = day;
                bindWeather();
            });
        });
    }

    private void bindWeather() {
        if (currentDay == null || getContext() == null) {
            return;
        }
        boolean f = AppPreferences.useFahrenheit(requireContext());
        titleView.setText(getString(R.string.detail_header, currentDay.getCity(), currentDay.getDate()));
        summaryView.setText(currentDay.getSummary());
        descriptionView.setText(currentDay.getDescription());
        tempView.setText(currentDay.formatDetailTemps(requireContext(), f));
        extraView.setText(getString(R.string.detail_extra, currentDay.getHumidity(), currentDay.getWind()));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {
            shareWeather();
            return true;
        }
        if (id == R.id.action_settings) {
            startActivity(new Intent(requireContext(), SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareWeather() {
        if (currentDay == null || getContext() == null) {
            return;
        }
        boolean f = AppPreferences.useFahrenheit(requireContext());
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_weather_subject));
        intent.putExtra(Intent.EXTRA_TEXT, currentDay.getShareText(requireContext(), f));
        startActivity(Intent.createChooser(intent, getString(R.string.share_weather)));
    }
}

package com.example.weatherforecast;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

public class WeatherAdapter extends ListAdapter<WeatherDay, RecyclerView.ViewHolder> {
    private static final Object PAYLOAD_TEMP_UNIT = "temp_unit";

    public interface OnItemClickListener {
        void onItemClick(WeatherDay day);
    }

    private static final DiffUtil.ItemCallback<WeatherDay> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull WeatherDay oldItem,
                                               @NonNull WeatherDay newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull WeatherDay oldItem,
                                                  @NonNull WeatherDay newItem) {
                    return Objects.equals(oldItem.getDate(), newItem.getDate())
                            && Objects.equals(oldItem.getSummary(), newItem.getSummary())
                            && oldItem.getTempHighC() == newItem.getTempHighC()
                            && oldItem.getTempLowC() == newItem.getTempLowC();
                }
            };

    private final OnItemClickListener listener;
    private boolean useFahrenheit;

    public WeatherAdapter(OnItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    public void setUseFahrenheit(boolean useFahrenheit) {
        if (this.useFahrenheit == useFahrenheit) {
            return;
        }
        this.useFahrenheit = useFahrenheit;
        if (getItemCount() > 0) {
            notifyItemRangeChanged(0, getItemCount(), PAYLOAD_TEMP_UNIT);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weather, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position,
                                 @NonNull List<Object> payloads) {
        ViewHolder vh = asWeatherHolder(holder);
        if (payloads.contains(PAYLOAD_TEMP_UNIT)) {
            WeatherDay day = getItem(position);
            vh.temp.setText(day.formatTempRange(vh.itemView.getContext(), useFahrenheit));
            return;
        }
        onBindViewHolder(holder, position);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder vh = asWeatherHolder(holder);
        WeatherDay day = getItem(position);
        vh.date.setText(day.getDate());
        vh.summary.setText(day.getSummary());
        vh.temp.setText(day.formatTempRange(vh.itemView.getContext(), useFahrenheit));
        vh.itemView.setOnClickListener(v -> listener.onItemClick(day));
    }

    private static ViewHolder asWeatherHolder(RecyclerView.ViewHolder holder) {
        return (ViewHolder) holder;
    }

    private static final class ViewHolder extends RecyclerView.ViewHolder {
        final TextView date;
        final TextView summary;
        final TextView temp;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.text_date);
            summary = itemView.findViewById(R.id.text_summary);
            temp = itemView.findViewById(R.id.text_temp);
        }
    }
}

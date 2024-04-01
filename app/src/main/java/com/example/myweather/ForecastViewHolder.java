package com.example.myweather;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class ForecastViewHolder extends RecyclerView.ViewHolder {
    public ImageView forecastIconImageView;
    public TextView weekTextView;
    public TextView forecastMaxTempTextView;
    public TextView forecastMinTempTextView;

    public ForecastViewHolder(View view) {
        super(view);
        forecastIconImageView = view.findViewById(R.id.forecastIconImageView);
        weekTextView = view.findViewById(R.id.weekTextView);
        forecastMaxTempTextView = view.findViewById(R.id.forecastMaxTempTextView);
        forecastMinTempTextView = view.findViewById(R.id.forecastMinTempTextView);
    }
}

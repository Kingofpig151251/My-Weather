package com.example.myweather;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class ForecastViewHolder extends RecyclerView.ViewHolder {
    public TextView dateTextView;
    public TextView temperatureTextView;

    public ForecastViewHolder(View view) {
        super(view);
    }
}

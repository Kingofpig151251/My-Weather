package com.example.myweather.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myweather.ForecastViewHolder;
import com.example.myweather.R;
import com.example.myweather.managers.PreferencesManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ForecastActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);
        RecyclerView recyclerView = findViewById(R.id.forecastRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }

    RecyclerView.Adapter<ForecastViewHolder> adapter = new RecyclerView.Adapter<ForecastViewHolder>() {
        @NonNull
        @Override
        public ForecastViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_forecast, parent, false);
            return new ForecastViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ForecastViewHolder holder, int position) {
            PreferencesManager preferencesManager = new PreferencesManager(ForecastActivity.this);
            String forecastString = preferencesManager.getString("weatherForecast", null);
            if (forecastString != null) {
                try {
                    JSONArray forecastArray = new JSONArray(forecastString);
                    JSONObject forecast = forecastArray.getJSONObject(position);
                    String week = forecast.getString("week");
                    holder.weekTextView.setText(week.substring(0, 3));
                    String iconData = forecast.getString("ForecastIcon");
                    String iconUrl = "https://www.hko.gov.hk/images/HKOWxIconOutline/pic" + iconData + ".png";
                    Glide.with(ForecastActivity.this).load(iconUrl).into(holder.forecastIconImageView);
                    holder.forecastMaxTempTextView.setText(forecast.getJSONObject("forecastMaxtemp").getString("value"));
                    holder.forecastMinTempTextView.setText(forecast.getJSONObject("forecastMintemp").getString("value"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public int getItemCount() {
            PreferencesManager preferencesManager = new PreferencesManager(ForecastActivity.this);
            String forecastString = preferencesManager.getString("weatherForecast", null);
            if (forecastString != null) {
                try {
                    JSONArray forecastArray = new JSONArray(forecastString);
                    return forecastArray.length();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return 0;
        }
    };

}

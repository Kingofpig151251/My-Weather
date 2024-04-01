package com.example.myweather.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myweather.ForecastViewHolder;
import com.example.myweather.R;

public class ForecastActivity extends AppCompatActivity {
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);
        recyclerView = findViewById(R.id.forecast_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    RecyclerView.Adapter<ForecastViewHolder> adapter = new RecyclerView.Adapter<ForecastViewHolder>() {
        @Override
        public ForecastViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_forecast, parent, false);
            return new ForecastViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ForecastViewHolder holder, int position) {
            holder.dateTextView.setText("test");
            holder.temperatureTextView.setText("test");
        }

        @Override
        public int getItemCount() {
            return 10;
        }
    };
}

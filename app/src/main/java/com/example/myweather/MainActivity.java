package com.example.myweather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnRequestCompletedListener {
    private static final String PREFS_NAME = "Weather";
    private static final String BASE_API_URL = "https://data.weather.gov.hk/weatherAPI/opendata/weather.php?lang=en&dataType=";
    private static final String DATA_TYPE_FLW = "flw";
    private static final String DATA_TYPE_RHRREAD = "rhrread";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ApiRequestHandler apiRequestHandler = new ApiRequestHandler(this, this);
        apiRequestHandler.makeRequests(Arrays.asList(
                BASE_API_URL + DATA_TYPE_FLW,
                BASE_API_URL + DATA_TYPE_RHRREAD));
    }

    @Override
    public void onRequestCompleted() {
        // Get data from SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int iconData = sharedPref.getInt("icon", 0);
        String outlook = sharedPref.getString("outlook", "");
        String updateTime = sharedPref.getString("updateTime", "");
        String humidityDataString = sharedPref.getString("humidity", "");


        // Update iconImageView
        String iconUrl = "https://www.hko.gov.hk/images/HKOWxIconOutline/pic" + iconData + ".png";
        ImageView iconImageView = findViewById(R.id.iconImageView);
        Glide.with(this)
                .load(iconUrl)
                .into(iconImageView);

        // Update outlookTextView
        TextView outlookTextView = findViewById(R.id.outlookTextView);
        outlookTextView.setText(outlook);

        // Update updateTimeTextView
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = isoFormat.parse(updateTime);
            if (date != null) {
                SimpleDateFormat readableFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                String readableUpdateTime = readableFormat.format(date);
                TextView updateTimeTextView = findViewById(R.id.updateTimeTextView);
                updateTimeTextView.setText(readableUpdateTime);
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error parsing date: " + e.getMessage());
        }

        // Update humidityTextView
        try {
            JSONObject humidityData = new JSONObject(humidityDataString);
            JSONArray dataArray = humidityData.optJSONArray("data");
            if (dataArray != null && dataArray.length() > 0) {
                JSONObject firstItem = dataArray.optJSONObject(0);
                if (firstItem != null) {
                    int value = firstItem.optInt("value");
                    TextView humidityTextView = findViewById(R.id.humidityTextView);
                    humidityTextView.setText(value + "%");
                }
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error parsing humidity data: " + e.getMessage());
        }
    }

    @Override
    public void onError(Exception e) {
        Log.e("MainActivity", "Error: " + e.getMessage());
    }
}
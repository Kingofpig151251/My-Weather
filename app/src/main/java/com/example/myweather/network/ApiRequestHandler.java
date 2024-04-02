package com.example.myweather.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.myweather.utils.Constants;
import com.example.myweather.utils.OnRequestCompletedListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiRequestHandler {
    private static final String PREFS_NAME = "Weather";
    private Context context;
    private Handler handler;
    private OnRequestCompletedListener listener;
    private ExecutorService executorService;


    public ApiRequestHandler(Context context, OnRequestCompletedListener listener) {
        this.context = context;
        this.listener = listener;
        this.handler = new Handler(Looper.getMainLooper());
        this.executorService = Executors.newFixedThreadPool(5);  // Create a thread pool
    }

    public void makeRequests(List<String> urlStrings) {
        for (String urlString : urlStrings) {
            makeRequest(urlString);
        }
    }

    private void makeRequest(String urlString) {
        executorService.submit(() -> {  // Use the thread pool to execute the request
            try {
                URL url = new URL(urlString);
                Log.d("ApiRequestHandler", "Requesting data from: " + urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                String responseData = response.toString();
                JSONObject jsonObject = new JSONObject(responseData);
                saveData(jsonObject);

                handler.post(() -> {
                    // Update UI on main thread after the request is completed
                    if (listener != null) {
                        listener.onRequestCompleted();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() -> {
                    if (listener != null) {
                        listener.onError(e);
                    }
                });
            }
        });
    }

    private void saveData(JSONObject data) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        if (data.has("generalSituation") && data.has("forecastPeriod") && data.has("forecastDesc") && data.has("outlook") && data.has("updateTime")) {
            editor.putString("generalSituation", data.optString("generalSituation"));
            editor.putString("forecastPeriod", data.optString("forecastPeriod"));
            editor.putString("forecastDesc", data.optString("forecastDesc"));
            editor.putString("outlook", data.optString("outlook"));
            editor.putString("updateTime", data.optString("updateTime"));
        }


        if (data.has("icon") && data.has("temperature") && data.has("humidity")) {
            JSONArray iconData = data.optJSONArray("icon");
            if (iconData != null) {
                editor.putInt("icon", iconData.optInt(0));
            }

            JSONObject temperatureData = data.optJSONObject("temperature");
            if (temperatureData != null) {
                editor.putString("temperature", temperatureData.toString());
            }

            JSONObject humidityData = data.optJSONObject("humidity");
            if (humidityData != null) {
                String recordTime = humidityData.optString("recordTime");
                editor.putString("humidityRecordTime", recordTime);

                JSONArray dataArray = humidityData.optJSONArray("data");
                if (dataArray != null && dataArray.length() > 0) {
                    JSONObject firstItem = dataArray.optJSONObject(0);
                    if (firstItem != null) {
                        String unit = firstItem.optString("unit");
                        int value = firstItem.optInt("value");
                        String place = firstItem.optString("place");

                        editor.putString("humidityUnit", unit);
                        editor.putInt("humidityValue", value);
                        editor.putString("humidityPlace", place);
                    }
                }
            }
        }

        if (data.has("weatherForecast")) {
            JSONArray weatherForecastData = data.optJSONArray("weatherForecast");
            if (weatherForecastData != null) {
                editor.putString("weatherForecast", weatherForecastData.toString());
            }
        }
        editor.apply();
    }
}
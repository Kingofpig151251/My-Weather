package com.example.myweather.managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.myweather.utils.Constants;

public class PreferencesManager {
    private final SharedPreferences sharedPreferences;

    public PreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    public String getWeatherForecast() {
        return sharedPreferences.getString("weatherForecast", null);
    }
}

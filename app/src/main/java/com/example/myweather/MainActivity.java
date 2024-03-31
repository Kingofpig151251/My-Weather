package com.example.myweather;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnRequestCompletedListener {
    // region Variables
    private static final String PREFS_NAME = "Weather";
    private static final String BASE_API_URL = "https://data.weather.gov.hk/weatherAPI/opendata/weather.php?lang=en&dataType=";
    private static final String DATA_TYPE_FLW = "flw";
    private static final String DATA_TYPE_RHRREAD = "rhrread";

    private LocationManager locationManager;
    private LocationListener locationListener;
    private TextView locationTextView;
    private static final String GEOCODING_API_URL = "https://api.opencagedata.com/geocode/v1/json?q=%1$f+%2$f&key=%3$s";

    // endregion

    // region Lifecycle Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationTextView = findViewById(R.id.locationTextView);
        setupApiRequestHandler();
        requestLocationPermission();
    }
    // endregion

    // region Helper Methods
    private void setupApiRequestHandler() {
        ApiRequestHandler apiRequestHandler = new ApiRequestHandler(this, this);
        apiRequestHandler.makeRequests(Arrays.asList(BASE_API_URL + DATA_TYPE_FLW, BASE_API_URL + DATA_TYPE_RHRREAD));
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            setupLocationUpdates();
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            } catch (SecurityException e) {
                Log.e("MainActivity", "Error requesting location updates: " + e.getMessage());
            }
        }
    }

    private void setupLocationUpdates() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                updateLocationName(latitude, longitude);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
    }

    private void updateLocationName(double latitude, double longitude) {
        String url = String.format(Locale.getDefault(), GEOCODING_API_URL, latitude, longitude, "37d9da6b6f7747e29085208abaa8b684");
        Log.d("MainActivity", "URL: " + url);
        new GetLocationNameTask(response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONObject results = jsonObject.getJSONArray("results").getJSONObject(0);
                String locationName = results.getString("formatted");
                locationTextView.setText(locationName);
            } catch (Exception e) {
                Log.e("MainActivity", "Error parsing location name: " + e.getMessage());
            }
        }).execute(url);
    }

    private void updateUI() {
        // Get data from SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int iconData = sharedPref.getInt("icon", 0);
        String outlook = sharedPref.getString("outlook", "");
        String updateTime = sharedPref.getString("updateTime", "");
        String humidityDataString = sharedPref.getString("humidity", "");

        // Update iconImageView
        String iconUrl = "https://www.hko.gov.hk/images/HKOWxIconOutline/pic" + iconData + ".png";
        ImageView iconImageView = findViewById(R.id.iconImageView);
        Glide.with(this).load(iconUrl).into(iconImageView);

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
    // endregion

    // region Permission Methods
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupLocationUpdates();
            }
        }
    }
    // endregion

    // region OnRequestCompletedListener Methods
    @Override
    public void onRequestCompleted() {
        updateUI();
    }

    @Override
    public void onError(Exception e) {
        Log.e("MainActivity", "Error: " + e.getMessage());
    }
    // endregion
}
package com.example.myweather;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnRequestCompletedListener {
    // region Variables
    private PreferencesManager preferencesManager;
    private Location lastLocation = null;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private ImageView iconImageView;
    private TextView locationDisplayTextView;
    private TextView forecastDescriptionTextView;
    private TextView lastUpdateTimeTextView;
    private TextView humidityDisplayTextView;
    private TextView weatherOutlookTextView;
    private TextView temperatureDisplayTextView;
    // endregion

    // region Lifecycle Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iconImageView = findViewById(R.id.iconImageView);
        locationDisplayTextView = findViewById(R.id.locationDisplayTextView);
        forecastDescriptionTextView = findViewById(R.id.forecastDescriptionTextView);
        lastUpdateTimeTextView = findViewById(R.id.lastUpdateTimeTextView);
        humidityDisplayTextView = findViewById(R.id.humidityDisplayTextView);
        weatherOutlookTextView = findViewById(R.id.weatherOutlookTextView);
        temperatureDisplayTextView = findViewById(R.id.temperatureDisplayTextView);
        preferencesManager = new PreferencesManager(this);
        setupApiRequestHandler();
        requestLocationPermission();
    }
    // endregion

    // region Permission Methods
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupLocationUpdates();
            }
        }
    }

    private void setupLocationUpdates() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                if (lastLocation == null || lastLocation.distanceTo(location) > 500) {
                    updateLocationName(location.getLatitude(), location.getLongitude());
                    lastLocation = location;
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
            }
        };
    }
    // endregion

    // region API Request Methods
    private void setupApiRequestHandler() {
        ApiRequestHandler apiRequestHandler = new ApiRequestHandler(this, this);
        apiRequestHandler.makeRequests(Arrays.asList(Constants.BASE_API_URL + Constants.DATA_TYPE_FLW, Constants.BASE_API_URL + Constants.DATA_TYPE_RHRREAD));
    }

    private void updateLocationName(double latitude, double longitude) {
        String url = String.format(Locale.getDefault(), Constants.GEOCODING_API_URL, latitude, longitude, "37d9da6b6f7747e29085208abaa8b684");
        new GetLocationNameTask(response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONObject results = jsonObject.getJSONArray("results").getJSONObject(0);
                String locationName = results.getString("formatted");
                String[] locationParts = locationName.split(", ");
                if (locationParts.length > 3) {
                    locationName = locationParts[3];
                }
                locationDisplayTextView.setText(locationName);
            } catch (Exception e) {
                Log.e("MainActivity", "Error parsing location name: " + e.getMessage());
            }
        }).execute(url);
    }
    // endregion

    // region UI Update Methods
    @SuppressLint("SetTextI18n")
    private void updateUI() {
        // Get data from PreferencesManager
        int iconData = preferencesManager.getInt("icon", 0);
        String updateTime = preferencesManager.getString("updateTime", "");
        String outlook = preferencesManager.getString("outlook", "");
        String humidityDataString = preferencesManager.getString("humidity", "");
        String forecastDesc = preferencesManager.getString("forecastDesc", "");

        // Update iconImageView
        String iconUrl = "https://www.hko.gov.hk/images/HKOWxIconOutline/pic" + iconData + ".png";
        Glide.with(this).load(iconUrl).into(iconImageView);

        // Update temperatureTextView
        try {
            JSONObject temperatureData = JsonParser.parseStringToJson(preferencesManager.getString("temperature", ""));
            JSONArray dataArray = JsonParser.getJsonArray(temperatureData, "data");
            if (dataArray != null) {
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject data = JsonParser.getJsonObject(dataArray, i);
                    if (JsonParser.getString(data, "place").equals("Hong Kong Observatory")) {
                        int temperature = JsonParser.getInt(data, "value");
                        temperatureDisplayTextView.setText(temperature + "˚");
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error parsing temperature data: " + e.getMessage());
        }

        // Update updateTimeTextView
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = isoFormat.parse(updateTime);
            if (date != null) {
                SimpleDateFormat readableFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                String readableUpdateTime = readableFormat.format(date);
                lastUpdateTimeTextView.setText(readableUpdateTime);
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
                    humidityDisplayTextView.setText(value + "%");
                }
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error parsing humidity data: " + e.getMessage());
        }

        // Update outlookTextView
        weatherOutlookTextView.setText(outlook);

        // Update forecastDescTextView
        String[] sentences = forecastDesc.split("\\.");
        String firstSentence = sentences[0].replace("with", "with\n");
        forecastDescriptionTextView.setText(firstSentence);
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
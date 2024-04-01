package com.example.myweather.managers;

import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class GetLocationNameManager extends AsyncTask<String, Void, String> {
    private OnTaskCompleted listener;

    public GetLocationNameManager(OnTaskCompleted listener) {
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... urls) {
        String response = "";
        try {
            URL url = new URL(urls[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = connection.getInputStream();
            Scanner scanner = new Scanner(inputStream);
            while (scanner.hasNext()) {
                response += scanner.nextLine();
            }
        } catch (Exception e) {
            Log.e("GetLocationNameTask", "Error getting location name: " + e.getMessage());
        }
        return response;
    }

    @Override
    protected void onPostExecute(String response) {
        listener.onTaskCompleted(response);
    }

    public interface OnTaskCompleted {
        void onTaskCompleted(String response);
    }
}
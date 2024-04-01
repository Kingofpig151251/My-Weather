package com.example.myweather;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonParser {

    public static JSONObject parseStringToJson(String jsonString) {
        try {
            return new JSONObject(jsonString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONArray getJsonArray(JSONObject jsonObject, String key) {
        try {
            return jsonObject.optJSONArray(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject getJsonObject(JSONArray jsonArray, int index) {
        try {
            return jsonArray.getJSONObject(index);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getString(JSONObject jsonObject, String key) {
        try {
            return jsonObject.getString(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int getInt(JSONObject jsonObject, String key) {
        try {
            return jsonObject.getInt(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
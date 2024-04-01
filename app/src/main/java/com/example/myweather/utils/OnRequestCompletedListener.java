package com.example.myweather.utils;

public interface OnRequestCompletedListener {
    void onRequestCompleted();
    void onError(Exception e);
}

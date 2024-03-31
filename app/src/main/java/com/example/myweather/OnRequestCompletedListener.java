package com.example.myweather;

public interface OnRequestCompletedListener {
    void onRequestCompleted();
    void onError(Exception e);
}

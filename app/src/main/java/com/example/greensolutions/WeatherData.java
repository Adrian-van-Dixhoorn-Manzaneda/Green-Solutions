package com.example.greensolutions;

public class WeatherData {
    private String title;
    private String value;
    private int iconResId;

    public WeatherData(String title, String value, int iconResId) {
        this.title = title;
        this.value = value;
        this.iconResId = iconResId;
    }

    public String getTitle() {
        return title;
    }

    public String getValue() {
        return value;
    }

    public int getIconResId() {
        return iconResId;
    }
}
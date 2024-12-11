package com.example.greensolutions;

public class PosteSensors {
    private String title;
    private String description;
    private int iconResId;

    public PosteSensors(String title, String description, int iconResId) {
        this.title = title;
        this.description = description;
        this.iconResId = iconResId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getIconResId() {
        return iconResId;
    }
}

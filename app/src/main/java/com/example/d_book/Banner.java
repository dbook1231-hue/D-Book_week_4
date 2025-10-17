package com.example.d_book;

public class Banner {
    private final int imageResId;
    private final String title;

    public Banner(int imageResId, String title) {
        this.imageResId = imageResId;
        this.title = title;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getTitle() {
        return title;
    }
}
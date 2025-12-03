package com.example.d_book.item;

public class TrendingBook {
    private String title;
    private String author;
    private int visitCount;       // 🔹 기존 searchCount → visitCount로 명확히 변경
    private String thumbnailUrl;
    private int thumbnailResId;

    // 🔹 URL 이미지 생성자
    public TrendingBook(String title, String author, int visitCount, String thumbnailUrl) {
        this.title = title;
        this.author = author;
        this.visitCount = visitCount;
        this.thumbnailUrl = thumbnailUrl;
        this.thumbnailResId = 0;
    }

    // 🔹 로컬 이미지 생성자
    public TrendingBook(String title, String author, int visitCount, int thumbnailResId) {
        this.title = title;
        this.author = author;
        this.visitCount = visitCount;
        this.thumbnailResId = thumbnailResId;
        this.thumbnailUrl = null;
    }

    // 🔹 Getter / Setter
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) { this.title = title; }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) { this.author = author; }

    public int getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(int visitCount) { this.visitCount = visitCount; }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
        this.thumbnailResId = 0;
    }

    public int getThumbnailResId() {
        return thumbnailResId;
    }

    public void setThumbnailResId(int thumbnailResId) {
        this.thumbnailResId = thumbnailResId;
        this.thumbnailUrl = null;
    }
}

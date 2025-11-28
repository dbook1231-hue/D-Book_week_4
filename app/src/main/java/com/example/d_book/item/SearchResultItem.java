package com.example.d_book.item;

public class SearchResultItem {

    private String title;
    private String author;
    private String thumbnailUrl; // 썸네일 이미지 URL 또는 리소스
    private int thumbnailResId; // 로컬 리소스 ID

    public SearchResultItem(String title, String author, String thumbnailUrl) {
        this.title = title;
        this.author = author;
        this.thumbnailUrl = thumbnailUrl;
        this.thumbnailResId = 0;
    }

    public SearchResultItem(String title, String author, int thumbnailResId) {
        this.title = title;
        this.author = author;
        this.thumbnailResId = thumbnailResId;
        this.thumbnailUrl = null;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public int getThumbnailResId() {
        return thumbnailResId;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void setThumbnailResId(int thumbnailResId) {
        this.thumbnailResId = thumbnailResId;
    }
}
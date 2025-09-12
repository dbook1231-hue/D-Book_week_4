package com.example.d_book.item;

public class SearchResultItem {

    private String title;
    private String author;
    private String thumbnailUrl; // ← 썸네일 URL 추가

    public SearchResultItem(String title, String author, String thumbnailUrl) {
        this.title = title;
        this.author = author;
        this.thumbnailUrl = thumbnailUrl;
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

    // 필요 시 setter 추가 가능
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}

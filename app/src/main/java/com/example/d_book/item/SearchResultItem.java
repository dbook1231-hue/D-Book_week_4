package com.example.d_book.item;

public class SearchResultItem {

    private String title;
    private String author;
    private String thumbnailUrl; // 온라인 이미지 URL
    private int thumbnailResId;  // 로컬 리소스 이미지 ID
    private String category;     // 카테고리
    private int visitCount;      // 방문 수

    // 🔹 URL 이미지 + visitCount 생성자
    public SearchResultItem(String title, String author, String thumbnailUrl, String category, int visitCount) {
        this.title = title;
        this.author = author;
        this.thumbnailUrl = thumbnailUrl;
        this.thumbnailResId = 0;
        this.category = category;
        this.visitCount = visitCount;
    }

    // 🔹 로컬 이미지 + visitCount 생성자
    public SearchResultItem(String title, String author, int thumbnailResId, String category, int visitCount) {
        this.title = title;
        this.author = author;
        this.thumbnailResId = thumbnailResId;
        this.thumbnailUrl = null;
        this.category = category;
        this.visitCount = visitCount;
    }

    // 기존 URL 생성자 (visitCount 기본 0)
    public SearchResultItem(String title, String author, String thumbnailUrl, String category) {
        this(title, author, thumbnailUrl, category, 0);
    }

    // 기존 로컬 이미지 생성자 (visitCount 기본 0)
    public SearchResultItem(String title, String author, int thumbnailResId, String category) {
        this(title, author, thumbnailResId, category, 0);
    }

    // 🔹 Getter / Setter
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public int getThumbnailResId() { return thumbnailResId; }
    public String getCategory() { return category; }
    public int getVisitCount() { return visitCount; }

    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
        this.thumbnailResId = 0;
    }
    public void setThumbnailResId(int thumbnailResId) {
        this.thumbnailResId = thumbnailResId;
        this.thumbnailUrl = null;
    }
    public void setCategory(String category) { this.category = category; }
    public void setVisitCount(int visitCount) { this.visitCount = visitCount; }

    // 🔹 편의 메서드
    public boolean hasThumbnailUrl() {
        return thumbnailUrl != null && !thumbnailUrl.isEmpty();
    }

    public boolean hasThumbnailRes() {
        return thumbnailResId != 0;
    }
}

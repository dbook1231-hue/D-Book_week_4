package com.example.d_book.item;

public class TrendingBook {
    private final String title;
    private final String author;
    private final int searchCount;
    private final String thumbnailUrl;

    public TrendingBook(String title, String author, int searchCount, String thumbnailUrl) {
        this.title = title;
        this.author = author;
        this.searchCount = searchCount;
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getSearchCount() {
        return searchCount;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
}

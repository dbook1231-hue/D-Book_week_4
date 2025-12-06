package com.example.d_book.item;

public class FavoriteBookItem {
    private final String bookId;     // Firestore 문서 ID
    private final String title;
    private final String author;
    private final String category;
    private final String thumbnail;

    public FavoriteBookItem(String bookId, String title, String author, String category, String thumbnail) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.category = category;
        this.thumbnail = thumbnail;
    }

    public String getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public String getThumbnail() { return thumbnail; }
}

package com.example.d_book.model;

public class UserModel {
    public String userName;
    public String profileImageUrl;
    public String uid;
    public String pushToken;
    public String comment;

    // 🔹 통계 필드 추가
    public long reviewCount;
    public long likeCount;
    public long replyCount;
    public long favoriteCount;
    public UserModel() {} // 기본 생성자 반드시 필요
}

package com.example.d_book;

import android.text.TextUtils;

/**
 * Resolves fallback thumbnails for known titles when the stored URL is missing.
 */
public final class ThumbnailHelper {

    private static final String DEMIAN_REMOTE = "https://covers.openlibrary.org/b/isbn/9780143106784-L.jpg";
    private static final String DEMIAN_LOCAL = "android.resource://com.example.d_book/drawable/demian_cover";

    private ThumbnailHelper() {}

    /**
     * Thumbnail to display in the app (prefers local fallback for known titles).
     */
    public static String display(String thumbnail, String title) {
        if (!isNullOrEmpty(thumbnail)) return thumbnail;
        if (isDemian(title)) return DEMIAN_LOCAL;
        return thumbnail;
    }

    /**
     * Thumbnail to persist in Firestore when missing (remote URL to share across clients).
     */
    public static String storage(String thumbnail, String title) {
        if (!isNullOrEmpty(thumbnail)) return thumbnail;
        if (isDemian(title)) return DEMIAN_REMOTE;
        return thumbnail;
    }

    public static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static boolean isDemian(String title) {
        if (TextUtils.isEmpty(title)) return false;
        String lower = title.toLowerCase();
        return title.contains("데미안") || lower.contains("demian");
    }
}

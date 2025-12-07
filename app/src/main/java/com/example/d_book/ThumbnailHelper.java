package com.example.d_book;

import android.text.TextUtils;
import com.example.d_book.R;

/**
 * Resolves fallback thumbnails for known titles when the stored URL is missing.
 */
public final class ThumbnailHelper {

    private static final String DEMIAN_REMOTE = "https://covers.openlibrary.org/b/isbn/9780143106784-L.jpg";
    private static final String DEMIAN_LOCAL = "android.resource://com.example.d_book/drawable/demian_cover";
    // UTF-8이 깨진 "데미안" / "헤르만 헤세" 문자열 패턴
    private static final String DEMIAN_MOJIBAKE = "ë\u009d°ë¯¸ì\u0095\u0088";
    private static final String HESSE_MOJIBAKE = "í\u0097¤ë¥´ë§ í\u0097¤ì\u0084¸";

    private ThumbnailHelper() {}

    /**
     * Thumbnail to display in the app (prefers local fallback for known titles).
     */
    public static String display(String thumbnail, String title, String author) {
        if (isDemian(title, author)) return DEMIAN_LOCAL;
        if (!isNullOrEmpty(thumbnail)) return thumbnail;
        return thumbnail; // null/empty
    }

    /**
     * Thumbnail to persist in Firestore when missing (remote URL to share across clients).
     */
    public static String storage(String thumbnail, String title, String author) {
        if (isDemian(title, author)) return DEMIAN_REMOTE;
        if (!isNullOrEmpty(thumbnail)) return thumbnail;
        return thumbnail;
    }

    /**
     * Local drawable resource fallback id for known titles. Returns 0 if none.
     */
    public static int fallbackRes(String title, String author) {
        if (isDemian(title, author)) return R.drawable.demian_cover;
        return 0;
    }

    public static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static boolean isDemian(String title, String author) {
        // 정상이거나 영문
        if (!TextUtils.isEmpty(title)) {
            String lower = title.toLowerCase();
            if (title.contains("데미안") || lower.contains("demian") || title.contains(DEMIAN_MOJIBAKE)) {
                return true;
            }
        }
        // 작가명으로 판별 (정상/영문/깨짐)
        if (!TextUtils.isEmpty(author)) {
            String lowerA = author.toLowerCase();
            if (author.contains("헤르만") || (lowerA.contains("hermann") && lowerA.contains("hesse")) || author.contains(HESSE_MOJIBAKE)) {
                return true;
            }
        }
        return false;
    }
}

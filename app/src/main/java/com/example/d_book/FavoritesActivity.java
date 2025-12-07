package com.example.d_book;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.d_book.adapter.FavoriteBookAdapter;
import com.example.d_book.item.FavoriteBookItem;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView recyclerFavorites;
    private FavoriteBookAdapter adapter;
    private List<FavoriteBookItem> favoriteBooks = new ArrayList<>();

    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    private ListenerRegistration favoriteListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // 🔹 Toolbar 뒤로가기
        MaterialToolbar toolbar = findViewById(R.id.searchToolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerFavorites = findViewById(R.id.recyclerFavorites);
        recyclerFavorites.setLayoutManager(new LinearLayoutManager(this));

        adapter = new FavoriteBookAdapter(favoriteBooks, this::openBookDetail);
        recyclerFavorites.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            listenFavoriteBooks();
        } else {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Firestore 즐겨찾기 실시간 감지
     */
    private void listenFavoriteBooks() {
        // 초기화 (선택)
        favoriteBooks.clear();
        adapter.notifyDataSetChanged();

        favoriteListener = firestore.collection("users")
                .document(currentUser.getUid())
                .collection("favorites")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        String docId = dc.getDocument().getId(); // favorites 문서 ID (bookId 또는 title)

                        switch (dc.getType()) {
                            case ADDED:
                                // 문서 필드에서 바로 꺼내 새로운 FavoriteBookItem 생성
                                String title = dc.getDocument().getString("title");
                                String author = dc.getDocument().getString("author");
                                String category = dc.getDocument().getString("category");
                                String rawThumb = dc.getDocument().getString("thumbnail");
                                String displayThumb = ThumbnailHelper.display(rawThumb, title);
                                String storageThumb = ThumbnailHelper.storage(rawThumb, title);
                                if (ThumbnailHelper.isNullOrEmpty(rawThumb) && !ThumbnailHelper.isNullOrEmpty(storageThumb)) {
                                    dc.getDocument().getReference().update("thumbnail", storageThumb);
                                }

                                // 널 대비 기본값
                                if (title == null) title = "제목 없음";
                                if (author == null) author = "작가 정보 없음";
                                if (category == null) category = "카테고리 없음";
                                if (displayThumb == null) displayThumb = "";

                                // 중복 체크 (bookId 기준)
                                boolean exists = false;
                                for (FavoriteBookItem it : favoriteBooks) {
                                    if (it.getBookId().equals(docId)) { exists = true; break; }
                                }
                                if (!exists) {
                                    favoriteBooks.add(new FavoriteBookItem(docId, title, author, category, displayThumb));
                                    adapter.notifyItemInserted(favoriteBooks.size() - 1);
                                }
                                break;

                            case MODIFIED:
                                // 문서가 수정되면 리스트의 해당 항목을 찾아 업데이트
                                for (int i = 0; i < favoriteBooks.size(); i++) {
                                    if (favoriteBooks.get(i).getBookId().equals(docId)) {
                                        String mTitle = dc.getDocument().getString("title");
                                        String mAuthor = dc.getDocument().getString("author");
                                        String mCategory = dc.getDocument().getString("category");
                                        String rawMThumb = dc.getDocument().getString("thumbnail");
                                        String mDisplayThumb = ThumbnailHelper.display(rawMThumb, mTitle);
                                        String mStorageThumb = ThumbnailHelper.storage(rawMThumb, mTitle);
                                        if (ThumbnailHelper.isNullOrEmpty(rawMThumb) && !ThumbnailHelper.isNullOrEmpty(mStorageThumb)) {
                                            dc.getDocument().getReference().update("thumbnail", mStorageThumb);
                                        }

                                        if (mTitle == null) mTitle = favoriteBooks.get(i).getTitle();
                                        if (mAuthor == null) mAuthor = favoriteBooks.get(i).getAuthor();
                                        if (mCategory == null) mCategory = favoriteBooks.get(i).getCategory();
                                        if (mDisplayThumb == null) mDisplayThumb = favoriteBooks.get(i).getThumbnail();

                                        favoriteBooks.set(i, new FavoriteBookItem(docId, mTitle, mAuthor, mCategory, mDisplayThumb));
                                        adapter.notifyItemChanged(i);
                                        break;
                                    }
                                }
                                break;

                            case REMOVED:
                                // 제거 이벤트 처리
                                for (int i = 0; i < favoriteBooks.size(); i++) {
                                    if (favoriteBooks.get(i).getBookId().equals(docId)) {
                                        favoriteBooks.remove(i);
                                        adapter.notifyItemRemoved(i);
                                        break;
                                    }
                                }
                                break;
                        }
                    }
                });
    }

    /**
     * 상세 화면 이동
     */
    private void openBookDetail(FavoriteBookItem item) {
        Intent intent = new Intent(this, BookDetailActivity.class);

        intent.putExtra("bookId", item.getBookId());
        intent.putExtra("title", item.getTitle());
        intent.putExtra("author", item.getAuthor());
        intent.putExtra("category", item.getCategory());
        intent.putExtra("thumbnail", item.getThumbnail());

        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (favoriteListener != null) favoriteListener.remove();
    }
}

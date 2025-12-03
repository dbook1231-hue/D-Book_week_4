package com.example.d_book;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.d_book.adapter.SearchResultAdapter;
import com.example.d_book.item.SearchResultItem;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private MaterialToolbar searchToolbar;
    private EditText editSearch;
    private TabLayout tabCategories;
    private RecyclerView recyclerSearchResults;

    private SearchResultAdapter adapter;
    private List<SearchResultItem> searchResults;
    private List<SearchResultItem> allBooks = new ArrayList<>();

    private FirebaseFirestore db;
    private String selectedCategory = "전체"; // 현재 선택된 카테고리

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // 🔹 Firestore 초기화
        db = FirebaseFirestore.getInstance();

        // 🔹 뷰 초기화
        initViews();

        // 🔹 툴바 설정
        setSupportActionBar(searchToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("검색");
        }

        // 🔹 RecyclerView 설정
        setupRecyclerView();

        // 🔹 Firestore에서 책 데이터 불러오기
        loadBooksFromFirestore();

        // 🔹 검색어 감지
        setupSearchListener();

        // 🔹 카테고리 탭 초기화
        initTabs();
    }

    private void initViews() {
        searchToolbar = findViewById(R.id.searchToolbar);
        editSearch = findViewById(R.id.editSearch);
        tabCategories = findViewById(R.id.tabCategories);
        recyclerSearchResults = findViewById(R.id.recyclerSearchResults);
    }

    private void setupRecyclerView() {
        searchResults = new ArrayList<>();
        adapter = new SearchResultAdapter(this, searchResults, item -> {
            // 🔹 책 클릭 시 BookDetailActivity로 이동
            Intent intent = new Intent(SearchActivity.this, BookDetailActivity.class);
            intent.putExtra("title", item.getTitle());
            intent.putExtra("author", item.getAuthor());
            intent.putExtra("thumbnail", item.getThumbnailUrl());
            intent.putExtra("category", item.getCategory());
            startActivity(intent);

            // 🔹 방문수 증가 (optional, Firestore 업데이트)
            incrementVisitCount(item.getTitle());
        });

        recyclerSearchResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerSearchResults.setAdapter(adapter);
    }

    private void setupSearchListener() {
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable s) {
                filterBooks(s.toString(), selectedCategory);
            }
        });
    }

    // 🔹 Firestore에서 전체 책 불러오기
    private void loadBooksFromFirestore() {
        db.collection("books")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allBooks.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String title = doc.getString("title");
                        String author = doc.getString("author");
                        String thumbnail = doc.getString("thumbnail");
                        String category = doc.getString("category");
                        Long visitCount = doc.getLong("visitCount"); // optional

                        allBooks.add(new SearchResultItem(title, author, thumbnail, category, visitCount != null ? visitCount.intValue() : 0));
                    }

                    filterBooks(editSearch.getText().toString(), selectedCategory);
                    Log.d("FIRESTORE", "Firestore 책 불러오기 완료 (" + allBooks.size() + "개)");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "데이터 불러오기 실패", Toast.LENGTH_SHORT).show();
                    Log.e("FIRESTORE", "에러: ", e);
                });
    }

    // 🔹 검색 + 카테고리 필터링
    private void filterBooks(String query, String category) {
        searchResults.clear();
        for (SearchResultItem book : allBooks) {
            boolean matchesQuery = query.isEmpty() ||
                    book.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    book.getAuthor().toLowerCase().contains(query.toLowerCase());

            boolean matchesCategory = category.equals("전체") ||
                    (book.getCategory() != null && book.getCategory().equals(category));

            if (matchesQuery && matchesCategory) {
                searchResults.add(book);
            }
        }
        adapter.notifyDataSetChanged();
    }

    // 🔹 카테고리 탭 초기화
    private void initTabs() {
        String[] categories = {"전체", "소설", "에세이", "자기계발", "인문학"};
        for (String cat : categories) {
            tabCategories.addTab(tabCategories.newTab().setText(cat));
        }

        tabCategories.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedCategory = tab.getText() != null ? tab.getText().toString() : "전체";
                filterBooks(editSearch.getText().toString(), selectedCategory);
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    // 🔹 책 클릭 시 Firestore visitCount 증가 (optional)
    private void incrementVisitCount(String title) {
        db.collection("books")
                .whereEqualTo("title", title)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        int currentCount = doc.getLong("visitCount") != null ? doc.getLong("visitCount").intValue() : 0;
                        doc.getReference().update("visitCount", currentCount + 1);
                    }
                })
                .addOnFailureListener(e -> Log.e("FIRESTORE", "visitCount 업데이트 실패", e));
    }

    // 🔹 뒤로가기 버튼
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

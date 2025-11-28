package com.example.d_book;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private MaterialToolbar searchToolbar;
    private EditText editSearch;
    private TabLayout tabCategories;
    private RecyclerView recyclerSearchResults;

    private SearchResultAdapter adapter;
    private List<SearchResultItem> searchResults;
    private List<SearchResultItem> allBooks; // 전체 도서 리스트 (샘플)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // 뷰 초기화
        searchToolbar = findViewById(R.id.searchToolbar);
        editSearch = findViewById(R.id.editSearch);
        tabCategories = findViewById(R.id.tabCategories);
        recyclerSearchResults = findViewById(R.id.recyclerSearchResults);

        // 툴바 뒤로가기 버튼
        setSupportActionBar(searchToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("검색");
        }

        // 샘플 도서 데이터 초기화
        initSampleBooks();

        // RecyclerView 세팅
        searchResults = new ArrayList<>();
        adapter = new SearchResultAdapter(this, searchResults, item -> {
            // 클릭 시 책 상세 페이지 이동
            Intent intent = new Intent(SearchActivity.this, BookDetailActivity.class);
            intent.putExtra("title", item.getTitle());
            intent.putExtra("author", item.getAuthor());
            intent.putExtra("thumbnail", item.getThumbnailUrl());
            startActivity(intent);
        });
        recyclerSearchResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerSearchResults.setAdapter(adapter);

        String initialQuery = getIntent().getStringExtra("query");
        if (initialQuery != null && !initialQuery.trim().isEmpty()) {
            editSearch.setText(initialQuery);
            editSearch.setSelection(initialQuery.length());
            filterBooks(initialQuery);
        } else {
            filterBooks("");
        }

        // 검색 입력 감지
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                filterBooks(s.toString());
            }
        });

        // 카테고리 탭 초기화
        initTabs();
    }

    // 샘플 도서 데이터
    private void initSampleBooks() {
        allBooks = new ArrayList<>();
        allBooks.add(new SearchResultItem("해리 포터와 마법사의 돌", "J.K. 롤링", "https://covers.openlibrary.org/b/isbn/9780439708180-L.jpg"));
        allBooks.add(new SearchResultItem("해리 포터와 비밀의 방", "J.K. 롤링", "https://covers.openlibrary.org/b/isbn/9780439064873-L.jpg"));
        allBooks.add(new SearchResultItem("해리 포터와 아즈카반의 죄수", "J.K. 롤링", "https://covers.openlibrary.org/b/isbn/9780439136365-L.jpg"));
        allBooks.add(new SearchResultItem("해리 포터와 불의 잔", "J.K. 롤링", "https://covers.openlibrary.org/b/isbn/9780439139601-L.jpg"));
        allBooks.add(new SearchResultItem("해리 포터와 불사조 기사단", "J.K. 롤링", "https://covers.openlibrary.org/b/isbn/9780439358071-L.jpg"));
        allBooks.add(new SearchResultItem("해리 포터와 혼혈 왕자", "J.K. 롤링", "https://covers.openlibrary.org/b/isbn/9780439785969-L.jpg"));
        allBooks.add(new SearchResultItem("해리 포터와 죽음의 성물", "J.K. 롤링", "https://covers.openlibrary.org/b/isbn/9780545010221-L.jpg"));
        allBooks.add(new SearchResultItem("반지의 제왕: 반지 원정대", "J.R.R. 톨킨", "https://covers.openlibrary.org/b/isbn/9780547928210-L.jpg"));
        allBooks.add(new SearchResultItem("반지의 제왕: 두 개의 탑", "J.R.R. 톨킨", "https://covers.openlibrary.org/b/isbn/9780547928203-L.jpg"));
        allBooks.add(new SearchResultItem("반지의 제왕: 왕의 귀환", "J.R.R. 톨킨", "https://covers.openlibrary.org/b/isbn/9780547928197-L.jpg"));
        allBooks.add(new SearchResultItem("어린 왕자", "앙투안 드 생텍쥐페리", "https://covers.openlibrary.org/b/isbn/9780156012195-L.jpg"));
        allBooks.add(new SearchResultItem("나미야 잡화점의 기적", "히가시노 게이고", R.drawable.namiya_cover));
        allBooks.add(new SearchResultItem("데미안", "헤르만 헤세", "https://covers.openlibrary.org/b/isbn/9780143106784-L.jpg"));
        allBooks.add(new SearchResultItem("노인과 바다", "어니스트 헤밍웨이", "https://covers.openlibrary.org/b/isbn/9780684801223-L.jpg"));
        allBooks.add(new SearchResultItem("위대한 개츠비", "F. 스콧 피츠제럴드", "https://covers.openlibrary.org/b/isbn/9780743273565-L.jpg"));
    }

    // 검색 필터링
    private void filterBooks(String query) {
        searchResults.clear();
        if (query.isEmpty()) {
            searchResults.addAll(allBooks);
        } else {
            for (SearchResultItem book : allBooks) {
                if (book.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        book.getAuthor().toLowerCase().contains(query.toLowerCase())) {
                    searchResults.add(book);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    // 샘플 카테고리 탭
    private void initTabs() {
        String[] categories = {"전체", "소설", "에세이", "자기계발", "인문학"};
        for (String cat : categories) {
            tabCategories.addTab(tabCategories.newTab().setText(cat));
        }

        // 탭 선택 이벤트
        tabCategories.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Toast.makeText(SearchActivity.this, tab.getText() + " 선택됨", Toast.LENGTH_SHORT).show();
                // 실제 앱에서는 카테고리별 필터링 로직 추가
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    // 툴바 뒤로가기 처리
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
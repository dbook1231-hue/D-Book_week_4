package com.example.d_book.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.d_book.R;
import com.example.d_book.ThumbnailHelper;
import com.example.d_book.item.SearchResultItem;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.SearchViewHolder> {

    private final Context context;
    private final List<SearchResultItem> searchResults;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(SearchResultItem item);
    }

    public SearchResultAdapter(Context context, List<SearchResultItem> searchResults, OnItemClickListener listener) {
        this.context = context;
        this.searchResults = searchResults;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_result, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        SearchResultItem item = searchResults.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return (searchResults != null) ? searchResults.size() : 0;
    }

    static class SearchViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageThumbnail;
        private final TextView textTitle;
        private final TextView textAuthor;
        private final TextView textCategory;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            imageThumbnail = itemView.findViewById(R.id.imageThumbnail);
            textTitle = itemView.findViewById(R.id.textTitle);
            textAuthor = itemView.findViewById(R.id.textAuthor);
            textCategory = itemView.findViewById(R.id.textCategory);
        }

        public void bind(final SearchResultItem item, final OnItemClickListener listener) {
            if (item == null) return;

            textTitle.setText(item.getTitle() != null ? item.getTitle() : "제목 없음");
            textAuthor.setText(item.getAuthor() != null ? item.getAuthor() : "작가 정보 없음");

            if (item.getCategory() != null && !item.getCategory().isEmpty()) {
                textCategory.setText(item.getCategory());
                textCategory.setVisibility(View.VISIBLE);
            } else {
                textCategory.setVisibility(View.GONE);
            }

            int fallbackRes = ThumbnailHelper.fallbackRes(item.getTitle(), item.getAuthor());

            // 데미안은 무조건 로컬 표지 사용
            if (fallbackRes != 0) {
                imageThumbnail.setImageResource(fallbackRes);
            } else if (item.hasThumbnailRes()) {
                Glide.with(itemView.getContext())
                        .load(item.getThumbnailResId())
                        .placeholder(R.drawable.ic_book_placeholder)
                        .error(R.drawable.ic_book_placeholder)
                        .into(imageThumbnail);
            } else if (item.hasThumbnailUrl()) {
                Glide.with(itemView.getContext())
                        .load(item.getThumbnailUrl())
                        .placeholder(R.drawable.ic_book_placeholder)
                        .error(R.drawable.ic_book_placeholder)
                        .into(imageThumbnail);
            } else {
                imageThumbnail.setImageResource(R.drawable.ic_book_placeholder);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}

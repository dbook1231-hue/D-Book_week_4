package com.example.d_book.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.d_book.R;
import com.example.d_book.item.TrendingBook;

import java.util.List;

public class TrendingBookAdapter extends RecyclerView.Adapter<TrendingBookAdapter.TrendingViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(TrendingBook item);
    }

    private final Context context;
    private final List<TrendingBook> items;
    private final OnItemClickListener listener;

    public TrendingBookAdapter(Context context, List<TrendingBook> items, OnItemClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TrendingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trending_book, parent, false);
        return new TrendingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrendingViewHolder holder, int position) {
        holder.bind(items.get(position), position, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class TrendingViewHolder extends RecyclerView.ViewHolder {
        TextView textRank;
        ImageView imageThumbnail;
        TextView textTitle;
        TextView textAuthor;
        TextView textSearchCount;

        public TrendingViewHolder(@NonNull View itemView) {
            super(itemView);
            textRank = itemView.findViewById(R.id.textRank);
            imageThumbnail = itemView.findViewById(R.id.imageThumbnail);
            textTitle = itemView.findViewById(R.id.textTitle);
            textAuthor = itemView.findViewById(R.id.textAuthor);
            textSearchCount = itemView.findViewById(R.id.textSearchCount);
        }

        public void bind(final TrendingBook item, int position, final OnItemClickListener listener) {
            textRank.setText(String.valueOf(position + 1));
            textTitle.setText(item.getTitle());
            textAuthor.setText(item.getAuthor());
            textSearchCount.setText("검색량: " + item.getSearchCount());

            if (item.getThumbnailResId() != 0) {
                Glide.with(itemView.getContext())
                        .load(item.getThumbnailResId())
                        .placeholder(R.drawable.ic_book_placeholder)
                        .error(R.drawable.ic_book_placeholder)
                        .into(imageThumbnail);
            } else if (item.getThumbnailUrl() != null && !item.getThumbnailUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(item.getThumbnailUrl())
                        .placeholder(R.drawable.ic_book_placeholder)
                        .error(R.drawable.ic_book_placeholder)
                        .into(imageThumbnail);
            } else {
                imageThumbnail.setImageResource(R.drawable.ic_book_placeholder);
            }

            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}
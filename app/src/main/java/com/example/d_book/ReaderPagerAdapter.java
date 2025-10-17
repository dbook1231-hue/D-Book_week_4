package com.example.d_book;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReaderPagerAdapter extends RecyclerView.Adapter<ReaderPagerAdapter.PageViewHolder> {

    private final List<String> pages;

    public ReaderPagerAdapter(List<String> pages) {
        this.pages = pages;
    }

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reader_page, parent, false);
        return new PageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        holder.textPage.setText(pages.get(position));
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }

    static class PageViewHolder extends RecyclerView.ViewHolder {
        TextView textPage;
        public PageViewHolder(@NonNull View itemView) {
            super(itemView);
            textPage = itemView.findViewById(R.id.textPage);
        }
    }
}
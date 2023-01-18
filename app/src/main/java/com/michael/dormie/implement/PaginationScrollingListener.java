package com.michael.dormie.implement;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class PaginationScrollingListener extends RecyclerView.OnScrollListener {
    private final LinearLayoutManager linearLayoutManager;

    public PaginationScrollingListener(LinearLayoutManager linearLayoutManager) {
        this.linearLayoutManager = linearLayoutManager;
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        int visibleItemCount = linearLayoutManager.getChildCount();
        int totalItemCount = linearLayoutManager.getItemCount();
        int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();

        if (isLoading() || isLastPage()) return;
        if (firstVisibleItemPosition >= 0 && visibleItemCount + firstVisibleItemPosition >= totalItemCount) {
            Log.e("Pagination", "Is Loading");
            onLoadItem();
        }
    }

    public abstract void onLoadItem();

    public abstract boolean isLoading();

    public abstract boolean isLastPage();
}

package com.example.smartinganything.RecyclerView;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class AutoFitGridLayoutManager extends GridLayoutManager {

    public AutoFitGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
        autoSizeItems();
    }

    private void autoSizeItems() {
        int totalSpace = getWidth() - getPaddingStart() - getPaddingEnd();
        int spanCount = getSpanCount();
        int itemSize = totalSpace / spanCount;

        setMeasuredDimension(getWidth(), itemSize * spanCount);

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            assert child != null;
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            params.width = itemSize;
            params.height = itemSize;
            child.setLayoutParams(params);
        }
    }
}
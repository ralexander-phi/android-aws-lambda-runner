package com.alexsci.android.lambdarunner.ui;

import android.content.Context;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public abstract class ExpandableItemArrayAdapter<T> extends BaseAdapter {
    protected Context context;
    protected ArrayList<T> items;

    public ExpandableItemArrayAdapter(Context context, List<T> items) {
        this.context = context;
        this.items = new ArrayList<>(items);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}

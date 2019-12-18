package com.alexsci.android.lambdarunner.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alexsci.android.lambdarunner.R

open class BaseArrayAdapter<T>(
    protected val data: MutableList<T>
): RecyclerView.Adapter<ViewHolder<T>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<T> {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder<T>, position: Int) {
        holder.t = data[position]
    }

    override fun getItemCount() = data.size
}


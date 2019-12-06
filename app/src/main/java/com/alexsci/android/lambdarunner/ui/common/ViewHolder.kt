package com.alexsci.android.lambdarunner.ui.common

import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alexsci.android.lambdarunner.R

class ViewHolder<T>(
    val view: View,
    var t: T? = null
): RecyclerView.ViewHolder(view) {
    val title: TextView = view.findViewById(R.id.title)
    val description: TextView = view.findViewById(R.id.description)
    val remove: ImageButton = view.findViewById(R.id.remove)
    val run: ImageButton = view.findViewById(R.id.run)
}
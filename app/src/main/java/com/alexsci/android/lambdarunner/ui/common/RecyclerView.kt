package com.alexsci.android.lambdarunner.ui.common

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView


class VerticalSpaceItemDecorator: RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        // Space between items
        val itemCount = parent.adapter?.itemCount
        if (itemCount != null && parent.getChildAdapterPosition(view) != itemCount - 1) {
            outRect.bottom = 25
        }
    }
}

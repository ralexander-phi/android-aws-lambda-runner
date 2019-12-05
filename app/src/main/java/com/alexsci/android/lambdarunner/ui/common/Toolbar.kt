package com.alexsci.android.lambdarunner.ui.common

import android.content.Context
import android.view.MenuItem
import com.alexsci.android.lambdarunner.R

class ToolbarHelper {
    fun onOptionsItemSelected(context: Context, item: MenuItem): Boolean? {
        return when (item.itemId) {
            R.id.about -> {
                DialogHelper().showAbout(context)
                true
            }

            R.id.feedback -> {
                DialogHelper().showFeedback(context)
                true
            }

            else -> null
        }
    }
}
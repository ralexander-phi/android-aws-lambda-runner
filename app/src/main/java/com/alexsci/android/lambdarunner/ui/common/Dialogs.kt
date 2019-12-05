package com.alexsci.android.lambdarunner.ui.common

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri

class DialogHelper {

    fun showAbout(context: Context) {
        show(context, "TODO app name", "Open Website", "https://alexsci.com/android-apps/")
    }

    fun showFeedback(context: Context) {
        show(context, "Submit feedback", "Open Github", "https://www.github.com/")
    }

    private fun show(context: Context, message: String, ok: String, url: String) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(message)
        builder.setPositiveButton(ok) { dialog, _ ->
            dialog.dismiss()
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            context.startActivity(intent)
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }
}


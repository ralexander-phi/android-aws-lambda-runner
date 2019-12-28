package com.alexsci.android.lambdarunner.ui.add_key.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.text.Html
import android.text.method.LinkMovementMethod
import android.widget.TextView
import com.alexsci.android.lambdarunner.R

class QRCodeHelpDialog {
    companion object {
        fun showQRCodeHint(activity: Activity) {
            val builder = AlertDialog.Builder(activity)
            val view = activity.layoutInflater.inflate(R.layout.qr_code_help_view, null)
            view.findViewById<TextView>(R.id.help_text).also {
                it.text = Html.fromHtml(activity.resources.getString(R.string.qr_code_help))
                it.movementMethod = LinkMovementMethod.getInstance()
            }
            builder.setView(view)
            builder.setMessage("Load credentials via QR code")
            builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            builder.create().show()
        }
    }
}
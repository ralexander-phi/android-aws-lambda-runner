package com.alexsci.android.lambdarunner.util.crypto

import android.content.Context

class AddKeyThread(val context: Context, val keyName: String, val description: String, val keySecret: String) : Thread() {
    private var success: Boolean? = null

    override fun run() {
        try {
            val keyManager = KeyManagement.getInstance(context)
            success = keyManager.addKey(keyName, description, keySecret)
        } finally {
            if (success == null) {
                success = false
            }
        }
    }
}

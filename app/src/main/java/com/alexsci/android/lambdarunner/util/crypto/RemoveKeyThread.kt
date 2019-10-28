package com.alexsci.android.lambdarunner.util.crypto

import android.content.Context

class RemoveKeyThread(val context: Context, val keyName: String) : Thread() {
    private var success: Boolean? = null

    override fun run() {
        try {
            val keyManager = KeyManagement.getInstance(context)
            success = keyManager.deleteKey(keyName)
        } finally {
            if (success == null) {
                success = false
            }
        }
    }
}


package com.alexsci.android.lambdarunner.util.crypto

import android.content.Context

class GetKeysThread(private val context: Context, private val keyName: String) : Thread() {
    var keySecret: String? = null

    override fun run() {
        val keyManager = KeyManagement.getInstance(context)
        keySecret = keyManager.getKey(keyName)
    }
}


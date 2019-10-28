package com.alexsci.android.lambdarunner.util.crypto

import android.content.Context

class ListKeysThread(private val context: Context) : Thread() {
    var keys: Collection<String>? = null

    override fun run() {
        val keyManager = KeyManagement.getInstance(context)
        keys = keyManager.listKeys()
    }
}


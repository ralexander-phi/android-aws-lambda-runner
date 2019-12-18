package com.alexsci.android.lambdarunner.data.list_keys

import com.alexsci.android.lambdarunner.data.list_keys.model.Key

class KeysRepository(private val dataSource: KeysDataSource) {
    fun list(): List<Key> {
        return dataSource.listKeys()
    }

    fun remove(key: Key) {
        dataSource.remove(key)
    }
}
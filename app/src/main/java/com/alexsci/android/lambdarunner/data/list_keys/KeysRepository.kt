package com.alexsci.android.lambdarunner.data.list_keys

import com.alexsci.android.lambdarunner.data.list_keys.model.Key

class KeysRepository(val dataSource: KeysDataSource) {
    var functions: List<Key>? = null
        private set

    fun list(): List<Key>? {
        functions = dataSource.listKeys()
        return functions
    }

    fun remove(key: Key) {
        dataSource.remove(key)
    }
}
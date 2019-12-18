package com.alexsci.android.lambdarunner.ui.list_keys

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alexsci.android.lambdarunner.data.list_keys.KeysDataSource
import com.alexsci.android.lambdarunner.data.list_keys.KeysRepository
import com.alexsci.android.lambdarunner.util.crypto.KeyManagement

class ListKeysViewModelFactory(private val keyManagement: KeyManagement): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListKeysViewModel::class.java)) {
            return ListKeysViewModel(
                KeysRepository(
                    KeysDataSource(
                        keyManagement
                    )
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

package com.alexsci.android.lambdarunner.ui.list_keys

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alexsci.android.lambdarunner.data.list_keys.KeysRepository
import com.alexsci.android.lambdarunner.data.list_keys.model.Key

class ListKeysViewModel(private val keysRepository: KeysRepository) : ViewModel() {

    private val _listResult = MutableLiveData<ListKeysResult>()
    val listResult: LiveData<ListKeysResult> = _listResult

    fun list() {
        ListTask().execute(null)
    }

    fun remove(key: Key) {
        RemoveKeyTask(key).execute(null)
    }

    private open inner class ListTask: AsyncTask<Void, Void, ListKeysResult>() {
        override fun doInBackground(vararg params: Void): ListKeysResult {
            val keys = keysRepository.list()
            if (keys != null) {
                return ListKeysResult(KeyListView(keys), null)
            }
            return ListKeysResult(null, 0) // TODO
        }

        override fun onPostExecute(listKeysResult: ListKeysResult?) {
            super.onPostExecute(listKeysResult)
            _listResult.value = listKeysResult
        }
    }

    private inner class RemoveKeyTask(
        val key: Key
    ): ListTask() {
        override fun doInBackground(vararg params: Void): ListKeysResult {
            keysRepository.remove(key)

            return super.doInBackground(*params)
        }
    }
}
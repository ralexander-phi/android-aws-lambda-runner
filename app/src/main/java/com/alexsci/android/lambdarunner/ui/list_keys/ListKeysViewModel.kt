package com.alexsci.android.lambdarunner.ui.list_keys

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alexsci.android.lambdarunner.data.list_functions.KeysRepository

private class ListTask(
    val repo: KeysRepository,
    val result: MutableLiveData<ListKeysResult>
): AsyncTask<Void, Void, ListKeysResult>() {
    override fun doInBackground(vararg params: Void): ListKeysResult {
        val keys = repo.list()
        if (keys != null) {
            return ListKeysResult(KeyListView(keys), null)
        }
        return ListKeysResult(null, 0) // TODO
    }


    override fun onPostExecute(listKeysResult: ListKeysResult?) {
        super.onPostExecute(listKeysResult)
        result.value = listKeysResult
    }
}

class ListKeysViewModel(private val keysRepository: KeysRepository) : ViewModel() {

    private val _listResult = MutableLiveData<ListKeysResult>()
    val listResult: LiveData<ListKeysResult> = _listResult

    fun list() {
        ListTask(
            keysRepository,
            _listResult
        ).execute(null)
    }
}
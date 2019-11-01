package com.alexsci.android.lambdarunner.ui.list_functions

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alexsci.android.lambdarunner.data.list_functions.FunctionsRepository

private class ListTask(
    val repo: FunctionsRepository,
    val result: MutableLiveData<ListFunctionsResult>
): AsyncTask<Void, Void, ListFunctionsResult>() {
    override fun doInBackground(vararg params: Void): ListFunctionsResult {
        val functions = repo.list()
        if (functions != null) {
            return ListFunctionsResult(FunctionListView(functions), null)
        }
        return ListFunctionsResult(null, 0) // TODO
    }

    override fun onPostExecute(listFunctionsResult: ListFunctionsResult?) {
        super.onPostExecute(listFunctionsResult)
        result.value = listFunctionsResult
    }
}

class ListFunctionsViewModel(val functionsRepository: FunctionsRepository) : ViewModel() {

    private val _listResult = MutableLiveData<ListFunctionsResult>()
    val listResult: LiveData<ListFunctionsResult> = _listResult

    fun list() {
        ListTask(functionsRepository, _listResult).execute(null)
    }
}
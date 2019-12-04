package com.alexsci.android.lambdarunner.ui.list_functions

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import arrow.core.Either
import com.alexsci.android.lambdarunner.data.list_functions.FunctionsRepository
import java.lang.Exception
import com.alexsci.android.lambdarunner.data.list_functions.model.Function

private class ListTask(
    val repo: FunctionsRepository,
    val result: MutableLiveData<Either<Exception, MutableList<Function>>>
): AsyncTask<String, Void, Either<Exception, MutableList<Function>>>() {
    override fun doInBackground(vararg params: String): Either<Exception, MutableList<Function>>? {
        return repo.list(params[0], params[1])
    }

    override fun onPostExecute(listFunctionsResult: Either<Exception, MutableList<Function>>?) {
        super.onPostExecute(listFunctionsResult)
        result.value = listFunctionsResult
    }
}

class ListFunctionsViewModel(private val functionsRepository: FunctionsRepository) : ViewModel() {

    private val _listResult = MutableLiveData<Either<Exception, MutableList<Function>>>()
    val listResult: LiveData<Either<Exception, MutableList<Function>>> = _listResult

    fun list(accessKeyId: String, region: String) {
        ListTask(functionsRepository, _listResult).execute(accessKeyId, region)
    }
}
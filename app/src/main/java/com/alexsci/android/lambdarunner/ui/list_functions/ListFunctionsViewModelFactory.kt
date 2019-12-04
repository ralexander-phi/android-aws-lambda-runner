package com.alexsci.android.lambdarunner.ui.list_functions

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alexsci.android.lambdarunner.aws.lambda.LambdaClient
import com.alexsci.android.lambdarunner.data.list_functions.FunctionsDataSource
import com.alexsci.android.lambdarunner.data.list_functions.FunctionsRepository

class ListFunctionsViewModelFactory(private val context: Context): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListFunctionsViewModel::class.java)) {
            return ListFunctionsViewModel(
                FunctionsRepository(
                    FunctionsDataSource(context)
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

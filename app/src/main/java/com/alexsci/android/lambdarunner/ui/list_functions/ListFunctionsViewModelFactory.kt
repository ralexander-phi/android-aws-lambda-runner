package com.alexsci.android.lambdarunner.ui.list_functions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alexsci.android.lambdarunner.aws.lambda.LambdaClient
import com.alexsci.android.lambdarunner.data.list_functions.FunctionsDataSource
import com.alexsci.android.lambdarunner.data.list_functions.FunctionsRepository

class ListFunctionsViewModelFactory(var lambdaClient: LambdaClient): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListFunctionsViewModel::class.java)) {
            return ListFunctionsViewModel(
                FunctionsRepository(
                    FunctionsDataSource(
                        lambdaClient
                    )
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

package com.alexsci.android.lambdarunner.ui.list_functions

import com.alexsci.android.lambdarunner.ui.common.BaseListResult

class ListFunctionsResult(
    success: FunctionListView? = null,
    error: Int? = null
): BaseListResult<FunctionListView>(success, error)

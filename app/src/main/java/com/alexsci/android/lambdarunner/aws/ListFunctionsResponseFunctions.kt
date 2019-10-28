package com.alexsci.android.lambdarunner.aws

import java.io.Serializable

data class ListFunctionsResponseFunctions(
    var description: String? = null,
    var functionArn: String? = null,
    var functionName: String? = null,
    var lastModified: String? = null
) : Serializable
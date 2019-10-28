package com.alexsci.android.lambdarunner.aws

import java.io.Serializable
import java.util.*

data class ListFunctionsResponse(
    var functions: List<ListFunctionsResponseFunctions>? = null,
    var nextMarker: String? = null,
    var statusCode : Integer? = null
) : Serializable


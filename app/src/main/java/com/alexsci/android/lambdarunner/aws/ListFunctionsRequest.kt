package com.alexsci.android.lambdarunner.aws

import com.amazonaws.AmazonWebServiceRequest
import java.io.Serializable

data class ListFunctionsRequest(val marker: String?) : AmazonWebServiceRequest(), Serializable

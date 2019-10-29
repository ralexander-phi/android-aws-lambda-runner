package com.alexsci.android.lambdarunner.aws.base

open class AWSCommonRequestParameters(
    val action: String,
    val version: String,
    var x_amz_algorithm: String? = null,
    var x_amz_credential: String? = null,
    var x_amz_date: String? = null,
    var x_amz_security_token: String? = null,
    var x_amz_signature: String? = null,
    var x_amz_signedheaders: String? = null
)


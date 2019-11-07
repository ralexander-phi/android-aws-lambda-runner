package com.alexsci.android.lambdarunner.ui.common

abstract class BaseListResult<T>(
    val success: T? = null,
    val error: Int? = null
)

package com.alexsci.android.lambdarunner.ui.add_key

/**
 * Data validation state of the login form.
 */
data class LoginFormState(
    val accessKeyIdError: Int? = null,
    val secretAccessKeyError: Int? = null,
    val isDataValid: Boolean = false
)

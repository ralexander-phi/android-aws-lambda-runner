package com.alexsci.android.lambdarunner.ui.login

/**
 * Data validation state of the login form.
 */
data class LoginFormState(
    val accessKeyIdError: Int? = null,
    val secretAccessKeyError: Int? = null,
    val isDataValid: Boolean = false
)

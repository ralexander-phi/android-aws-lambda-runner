package com.alexsci.android.lambdarunner.ui.add_key

import com.alexsci.android.lambdarunner.ui.add_key.LoggedInUserView

/**
 * Authentication result : success (user details) or error message.
 */
data class LoginResult(
    val success: LoggedInUserView? = null,
    val error: Int? = null
)

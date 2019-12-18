package com.alexsci.android.lambdarunner.data.add_key

import com.alexsci.android.lambdarunner.data.Result
import com.alexsci.android.lambdarunner.data.add_key.model.LoggedInUser
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {
    fun login(username: String, password: String): Result<LoggedInUser> {
        return try {
            val fakeUser = LoggedInUser(java.util.UUID.randomUUID().toString(), "Jane Doe")
            Result.Success(fakeUser)
        } catch (e: Throwable) {
            Result.Error(
                IOException(
                    "Error logging in",
                    e
                )
            )
        }
    }
}


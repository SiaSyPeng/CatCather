package com.cs65.gnf.lab3

/**
 * java reflection to catch the POST response from server at login
 * Created by siapeng on 10/12/17.
 * Edited to a Kotlin data class by Naman on 10/18/17
 */
data class LoginResponse (
        // Success case, will fetch all fields user saved
        val name: String?,
        val realName: String?,
        val password: String?,
        val privacy: Boolean?,
        val alert: String?,
        val mode: Boolean?,
        val dis: String?,
        val minTime: String?,

        // Error case, will fetch status(error) and what error the user get(code)
        val error: String?,
        val code: String?
)

data class ProfileObject (
        val status: String?,
        val data: String?,    //this will get string response (need to parse again)
        val json: ProfJson?,  //json response
        val code: String?     //error case
)

data class ProfJson (
        val name: String?,
        val realName: String?,
        val password: String?,
        val privacy: Boolean?,
        val alert: String?,
        val mode: Boolean?,
        val dis: String?,
        val minTime: String?
)

data class UnameObject (
        val name: String?,
        val avail: String?

)


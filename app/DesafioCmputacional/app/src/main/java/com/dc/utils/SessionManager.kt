package com.dc.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    companion object {
        private const val PREFS_NAME = "my_app_prefs"
        private const val KEY_IS_LOGGED_IN = "is_user_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"

        @Volatile
        private var INSTANCE: SessionManager? = null

        // singleton pattern to ensure only one instance of SessionManager exists
        fun getInstance(context: Context): SessionManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SessionManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * creates a login session for the user
     *
     * @param userId The ID of the logged-in user
     * @param email The email of the logged-in user
     */
    fun createLoginSession(userId:Int, email: String) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putInt(KEY_USER_ID, userId)
        editor.putString(KEY_USER_EMAIL, email)
        editor.apply() // Use apply for asynchronous saving
    }

    fun getUserId(): Int {
        return sharedPreferences.getInt(KEY_USER_ID, -1)
    }
    fun getUserEmail(): String? {
        return sharedPreferences.getString(KEY_USER_EMAIL, null)
    }
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * clears session details on logout.
     */
    fun logoutUser() {
        editor.clear()
        editor.apply()
        // You might want to navigate the user to the login screen after this.
    }
}

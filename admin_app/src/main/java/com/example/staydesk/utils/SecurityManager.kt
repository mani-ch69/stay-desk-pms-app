package com.example.staydesk.utils

import android.content.Context
import android.content.SharedPreferences

class SecurityManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("StayDeskPrefs", Context.MODE_PRIVATE)

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getUserData(): Map<String, String?> {
        return mapOf(
            "email" to prefs.getString(KEY_USER_EMAIL, ""),
            "phone" to prefs.getString(KEY_USER_PHONE, ""),
            "name" to prefs.getString(KEY_USER_NAME, "Admin"),
            "photo" to prefs.getString(KEY_USER_PHOTO, null),
            "hotelId" to prefs.getString(KEY_HOTEL_ID, "StayDesk_001"),
            "role" to prefs.getString(KEY_USER_ROLE, "ADMIN"),
            "uid" to prefs.getString(KEY_USER_UID, null)
        )
    }

    fun getHotelId(): String? = prefs.getString(KEY_HOTEL_ID, "StayDesk_001")

    fun updateHotelId(hotelId: String) {
        prefs.edit().putString(KEY_HOTEL_ID, hotelId).apply()
    }

    fun setLoggedIn(
        isLoggedIn: Boolean,
        id: String? = null,
        name: String? = null,
        photo: String? = null,
        phone: String? = null,
        role: String? = "ADMIN",
        hotelId: String? = null,
        uid: String? = null
    ) {
        val editor = prefs.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
        id?.let { editor.putString(KEY_USER_EMAIL, it) }
        name?.let { editor.putString(KEY_USER_NAME, it) }
        photo?.let { editor.putString(KEY_USER_PHOTO, it) }
        phone?.let { editor.putString(KEY_USER_PHONE, it) }
        role?.let { editor.putString(KEY_USER_ROLE, it) }
        hotelId?.let { editor.putString(KEY_HOTEL_ID, it) }
        uid?.let { editor.putString(KEY_USER_UID, it) }
        editor.apply()
    }

    fun setAppLock(pin: String?, useBiometric: Boolean) {
        val editor = prefs.edit()
        editor.putBoolean(KEY_LOCK_ENABLED, true)
        pin?.let { editor.putString(KEY_LOCK_PIN, it) }
        editor.putBoolean(KEY_USE_BIOMETRIC, useBiometric)
        editor.apply()
    }

    fun isLockEnabled(): Boolean = prefs.getBoolean(KEY_LOCK_ENABLED, false)

    fun disableLock() {
        prefs.edit()
            .putBoolean(KEY_LOCK_ENABLED, false)
            .remove(KEY_LOCK_PIN)
            .putBoolean(KEY_USE_BIOMETRIC, false)
            .apply()
    }

    fun getPin(): String = prefs.getString(KEY_LOCK_PIN, "") ?: ""

    fun useBiometric(): Boolean = prefs.getBoolean(KEY_USE_BIOMETRIC, false)

    fun logout() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_PHOTO)
            .remove(KEY_USER_PHONE)
            .remove(KEY_USER_UID)
            .apply()
    }

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_PHOTO = "user_photo"
        private const val KEY_USER_PHONE = "user_phone"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_HOTEL_ID = "hotel_id"
        private const val KEY_USER_UID = "user_uid"
        private const val KEY_LOCK_ENABLED = "lock_enabled"
        private const val KEY_LOCK_PIN = "lock_pin"
        private const val KEY_USE_BIOMETRIC = "use_biometric"
    }
}

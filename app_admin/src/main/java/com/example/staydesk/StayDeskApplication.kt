package com.example.staydesk

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class StayDeskApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Enable Firebase Offline Persistence for better performance
        // Note: Re-enable this after adding new google-services.json
        /*
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        } catch (e: Exception) {
            // Already set or error
        }
        */
    }
}

package com.example.staydesk

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

class StayDeskApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Manual Firebase initialization
        try {
            FirebaseApp.initializeApp(this)
            
            // Explicitly set database URL
            val db = FirebaseDatabase.getInstance("https://stay-desk-pms-app-default-rtdb.asia-southeast1.firebasedatabase.app/")
            db.setPersistenceEnabled(true)
        } catch (e: Exception) {
            android.util.Log.e("StayDeskApp", "Firebase initialization failed. " +
                "Ensure configuration file is present in the app module.", e)
        }
    }
}

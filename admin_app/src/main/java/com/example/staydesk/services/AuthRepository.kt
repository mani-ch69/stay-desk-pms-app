package com.example.staydesk.data

object AuthRepository {
    private val auth by lazy { com.google.firebase.auth.FirebaseAuth.getInstance() }
    private val db by lazy { 
        com.google.firebase.database.FirebaseDatabase.getInstance("https://stay-desk-pms-app-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("admins") 
    }

    fun signUpAdmin(
        name: String,
        email: String,
        phone: String,
        pass: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = task.result?.user?.uid ?: ""
                    val adminData = mapOf(
                        "id" to uid,
                        "name" to name,
                        "email" to email,
                        "phone" to phone,
                        "role" to "ADMIN"
                    )
                    db.child(uid).setValue(adminData)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onFailure(it.message ?: "Database Error") }
                } else {
                    onFailure(task.exception?.message ?: "Authentication Failed")
                }
            }
    }

    @Suppress("UNCHECKED_CAST")
    fun syncUserDataByPhone(
        phone: String,
        onSuccess: (Map<String, Any>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        db.orderByChild("phone").equalTo(phone).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val adminData = snapshot.children.first().value as? Map<String, Any>
                if (adminData != null) {
                    onSuccess(adminData + ("role" to "ADMIN"))
                    return@addOnSuccessListener
                }
            }
            
            com.google.firebase.database.FirebaseDatabase.getInstance().getReference("staff_lookup")
                .orderByChild("phone").equalTo(phone).get().addOnSuccessListener { staffSnapshot ->
                    if (staffSnapshot.exists()) {
                        val staffData = staffSnapshot.children.first().value as? Map<String, Any>
                        if (staffData != null) {
                            onSuccess(staffData)
                            return@addOnSuccessListener
                        }
                    }
                    onFailure("Phone number not registered.")
                }.addOnFailureListener {
                    onFailure("Error syncing data from server")
                }
        }.addOnFailureListener {
            onFailure("Network Error: Failed to sync profile")
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun loginAdmin(
        userId: String,
        pass: String,
        onSuccess: (Map<String, Any>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        android.util.Log.d("StayDeskAuth", "Attempting login for: $userId")
        val isEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(userId).matches()
        
        if (isEmail) {
            android.util.Log.d("StayDeskAuth", "Input identified as Email")
            auth.signInWithEmailAndPassword(userId, pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        android.util.Log.d("StayDeskAuth", "Firebase Auth successful for email")
                        fetchAdminData(auth.currentUser?.uid ?: "", userId, onSuccess, onFailure)
                    } else {
                        val error = task.exception?.message ?: "Login Failed"
                        android.util.Log.e("StayDeskAuth", "Firebase Auth failed: $error")
                        onFailure(error)
                    }
                }
        } else {
            android.util.Log.d("StayDeskAuth", "Input identified as User ID / Phone. Searching database...")
            // Check 'admins' node first
            db.orderByChild("phone").equalTo(userId).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    android.util.Log.d("StayDeskAuth", "Found user in 'admins' node")
                    val adminData = snapshot.children.first().value as? Map<String, Any>
                    val email = adminData?.get("email")?.toString()
                    if (email != null) {
                        auth.signInWithEmailAndPassword(email, pass)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    onSuccess(adminData)
                                } else {
                                    val error = task.exception?.message ?: "Login Failed"
                                    android.util.Log.e("StayDeskAuth", "Firebase Auth failed after lookup: $error")
                                    onFailure(error)
                                }
                            }
                    } else {
                        onFailure("Account associated with this number has no email.")
                    }
                } else {
                    // Try 'staff_lookup' or search by a generic 'username' or 'id' if applicable
                    android.util.Log.d("StayDeskAuth", "Not found in 'admins' phone. Checking 'staff_lookup'...")
                    com.google.firebase.database.FirebaseDatabase.getInstance("https://stay-desk-pms-app-default-rtdb.asia-southeast1.firebasedatabase.app/")
                        .getReference("staff_lookup")
                        .orderByChild("userId").equalTo(userId).get().addOnSuccessListener { staffSnapshot ->
                            if (staffSnapshot.exists()) {
                                android.util.Log.d("StayDeskAuth", "Found user in 'staff_lookup'")
                                val staffData = staffSnapshot.children.first().value as? Map<String, Any>
                                val email = staffData?.get("email")?.toString()
                                if (email != null) {
                                    auth.signInWithEmailAndPassword(email, pass)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                onSuccess(staffData)
                                            } else {
                                                onFailure(task.exception?.message ?: "Login Failed")
                                            }
                                        }
                                } else {
                                    onFailure("Staff account has no email for authentication.")
                                }
                            } else {
                                android.util.Log.e("StayDeskAuth", "User ID '$userId' not found anywhere.")
                                onFailure("User ID / Phone not found. Please check and try again.")
                            }
                        }.addOnFailureListener {
                            onFailure("Database Error: ${it.message}")
                        }
                }
            }.addOnFailureListener {
                onFailure("Database Error: ${it.message}")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun fetchAdminData(
        uid: String,
        email: String,
        onSuccess: (Map<String, Any>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        db.child(uid).get().addOnSuccessListener { snapshot ->
            val data = snapshot.value as? Map<String, Any>
            if (data != null) {
                com.example.staydesk.data.HotelSession.setHotelId(uid)
                onSuccess(data)
            } else {
                val fallbackData = mapOf(
                    "id" to uid,
                    "name" to "Admin",
                    "email" to email,
                    "role" to "ADMIN"
                )
                db.child(uid).setValue(fallbackData)
                com.example.staydesk.data.HotelSession.setHotelId(uid)
                onSuccess(fallbackData)
            }
        }.addOnFailureListener {
            onFailure("Failed to restore profile data")
        }
    }
}

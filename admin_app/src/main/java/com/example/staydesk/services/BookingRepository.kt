package com.example.staydesk.data

import com.example.staydesk.data.model.Booking
import com.example.staydesk.data.model.BookingStatus
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object BookingRepository {
    private val dbUrl = "https://stay-desk-pms-app-default-rtdb.asia-southeast1.firebasedatabase.app/"
    private var db = com.google.firebase.database.FirebaseDatabase.getInstance(dbUrl).getReference("hotels/StayDesk_001/bookings")
    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()
    private var listener: ValueEventListener? = null

    fun startListening(hotelId: String) {
        stopListening()
        db = FirebaseDatabase.getInstance(dbUrl).getReference("hotels").child(hotelId).child("bookings")
        // Optimization: Enable offline persistence for faster subsequent loads
        db.keepSynced(true)
        
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Optimization: Efficient mapping using sequence for large datasets
                val list = snapshot.children.asSequence()
                    .mapNotNull { it.getValue(Booking::class.java) }
                    .toList()
                _bookings.value = list
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        db.addValueEventListener(listener!!)
    }

    private fun stopListening() {
        listener?.let { db.removeEventListener(it) }
        listener = null
    }

    fun addBooking(booking: Booking) {
        val bookingId = if (booking.id.isBlank()) java.util.UUID.randomUUID().toString() else booking.id
        val hId = if (booking.hotelId.isNotBlank()) booking.hotelId else HotelSession.hotelId.value ?: "StayDesk_001"
        
        val randomNum = if (booking.bookingNumber.isBlank()) (1_000_000_000L..9_999_999_999L).random().toString() else booking.bookingNumber
        val rNum = if (booking.roomNumber.isBlank()) booking.roomType.ifBlank { "101" } else booking.roomNumber
        
        val finalBooking = booking.copy(
            id = bookingId,
            hotelId = hId,
            bookingNumber = randomNum,
            roomNumber = rNum
        )

        // Write to Firebase Realtime Database
        val targetDb = FirebaseDatabase.getInstance(dbUrl).getReference("hotels").child(hId).child("bookings")
        targetDb.child(finalBooking.id).setValue(finalBooking)
        
        // Optimistic UI update
        val currentList = _bookings.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == finalBooking.id }
        if (index >= 0) {
            currentList[index] = finalBooking
        } else {
            currentList.add(0, finalBooking)
        }
        _bookings.value = currentList
    }

    fun updateBookingStatus(id: String, status: BookingStatus) {
        db.child(id).child("status").setValue(status)
    }

    fun checkInGuest(bookingId: String, idPhotoUrl: String) {
        val updates = mapOf(
            "status" to BookingStatus.CHECKED_IN,
            "guestIdentities/0/frontPhotoUrl" to idPhotoUrl
        )
        db.child(bookingId).updateChildren(updates)
    }

    fun deleteBooking(id: String) {
        db.child(id).removeValue()
    }

    fun clearData() {
        stopListening()
        _bookings.value = emptyList()
    }
}

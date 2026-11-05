package com.example.staydesk.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.staydesk.data.model.Booking
import com.example.staydesk.data.model.BusinessDetails

object ReceiptHelper {

    fun printBookingReceipt(
        context: Context,
        booking: Booking,
        businessDetails: BusinessDetails,
        roomType: String
    ) {
        val receiptText = buildReceiptText(booking, businessDetails, roomType)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Booking Receipt - ${booking.id}")
            putExtra(Intent.EXTRA_TEXT, receiptText)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Print / Share Receipt"))
    }

    fun shareReceiptOnWhatsApp(
        context: Context,
        booking: Booking,
        businessDetails: BusinessDetails,
        roomType: String
    ) {
        val receiptText = buildReceiptText(booking, businessDetails, roomType)
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?phone=${booking.guestPhone}&text=${Uri.encode(receiptText)}")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp is not installed on this device", Toast.LENGTH_SHORT).show()
        }
    }

    private fun buildReceiptText(
        booking: Booking,
        businessDetails: BusinessDetails,
        roomType: String
    ): String {
        return """
            --- ${businessDetails.propertyName.ifEmpty { "HOTEL RECEIPT" }} ---
            Booking ID: ${booking.id}
            Guest Name: ${booking.guestName}
            Phone: ${booking.guestPhone}
            Room No: ${booking.roomNumber} ($roomType)
            Check-In: ${booking.checkInDate}
            Check-Out: ${booking.checkOutDate}
            Total Amount: ₹${booking.totalAmount}
            Status: ${booking.status}
            ----------------------------
            Thank you for choosing ${businessDetails.propertyName}!
        """.trimIndent()
    }
}

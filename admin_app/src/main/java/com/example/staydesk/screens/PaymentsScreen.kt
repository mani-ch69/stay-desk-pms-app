package com.example.staydesk.ui.waiter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.staydesk.data.model.Booking
import com.example.staydesk.data.model.Room
import com.example.staydesk.ui.common.BookingCard
import com.example.staydesk.ui.common.StatCard
import java.util.*

@Composable
fun PaymentsScreen(bookings: List<Booking>, rooms: List<Room>) {
    var selectedRange by remember { mutableStateOf("Today") }
    var editingBooking by remember { mutableStateOf<Booking?>(null) }
    
    val filteredBookings = remember(bookings, selectedRange) {
        val now = Calendar.getInstance()
        val startOfToday = now.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
        
        bookings.filter { b ->
            when (selectedRange) {
                "Today" -> b.timestamp >= startOfToday
                "Last 7 Days" -> b.timestamp >= (System.currentTimeMillis() - 7 * 86400000L)
                "Last 30 Days" -> b.timestamp >= (System.currentTimeMillis() - 30 * 86400000L)
                else -> true
            }
        }.sortedByDescending { it.timestamp }
    }

    val totalRevenue = filteredBookings.sumOf { it.totalAmount }
    val totalPaid = filteredBookings.sumOf { it.advancePaid }
    val totalOutstanding = filteredBookings.sumOf { it.totalAmount - it.advancePaid - it.discount }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        ScrollableTabRow(
            selectedTabIndex = when(selectedRange) { "Today" -> 0; "Last 7 Days" -> 1; "Last 30 Days" -> 2; else -> 3 },
            containerColor = Color.White,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 16.dp,
            divider = {}
        ) {
            listOf("Today", "Last 7 Days", "Last 30 Days", "All Time").forEach { range ->
                Tab(
                    selected = selectedRange == range,
                    onClick = { selectedRange = range },
                    text = { Text(range, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Revenue", "₹${totalRevenue.toInt()}", Icons.Default.TrendingUp, Color(0xFF1976D2), Modifier.weight(1f))
                    StatCard("Collected", "₹${totalPaid.toInt()}", Icons.Default.CheckCircle, Color(0xFF2E7D32), Modifier.weight(1f))
                }
            }
            
            item {
                StatCard("Outstanding", "₹${totalOutstanding.toInt()}", Icons.Default.History, Color(0xFFF57C00), Modifier.fillMaxWidth())
            }

            item {
                Text(
                    text = "Recent Transactions", 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 16.sp, 
                    color = Color.Black, 
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (filteredBookings.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No transactions found", color = Color.Gray)
                    }
                }
            } else {
                items(filteredBookings, key = { it.id }) { booking ->
                    BookingCard(
                        booking = booking,
                        rooms = rooms,
                        onEdit = { editingBooking = booking }
                    )
                }
            }
            
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
    
    if (editingBooking != null) {
        AddPaymentDialog(
            booking = editingBooking!!,
            onDismiss = { editingBooking = null },
            onConfirm = { updated ->
                com.google.firebase.database.FirebaseDatabase.getInstance().getReference("hotels")
                    .child(updated.hotelId).child("bookings").child(updated.id).setValue(updated)
                editingBooking = null
            }
        )
    }
}

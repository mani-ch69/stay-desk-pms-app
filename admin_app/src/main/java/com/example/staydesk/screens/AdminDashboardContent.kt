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
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.staydesk.data.model.Booking
import com.example.staydesk.ui.theme.StayDeskTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminDashboardContent(
    roomStatuses: List<RoomLiveStatus>,
    bookings: List<Booking> = emptyList(),
    onDeleteBooking: (String) -> Unit = {},
    viewModel: AdminMenuViewModel? = null // Made optional for preview
) {
    val rooms = remember(roomStatuses) { roomStatuses.map { it.room } }
    
    val today = remember { Calendar.getInstance() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        // 2. Top Metrics (3 Cards: Check-in, Check-out, Stayover)
        item {
            val todayStartMs = remember(today) {
                Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
            val todayEndMs = todayStartMs + 86400000L

            val checkInCount = remember(bookings, todayStartMs) {
                bookings.count { b ->
                    b.status == com.example.staydesk.data.model.BookingStatus.BOOKED && b.checkInDate in todayStartMs until todayEndMs
                }
            }

            val checkOutCount = remember(bookings, todayStartMs) {
                bookings.count { b ->
                    b.status == com.example.staydesk.data.model.BookingStatus.CHECKED_IN && b.checkOutDate in todayStartMs until todayEndMs
                }
            }

            val stayoverCount = remember(bookings, todayStartMs) {
                bookings.count { b ->
                    b.status == com.example.staydesk.data.model.BookingStatus.CHECKED_IN && b.checkOutDate >= todayEndMs
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Top 3 Cards Row: Check-in, Check-out, Stayover
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DashboardMetricCard(
                        title = "Check-in",
                        value = checkInCount.toString(),
                        icon = Icons.Default.ArrowCircleRight,
                        iconColor = Color(0xFF4CAF50),
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                    DashboardMetricCard(
                        title = "Check-out",
                        value = checkOutCount.toString(),
                        icon = Icons.Default.ArrowCircleLeft,
                        iconColor = Color(0xFFF57C00),
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                    DashboardMetricCard(
                        title = "Stayover",
                        value = stayoverCount.toString(),
                        icon = Icons.Default.Bed,
                        iconColor = Color(0xFF1976D2),
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Recent Bookings
        item {
            Column {
                DashboardSectionHeader(title = "Recent Bookings", actionText = "View All", onActionClick = {})
                bookings.take(3).forEach { booking ->
                    val roomType = rooms.find { it.roomNumber == booking.roomNumber }?.roomType ?: "Room"
                    RecentBookingItem(booking = booking, roomType = roomType, onClick = {})
                }
            }
        }

        item { Spacer(Modifier.height(40.dp)) }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    StayDeskTheme {
        AdminDashboardContent(
            roomStatuses = emptyList(),
            bookings = listOf(
                Booking(id = "1", bookingNumber = "BK250520001", guestName = "Rohit Sharma", guestPhone = "98765 43210", roomNumber = "101", checkInDate = System.currentTimeMillis()),
                Booking(id = "2", bookingNumber = "BK250520002", guestName = "Amit Verma", guestPhone = "91234 56789", roomNumber = "202", checkInDate = System.currentTimeMillis())
            )
        )
    }
}

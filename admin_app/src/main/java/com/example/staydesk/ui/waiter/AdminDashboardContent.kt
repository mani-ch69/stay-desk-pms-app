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
    val statsState = viewModel?.dashboardStats?.collectAsState()
    val stats = statsState?.value ?: DashboardStats(totalBookings = bookings.size)
    
    val rooms = remember(roomStatuses) { roomStatuses.map { it.room } }
    
    val today = remember { Calendar.getInstance() }
    val sdfDate = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val sdfDay = remember { SimpleDateFormat("EEEE", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Header: Greeting and Date
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Good Morning, Admin 👋",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "Welcome back! Have a great day.",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(sdfDate.format(today.time), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(sdfDay.format(today.time), fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }

        // 2. Main Metrics (2x2 Grid)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DashboardMetricCard(
                        title = "Today's Check-ins",
                        value = stats.pendingArrivalsToday.toString(), 
                        icon = Icons.Default.ArrowCircleRight,
                        iconColor = Color(0xFF4CAF50),
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                    DashboardMetricCard(
                        title = "Today's Check-outs",
                        value = "3", // Placeholder
                        icon = Icons.Default.ArrowCircleRight,
                        iconColor = Color(0xFFF57C00),
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DashboardMetricCard(
                        title = "Occupied Rooms",
                        value = stats.activeStays.toString(), 
                        icon = Icons.Default.Bed,
                        iconColor = Color(0xFF1976D2),
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                    DashboardMetricCard(
                        title = "Available Rooms",
                        value = (rooms.size - stats.activeStays).coerceAtLeast(0).toString(),
                        icon = Icons.Default.MeetingRoom,
                        iconColor = Color(0xFF2E7D32),
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Today's Overview (Financial)
        item {
            Column {
                DashboardSectionHeader(title = "Today's Overview")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DashboardOverviewCard(
                        title = "Today's Revenue",
                        value = "₹ 12,450",
                        subtitle = "15% vs yesterday",
                        icon = Icons.Default.AccountBalanceWallet,
                        iconColor = Color(0xFF673AB7),
                        trend = "↗ 15% vs yesterday",
                        modifier = Modifier.weight(1.5f)
                    )
                    DashboardOverviewCard(
                        title = "Advance",
                        value = "₹ 7,000",
                        subtitle = "3 Bookings",
                        icon = Icons.Default.CardGiftcard,
                        iconColor = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(12.dp))
                DashboardOverviewCard(
                    title = "Pending Payment",
                    value = "₹ 5,450",
                    subtitle = "5 Bookings",
                    icon = Icons.Default.History,
                    iconColor = Color(0xFFF57C00),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 4. Quick Actions
        item {
            Column {
                DashboardSectionHeader(title = "Quick Actions")
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        QuickActionButton("New\nBooking", Icons.Default.Add, Color(0xFF0052FF), {})
                        QuickActionButton("Walk-in\nBooking", Icons.Default.Person, Color(0xFFF57C00), {})
                        QuickActionButton("Block\nRoom", Icons.Default.Block, Color(0xFF673AB7), {})
                        QuickActionButton("H-keeping\nPending", Icons.Default.CleaningServices, Color(0xFF4CAF50), {})
                        QuickActionButton("Receive\nPayment", Icons.Default.Receipt, Color(0xFFE91E63), {})
                    }
                }
            }
        }

        // 5. Recent Bookings
        item {
            Column {
                DashboardSectionHeader(title = "Recent Bookings", actionText = "View All", onActionClick = {})
                bookings.take(3).forEach { booking ->
                    val roomType = rooms.find { it.roomNumber == booking.roomNumber }?.roomType ?: "Room"
                    RecentBookingItem(booking = booking, roomType = roomType, onClick = {})
                }
            }
        }

        // 6. Today's Timeline
        item {
            Column {
                DashboardSectionHeader(title = "Today's Timeline", actionText = "View Calendar", onActionClick = {})
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        TimelineItem("11:00 AM", "Check-out", "Room 101", Color(0xFF4CAF50))
                        TimelineItem("12:00 PM", "Room Cleaning", "Room 101", Color(0xFFF57C00))
                        TimelineItem("01:00 PM", "Check-in", "Room 103", Color(0xFF0052FF))
                        TimelineItem("02:00 PM", "Check-in", "Room 104", Color(0xFF673AB7), isLast = true)
                    }
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

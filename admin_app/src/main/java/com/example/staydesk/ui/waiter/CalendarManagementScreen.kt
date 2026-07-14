package com.example.staydesk.ui.waiter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.staydesk.data.model.Booking
import com.example.staydesk.data.model.BookingStatus
import com.example.staydesk.data.model.Room
import com.example.staydesk.ui.theme.StayDeskTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarManagementScreen(
    rooms: List<Room>,
    bookings: List<Booking>,
    onNewBookingClick: () -> Unit = {}
) {
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var calendarMonth by remember { mutableStateOf(Calendar.getInstance()) }
    
    val sdfMonth = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val sdfSelected = remember { SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()) }

    val bookingsForSelectedDate by remember(bookings, selectedDate) {
        derivedStateOf {
            bookings.filter { b ->
                val checkIn = Calendar.getInstance().apply { timeInMillis = b.checkInDate; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
                val checkOut = Calendar.getInstance().apply { timeInMillis = b.checkOutDate; set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }.timeInMillis
                val sel = Calendar.getInstance().apply { timeInMillis = selectedDate; set(Calendar.HOUR_OF_DAY, 12); set(Calendar.MINUTE, 0) }.timeInMillis
                sel in checkIn..checkOut
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Calendar", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { /* Drawer */ }) { Icon(Icons.Default.Menu, null) } },
                actions = {
                    IconButton(onClick = { /* Search */ }) { Icon(Icons.Default.Search, null) }
                    IconButton(onClick = { /* Filter */ }) { Icon(Icons.Default.FilterList, null) }
                    IconButton(onClick = { /* Notification */ }) { Icon(Icons.Default.Notifications, null) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewBookingClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("New Booking") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Calendar Header (Month Selector)
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { 
                    calendarMonth = (calendarMonth.clone() as Calendar).apply { add(Calendar.MONTH, -1) } 
                }) { Icon(Icons.Default.ChevronLeft, null) }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
                    color = Color.White,
                    modifier = Modifier.clickable { /* Show picker */ }
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(sdfMonth.format(calendarMonth.time), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                    }
                }

                IconButton(onClick = { 
                    calendarMonth = (calendarMonth.clone() as Calendar).apply { add(Calendar.MONTH, 1) } 
                }) { Icon(Icons.Default.ChevronRight, null) }
            }

            // Monthly Grid
            Surface(
                modifier = Modifier.padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // Week Days
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { 
                            Text(
                                it, 
                                modifier = Modifier.weight(1f), 
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center, 
                                fontSize = 11.sp, 
                                color = if(it == "Sat" || it == "Sun") Color.Red else Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    val dates = getDatesForMonth(calendarMonth)
                    dates.chunked(7).forEach { week ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            week.forEach { date ->
                                if (date == null) {
                                    Spacer(Modifier.weight(1f))
                                } else {
                                    val cal = Calendar.getInstance().apply { time = date }
                                    val isSelected = isSameDay(cal, Calendar.getInstance().apply { timeInMillis = selectedDate })
                                    val isToday = isSameDay(cal, Calendar.getInstance())
                                    val isWeekend = cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
                                    
                                    val statusDots = mutableListOf<Color>()
                                    val dayBookings = bookings.filter { b ->
                                        val checkIn = startOfDay(b.checkInDate)
                                        val checkOut = startOfDay(b.checkOutDate)
                                        cal.timeInMillis in checkIn..checkOut
                                    }
                                    if (dayBookings.any { isSameDay(Calendar.getInstance().apply { timeInMillis = it.checkInDate }, cal) }) statusDots.add(Color(0xFF4CAF50))
                                    if (dayBookings.any { cal.timeInMillis > startOfDay(it.checkInDate) && cal.timeInMillis < startOfDay(it.checkOutDate) }) statusDots.add(Color(0xFF0052FF))
                                    if (dayBookings.any { isSameDay(Calendar.getInstance().apply { timeInMillis = it.checkOutDate }, cal) }) statusDots.add(Color(0xFFF57C00))

                                    CalendarDayCell(
                                        day = cal.get(Calendar.DAY_OF_MONTH),
                                        isToday = isToday,
                                        isSelected = isSelected,
                                        isWeekend = isWeekend,
                                        statusDots = statusDots,
                                        onClick = { selectedDate = cal.timeInMillis },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            if (week.size < 7) repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                    
                    CalendarLegend()
                }
            }

            Spacer(Modifier.height(24.dp))

            // Bookings List Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Bookings on ${sdfSelected.format(Date(selectedDate))}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("${bookingsForSelectedDate.size} Bookings", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            // Bookings List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                if (bookingsForSelectedDate.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("No bookings for this date", color = Color.Gray)
                        }
                    }
                } else {
                    items(bookingsForSelectedDate) { booking ->
                        val roomType = rooms.find { it.roomNumber == booking.roomNumber }?.roomType ?: "Deluxe Room"
                        
                        // Determine status display
                        val isCheckIn = isSameDay(Calendar.getInstance().apply { timeInMillis = booking.checkInDate }, Calendar.getInstance().apply { timeInMillis = selectedDate })
                        val isCheckOut = isSameDay(Calendar.getInstance().apply { timeInMillis = booking.checkOutDate }, Calendar.getInstance().apply { timeInMillis = selectedDate })
                        
                        val label: String
                        val color: Color
                        val icon: androidx.compose.ui.graphics.vector.ImageVector
                        val time: String
                        
                        if (isCheckIn) {
                            label = "Check-in"
                            color = Color(0xFF4CAF50)
                            icon = Icons.Default.Login
                            time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(booking.checkInDate))
                        } else if (isCheckOut) {
                            label = "Check-out"
                            color = Color(0xFFF57C00)
                            icon = Icons.Default.Logout
                            time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(booking.checkOutDate))
                        } else {
                            label = "Stay"
                            color = Color(0xFF0052FF)
                            icon = Icons.Default.Bed
                            time = "2 Nights"
                        }
                        
                        CalendarBookingItem(
                            booking = booking,
                            roomType = roomType,
                            statusLabel = label,
                            statusColor = color,
                            icon = icon,
                            timeLabel = time
                        )
                    }
                    
                    item {
                        TextButton(
                            onClick = { /* View all */ },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Text("View All Bookings", fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalendarPreview() {
    StayDeskTheme {
        CalendarManagementScreen(
            rooms = emptyList(),
            bookings = listOf(
                Booking(guestName = "Rohit Sharma", roomNumber = "101", checkInDate = System.currentTimeMillis()),
                Booking(guestName = "Amit Verma", roomNumber = "202", checkInDate = System.currentTimeMillis() - 86400000, checkOutDate = System.currentTimeMillis() + 86400000)
            )
        )
    }
}

private fun getDatesForMonth(month: Calendar): List<Date?> {
    val dates = mutableListOf<Date?>()
    val cal = (month.clone() as Calendar).apply { 
        set(Calendar.DAY_OF_MONTH, 1)
        // Adjust for Mon-Sun week start (Design shows Mon start)
        val firstDay = get(Calendar.DAY_OF_WEEK)
        val offset = if (firstDay == Calendar.SUNDAY) 6 else firstDay - 2
        repeat(offset) { dates.add(null) }
    }
    val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    repeat(maxDay) {
        dates.add(cal.time)
        cal.add(Calendar.DAY_OF_MONTH, 1)
    }
    return dates
}

private fun startOfDay(ts: Long): Long = Calendar.getInstance().apply { 
    timeInMillis = ts; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) 
}.timeInMillis

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

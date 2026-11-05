package com.example.staydesk.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.staydesk.R
import com.example.staydesk.data.model.Booking
import com.example.staydesk.data.model.BookingStatus
import com.example.staydesk.data.model.Room
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = label, 
                    fontSize = 11.sp, 
                    color = Color.Gray, 
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = value, 
                fontSize = 20.sp, 
                fontWeight = FontWeight.Black, 
                color = Color.Black
            )
        }
    }
}

@Composable
fun BookingCard(
    booking: Booking,
    rooms: List<Room>,
    selectedDate: Long? = null,
    onEdit: (() -> Unit)? = null,
    onStatusClick: (() -> Unit)? = null,
    onPrint: (() -> Unit)? = null,
    onWhatsApp: (() -> Unit)? = null,
    onPhoneClick: (() -> Unit)? = null,
    actionButton: (@Composable () -> Unit)? = null
) {
    val context = LocalContext.current
    val roomType = remember(booking.roomNumber, rooms) {
        rooms.find { it.roomNumber == booking.roomNumber }?.roomType ?: "Deluxe Room"
    }
    
    val referenceDate = selectedDate ?: System.currentTimeMillis()
    val isCheckIn = isSameDay(Calendar.getInstance().apply { timeInMillis = booking.checkInDate }, Calendar.getInstance().apply { timeInMillis = referenceDate })
    val isCheckOut = isSameDay(Calendar.getInstance().apply { timeInMillis = booking.checkOutDate }, Calendar.getInstance().apply { timeInMillis = referenceDate })
    
    val (statusLabel, statusColor, icon) = when {
        isCheckIn -> Triple("Check-in", Color(0xFF4CAF50), Icons.Default.Login)
        isCheckOut -> Triple("Check-out", Color(0xFFF57C00), Icons.Default.Logout)
        else -> Triple("Stay", Color(0xFF0052FF), Icons.Default.Bed)
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Left Status Line
            Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(statusColor))
            
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                // Icon
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = statusColor, modifier = Modifier.size(20.dp))
                    }
                }
                
                Spacer(Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(booking.guestName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                    Text(roomType, fontSize = 12.sp, color = Color.Gray)
                    Text("${booking.numberOfGuests} Guests", fontSize = 11.sp, color = Color.Gray)
                }
                
                Column(modifier = Modifier.width(80.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(booking.roomNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                    Text("Room No.", fontSize = 10.sp, color = Color.Gray)
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(statusLabel, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    val timeStr = if (isCheckIn) SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(booking.checkInDate))
                                 else if (isCheckOut) SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(booking.checkOutDate))
                                 else "Active"
                    Text(timeStr, fontSize = 11.sp, color = Color.Gray)
                }
                
                IconButton(onClick = { 
                    if (onEdit != null) onEdit() 
                }) {
                    Icon(Icons.Default.MoreVert, null, tint = Color.Gray)
                }
            }
        }
    }
}

fun isSameDay(c1: Calendar, c2: Calendar) = 
    c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && 
    c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)

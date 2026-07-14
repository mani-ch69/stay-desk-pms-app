package com.example.staydesk.ui.waiter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.staydesk.data.model.Booking

@Composable
fun CalendarDayCell(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    isWeekend: Boolean,
    statusDots: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = day.toString(),
            fontSize = 14.sp,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isSelected -> Color.White
                isWeekend -> Color.Red
                else -> Color.Black
            }
        )
        Row(
            modifier = Modifier.padding(top = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            statusDots.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(color, CircleShape)
                )
            }
        }
    }
}

@Composable
fun CalendarLegend() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        CalendarLegendItem(Color(0xFF4CAF50), "Check-in")
        CalendarLegendItem(Color(0xFF0052FF), "Occupied")
        CalendarLegendItem(Color(0xFFF57C00), "Check-out")
        CalendarLegendItem(Color(0xFFD32F2F), "Blocked")
    }
}

@Composable
private fun CalendarLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 11.sp, color = Color.Gray)
    }
}

@Composable
fun CalendarBookingItem(
    booking: Booking,
    roomType: String,
    statusLabel: String,
    statusColor: Color,
    icon: ImageVector,
    timeLabel: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .fillMaxWidth()
        ) {
            // Left Status Border
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(statusColor)
            )
            
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Icon
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
                    Text(booking.guestName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(roomType, fontSize = 12.sp, color = Color.Gray)
                    Text("${booking.numberOfGuests} Guests", fontSize = 11.sp, color = Color.Gray)
                }
                
                Column(modifier = Modifier.width(80.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(booking.roomNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Room No.", fontSize = 10.sp, color = Color.Gray)
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(statusLabel, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(timeLabel, fontSize = 11.sp, color = Color.Gray)
                }
                
                IconButton(onClick = { /* Menu */ }) {
                    Icon(Icons.Default.MoreVert, null, tint = Color.Gray)
                }
            }
        }
    }
}

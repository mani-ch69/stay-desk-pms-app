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

    val dfDate = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val dfDateTime24 = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }

    val nights = remember(booking.checkInDate, booking.checkOutDate) {
        val diff = ((booking.checkOutDate - booking.checkInDate) / 86400000L).toInt()
        if (diff <= 0) 1 else diff
    }

    val perNightRate = booking.roomRent
    val calculatedTotal = (perNightRate * nights * booking.roomQuantity) - booking.discount
    val totalAmount = if (booking.totalAmount > 0) booking.totalAmount else calculatedTotal

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Booking #, Booked Date, Edit Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ) {
                        Text(
                            text = "BK# ${booking.bookingNumber.ifBlank { "BK1001" }}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Booked: ${dfDate.format(Date(booking.timestamp))}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFE8F5E9)
                    ) {
                        Text(
                            text = booking.status.name,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    if (onEdit != null) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.MoreVert, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))

            // Guest Name & Mobile + Room Type & Adults
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = booking.guestName.ifBlank { "Guest" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.Black
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                        Spacer(Modifier.width(4.dp))
                        Text(text = booking.guestPhone.ifBlank { "N/A" }, fontSize = 12.sp, color = Color.Gray)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = roomType.ifBlank { "Room ${booking.roomNumber}" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Room ${booking.roomNumber} • ${booking.numberOfGuests} Adults",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                }
            }

            // Check-in, Check-out & Nights
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Check-in", fontSize = 10.sp, color = Color.Gray)
                        Text(dfDateTime24.format(Date(booking.checkInDate)), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "$nights Night${if(nights > 1) "s" else ""}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Check-out", fontSize = 10.sp, color = Color.Gray)
                        Text(dfDateTime24.format(Date(booking.checkOutDate)), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF57C00))
                    }
                }
            }

            // Per Night Rate & Total Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Rate: ", fontSize = 12.sp, color = Color.Gray)
                    Text("₹ ${perNightRate.toInt()} / night", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Total: ", fontSize = 12.sp, color = Color.Gray)
                    Text("₹ ${totalAmount.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF1976D2))
                }
            }
        }
    }
}

fun isSameDay(c1: Calendar, c2: Calendar) = 
    c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && 
    c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)

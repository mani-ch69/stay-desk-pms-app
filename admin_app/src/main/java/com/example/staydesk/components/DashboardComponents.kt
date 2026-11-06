package com.example.staydesk.ui.waiter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.staydesk.data.model.Booking
import com.example.staydesk.data.model.BookingStatus
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardSectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        if (actionText != null) {
            Text(
                text = actionText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onActionClick() }
            )
        }
    }
}

@Composable
fun DashboardMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(96.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title, 
                    fontSize = 11.sp, 
                    color = Color.Gray, 
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = CircleShape,
                    color = iconColor.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = iconColor, modifier = Modifier.size(16.dp))
                    }
                }
            }
            
            Text(
                text = value, 
                fontSize = 20.sp, 
                fontWeight = FontWeight.ExtraBold, 
                color = Color.Black
            )
        }
    }
}

@Composable
fun DashboardOverviewCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    trend: String? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
                }
            }
            
            Spacer(Modifier.width(12.dp))
            
            Column {
                Text(title, fontSize = 11.sp, color = Color.Gray)
                Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                if (trend != null) {
                    Text(trend, fontSize = 10.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                } else {
                    Text(subtitle, fontSize = 11.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Surface(
            modifier = Modifier.size(50.dp),
            shape = CircleShape,
            color = color.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            label, 
            fontSize = 11.sp, 
            fontWeight = FontWeight.Medium, 
            color = Color.Black,
            maxLines = 2,
            lineHeight = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun RecentBookingItem(
    booking: Booking,
    roomType: String,
    onClick: () -> Unit
) {
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
        onClick = onClick,
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
            // Header Row: Booking #, Booked Date, Status
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
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
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

@Composable
fun TimelineItem(
    time: String,
    title: String,
    subtitle: String,
    color: Color,
    isLast: Boolean = false
) {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(80.dp)
        ) {
            Text(time, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .width(1.dp)
                    .background(if (isLast) Color.Transparent else Color.LightGray)
            )
        }
        
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(8.dp)
                .background(color, CircleShape)
        )
        
        Spacer(Modifier.width(16.dp))
        
        Column(modifier = Modifier.padding(bottom = 20.dp).weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
        }
        
        Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray, modifier = Modifier.size(16.dp).align(Alignment.CenterVertically))
    }
}

@Composable
fun DeleteConfirmDialog(title: String, message: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = Color.Red), shape = RoundedCornerShape(8.dp)) { Text("DELETE") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL") }
        }
    )
}

@Composable
fun StatusSelectionDialog(
    currentStatus: BookingStatus,
    onDismiss: () -> Unit,
    onStatusSelected: (BookingStatus) -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Update Status", fontWeight = FontWeight.Black, fontSize = 20.sp)
                Spacer(Modifier.height(24.dp))
                StatusOptionRow("OK", Icons.Default.CheckCircle, Color(0xFF2E7D32), currentStatus == BookingStatus.COMPLETED) { onStatusSelected(BookingStatus.COMPLETED) }
                Spacer(Modifier.height(12.dp))
                StatusOptionRow("CANCELLED", Icons.Default.Cancel, Color.Red, currentStatus == BookingStatus.CANCELLED) { onStatusSelected(BookingStatus.CANCELLED) }
                Spacer(Modifier.height(24.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("CLOSE", color = Color.Gray) }
            }
        }
    }
}

@Composable
fun StatusOptionRow(label: String, icon: ImageVector, color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) color.copy(alpha = 0.1f) else Color(0xFFF8FAFC),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, color) else null
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Text(text = label, fontWeight = FontWeight.Bold, color = if (isSelected) color else Color.Black, fontSize = 16.sp)
            Spacer(Modifier.weight(1f))
            if (isSelected) Icon(Icons.Default.Check, null, tint = color, modifier = Modifier.size(20.dp))
        }
    }
}

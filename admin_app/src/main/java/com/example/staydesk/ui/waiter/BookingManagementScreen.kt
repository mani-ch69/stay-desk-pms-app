package com.example.staydesk.ui.waiter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.staydesk.data.model.*
import com.example.staydesk.ui.common.BookingCard
import com.example.staydesk.ui.common.CommonDateRangePicker
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingManagementScreen(
    bookings: List<Booking>,
    rooms: List<Room>,
    onDeleteBooking: (String) -> Unit
) {
    val startOfToday = remember { Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis }
    val endOfToday = remember { Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }.timeInMillis }

    var activeDateOfType by remember { mutableStateOf("Check-in") }
    var activeDateFrom by remember { mutableLongStateOf(startOfToday) }
    var activeDateTo by remember { mutableLongStateOf(endOfToday) }
    var activeSearchQuery by remember { mutableStateOf("") }
    val activeStatuses = remember { mutableStateListOf<String>() }

    var draftDateOfType by remember { mutableStateOf("Check-in") }
    var draftDateFrom by remember { mutableLongStateOf(startOfToday) }
    var draftDateTo by remember { mutableLongStateOf(endOfToday) }
    var draftSearchQuery by remember { mutableStateOf("") }
    val draftStatuses = remember { mutableStateListOf<String>() }

    var isMoreFiltersExpanded by remember { mutableStateOf(false) }
    var showRangePicker by remember { mutableStateOf(false) }
    
    val sdf = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    var filteredBookings by remember { mutableStateOf(bookings) }

    fun applyFilters() {
        filteredBookings = bookings.filter { b ->
            val dateToCompare = when (activeDateOfType) {
                "Check-in" -> b.checkInDate
                "Check-out" -> b.checkOutDate
                "Reservation" -> b.timestamp
                else -> b.checkInDate
            }

            val inDateRange = dateToCompare in (activeDateFrom..activeDateTo)
            val matchesStatus = activeStatuses.isEmpty() || activeStatuses.any { s ->
                when(s) {
                    "Ok" -> b.status == BookingStatus.COMPLETED
                    "Canceled" -> b.status == BookingStatus.CANCELLED
                    "No-show" -> b.status == BookingStatus.BOOKED 
                    else -> true
                }
            }
            val matchesSearch = activeSearchQuery.isEmpty() || 
                             b.guestName.contains(activeSearchQuery, ignoreCase = true) || 
                             b.bookingNumber.contains(activeSearchQuery, ignoreCase = true)

            inDateRange && matchesStatus && matchesSearch
        }.sortedByDescending { it.checkInDate }
    }

    LaunchedEffect(bookings) { applyFilters() }

    val context = LocalContext.current
    val businessDetails by com.example.staydesk.data.BusinessDetailsRepository.details.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        // Filters Card
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        FilterDropdownFieldCompact("Date of", draftDateOfType) { draftDateOfType = it }
                    }
                    Surface(
                        modifier = Modifier.weight(1.5f).height(48.dp).clickable { showRangePicker = true }.border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${sdf.format(Date(draftDateFrom))} - ${sdf.format(Date(draftDateTo))}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.DateRange, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                        }
                    }
                }
                
                Spacer(Modifier.height(12.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = { isMoreFiltersExpanded = !isMoreFiltersExpanded },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Icon(Icons.Default.FilterList, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Filters")
                    }
                    Button(
                        onClick = { 
                            activeDateOfType = draftDateOfType; activeDateFrom = draftDateFrom; activeDateTo = draftDateTo; activeSearchQuery = draftSearchQuery; activeStatuses.clear(); activeStatuses.addAll(draftStatuses); applyFilters() 
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Show Results")
                    }
                }

                AnimatedVisibility(visible = isMoreFiltersExpanded) {
                    Column(modifier = Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = draftSearchQuery,
                            onValueChange = { draftSearchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            placeholder = { Text("Search guest or booking #") },
                            leadingIcon = { Icon(Icons.Default.Search, null) }
                        )
                        FilterGroup("Status", listOf("Ok", "Canceled", "No-show"), draftStatuses)
                    }
                }
            }
        }

        if (filteredBookings.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Text("No bookings found", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Found ${filteredBookings.size} bookings",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                items(filteredBookings, key = { it.id }) { booking ->
                    var showEditDialog by remember { mutableStateOf(false) }
                    var showDeleteConfirm by remember { mutableStateOf(false) }
                    val roomType = rooms.find { it.roomNumber == booking.roomNumber }?.roomType ?: ""

                    BookingCard(
                        booking = booking,
                        rooms = rooms,
                        onEdit = { showEditDialog = true },
                        onPrint = { com.example.staydesk.util.ReceiptHelper.printBookingReceipt(context, booking, businessDetails, roomType) },
                        onWhatsApp = { com.example.staydesk.util.ReceiptHelper.shareReceiptOnWhatsApp(context, booking, businessDetails, roomType) }
                    )

                    if (showDeleteConfirm) {
                        DeleteConfirmDialog(
                            title = "Delete Booking?",
                            message = "Are you sure you want to permanently delete ${booking.guestName}\u0027s booking?",
                            onDismiss = { showDeleteConfirm = false },
                            onConfirm = { onDeleteBooking(booking.id); showDeleteConfirm = false }
                        )
                    }

                    if (showEditDialog) {
                        AddBookingDialog(
                            rooms = rooms,
                            initialBooking = booking,
                            onDismiss = { showEditDialog = false },
                            onConfirm = { updated ->
                                com.google.firebase.database.FirebaseDatabase.getInstance().getReference("hotels")
                                    .child(booking.hotelId).child("bookings").child(booking.id).setValue(updated)
                                showEditDialog = false
                            },
                            onDeleteClick = { showEditDialog = false; showDeleteConfirm = true }
                        )
                    }
                }
                item { Spacer(Modifier.height(100.dp)) }
            }
        }
    }

    if (showRangePicker) {
        val dateRangePickerState = rememberDateRangePickerState(initialSelectedStartDateMillis = draftDateFrom, initialSelectedEndDateMillis = draftDateTo)
        CommonDateRangePicker(state = dateRangePickerState, onDismiss = { showRangePicker = false }, onConfirm = { start, end -> start?.let { draftDateFrom = it }; end?.let { draftDateTo = it }; showRangePicker = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdownFieldCompact(label: String, selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(value = selected, onValueChange = {}, readOnly = true, label = { Text(label, fontSize = 11.sp) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = Color.LightGray, unfocusedContainerColor = Color.White))
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf("Reservation", "Check-in", "Check-out", "Stay").forEach { option ->
                DropdownMenuItem(text = { Text(option, fontSize = 13.sp) }, onClick = { onSelect(option); expanded = false })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterGroup(title: String, options: List<String>, selectedList: MutableList<String>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
        Spacer(Modifier.height(4.dp))
        FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            options.forEach { option ->
                val isSelected = selectedList.contains(option)
                FilterChip(selected = isSelected, onClick = { if (isSelected) selectedList.remove(option) else selectedList.add(option) }, label = { Text(option, fontSize = 11.sp) }, shape = RoundedCornerShape(6.dp), colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = Color.White))
            }
        }
    }
}


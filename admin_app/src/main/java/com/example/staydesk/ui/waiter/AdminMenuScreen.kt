package com.example.staydesk.ui.waiter

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.staydesk.data.model.BookingStatus
import com.example.staydesk.ui.settings.SettingsScreen
import com.example.staydesk.ui.settings.GeneralSettingsScreen
import com.example.staydesk.ui.settings.SecuritySettingsScreen
import com.example.staydesk.ui.settings.AppLockSettingsScreen
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMenuScreen(
    staffName: String = "Admin Profile",
    staffIdLabel: String = "Admin ID",
    staffPhoto: String? = null,
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    viewModel: AdminMenuViewModel = viewModel()
) {
    val roomStatuses by viewModel.roomLiveStatuses.collectAsState(initial = emptyList())
    val bookings by viewModel.bookings.collectAsState()
    val rooms = remember(roomStatuses) { roomStatuses.map { it.room } }
    
    var selectedBottomTab by remember { mutableStateOf("home") }
    var moreTabSubScreen by remember { mutableStateOf("main") }
    val context = LocalContext.current
    val activity = context as? android.app.Activity

    BackHandler {
        if (selectedBottomTab == "settings" && moreTabSubScreen != "main") {
            moreTabSubScreen = when(moreTabSubScreen) {
                "general_settings", "security_settings", "inbox" -> "settings_details"
                "app_lock" -> "security_settings"
                "hotel_stays" -> "main"
                "hotel_rooms_list", "hotel_bookings", "hotel_rates_menu", "hotel_property_detail", "hotel_availability", "pricing_guest" -> "hotel_stays"
                "hotel_prop_general_info", "hotel_prop_vat_tax", "hotel_prop_photos", "hotel_prop_policies", "hotel_prop_res_policies", "hotel_prop_facilities", "hotel_prop_room_details", "hotel_prop_amenities", "hotel_prop_profile", "hotel_prop_descriptions", "hotel_prop_messaging", "hotel_prop_sustainability" -> "hotel_property_detail"
                else -> "main"
            }
        } else if (selectedBottomTab != "home") {
            selectedBottomTab = "home"
        } else {
            activity?.moveTaskToBack(true)
        }
    }

    var showAddBookingDialog by remember { mutableStateOf(false) }
    var showAddRoomDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    val title = when (selectedBottomTab) {
                        "home" -> "StayDesk"
                        "calendar" -> "Calendar"
                        "reservations" -> "Bookings"
                        "rooms" -> "Rooms"
                        else -> "More"
                    }
                    Text(
                        text = title, 
                        fontWeight = FontWeight.Bold, 
                        color = if (selectedBottomTab == "home") MaterialTheme.colorScheme.primary else Color.Black,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    // Hamburger icon removed
                },
                actions = {
                    IconButton(onClick = { selectedBottomTab = "settings"; moreTabSubScreen = "main" }) { 
                        Icon(Icons.Default.AccountCircle, null, tint = Color.Black) 
                    }
                    IconButton(onClick = { /* Notifications */ }) { Icon(Icons.Default.Notifications, null, tint = Color.Black) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = selectedBottomTab == "home", 
                    onClick = { selectedBottomTab = "home" }, 
                    icon = { Icon(Icons.Default.Home, null) }, 
                    label = { Text("Dashboard", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                )
                
                NavigationBarItem(
                    selected = selectedBottomTab == "calendar", 
                    onClick = { selectedBottomTab = "calendar" }, 
                    icon = { Icon(Icons.Default.CalendarMonth, null) }, 
                    label = { Text("Calendar", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                )
                
                NavigationBarItem(
                    selected = selectedBottomTab == "reservations", 
                    onClick = { selectedBottomTab = "reservations" }, 
                    icon = { Icon(Icons.Default.BookOnline, null) }, 
                    label = { Text("Bookings", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                )

                NavigationBarItem(
                    selected = selectedBottomTab == "rooms", 
                    onClick = { selectedBottomTab = "rooms" }, 
                    icon = { Icon(Icons.Default.Bed, null) }, 
                    label = { Text("Rooms", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                )

                NavigationBarItem(
                    selected = selectedBottomTab == "settings", 
                    onClick = { selectedBottomTab = "settings"; moreTabSubScreen = "main" }, 
                    icon = { Icon(Icons.Default.MoreHoriz, null) }, 
                    label = { Text("More", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (selectedBottomTab) {
                "home" -> AdminDashboardContent(
                    roomStatuses = roomStatuses, 
                    bookings = bookings, 
                    onDeleteBooking = { viewModel.deleteBooking(it) }
                )
                "calendar" -> CalendarManagementScreen(
                    rooms = rooms, 
                    bookings = bookings,
                    onNewBookingClick = { showAddBookingDialog = true }
                )
                "reservations" -> BookingManagementScreen(bookings, rooms) { viewModel.deleteBooking(it) }
                "rooms" -> RoomManagementScreen { selectedBottomTab = "home" }
                "settings" -> {
                    when (moreTabSubScreen) {
                        "main" -> AdminMoreContent(
                            staffName = staffName, staffIdLabel = staffIdLabel, staffPhoto = staffPhoto,
                            onHotelStaysClick = { moreTabSubScreen = "hotel_stays" },
                            onSettingsClick = { moreTabSubScreen = "settings_details" },
                            onLogoutClick = onLogoutClick
                        )
                        "hotel_stays" -> HotelStaysMenu(
                            onBookingsClick = { moreTabSubScreen = "hotel_bookings" },
                            onRatesClick = { moreTabSubScreen = "hotel_rates_menu" },
                            onPropertyClick = { moreTabSubScreen = "hotel_property_detail" }
                        )
                        "hotel_bookings" -> {
                            BookingManagementScreen(bookings, rooms) { viewModel.deleteBooking(it) }
                        }
                        "hotel_rates_menu" -> RatesAvailabilityMenu(
                            onCalendarClick = { moreTabSubScreen = "hotel_availability" },
                            onItemClick = { 
                                moreTabSubScreen = when(it) {
                                    "pricing_guest" -> "pricing_guest"
                                    else -> moreTabSubScreen
                                }
                            }
                        )
                        "pricing_guest" -> PricingPerGuestScreen(rooms, { moreTabSubScreen = "hotel_rates_menu" })
                        "hotel_availability" -> CalendarManagementScreen(rooms, bookings)
                        "hotel_property_detail" -> PropertyDetailMenu({ moreTabSubScreen = "hotel_prop_general_info" }, { moreTabSubScreen = "hotel_prop_vat_tax" }, { moreTabSubScreen = "hotel_prop_photos" }, { moreTabSubScreen = "hotel_prop_policies" }, { moreTabSubScreen = "hotel_prop_res_policies" }, { moreTabSubScreen = "hotel_prop_facilities" }, { moreTabSubScreen = "hotel_rooms_list" }, { moreTabSubScreen = "hotel_prop_amenities" }, { moreTabSubScreen = "hotel_prop_profile" }, { moreTabSubScreen = "hotel_prop_descriptions" }, { moreTabSubScreen = "hotel_prop_messaging" }, { moreTabSubScreen = "hotel_prop_sustainability" })
                        "hotel_prop_general_info" -> GeneralInfoStatusScreen()
                        "hotel_prop_vat_tax" -> VatTaxChargesScreen()
                        "hotel_prop_photos" -> PropertyPhotosScreen()
                        "hotel_prop_policies" -> PropertyPoliciesScreen()
                        "hotel_prop_res_policies" -> ReservationPoliciesScreen()
                        "hotel_prop_amenities" -> RoomAmenitiesScreen()
                        "hotel_prop_profile" -> YourProfileScreen()
                        "hotel_prop_messaging" -> MessagingPreferencesScreen()
                        "hotel_rooms_list" -> {
                            RoomManagementScreen { moreTabSubScreen = "hotel_property_detail" }
                            if (showAddRoomDialog) AddEditRoomDialog(rooms, null, { showAddRoomDialog = false }, { com.example.staydesk.data.RoomRepository.addRoom(it); showAddRoomDialog = false })
                        }
                        "settings_details" -> SettingsScreen({ moreTabSubScreen = "main" }, { moreTabSubScreen = "general_settings" }, { moreTabSubScreen = "security_settings" }, onInboxClick = { moreTabSubScreen = "inbox" })
                        "general_settings" -> GeneralSettingsScreen({ moreTabSubScreen = "settings_details" }, onProfileClick)
                        "security_settings" -> SecuritySettingsScreen({ moreTabSubScreen = "settings_details" }, { moreTabSubScreen = "app_lock" })
                        "app_lock" -> AppLockSettingsScreen { moreTabSubScreen = "security_settings" }
                        "inbox" -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Inbox Coming Soon", color = Color.Gray) }
                        else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Coming Soon") }
                    }
                }
            }

            if (showAddBookingDialog) {
                AddBookingDialog(
                    rooms = rooms,
                    onDismiss = { showAddBookingDialog = false },
                    onConfirm = { 
                        viewModel.addBooking(it)
                        showAddBookingDialog = false 
                    }
                )
            }
        }
    }
}

@Composable
fun AdminMoreContent(
    staffName: String, 
    staffIdLabel: String, 
    staffPhoto: String?, 
    onHotelStaysClick: () -> Unit, 
    onSettingsClick: () -> Unit, 
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // PROFILE CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    if (staffPhoto != null) {
                        AsyncImage(
                            model = staffPhoto,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.Person, 
                            null, 
                            modifier = Modifier.padding(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.width(20.dp))
                Column {
                    Text(
                        staffName, 
                        fontWeight = FontWeight.Black, 
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                    Text(
                        staffIdLabel, 
                        fontSize = 13.sp, 
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // MENU GROUPS
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                MoreMenuItem("Hotel & Stay's", Icons.Default.Hotel, onHotelStaysClick)
                MoreMenuItem("Settings", Icons.Default.Settings, onSettingsClick)
            }
        }

        Spacer(Modifier.weight(1f))

        // LOGOUT BUTTON
        Button(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFEBEE),
                contentColor = Color(0xFFD32F2F)
            )
        ) {
            Icon(Icons.Default.Logout, null)
            Spacer(Modifier.width(12.dp))
            Text("Logout", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
fun MoreMenuItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, tint: Color = Color.Black) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), color = Color.Transparent) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Text(label, color = tint, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))
}

@Composable
fun HotelStaysMenu(onBookingsClick: () -> Unit, onRatesClick: () -> Unit, onPropertyClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        MoreMenuItem("Reservations", Icons.Default.BookOnline, onBookingsClick)
        MoreMenuItem("Rates & Availability", Icons.Default.EventAvailable, onRatesClick)
        MoreMenuItem("Property Detail", Icons.Default.Business, onPropertyClick)
    }
}

@Composable
fun RatesAvailabilityMenu(onCalendarClick: () -> Unit, onItemClick: (String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { MoreMenuItem("Calendar", Icons.Default.CalendarMonth, onCalendarClick) }
        item { MoreMenuItem("Pricing per guest", Icons.Default.Groups, { onItemClick("pricing_guest") }) }
    }
}


@Composable
fun PropertyDetailMenu(onGeneralInfoClick: () -> Unit, onVatTaxClick: () -> Unit, onPhotosClick: () -> Unit, onPoliciesClick: () -> Unit, onResPoliciesClick: () -> Unit, onFacilitiesClick: () -> Unit, onRoomDetailsClick: () -> Unit, onAmenitiesClick: () -> Unit, onProfileClick: () -> Unit, onDescriptionsClick: () -> Unit, onMessagingClick: () -> Unit, onSustainabilityClick: () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { MoreMenuItem("General info & property status", Icons.Default.Info, onGeneralInfoClick) }
        item { MoreMenuItem("VAT/Tax/Charges", Icons.Default.Payments, onVatTaxClick) }
        item { MoreMenuItem("Photos", Icons.Default.PhotoLibrary, onPhotosClick) }
        item { MoreMenuItem("Room details", Icons.Default.Bed, onRoomDetailsClick) }
        item { MoreMenuItem("Room Amenities", Icons.Default.HotTub, onAmenitiesClick) }
        item { MoreMenuItem("Your Profile", Icons.Default.AccountBox, onProfileClick) }
        item { MoreMenuItem("Messaging Preferences", Icons.Default.Message, onMessagingClick) }
    }
}

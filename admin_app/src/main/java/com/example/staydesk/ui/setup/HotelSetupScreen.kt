package com.example.staydesk.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.staydesk.ui.theme.StayDeskTheme
import androidx.compose.foundation.Canvas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelSetupScreen(
    onBackClick: () -> Unit,
    onComplete: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(1) }
    
    // Step 1 State
    var hotelName by remember { mutableStateOf("") }
    var hotelType by remember { mutableStateOf("") }
    var starCategory by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var pinCode by remember { mutableStateOf("") }
    
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("Hotel Setup", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Set up your hotel details to get started", fontSize = 12.sp, color = Color.Gray)
                        }
                        Spacer(Modifier.weight(1f))
                        // Header Illustration Simulation
                        Icon(Icons.Default.Hotel, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), modifier = Modifier.size(60.dp))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { /* Save & Exit */ }) {
                        Text("Save & Exit", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    
                    Button(
                        onClick = { if (currentStep < 4) currentStep++ else onComplete() },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(48.dp).width(200.dp)
                    ) {
                        Text(if (currentStep == 1) "Next: Add Rooms" else "Continue")
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // STEPPER
            SetupStepper(currentStep = currentStep)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (currentStep == 1) {
                    // BASIC INFORMATION
                    item {
                        SetupFormSection(
                            title = "Basic Information",
                            subtitle = "Tell us about your hotel",
                            icon = Icons.Default.Business,
                            iconBg = Color(0xFFE3F2FD),
                            iconTint = Color(0xFF1976D2)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                SetupTextField(
                                    value = hotelName,
                                    onValueChange = { hotelName = it },
                                    label = "Hotel Name",
                                    placeholder = "Enter hotel name",
                                    icon = Icons.Default.Domain
                                )
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        SetupDropdownField("Hotel Type", listOf("Hotel", "Resort", "Guest House", "Villa"), hotelType) { hotelType = it }
                                    }
                                    Box(modifier = Modifier.weight(1f)) {
                                        SetupDropdownField("Star Category", listOf("1 Star", "2 Star", "3 Star", "4 Star", "5 Star"), starCategory) { starCategory = it }
                                    }
                                }

                                SetupTextField(
                                    value = address,
                                    onValueChange = { address = it },
                                    label = "Address",
                                    placeholder = "Enter full address",
                                    icon = Icons.Default.LocationOn
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        SetupTextField(city, { city = it }, "City", "Enter city", Icons.Default.Domain)
                                    }
                                    Box(modifier = Modifier.weight(1f)) {
                                        SetupDropdownField("State", listOf("Delhi", "Mumbai", "UP", "Varanasi"), state) { state = it }
                                    }
                                    Box(modifier = Modifier.weight(0.8f)) {
                                        SetupTextField(pinCode, { pinCode = it }, "PIN Code", "Enter PIN", Icons.Default.Numbers, KeyboardType.Number)
                                    }
                                }
                            }
                        }
                    }

                    // CONTACT INFORMATION
                    item {
                        SetupFormSection(
                            title = "Contact Information",
                            subtitle = "How can guests reach you?",
                            icon = Icons.Default.ContactPhone,
                            iconBg = Color(0xFFE8F5E9),
                            iconTint = Color(0xFF2E7D32)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                SetupTextField(phone, { phone = it }, "Phone Number", "Enter phone number", Icons.Default.Phone, KeyboardType.Phone)
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        SetupTextField(email, { email = it }, "Email Address", "Enter email address", Icons.Default.Email, KeyboardType.Email)
                                    }
                                    Box(modifier = Modifier.weight(1f)) {
                                        SetupTextField(whatsapp, { whatsapp = it }, "WhatsApp Number", "Enter WhatsApp number", Icons.Default.Message, KeyboardType.Phone)
                                    }
                                }

                                SetupTextField(website, { website = it }, "Website (Optional)", "Enter website URL", Icons.Default.Language)
                            }
                        }
                    }

                    // HOTEL IMAGES
                    item {
                        SetupFormSection(
                            title = "Hotel Images",
                            subtitle = "Add photos of your hotel",
                            icon = Icons.Default.PhotoLibrary,
                            iconBg = Color(0xFFF3E5F5),
                            iconTint = Color(0xFF7B1FA2),
                            actionText = "0/5 Images"
                        ) {
                            DashedUploadArea(onClick = { /* Upload */ })
                        }
                    }
                } else {
                    item {
                        Box(Modifier.fillMaxSize().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("Step $currentStep Content Coming Soon", color = Color.Gray)
                        }
                    }
                }
                
                item { Spacer(Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
fun DashedUploadArea(onClick: () -> Unit) {
    val stroke = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(color = Color.LightGray, style = stroke)
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            Icon(Icons.Default.CloudUpload, null, tint = Color(0xFF7B1FA2), modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text("Tap to upload photos", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("Add up to 5 photos (JPG, PNG)", fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
fun SetupStepper(currentStep: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 16.dp, horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepItem(1, "Hotel Details", currentStep >= 1)
        Divider(modifier = Modifier.width(30.dp).padding(top = 10.dp), color = if (currentStep > 1) MaterialTheme.colorScheme.primary else Color.LightGray)
        StepItem(2, "Rooms", currentStep >= 2)
        Divider(modifier = Modifier.width(30.dp).padding(top = 10.dp), color = if (currentStep > 2) MaterialTheme.colorScheme.primary else Color.LightGray)
        StepItem(3, "Policies", currentStep >= 3)
        Divider(modifier = Modifier.width(30.dp).padding(top = 10.dp), color = if (currentStep > 3) MaterialTheme.colorScheme.primary else Color.LightGray)
        StepItem(4, "Finish", currentStep >= 4)
    }
}

@Composable
fun StepItem(step: Int, label: String, isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(24.dp),
            shape = CircleShape,
            color = if (isActive) MaterialTheme.colorScheme.primary else Color(0xFFE2E8F0)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(step.toString(), color = if (isActive) Color.White else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium, color = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray)
    }
}

@Composable
fun SetupFormSection(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    actionText: String? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(36.dp), shape = RoundedCornerShape(8.dp), color = iconBg) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(subtitle, fontSize = 12.sp, color = Color.Gray)
                }
                if (actionText != null) {
                    Text(actionText, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(Modifier.height(20.dp))
            content()
        }
    }
}

@Composable
fun SetupTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(text = "$label *", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            leadingIcon = { Icon(icon, null, modifier = Modifier.size(18.dp), tint = Color.Gray) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
            ),
            singleLine = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupDropdownField(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(text = "$label *", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = selected,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Select", fontSize = 13.sp) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                leadingIcon = { 
                    val icon = if (label.contains("Type")) Icons.Default.Bed else if (label.contains("Star")) Icons.Default.Star else Icons.Default.Map
                    Icon(icon, null, modifier = Modifier.size(18.dp), tint = Color.Gray) 
                },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                )
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = { onSelect(option); expanded = false }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HotelSetupPreview() {
    StayDeskTheme {
        HotelSetupScreen({}, {})
    }
}

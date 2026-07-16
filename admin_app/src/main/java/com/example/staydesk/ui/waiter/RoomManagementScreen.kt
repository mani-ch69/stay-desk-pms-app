package com.example.staydesk.ui.waiter

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.example.staydesk.data.RoomRepository
import com.example.staydesk.data.HotelSession
import com.example.staydesk.data.model.Room
import com.example.staydesk.util.QRCodeGenerator
import com.example.staydesk.ui.theme.StayDeskTheme
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomManagementScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val rooms by RoomRepository.rooms.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedRoomForDetails by remember { mutableStateOf<Room?>(null) }
    var selectedRoomForQR by remember { mutableStateOf<Room?>(null) }
    var roomToDelete by remember { mutableStateOf<String?>(null) }
    
    // Photo Upload State
    var showPhotoOptions by remember { mutableStateOf(false) }
    var selectedRoomForUpload by remember { mutableStateOf<Room?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val cameraImageUri = remember { 
        try { 
            val file = File(context.cacheDir, "temp_room_photo.jpg")
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file) 
        } catch (e: Exception) { null } 
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && cameraImageUri != null && selectedRoomForUpload != null) {
            uploadRoomPhoto(context, cameraImageUri, selectedRoomForUpload!!, { isUploading = it }) { updatedRoom ->
                RoomRepository.updateRoom(updatedRoom)
                selectedRoomForUpload = null
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && selectedRoomForUpload != null) {
            uploadRoomPhoto(context, uri, selectedRoomForUpload!!, { isUploading = it }) { updatedRoom ->
                RoomRepository.updateRoom(updatedRoom)
                selectedRoomForUpload = null
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted && cameraImageUri != null) {
            cameraLauncher.launch(cameraImageUri)
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Room Detail", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, null)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Add Room") }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (rooms.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("No rooms added yet", color = Color.Gray)
                        }
                    }
                } else {
                    items(rooms) { room ->
                        PropertyRoomCard(
                            room = room,
                            onEdit = { selectedRoomForDetails = room },
                            onDelete = { roomToDelete = room.roomType }, // Use roomType as ID for deletion logic
                            onUpload = { 
                                selectedRoomForUpload = room
                                showPhotoOptions = true
                            }
                        )
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }

        if (showAddDialog) AddEditRoomDialog(rooms, null, { showAddDialog = false }, { RoomRepository.addRoom(it) })
        if (selectedRoomForDetails != null) {
            AddEditRoomDialog(
                rooms = rooms, 
                initialRoom = selectedRoomForDetails, 
                onDismiss = { selectedRoomForDetails = null }, 
                onConfirm = { 
                    RoomRepository.updateRoom(it)
                    selectedRoomForDetails = null 
                },
                onDeleteClick = {
                    roomToDelete = selectedRoomForDetails!!.roomNumber
                    selectedRoomForDetails = null
                }
            )
        }
        if (selectedRoomForQR != null) QRDialog(selectedRoomForQR!!, { selectedRoomForQR = null }, { bitmap -> shareQRCode(context, bitmap, selectedRoomForQR!!.roomType) })
        
        if (showPhotoOptions) {
            AlertDialog(
                onDismissRequest = { showPhotoOptions = false },
                title = { Text("Update Room Photo") },
                text = { Text("Choose a source for the room photo") },
                confirmButton = {
                    TextButton(onClick = { 
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            cameraImageUri?.let { cameraLauncher.launch(it) }
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                        showPhotoOptions = false 
                    }) { Text("CAMERA") }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        galleryLauncher.launch("image/*")
                        showPhotoOptions = false 
                    }) { Text("GALLERY") }
                }
            )
        }

        if (isUploading) {
            Dialog(onDismissRequest = {}) {
                Box(
                    modifier = Modifier.size(100.dp).background(Color.White, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        if (roomToDelete != null) {
            AlertDialog(
                onDismissRequest = { roomToDelete = null },
                title = { Text("Delete Room") },
                text = { Text("Are you sure you want to delete room $roomToDelete?") },
                confirmButton = {
                    Button(onClick = {
                        RoomRepository.deleteRoom(roomToDelete!!)
                        roomToDelete = null
                        Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                    }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                        Text("DELETE")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { roomToDelete = null }) { Text("CANCEL") }
                }
            )
        }
    }
}

@Composable
fun PropertyRoomCard(
    room: Room,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUpload: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                val img = if (room.imageUrl.isNotEmpty()) room.imageUrl 
                          else "https://images.unsplash.com/photo-1611892440504-42a792e24d32?q=80&w=2070&auto=format&fit=crop"
                
                AsyncImage(
                    model = img, 
                    contentDescription = null, 
                    modifier = Modifier.fillMaxSize(), 
                    contentScale = ContentScale.Crop
                )
                
                Surface(
                    modifier = Modifier.padding(12.dp).align(Alignment.TopEnd),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "${room.totalUnits} Units",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = room.roomType, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = "Total Rooms of this type: ${room.totalUnits}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                Text(text = "Max Occupancy: ${room.maxGuests} Guests", fontSize = 13.sp, color = Color.Gray)
                
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Edit")
                    }
                    OutlinedButton(
                        onClick = onUpload,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Photos")
                    }
                    IconButton(
                        onClick = onDelete,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color.Red)
                    ) {
                        Icon(Icons.Default.Delete, null)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRoomDialog(
    rooms: List<Room>, 
    initialRoom: Room?, 
    onDismiss: () -> Unit, 
    onConfirm: (Room) -> Unit,
    onDeleteClick: (() -> Unit)? = null
) {
    var roomType by remember { mutableStateOf(initialRoom?.roomType ?: "") }
    var totalUnits by remember { mutableIntStateOf(initialRoom?.totalUnits ?: 1) }
    var maxGuests by remember { mutableIntStateOf(initialRoom?.maxGuests ?: 2) }
    
    val hotelId by HotelSession.hotelId.collectAsState()

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            Column {
                TopAppBar(
                    title = { Text(if (initialRoom == null) "Add Room Category" else "Edit Category", fontWeight = FontWeight.Bold) },
                    navigationIcon = { IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )

                Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = roomType,
                        onValueChange = { roomType = it },
                        label = { Text("Room Category Name (e.g. Deluxe, Suite)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    Text("Numbers of this type (Total Units)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if(totalUnits > 1) totalUnits-- }) { Icon(Icons.Default.Remove, null) }
                        Text(totalUnits.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 16.dp))
                        IconButton(onClick = { totalUnits++ }) { Icon(Icons.Default.Add, null) }
                    }
                    
                    Text("Maximum Guests per Room", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if(maxGuests > 1) maxGuests-- }) { Icon(Icons.Default.Remove, null) }
                        Text(maxGuests.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 16.dp))
                        IconButton(onClick = { maxGuests++ }) { Icon(Icons.Default.Add, null) }
                    }

                    Spacer(Modifier.height(24.dp))
                    
                    Button(
                        onClick = { 
                            if (roomType.isNotBlank()) {
                                onConfirm(Room(
                                    roomNumber = roomType, // Syncing internal ID with Category Name
                                    roomType = roomType,
                                    totalUnits = totalUnits,
                                    maxGuests = maxGuests,
                                    hotelId = hotelId ?: ""
                                ))
                                onDismiss()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Category", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun uploadRoomPhoto(
    context: Context, 
    uri: Uri, 
    room: Room, 
    setLoading: (Boolean) -> Unit,
    onSuccess: (Room) -> Unit
) {
    setLoading(true)
    val storageRef = FirebaseStorage.getInstance().reference
        .child("hotels")
        .child(room.hotelId)
        .child("rooms")
        .child(room.roomNumber)
        .child("${UUID.randomUUID()}.jpg")

    storageRef.putFile(uri)
        .addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                val updatedRoom = room.copy(imageUrl = downloadUri.toString())
                onSuccess(updatedRoom)
                setLoading(false)
                Toast.makeText(context, "Photo uploaded successfully", Toast.LENGTH_SHORT).show()
            }
        }
        .addOnFailureListener {
            setLoading(false)
            Toast.makeText(context, "Failed to upload photo: ${it.message}", Toast.LENGTH_SHORT).show()
        }
}

private fun shareQRCode(context: Context, bitmap: Bitmap, roomNumber: String) {
    try {
        val path = File(context.cacheDir, "images"); path.mkdirs()
        val file = File(path, "room_$roomNumber.png")
        val stream = FileOutputStream(file); bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream); stream.close()
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); setDataAndType(uri, context.contentResolver.getType(uri)); putExtra(Intent.EXTRA_STREAM, uri) }, "Share Room $roomNumber QR"))
    } catch (e: Exception) { e.printStackTrace() }
}

@Composable
fun QRDialog(room: Room, onDismiss: () -> Unit, onShare: (Bitmap) -> Unit) {
    val qrBitmap = remember(room) { 
        QRCodeGenerator.generate(room.generateWebPortalLink()) 
    }
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(24.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("R-${room.roomNumber}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Scan to open app", color = Color.Gray)
                Spacer(modifier = Modifier.height(24.dp))
                qrBitmap?.let { Image(it.asImageBitmap(), null, Modifier.size(200.dp)) }
                Spacer(modifier = Modifier.height(24.dp))
                Button({ qrBitmap?.let { onShare(it) } }, Modifier.fillMaxWidth()) { Icon(Icons.Default.Share, null); Spacer(Modifier.width(8.dp)); Text("Share or Print") }
                TextButton(onDismiss, Modifier.fillMaxWidth()) { Text("Close") }
            }
        }
    }
}

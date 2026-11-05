package com.example.staydesk.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.staydesk.ui.theme.StayDeskTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit,
    onSendOtpClick: (String) -> Unit
) {
    var mobileNumber by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Forgot Password",
                modifier = Modifier.align(Alignment.Center),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(40.dp))

        // Illustration Placeholder
        Surface(
            modifier = Modifier.size(160.dp),
            color = Color(0xFFF1F5FE),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Envelope Illustration Simulation
                Surface(
                    modifier = Modifier.size(100.dp, 70.dp),
                    color = Color(0xFF7C9EFB),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Surface(
                            modifier = Modifier.size(32.dp).offset(x = 10.dp, y = 10.dp),
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Lock, null, tint = Color(0xFF0052FF), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Text(
            "Reset Password",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(Modifier.height(12.dp))

        Text(
            "Enter your registered mobile number and we will send you a verification code.",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(Modifier.height(40.dp))

        // Mobile Number Input
        OutlinedTextField(
            value = mobileNumber,
            onValueChange = { mobileNumber = it },
            placeholder = { Text("Mobile Number") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Phone, null) },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                    Text("+91", fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.ArrowDropDown, null)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        Spacer(Modifier.height(32.dp))

        // Send OTP Button
        AuthPrimaryButton(
            text = "Send OTP",
            onClick = { 
                isProcessing = true
                onSendOtpClick(mobileNumber)
            },
            isLoading = isProcessing
        )

        Spacer(Modifier.height(24.dp))

        // Back to Login link
        Text(
            "Back to Login",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onBackClick() }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordPreview() {
    StayDeskTheme {
        ForgotPasswordScreen(
            onBackClick = { },
            onSendOtpClick = { }
        )
    }
}

package com.example.staydesk.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhonelinkLock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.staydesk.ui.theme.StayDeskTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(
    mobileNumber: String,
    verificationId: String,
    onBackClick: () -> Unit,
    onResendClick: () -> Unit,
    onVerifySuccess: (String) -> Unit
) {
    var otpValue by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableIntStateOf(45) }
    
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val auth = remember { if (isPreview) null else FirebaseAuth.getInstance() }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Timer logic
    LaunchedEffect(timeLeft) {
        if (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
    }

    // Automatically focus and show keyboard
    LaunchedEffect(Unit) {
        delay(500)
        focusRequester.requestFocus()
        keyboardController?.show()
    }

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
                "Verify OTP",
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.PhonelinkLock, 
                        contentDescription = null, 
                        tint = Color(0xFF7C9EFB),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Icon(
                        Icons.Default.CheckCircle, 
                        contentDescription = null, 
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Text(
            "Enter OTP",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(Modifier.height(12.dp))

        Text(
            "We have sent a 6-digit OTP to",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Text(
            "+91 $mobileNumber",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))

        // OTP INPUT AREA
        BasicTextField(
            value = otpValue,
            onValueChange = { 
                if (it.length <= 6 && it.all { c -> c.isDigit() }) otpValue = it 
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword
            ),
            decorationBox = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(6) { index ->
                        val char = otpValue.getOrNull(index)?.toString() ?: ""
                        val isFocused = index == otpValue.length
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                                .border(
                                    width = if (isFocused) 2.dp else 1.dp,
                                    color = if (isFocused) MaterialTheme.colorScheme.primary else Color.LightGray,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(if (isFocused) Color(0xFFF1F5FE) else Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        )

        Spacer(Modifier.height(32.dp))

        // Resend OTP text
        if (timeLeft > 0) {
            Row {
                Text("Resend OTP in ", color = Color.Gray, fontSize = 14.sp)
                Text(
                    String.format("%02d:%02d", timeLeft / 60, timeLeft % 60), 
                    color = MaterialTheme.colorScheme.primary, 
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        } else {
            Text(
                "Resend OTP",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable { 
                    timeLeft = 60
                    onResendClick() 
                }
            )
        }

        Spacer(Modifier.weight(1f))

        // Verify & Login Button
        AuthPrimaryButton(
            text = "Verify & Login",
            onClick = { 
                if (otpValue.length == 6) {
                    isVerifying = true
                    val credential = PhoneAuthProvider.getCredential(verificationId, otpValue)
                    auth?.let {
                        signInWithCredential(it, credential, {
                            onVerifySuccess(mobileNumber)
                        }, { error ->
                            isVerifying = false
                            otpValue = ""
                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        })
                    } ?: run {
                        // For preview/testing without Firebase
                        onVerifySuccess(mobileNumber)
                    }
                }
            },
            isLoading = isVerifying,
            enabled = otpValue.length == 6
        )
        
        Spacer(Modifier.height(24.dp))
    }
}

private fun signInWithCredential(
    auth: FirebaseAuth, 
    credential: PhoneAuthCredential,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    auth.signInWithCredential(credential).addOnCompleteListener { task ->
        if (task.isSuccessful) {
            onSuccess()
        } else {
            onFailure(task.exception?.message ?: "Verification failed")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OtpVerificationPreview() {
    StayDeskTheme {
        OtpVerificationScreen(
            mobileNumber = "98765 43210",
            verificationId = "test",
            onBackClick = { },
            onResendClick = { },
            onVerifySuccess = { }
        )
    }
}

package com.example.staydesk.ui.auth

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.example.staydesk.R
import com.example.staydesk.data.AuthRepository
import com.example.staydesk.ui.theme.StayDeskTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

@Composable
fun LoginScreen(
    onLoginSuccess: (String, String, String?) -> Unit,
    onSignUpClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    var isProcessing by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val auth = remember { if (isPreview) null else FirebaseAuth.getInstance() }

    val gso = remember(context) {
        val webClientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            "160367544031-vcl96unr9uioer7scj9sc99h2o5q90re.apps.googleusercontent.com"
        }
        
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .requestProfile()
            .build()
    }
    
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                if (auth != null) {
                    auth.signInWithCredential(credential).addOnCompleteListener { signInTask ->
                        if (signInTask.isSuccessful) {
                            val user = auth.currentUser
                            onLoginSuccess(
                                user?.email ?: account.email ?: "", 
                                user?.displayName ?: account.displayName ?: "Google User", 
                                user?.photoUrl?.toString() ?: account.photoUrl?.toString()
                            )
                        } else {
                            val msg = signInTask.exception?.localizedMessage ?: "Firebase auth failed"
                            Toast.makeText(context, "Auth failed: $msg", Toast.LENGTH_LONG).show()
                        }
                        isProcessing = false
                    }
                } else {
                    onLoginSuccess(account.email ?: "", account.displayName ?: "Google User", account.photoUrl?.toString())
                    isProcessing = false
                }
            } else {
                Toast.makeText(context, "Could not retrieve Google ID Token", Toast.LENGTH_SHORT).show()
                isProcessing = false
            }
        } catch (e: ApiException) {
            val errorMsg = when (e.statusCode) {
                12501 -> "Google Sign-In canceled"
                10 -> "Developer Error (10): Missing SHA-1 in Firebase Console"
                12500 -> "Sign-in Failed (12500): Check Play Services or Firebase"
                else -> "Google Sign-In error (${e.statusCode}): ${e.localizedMessage}"
            }
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            isProcessing = false
        } catch (e: Exception) {
            Toast.makeText(context, "Google Sign-In failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            isProcessing = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        AsyncImage(
            model = "https://images.unsplash.com/photo-1566073771259-6a8506099945?q=80&w=2070&auto=format&fit=crop",
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Gradient Overlay (Simulated with semi-transparent background on content)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))
            
            // Logo and Title
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    color = Color.Transparent
                ) {
                    StayDeskLogo(modifier = Modifier.fillMaxSize())
                }
                Text(
                    text = "StayDesk",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Hotel Management Simplified",
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(40.dp))

            // Login Card (More Translucent / Pardarshi)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Welcome!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Sign in to manage your hotel property",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(28.dp))

                    // Google Login
                    GoogleLoginButton(
                        onClick = {
                            isProcessing = true
                            val activity = context.findActivity()
                            if (activity != null) {
                                val client = GoogleSignIn.getClient(activity, gso)
                                client.signOut().addOnCompleteListener {
                                    googleLauncher.launch(client.signInIntent)
                                }
                            } else {
                                isProcessing = false
                                Toast.makeText(context, "Activity error", Toast.LENGTH_SHORT).show()
                            }
                        },
                        isLoading = isProcessing
                    )
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    StayDeskTheme {
        LoginScreen(
            onLoginSuccess = { _, _, _ -> },
            onSignUpClick = { },
            onForgotPasswordClick = { }
        )
    }
}

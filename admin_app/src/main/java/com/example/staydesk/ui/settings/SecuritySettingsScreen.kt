package com.example.staydesk.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SecuritySettingsScreen(
    onBackClick: () -> Unit,
    onAppLockClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SettingsCardItem(
                title = "App Lock",
                subtitle = "Manage Mobile PIN and Biometric lock",
                icon = Icons.Default.Lock,
                onClick = onAppLockClick
            )
        }
    }
}

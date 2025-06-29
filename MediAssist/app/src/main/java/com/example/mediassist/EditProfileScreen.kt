package com.example.mediassist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    fullName: String,
    email: String,
    userId: String,
    db: FirebaseFirestore,
    onLogoutClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var name by remember { mutableStateOf(fullName) }
    var emailInput by remember { mutableStateOf(email) }
    var showMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Edit Profile", style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile Image",
                    modifier = Modifier.size(100.dp)
                )

                Text(text = name, style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val updates = mapOf(
                            "fullName" to name,
                            "email" to emailInput
                        )
                        db.collection("users").document(userId).update(updates)
                            .addOnSuccessListener {
                                showMessage = "Profile updated successfully!"
                            }
                            .addOnFailureListener {
                                showMessage = "Failed to update profile: ${it.message}"
                            }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // Green
                ) {
                    Text("SAVE CHANGES", color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onLogoutClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("LOGOUT", color = Color.White)
                }

                if (showMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = showMessage, color = Color.DarkGray, fontSize = 14.sp)
                }
            }
        }
    )
}
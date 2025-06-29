package com.example.mediassist.util

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.mediassist.EditProfileScreen
import com.example.mediassist.WelcomeActivity
import com.example.mediassist.ui.theme.MediAssistTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid

            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    val fullName = document.getString("fullName") ?: ""
                    val email = document.getString("email") ?: ""

                    setContent {
                        MediAssistTheme {
                            EditProfileScreen(
                                fullName = fullName,
                                email = email,
                                userId = userId,
                                db = db,
                                onLogoutClick = {
                                    auth.signOut()
                                    // Redirect to welcome page
                                    val intent = Intent(this, WelcomeActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                },
                                onBackClick = { finish() }
                            )
                        }
                    }
                }
                .addOnFailureListener {
                }
        }
    }
}

package com.example.mediassist

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthEmailException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.btn_signin).setOnClickListener {
            val email = findViewById<EditText>(R.id.editTextEmail).text.toString().trim()
            val password = findViewById<EditText>(R.id.editTextPassword).text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null) {
                            checkUserRoleAndNavigate(user)
                        }
                    } else {
                        val errorMessage = when (task.exception) {
                            is FirebaseAuthInvalidCredentialsException -> {
                                "Invalid password. Please check your password and try again."
                            }
                            is FirebaseAuthInvalidUserException -> {
                                "No account found with this email address. Please check your email or create a new account."
                            }
                            is FirebaseAuthWeakPasswordException -> {
                                "Password is too weak. Please use a stronger password."
                            }
                            is FirebaseAuthUserCollisionException -> {
                                "An account with this email already exists. Please use a different email or try signing in."
                            }
                            is FirebaseAuthEmailException -> {
                                "Invalid email format. Please enter a valid email address."
                            }
                            is FirebaseTooManyRequestsException -> {
                                "Too many failed login attempts. Please try again later."
                            }
                            is FirebaseNetworkException -> {
                                "Network error. Please check your internet connection and try again."
                            }
                            else -> {
                                "Login failed: ${task.exception?.message ?: "Unknown error occurred"}"
                            }
                        }
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
        }

        // Navigate to Register
        findViewById<TextView>(R.id.textView2).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Navigate to Forgot Password
        findViewById<TextView>(R.id.textView3).setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun checkUserRoleAndNavigate(user: FirebaseUser) {
        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                val role = document.getString("role")
                if (role == "healthcare" || role == "healthcare professional") {
                    startActivity(Intent(this, DoctorHomepageActivity::class.java))
                } else {
                    startActivity(Intent(this, MainActivity::class.java))
                }
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to get user role", Toast.LENGTH_SHORT).show()
            }
    }
}

package com.example.mediassist

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val fullNameField = findViewById<EditText>(R.id.editTextFullName)
        val emailField = findViewById<EditText>(R.id.editTextEmail)
        val passwordField = findViewById<EditText>(R.id.editTextPassword)
        val confirmPasswordField = findViewById<EditText>(R.id.editTextConfirmPassword)
        val radioGroupRole = findViewById<RadioGroup>(R.id.radioGroupRole)
        val buttonRegister = findViewById<Button>(R.id.buttonRegister)

        // Ensure default selection is Patient
        radioGroupRole.check(R.id.radioPatient)

        buttonRegister.setOnClickListener {
            val fullName = fullNameField.text.toString().trim()
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            val confirmPassword = confirmPasswordField.text.toString().trim()
            val selectedRoleId = radioGroupRole.checkedRadioButtonId
            
            // Explicit role selection logic
            val selectedRole = when (selectedRoleId) {
                R.id.radioPatient -> "patient"
                R.id.radioHealthcare -> "healthcare"
                else -> null
            }

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedRole == null) {
                Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firebase registration
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        val userMap = hashMapOf(
                            "fullName" to fullName,
                            "email" to email,
                            "role" to selectedRole
                        )

                        db.collection("users").document(userId!!)
                            .set(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registered successfully as $selectedRole", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                            }

                    } else {
                        Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}

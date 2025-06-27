package com.example.mediassist

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class CompleteProfileActivity : AppCompatActivity() {

    private lateinit var editTextName: EditText
    private lateinit var radioGroupRole: RadioGroup
    private lateinit var btnSave: Button

    private lateinit var firebaseUser: FirebaseUser
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complete_profile)

        editTextName = findViewById(R.id.editTextName)
        radioGroupRole = findViewById(R.id.radioGroupRole)
        btnSave = findViewById(R.id.btnSaveProfile)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        btnSave.setOnClickListener {
            saveUserProfile()
        }
    }

    private fun saveUserProfile() {
        val name = editTextName.text.toString().trim()
        val selectedRoleId = radioGroupRole.checkedRadioButtonId

        if (name.isEmpty() || selectedRoleId == -1) {
            Toast.makeText(this, "Please enter name and select a role", Toast.LENGTH_SHORT).show()
            return
        }

        val role = findViewById<RadioButton>(selectedRoleId).text.toString().lowercase()

        val userData = hashMapOf(
            "email" to firebaseUser.email,
            "fullName" to name,
            "role" to role
        )

        firestore.collection("users").document(firebaseUser.uid)
            .set(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show()

                // Redirect based on role
                if (role.contains("healthcare")) {
                    startActivity(Intent(this, DoctorHomepageActivity::class.java))
                } else {
                    startActivity(Intent(this, MainActivity::class.java))
                }
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error saving profile: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}

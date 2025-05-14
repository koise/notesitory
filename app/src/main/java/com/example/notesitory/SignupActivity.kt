package com.example.notesitory

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.UnderlineSpan
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.material.textfield.TextInputEditText

class SignupActivity : AppCompatActivity() {
    private lateinit var usernameInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText
    private lateinit var phoneNumberInput: TextInputEditText
    private lateinit var sharedPreferences: SharedPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Initialize secure shared preferences
        val masterKey = MasterKey.Builder(applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
            
        sharedPreferences = EncryptedSharedPreferences.create(
            applicationContext,
            "notesitory_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        
        // Initialize UI elements using IDs from layout
        // Using email as username for simplicity
        usernameInput = findViewById(R.id.edtEmail)
        passwordInput = findViewById(R.id.edtPassword)
        confirmPasswordInput = findViewById(R.id.edtConfirmPassword)
        phoneNumberInput = findViewById(R.id.edtPhoneNumber)

        val textView = findViewById<TextView>(R.id.txtLogin)
        val text = "Already have an account? Login here"
        val spannable = SpannableString(text)
        spannable.setSpan(UnderlineSpan(), text.indexOf("Login here"), text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView.text = spannable
        textView.movementMethod = LinkMovementMethod.getInstance()
        val txtLogin = findViewById<TextView>(R.id.txtLogin)
        txtLogin.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Phone number formatting
        phoneNumberInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                if (charSequence.isNotEmpty() && !charSequence.toString().startsWith("+63")) {
                    phoneNumberInput.setText("+63")
                    phoneNumberInput.setSelection(3)
                }
            }
            override fun afterTextChanged(editable: Editable) {}
        })

        // Sign up button handler
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        btnSignUp.setOnClickListener {
            if (validateInputs()) {
                createAccount()
            }
        }
    }
    
    private fun validateInputs(): Boolean {
        val username = usernameInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        val confirmPassword = confirmPasswordInput.text.toString().trim()
        val phoneNumber = phoneNumberInput.text.toString().trim()
        
        if (username.isEmpty()) {
            showError("Please enter a username")
            return false
        }
        
        if (password.isEmpty()) {
            showError("Please enter a password")
            return false
        }
        
        if (password.length < 6) {
            showError("Password must be at least 6 characters")
            return false
        }
        
        if (password != confirmPassword) {
            showError("Passwords do not match")
            return false
        }
        
        if (phoneNumber.length < 13) {  // +63 plus 10 digits
            showError("Please enter a valid phone number")
            return false
        }
        
        // Check if username already exists
        if (sharedPreferences.contains(username)) {
            showError("Username already exists")
            return false
        }
        
        return true
    }
    
    private fun createAccount() {
        val username = usernameInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        val phoneNumber = phoneNumberInput.text.toString().trim()
        
        // Store user credentials
        sharedPreferences.edit()
            .putString(username, password)
            .putString("$username:phone", phoneNumber)
            .apply()
            
        Toast.makeText(applicationContext, "Account created successfully", Toast.LENGTH_SHORT).show()
        
        // Navigate back to login
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    private fun showError(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
}

package com.example.notesitory

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
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

class MainActivity : AppCompatActivity() {
    private lateinit var usernameInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var sharedPreferences: SharedPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
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
        
        // Check if user is already logged in
        if (isLoggedIn()) {
            navigateToDashboard()
            return
        }
        
        // Initialize UI elements - using the IDs from the layout file
        usernameInput = findViewById(R.id.edtName)
        passwordInput = findViewById(R.id.edtPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnLoginGoogle = findViewById<Button>(R.id.btnLoginGoogle)

        btnLogin.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            
            if (validateCredentials(username, password)) {
                loginUser(username)
            }
        }

        btnLoginGoogle.setOnClickListener {
            // For demonstration, we'll just simulate Google login
            Toast.makeText(applicationContext, "Google login would be implemented here", Toast.LENGTH_SHORT)
                .show()
            loginUser("google_user@example.com")
        }

        val textView = findViewById<TextView>(R.id.txtSignup)
        val text = "Don't have an account? Sign up here"
        val spannable = SpannableString(text)

        spannable.setSpan(
            UnderlineSpan(),
            text.indexOf("Sign up here"),
            text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        textView.text = spannable
        textView.movementMethod = LinkMovementMethod.getInstance()

        textView.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun validateCredentials(username: String, password: String): Boolean {
        if (username.isEmpty()) {
            showError("Please enter a username")
            return false
        }
        
        if (password.isEmpty()) {
            showError("Please enter a password")
            return false
        }
        
        // Check if this is a registered user
        val storedPassword = sharedPreferences.getString(username, null)
        if (storedPassword == null) {
            showError("User not found. Please sign up first.")
            return false
        }
        
        if (storedPassword != password) {
            showError("Incorrect password")
            return false
        }
        
        return true
    }
    
    private fun loginUser(username: String) {
        // Save login state
        sharedPreferences.edit()
            .putBoolean("is_logged_in", true)
            .putString("current_user", username)
            .apply()
            
        Toast.makeText(applicationContext, "Welcome back, $username", Toast.LENGTH_SHORT).show()
        navigateToDashboard()
    }
    
    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish() // Prevent going back to login screen
    }
    
    private fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("is_logged_in", false)
    }
    
    private fun showError(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
}

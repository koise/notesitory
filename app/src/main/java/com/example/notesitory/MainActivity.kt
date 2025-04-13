package com.example.notesitory

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.UnderlineSpan
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnLoginGoogle: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        usernameEditText = findViewById(R.id.edtName)
        passwordEditText = findViewById(R.id.edtPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnLoginGoogle = findViewById(R.id.btnLoginGoogle)

        // Setting up the signup link
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

        setupWebView()

        btnLogin.setOnClickListener {
            sendLoginRequest()
        }

        btnLoginGoogle.setOnClickListener {
            Toast.makeText(applicationContext, "You logged via Google", Toast.LENGTH_SHORT)
                .show()
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupWebView() {
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // When the login page finishes loading, check for the response
                if (url != null && url.contains("login.php")) {
                    webView.evaluateJavascript(
                        "(function() { return document.body.innerText; })();"
                    ) { response ->
                        val cleanResponse = response.replace("\"", "").trim() // Clean the response
                        handleLoginResponse(cleanResponse)
                    }
                }
            }
        }
    }

    private fun sendLoginRequest() {
        val username = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Both fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        val apiUrl = "http://koise.fwh.is/login.php?txtUsername=$username&txtPassword=$password"
        webView.loadUrl(apiUrl)
    }

    private fun handleLoginResponse(response: String) {
        try {
            // Log the raw response first to understand the format
            Log.d("LoginResponse", "Raw response: $response")

            // Clean up the response: remove unnecessary backslashes and unwanted characters
            var cleanedResponse = response.replace("\\", "")

            // Add quotes around keys and values (adjusting the pattern for better cleanup)
            cleanedResponse = cleanedResponse
                .replace(Regex("([a-zA-Z0-9_]+):"), "\"$1\":") // Add quotes around keys
                .replace(Regex(":([^\",}]+)(?=[,}])"), ":\"$1\"") // Add quotes around unquoted values

            // Log the cleaned response for debugging
            Log.d("LoginResponse", "Cleaned response: $cleanedResponse")

            // Check if response is empty after cleaning
            if (cleanedResponse.isEmpty()) {
                Toast.makeText(this, "Empty or invalid response", Toast.LENGTH_SHORT).show()
                return
            }

            // Parse the cleaned JSON response
            val jsonResponse = JSONObject(cleanedResponse)

            // Check if the response contains expected fields
            if (!jsonResponse.has("status") || !jsonResponse.has("firstName") ||
                !jsonResponse.has("lastName") || !jsonResponse.has("phone") || !jsonResponse.has("email")) {
                Toast.makeText(this, "Invalid response format", Toast.LENGTH_SHORT).show()
                return
            }

            // Extract values from the JSON object
            val status = jsonResponse.getString("status")
            val firstName = jsonResponse.getString("firstName")
            val lastName = jsonResponse.getString("lastName")

            // Set default phone number if not available or invalid
            val phone = jsonResponse.optString("phone", "09")  // Use "09" as default if phone is missing or invalid

            val email = jsonResponse.getString("email")

            // Process login response based on the status
            if (status == "Login successful") {
                // Save user data in SharedPreferences
                saveUserInfo(firstName, lastName, phone, email)

                // Show welcome message
                Toast.makeText(this, "Welcome, $firstName!", Toast.LENGTH_SHORT).show()

                // Proceed to Dashboard Activity
                val intent = Intent(this, DashboardActivity::class.java)
                intent.putExtra("firstName", firstName) // Optionally pass the firstName to DashboardActivity
                startActivity(intent)
                finish()  // Close the login screen
            } else {
                // Show error message for login failure
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            // Log the error and show a Toast message for the user
            Log.e("LoginError", "Error processing the response: ${e.message}")
            Toast.makeText(this, "Error processing the response", Toast.LENGTH_SHORT).show()
        }
    }





    private fun saveUserInfo(firstName: String, lastName: String, phone: String, email: String) {
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Save data in SharedPreferences
        editor.putString("firstName", firstName)
        editor.putString("lastName", lastName)
        editor.putString("phone", phone)
        editor.putString("email", email)
        editor.apply() // Commit changes

        // Log the saved data for debugging
        Log.d("UserInfo", "Saved firstName: $firstName")
        Log.d("UserInfo", "Saved lastName: $lastName")
        Log.d("UserInfo", "Saved phone: $phone")
        Log.d("UserInfo", "Saved email: $email")
    }

}

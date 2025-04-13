package com.example.notesitory

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import java.net.URLEncoder
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var passwordConfirmEditText: EditText
    private lateinit var signUpButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        webView = findViewById(R.id.webView)
        firstNameEditText = findViewById(R.id.edtFirstName)
        lastNameEditText = findViewById(R.id.edtLastName)
        phoneEditText = findViewById(R.id.edtPhoneNumber)
        emailEditText = findViewById(R.id.edtEmail)
        usernameEditText = findViewById(R.id.edtUsername)
        passwordEditText = findViewById(R.id.edtPassword)
        passwordConfirmEditText = findViewById(R.id.edtConfirmPassword)
        signUpButton = findViewById(R.id.btnSignUp)

        val loginTextView = findViewById<TextView>(R.id.txtLogin)
        loginTextView.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        setupWebView()

        signUpButton.setOnClickListener {
            sendSignupRequest()
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

                // Ensure the page has finished loading before evaluating JavaScript
                if (url != null && url.contains("addUser.php")) {
                    webView.evaluateJavascript(
                        "(function() { return document.body.innerText; })();"
                    ) { response ->
                        val cleanResponse = response.replace("\"", "").trim() // Trim response
                        Log.d("SignupActivity", "WebView response: $cleanResponse") // Log response

                        // Check if the response contains "User Added Successfully"
                        if (cleanResponse.contains("User Added Successfully", ignoreCase = true)) {
                            showAlertDialog("Success", "User Added Successfully") {
                                // After user presses "OK", go to MainActivity
                                startActivity(Intent(this@SignupActivity, MainActivity::class.java))
                                finish() // Close the SignupActivity
                            }
                        } else {
                            showAlertDialog("Error", cleanResponse)
                        }
                    }
                }
            }
        }
    }

    private fun sendSignupRequest() {
        val firstName = URLEncoder.encode(firstNameEditText.text.toString().trim(), "UTF-8")
        val lastName = URLEncoder.encode(lastNameEditText.text.toString().trim(), "UTF-8")
        val phone = URLEncoder.encode(phoneEditText.text.toString().trim(), "UTF-8")
        val email = URLEncoder.encode(emailEditText.text.toString().trim(), "UTF-8")
        val username = URLEncoder.encode(usernameEditText.text.toString().trim(), "UTF-8")
        val password = URLEncoder.encode(passwordEditText.text.toString().trim(), "UTF-8")
        val confirmPassword = passwordConfirmEditText.text.toString().trim()

        // Validate the password and confirm password
        if (password != confirmPassword) {
            showAlertDialog("Error", "Passwords do not match")
            return
        }

        if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty() || email.isEmpty() ||
            username.isEmpty() || password.isEmpty()
        ) {
            showAlertDialog("Error", "All fields are required")
            return
        }

        // Construct the URL with proper parameters
        val apiUrl = "http://koise.fwh.is/addUser.php?txtFirst=$firstName&txtLast=$lastName&txtPhone=$phone&txtEmail=$email&txtUsername=$username&txtPassword=$password"
        Log.d("SignupActivity", "Final API URL: $apiUrl")

        // Load the URL in WebView
        webView.loadUrl(apiUrl)
    }


    private fun showAlertDialog(title: String, message: String, onOkClick: (() -> Unit)? = null) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, id ->
            dialog.dismiss()
            onOkClick?.invoke()
        }
        builder.show()
    }
}

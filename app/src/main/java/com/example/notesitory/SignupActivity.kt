package com.example.notesitory

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val firstNameEditText = findViewById<EditText>(R.id.edtFirstName)
        val lastNameEditText = findViewById<EditText>(R.id.edtLastName)
        val phoneEditText = findViewById<EditText>(R.id.edtPhoneNumber)
        val emailEditText = findViewById<EditText>(R.id.edtEmail)
        val usernameEditText = findViewById<EditText>(R.id.editUsername)
        val passwordEditText = findViewById<EditText>(R.id.edtPassword)
        val confirmPasswordEditText = findViewById<EditText>(R.id.edtConfirmPassword)
        val signUpButton = findViewById<Button>(R.id.btnSignUp)
        val loginTextView = findViewById<TextView>(R.id.txtLogin)

        loginTextView.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        phoneEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Ensure prefix "+63" stays
                if (!s.toString().startsWith("+63")) {
                    phoneEditText.setText("+63")
                    phoneEditText.setSelection( phoneEditText.text.length)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        signUpButton.setOnClickListener {
            val firstName = firstNameEditText.text.toString().trim()
            val lastName = lastNameEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty() || email.isEmpty() ||
                username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Thread {
                sendSignUpRequest(firstName, lastName, phone, email, username, password)
            }.start()
        }
    }

    private fun sendSignUpRequest(
        firstName: String, lastName: String, phone: String,
        email: String, username: String, password: String
    ) {
        try {
            val url = URL("http://koise.fwh.is/addUser.php?firstName=$firstName&lastName=$lastName&phone=$phone&email=$email&username=$username&password=$password")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            if (responseCode == 200) {
                val inputStream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
                inputStream.close()

                runOnUiThread {
                    showDialog("Signup Status", response.toString())
                }
            } else {
                runOnUiThread {
                    showDialog("Signup Status", "Failed to send request. Response code: $responseCode")
                }
            }
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                showDialog("Signup Status", "Error: ${e.message}")
            }
        }
    }

    private fun showDialog(title: String, message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
        builder.setTitle(title)
        builder.setCancelable(false)
        builder.setPositiveButton("Close") { dialog, _ ->
            dialog.cancel()
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }
}

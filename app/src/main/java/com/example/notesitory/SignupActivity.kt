package com.example.notesitory

import android.content.Intent
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

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val textView = findViewById<TextView>(R.id.txtLogin)
        val text = "Don't have an account? Login in here"
        val spannable = SpannableString(text)
        spannable.setSpan(UnderlineSpan(), text.indexOf("Login in here"), text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView.text = spannable
        textView.movementMethod = LinkMovementMethod.getInstance()
        val txtLogin = findViewById<TextView>(R.id.txtLogin)
        txtLogin.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


        val edtPhoneNumber = findViewById<EditText>(R.id.edtPhoneNumber)
        edtPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                if (charSequence.isNotEmpty() && !charSequence.toString().startsWith("+63")) {
                    edtPhoneNumber.setText("+63")
                    edtPhoneNumber.setSelection(3)
                }
            }
            override fun afterTextChanged(editable: Editable) {}
        })


        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        btnSignUp.setOnClickListener{
            Toast.makeText(applicationContext, "Created an accounted Completed", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


    }
}

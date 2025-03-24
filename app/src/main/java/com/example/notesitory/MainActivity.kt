package com.example.notesitory

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.UnderlineSpan
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnLoginGoogle = findViewById<Button>(R.id.btnLoginGoogle)

        btnLogin.setOnClickListener {
            Toast.makeText(applicationContext, "You logged via Username", Toast.LENGTH_SHORT)
                .show()
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }

        btnLoginGoogle.setOnClickListener {
            Toast.makeText(applicationContext, "You logged via Google", Toast.LENGTH_SHORT)
                .show()
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
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
}

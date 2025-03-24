package com.example.notesitory

import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ViewNote : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_note)

        val title = intent.getStringExtra("title") ?: "No Title"
        val description = intent.getStringExtra("description") ?: "No Description"
        val date = intent.getStringExtra("date") ?: "No Date"

        findViewById<TextView>(R.id.viewTitle).text = title
        findViewById<TextView>(R.id.viewDescription).apply {
            text = description
            Linkify.addLinks(this, Linkify.WEB_URLS)
            movementMethod = LinkMovementMethod.getInstance()
        }
    }
}

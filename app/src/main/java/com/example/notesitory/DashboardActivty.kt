package com.example.notesitory

import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DashboardActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper()) // Handler for delay
    private var isScrolling = false // To track scroll state


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        //val nestedScrollView: NestedScrollView? = findViewById(R.id.nestedScrollView)
        bottomNavigationView.itemRippleColor = ColorStateList.valueOf(Color.TRANSPARENT)
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.bottom_nav_slide_up)
        bottomNavigationView.startAnimation(slideUp)

        val notesFragment = NoteFragment()
        val todoFragment = TodoFragment()

        setCurrentFragment(notesFragment)


        val btnAddNote = findViewById<FloatingActionButton>(R.id.btnAddNote)

        // Show Dialog on FAB Click
        btnAddNote.setOnClickListener {
            showChoiceDialog()
        }

        /*
        nestedScrollView?.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            isScrolling = true
            handler.removeCallbacksAndMessages(null)

            if (scrollY > oldScrollY) {
                bottomNavigationView.animate()
                    .translationY(bottomNavigationView.height.toFloat())
                    .setDuration(300)
                    .start()
            } else if (scrollY < oldScrollY) { // Scrolling Up
                bottomNavigationView.animate()
                    .translationY(0f)
                    .setDuration(300)
                    .start()
            }

            // Auto pop-up after 500ms if user stops scrolling
            handler.postDelayed({
                if (!isScrolling) {
                    bottomNavigationView.animate()
                        .translationY(0f)
                        .setDuration(300)
                        .start()
                }
            }, 500)

            isScrolling = false // Reset scrolling state
        }
        */

        // Handle Bottom Navigation Clicks
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.note -> setCurrentFragment(notesFragment)
                R.id.todo -> setCurrentFragment(todoFragment)
            }
            true
        }
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Exit App")
        builder.setMessage("Are you sure you want to exit?")

        builder.setPositiveButton("Yes") { _, _ ->
            finishAffinity() // Close all activities and exit the app
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss() // Dismiss the dialog and stay in the app
        }

        builder.show()
    }


    private fun setCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(R.id.flFragment, fragment)
            .commit()
    }

    private fun showChoiceDialog() {
    val options = arrayOf("To-Do", "Note")

    val builder = AlertDialog.Builder(this)
    builder.setTitle("Choose an Option")
    builder.setItems(options) { _, which ->
        when (which) {
            0 -> startActivity(Intent(this, NewNote::class.java))
            1 -> startActivity(Intent(this, NewNote::class.java))
        }
    }
    builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
    builder.show()
}
}

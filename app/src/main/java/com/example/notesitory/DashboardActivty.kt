package com.example.notesitory

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView

class DashboardActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var navigationView: NavigationView
    private lateinit var btnAddNote: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Initialize views
        drawerLayout = findViewById(R.id.drawerLayout)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        navigationView = findViewById(R.id.navigationView)
        btnAddNote = findViewById(R.id.btnAddNote)

        setSupportActionBar(toolbar)

        // Set up ActionBarDrawerToggle to handle menu icon clicks
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Move navigation drawer to open from the RIGHT
        toolbar.setNavigationOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                drawerLayout.openDrawer(GravityCompat.END)
            }
        }

        // Set default fragment
        val notesFragment = NoteFragment()
        val todoFragment = TodoFragment()
        setCurrentFragment(notesFragment)

        // Floating button click
        btnAddNote.setOnClickListener {
            showChoiceDialog()
        }

        // Bottom Navigation Clicks
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.note -> setCurrentFragment(notesFragment)
                R.id.todo -> setCurrentFragment(todoFragment)
            }
            true
        }

        // Drawer Navigation Clicks
        navigationView.setNavigationItemSelectedListener { menuItem ->
            handleDrawerItemClick(menuItem)
            true
        }
    }

    private fun setCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.flFragment, fragment)
            .commit()
    }

    private fun showChoiceDialog() {
        val options = arrayOf("To-Do", "Note")
        val builder = android.app.AlertDialog.Builder(this)
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

    private fun handleDrawerItemClick(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.nav_profile -> {
                // Handle Profile
            }
            R.id.nav_logout -> {
                logout()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.END)
    }

    private fun logout() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to logout?")
        builder.setPositiveButton("Yes") { _, _ ->
            val sharedPreferences = getSharedPreferences("USERPREF", MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }
}

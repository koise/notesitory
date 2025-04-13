package com.example.notesitory

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.widget.Toast
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
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Initialize views
        drawerLayout = findViewById(R.id.drawerLayout)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        navigationView = findViewById(R.id.navigationView)
        btnAddNote = findViewById(R.id.btnAddNote)
        sharedPreferences = getSharedPreferences("USERPREF", MODE_PRIVATE)

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
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose an Option")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> {
                    val addTodoDialog = AddTodoDialogFragment()
                    addTodoDialog.show(supportFragmentManager, "AddTodoDialog")
                }
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
            R.id.nav_recentlydeleted -> {
                val intent = Intent(this, RecentlyDeleted::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            R.id.nav_logout -> {
                showLogoutDialog()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.END)
    }

    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to logout?")
        builder.setPositiveButton("Yes") { _, _ ->
            logout()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun logout() {
        // Clear SharedPreferences
        sharedPreferences.edit().clear().apply()

        // Navigate to LoginActivity and finish current activity
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Exit App")
        builder.setMessage("Are you sure you want to exit?")
        builder.setPositiveButton("Yes") { _, _ ->
            finishAffinity() // Close all activities
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }
}

package com.example.notesitory

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var folderViewModel: FolderViewModel
    private lateinit var sharedPreferences: SharedPreferences
    
    private val createBackupLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                createBackup(uri)
            }
        }
    }
    
    private val restoreBackupLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                restoreBackup(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        setupViews()
        
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
        
        setupNavigationDrawer()
        setupFabButton()
        
        // Initialize default fragment
        if (savedInstanceState == null) {
            setCurrentFragment(NoteFragment())
            navigationView.setCheckedItem(R.id.nav_notes)
        }
        
        // Handle back press with custom behavior
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    showExitConfirmationDialog()
                }
            }
        })

        // Initialize view models
        noteViewModel = ViewModelProvider(this)[NoteViewModel::class.java]
        folderViewModel = ViewModelProvider(this)[FolderViewModel::class.java]
    }

    private fun setupViews() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        fabAdd = findViewById(R.id.fabAdd)
    }
    
    private fun setupNavigationDrawer() {
        val toggle = ActionBarDrawerToggle(
            this, 
            drawerLayout, 
            toolbar, 
            R.string.navigation_drawer_open, 
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        
        navigationView.setNavigationItemSelectedListener(this)
        
        // Update user email if available
        val headerView = navigationView.getHeaderView(0)
        val userEmailTextView = headerView.findViewById<TextView>(R.id.textViewUserEmail)
        val userEmail = sharedPreferences.getString("user_email", "Your Notes Repository")
        userEmailTextView.text = userEmail
    }
    
    private fun setupFabButton() {
        fabAdd.setOnClickListener {
            // Check current fragment to determine action
            val currentFragment = supportFragmentManager.findFragmentById(R.id.flFragment)
            when (currentFragment) {
                is NoteFragment -> {
                    val intent = Intent(this, NewNote::class.java)
                    startActivity(intent)
                }
                is FolderFragment -> {
                    val folderFragment = currentFragment as? FolderFragment
                    folderFragment?.let {
                        val method = it.javaClass.getDeclaredMethod("showAddFolderDialog")
                        method.isAccessible = true
                        method.invoke(it)
                    }
                }
            }
        }
    }
    
    private fun showExitConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Exit App")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Yes") { _, _ ->
                finishAffinity() // Close all activities and exit the app
            }
            .setNegativeButton("No", null)
            .show()
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
            
        // Update UI according to fragment type
        updateUiForFragment(fragment)
    }
    
    private fun updateUiForFragment(fragment: Fragment) {
        when (fragment) {
            is NoteFragment -> {
                // Get folder name if available from fragment arguments
                val folderName = fragment.arguments?.getString("folderName")
                title = folderName ?: "Notes"
                fabAdd.show()
            }
            is FolderFragment -> {
                title = "Folders"
                fabAdd.show()
            }
            else -> {
                fabAdd.hide()
            }
        }
    }
    
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_notes -> {
                setCurrentFragment(NoteFragment())
            }
            R.id.nav_folders -> {
                setCurrentFragment(FolderFragment())
            }
            R.id.nav_backup -> {
                initiateBackup()
            }
            R.id.nav_restore -> {
                initiateRestore()
            }
            R.id.nav_logout -> {
                logout()
            }
        }
        
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    
    private fun initiateBackup() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            putExtra(Intent.EXTRA_TITLE, "notesitory_backup_$timestamp.json")
        }
        createBackupLauncher.launch(intent)
    }
    
    private fun initiateRestore() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Restore Backup")
            .setMessage("This will replace all your current data with the backup. Continue?")
            .setPositiveButton("Continue") { _, _ ->
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/json"
                }
                restoreBackupLauncher.launch(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun createBackup(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val backupJson = JSONObject()
                
                // Collect notes
                val notesArray = JSONArray()
                noteViewModel.allNotes.value?.forEach { note ->
                    val noteJson = JSONObject().apply {
                        put("id", note.id)
                        put("title", note.title)
                        put("description", note.description)
                        put("date", note.date)
                        put("folderId", note.folderId ?: JSONObject.NULL)
                        put("tags", note.tags)
                    }
                    notesArray.put(noteJson)
                }
                backupJson.put("notes", notesArray)
                
                // Collect folders
                val foldersArray = JSONArray()
                folderViewModel.allFolders.value?.forEach { folder ->
                    val folderJson = JSONObject().apply {
                        put("id", folder.id)
                        put("name", folder.name)
                        put("description", folder.description)
                        put("creationDate", folder.creationDate)
                        put("color", folder.color)
                    }
                    foldersArray.put(folderJson)
                }
                backupJson.put("folders", foldersArray)
                
                // Write to file
                contentResolver.openFileDescriptor(uri, "w")?.use { pfd ->
                    FileOutputStream(pfd.fileDescriptor).use { fos ->
                        fos.write(backupJson.toString(2).toByteArray())
                    }
                }
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DashboardActivity, "Backup created successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DashboardActivity, "Backup failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun restoreBackup(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val jsonString = contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        val stringBuilder = StringBuilder()
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            stringBuilder.append(line)
                        }
                        stringBuilder.toString()
                    }
                } ?: throw Exception("Could not read backup file")
                
                val backupJson = JSONObject(jsonString)
                
                // Clear existing data
                val appDatabase = AppDataSource.getInstance(applicationContext).database
                appDatabase.clearAllTables()
                
                // Restore folders first (for foreign key constraints)
                val foldersArray = backupJson.getJSONArray("folders")
                for (i in 0 until foldersArray.length()) {
                    val folderJson = foldersArray.getJSONObject(i)
                    folderViewModel.insertFolder(
                        name = folderJson.getString("name"),
                        description = folderJson.getString("description"),
                        color = folderJson.getString("color")
                    )
                }
                
                // Need to wait a bit for folders to be inserted
                withContext(Dispatchers.Main) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                // Restore notes
                                val notesArray = backupJson.getJSONArray("notes")
                                for (i in 0 until notesArray.length()) {
                                    val noteJson = notesArray.getJSONObject(i)
                                    val folderId = if (noteJson.has("folderId") && !noteJson.isNull("folderId")) 
                                        noteJson.getInt("folderId") else null
                                    val tags = if (noteJson.has("tags")) noteJson.getString("tags") else ""
                                    
                                    noteViewModel.insertNote(
                                        noteJson.getString("title"),
                                        noteJson.getString("description"),
                                        folderId,
                                        tags
                                    )
                                }
                                
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@DashboardActivity, "Backup restored successfully", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@DashboardActivity, "Restore failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }, 1000) // Give a second for folders to be inserted
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DashboardActivity, "Restore failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun logout() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                // Clear login status
                sharedPreferences.edit()
                    .putBoolean("is_logged_in", false)
                    .apply()
                
                // Navigate to login screen
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }
}

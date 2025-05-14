package com.example.notesitory

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import yuku.ambilwarna.AmbilWarnaDialog
import androidx.appcompat.widget.PopupMenu

class FolderFragment : Fragment() {
    
    private lateinit var folderViewModel: FolderViewModel
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var adapter: FolderAdapter
    private lateinit var recyclerView: RecyclerView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_folder, container, false)
        
        folderViewModel = ViewModelProvider(requireActivity())[FolderViewModel::class.java]
        noteViewModel = ViewModelProvider(requireActivity())[NoteViewModel::class.java]

        recyclerView = view.findViewById(R.id.folder_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        
        adapter = FolderAdapter(
            onFolderClickListener = { folder ->
                // Navigate to folder contents
                val bundle = Bundle().apply {
                    putInt("folderId", folder.id)
                    putString("folderName", folder.name)
                }
                
                // You can decide which fragment to navigate to based on folder type
                // or create a new FolderContentsFragment that displays both notes and todos
                // For now, we'll navigate to NoteFragment with folder info
                val noteFragment = NoteFragment().apply {
                    arguments = bundle
                }
                
                parentFragmentManager.beginTransaction()
                    .replace(R.id.flFragment, noteFragment)
                    .addToBackStack(null)
                    .commit()
            },
            onFolderLongClickListener = { folder, view ->
                showFolderOptions(folder, view)
                true
            }
        )
        
        recyclerView.adapter = adapter
        
        // Set up the FAB for adding new folders
        val fab: FloatingActionButton = view.findViewById(R.id.add_folder_fab)
        fab.setOnClickListener {
            showAddFolderDialog()
        }
        
        // Observe folders from the ViewModel
        folderViewModel.allFolders.observe(viewLifecycleOwner) { folders ->
            adapter.submitList(folders)
        }
        
        return view
    }
    
    fun showAddFolderDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_new_folder, null)
        
        val nameEditText = dialogView.findViewById<EditText>(R.id.folder_name_edit)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.folder_description_edit)
        val colorPicker = dialogView.findViewById<View>(R.id.color_picker)
        
        var selectedColor = "#4CAF50" // Default green color
        
        colorPicker.setOnClickListener {
            val colorPickerDialog = AmbilWarnaDialog(
                requireContext(),
                android.graphics.Color.parseColor(selectedColor),
                object : AmbilWarnaDialog.OnAmbilWarnaListener {
                    override fun onCancel(dialog: AmbilWarnaDialog?) {}
                    
                    override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                        selectedColor = String.format("#%06X", (0xFFFFFF and color))
                        colorPicker.setBackgroundColor(color)
                    }
                }
            )
            colorPickerDialog.show()
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Create New Folder")
            .setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val name = nameEditText.text.toString().trim()
                val description = descriptionEditText.text.toString().trim()
                
                if (name.isNotEmpty()) {
                    folderViewModel.insertFolder(name, description, selectedColor)
                    Toast.makeText(requireContext(), "Folder created", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Folder name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showFolderOptions(folder: Folder, view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.folder_options_menu, popup.menu)
        
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.edit_folder -> {
                    showEditFolderDialog(folder)
                    true
                }
                R.id.delete_folder -> {
                    showDeleteFolderConfirmation(folder)
                    true
                }
                else -> false
            }
        }
        
        popup.show()
    }
    
    private fun showEditFolderDialog(folder: Folder) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_new_folder, null)
        
        val nameEditText = dialogView.findViewById<EditText>(R.id.folder_name_edit)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.folder_description_edit)
        val colorPicker = dialogView.findViewById<View>(R.id.color_picker)
        
        nameEditText.setText(folder.name)
        descriptionEditText.setText(folder.description)
        
        var selectedColor = folder.color
        try {
            colorPicker.setBackgroundColor(android.graphics.Color.parseColor(selectedColor))
        } catch (e: Exception) {
            selectedColor = "#FFFFFF"
            colorPicker.setBackgroundColor(android.graphics.Color.WHITE)
        }
        
        colorPicker.setOnClickListener {
            val colorPickerDialog = AmbilWarnaDialog(
                requireContext(),
                android.graphics.Color.parseColor(selectedColor),
                object : AmbilWarnaDialog.OnAmbilWarnaListener {
                    override fun onCancel(dialog: AmbilWarnaDialog?) {}
                    
                    override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                        selectedColor = String.format("#%06X", (0xFFFFFF and color))
                        colorPicker.setBackgroundColor(color)
                    }
                }
            )
            colorPickerDialog.show()
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Edit Folder")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = nameEditText.text.toString().trim()
                val description = descriptionEditText.text.toString().trim()
                
                if (name.isNotEmpty()) {
                    val updatedFolder = folder.copy(
                        name = name,
                        description = description,
                        color = selectedColor
                    )
                    folderViewModel.updateFolder(updatedFolder)
                    Toast.makeText(requireContext(), "Folder updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Folder name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showDeleteFolderConfirmation(folder: Folder) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Folder")
            .setMessage("Are you sure you want to delete '${folder.name}'? This will also delete all notes and tasks in this folder.")
            .setPositiveButton("Delete") { _, _ ->
                folderViewModel.deleteFolder(folder)
                Toast.makeText(requireContext(), "Folder deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
} 
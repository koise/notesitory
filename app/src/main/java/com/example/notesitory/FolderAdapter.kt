package com.example.notesitory

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class FolderAdapter(
    private val onFolderClickListener: (Folder) -> Unit,
    private val onFolderLongClickListener: (Folder, View) -> Boolean
) : ListAdapter<Folder, FolderAdapter.FolderViewHolder>(FolderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_folder, parent, false)
        return FolderViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = getItem(position)
        holder.bind(folder)
    }

    inner class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val folderName: TextView = itemView.findViewById(R.id.folder_name)
        private val folderDescription: TextView = itemView.findViewById(R.id.folder_description)
        private val folderDate: TextView = itemView.findViewById(R.id.folder_date)
        private val folderCard: CardView = itemView.findViewById(R.id.folder_card)
        private val itemCount: TextView = itemView.findViewById(R.id.item_count)

        fun bind(folder: Folder) {
            folderName.text = folder.name
            folderDescription.text = folder.description
            folderDate.text = folder.creationDate
            
            try {
                folderCard.setCardBackgroundColor(Color.parseColor(folder.color))
            } catch (e: Exception) {
                // Use default color if parsing fails
                folderCard.setCardBackgroundColor(Color.WHITE)
            }
            
            // Set item count dynamically if provided
            if (itemView.tag != null) {
                val count = itemView.tag as Int
                itemCount.text = count.toString()
                itemCount.visibility = View.VISIBLE
            } else {
                itemCount.visibility = View.GONE
            }

            itemView.setOnClickListener {
                onFolderClickListener(folder)
            }

            itemView.setOnLongClickListener { view ->
                onFolderLongClickListener(folder, view)
            }
        }
    }

    class FolderDiffCallback : DiffUtil.ItemCallback<Folder>() {
        override fun areItemsTheSame(oldItem: Folder, newItem: Folder): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Folder, newItem: Folder): Boolean {
            return oldItem == newItem
        }
    }
} 
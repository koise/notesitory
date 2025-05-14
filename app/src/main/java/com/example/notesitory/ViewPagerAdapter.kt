package com.example.notesitory

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    
    override fun getItemCount(): Int = 3
    
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> NoteFragment()
            1 -> FolderFragment()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
} 
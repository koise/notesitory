package com.example.notesitory

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddTodoDialogFragment : BottomSheetDialogFragment() {

    private lateinit var todoTitleEditText: EditText
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private var listener: OnTodoAddedListener? = null

    interface OnTodoAddedListener {
        fun onTodoAdded(todoText: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnTodoAddedListener) {
            listener = context
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext())
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_add_todo, null)
        dialog.setContentView(view)

        todoTitleEditText = view.findViewById(R.id.todoTitleEditText)
        btnSave = view.findViewById(R.id.btnSave)
        btnCancel = view.findViewById(R.id.btnCancel)

        btnSave.setOnClickListener {
            val todoText = todoTitleEditText.text.toString().trim()
            if (todoText.isNotEmpty()) {
                listener?.onTodoAdded(todoText)
                dismiss()
            }
        }

        btnCancel.setOnClickListener {
            dismiss()
        }

        return dialog
    }
}

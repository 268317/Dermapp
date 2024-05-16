package com.example.dermapp

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class ConfirmationDialogFragment : DialogFragment() {

    interface ConfirmationDialogListener {
        fun onConfirmButtonClicked(password: String)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.activity_confirmation_dialog, null)

        val passwordEditText = view.findViewById<EditText>(R.id.passwordEditText)

        builder.setView(view)
            .setPositiveButton("Confirm") { _, _ ->
                val password = passwordEditText.text.toString()
                val listener = activity as ConfirmationDialogListener?
                listener?.onConfirmButtonClicked(password)
            }
            .setNegativeButton("Cancel") { _, _ ->
                dialog?.cancel()
            }

        return builder.create()
    }
}
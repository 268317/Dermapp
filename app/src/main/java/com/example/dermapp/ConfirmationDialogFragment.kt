package com.example.dermapp

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

/**
 * A DialogFragment for displaying a confirmation dialog that requires a password.
 */
class ConfirmationDialogFragment : DialogFragment() {

    /**
     * Interface for handling confirmation button clicks.
     */
    interface ConfirmationDialogListener {
        /**
         * Called when the confirm button is clicked.
         * @param password The entered password.
         * @param dialog The dialog fragment.
         */
        fun onConfirmButtonClicked(password: String, dialog: DialogFragment)
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
                listener?.onConfirmButtonClicked(password, this)
            }
            .setNegativeButton("Cancel") { _, _ ->
                dialog?.cancel()
            }

        return builder.create()
    }
}
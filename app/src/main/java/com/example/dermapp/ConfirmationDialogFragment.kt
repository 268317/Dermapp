package com.example.dermapp

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

/**
 * A DialogFragment for displaying a confirmation dialog that requires a password.
 * This dialog is used to confirm certain actions that need a password for verification.
 */
class ConfirmationDialogFragment : DialogFragment() {

    /**
     * Interface for handling confirmation button clicks.
     * The implementing activity must define the behavior when the confirmation button is clicked.
     */
    interface ConfirmationDialogListener {
        /**
         * Called when the confirm button is clicked.
         * @param password The entered password.
         * @param dialog The dialog fragment.
         */
        fun onConfirmButtonClicked(password: String, dialog: DialogFragment)
    }

    /**
     * Creates and returns the dialog for password confirmation.
     * The dialog displays an input field for the password and two buttons: confirm and cancel.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.activity_confirmation_dialog, null)

        // Initialize the EditText for password input
        val passwordEditText = view.findViewById<EditText>(R.id.passwordEditText)

        // Set up the dialog builder
        builder.setView(view)
            .setPositiveButton("Confirm") { _, _ ->
                // When the confirm button is clicked, get the entered password and notify the listener
                val password = passwordEditText.text.toString()
                val listener = activity as ConfirmationDialogListener?
                listener?.onConfirmButtonClicked(password, this)
            }
            .setNegativeButton("Cancel") { _, _ ->
                // When the cancel button is clicked, dismiss the dialog
                dialog?.cancel()
            }

        return builder.create()
    }
}

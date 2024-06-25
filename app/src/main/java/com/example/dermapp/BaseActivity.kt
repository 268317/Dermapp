package com.example.dermapp

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

/**
 * Base class for all activities in the application.
 * Contains a method to display a Snackbar with a message.
 */
open class BaseActivity : AppCompatActivity() {

    /**
     * Displays a Snackbar with the specified message.
     * @param message The message to be displayed in the Snackbar.
     * @param errorMessage A flag indicating whether the message is an error (true) or a success (false).
     */
    fun showErrorSnackBar(message: String, errorMessage: Boolean) {
        val snackbar =
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        val snackbarView = snackbar.view

        // Set the color of the Snackbar based on the type of message
        if (errorMessage) {
            snackbarView.setBackgroundColor(
                ContextCompat.getColor(this@BaseActivity,
                    R.color.colorSnackBarError
                )
            )
        } else {
            snackbarView.setBackgroundColor(
                ContextCompat.getColor(this@BaseActivity,
                    R.color.colorSnackBarSuccess
                )
            )
        }
        snackbar.show()
    }

}
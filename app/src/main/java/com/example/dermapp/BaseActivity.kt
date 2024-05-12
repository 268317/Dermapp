package com.example.dermapp

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

/**
 * Klasa bazowa dla wszystkich aktywności w aplikacji.
 * Zawiera metodę do wyświetlania paska Snackbar z komunikatem.
 */
open class BaseActivity : AppCompatActivity() {

    /**
     * Wyświetla pasek Snackbar z określonym komunikatem.
     * @param message Wiadomość do wyświetlenia w pasku Snackbar.
     * @param errorMessage Flaga określająca, czy komunikat jest błędem (true) lub sukcesem (false).
     */
    fun showErrorSnackBar(message: String, errorMessage: Boolean){
        val snackbar =
            Snackbar.make(findViewById(android.R.id.content),message,Snackbar.LENGTH_LONG)
        val snackbarView = snackbar.view

        // Ustawienie koloru paska Snackbar na podstawie typu komunikatu
        if (errorMessage) {
            snackbarView.setBackgroundColor(
                ContextCompat.getColor(this@BaseActivity,
                    R.color.colorSnackBarError
                )
            )
        }else{
            snackbarView.setBackgroundColor(
                ContextCompat.getColor(this@BaseActivity,
                    R.color.colorSnackBarSuccess
                )
            )
        }
        snackbar.show()
    }

}
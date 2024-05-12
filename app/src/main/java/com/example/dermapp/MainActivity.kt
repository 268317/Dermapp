package com.example.dermapp
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth


/**
 * Aktywność obsługująca logowanie użytkownika za pomocą Firebase Authentication.
 */
class MainActivity : BaseActivity() {

    private var inputEmail: EditText? = null
    private var inputPassword: EditText? = null
    private var loginButton: Button? = null
    private var signUpButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicjalizacja pól wejściowych i przycisku logowania
        inputEmail = findViewById(R.id.editTextEmailAddress)
        inputPassword = findViewById(R.id.editTextPassword)
        loginButton = findViewById(R.id.LogInButton)
        signUpButton = findViewById(R.id.SignUpButton)

        // Ustawienie nasłuchiwania kliknięć przycisku logowania
        loginButton?.setOnClickListener{
            logInRegisteredUser()
        }

        signUpButton?.setOnClickListener{
            goToSignIn()
        }

    }


    /**
     * Metoda walidująca wprowadzone dane logowania.
     * @return True, jeśli dane są poprawne, w przeciwnym razie False.
     */
    private fun validateLoginDetails(): Boolean {

        return when{
            TextUtils.isEmpty(inputEmail?.text.toString().trim{ it <= ' '}) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }

            TextUtils.isEmpty(inputPassword?.text.toString().trim{ it <= ' '}) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password),true)
                false
            }

            else -> {
                showErrorSnackBar("Your details are valid",false)
                true
            }
        }


    }

    /**
     * Metoda logowania zarejestrowanego użytkownika za pomocą Firebase Authentication.
     */
    private fun logInRegisteredUser(){


        if(validateLoginDetails()){
            val email = inputEmail?.text.toString().trim { it<= ' '}
            val password = inputPassword?.text.toString().trim { it<= ' '}

            // Logowanie za pomocą FirebaseAuth
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                .addOnCompleteListener{task ->
                    if(task.isSuccessful){
                        showErrorSnackBar("You are logged in successfully.", false)
                        goToNextActivity()
                        finish()

                    } else{
                        showErrorSnackBar(task.exception!!.message.toString(),true)
                    }
                }
        }
    }

    /**
     * Metoda przechodzenia do głównej aktywności po pomyślnym zalogowaniu i przekazanie uid do głównej aktywności.
     */
    private fun goToNextActivity() {

        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.email.toString()

        val intent = Intent(this, AppointmentDetailsPatActivity::class.java)
        intent.putExtra("uID",uid)
        startActivity(intent)
    }

    private fun goToSignIn() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
   }

}
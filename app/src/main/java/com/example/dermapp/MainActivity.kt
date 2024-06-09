package com.example.dermapp
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import com.example.dermapp.messages.MessagesPatActivity
import com.example.dermapp.startPatient.StartPatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


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
        val uid = user?.uid ?:""
        if (user != null) {
            FirebaseFirestore.getInstance().collection("patients").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val intent = Intent(this, StartPatActivity::class.java)
                        intent.putExtra("uID", uid)
                        startActivity(intent)
                    } else {
                        FirebaseFirestore.getInstance().collection("doctors").document(uid)
                            .get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val intent = Intent(this, StartDocActivity::class.java)
                                    intent.putExtra("uID", uid)
                                    startActivity(intent)
                                } else {
                                    val intent = Intent(this, MessagesPatActivity::class.java)
                                    intent.putExtra("uID", uid)
                                    startActivity(intent)
                                }
                            }
                    }
                }
        }

    }

    private fun goToSignIn() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
   }

}
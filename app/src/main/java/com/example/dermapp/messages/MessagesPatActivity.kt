package com.example.dermapp.messages

import com.example.dermapp.database.Doctor
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R
import com.example.dermapp.startPatient.StartPatActivity

class MessagesPatActivity : AppCompatActivity() {
    private lateinit var backButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages_pat)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewMessagesPat)
        val items: MutableList<Doctor> = ArrayList()

        items.add(
            Doctor(
                email = "doctor1@gmail.com",
                firstName = "Doctor",
                lastName = "Doctor 1",
                address = "Warsaw",
                phone = "123456789",
                doctorId = "1234"
            )
        )

        items.add(
            Doctor(
                email = "doctor2@gmail.com",
                firstName = "Doctor",
                lastName = "Doctor 2",
                address = "Berlin",
                phone = "123456789",
                doctorId = "1234"
            )
        )

        items.add(
            Doctor(
                email = "doctor3@gmail.com",
                firstName = "Doctor",
                lastName = "Doctor 3",
                address = "Warsaw",
                phone = "123456789",
                doctorId = "1234"
            )
        )

        val header = findViewById<LinearLayout>(R.id.backHeaderPat)
        backButton = header.findViewById(R.id.arrowButton)

        backButton.setOnClickListener {
            val intent = Intent(this, StartPatActivity::class.java)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = MyAdapterMessagesPat(this, items)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.recyclerViewMessagesPat)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setDoctorEntryClickListener(imageViewId: Int, textViewId: Int, doctorName: String) {
        val imageView = findViewById<ImageView>(imageViewId)
        val textView = findViewById<TextView>(textViewId)

        imageView.setOnClickListener {
            showToast("Clicked on $doctorName's image")
        }

        textView.setOnClickListener {
            showToast("Clicked on $doctorName's name")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

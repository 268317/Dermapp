package com.example.dermapp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import java.util.Calendar

class StartPatActivity : AppCompatActivity(){
    private lateinit var textDate: AutoCompleteTextView
    private lateinit var autoDoc: AutoCompleteTextView
    private lateinit var autoLoc: AutoCompleteTextView
    private lateinit var bookButton: Button
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var menuButton: ImageButton
    private lateinit var navView: NavigationView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_start_patient)

        textDate = findViewById(R.id.autoCompleteTextDate)
        autoDoc = findViewById(R.id.autoCompleteTextViewDoctor)
        autoLoc = findViewById(R.id.autoCompleteTextViewLocalization)
        bookButton = findViewById(R.id.bookButton)
        drawerLayout = findViewById(R.id.drawer_layout)

        val header = findViewById<LinearLayout>(R.id.include)
        menuButton = header.findViewById(R.id.menuButton)

        menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        textDate.setOnClickListener{
            openCalendar()
        }

        val docOptions = arrayOf("Doc 1", "Doc 2", "Doc 3", "Doc 4") // Chwilowe opcje doc
        val locOptions = arrayOf("Loc 1", "Loc 2", "Loc 3", "Loc 4") // Chwilowe opcje loc

        val docAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, docOptions)
        autoDoc.setAdapter(docAdapter)

        val locAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, locOptions)
        autoLoc.setAdapter(locAdapter)

        bookButton.setOnClickListener{
            bookAppointment()
        }

        navView = findViewById(R.id.nav_view)

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_make -> {
                    val intent = Intent(this, MakeAppointmentPatActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_cancel -> {
                    true
                }
                R.id.nav_myAppointments -> {
                    // Obsługa kliknięcia na "My appointments"
                    Toast.makeText(this, "My appointments clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_newReport -> {
                    val intent = Intent(this, CreateNewReportActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_myRecords -> {
                    Toast.makeText(this, "My medical records clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_myReports -> {
                    Toast.makeText(this, "My reports clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_myPrescriptions -> {
                    Toast.makeText(this, "My prescriptions clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_myMailbox -> {
                    val intent = Intent(this, MessagesPatActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun openCalendar() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDay = String.format("%02d", selectedDay)
            val formattedMonth = String.format("%02d", selectedMonth + 1)
            textDate.setText("$formattedDay-$formattedMonth-$selectedYear")
        }, year, month, dayOfMonth)

        datePickerDialog.show()
    }

    private fun bookAppointment() {
        val doctor = autoDoc.text.toString()
        val location = autoLoc.text.toString()
        val date = textDate.text.toString()

    }
}
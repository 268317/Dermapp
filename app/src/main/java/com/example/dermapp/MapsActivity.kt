package com.example.dermapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import android.location.Geocoder
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var destinationAddress: String? = null
    private var doctorLocation: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Pobierz adres przekazany z poprzedniej aktywności
        destinationAddress = intent.getStringExtra("address")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        getCurrentLocationAndDrawRoute()
    }

    private fun getCurrentLocationAndDrawRoute() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener(this) { location: Location? ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))

                    getDoctorLocation(destinationAddress ?: "")
                } else {
                    Toast.makeText(this, "Nie można uzyskać bieżącej lokalizacji.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun getDoctorLocation(address: String) {
        val geocoder = Geocoder(this, Locale.getDefault())
        Thread {
            try {
                val addresses = geocoder.getFromLocationName(address, 1)
                Log.d("addresses", addresses.toString())
                if (addresses != null && addresses.isNotEmpty()) {
                    val doctorLatLng = LatLng(addresses[0].latitude, addresses[0].longitude)
                    doctorLocation = doctorLatLng
                    Log.d("doctorLocation", doctorLocation.toString())

                    runOnUiThread {
                        mMap.addMarker(MarkerOptions().position(doctorLatLng).title("Appointment location"))
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(doctorLatLng, 15f))
                    }

                    val url = getDirectionsUrl(doctorLatLng)
                    FetchUrl().execute(url)
                } else {
                    Log.e("MapsActivity", "Address not found: $address")
                }
            } catch (e: Exception) {
                Log.e("MapsActivity", "Error during geocoding: ${e.message}")
            }
        }.start()
    }

    fun getApiKey(): String {
        try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            val bundle = applicationInfo.metaData
            return bundle.getString("com.google.android.geo.API_KEY") ?: throw IllegalStateException("API Key not found.")
        } catch (e: PackageManager.NameNotFoundException) {
            throw IllegalStateException("Application info not found", e)
        }
    }
    private fun getDirectionsUrl(destination: LatLng): String {
        val apiKey: String = getApiKey()
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${mMap.cameraPosition.target.latitude},${mMap.cameraPosition.target.longitude}&destination=${destination.latitude},${destination.longitude}&key=$apiKey"
    }

    private inner class FetchUrl : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg url: String?): String {
            var data = ""
            try {
                val iStream = URL(url[0]).openStream()
                val reader = BufferedReader(InputStreamReader(iStream))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                data = sb.toString()
                reader.close()
            } catch (e: Exception) {
                Log.d("Exception while downloading url", e.toString())
            }
            return data
        }
    }

}
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
import android.widget.ImageButton
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Polyline
import org.json.JSONObject
import java.util.*
import com.google.android.gms.maps.model.PolylineOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var destinationAddress: String? = null
    private var doctorLocation: LatLng? = null
    private var currentPolyline: Polyline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Get the address passed from the previous activity
        destinationAddress = intent.getStringExtra("address")

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val btnGetDirections: ImageButton = findViewById(R.id.btnGetDirections)
        btnGetDirections.setOnClickListener {
            doctorLocation?.let {
                drawRoute(it)
            } ?: Toast.makeText(this, "Doctor's localisation not found", Toast.LENGTH_SHORT).show()
        }

        val btnFindPharmacies: ImageButton = findViewById(R.id.btnFindPharmacies)
        btnFindPharmacies.setOnClickListener {
            findNearbyPharmacies() // Ustaw akcję dla naciśnięcia przycisku
        }

        // Set up the map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Enable the My Location layer on the map
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true // Enable My Location layer
            getCurrentLocationAndDrawRoute() // Get the current location and draw the route
        }

        mMap.uiSettings.isMapToolbarEnabled = false
        getCurrentLocationAndDrawRoute()

        // Ustaw listener na markery
        mMap.setOnMarkerClickListener { marker ->
            if (marker.title != null && marker.title != "Doktor") {
                // Jeśli kliknięto aptekę, narysuj trasę do niej
                drawRoute(marker.position)
                true // Zwracamy true, aby zaznaczyć, że zdarzenie zostało obsłużone
            } else {
                false // Pozwól na domyślne zachowanie, jeśli kliknięto doktora
            }
        }
    }

    private fun getCurrentLocationAndDrawRoute() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        // Get the last known location
        fusedLocationClient.lastLocation
            .addOnSuccessListener(this) { location: Location? ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                    getDoctorLocation(destinationAddress ?: "") // Get doctor location if available
                } else {
                    Toast.makeText(this, "Unable to get current location.", Toast.LENGTH_SHORT).show()
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
                        // Dodaj marker dla lekarza
                        mMap.addMarker(MarkerOptions().position(doctorLatLng).title("Doktor"))
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(doctorLatLng, 15f))
                    }

                } else {
                    Log.e("MapsActivity", "Address not found: $address")
                }
            } catch (e: Exception) {
                Log.e("MapsActivity", "Error during geocoding: ${e.message}")
            }
        }.start()
    }

    private fun drawRoute(destination: LatLng) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        // Get the current location
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location: Location? ->
            if (location != null) {
                val origin = LatLng(location.latitude, location.longitude)
                val url = getDirectionsUrl(origin, destination)
                FetchUrl().execute(url)
            } else {
                Toast.makeText(this, "Unable to get current location.", Toast.LENGTH_SHORT).show()
            }
        }
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

    private fun findNearbyPharmacies() {
        // Upewnij się, że doctorLocation jest ustawione
        doctorLocation?.let { location ->
            val url = getNearbyPharmaciesUrl(location)
            FetchPharmacies().execute(url)
        } ?: Toast.makeText(this, "Nie znaleziono lokalizacji lekarza", Toast.LENGTH_SHORT).show()
    }

    private fun getNearbyPharmaciesUrl(location: LatLng): String {
        val apiKey: String = getApiKey() // Użyj swojego klucza API
        return "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=${location.latitude},${location.longitude}&radius=5000&type=pharmacy&key=$apiKey"
    }

    private inner class FetchPharmacies : AsyncTask<String, Void, String>() {
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

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            parsePharmaciesResult(result) // Przetwórz odpowiedź
        }
    }

    private fun parsePharmaciesResult(jsonData: String) {
        try {
            val jsonObject = JSONObject(jsonData)
            val resultsArray = jsonObject.getJSONArray("results")

            // Zresetowanie poprzednich markerów
            mMap.clear()
            // Dodaj marker dla lekarza z powrotem
            doctorLocation?.let {
                mMap.addMarker(MarkerOptions().position(it).title("Doktor"))
            }

            // Dodanie nowych markerów dla najbliższych aptek
            for (i in 0 until resultsArray.length()) {
                val pharmacy = resultsArray.getJSONObject(i)
                val name = pharmacy.getString("name")
                val location = pharmacy.getJSONObject("geometry").getJSONObject("location")
                val lat = location.getDouble("lat")
                val lng = location.getDouble("lng")
                val pharmacyLatLng = LatLng(lat, lng)

                mMap.addMarker(MarkerOptions()
                    .position(pharmacyLatLng)
                    .title(name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))
            }
        } catch (e: Exception) {
            Log.e("MapsActivity", "Błąd podczas przetwarzania odpowiedzi: ${e.message}")
        }
    }

    private fun getDirectionsUrl(origin: LatLng, destination: LatLng): String {
        val apiKey: String = getApiKey()
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${destination.latitude},${destination.longitude}&key=$apiKey"
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

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            drawPolyline(result) // Draw the polyline on the map
        }
    }

    private fun drawPolyline(jsonData: String?) {
        try {
            val jsonObject = JSONObject(jsonData)
            val routes = jsonObject.getJSONArray("routes")
            val points = ArrayList<LatLng>()

            // Check if routes are available
            if (routes.length() > 0) {
                val legs = routes.getJSONObject(0).getJSONArray("legs")
                val steps = legs.getJSONObject(0).getJSONArray("steps")

                for (i in 0 until steps.length()) {
                    val polyline = steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                    points.addAll(decodePoly(polyline))
                }

                // Usuwanie poprzedniej polilinii, jeśli istnieje
                currentPolyline?.remove()

                // Rysowanie nowej polilinii na mapie
                val lineOptions = PolylineOptions()
                lineOptions.addAll(points)
                lineOptions.width(10f)
                lineOptions.color(Color.BLUE)
                currentPolyline = mMap.addPolyline(lineOptions) // Przechowywanie referencji do aktualnej polilinii
            }
        } catch (e: Exception) {
            Log.e("MapsActivity", "Error parsing directions JSON: ${e.message}")
        }
    }

    private fun decodePoly(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) {
                -(result shr 1)
            } else {
                result shr 1
            }
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) {
                -(result shr 1)
            } else {
                result shr 1
            }
            lng += dlng

            val p = LatLng((lat / 1E5), (lng / 1E5))
            poly.add(p)
        }

        return poly
    }
}
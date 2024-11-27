package com.example.dermapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
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
import android.location.LocationManager
import android.net.Uri
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.example.dermapp.startPatient.StartPatActivity
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Polyline
import org.json.JSONObject
import java.util.*
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var destinationAddress: String? = null
    private var doctorLocation: LatLng? = null
    private var currentPolyline: Polyline? = null
    private lateinit var backButton: ImageButton
    private var selectedLocation: LatLng? = null
    private var selectedDistance: Int = 1000
    private lateinit var placesClient: PlacesClient
    private lateinit var autocompleteSessionToken: AutocompleteSessionToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Get the address passed from the previous activity
        destinationAddress = intent.getStringExtra("address")

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val header = findViewById<LinearLayout>(R.id.backHeader)
        backButton = header.findViewById(R.id.arrowButton)

        backButton.setOnClickListener {
            val intent = Intent(this, StartPatActivity::class.java)
            startActivity(intent)
        }


        val btnGetDirections: ImageButton = findViewById(R.id.btnGetDirections)
        btnGetDirections.setOnClickListener {
            selectedLocation?.let { location ->
                // Tworzymy URI dla Google Maps z lokalizacją klikniętego markera
                val gmmIntentUri = Uri.parse("google.navigation:q=${location.latitude},${location.longitude}")

                // Tworzymy intencję
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps") // Upewniamy się, że otworzymy Google Maps

                // Sprawdzamy, czy aplikacja Google Maps jest zainstalowana
                if (mapIntent.resolveActivity(packageManager) != null) {
                    startActivity(mapIntent)
                } else {
                    Toast.makeText(this, "Google Maps not installed", Toast.LENGTH_SHORT).show()
                }
            } ?: Toast.makeText(this, "Location not selected", Toast.LENGTH_SHORT).show()
        }

        val btnFindPharmacies: ImageButton = findViewById(R.id.btnFindPharmacies)
        btnFindPharmacies.setOnClickListener {
            findNearbyPharmacies()
        }

        findViewById<ImageButton>(R.id.btnFindDoctor).setOnClickListener {
            if (destinationAddress != null) {
                getDoctorLocation(destinationAddress ?: "") // Get doctor location if available
            } else {
                Toast.makeText(this, "Unable to get current location.", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up the map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val seekBar: SeekBar = findViewById(R.id.distanceSeekBar) // Twoje ID suwaka
        val distanceText: TextView = findViewById(R.id.distanceLabel) // Tekst do wyświetlenia odległości

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                selectedDistance = (progress + 1) * 1000
                distanceText.text = "${progress + 1} km"
                findNearbyPharmacies()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        if (!Places.isInitialized()) {
            val apiKey: String = getApiKey()
            Places.initialize(applicationContext, apiKey)
        }
        placesClient = Places.createClient(this)
        autocompleteSessionToken = AutocompleteSessionToken.newInstance()

        val btnMyLocation: ImageButton = findViewById(R.id.btnMyLocation)

        btnMyLocation.setOnClickListener {
            if (::mMap.isInitialized) {
                // Sprawdź, czy mamy uprawnienia do lokalizacji
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    mMap.isMyLocationEnabled = false
                    val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

                    location?.let {
                        val myLatLng = LatLng(location.latitude, location.longitude)
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 15f))
                    } ?: Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isMapToolbarEnabled = true

        // Enable the My Location layer on the map
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = false
            getCurrentLocationAndDrawRoute() // Get the current location and draw the route
        }

        getCurrentLocationAndDrawRoute()

        mMap.uiSettings.isMapToolbarEnabled = true

        mMap.setOnMarkerClickListener { marker ->
            if (marker.title != null) {
                marker.showInfoWindow()
                selectedLocation = marker.position
                drawRoute(marker.position)
                true
            } else {
                false
            }
        }

        setupSearchBar()

    }

    private fun getCurrentLocationAndDrawRoute() {
        mMap.uiSettings.isMapToolbarEnabled = true
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
                if (!addresses.isNullOrEmpty()) {
                    val doctorLatLng = LatLng(addresses[0].latitude, addresses[0].longitude)
                    doctorLocation = doctorLatLng
                    Log.d("doctorLocation", doctorLocation.toString())

                    runOnUiThread {
                        mMap.addMarker(MarkerOptions().position(doctorLatLng).title("Appointment localisation").snippet(address))
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
        doctorLocation?.let { location ->
            val url = getNearbyPharmaciesUrl(location)
            FetchPharmacies().execute(url)
        } ?: Toast.makeText(this, "Unable to get appointment location.", Toast.LENGTH_SHORT).show()
    }

    private fun getNearbyPharmaciesUrl(location: LatLng): String {
        val apiKey: String = getApiKey()
        return "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=${location.latitude},${location.longitude}&radius=${selectedDistance}&type=pharmacy&key=$apiKey"
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
            parsePharmaciesResult(result)
        }
    }

    private fun getPharmacyIcon(): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(this, R.mipmap.ic_pharmacies) ?: return BitmapDescriptorFactory.defaultMarker()

        val scale = 0.65f
        val width = (vectorDrawable.intrinsicWidth * scale).toInt()
        val height = (vectorDrawable.intrinsicHeight * scale).toInt()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, width, height)
        vectorDrawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun parsePharmaciesResult(jsonData: String) {
        try {
            val jsonObject = JSONObject(jsonData)
            val resultsArray = jsonObject.getJSONArray("results")

            mMap.clear()
            doctorLocation?.let {
                mMap.addMarker(MarkerOptions().position(it).title("Appointment localisation"))
            }

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
                    .icon(getPharmacyIcon()))
            }
        } catch (e: Exception) {
            Log.e("MapsActivity", "Error during processing the response: ${e.message}")
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

    private fun setupSearchBar() {
        val searchBar: EditText = findViewById(R.id.searchBar)

        searchBar.addTextChangedListener { text ->
            val query = text.toString()
            if (query.isNotEmpty()) {
                fetchAutocompleteSuggestions(query)
            }
        }
    }


    private fun searchForPlace(placeId: String) {
        val placeFields = listOf(Place.Field.LAT_LNG, Place.Field.NAME)
        val request = FetchPlaceRequest.builder(placeId, placeFields).build()

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val place = response.place
                val latLng = place.latLng

                if (latLng != null) {
                    mMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title(place.name)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                    )
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to fetch place details: $exception", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchAutocompleteSuggestions(query: String) {
        val listView: ListView = findViewById(R.id.searchResultsListView)
        val request = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(autocompleteSessionToken)
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
            val predictions = response.autocompletePredictions
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ArrayList<String>())
            val placeIds = mutableListOf<String>() // Lista do przechowywania placeId dla każdego wyniku
            for (prediction in predictions) {
                val placeName = prediction.getPrimaryText(null).toString()
                val placeId = prediction.placeId

                adapter.add(placeName) // Dodaj nazwę do adaptera
                placeIds.add(placeId)  // Dodaj placeId do listy
            }
            listView.adapter = adapter

            listView.setOnItemClickListener { parent, view, position, id ->
                val selectedPlaceId = placeIds[position]
                searchForPlace(selectedPlaceId)

                val emptyList = emptyList<String>()
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, emptyList)
                listView.adapter = adapter
            }
        }
    }
}
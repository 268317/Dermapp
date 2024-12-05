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


/**
 * This activity provides functionality for displaying a map with various interactive features.
 * Users can view their current location, search for places, find nearby pharmacies, and get directions.
 */
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    // Google Map object to interact with the map view
    private lateinit var mMap: GoogleMap

    // Client to fetch the user's location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Destination address passed from the previous activity
    private var destinationAddress: String? = null

    // Latitude and longitude of the doctor's location
    private var doctorLocation: LatLng? = null

    // Reference to the currently drawn polyline on the map
    private var currentPolyline: Polyline? = null

    // Button to navigate back to the previous activity
    private lateinit var backButton: ImageButton

    // User-selected marker location
    private var selectedLocation: LatLng? = null

    // Selected distance for pharmacy search, in meters
    private var selectedDistance: Int = 1000

    // Google Places client for place-related functionality
    private lateinit var placesClient: PlacesClient

    // Session token for autocomplete requests in Google Places API
    private lateinit var autocompleteSessionToken: AutocompleteSessionToken

    /**
     * Called when the activity is first created. Initializes the map and UI elements.
     * @param savedInstanceState Contains the data most recently supplied if the activity is being reinitialized.
     */
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

    /**
     * Called when the map is ready for use. Configures map settings and features.
     * @param googleMap The GoogleMap object representing the map.
     */
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

    /**
     * Retrieves the user's current location and displays a route to the destination on the map.
     */
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

    /**
     * Geocodes the provided address and sets the doctor's location on the map.
     * @param address The address of the doctor.
     */
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

    /**
     * Draws a route from the user's current location to the specified destination.
     * @param destination The destination LatLng object.
     */
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

    /**
     * Retrieves the Google API key from the application's metadata.
     * @return The Google API key.
     */
    fun getApiKey(): String {
        try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            val bundle = applicationInfo.metaData
            return bundle.getString("com.google.android.geo.API_KEY") ?: throw IllegalStateException("API Key not found.")
        } catch (e: PackageManager.NameNotFoundException) {
            throw IllegalStateException("Application info not found", e)
        }
    }

    /**
     * Finds nearby pharmacies within the selected radius of the doctor's location.
     */
    private fun findNearbyPharmacies() {
        doctorLocation?.let { location ->
            val url = getNearbyPharmaciesUrl(location)
            FetchPharmacies().execute(url)
        } ?: Toast.makeText(this, "Unable to get appointment location.", Toast.LENGTH_SHORT).show()
    }

    /**
     * Builds the URL for searching nearby pharmacies using the Google Places API.
     * @param location The location to search around.
     * @return The URL for the API request.
     */
    private fun getNearbyPharmaciesUrl(location: LatLng): String {
        val apiKey: String = getApiKey()
        return "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=${location.latitude},${location.longitude}&radius=${selectedDistance}&type=pharmacy&key=$apiKey"
    }

    /**
     * AsyncTask to fetch pharmacy data in the background.
     */
    private inner class FetchPharmacies : AsyncTask<String, Void, String>() {

        /**
         * Downloads the data from the provided URL.
         * @param url The URL to fetch the data from.
         * @return The downloaded JSON string.
         */
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

        /**
         * Handles the result after fetching pharmacy data.
         * @param result The JSON result from the API request.
         */
        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            parsePharmaciesResult(result)
        }
    }

    /**
     * Generates the pharmacy icon to be used for markers on the map.
     * @return A BitmapDescriptor representing the pharmacy icon.
     */
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

    /**
     * Parses the pharmacy data and displays markers for pharmacies on the map.
     * @param jsonData The JSON response from the Google Places API.
     */
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

    /**
     * Builds the URL for directions between two locations using the Google Directions API.
     * @param origin The origin location as a LatLng.
     * @param destination The destination location as a LatLng.
     * @return The URL for the API request.
     */
    private fun getDirectionsUrl(origin: LatLng, destination: LatLng): String {
        val apiKey: String = getApiKey()
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${destination.latitude},${destination.longitude}&key=$apiKey"
    }

    /**
     * AsyncTask to fetch route data from the Directions API.
     */
    private inner class FetchUrl : AsyncTask<String, Void, String>() {

        /**
         * Downloads the directions data from the provided URL.
         * @param url The URL to fetch the directions from.
         * @return The downloaded JSON string.
         */
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

        /**
         * Handles the result after fetching route data.
         * @param result The JSON result from the Directions API.
         */
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            drawPolyline(result) // Draw the polyline on the map
        }
    }

    /**
     * Draws a polyline on the map based on the decoded route data.
     * @param jsonData The JSON data representing the route.
     */
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

    /**
     * Decodes a polyline encoded string into a list of LatLng points.
     * @param encoded The encoded polyline string.
     * @return A list of LatLng points.
     */
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

    /**
     * Sets up the search bar for place autocomplete functionality.
     */
    private fun setupSearchBar() {
        val searchBar: EditText = findViewById(R.id.searchBar)

        searchBar.addTextChangedListener { text ->
            val query = text.toString()
            if (query.isNotEmpty()) {
                fetchAutocompleteSuggestions(query)
            }
        }
    }

    /**
     * Fetches details of a place by its place ID and adds it as a marker on the map.
     * @param placeId The place ID to fetch details for.
     */
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

    /**
     * Fetches autocomplete suggestions for a given query using the Google Places API.
     * @param query The search query string.
     */
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
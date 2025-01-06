package com.example.renter_app_g04

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.renter_app_g04.databinding.ActivityMainBinding
import com.example.renter_app_g04.model.Property
import com.example.renter_app_g04.model.Renter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMainBinding

    private lateinit var googleMap: GoogleMap
    private var db = Firebase.firestore
    private lateinit var auth: FirebaseAuth
    private var newWatchedAddressId: String? = null
    private val watchList: MutableList<String> = mutableListOf()

    private var properties:MutableList<Property> = mutableListOf()

    private var propertiesToDisplay:MutableList<Property> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        getAllProperties()


        val mapFragment = binding.map.getFragment<SupportMapFragment>()
        mapFragment.getMapAsync(this)

        binding.btnRequestPermission.setOnClickListener {
            requestLocationPermission()
        }

        binding.btnSearch.setOnClickListener {
            search()
        }

        binding.btnAddToWatchList.setOnClickListener {
            addToWatchList()
        }

        if (!checkLocationPermission()) {
            requestLocationPermission()
        } else {
            binding.btnRequestPermission.isVisible = false
        }
    }

    override fun onResume() {
        super.onResume()
        if (auth.currentUser != null) {
            getProfileData()
        }
        getAllProperties()
        resetPropertyDetails()
    }

    private fun resetPropertyDetails() {
        binding.address.text = ""
        binding.price.text = ""
        binding.bedrooms.text = ""
        binding.btnAddToWatchList.isVisible = false
        binding.cvPhoto.isVisible = false
        binding.bedrooms.isVisible = false
        binding.price.isVisible = false
    }



    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isCompassEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = true

        val defaultLocation = LatLng(43.66, -79.44)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 11f))
    }

    @SuppressLint("SetTextI18n")
    private fun getAllProperties() {
        db.collection("landlordProperties")
            .get()
            .addOnSuccessListener {
                    results: QuerySnapshot ->

                properties.clear()
                propertiesToDisplay.clear()

                for (document: QueryDocumentSnapshot in results) {
                    val property: Property = document.toObject(Property::class.java)
                    Log.d("TESTING", "PROPERTY ID: ${property.id}")
                    properties.add(property)
                    propertiesToDisplay.add(property)

                    val newMarker = MarkerOptions()
                        .position(LatLng(property.latitude, property.longitude))
                        .title(property.propertyAddress)



                    googleMap.setOnMarkerClickListener { marker ->
                        val options: RequestOptions = RequestOptions()
                            .centerCrop()

                        val clickedProperty = properties.first { it.propertyAddress == marker.title }
                        Glide.with(binding.root).load(clickedProperty.imageUrl).apply(options).into(binding.ivPhoto);
                        binding.address.text = clickedProperty.propertyAddress
                        binding.price.text = "$${clickedProperty.monthlyRentalPrice.toInt()}"
                        binding.bedrooms.text = "Number of Bedrooms: ${clickedProperty.numberOfBedRooms}"
                        this.newWatchedAddressId = clickedProperty.id
                        binding.btnAddToWatchList.isVisible = true
                        binding.cvPhoto.isVisible = true
                        binding.bedrooms.isVisible = true
                        binding.price.isVisible = true

                        if (watchList.contains(clickedProperty.id)) {
                            Log.d("TESTING", "WATCHLIST CONTAINS PROPERTY")
                            binding.btnAddToWatchList.setImageResource(R.drawable.favourite_icon_inactive)
                        } else {
                            binding.btnAddToWatchList.setImageResource(R.drawable.favourite_icon)
                            Log.d("TESTING", "WATCHLIST DOES NOT CONTAIN PROPERTY: ${property.id}")
                            Log.d("TESTING", "WATCHLIST: ${watchList.toString()}")
                        }

                        true
                    }
                    if (!property.rented) {
                        googleMap.addMarker(newMarker)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("TESTING", "Error getting documents.", exception)
            }
    }


    private fun addToWatchList() {
        if (this.newWatchedAddressId != null && auth.currentUser != null) {
            val data: MutableMap<String, Any> = HashMap();
            if (watchList.contains(this.newWatchedAddressId)) {
                binding.btnAddToWatchList.setImageResource(R.drawable.favourite_icon)
                watchList.remove(newWatchedAddressId!!)
                data["watchList"] = watchList.toList()
                db.collection("renters")
                    .document(auth.currentUser!!.uid)
                    .set(data)
                    .addOnSuccessListener {
                        Snackbar.make(binding.root, "Property Removed from Watch List", Snackbar.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Snackbar.make(binding.root, "FAILED Removing TO WATCH LIST", Snackbar.LENGTH_SHORT).show()
                    }
            } else {
                binding.btnAddToWatchList.setImageResource(R.drawable.favourite_icon_inactive)
                watchList.add(newWatchedAddressId!!)
                data["watchList"] = watchList.toList()
                db.collection("renters")
                    .document(auth.currentUser!!.uid)
                    .set(data)
                    .addOnSuccessListener {
                        Snackbar.make(binding.root, "Property added to Watch List", Snackbar.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Snackbar.make(binding.root, "FAILED ADDING TO WATCH LIST", Snackbar.LENGTH_SHORT).show()
                    }
            }

        } else {
            Snackbar.make(binding.root, "Please select and address and login", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun getProfileData() {
        db.collection("renters")
            .document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener {
                    document: DocumentSnapshot ->
                val renterFromDB: Renter? = document.toObject(Renter::class.java)

                watchList.clear()

                if (renterFromDB == null) {
                    Snackbar.make(binding.root, "ERROR: This user has no profile", Snackbar.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                } else {
                    watchList.addAll(renterFromDB.watchList)
                }
            }.addOnFailureListener {
                    exception ->
                Log.w("TESTING", "Error getting documents.", exception)
            }
    }

    private fun search() {
        val searchValue = binding.etPrice.text.toString()
        if (searchValue.isEmpty()) {
            propertiesToDisplay = properties
        } else {
            propertiesToDisplay = properties.filter { it.monthlyRentalPrice <= searchValue.toInt() }.toMutableList()
        }

        googleMap.clear()

        for (property in propertiesToDisplay) {
            val newMarker = MarkerOptions()
                .position(LatLng(property.latitude, property.longitude))
                .title(property.propertyAddress)

            googleMap.addMarker(newMarker)
        }

    }


    private fun checkLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
        return fineLocation == PackageManager.PERMISSION_GRANTED
    }


    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)


        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                binding.btnRequestPermission.isVisible = false
                Toast.makeText(this, "Location permission granted.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mi_login -> {
                if (auth.currentUser == null) {
                    val intent = Intent(this@MainActivity, LoginScreen::class.java)
                    startActivity(intent)
                    return true
                } else {
                    Snackbar.make(binding.root, "Already logged in!", Snackbar.LENGTH_SHORT).show()
                    return true
                }

            }
            R.id.mi_watch_list -> {
                if (auth.currentUser == null) {
                    Snackbar.make(binding.root, "Please login to see Watch List", Snackbar.LENGTH_SHORT).show()
                    return true
                } else {
                    val intent = Intent(this@MainActivity, ListingScreen::class.java)
                    intent.putExtra("EXTRA_IS_ADDING",true)
                    startActivity(intent)
                    return true
                }
            }
            R.id.mi_logout -> {
                auth.signOut()
                Snackbar.make(binding.root, "User logged out", Snackbar.LENGTH_SHORT).show()
                return true

            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
package com.example.renter_app_g04

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.renter_app_g04.adapters.PropertyAdapter
import com.example.renter_app_g04.databinding.ListingScreenBinding
import com.example.renter_app_g04.model.Property
import com.example.renter_app_g04.model.Renter
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ListingScreen : AppCompatActivity(), ClickDetectorInterface {

    private lateinit var binding: ListingScreenBinding
    private lateinit var adapter: PropertyAdapter

    private var db = Firebase.firestore
    private lateinit var auth: FirebaseAuth

    private var properties:MutableList<Property> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ListingScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        //Action Bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Watch List"

        //Recycler View
        adapter = PropertyAdapter(properties, this)
        binding.rvListings.adapter = adapter
        binding.rvListings.layoutManager = LinearLayoutManager(this)
        binding.rvListings.addItemDecoration(
            DividerItemDecoration(
                this,
                LinearLayoutManager.VERTICAL
            )
        )
        getProfileData()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun getAllProperties(watchList: List<String>) {
        db.collection("landlordProperties")
            .get()
            .addOnSuccessListener {
                    results: QuerySnapshot ->

                Log.d("TESTING", "GET ALL PROPERTIES SUCCESS LISTENER WATCHLIST: $watchList")
                properties.clear()

                for (document: QueryDocumentSnapshot in results) {
                    val property:Property = document.toObject(Property::class.java)
                    Log.d("TESTING", "PROPERTY ID: ${property.id}")

                    if (watchList.contains(property.id)) {
                        Log.d("TESTING", "PROPERTY ADDED!")
                        properties.add(property)
                    }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w("TESTING", "Error getting documents.", exception)
            }
    }

    private fun getProfileData() {
        db.collection("renters")
            .document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener {
                    document: DocumentSnapshot ->

                Log.d("TESTING", "GET PROFILE SUCCESS LISTENER")

                val renterFromDB: Renter? = document.toObject(Renter::class.java)

                if (renterFromDB == null) {
                    Log.d("TESTING", "No results found")
                    Snackbar.make(binding.root, "ERROR: This user has no profile", Snackbar.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                } else {
                    getAllProperties(renterFromDB.watchList)
                }
            }.addOnFailureListener {
                    exception ->
                Log.w("TESTING", "Error getting documents.", exception)
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun deleteRow(position: Int) {
        properties.removeAt(position)
        val data: MutableMap<String, Any> = HashMap();
        data["watchList"] = properties.toList()
        db.collection("renters")
            .document(auth.currentUser!!.uid)
            .set(data)
            .addOnSuccessListener {
                Snackbar.make(binding.root, "Property Removed from Watch list", Snackbar.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Snackbar.make(binding.root, "FAILED ADDING TO WATCH LIST", Snackbar.LENGTH_SHORT).show()
            }
        adapter.notifyDataSetChanged()
    }
}
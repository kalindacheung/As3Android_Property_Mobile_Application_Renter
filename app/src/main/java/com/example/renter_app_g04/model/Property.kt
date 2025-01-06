package com.example.renter_app_g04.model

import com.google.firebase.firestore.DocumentId

data class Property(

    var owner: String = "",
    val imageUrl: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val monthlyRentalPrice: Double = 0.0,
    val numberOfBedRooms: Int = 1,
    val propertyAddress: String = "",
    var rented: Boolean = false,
    @DocumentId
    val id: String = ""
)
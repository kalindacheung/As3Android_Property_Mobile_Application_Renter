package com.example.renter_app_g04.model

import com.google.firebase.firestore.DocumentId

data class Renter(
    @DocumentId
    var id: String = "",
    var watchList: List<String> = mutableListOf()
)
package com.example.vehiclenumberidenticationapp.models

data class PoliceUser(
    var email: String,
    var firstName: String,
    var lastName: String,
    val fullName: String = "$firstName $lastName",
    var password: String
)
